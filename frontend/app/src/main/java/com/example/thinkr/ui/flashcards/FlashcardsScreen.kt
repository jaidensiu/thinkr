package com.example.thinkr.ui.flashcards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thinkr.domain.FlashcardsManager
import com.example.thinkr.domain.model.DocumentItem
import com.example.thinkr.ui.shared.AnimatedCardDeck

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    documentItem: DocumentItem,
    navController: NavController,
    flashcardsManager: FlashcardsManager,
    viewModel: FlashcardsViewModel = FlashcardsViewModel(flashcardsManager)
) {
    viewModel.onStart(documentItem)
    val state by viewModel.state.collectAsState()

    val frontBackPairs: List<Pair<@Composable () -> Unit, @Composable () -> Unit>> = remember {
        state.flashcards.map { flashcard ->
            Pair(
                // First composable function (front)
                {
                    Text(
                        text = flashcard.frontQuestion,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(16.dp)
                    )
                },
                // Second composable function (back)
                {
                    Text(
                        text = flashcard.backAnswer,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        TopAppBar(
            title = { Text("Flashcards") },
            navigationIcon = {
                IconButton(
                    onClick = { viewModel.onBackPressed(navController) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedCardDeck(
                frontBackPairs = frontBackPairs,
                enableHorizontalSwipe = true
            )
        }
    }
}
