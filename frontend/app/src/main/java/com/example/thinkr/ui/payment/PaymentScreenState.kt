package com.example.thinkr.ui.payment

data class PaymentScreenState(
    val cardNumber: String = "cardNumber",
    val cardExpiration: String = "expiry",
    val cardCvc: String = "cvc",
    val cardBillingAddress: String = "billingAddress" // TODO: could probably break this down
)
