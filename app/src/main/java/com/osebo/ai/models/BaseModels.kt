package com.osebo.ai.models

data class AppResponse<T>(
    val status: String = "success",
    val message: String = "",
    val data: T? = null
)

data class HistoryItem(
    val id: String = "",
    val action: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ShopPerformance(
    val shopName: String = "",
    val sales: Double = 0.0,
    val expenses: Double = 0.0
)

object SubscriptionConstants {
    const val STATUS_PENDING = "Pending"
    const val STATUS_ACTIVE = "Active"
    const val STATUS_EXPIRED = "Expired"
    const val STATUS_CANCELLED = "Cancelled"
}
