package com.osebo.ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.databinding.ActivityReportsBinding
import com.osebo.ai.models.Product
import com.osebo.ai.models.Sale

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchReports()
    }

    private fun fetchReports() {
        val userId = auth.currentUser?.uid ?: return

        // Fetch Sales for Reports (Filtered by ownerId)
        db.collection("sales")
            .whereEqualTo("ownerId", userId)
            .get()
            .addOnSuccessListener { salesDocs ->
                var totalRevenue = 0.0
                salesDocs.documents.forEach { doc ->
                    val sale = doc.toObject(Sale::class.java)
                    if (sale != null) totalRevenue += sale.totalAmount
                }
                binding.txtTotalSalesRevenue.text = "Total Revenue: UGX %,.0f".format(totalRevenue)
                binding.txtTotalTransactionsCount.text = "Total Transactions: ${salesDocs.size()}"
            }

        // Fetch Inventory for Reports
        db.collection("inventory")
            .whereEqualTo("ownerId", userId)
            .get()
            .addOnSuccessListener { productDocs ->
                var totalItems = 0
                var lowStock = 0
                productDocs.documents.forEach { doc ->
                    val product = doc.toObject(Product::class.java)
                    if (product != null) {
                        totalItems += product.quantity
                        if (product.quantity <= product.minStock) lowStock++
                    }
                }
                binding.txtInventoryTotal.text = "Items in Stock: $totalItems"
                binding.txtInventoryLowStock.text = "Low Stock Alerts: $lowStock"
            }
    }
}