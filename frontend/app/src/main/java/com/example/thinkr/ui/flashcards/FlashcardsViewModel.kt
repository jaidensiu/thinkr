package com.example.thinkr.ui.flashcards

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.thinkr.domain.FlashcardsManager

class FlashcardsViewModel(private val flashcardsManager: FlashcardsManager) : ViewModel() {
    fun onBackPressed(navController: NavController) {
        navController.popBackStack()
    }

}