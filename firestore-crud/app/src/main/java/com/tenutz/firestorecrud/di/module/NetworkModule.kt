package com.tenutz.firestorecrud.di.module

import com.tenutz.firestorecrud.BuildConfig
import com.tenutz.firestorecrud.data.OpenAIApi
import com.tenutz.sociallogin.util.ResultCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Singleton
    @Provides
    fun provideRetrofitBuilder(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        resultCallAdapterFactory: ResultCallAdapterFactory,
    ): Retrofit.Builder {

        val client = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        return Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(resultCallAdapterFactory)
            .client(client)
    }

    @Singleton
    @Provides
    fun provideOpenAIApi(builder: Retrofit.Builder): OpenAIApi {
        return builder
            .baseUrl(BuildConfig.OPEN_API_BASE_URL)
            .build()
            .create()
    }
}