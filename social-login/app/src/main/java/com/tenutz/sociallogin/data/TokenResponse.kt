package com.tenutz.sociallogin.data

data class TokenResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val expiresIn: Long?,
    val type: String? = null,
    val state: String? = null,
)