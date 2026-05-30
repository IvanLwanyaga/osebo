package com.osebo.ai.models

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val stockQuantity: Int = 0,
    val shopId: String = ""
)