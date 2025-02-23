package com.example.thinkr.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DocumentItem(
    val username: String,
    val name: String,
    //TODO: Add more as needed
)