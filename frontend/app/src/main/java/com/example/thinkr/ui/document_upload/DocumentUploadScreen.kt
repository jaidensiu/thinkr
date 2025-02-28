package com.example.thinkr.ui.document_upload

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thinkr.R
import com.example.thinkr.domain.DocumentManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentUploadScreen(
    navController: NavController,
    selectedUri: Uri,
    documentManager: DocumentManager,
    viewModel: DocumentUploadViewModel = DocumentUploadViewModel(documentManager = documentManager)
) {
    var name by remember { mutableStateOf("") }
    var context by remember { mutableStateOf("") }
    val contextForToast = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TopAppBar(
            title = { Text("Upload Document") },
            navigationIcon = {
                IconButton(
                    onClick = { viewModel.onBackPressed(navController) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(80.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.document_placeholder_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Selected File: ${selectedUri.lastPathSegment}", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= DocumentManager.MAX_NAME_LENGTH) name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(0.8f),
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = context,
            onValueChange = { if (it.length <= DocumentManager.MAX_CONTEXT_LENGTH) context = it },
            label = { Text("Context") },
            modifier = Modifier
                .fillMaxWidth(0.8f) // Context box is now ~40% of the screen width
                .height(240.dp), // Increased default height
            maxLines = 10
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    Toast.makeText(contextForToast, "Please fill in the name", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    viewModel.onUpload(navController, name, context, selectedUri)
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(50.dp)
        ) {
            Text("Upload")
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
