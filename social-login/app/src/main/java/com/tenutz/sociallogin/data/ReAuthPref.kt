package com.tenutz.sociallogin.data

import com.chibatching.kotpref.KotprefModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthCredential
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.OAuthCredential
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kakao.sdk.common.KakaoSdk.type

object ReAuthPref: KotprefModel() {

    private val gson = Gson()
    private val facebookAuthCredentialType = object: TypeToken<FacebookAuthCredential>() {}.type
    private val googleAuthCredentialType = object: TypeToken<GoogleAuthCredential>() {}.type
    private val kakaoAuthCredentialType = object: TypeToken<com.google.firebase.auth.zzf>() {}.type

    private var credentialJson: String? by nullableStringPref()
    private var provider: String? by nullableStringPref()
    var firebaseToken: String? by nullableStringPref()

    var credential: AuthCredential?
        get() = credentialJson?.let {
            when(provider) {
                "facebook" -> gson.fromJson<FacebookAuthCredential>(it, facebookAuthCredentialType)
                "google" -> gson.fromJson<GoogleAuthCredential>(it, googleAuthCredentialType)
                "kakao" -> gson.fromJson<OAuthCredential>(it, kakaoAuthCredentialType)
                else -> null
            }
        }
        set(value) {
            when(value) {
                is FacebookAuthCredential -> { provider = "facebook" }
                is GoogleAuthCredential -> { provider = "google" }
                is com.google.firebase.auth.zzf -> { provider = "kakao" }
                else -> { provider = null }
            }
            credentialJson = value?.let { gson.toJson(it) }
        }


}