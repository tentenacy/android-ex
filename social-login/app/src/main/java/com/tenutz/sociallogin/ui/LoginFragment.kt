package com.tenutz.sociallogin.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.*
import com.google.firebase.firestore.firestore
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.tenutz.sociallogin.BuildConfig
import com.tenutz.sociallogin.data.ReAuthPref
import com.tenutz.sociallogin.data.TokenResponse
import com.tenutz.sociallogin.databinding.FragmentLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class LoginFragment: Fragment() {

    companion object {
        private val TAG = LoginFragment::class.java.simpleName
    }

    private var _binding: FragmentLoginBinding? = null
    val binding: FragmentLoginBinding get() = _binding!!

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val callbackManager = CallbackManager.Factory.create()

    private val naverLoginCallback: () -> Unit = {
        val firebaseToken = arguments?.getString("firebaseToken")
        val name = arguments?.getString("name")
        val profileImage = arguments?.getString("profileImage")

        Log.d(TAG, "firebaseToken: $firebaseToken")
        Log.d(TAG, "name: $name")
        Log.d(TAG, "profileImage: $profileImage")

        updateInfoUI(TokenResponse(null, null, null))
        ReAuthPref.firebaseToken = firebaseToken

        auth.signInWithCustomToken(firebaseToken!!).signIn()
    }

    private val kakaoLoginCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "카카오계정으로 로그인 실패", error)
        } else if (token != null) {
            Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
            updateInfoUI(TokenResponse(token.accessToken, token.refreshToken, token.accessTokenExpiresAt.time))
            val authCredential = kakaoCredential(token)
            ReAuthPref.credential = authCredential

            auth.signInWithCredential(authCredential).signIn()
        }
    }

    private val googleLoginCallback: (GetCredentialResponse) -> Unit = { result ->
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                // If you are also using any external sign-in libraries, parse them
                // here with the utility functions provided.
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    CoroutineScope(Dispatchers.Main).launch {
                        updateInfoUI(TokenResponse(idToken, null, null))
                    }
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    ReAuthPref.credential = authCredential

                    auth.signInWithCredential(authCredential).signIn()
                }
            }
        }
    }

    private val facebookLoginCallback = object : FacebookCallback<LoginResult> {
        override fun onCancel() {
            Log.e(TAG, "Login canceled")
        }

        override fun onError(error: FacebookException) {
            Log.e(TAG, "${error.message}")
        }

        override fun onSuccess(result: LoginResult) {
            val token = result.accessToken.token

            updateInfoUI(TokenResponse(result.accessToken.token, null, result.accessToken.expires.time))
            val authCredential = FacebookAuthProvider.getCredential(token)
            ReAuthPref.credential = authCredential

            auth.signInWithCredential(authCredential).signIn()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentLoginBinding.inflate(inflater, container, false).apply {
            _binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authority = arguments?.getString("authority")

        when(authority) {
            "login-callback" -> {
                naverLoginCallback()
            }
        }

        binding.btnLoginNaverlogin.setOnClickListener {
            naverLogin()
        }
        binding.btnLoginKakaologin.setOnClickListener {
            kakaoLogin()
        }
        binding.btnLoginGooglelogin.setOnClickListener {
            googleLogin()
        }
        binding.btnLoginFacebooklogin.setOnClickListener {
            facebookLogin()
        }
    }

    private fun updateInfoUI(token: TokenResponse) {
        binding.tvAccessToken.text = "access_token: ${token.accessToken}"
        binding.tvRefreshToken.text = "refresh_token: ${token.refreshToken}"
        binding.tvExpires.text = "expires_in: ${token.expiresIn}"
        binding.tvType.text = "type: ${token.type}"
        binding.tvState.text = "state: ${token.state}"
    }

    private fun naverLogin() {
        val baseUri = "https://nid.naver.com/oauth2.0/authorize"
        val clientId = BuildConfig.SOCIAL_NAVER_CLIENT_ID
        val redirectUri = "https://us-central1-social-login-8d3c7.cloudfunctions.net/naver_login_callback"

        // URL-safe Base64로 인코딩
        // 16바이트 길이의 임의의 바이트 배열 생성
        val stateString = Base64.encodeToString(ByteArray(16) { Random.nextInt(0, 256).toByte() }, Base64.NO_PADDING)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$baseUri?response_type=code&client_id=$clientId&redirect_uri=$redirectUri&state=$stateString"))
        startActivity(intent)
    }

    private fun kakaoLogin() {
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireContext())) {
            UserApiClient.instance.loginWithKakaoTalk(requireContext()) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(requireContext(), callback = kakaoLoginCallback)
                } else if (token != null) {
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                    updateInfoUI(TokenResponse(token.accessToken, token.refreshToken, token.accessTokenExpiresAt.time))

                    val authCredential = kakaoCredential(token)
                    ReAuthPref.credential = authCredential

                    auth.signInWithCredential(authCredential).signIn()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(requireContext(), callback = kakaoLoginCallback)
        }
    }

    private fun googleLogin() {

        val credentialManager = CredentialManager.create(requireContext())

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.SOCIAL_GOOGLE_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val credential = credentialManager.getCredential(requireContext(), request)
                googleLoginCallback(credential)
            } catch (e: GetCredentialException) {
                Log.e(TAG, e.errorMessage.toString())
            }
        }
    }

    private fun facebookLogin() {
        LoginManager.getInstance().run {
            logInWithReadPermissions(
                this@LoginFragment,
                listOf(
                    "email",
                    "public_profile"
                )
            )
            registerCallback(callbackManager, facebookLoginCallback)
        }
    }

    private fun kakaoCredential(token: OAuthToken): AuthCredential {
        val providerId = "oidc.sociallogin"
        val credential = oAuthCredential(providerId) {
            idToken = token.idToken
            accessToken = token.accessToken
        }
        return credential
    }

    private fun Task<AuthResult>.signIn() {
        addOnSuccessListener { result ->
            Log.i(TAG, "파이어베이스로 로그인 성공")
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToMainFragment())
        }
        .addOnFailureListener {
            Log.e(TAG, "파이어베이스로 로그인 실패")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null) {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToMainFragment())
        }
    }


}