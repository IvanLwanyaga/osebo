package com.osebo.ai.models

data class CartItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val costPrice: Double = 0.0,
    var quantity: Int = 0
)
