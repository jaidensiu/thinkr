package com.example.thinkr.ui.document_details

import android.net.Uri

data class DocumentDetailsState(
    val name: String = "document name",
    val context: String = "context",
    val uri: Uri = Uri.EMPTY
)
