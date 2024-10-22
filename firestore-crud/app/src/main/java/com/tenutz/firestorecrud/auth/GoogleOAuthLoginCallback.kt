package com.tenutz.firestorecrud.auth

import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.tenutz.firestorecrud.ui.login.LoginViewModel
import com.tenutz.firestorecrud.util.TAG_AUTH

class GoogleOAuthLoginCallback(private val fragment: Fragment): (GetCredentialResponse) -> Unit {

    private val viewModel: Loginable by lazy {
        ViewModelProvider(fragment).get(LoginViewModel::class.java)
    }

    override fun invoke(response: GetCredentialResponse) {
        when (val credential = response.credential) {
            is CustomCredential -> {
                // If you are also using any external sign-in libraries, parse them
                // here with the utility functions provided.
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)

                    Log.d(TAG_AUTH, "구글 로그인 성공: $authCredential")

                    viewModel.signInWithCredential(authCredential, "google")
                }
            }
        }
    }
}