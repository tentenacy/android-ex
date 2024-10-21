package com.tenutz.sociallogin.util

import android.content.Context
import com.tenutz.sociallogin.data.GoogleApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

object RetrofitInstance {

    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    private val retrofit: Retrofit by lazy {

        val httpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .client(client)    // Logcat에서 패킷 내용을 로그로 남기는 속성
            .build()
    }

    val googleApi: GoogleApi by lazy {
        retrofit.create()
    }

}