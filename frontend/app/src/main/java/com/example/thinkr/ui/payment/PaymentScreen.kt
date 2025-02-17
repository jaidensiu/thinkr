package com.example.thinkr.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel = koinViewModel(),
    onConfirm: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Payment information")
        OutlinedTextField(
            value = state.cardNumber,
            onValueChange = { viewModel.onCardNumberChange(it) },
            label = { Text(text = "Card Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.cardExpiration,
            onValueChange = { viewModel.onExpirationChange(it) },
            label = { Text(text = "MM/YY") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.cardCvc,
            onValueChange = { viewModel.onCvcChange(it) },
            label = { Text(text = "CVC") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.cardBillingAddress,
            onValueChange = { viewModel.onBillingAddressChange(it) },
            label = { Text(text = "Billing Address") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onConfirm) {
            Text(text = "Pay")
        }
    }
}
