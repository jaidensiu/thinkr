package com.example.thinkr.ui.document_details

import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DocumentDetails(selectedUri: Uri) {
    Text("Document Details Screen ${selectedUri.path}")
}