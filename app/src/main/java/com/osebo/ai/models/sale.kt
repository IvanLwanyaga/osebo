package com.osebo.ai.models

data class Sale(
    val id: String = "",
    val shopId: String = "",
    val cashierId: String = "",
    val ownerId: String = "",
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "Cash",
    val createdAt: Long = System.currentTimeMillis()
)