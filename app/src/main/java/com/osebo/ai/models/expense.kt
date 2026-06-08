package com.osebo.ai.models

import com.google.firebase.firestore.DocumentId

data class Expense(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val cost: Double = 0.0,
    val amount: Double = 0.0, // Alias for cost
    val description: String = "",
    val shopId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
