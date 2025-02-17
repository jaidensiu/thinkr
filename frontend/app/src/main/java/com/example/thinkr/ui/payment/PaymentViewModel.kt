package com.example.thinkr.ui.payment

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PaymentViewModel : ViewModel() {
    private val _state = MutableStateFlow(PaymentScreenState())
    val state = _state.asStateFlow()

    fun onCardNumberChange(cardNumber: String) {
        _state.update { it.copy(cardNumber = cardNumber) }
    }

    fun onExpirationChange(cardExpiration: String) {
        _state.update { it.copy(cardExpiration = cardExpiration) }
    }

    fun onCvcChange(cardCvc: String) {
        _state.update { it.copy(cardCvc = cardCvc) }
    }

    fun onBillingAddressChange(cardBillingAddress: String) {
        _state.update { it.copy(cardBillingAddress = cardBillingAddress) }
    }
}
