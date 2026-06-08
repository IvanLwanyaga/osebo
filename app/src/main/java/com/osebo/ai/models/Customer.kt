package com.osebo.ai.models

import com.google.firebase.firestore.DocumentId

data class Customer(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val tier: String = "NEW",
    val lastPurchase: String = "",
    val totalSpent: String = "0",
    val totalSpentAmount: Double = 0.0,
    val totalDebt: Double = 0.0,
    val orders: Int = 0,
    val avatar: String = "",
    val totalPurchases: Double = 0.0,
    val ownerId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
