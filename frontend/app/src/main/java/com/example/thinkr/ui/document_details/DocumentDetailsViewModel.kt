package com.example.thinkr.ui.document_details

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.thinkr.app.Route
import com.example.thinkr.data.repositories.DocRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DocumentDetailsViewModel(private val documentRepositoryImpl: DocRepositoryImpl) : ViewModel() {
    private val _state = MutableStateFlow(DocumentDetailsState())

    fun onBackPressed(navController: NavController) {
        navController.navigate(Route.Home)
    }

    suspend fun onUpload(navController: NavController, name: String, context: String, uri: Uri) {
        viewModelScope.launch {

        }
        _state.update { it.copy(name = name, context = context, uri = uri) }
        documentRepositoryImpl.uploadDocument(
            document = TODO(),
            userId = TODO(), // pass this from dao
            documentName = TODO(),
            documentContext = TODO()
        )
        navController.navigate(Route.Home)
    }

    companion object {
        const val MAX_NAME_LENGTH = 50
        const val MAX_CONTEXT_LENGTH = 500
    }
}
