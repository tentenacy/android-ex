package com.tenutz.firestorecrud.auth

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tenutz.firestorecrud.ui.login.LoginViewModel
import com.tenutz.firestorecrud.util.TAG_AUTH

class NaverOAuthLoginCallback(
    private val fragment: Fragment,
): () -> Unit {

    private val viewModel: Loginable by lazy {
        ViewModelProvider(fragment)[LoginViewModel::class.java]
    }

    override fun invoke() {

        val firebaseToken = viewModel.savedStateHandle.get<String>("firebaseToken")
        val name = viewModel.savedStateHandle.get<String>("name")
        val profileImage = viewModel.savedStateHandle.get<String>("profileImage")

        Log.d(TAG_AUTH, "네이버 로그인 성공")
        Log.d(TAG_AUTH, "firebaseToken: $firebaseToken")
        Log.d(TAG_AUTH, "name: $name")
        Log.d(TAG_AUTH, "profileImage: $profileImage")

        viewModel.signInWithCustomToken(firebaseToken!!)
    }
}