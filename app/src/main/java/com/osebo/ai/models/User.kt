package com.osebo.ai.models

data class User(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "",
    val shopId: String = "",
    val ownerId: String = "",
    val isActive: Boolean = true
)