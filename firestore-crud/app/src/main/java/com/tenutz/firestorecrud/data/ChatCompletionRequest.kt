package com.tenutz.firestorecrud.data

data class ChatCompletionRequest(
    val model: String,
    val messages: List<MessageRequest>
)

data class MessageRequest(
    val role: String,
    val content: String
)