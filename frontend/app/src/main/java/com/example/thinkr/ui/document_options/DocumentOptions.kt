package com.example.thinkr.ui.document_options

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.thinkr.domain.model.DocumentItem

@Composable
fun DocumentOptionsScreen(documentItem: DocumentItem) {
    Text("Document Options Screen ${documentItem.name}")
}