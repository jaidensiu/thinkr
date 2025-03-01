package com.example.thinkr.data.remote

import com.example.thinkr.data.models.AuthResponse

interface RemoteApi {
    suspend fun postAuthBearerToken(token: String): AuthResponse
}
