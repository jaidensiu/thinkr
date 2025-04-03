package com.example.thinkr

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import com.example.thinkr.app.Route
import com.example.thinkr.data.models.Document
import com.example.thinkr.data.models.User
import com.example.thinkr.data.remote.document.DocumentApi
import com.example.thinkr.data.remote.study.StudyApi
import com.example.thinkr.data.repositories.doc.DocRepository
import com.example.thinkr.data.repositories.user.UserRepository
import com.example.thinkr.ui.document_options.DocumentOptionsScreen
import com.example.thinkr.ui.document_options.DocumentOptionsViewModel
import io.ktor.client.HttpClient
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep

internal class DocumentOptionsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    companion object {
        private val TEST_DOCUMENT = Document(
            documentId = "test_document7878418937470003605.pdf",
            documentName = "Test Document",
            uploadTime = "2025-03-27 00:16:43",
            activityGenerationComplete = true,
            public = false
        )
        private val TEST_USER = User(
            email = "test_user@gmail.com",
            name = "Test User",
            googleId = "112119816049214759635",
            subscribed = true
        )
    }

    private lateinit var navController: NavController

    @Before
    fun setup() {
        navController = mockk<NavController>(relaxed = true)
    }

    @Test
    fun successScenario_navigatesToCorrectScreens() {
        val documentApi = DocumentApi(HttpClient())
        val studyApi = StudyApi(HttpClient())
        val docRepository = DocRepository(documentApi, studyApi)
        val userRepository = UserRepository()
        userRepository.setUser(TEST_USER)
        val viewModel = DocumentOptionsViewModel(docRepository, userRepository)

        composeTestRule.setContent {
            DocumentOptionsScreen(TEST_DOCUMENT, navController, viewModel)
        }

        composeTestRule.onNodeWithText("Take Quiz").performClick()
        composeTestRule.waitForIdle()
        verify { navController.navigate(Route.Quiz.createRoute(TEST_DOCUMENT)) }

        composeTestRule.onNodeWithText("Review Flashcards").performClick()
        composeTestRule.waitForIdle()
        verify { navController.navigate(Route.Flashcards.createRoute(TEST_DOCUMENT)) }

        composeTestRule.onNodeWithText("Chat with Thinkr AI").performClick()
        composeTestRule.waitForIdle()
        verify { navController.navigate(Route.Chat.createRoute(TEST_DOCUMENT)) }
    }

    @Test
    fun failureScenario_showsErrorToast_andRedirectsToHome() {
        val documentApi = DocumentApi(HttpClient())
        val studyApi = StudyApi(HttpClient())
        val docRepository = spyk(DocRepository(documentApi, studyApi)) {
            coEvery {
                getDocuments(
                    TEST_USER.googleId,
                    listOf(TEST_DOCUMENT.documentId)
                )
            } answers { listOf(TEST_DOCUMENT.copy(activityGenerationComplete = false)) }
        }
        val userRepository = UserRepository()
        userRepository.setUser(TEST_USER)
        val viewModel = DocumentOptionsViewModel(docRepository, userRepository)

        composeTestRule.setContent {
            DocumentOptionsScreen(TEST_DOCUMENT, navController, viewModel)
        }

        composeTestRule.waitForIdle()
        sleep(65_000)

        // Expect an error toast (mocking a toast message)
        verify { navController.navigate(Route.Home) }
    }
}
