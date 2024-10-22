package com.tenutz.firestorecrud.di.module

import com.facebook.CallbackManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AuthSingletonModule {

    @Provides
    fun provideFacebookCallbackManager(): CallbackManager {
        return CallbackManager.Factory.create()
    }
}