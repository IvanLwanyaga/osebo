package com.osebo.ai.models

data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val buyPrice: Double = 0.0,
    val minStock: Int = 5,
    val shopId: String = "",
    val ownerId: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)