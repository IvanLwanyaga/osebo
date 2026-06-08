package com.osebo.ai.models

data class Payment(
    val id: String = "",
    val transactionId: String = "",
    val amount: Double = 0.0,
    val method: String = "", // Cash, Mobile Money, Card
    val reference: String = "",
    val status: String = "Success",
    val timestamp: Long = System.currentTimeMillis()
)

data class PaymentChannel(
    val id: String = "",
    val name: String = "",
    val type: String = "", // Mobile, Bank, Cash
    val icon: String = ""
)
