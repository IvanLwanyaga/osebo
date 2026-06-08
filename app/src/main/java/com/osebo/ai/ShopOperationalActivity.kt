package com.osebo.ai

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.osebo.ai.databinding.ActivityShopOperationalBinding
import com.osebo.ai.databinding.DialogAddExpenseBinding
import com.osebo.ai.models.*
import java.util.*

class ShopOperationalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopOperationalBinding
    private val db = FirebaseFirestore.getInstance()
    private var shopId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopOperationalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        shopId = intent.getStringExtra("SHOP_ID")

        setupUI()
        loadShopData()
        loadStats()
        loadTopItems()
        loadRecentTransactions()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnExpenses.setOnClickListener { showAddExpenseDialog() }
        binding.btnNewSale.setOnClickListener {
            val intent = Intent(this, CreateSaleActivity::class.java)
            intent.putExtra("SHOP_ID", shopId)
            startActivity(intent)
        }
        
        binding.btnRestock.setOnClickListener {
            val intent = Intent(this, InventoryActivity::class.java)
            intent.putExtra("SHOP_ID", shopId)
            startActivity(intent)
        }
    }

    private fun loadShopData() {
        shopId?.let { id ->
            db.collection("shops").document(id).get()
                .addOnSuccessListener { doc ->
                    val shop = doc.toObject(Shop::class.java)
                    if (shop != null) {
                        binding.toolbar.title = shop.name
                        // Load owner details if available
                        db.collection("users").document(shop.ownerId).get()
                            .addOnSuccessListener { userDoc ->
                                binding.txtOwnerName.text = userDoc.getString("displayName") ?: "Owner"
                            }
                    }
                }
        }
    }

    private fun loadStats() {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        shopId?.let { id ->
            // Sales today
            db.collection("sales")
                .whereEqualTo("shopId", id)
                .whereGreaterThanOrEqualTo("createdAt", startOfDay)
                .get()
                .addOnSuccessListener { query ->
                    var todayTotal = 0.0
                    query.documents.forEach { todayTotal += it.getDouble("totalAmount") ?: 0.0 }
                    binding.txtTodaySales.text = "UGX ${formatAmount(todayTotal)}"
                    updateBalance()
                }

            // Expenses total (or today)
            db.collection("expenses")
                .whereEqualTo("shopId", id)
                .get()
                .addOnSuccessListener { query ->
                    var totalExp = 0.0
                    query.documents.forEach { totalExp += it.getDouble("cost") ?: 0.0 }
                    binding.txtTotalExpenses.text = "UGX ${formatAmount(totalExp)}"
                    updateBalance()
                }
                
            // Counts
            db.collection("users").whereEqualTo("shopId", id).get().addOnSuccessListener { binding.chipEmployees.text = "Staff: ${it.size()}" }
            db.collection("customers").whereEqualTo("shopId", id).get().addOnSuccessListener { binding.chipCustomers.text = "Customers: ${it.size()}" }
            db.collection("suppliers").whereEqualTo("shopId", id).get().addOnSuccessListener { binding.chipSuppliers.text = "Suppliers: ${it.size()}" }
        }
    }

    private fun updateBalance() {
        // Simple mock balance for now
        val salesText = binding.txtTodaySales.text.toString().replace("UGX ", "").replace(",", "").toDoubleOrNull() ?: 0.0
        val expText = binding.txtTotalExpenses.text.toString().replace("UGX ", "").replace(",", "").toDoubleOrNull() ?: 0.0
        binding.txtTodayBalance.text = "UGX ${formatAmount(salesText - expText)}"
    }

    private fun loadTopItems() {
        shopId?.let { id ->
            db.collection("inventory")
                .whereEqualTo("shopId", id)
                .orderBy("quantity", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener { query ->
                    val items = query.documents.map { 
                        Pair(it.getString("name") ?: "Item", it.getLong("quantity").toString())
                    }
                    binding.recyclerTopByQty.layoutManager = LinearLayoutManager(this)
                    binding.recyclerTopByQty.adapter = com.osebo.ai.adapters.TopItemAdapter(items)
                }

            // Top by amount usually needs sales analysis, for now just show value in stock
            db.collection("inventory")
                .whereEqualTo("shopId", id)
                .get()
                .addOnSuccessListener { query ->
                    val items = query.documents.map {
                        val price = it.getDouble("sellingPrice") ?: 0.0
                        val qty = it.getLong("quantity") ?: 0
                        Pair(it.getString("name") ?: "Item", "UGX ${formatAmount(price * qty)}")
                    }.sortedByDescending { it.second }.take(5)
                    
                    binding.recyclerTopByAmount.layoutManager = LinearLayoutManager(this)
                    binding.recyclerTopByAmount.adapter = com.osebo.ai.adapters.TopItemAdapter(items)
                }
        }
    }

    private fun loadRecentTransactions() {
        shopId?.let { id ->
            db.collection("sales")
                .whereEqualTo("shopId", id)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener { query ->
                    val sales = query.toObjects(Sale::class.java)
                    binding.recyclerRecentTransactions.layoutManager = LinearLayoutManager(this)
                    binding.recyclerRecentTransactions.adapter = com.osebo.ai.adapters.TransactionAdapter(sales)
                }
        }
    }

    private fun showAddExpenseDialog() {
        val dialogBinding = DialogAddExpenseBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        val categories = listOf("Rent", "Utility", "Salary", "Inventory", "Marketing", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        dialogBinding.autoExpenseCategory.setAdapter(adapter)

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSubmitExpense.setOnClickListener {
            val name = dialogBinding.etExpenseName.text.toString()
            val cost = dialogBinding.etExpenseCost.text.toString().toDoubleOrNull() ?: 0.0
            val cat = dialogBinding.autoExpenseCategory.text.toString()
            val desc = dialogBinding.etExpenseDesc.text.toString()

            if (name.isNotEmpty() && cost > 0) {
                val expenseId = UUID.randomUUID().toString()
                val expense = Expense(expenseId, name, cat, cost, desc, shopId ?: "")
                db.collection("expenses").document(expenseId).set(expense)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadStats()
                    }
            } else {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun formatAmount(amount: Double): String {
        return String.format("%,.0f", amount)
    }
}