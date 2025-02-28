package com.example.thinkr.ui.document_upload

import android.net.Uri

data class DocumentUploadState(
    val name: String = "document name",
    val context: String = "context",
    val uri: Uri = Uri.EMPTY
)
