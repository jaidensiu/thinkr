package com.example.thinkr.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Composable that displays a file picker dialog.
 *
 * This dialog allows the user to select a file from their device's storage.
 * After selection, it copies the file to the app's private storage and provides
 * the resulting URI to the caller. Handles file access errors and displays
 * appropriate error messages.
 *
 * @param onDismiss Callback function to be invoked when the dialog should be dismissed.
 * @param onSelected Callback function that receives the selected file's URI after processing.
 */
@SuppressLint("Recycle")
@Composable
fun FilePickerDialog(
    onDismiss: () -> Unit = {},
    onSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var filePickerError by remember { mutableStateOf(value = false) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            try {
                uri?.let {
                    val inputStream = context.contentResolver.openInputStream(uri) ?: throw IOException(
                        "Could not open document"
                    )
                    val fileName = getFileName(context, uri)
                    val file = File(context.filesDir, fileName)
                    inputStream.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                    selectedFileUri = Uri.fromFile(file)
                    selectedFileName = file.name
                }
                filePickerError = false
            } catch (e: FileNotFoundException) {
                filePickerError = true
                e.printStackTrace()
            } catch (e: SecurityException) {
                filePickerError = true
                e.printStackTrace()
            } catch (e: IOException) {
                filePickerError = true
                e.printStackTrace()
            }
        }
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Select a File", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { filePickerLauncher.launch(arrayOf("*/*")) }) {
                        Text(text = "Choose File")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    selectedFileUri?.let {
                        Text(text = "File Name: ${selectedFileName ?: "Unknown"}")
                        Text(text = "URI: $it")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDismiss) {
                        Text(text = "Close")
                    }
                    if (filePickerError) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error: please select a PDF file",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    if (selectedFileUri != null) {
        onDismiss()
        onSelected(selectedFileUri!!)
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    // Get the display name from ContentResolver
    var fileName = context.contentResolver.query(uri,
        arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        } else {
            null
        }
    } ?: uri.lastPathSegment?.substringAfterLast('/') ?: "unknown"
    // Sanitize filename
    fileName = fileName.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
    return fileName
}
