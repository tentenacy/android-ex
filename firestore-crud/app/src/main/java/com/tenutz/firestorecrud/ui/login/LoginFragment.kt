package com.tenutz.firestorecrud.ui.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tenutz.firestorecrud.BuildConfig
import com.tenutz.firestorecrud.auth.FacebookOAuthLoginCallback
import com.tenutz.firestorecrud.auth.GoogleOAuthLoginCallback
import com.tenutz.firestorecrud.auth.KakaoOAuthLoginCallback
import com.tenutz.firestorecrud.auth.NaverOAuthLoginCallback
import com.tenutz.firestorecrud.databinding.FragmentLoginBinding
import com.tenutz.firestorecrud.util.EVENT_NAVIGATE
import com.tenutz.firestorecrud.util.TAG_AUTH
import com.tenutz.firestorecrud.util.loginWithKakaoMethod
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class LoginFragment: Fragment() {

    private var _binding: FragmentLoginBinding? = null
    val binding get() = _binding!!

    private val vm by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    @Inject
    lateinit var callbackManager: CallbackManager

    @Inject
    lateinit var naverOAuthLoginCallback: NaverOAuthLoginCallback

    @Inject
    lateinit var kakaoOAuthLoginCallback: KakaoOAuthLoginCallback

    @Inject
    lateinit var googleOAuthLoginCallback: GoogleOAuthLoginCallback

    @Inject
    lateinit var facebookOAuthLoginCallback: FacebookOAuthLoginCallback


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentLoginBinding.inflate(inflater, container, false).apply {
            _binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        vm.viewEvent.observe(viewLifecycleOwner, { event ->
            event?.getContentIfNotHandled()?.let {
                when(it.first) {
                    EVENT_NAVIGATE -> {
                        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToProfileFragment())
                    }
                }
            }
        })

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

    override fun onStart() {
        super.onStart()

        val authority = arguments?.getString("authority")
        if(authority == "login-callback") {
            naverOAuthLoginCallback()
        }

        Firebase.auth.currentUser?.let {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToProfileFragment())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun naverLogin() {
        val baseUri = BuildConfig.SOCIAL_NAVER_AUTH_URL
        val clientId = BuildConfig.SOCIAL_NAVER_CLIENT_ID
        val redirectUri = BuildConfig.SOCIAL_NAVER_AUTH_CALLBACK_URI

        // URL-safe Base64로 인코딩
        // 16바이트 길이의 임의의 바이트 배열 생성
        val stateString = Base64.encodeToString(ByteArray(16) { Random.nextInt(0, 256).toByte() }, Base64.NO_PADDING)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$baseUri?response_type=code&client_id=$clientId&redirect_uri=$redirectUri&state=$stateString"))
        startActivity(intent)
    }

    private fun kakaoLogin() {
        loginWithKakaoMethod(kakaoOAuthLoginCallback)
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
                val response = credentialManager.getCredential(requireContext(), request)
                googleOAuthLoginCallback(response)
            } catch (e: GetCredentialException) {
                Log.e(TAG_AUTH, "구글 로그인 실패", e)
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
            registerCallback(callbackManager, facebookOAuthLoginCallback)
        }
    }
}