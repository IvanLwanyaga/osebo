package com.osebo.ai.data.repositories

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.osebo.ai.models.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "FirebaseRepository"

    // Get today's sales
    suspend fun getTodaySales(): Double {
        return try {
            val today = getStartOfDay()
            val snapshot = db.collection("transactions")
                .whereGreaterThanOrEqualTo("timestamp", Timestamp(today))
                .whereEqualTo("status", "completed")
                .get()
                .await()

            snapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting today's sales: ${e.message}")
            0.0
        }
    }

    // Get yesterday's sales
    suspend fun getYesterdaySales(): Double {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = getStartOfDay(calendar.time)
            val today = getStartOfDay()

            val snapshot = db.collection("transactions")
                .whereGreaterThanOrEqualTo("timestamp", Timestamp(yesterday))
                .whereLessThan("timestamp", Timestamp(today))
                .whereEqualTo("status", "completed")
                .get()
                .await()

            snapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting yesterday's sales: ${e.message}")
            0.0
        }
    }

    // Get open alerts count
    fun getOpenAlerts(): Flow<Int> = callbackFlow {
        val listener = db.collection("alerts")
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(0)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    // Get critical alerts count
    fun getCriticalAlerts(): Flow<Int> = callbackFlow {
        val listener = db.collection("alerts")
            .whereEqualTo("isRead", false)
            .whereEqualTo("type", "critical")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(0)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    // Get staff online status
    fun getStaffOnlineStatus(): Flow<Pair<Int, Int>> = callbackFlow {
        val listener = db.collection("staff")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Pair(0, 0))
                    return@addSnapshotListener
                }
                val totalStaff = snapshot?.size() ?: 0
                val onlineStaff = snapshot?.documents?.count {
                    it.getBoolean("isClockedIn") == true
                } ?: 0
                trySend(Pair(onlineStaff, totalStaff))
            }
        awaitClose { listener.remove() }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    // Get low stock items count
    fun getLowStockItems(): Flow<Int> = callbackFlow {
        val listener = db.collection("products")
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(0)
                    return@addSnapshotListener
                }
                val count = snapshot?.documents?.count { doc ->
                    val quantity = doc.getLong("quantity") ?: 0
                    val minStock = doc.getLong("minStockLevel") ?: 5
                    quantity <= minStock
                } ?: 0
                trySend(count)
            }
        awaitClose { listener.remove() }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    // Get total customers
    fun getTotalCustomers(): Flow<Int> = callbackFlow {
        val listener = db.collection("customers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(0)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    // Get pending suppliers
    fun getPendingSuppliers(): Flow<Int> = callbackFlow {
        val listener = db.collection("suppliers")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(0)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    // Get today's transactions count
    fun getTodayTransactionsCount(): Flow<Int> = callbackFlow {
        val today = getStartOfDay()
        val listener = db.collection("transactions")
            .whereGreaterThanOrEqualTo("timestamp", Timestamp(today))
            .whereEqualTo("status", "completed")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(0)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    // Get pending refunds count
    fun getPendingRefunds(): Flow<Int> = callbackFlow {
        val listener = db.collection("refunds")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(0)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    // Get recent alerts
    fun getRecentAlerts(): Flow<List<Alert>> = callbackFlow {
        val listener = db.collection("alerts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val alerts = snapshot?.toObjects(Alert::class.java) ?: emptyList()
                trySend(alerts)
            }
        awaitClose { listener.remove() }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    // Get low stock products list
    fun getLowStockProducts(): Flow<List<Product>> = callbackFlow {
        val listener = db.collection("products")
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList<Product>())
                    return@addSnapshotListener
                }
                val products = snapshot?.toObjects(Product::class.java)?.filter {
                    it.quantity <= it.minStockLevel
                } ?: emptyList()
                trySend(products)
            }
        awaitClose { listener.remove() }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    // Create discount code
    suspend fun createDiscountCode(discount: Discount): Result<Boolean> {
        return try {
            db.collection("discounts").document(discount.code).set(discount).await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating discount: ${e.message}")
            Result.failure(e)
        }
    }

    // Add expense
    suspend fun addExpense(expense: Expense): Result<Boolean> {
        return try {
            db.collection("expenses").add(expense).await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding expense: ${e.message}")
            Result.failure(e)
        }
    }

    // Update product stock
    suspend fun updateProductStock(productId: String, newQuantity: Int): Result<Boolean> {
        return try {
            db.collection("products").document(productId)
                .update("quantity", newQuantity)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating stock: ${e.message}")
            Result.failure(e)
        }
    }

    // Generate barcode
    suspend fun generateBarcode(productId: String): Result<String> {
        return try {
            val barcode = UUID.randomUUID().toString().take(12).uppercase()
            db.collection("products").document(productId)
                .update("barcode", barcode)
                .await()
            Result.success(barcode)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating barcode: ${e.message}")
            Result.failure(e)
        }
    }

    // Clear all data
    suspend fun clearAllData(adminId: String): Result<Boolean> {
        return try {
            // Log the action
            db.collection("audit_logs").add(mapOf(
                "action" to "clear_all_data",
                "adminId" to adminId,
                "timestamp" to Timestamp.now()
            )).await()

            // Delete transaction data
            val transactions = db.collection("transactions").get().await()
            transactions.documents.forEach { doc ->
                db.collection("transactions").document(doc.id).delete().await()
            }

            // Delete refunds
            val refunds = db.collection("refunds").get().await()
            refunds.documents.forEach { doc ->
                db.collection("refunds").document(doc.id).delete().await()
            }

            // Delete alerts
            val alerts = db.collection("alerts").get().await()
            alerts.documents.forEach { doc ->
                db.collection("alerts").document(doc.id).delete().await()
            }

            // Reset stock levels
            val products = db.collection("products").get().await()
            products.documents.forEach { doc ->
                db.collection("products").document(doc.id).update("quantity", 0).await()
            }

            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing data: ${e.message}")
            Result.failure(e)
        }
    }

    // Lock system (force logout all users)
    suspend fun lockSystem(adminId: String): Result<Boolean> {
        return try {
            // Log lock action
            db.collection("audit_logs").add(mapOf(
                "action" to "system_lock",
                "adminId" to adminId,
                "timestamp" to Timestamp.now()
            )).await()

            // Force all staff to be clocked out
            val staff = db.collection("staff").get().await()
            staff.documents.forEach { doc ->
                db.collection("staff").document(doc.id).update(
                    mapOf(
                        "isClockedIn" to false,
                        "shiftEnd" to Timestamp.now()
                    )
                ).await()
            }

            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error locking system: ${e.message}")
            Result.failure(e)
        }
    }

    // Sign out current user
    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out: ${e.message}")
        }
    }

    // Get current user ID
    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "unknown_user"
    }

    // Helper function to get start of day
    private fun getStartOfDay(date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}
