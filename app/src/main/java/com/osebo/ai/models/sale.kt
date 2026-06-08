package com.osebo.ai.models

data class Sale(
    val id: String = "",
    val shopId: String = "",
    val shopName: String = "",
    val cashierId: String = "",
    val cashierName: String = "",
    val ownerId: String = "",

    // Customer information - Enhanced to support different customer types
    val customerId: String = "",
    val customerName: String = "Walk-in Customer",
    val customerType: String = "Walk-in", // Walk-in, Registered, VIP, Wholesale
    val customerPhone: String = "",
    val customerEmail: String = "",
    val customerPoints: Int = 0,
    val customerPointsEarned: Int = 0,
    val customerPointsRedeemed: Int = 0,

    // Sale details
    val totalAmount: Double = 0.0,
    val totalCostPrice: Double = 0.0,
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val discountType: String = "None", // Percentage, Fixed, None
    val discountValue: Double = 0.0,

    // Payment details
    val paymentMethod: String = "Cash", // Cash, Credit Card, Debit Card, Mobile Money, Bank Transfer, Store Credit
    val paymentStatus: String = "Paid", // Paid, Pending, Partial
    val paymentReference: String = "",

    // Status
    val status: String = "Completed", // Completed, Pending, Cancelled, Refunded
    val orderStatus: String = "Delivered", // Pending, Processing, Shipped, Delivered, Cancelled

    // Additional info
    val description: String = "",
    val notes: String = "",
    val items: List<CartItem> = emptyList(),
    val itemCount: Int = 0,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0
) {
    // Aliases
    val cost: Double get() = totalCostPrice

    // Helper function to check if customer is registered
    fun isRegisteredCustomer(): Boolean {
        return customerType != "Walk-in" && customerId.isNotEmpty()
    }

    // Helper function to check if customer is VIP
    fun isVipCustomer(): Boolean {
        return customerType == "VIP"
    }

    // Helper function to check if customer is wholesale
    fun isWholesaleCustomer(): Boolean {
        return customerType == "Wholesale"
    }

    // Helper function to get formatted total
    fun getFormattedTotal(): String {
        return "UGX ${String.format("%,.0f", totalAmount)}"
    }

    // Helper function to get formatted date
    fun getFormattedDate(): String {
        val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date(createdAt))
    }

    // Helper function to get short date
    fun getShortDate(): String {
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date(createdAt))
    }
}

// Customer Types Enum
enum class CustomerType(val displayName: String) {
    WALK_IN("Walk-in Customer"),
    REGISTERED("Registered Customer"),
    VIP("VIP Customer"),
    WHOLESALE("Wholesale Customer");

    companion object {
        fun fromString(type: String): CustomerType {
            return values().find { it.name == type } ?: WALK_IN
        }
    }
}

// Payment Methods Enum
enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    MOBILE_MONEY("Mobile Money"),
    BANK_TRANSFER("Bank Transfer"),
    STORE_CREDIT("Store Credit");

    companion object {
        fun fromString(method: String): PaymentMethod {
            return values().find { it.name == method || it.displayName == method } ?: CASH
        }
    }
}

// Sale Status Enum
enum class SaleStatus(val displayName: String) {
    COMPLETED("Completed"),
    PENDING("Pending"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded");

    companion object {
        fun fromString(status: String): SaleStatus {
            return values().find { it.name == status || it.displayName == status } ?: COMPLETED
        }
    }
}
