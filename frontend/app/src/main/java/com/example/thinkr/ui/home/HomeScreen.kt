package com.example.thinkr.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.thinkr.R
import com.example.thinkr.ui.shared.ListItem

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeScreenViewModel,
    onSignOut: () -> Unit
) {
    val state = viewModel.state.collectAsState()
    var showDialog by remember { mutableStateOf(value = false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Sign Out") },
            text = { Text(text = "Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = { showDialog = false; onSignOut() }) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = "No")
                }
            }
        )
    }

    HomeScreenContent(
        state = state,
        onAction = { action -> viewModel.onAction(action, navController) },
        onSignOut = { showDialog = true }
    )
}

@Composable
fun HomeScreenContent(
    state: State<HomeScreenState>,
    onAction: (HomeScreenAction) -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val retrievedDocuments = state.value.docRepositoryImpl.getRetrievedDocuments().collectAsState()
        val uploadingDocuments = state.value.docRepositoryImpl.getUploadingDocuments().collectAsState()
        // Top Row with two buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onSignOut) {
                Text(text = "Sign out")
            }
            Image(
                painter = painterResource(id = R.drawable.account_circle),
                contentDescription = "Back",
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onAction(HomeScreenAction.ProfileButtonClicked) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            // List of items
            items(retrievedDocuments.value) { item ->
                ListItem(item, onAction)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(uploadingDocuments.value) { item ->
                ListItem(item, onAction)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Add item
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAction(HomeScreenAction.AddButtonClicked)
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Circular logo
                    Image(
                        painter = painterResource(id = R.drawable.add_box),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Name
                    Text(text = "Add", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
            }

        }
        // Dialog
        if (state.value.showDialog) {
            FilePickerDialog(
                onDismiss = { onAction(HomeScreenAction.DismissDialog) },
                onSelected = { onAction(HomeScreenAction.FileSelected(it)) })
        }
    }
}

fun showSettingsDialog(context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Permission Denied")
        .setMessage("You have denied this permission permanently. Please enable it in settings.")
        .setPositiveButton("Go to Settings") { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
        .setNegativeButton("Cancel", null)
        .show()
}

fun getFileName(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                return it.getString(nameIndex)
            }
        }
    }
    return null
}

@Composable
fun FilePickerDialog(onDismiss: () -> Unit = {}, onSelected: (Uri) -> Unit) {
    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        selectedFileName = uri?.let { getFileName(context, it) }
    }

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
                    modifier = Modifier
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Select a File", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { filePickerLauncher.launch("*/*") }) {
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
                }
            }
        }
    }

    if (selectedFileUri != null) {
        onDismiss()
        onSelected(selectedFileUri!!)
    }
}
