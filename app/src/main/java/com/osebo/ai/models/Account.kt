package com.osebo.ai.models

data class Account(
    val id: String = "",
    val businessName: String = "",
    val regNo: String = "",
    val taxId: String = "",
    val address: String = "",
    val status: String = "Active",
    val paymentMethod: String = "",
    val billingCycle: String = "Monthly",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
