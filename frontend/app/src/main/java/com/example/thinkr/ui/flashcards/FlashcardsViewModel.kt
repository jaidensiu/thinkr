package com.example.thinkr.ui.flashcards

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.thinkr.data.models.Document
import com.example.thinkr.data.models.FlashcardItem
import com.example.thinkr.data.repositories.FlashcardsRepositoryImpl

class FlashcardsViewModel(private val flashcardsRepositoryImpl: FlashcardsRepositoryImpl) : ViewModel() {
    fun onBackPressed(navController: NavController) {
        navController.popBackStack()
    }

    fun getFlashcards(documentItem: Document): List<FlashcardItem> {
        return flashcardsRepositoryImpl.getFlashcards(documentItem)
    }
}
