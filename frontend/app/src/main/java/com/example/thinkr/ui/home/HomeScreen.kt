package com.example.thinkr.ui.home

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thinkr.R
import org.koin.androidx.compose.koinViewModel
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.thinkr.domain.model.DocumentItem

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeScreenViewModel = koinViewModel()) {
    val state = viewModel.state.collectAsState()

    HomeScreenContent(
        state = state,
        onAction = { action -> viewModel.onAction(action, navController) }
    )
}

@Composable
fun HomeScreenContent(
    state: State<HomeScreenState>,
    onAction: (HomeScreenAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Row with two buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onAction(HomeScreenAction.BackButtonClicked) }
            )
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
            items(state.value.items) { item ->
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
            FilePickerDialog(onDismiss = { onAction(HomeScreenAction.DismissDialog) })
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
fun FilePickerDialog(onDismiss: () -> Unit = {}) {
    val context = LocalContext.current
    val storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        selectedFileName = uri?.let { getFileName(context, it) }
    }

    var showPermissionDialog by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                filePickerLauncher.launch("*/*")
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, storagePermission)) {
                         showSettingsDialog(context)
                }
                showPermissionDialog = true
            }
        }
    )

    fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(context, storagePermission) == PackageManager.PERMISSION_GRANTED) {
            filePickerLauncher.launch("*/*")
        } else {
            requestPermissionLauncher.launch(storagePermission)
        }
    }

    if (showPermissionDialog) {
        Toast.makeText(
            LocalContext.current,
            "Permission Denied",
            Toast.LENGTH_SHORT
        ).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Select a File", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { checkStoragePermission() }) {
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

@Composable
fun ListItem(item: DocumentItem, onAction: (HomeScreenAction) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onAction(HomeScreenAction.DocumentItemClicked(documentItem = item))
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular logo
        Image(
            painter = painterResource(id = R.drawable.document_placeholder_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Name
        Text(text = item.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}

