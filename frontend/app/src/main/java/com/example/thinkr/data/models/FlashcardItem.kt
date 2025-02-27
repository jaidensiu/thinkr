package com.example.thinkr.data.models

import kotlinx.serialization.Serializable

@Serializable
data class FlashcardItem (
    val frontQuestion: String,
    val backAnswer: String,
)