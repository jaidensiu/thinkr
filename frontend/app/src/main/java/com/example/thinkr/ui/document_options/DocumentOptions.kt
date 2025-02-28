package com.example.thinkr.ui.document_options

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thinkr.app.Route
import com.example.thinkr.domain.model.DocumentItem
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentOptionsScreen(documentItem: DocumentItem, navController: NavController, viewModel: DocumentOptionsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    // Animation states
    var optionsVisible by remember { mutableStateOf(false) }

    // Staggered animation for options
    LaunchedEffect(Unit) {
        optionsVisible = true
    }

 // Title and main content
    TopAppBar(
        title = { Text("Document Options") },
        navigationIcon = {
            IconButton(
                onClick = { navController.navigate(Route.Home) }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!state.isReady) {
            Text("We are preparing your study material", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(18.dp))
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(32.dp))
        }
        // Option buttons with staggered animation
        val options: List<Pair<String, () -> Unit>> = listOf(
            Pair("Take Quiz") {
                navController.navigate(Route.Quiz.createRoute(documentItem))
            },
            Pair("Review Flashcards") {
                navController.navigate(Route.Flashcards.createRoute(documentItem))
            },
            Pair("Chat with Thinkr AI") {
                navController.navigate(Route.Chat.createRoute(documentItem))
            }
        )

        options.forEachIndexed { index, (title, onClick) ->
            val showDelay = 100L * index

            LaunchedEffect(optionsVisible) {
                if (optionsVisible) {
                    delay(showDelay)
                }
            }

            DocumentOptionButtonWrapper(optionsVisible, title, onClick, state.isReady)
            Spacer(modifier = Modifier.height(50.dp))
        }
    }

}

@Composable
fun DocumentOptionButtonWrapper(visible: Boolean, title: String, onClick: () -> Unit, isReady: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(spring(dampingRatio = 0.7f)) +
                expandVertically(spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow))
    ) {
        DocumentOptionButton(title = title, onClick = onClick, isReady = isReady)
    }
}

@Composable
fun DocumentOptionButton(
    title: String,
    onClick: () -> Unit,
    isReady: Boolean
) {
    Button(
        onClick = {
            if (isReady) onClick() },
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isReady) MaterialTheme.colorScheme.primary else Color.LightGray,
            contentColor = if (isReady) Color.LightGray else Color.Gray
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}
