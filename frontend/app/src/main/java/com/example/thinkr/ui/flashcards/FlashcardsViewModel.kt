package com.example.thinkr.ui.flashcards

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.thinkr.domain.FlashcardsManager
import com.example.thinkr.domain.model.DocumentItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FlashcardsViewModel(private val flashcardsManager: FlashcardsManager) : ViewModel() {
    private val _state = MutableStateFlow(FlashcardsState())
    val state = _state.asStateFlow()

    fun onStart(documentItem: DocumentItem) {
        _state.update { it.copy(flashcards = flashcardsManager.getFlashcards(documentItem)) }
    }

    fun onBackPressed(navController: NavController) {
        navController.popBackStack()
    }
}
