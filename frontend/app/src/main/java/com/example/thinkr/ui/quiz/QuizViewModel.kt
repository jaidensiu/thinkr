package com.example.thinkr.ui.quiz

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class QuizViewModel : ViewModel() {
    private var _state = MutableStateFlow(QuizState())
    val state = _state.asStateFlow()

    init {
        // TODO: Replace with api call
        _state.update {
            it.copy(
                quiz = Quiz(
                    multipleChoiceQuestions = listOf(
                        Pair(
                            "What is the capital of France?",
                            listOf("Paris", "London", "Berlin", "Madrid")
                        ),
                        Pair(
                            "Which planet is known as the Red Planet?",
                            listOf("Mars", "Venus", "Jupiter", "Saturn")
                        ),
                        Pair(
                            "What is the largest mammal in the world?",
                            listOf("Blue Whale", "Elephant", "Giraffe", "Hippopotamus")
                        ),
                    ),
                    correctAnswerIndexList = listOf(0, 1, 2)
                ),
                totalTimeSeconds = 20,
                selectedAnswerIndices = List(3) { -1 },
                started = false,
                revealAnswer = false,
                totalScore = 0
            )
        }
    }

    fun onBackPressed(navController: NavController) {
        navController.popBackStack()
    }

    fun onStartQuiz() {
        _state.update {
            it.copy(started = true)
        }
    }

    fun onQuizTimeUp() {
        _state.update {
            it.copy(
                revealAnswer = true,
                totalScore = it.selectedAnswerIndices.mapIndexed { index, selectedIndex ->
                    if (selectedIndex == it.quiz.correctAnswerIndexList[index]) 1 else 0
                }.reduce { sum, element -> sum + element }
            )
        }
    }

    fun onAnswerSelected(questionIndex: Int, answerIndex: Int) {
        _state.update {
            it.copy(
                selectedAnswerIndices = it.selectedAnswerIndices.toMutableList().apply {
                    this[questionIndex] = answerIndex
                }
            )
        }
    }
}

private fun vibrate(context: Context) {
    val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(200)
    }
}
