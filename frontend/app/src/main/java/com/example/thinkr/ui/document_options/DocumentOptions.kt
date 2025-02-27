package com.example.thinkr.ui.document_options

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.thinkr.data.models.Document

@Composable
fun DocumentOptionsScreen(documentItem: Document) {
    Text("Document Options Screen ${documentItem.name}")
}