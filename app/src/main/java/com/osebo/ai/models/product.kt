package com.osebo.ai.models

import com.google.firebase.firestore.DocumentId

data class Product(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val sku: String = "",
    val barcode: String = "",
    val category: String = "",
    val supplier: String = "",
    val description: String = "",

    // Product Image
    val imageUrl: String = "",

    // Pricing
    val price: Double = 0.0, // Selling price
    val costPrice: Double = 0.0,
    val taxRate: Double = 0.0,

    // Stock
    val stock: Int = 0, // Current quantity
    val lowStockAlert: Int = 5, // minStock

    // Physical
    val weight: Double = 0.0,

    // Meta
    val shopId: String = "",
    val ownerId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
) {
    // Aliases for compatibility
    val sellingPrice: Double get() = price
    val unitPrice: Double get() = price
    val quantity: Int get() = stock
    val minStock: Int get() = lowStockAlert
    val minStockLevel: Int get() = lowStockAlert
    val cost: Double get() = costPrice
}
