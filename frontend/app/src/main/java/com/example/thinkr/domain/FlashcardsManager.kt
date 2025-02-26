package com.example.thinkr.domain

import com.example.thinkr.domain.model.DocumentItem
import com.example.thinkr.domain.model.FlashcardItem

class FlashcardsManager {
    fun getFlashcards(documentItem: DocumentItem): List<FlashcardItem> {
        // TODO: replace with get request
        return listOf(
            FlashcardItem("Question 1", "Answer 1"),
            FlashcardItem("Question 2", "Answer 2"),
            FlashcardItem("Question 3", "Answer 3")
        )
    }
}