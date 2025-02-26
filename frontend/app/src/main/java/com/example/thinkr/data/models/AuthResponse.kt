package com.example.thinkr.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val data: AuthData
)
