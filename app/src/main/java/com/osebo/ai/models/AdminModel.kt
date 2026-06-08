package com.osebo.ai.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Date

data class DashboardStats(
    val todaySales: Double = 0.0,
    val yesterdaySales: Double = 0.0,
    val openAlerts: Int = 0,
    val criticalAlerts: Int = 0,
    val staffOnline: Int = 0,
    val totalStaff: Int = 0,
    val lowStockItems: Int = 0,
    val needsReorder: Int = 0,
    val totalCustomers: Int = 0,
    val pendingSuppliers: Int = 0,
    val todayTransactions: Int = 0,
    val pendingRefunds: Int = 0
)

data class SaleTransaction(
    @DocumentId
    val id: String = "",
    val amount: Double = 0.0,
    val timestamp: Timestamp = Timestamp.now(),
    val cashierId: String = "",
    val cashierName: String = "",
    val paymentMethod: String = "",
    val items: List<SaleItem> = emptyList(),
    val status: String = "completed"
)

data class SaleItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val total: Double = 0.0
)

data class Staff(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "cashier",
    val isActive: Boolean = true,
    val lastLogin: Timestamp? = null,
    val shiftStart: Timestamp? = null,
    val shiftEnd: Timestamp? = null,
    val isClockedIn: Boolean = false,
    val salesToday: Double = 0.0
)

data class Alert(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "info",
    val isRead: Boolean = false,
    val timestamp: Timestamp = Timestamp.now(),
    val priority: Int = 0
)

data class Supplier(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val status: String = "pending"
)

data class RefundRequest(
    @DocumentId
    val id: String = "",
    val transactionId: String = "",
    val amount: Double = 0.0,
    val reason: String = "",
    val status: String = "pending",
    val requestedBy: String = "",
    val approvedBy: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

data class Discount(
    @DocumentId
    val id: String = "",
    val code: String = "",
    val description: String = "",
    val type: String = "percentage",
    val value: Double = 0.0,
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val isActive: Boolean = true,
    val usageLimit: Int = 0,
    val usedCount: Int = 0
)
