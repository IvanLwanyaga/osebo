package com.osebo.ai.models

data class Employee(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "Staff",
    val shopId: String = "",
    val ownerId: String = "",
    val permissions: List<String> = emptyList(),
    val status: String = "Active",
    val createdAt: Long = System.currentTimeMillis()
)
