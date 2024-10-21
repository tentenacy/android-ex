package com.tenutz.sociallogin.data

import com.tenutz.sociallogin.util.ApiResult
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface GoogleApi {

    @POST
    fun accessToken(
        @Url url: String,
        @Query("code") code: String,
        @Query("client_id") client_id: String,
        @Query("client_secret") client_secret: String,
        @Query("redirect_uri") redirect_uri: String,
        @Query("grant_type") grant_type: String,
    ): ApiResult<GetAccessTokenResponse>

    @POST
    fun refreshToken(
        @Url url: String,
        @Query("client_id") client_id: String,
        @Query("client_secret") client_secret: String,
        @Query("refresh_token") refresh_token: String,
        @Query("grant_type") grant_type: String,
    ): ApiResult<RefreshTokenResponse>
}