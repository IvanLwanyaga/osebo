package com.osebo.ai.models

data class Receipt(
    val saleId: String = "",
    val shopId: String = "",
    val total: Double = 0.0,
    val createdAt: Long = 0
)