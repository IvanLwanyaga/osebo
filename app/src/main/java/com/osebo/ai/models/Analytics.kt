package com.osebo.ai.models

data class TimeSeriesData(
    val label: String = "",
    val value: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class SalesData(
    val totalRevenue: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val transactionCount: Int = 0,
    val period: String = "Daily"
)
