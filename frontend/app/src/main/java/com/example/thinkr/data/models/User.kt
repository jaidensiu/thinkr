package com.example.thinkr.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val email: String,
    val name: String,
    val googleId: String,
    val userId: String,
    val subscribed: Boolean
)
