package com.example.thinkr.ui.document_details

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.thinkr.app.Route
import com.example.thinkr.domain.DocumentManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class DocumentDetailsViewModel(private val documentManager: DocumentManager) : ViewModel() {
    private val _state = MutableStateFlow(DocumentDetailsState())

    fun onBackPressed(navController: NavController) {
        navController.navigate(Route.Home)
    }

    fun onUpload(navController: NavController, name: String, context: String, uri: Uri) {
        _state.update { it.copy(name = name, context = context, uri = uri) }
        documentManager.uploadDocument(name, uri)
        navController.navigate(Route.Home)
    }
}
