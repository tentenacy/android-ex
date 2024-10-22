package com.tenutz.firestorecrud.data

data class ChatCompletionResponse(
    val id: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val index: Int,
    val message: MessageResponse,
    val logprobs: Any?, // null일 수 있으므로 Any? 타입
    val finish_reason: String
)

data class MessageResponse(
    val role: String,
    val content: String,
    val refusal: Any? // null일 수 있으므로 Any? 타입
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int,
    val prompt_tokens_details: PromptTokensDetails,
    val completion_tokens_details: CompletionTokensDetails
)

data class PromptTokensDetails(
    val cached_tokens: Int
)

data class CompletionTokensDetails(
    val reasoning_tokens: Int
)