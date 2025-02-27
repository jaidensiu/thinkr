package com.example.thinkr.data.repositories

import com.example.thinkr.data.models.DocumentItem
import com.example.thinkr.data.models.FlashcardItem

interface FlashcardsRepository {
    fun getFlashcards(documentItem: DocumentItem): List<FlashcardItem>
}