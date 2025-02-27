package com.example.thinkr.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthData(
    val token: String,
    val user: User
)
