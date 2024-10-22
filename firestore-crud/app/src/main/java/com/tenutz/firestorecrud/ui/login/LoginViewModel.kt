package com.tenutz.firestorecrud.ui.login

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tenutz.firestorecrud.auth.Loginable
import com.tenutz.firestorecrud.data.User
import com.tenutz.firestorecrud.ui.base.BaseViewModel
import com.tenutz.firestorecrud.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val _savedStateHandle: SavedStateHandle,
) : BaseViewModel(), Loginable {

    override val savedStateHandle: SavedStateHandle
        get() = _savedStateHandle

    override fun signInWithCustomToken(customToken: String) {
        Firebase.auth.signInWithCustomToken(customToken)
            .addOnSuccessListener { authResult ->

                Log.d(TAG_AUTH, "파이어베이스 로그인 성공: $authResult")

                updateUser("naver")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG_AUTH, "파이어베이스 로그인 실패", exception)
            }
    }

    override fun signInWithCredential(credential: AuthCredential, provider: String) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->

                Log.d(TAG_AUTH, "파이어베이스 로그인 성공: $authResult")

                updateUser(provider)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG_AUTH, "파이어베이스 로그인 실패", exception)
            }
    }

    private fun updateUser(provider: String)  = viewModelScope.launch(Dispatchers.IO) {

        val newUser = hashMapOf(
            "uid" to user!!.uid,
            "displayName" to user!!.displayName,
            "provider" to provider,
        )

        db.collection("users")
            .document(user!!.uid)
            .set(newUser)
            .addOnSuccessListener {
                User.provider = provider
                viewEvent(EVENT_NAVIGATE to Unit)
            }
            .addOnFailureListener { exception ->
                auth.signOut()
            }
            .await()
    }
}