package com.example.thinkr.ui.quiz

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thinkr.domain.model.DocumentItem
import com.example.thinkr.ui.shared.AnimatedCardDeck
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    document: DocumentItem,
    navController: NavController,
    viewModel: QuizViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current

    val frontBackPairs: List<Pair<@Composable () -> Unit, @Composable () -> Unit>> = remember {
        state.quiz.multipleChoiceQuestions.mapIndexed { index, questionAnswerPair ->
            Pair(
                // First composable function
                {
                    MultipleChoiceQuizCard(
                        quizState = state,
                        quizViewModel = viewModel,
                        questionIndex = index,
                        question = questionAnswerPair.first,
                        choices = questionAnswerPair.second,
                        correctAnswerIndex = state.quiz.correctAnswerIndexList[index],
                        revealAnswer = state.revealAnswer,
                    )
                },
                // Second composable function (back) Empty composable
                { }
            )
        }
    }

    if (!state.started) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            TopAppBar(
                title = { Text("Quiz") },
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
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { viewModel.onStartQuiz() }) {
                    Text(text = "Start Quiz")
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            TopAppBar(
                title = { Text("Quiz") },
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

            Spacer(modifier = Modifier.height(16.dp))

            QuizTimer(
                totalTimeSeconds = state.totalTimeSeconds,
                onTimeUp = {
                    viewModel.onQuizTimeUp()
                    vibrate(context)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.revealAnswer) {
                Text(
                    text = "Score: ${state.totalScore} / ${state.quiz.multipleChoiceQuestions.size}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedCardDeck(
                    cardSize = 500.dp,
                    frontBackPairs = frontBackPairs,
                    enableHorizontalSwipe = false
                )
            }
        }
    }
}

@Composable
fun MultipleChoiceQuizCard(
    quizState: QuizState,
    quizViewModel: QuizViewModel,
    questionIndex: Int,
    question: String,
    choices: List<String>,
    correctAnswerIndex: Int,
    revealAnswer: Boolean = false,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // Question
        Text(
            text = question,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // Choices
        choices.forEachIndexed { index, choice ->
            val isSelected = quizState.selectedAnswerIndices.get(questionIndex) == index
            val isCorrect = index == correctAnswerIndex
            val backgroundColor = when {
                !revealAnswer -> if (isSelected) Color.Gray else Color.Transparent
                isSelected && isCorrect -> Color(0xFFD0F0D0) // Light green
                isSelected && !isCorrect -> Color(0xFFF0D0D0) // Light red
                isCorrect -> Color(0xFFD0F0D0) // Show correct answer
                else -> Color.Transparent
            }
            val borderColor = when {
                !revealAnswer && isSelected -> Color.Black
                !revealAnswer && !isSelected -> Color.Gray
                isSelected && isCorrect -> Color.Green
                else -> Color.Gray
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            if (!revealAnswer) {
                                if (isSelected) {
                                    quizViewModel.onAnswerSelected(questionIndex, -1)
                                } else {
                                    quizViewModel.onAnswerSelected(questionIndex, index)
                                }
                            }
                        }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${('A' + index)}.",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = choice,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuizTimer(
    totalTimeSeconds: Int,
    onTimeUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var timeRemaining by remember { mutableStateOf(totalTimeSeconds) }
    var isRunning by remember { mutableStateOf(true) }

    // Calculate the progress (1.0 -> 0.0 as time passes)
    val progress = timeRemaining.toFloat() / totalTimeSeconds.toFloat()

    // Animate the progress value for smooth transitions
    val animatedProgress by animateFloatAsState(targetValue = progress)

    // Interpolate between green and red based on progress
    val startColor = Color(0xFF4CAF50) // Green
    val endColor = Color(0xFFF44336)   // Red
    val currentColor = lerp(endColor, startColor, animatedProgress)

    // Timer logic
    LaunchedEffect(key1 = timeRemaining, key2 = isRunning) {
        if (isRunning && timeRemaining > 0) {
            delay(1000) // Update every second
            timeRemaining--
        } else if (timeRemaining <= 0) {
            isRunning = false
            onTimeUp()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Time Remaining:",
            fontWeight = FontWeight.Medium
        )

        Text(
            text = formatTime(timeRemaining),
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Timer progress bar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.LightGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(currentColor)
        )
    }
}

// Helper function to format seconds as MM:SS
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
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
