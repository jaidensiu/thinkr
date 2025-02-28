package com.example.thinkr.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController

class QuizViewModel: ViewModel() {
    fun onBackPressed(navController: NavController) {
        navController.popBackStack()
    }
}