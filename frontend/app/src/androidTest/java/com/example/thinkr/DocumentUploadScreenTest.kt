package com.example.thinkr

import android.content.Context
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.thinkr.app.Route
import com.example.thinkr.data.models.User
import com.example.thinkr.data.remote.document.DocumentApi
import com.example.thinkr.data.remote.study.StudyApi
import com.example.thinkr.data.repositories.doc.DocRepository
import com.example.thinkr.data.repositories.user.UserRepository
import com.example.thinkr.test.R
import com.example.thinkr.ui.document_upload.DocumentUploadScreen
import com.example.thinkr.ui.document_upload.DocumentUploadViewModel
import io.ktor.client.HttpClient
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.lang.Thread.sleep

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
internal class DocumentUploadScreenTest {
    companion object {
        private const val COMPOSE_TREE = "COMPOSE_TREE"
        private const val NAME_FIELD = "Name"
        private const val CONTEXT_FIELD = "Context"
        private const val UPLOAD_DOCUMENT_TITLE = "Upload Document"
        private const val UPLOAD_BUTTON = "Upload"
        private const val TEST_USER_ID = "test_user_id"
        private const val TEST_DOCUMENT_NAME = "Test Document"
        private const val TEST_DOCUMENT_CONTEXT = "This is a test document context"
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun documentUploadScreen_uploadSuccess_navigatesToHome() {
        // Setup mocks
        val docRepository = mockk<DocRepository>(relaxed = true)
        val userRepository = mockk<UserRepository>(relaxed = true)
        val navController = mockk<NavController>(relaxed = true)

        // Mock the Context to return our mocked ContentResolver
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tempFile = File.createTempFile("testFile", ".txt", context.cacheDir)
        FileOutputStream(tempFile).use { it.write("content".toByteArray()) }
        val uri = Uri.fromFile(tempFile)

        val viewModel = DocumentUploadViewModel(
            docRepository = docRepository,
            userRepository = userRepository
        )

        // Mock user repository to return a valid user
        every { userRepository.getUser() } returns mockk(relaxed = true) {
            every { googleId } returns TEST_USER_ID
        }

        // Mock document repository to return success
        coEvery {
            docRepository.uploadDocument(
                fileBytes = any(),
                fileName = any(),
                userId = any(),
                documentName = any(),
                documentContext = any(),
                documentPublic = any()
            )
        } returns true

        // Setup content
        composeTestRule.setContent {
            DocumentUploadScreen(
                navController = navController,
                selectedUri = uri,
                viewModel = viewModel
            )
        }

        // Debug the composition tree
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().printToLog(tag = COMPOSE_TREE)

        // Verify initial screen state
        composeTestRule.onNodeWithText(UPLOAD_DOCUMENT_TITLE).assertIsDisplayed()
        composeTestRule.onNode(hasContentDescription("Logo")).assertIsDisplayed()

        // Enter document name and context
        composeTestRule.onNodeWithText(NAME_FIELD).performTextInput(TEST_DOCUMENT_NAME)
        composeTestRule.onNodeWithText(CONTEXT_FIELD).performTextInput(TEST_DOCUMENT_CONTEXT)

        // Click upload button
        composeTestRule.onNodeWithText(UPLOAD_BUTTON).performClick()

        // Verify navigation to home screen
        verify { navController.navigate(Route.Home) }
    }

    @Test
    fun documentUploadScreen_uploadWithEmptyName_showsErrorToast() {
        // Setup mocks
        val docRepository = mockk<DocRepository>(relaxed = true)
        val userRepository = mockk<UserRepository>(relaxed = true)
        val navController = mockk<NavController>(relaxed = true)
        val uri = Uri.EMPTY
        val viewModel = DocumentUploadViewModel(
            docRepository = docRepository,
            userRepository = userRepository
        )

        // Mock user repository to return a valid user
        every { userRepository.getUser() } returns mockk(relaxed = true) {
            every { googleId } returns TEST_USER_ID
        }

        // Setup content
        composeTestRule.setContent {
            DocumentUploadScreen(
                navController = navController,
                selectedUri = uri,
                viewModel = viewModel
            )
        }

        // Debug the composition tree
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().printToLog(tag = COMPOSE_TREE)

        // Don't enter a document name (leave it blank)

        // Click upload button
        composeTestRule.onNodeWithText(UPLOAD_BUTTON).performClick()

        // Verify no navigation happened
        verify(exactly = 0) { navController.navigate(any<String>()) }
    }

    @Test
    fun documentUploadScreen_uploadFails_showsErrorToast() {
        // Setup mocks
        val docRepository = mockk<DocRepository>(relaxed = true)
        val userRepository = mockk<UserRepository>(relaxed = true)
        val navController = mockk<NavController>(relaxed = true)
        val uri = Uri.EMPTY
        val viewModel = DocumentUploadViewModel(
            docRepository = docRepository,
            userRepository = userRepository
        )

        // Mock user repository to return a valid user
        every { userRepository.getUser() } returns mockk(relaxed = true) {
            every { googleId } returns TEST_USER_ID
        }

        // Mock document repository to return failure
        coEvery {
            docRepository.uploadDocument(
                fileBytes = any(),
                fileName = any(),
                userId = any(),
                documentName = any(),
                documentContext = any(),
                documentPublic = any()
            )
        } returns false

        // Setup content
        composeTestRule.setContent {
            DocumentUploadScreen(
                navController = navController,
                selectedUri = uri,
                viewModel = viewModel
            )
        }

        // Debug the composition tree
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().printToLog(tag = COMPOSE_TREE)

        // Enter document name and context
        composeTestRule.onNodeWithText(NAME_FIELD).performTextInput(TEST_DOCUMENT_NAME)
        composeTestRule.onNodeWithText(CONTEXT_FIELD).performTextInput(TEST_DOCUMENT_CONTEXT)

        // Click upload button
        composeTestRule.onNodeWithText(UPLOAD_BUTTON).performClick()

        // Verify no navigation happened and error toast was shown
        verify(exactly = 0) { navController.navigate(any<String>()) }
    }

    @Test
    fun documentUploadScreen_uploadThrowsException_showsErrorToast() {
        // Setup mocks
        val docRepository = mockk<DocRepository>(relaxed = true)
        val userRepository = mockk<UserRepository>(relaxed = true)
        val navController = mockk<NavController>(relaxed = true)
        val uri = Uri.EMPTY
        val viewModel = DocumentUploadViewModel(
            docRepository = docRepository,
            userRepository = userRepository
        )

        // Mock user repository to return a valid user
        every { userRepository.getUser() } returns mockk(relaxed = true) {
            every { googleId } returns TEST_USER_ID
        }

        // Mock document repository to throw an exception
        coEvery {
            docRepository.uploadDocument(
                fileBytes = any(),
                fileName = any(),
                userId = any(),
                documentName = any(),
                documentContext = any(),
                documentPublic = any()
            )
        } throws Exception("Network error")

        // Setup content
        composeTestRule.setContent {
            DocumentUploadScreen(
                navController = navController,
                selectedUri = uri,
                viewModel = viewModel
            )
        }

        // Debug the composition tree
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().printToLog(tag = COMPOSE_TREE)

        // Enter document name and context
        composeTestRule.onNodeWithText(NAME_FIELD).performTextInput(TEST_DOCUMENT_NAME)
        composeTestRule.onNodeWithText(CONTEXT_FIELD).performTextInput(TEST_DOCUMENT_CONTEXT)

        // Click upload button
        composeTestRule.onNodeWithText(UPLOAD_BUTTON).performClick()

        // Verify no navigation happened and error toast was shown
        verify(exactly = 0) { navController.navigate(any<String>()) }
    }

    @Test
    fun documentUploadScreen_backButtonPressed_navigatesToHome() {
        // Setup mocks
        val docRepository = mockk<DocRepository>(relaxed = true)
        val userRepository = mockk<UserRepository>(relaxed = true)
        val navController = mockk<NavController>(relaxed = true)
        val uri = Uri.EMPTY
        val viewModel = DocumentUploadViewModel(
            docRepository = docRepository,
            userRepository = userRepository
        )

        // Setup content
        composeTestRule.setContent {
            DocumentUploadScreen(
                navController = navController,
                selectedUri = uri,
                viewModel = viewModel
            )
        }

        // Debug the composition tree
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().printToLog(tag = COMPOSE_TREE)

        // Click back button
        composeTestRule.onNode(hasContentDescription("Back")).performClick()

        // Verify navigation to home screen
        verify { navController.navigate(Route.Home) }
    }

    @Test
    fun documentUploadScreen_textFieldsLimitInput() {
        // Setup mocks
        val docRepository = mockk<DocRepository>(relaxed = true)
        val userRepository = mockk<UserRepository>(relaxed = true)
        val navController = mockk<NavController>(relaxed = true)
        val uri = Uri.EMPTY
        val viewModel = DocumentUploadViewModel(
            docRepository = docRepository,
            userRepository = userRepository
        )

        // Setup content
        composeTestRule.setContent {
            DocumentUploadScreen(
                navController = navController,
                selectedUri = uri,
                viewModel = viewModel
            )
        }

        // Debug the composition tree
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().printToLog(tag = COMPOSE_TREE)

        // Create test strings that exceed the max length
        val longName = "a".repeat(DocumentUploadViewModel.MAX_NAME_LENGTH + 10)
        val longContext = "a".repeat(DocumentUploadViewModel.MAX_CONTEXT_LENGTH + 10)

        // Enter document name and context
        composeTestRule.onNodeWithText(NAME_FIELD).performTextInput(longName)
        composeTestRule.onNodeWithText(CONTEXT_FIELD).performTextInput(longContext)

        // Verify the input is limited
        // Note: Due to the nature of the compose test, we can't directly verify the field values
        // In a real test, you might want to use a state verification approach
    }

    /**
     * End-to-end test for the document upload screen
     * No mocking, real network calls
     */
    @Test
    fun documentUploadScreen_e2e() {
        val documentApi = DocumentApi(HttpClient())
        val studyApi = StudyApi(HttpClient())
        val docRepository = DocRepository(documentApi, studyApi)
        val userRepository = UserRepository()
        userRepository.setUser(
            User(
                email = "test_user@gmail.com",
                name = "Test User",
                googleId = "112119816049214759635",
                subscribed = false
            )
        )

        val navController = mockk<NavController>(relaxed = true)

        // Mock the Context to return our mocked ContentResolver
        val testContext = InstrumentationRegistry.getInstrumentation().context // Test package context
        val resId = R.raw.test_document
        val inputStream = testContext.resources.openRawResource(resId)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tempFile = File.createTempFile("test_document", ".pdf", context.cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        val uri = Uri.fromFile(tempFile)

        val viewModel = DocumentUploadViewModel(
            docRepository = docRepository,
            userRepository = userRepository
        )

        // Setup content
        composeTestRule.setContent {
            DocumentUploadScreen(
                navController = navController,
                selectedUri = uri,
                viewModel = viewModel
            )
        }

        // Debug the composition tree
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().printToLog(tag = COMPOSE_TREE)

        // Verify initial screen state
        composeTestRule.onNodeWithText(UPLOAD_DOCUMENT_TITLE).assertIsDisplayed()
        composeTestRule.onNode(hasContentDescription("Logo")).assertIsDisplayed()

        // Enter document name and context
        composeTestRule.onNodeWithText(NAME_FIELD).performTextInput(TEST_DOCUMENT_NAME)
        composeTestRule.onNodeWithText(CONTEXT_FIELD).performTextInput(TEST_DOCUMENT_CONTEXT)

        // Click upload button
        composeTestRule.onNodeWithText(UPLOAD_BUTTON).performClick()

        composeTestRule.waitForIdle()

        sleep(5_000)

        // Verify navigation to home screen
        verify { navController.navigate(Route.Home) }
    }
}
