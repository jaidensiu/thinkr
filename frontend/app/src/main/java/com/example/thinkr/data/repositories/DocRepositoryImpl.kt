package com.example.thinkr.data.repositories

import android.net.Uri
import com.example.thinkr.data.models.DocumentItem
import com.example.thinkr.data.remote.RemoteApiImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DocRepositoryImpl(private val remoteApi: RemoteApiImpl): DocRepository {
    companion object {
        val MAX_NAME_LENGTH = 50
        val MAX_CONTEXT_LENGTH = 500
    }

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

    override fun uploadDocument(name: String, uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            _uploadingDocuments.update { it + DocumentItem(name, false) }
            // TODO: Upload documents to the database
            delay(30_000)
            _uploadingDocuments.update { it - DocumentItem(name, false) }
            loadDocuments()
        }
    }

    override fun getRetrievedDocuments() = _retrievedDocuments
    override fun getUploadingDocuments() = _uploadingDocuments
}