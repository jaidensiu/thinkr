package com.example.thinkr.data.repositories

import com.example.thinkr.data.models.Document
import com.example.thinkr.data.models.FlashcardItem

interface FlashcardsRepository {
    fun getFlashcards(documentItem: Document): List<FlashcardItem>
}