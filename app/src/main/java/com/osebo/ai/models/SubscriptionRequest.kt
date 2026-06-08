package com.osebo.ai.models

data class SubscriptionRequest(
    val id: String = "",
    val userId: String = "",
    val planType: String = "", // Basic, Pro, Business
    val amount: Double = 0.0,
    val status: String = "Pending",
    val timestamp: Long = System.currentTimeMillis()
)
