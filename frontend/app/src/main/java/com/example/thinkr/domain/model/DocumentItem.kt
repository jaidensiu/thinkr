package com.example.thinkr.domain.model

import android.net.Uri
import kotlinx.serialization.Serializable

@Serializable
data class DocumentItem(
    val name: String,
    val uploadCompleted: Boolean = true,
    //TODO: Add more as needed
)
