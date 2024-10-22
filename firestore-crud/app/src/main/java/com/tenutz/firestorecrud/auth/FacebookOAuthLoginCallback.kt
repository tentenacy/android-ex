package com.tenutz.firestorecrud.auth

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.tenutz.firestorecrud.ui.login.LoginViewModel
import com.tenutz.firestorecrud.util.TAG_AUTH

class FacebookOAuthLoginCallback(private val fragment: Fragment): FacebookCallback<LoginResult> {

    private val viewModel: Loginable by lazy {
        ViewModelProvider(fragment)[LoginViewModel::class.java]
    }

    override fun onCancel() {
        Log.d(TAG_AUTH, "FacebookOAuthLoginCallback:cancel")
    }

    override fun onError(error: FacebookException) {
        Log.e(TAG_AUTH, "페이스북 로그인 실패", error)
    }

    override fun onSuccess(result: LoginResult) {

        Log.d(TAG_AUTH, "페이스북 로그인 성공: $result")

        val token = result.accessToken.token

        val authCredential = FacebookAuthProvider.getCredential(token)

        viewModel.signInWithCredential(authCredential, "facebook")
    }
}