package com.example.thinkr.data.repositories

import android.net.Uri
import com.example.thinkr.data.models.DocumentItem
import kotlinx.coroutines.flow.MutableStateFlow

interface DocRepository {
    fun uploadDocument(name: String, uri: Uri)
    fun getRetrievedDocuments(): MutableStateFlow<List<DocumentItem>>
    fun getUploadingDocuments(): MutableStateFlow<List<DocumentItem>>
}