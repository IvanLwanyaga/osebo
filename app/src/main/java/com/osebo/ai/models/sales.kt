package com.osebo.ai.models


data class Sale(
    val id: String = "",
    val productName: String = "",
    val amount: Double = 0.0,
    val quantity: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)