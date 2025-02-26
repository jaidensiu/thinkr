package com.example.thinkr.ui.flashcards

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.thinkr.domain.FlashcardsManager
import com.example.thinkr.domain.model.DocumentItem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thinkr.R


@Composable
fun FlashcardsScreen(documentItem: DocumentItem, navController: NavController, flashcardsManager: FlashcardsManager, viewModel: FlashcardsViewModel = FlashcardsViewModel(flashcardsManager)) {
    val flashcards = flashcardsManager.getFlashcards(documentItem)
    var currentIndex by remember { mutableIntStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row {
            Image(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(48.dp)
                    .clickable { viewModel.onBackPressed(navController) }
            )
        }

        Flashcard(
        question = flashcards[currentIndex].frontQuestion,
        answer = flashcards[currentIndex].backAnswer,
        showAnswer = showAnswer,
        onSwipeRight = { showAnswer = !showAnswer },
        onSwipeLeft = { showAnswer = !showAnswer },
        onSwipeUp = {
            if (currentIndex < flashcards.size - 1) {
                currentIndex++
                showAnswer = !showAnswer
            }
        },
        onSwipeDown = {
            if (currentIndex > 0) {
                currentIndex--
                showAnswer = !showAnswer
            }
        }
        )
    }
}

@Composable
fun Flashcard(
    question: String,
    answer: String,
    showAnswer: Boolean,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
) {
    var verticalDragThreshold = 20f
    var horizontalDragThreshold = 10f
    var isDragging by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false })
                { change, dragAmount ->
                    if (!isDragging) return@detectDragGestures

                    if (dragAmount.y > verticalDragThreshold) {
                        onSwipeDown()
                        Log.d("Drag", "Down")
                        isDragging = false
                    } else if (dragAmount.y < -verticalDragThreshold) {
                        onSwipeUp()
                        Log.d("Drag", "Up")
                        isDragging = false
                    } else if (dragAmount.x > horizontalDragThreshold) {
                        onSwipeRight()
                        Log.d("Drag", "Right")
                        isDragging = false
                    } else if (dragAmount.x < -horizontalDragThreshold) {
                        onSwipeLeft()
                        Log.d("Drag", "Left")
                        isDragging = false
                    }
                    change.consume()
                }
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .border(2.dp, Color.Black, RoundedCornerShape(16.dp))
                .background(Color.LightGray, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (showAnswer) answer else question,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}
