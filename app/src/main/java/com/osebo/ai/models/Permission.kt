package com.osebo.ai.models

data class Permission(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val group: String = "General" // Inventory, Sales, HR, Admin
)

data class UpdatePermissions(
    val employeeId: String = "",
    val permissions: List<String> = emptyList()
)
