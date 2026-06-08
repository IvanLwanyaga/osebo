package com.osebo.ai.models

data class SupportMessage(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val message: String = "",
    val status: String = "Unread", // Unread, Read, Replied
    val timestamp: Long = System.currentTimeMillis()
)
