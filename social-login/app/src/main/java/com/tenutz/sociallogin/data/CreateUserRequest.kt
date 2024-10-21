package com.tenutz.sociallogin.data

data class CreateUserRequest(
    val accessToken: String,
    val refreshToken: String?,
    val provider: String,
)
