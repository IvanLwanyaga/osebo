package com.osebo.ai.models

data class Shop(
    var id: String = "",
    val name: String = "",
    val category: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    val description: String = "",
    val ownerId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)