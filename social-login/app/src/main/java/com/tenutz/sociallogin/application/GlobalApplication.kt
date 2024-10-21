package com.tenutz.sociallogin.application

import android.app.Application
import com.chibatching.kotpref.Kotpref
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.FirebaseApp
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK
import com.tenutz.sociallogin.BuildConfig

class GlobalApplication: Application() {

    override fun onCreate() {

        super.onCreate()

        FirebaseApp.initializeApp(this)
        NaverIdLoginSDK.initialize(applicationContext, BuildConfig.SOCIAL_NAVER_CLIENT_ID, BuildConfig.SOCIAL_NAVER_CLIENT_SECRET, BuildConfig.SOCIAL_NAVER_CLIENT_NAME)
        KakaoSdk.init(this, BuildConfig.SOCIAL_KAKAO_CLIENT_ID)
        FacebookSdk.sdkInitialize(this);
        AppEventsLogger.activateApp(this);
        Kotpref.init(this)
    }
}