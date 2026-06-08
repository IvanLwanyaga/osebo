package com.osebo.ai.models

data class Session(
    val id: String = "",
    val userId: String = "",
    val shopId: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val totalSales: Double = 0.0,
    val status: String = "Open" // Open, Closed
)
