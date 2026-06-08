package com.osebo.ai.models

data class Transaction(
    val id: String = "",
    val type: String = "Sale", // Sale, Expense, Subscription, Refund
    val amount: Double = 0.0,
    val description: String = "",
    val shopId: String = "",
    val userId: String = "",
    val paymentMethod: String = "Cash",
    val status: String = "Completed",
    val timestamp: Long = System.currentTimeMillis()
)
