package com.osebo.ai.models

data class PackageType(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val features: List<String> = emptyList(),
    val billingCycle: String = "Monthly"
)
