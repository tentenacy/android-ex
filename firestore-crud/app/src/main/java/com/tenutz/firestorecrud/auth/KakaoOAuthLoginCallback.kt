package com.tenutz.firestorecrud.auth

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.oAuthCredential
import com.kakao.sdk.auth.model.OAuthToken
import com.tenutz.firestorecrud.ui.login.LoginViewModel
import com.tenutz.firestorecrud.util.TAG_AUTH

class KakaoOAuthLoginCallback(private val fragment: Fragment): (OAuthToken?, Throwable?) -> Unit {

    private val viewModel: Loginable by lazy {
        ViewModelProvider(fragment)[LoginViewModel::class.java]
    }

    override fun invoke(token: OAuthToken?, error: Throwable?) {
        error?.let { onError(it) }
        token?.let { onSuccess(it) }
        onCancel()
    }

    private fun onError(error: Throwable?)  {
        Log.e(TAG_AUTH, "카카오 로그인 실패", error)
    }

    private fun onSuccess(token: OAuthToken) {

        Log.d(TAG_AUTH, "카카로 로그인 성공: $token")

        val providerId = "oidc.firebasecrud"
        val authCredential = oAuthCredential(providerId) {
            idToken = token.idToken
            accessToken = token.accessToken
        }
        viewModel.signInWithCredential(authCredential, "kakao")
    }

    private fun onCancel() {
        Log.d(TAG_AUTH, "KakaoOAuthLoginCallback:cancel")
    }
}