package com.osebo.ai

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.osebo.ai.data.repositories.FirebaseRepository
import com.osebo.ai.models.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class HRActivity : AppCompatActivity() {

    private lateinit var repository: FirebaseRepository

    // View references
    private lateinit var tvTodaySales: TextView
    private lateinit var tvSalesVsYesterday: TextView
    private lateinit var tvOpenAlerts: TextView
    private lateinit var tvCriticalAlerts: TextView
    private lateinit var tvStaffOnline: TextView
    private lateinit var tvStaffOffline: TextView
    private lateinit var tvLowStock: TextView
    private lateinit var tvLowStockStatus: TextView
    private lateinit var tvTotalCustomers: TextView
    private lateinit var tvPendingSuppliers: TextView
    private lateinit var tvTodayTransactions: TextView
    private lateinit var tvPendingRefunds: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hractivity)

        repository = FirebaseRepository()

        initializeViews()
        setupClickListeners()
        loadDashboardData()
        setupRealTimeListeners()
    }

    private fun initializeViews() {
        tvTodaySales = findViewById(R.id.tvTodaySales)
        tvSalesVsYesterday = findViewById(R.id.tvSalesVsYesterday)
        tvOpenAlerts = findViewById(R.id.tvOpenAlerts)
        tvCriticalAlerts = findViewById(R.id.tvCriticalAlerts)
        tvStaffOnline = findViewById(R.id.tvStaffOnline)
        tvStaffOffline = findViewById(R.id.tvStaffOffline)
        tvLowStock = findViewById(R.id.tvLowStock)
        tvLowStockStatus = findViewById(R.id.tvLowStockStatus)
        tvTotalCustomers = findViewById(R.id.tvTotalCustomers)
        tvPendingSuppliers = findViewById(R.id.tvPendingSuppliers)
        tvTodayTransactions = findViewById(R.id.tvTodayTransactions)
        tvPendingRefunds = findViewById(R.id.tvPendingRefunds)
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                val todaySales = repository.getTodaySales()
                val yesterdaySales = repository.getYesterdaySales()

                tvTodaySales.text = formatCurrency(todaySales)

                val percentageChange = if (yesterdaySales > 0) {
                    ((todaySales - yesterdaySales) / yesterdaySales * 100).toInt()
                } else if (todaySales > 0) 100 else 0

                val arrow = if (percentageChange >= 0) "↑" else "↓"
                tvSalesVsYesterday.text = "$arrow ${kotlin.math.abs(percentageChange)}% vs yesterday"

            } catch (e: Exception) {
                showToast("Error loading sales data: ${e.message}")
            }
        }
    }

    private fun setupRealTimeListeners() {
        lifecycleScope.launch {
            repository.getStaffOnlineStatus().collect { (online, total) ->
                tvStaffOnline.text = "$online / $total"
                tvStaffOffline.text = "${total - online} offline"
            }
        }

        lifecycleScope.launch {
            repository.getLowStockItems().collect { count ->
                tvLowStock.text = count.toString()
                tvLowStockStatus.text = if (count > 0) "$count needs reorder" else "All stock OK"
            }
        }

        lifecycleScope.launch {
            repository.getOpenAlerts().collect { count ->
                tvOpenAlerts.text = count.toString()
            }
        }

        lifecycleScope.launch {
            repository.getCriticalAlerts().collect { count ->
                tvCriticalAlerts.text = "$count critical"
            }
        }

        lifecycleScope.launch {
            repository.getTotalCustomers().collect { count ->
                tvTotalCustomers.text = formatNumber(count)
            }
        }

        lifecycleScope.launch {
            repository.getPendingSuppliers().collect { count ->
                tvPendingSuppliers.text = "$count pending"
            }
        }

        lifecycleScope.launch {
            repository.getTodayTransactionsCount().collect { count ->
                tvTodayTransactions.text = "$count today"
            }
        }

        lifecycleScope.launch {
            repository.getPendingRefunds().collect { count ->
                tvPendingRefunds.text = "$count pending"
            }
        }
    }

    private fun setupClickListeners() {
        findViewById<CardView>(R.id.cardInventory).setOnClickListener {
            showToast("Inventory Management - Coming Soon")
        }

        findViewById<CardView>(R.id.cardFinance).setOnClickListener {
            showToast("Finance Dashboard - Coming Soon")
        }

        findViewById<CardView>(R.id.cardCustomers).setOnClickListener {
            showToast("Customer Management - Coming Soon")
        }

        findViewById<CardView>(R.id.cardSuppliers).setOnClickListener {
            showToast("Supplier Management - Coming Soon")
        }

        findViewById<CardView>(R.id.cardReports).setOnClickListener {
            showToast("Reports - Coming Soon")
        }

        findViewById<CardView>(R.id.cardAlerts).setOnClickListener {
            showAlertsDialog()
        }

        findViewById<CardView>(R.id.cardSaleTransactions).setOnClickListener {
            showToast("Sale Transactions - Coming Soon")
        }

        findViewById<CardView>(R.id.cardRefunds).setOnClickListener {
            showToast("Refunds & Returns - Coming Soon")
        }


        findViewById<CardView>(R.id.cardStaffAccounts).setOnClickListener {
            showToast("Staff Accounts - Coming Soon")
        }

        findViewById<CardView>(R.id.cardRoles).setOnClickListener {
            showToast("Roles & Permissions - Coming Soon")
        }


        findViewById<CardView>(R.id.cardRevenue).setOnClickListener {
            showToast("Revenue & P&L - Coming Soon")
        }

        findViewById<CardView>(R.id.cardExpense).setOnClickListener {
            showExpenseDialog()
        }

        findViewById<CardView>(R.id.cardLowStockStat).setOnClickListener {
            showLowStockDialog()
        }
        findViewById<CardView>(R.id.cardClearData).setOnClickListener {
            showClearDataConfirmation()
        }

    }

    private fun showAlertsDialog() {
        lifecycleScope.launch {
            repository.getRecentAlerts().collect { alerts ->
                if (alerts.isEmpty()) {
                    showToast("No new alerts")
                    return@collect
                }

                val alertMessages = alerts.map { alert ->
                    val typeIcon = when (alert.type) {
                        "critical" -> "🔴"
                        "warning" -> "⚠️"
                        else -> "ℹ️"
                    }
                    "$typeIcon ${alert.title}: ${alert.message}"
                }.toTypedArray()

                AlertDialog.Builder(this@HRActivity)
                    .setTitle("Recent Alerts (${alerts.size})")
                    .setItems(alertMessages) { _, which ->
                        showToast("Selected: ${alerts[which].title}")
                    }
                    .setPositiveButton("View All") { _, _ ->
                        showToast("Opening full alerts page...")
                    }
                    .setNegativeButton("Dismiss", null)
                    .show()
            }
        }
    }

    private fun showLowStockDialog() {
        lifecycleScope.launch {
            repository.getLowStockProducts().collect { products ->
                if (products.isEmpty()) {
                    showToast("No low stock items")
                    return@collect
                }

                val productNames = products.map {
                    "${it.name}: ${it.stock} units (Min: ${it.lowStockAlert})"
                }.toTypedArray()

                AlertDialog.Builder(this@HRActivity)
                    .setTitle("Low Stock Items (${products.size})")
                    .setItems(productNames) { _, which ->
                        val product = products[which]
                        AlertDialog.Builder(this@HRActivity)
                            .setTitle("Reorder ${product.name}")
                            .setMessage("Current stock: ${product.stock}\nMinimum: ${product.lowStockAlert}\nSuggested order: ${product.lowStockAlert * 2} units")
                            .setPositiveButton("Place Order") { _, _ ->
                                showToast("Reorder placed for ${product.name}")
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                    .setPositiveButton("Reorder All") { _, _ ->
                        showToast("Reorder request sent for all items")
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun setItems(
        items: Array<String>,
        listener: (DialogInterface, Int) -> Unit
    ) {
    }

    private fun showDiscountDialog() {
        showToast("Discount Management coming soon")
    }

    private fun showExpenseDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null)

        AlertDialog.Builder(this)
            .setTitle("Add Expense")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val nameInput = dialogView.findViewById<TextInputEditText>(R.id.etExpenseName)
                val costInput = dialogView.findViewById<TextInputEditText>(R.id.etExpenseCost)
                val descriptionInput = dialogView.findViewById<TextInputEditText>(R.id.etExpenseDesc)

                val name = nameInput.text.toString().trim()
                val cost = costInput.text.toString().toDoubleOrNull() ?: 0.0
                val description = descriptionInput.text.toString().trim()

                if (name.isEmpty() || cost <= 0) {
                    showToast("Please enter valid expense details")
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val expense = Expense(
                        name = name,
                        cost = cost,
                        description = description
                    )
                    val result = repository.addExpense(expense)
                    if (result.isSuccess) {
                        showToast("Expense added successfully")
                    } else {
                        showToast("Error adding expense: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearDataConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Clear All Data")
            .setMessage("This action will permanently wipe all transactions and reset stock. This cannot be undone. Are you sure?")
            .setPositiveButton("Yes, Clear All Data") { _, _ ->
                lifecycleScope.launch {
                    val result = repository.clearAllData(repository.getCurrentUserId())
                    if (result.isSuccess) {
                        showToast("All data cleared successfully")
                    } else {
                        showToast("Error clearing data: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSignOutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                repository.signOut()
                showToast("Signed out successfully")
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "US"))
        return format.format(amount).replace("$", "UGX ")
    }

    private fun formatNumber(number: Int): String {
        return when {
            number >= 1_000_000 -> "${number / 1_000_000}M"
            number >= 1_000 -> "${number / 1_000}K"
            else -> number.toString()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
