package com.example.thinkr.ui.quiz

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thinkr.R
import com.example.thinkr.domain.model.DocumentItem
import com.example.thinkr.ui.shared.AnimatedCardDeck
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel


data class Quiz(
    var multipleChoiceQuestions: List<Pair<String, List<String>>> = emptyList(),
    var correctAnswerIndexList: List<Int> = emptyList()
)

@Composable
fun QuizScreen(
    document: DocumentItem,
    navController: NavController,
    viewModel: QuizViewModel = koinViewModel()
) {
    val totalTimeSeconds = 20
    val quiz = Quiz(
        multipleChoiceQuestions = listOf(
            Pair("What is the capital of France?", listOf("Paris", "London", "Berlin", "Madrid")),
            Pair("Which planet is known as the Red Planet?", listOf("Mars", "Venus", "Jupiter", "Saturn")),
            Pair("What is the largest mammal in the world?", listOf("Blue Whale", "Elephant", "Giraffe", "Hippopotamus")),
        ),
        correctAnswerIndexList = listOf(0, 1, 2)
    )

    val context = LocalContext.current

    val selectedAnswerIndices = remember { mutableStateListOf<Int>().apply { repeat(quiz.multipleChoiceQuestions.size) { add(-1) } } }

    var started by remember { mutableStateOf(value = false) }
    var revealAnswer by remember { mutableStateOf(value = false) }
    var totalScore by remember { mutableIntStateOf(value = 0) }

    val frontBackPairs: List<Pair<@Composable () -> Unit, @Composable () -> Unit>> = remember {
        quiz.multipleChoiceQuestions.mapIndexed { index, questionAnswerPair ->
            Pair(
                // First composable function
                {
                    MultipleChoiceQuizCard(
                        questionIndex = index,
                        question = questionAnswerPair.first,
                        choices = questionAnswerPair.second,
                        correctAnswerIndex = quiz.correctAnswerIndexList[index],
                        revealAnswer = revealAnswer,
                        selectedAnswerIndices = selectedAnswerIndices,
                    )
                },
                // Second composable function (back) Empty composable
                { }
            )
        }
    }

    if (!started) {
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

            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { started = true }) {
                    Text(text = "Start Quiz")
                }
            }
        }
    } else {
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

            Spacer(modifier = Modifier.height(16.dp))

            QuizTimer(
                totalTimeSeconds = totalTimeSeconds,
                onTimeUp = {
                    revealAnswer = true
                    vibrate(context)
                    totalScore = 0
                    selectedAnswerIndices.forEachIndexed { index, selectedIndex ->
                        Log.d("Quiz", "Question: $index, Selected Index: $selectedIndex, Correct Index: ${quiz.correctAnswerIndexList[index]}")
                        if (selectedIndex == quiz.correctAnswerIndexList[index]) {
                            totalScore++
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (revealAnswer) {
                Text(
                    text = "Score: $totalScore / ${quiz.multipleChoiceQuestions.size}",
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
    questionIndex: Int,
    question: String,
    choices: List<String>,
    correctAnswerIndex: Int,
    revealAnswer: Boolean = false,
    selectedAnswerIndices: MutableList<Int>
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
            val isSelected = selectedAnswerIndices.get(questionIndex) == index
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
                                    selectedAnswerIndices[questionIndex] = -1
                                } else {
                                    selectedAnswerIndices[questionIndex] = index
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
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
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