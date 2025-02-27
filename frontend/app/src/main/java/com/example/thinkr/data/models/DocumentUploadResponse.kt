package com.example.thinkr.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UploadResponse(
    val data: DocumentData
)

@Serializable
data class DocumentData(
    val docs: DocumentDetails
)

@Serializable
data class DocumentDetails(
    val documentId: String,
    val uploadTime: String,
    val activityGenerationComplete: Boolean
)
