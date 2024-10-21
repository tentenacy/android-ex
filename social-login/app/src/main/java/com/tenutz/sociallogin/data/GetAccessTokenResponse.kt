package com.tenutz.sociallogin.data

data class GetAccessTokenResponse(
    val access_token: String? = null,
    val expires_in: Long? = null,
    val token_type: String? = null,
    val scope: String? = null,
    val refresh_token: String? = null,
)