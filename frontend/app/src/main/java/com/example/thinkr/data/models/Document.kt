package com.example.thinkr.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Document(
    val documentId: String,
    val documentName: String,
    val uploadTime: String,
    val activityGenerationComplete: Boolean
)
