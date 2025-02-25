package com.example.thinkr.domain

import android.net.Uri
import com.example.thinkr.domain.model.DocumentItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DocumentManager {
    private val _retrievedDocuments = MutableStateFlow<List<DocumentItem>>(emptyList()) // 🔄 Reactive state
    private val _uploadingDocuments = MutableStateFlow<List<DocumentItem>>(emptyList()) // 🔄 Reactive state

    fun loadDocuments() {
        // TODO: Load documents from the database
        _retrievedDocuments.value = listOf(
            DocumentItem("Item1"),
            DocumentItem("Item2"),
            DocumentItem("Item3"),
        )
    }

    fun uploadDocument(name: String, uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            _uploadingDocuments.update { it + DocumentItem(name, false) }
            // TODO: Upload documents to the database
            delay(30_000)
            _uploadingDocuments.update { it - DocumentItem(name, false) }
            loadDocuments()
        }
    }

    fun getRetrievedDocuments() = _retrievedDocuments
    fun getUploadingDocuments() = _uploadingDocuments
}