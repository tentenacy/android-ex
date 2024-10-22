package com.tenutz.firestorecrud.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIApi {
    @POST("v1/chat/completions")
    fun generateText(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest,
    ): Call<ChatCompletionResponse>
}