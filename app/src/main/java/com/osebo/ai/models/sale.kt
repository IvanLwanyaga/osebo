package com.osebo.ai.models

data class Sale(
    val id: String = "",
    val shopId: String = "",
    val cashierId: String = "",
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "CASH",
    val createdAt: Long = 0
)