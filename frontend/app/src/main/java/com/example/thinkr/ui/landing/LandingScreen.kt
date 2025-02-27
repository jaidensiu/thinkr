package com.example.thinkr.ui.landing

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thinkr.app.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import org.koin.androidx.compose.koinViewModel

@Composable
fun LandingScreen(
    viewModel: LandingScreenViewModel = koinViewModel(),
    onLogin: () -> Unit,
    onSignUp: () -> Unit,
    navigateToHome: () -> Unit,
    onSignOut: () -> Unit
) {
    val state = viewModel.state.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }
    val focusManager = LocalFocusManager.current
    val focusRequester = FocusRequester()
    var passwordVisible by remember { mutableStateOf(value = false) }
    val context = LocalContext.current
    val activity = context as MainActivity
    val signInIntent = activity.googleSignInClient.signInIntent
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.onGoogleSignInResult(account, onSignOut)
            Log.d("ServerScreen", "Sign-in successful: ${account?.email}")
        } catch (e: ApiException) {
            Log.e("ServerScreen", "Sign-in failed: ${e.statusCode}", e)
        }
    }

    LaunchedEffect(state.value.isAuthenticated) {
        if (state.value.isAuthenticated) {
            navigateToHome()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { focusManager.clearFocus() }
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome to Thinkr")
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = state.value.username,
            onValueChange = viewModel::onEditUsername,
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth(fraction = 0.75f),
            placeholder = { Text(text = "Username") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = if (state.value.username.isNotEmpty()) {
                    ImeAction.Next
                } else {
                    ImeAction.Done
                }
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = state.value.password,
            onValueChange = viewModel::onEditPassword,
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth(fraction = 0.75f),
            placeholder = { Text(text = "Password") },
            trailingIcon = {
                TextButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = if (passwordVisible) "hide" else "show",
                        fontSize = 14.sp
                    )
                }
            },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth(fraction = 0.5f)
        ) {
            Text(text = "Login")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onSignUp,
            modifier = Modifier.fillMaxWidth(fraction = 0.5f)
        ) {
            Text(text = "Sign Up")
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (state.value.isLoading) {
            CircularProgressIndicator()
        }
        Button(
            onClick = { signInLauncher.launch(signInIntent) },
            modifier = Modifier.fillMaxWidth(fraction = 0.5f)
        ) {
            Text(text = "Sign in with Google")
        }
        Text(
            text = state.value.error ?: "",
            color = if (state.value.error != null) {
                MaterialTheme.colorScheme.error
            } else {
                Color.Transparent
            },
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
