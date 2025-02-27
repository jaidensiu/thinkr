package com.example.thinkr.ui.flashcards

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thinkr.R
import com.example.thinkr.data.models.Document
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.thinkr.data.models.FlashcardItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FlashcardsScreen(documentItem: Document, navController: NavController, viewModel: FlashcardsViewModel) {
    val flashcards = viewModel.getFlashcards(documentItem)
    var currentIndex by remember { mutableIntStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }

    // Animation states
    var isFlipping by remember { mutableStateOf(false) }
    var isChangingCard by remember { mutableStateOf(false) }
    var targetIndex by remember { mutableIntStateOf(currentIndex) }
    var slideDirection by remember { mutableStateOf(0) } // -1 for up, 1 for down

    val coroutineScope = rememberCoroutineScope()

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
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedFlashcard(
                currentFlashcard = flashcards[currentIndex],
                showAnswer = showAnswer,
                isFlipping = isFlipping,
                isChangingCard = isChangingCard,
                slideDirection = slideDirection,
                onSwipeRight = {
                    if (!isFlipping && !isChangingCard) {
                        coroutineScope.launch {
                            isFlipping = true
                            delay(300) // Allow animation to play
                            showAnswer = !showAnswer
                            delay(50)
                            isFlipping = false
                        }
                    }
                },
                onSwipeLeft = {
                    if (!isFlipping && !isChangingCard) {
                        coroutineScope.launch {
                            isFlipping = true
                            delay(300) // Allow animation to play
                            showAnswer = !showAnswer
                            delay(50)
                            isFlipping = false
                        }
                    }
                },
                onSwipeUp = {
                    if (currentIndex < flashcards.size - 1 && !isChangingCard && !isFlipping) {
                        coroutineScope.launch {
                            slideDirection = -1
                            isChangingCard = true
                            targetIndex = currentIndex + 1
                            delay(300) // Allow animation to play
                            currentIndex = targetIndex
                            showAnswer = false
                            delay(50)
                            isChangingCard = false
                        }
                    }
                },
                onSwipeDown = {
                    if (currentIndex > 0 && !isChangingCard && !isFlipping) {
                        coroutineScope.launch {
                            slideDirection = 1
                            isChangingCard = true
                            targetIndex = currentIndex - 1
                            delay(300) // Allow animation to play
                            currentIndex = targetIndex
                            showAnswer = false
                            delay(50)
                            isChangingCard = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AnimatedFlashcard(
    currentFlashcard: FlashcardItem,
    showAnswer: Boolean,
    isFlipping: Boolean,
    isChangingCard: Boolean,
    slideDirection: Int,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit
) {
    // Animation state for flipping
    // When isFlipping is true, we animate to 90 degrees (halfway)
    // After the state change, it will automatically continue to 180 or back to 0
    val targetRotation = when {
        isFlipping -> if (showAnswer) 90f else 90f // During flip animation
        showAnswer -> 180f // After flip is complete, answer side
        else -> 0f // Question side
    }

    val rotationY by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "rotationY"
    )

    // Scale animation for touch feedback
    val scale by animateFloatAsState(
        targetValue = if (isFlipping || isChangingCard) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Slide animation for card change
    val slideOffset by animateFloatAsState(
        targetValue = if (isChangingCard) (slideDirection * 100f) else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing
        ),
        label = "slideOffset"
    )

    val verticalDragThreshold = 20f
    val horizontalDragThreshold = 10f
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false }
                ) { change, dragAmount ->
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
        contentAlignment = Alignment.Center
    ) {
        // Card content
        Box(
            modifier = Modifier
                .size(300.dp)
                .graphicsLayer {
                    this.rotationY = rotationY  // Use the animated value directly
                    translationY = if (isChangingCard) slideOffset else 0f
                    scaleX = scale
                    scaleY = scale
                    cameraDistance = 12f * density
                }
                .border(2.dp, Color.Black, RoundedCornerShape(16.dp))
                .background(Color.LightGray, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Question side
            AnimatedVisibility(
                visible = rotationY < 90f,
                enter = fadeIn(tween(150, 150)) + slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { -it }
                ),
                exit = fadeOut(tween(150)) + slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { it }
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentFlashcard.frontQuestion,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // Answer side
            AnimatedVisibility(
                visible = rotationY > 90f,
                enter = fadeIn(tween(150, 150)) + slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { it }
                ),
                exit = fadeOut(tween(150)) + slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { -it }
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // We need to rotate the answer side 180 degrees so it's not upside down
                            // when the card is flipped
                            this.rotationY = 180f
                        }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentFlashcard.backAnswer,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}