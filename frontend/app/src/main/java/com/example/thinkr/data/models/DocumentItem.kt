package com.example.thinkr.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DocumentItem(
    val name: String,
    val uploadCompleted: Boolean = true,
    //TODO: Add more as needed
)
