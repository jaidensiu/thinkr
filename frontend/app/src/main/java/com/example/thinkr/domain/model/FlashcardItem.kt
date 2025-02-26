package com.example.thinkr.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FlashcardItem (
    val frontQuestion: String,
    val backAnswer: String,
)