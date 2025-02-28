package com.example.thinkr.ui.flashcards

import com.example.thinkr.domain.model.FlashcardItem

data class FlashcardsState (
    var flashcards: List<FlashcardItem> = emptyList()
)