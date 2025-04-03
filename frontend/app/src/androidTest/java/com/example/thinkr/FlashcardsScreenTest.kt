package com.example.thinkr

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.thinkr.data.models.Document
import com.example.thinkr.data.models.FlashcardItem
import com.example.thinkr.data.models.User
import com.example.thinkr.data.remote.study.StudyApi
import com.example.thinkr.data.repositories.flashcards.FlashcardsRepository
import com.example.thinkr.data.repositories.user.UserRepository
import com.example.thinkr.ui.flashcards.FlashcardsScreen
import com.example.thinkr.ui.flashcards.FlashcardsViewModel
import io.ktor.client.HttpClient
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
internal class FlashcardsScreenTest {
    companion object {
        private const val COMPOSE_TREE = "COMPOSE_TREE"

        // Test data for flashcards
        private val TEST_FLASHCARDS = listOf(
            FlashcardItem(
                front = "Eisenberg Institute for Quantum Research",
                back = "Research institute that made a breakthrough in quantum entanglement in 2094"
            ),
            FlashcardItem(
                front = "Schrödinger's Constant (SC)",
                back = "Constant used to manipulate structured data transmission without energy loss"
            ),
            FlashcardItem(
                front = "Synthetic Neurons",
                back = "Neurons that can encode memories and allow for skill 'uploading'"
            ),
            FlashcardItem(
                front = "Twin Prime Conjecture",
                back = "Mathematical conjecture related to prime numbers"
            ),
            FlashcardItem(
                front = "Fibonacci Sequence",
                back = "Sequence of numbers where each number is the sum of the two preceding ones"
            ),
            FlashcardItem(
                front = "The Singularity Equation",
                back = "Unclassified document hinting at a mathematical entity redefining physics"
            ),
            FlashcardItem(
                front = "Artificial Intelligence (AI)",
                back = "Intelligent machines capable of performing tasks that typically require human intelligence"
            ),
            FlashcardItem(
                front = "Neuronal Patch",
                back = "Patch embedded in the brain to facilitate rapid learning or skill acquisition"
            ),
            FlashcardItem(
                front = "Quantum Entanglement",
                back = "Phenomenon where particles become interconnected and share properties regardless of distance"
            ),
            FlashcardItem(
                front = "The Quantum Paradox of 2094",
                back = "Series of scientific discoveries and events in 2094 with profound implications"
            )
        )

        private val TEST_DOCUMENT = Document(
            documentId = "test_document7878418937470003605.pdf",
            documentName = "Test Document",
            uploadTime = "2025-03-13 17:49:53",
            activityGenerationComplete = true,
            public = false
        )

        private val TEST_USER = User(
            email = "test_user@gmail.com",
            name = "Test User",
            googleId = "112119816049214759635",
            subscribed = false
        )
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun flashcardsScreen_e2e() {
        val studyApi = StudyApi(HttpClient())
        val userRepository = UserRepository()
        userRepository.setUser(TEST_USER)
        val flashcardsRepository = FlashcardsRepository(studyApi, userRepository)
        val navController = mockk<NavController>(relaxed = true)
        val viewModel = FlashcardsViewModel(flashcardsRepository)

        composeTestRule.setContent {
            FlashcardsScreen(
                document = TEST_DOCUMENT,
                suggestedFlashcards = null,
                navController = navController,
                viewModel = viewModel
            )
        }

        // Debug the composition tree
        composeTestRule.onRoot().printToLog(tag = COMPOSE_TREE)

        for (flashcard in TEST_FLASHCARDS) {
            composeTestRule.waitForIdle()
            sleep(1_000)
            composeTestRule.onRoot().printToLog(tag = COMPOSE_TREE)

            println("front: ${flashcard.front}")
            val node = composeTestRule.onNodeWithText(flashcard.front)
            node.assertIsDisplayed()
            node.performTouchInput { swipeLeft() }

            composeTestRule.waitForIdle()
            sleep(2_000)
            composeTestRule.onRoot().printToLog(tag = COMPOSE_TREE)

            println("back: ${flashcard.back}")
            val backNode = composeTestRule.onNodeWithText(flashcard.back)
            backNode.assertIsDisplayed()
            backNode.performTouchInput { swipeUp(startY = 2 * bottom, endY = 2 * top) }

            composeTestRule.waitForIdle()
            sleep(1_000)
            composeTestRule.onRoot().printToLog(tag = COMPOSE_TREE)
        }
    }
}
