package com.tenutz.sociallogin.data

data class RefreshTokenResponse(
    val access_token: String? = null,
    val expires_in: Long? = null,
    val scope: String? = null,
    val token_type: String? = null,
)
