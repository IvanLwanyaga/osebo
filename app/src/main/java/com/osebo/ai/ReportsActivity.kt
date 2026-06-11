package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.osebo.ai.models.Expense
import com.osebo.ai.models.Product
import com.osebo.ai.models.Sale
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class ReportsActivity : AppCompatActivity() {

    // Views
    private lateinit var txtTotalRevenue: TextView
    private lateinit var txtTotalSales: TextView
    private lateinit var txtAverageOrder: TextView
    private lateinit var txtConversionRate: TextView
    private lateinit var tvDateRange: TextView
    
    // Trend Views
    private lateinit var tvRevenueTrend: TextView
    private lateinit var tvSalesTrend: TextView

    // Firebase
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Data collections
    private val salesList = mutableListOf<Sale>()
    private val expensesList = mutableListOf<Expense>()
    private val productsList = mutableListOf<Product>()

    // Listeners
    private var salesListener: ListenerRegistration? = null
    private var expensesListener: ListenerRegistration? = null
    private var productsListener: ListenerRegistration? = null

    // Date range
    private var currentStartDate = Calendar.getInstance()
    private var currentEndDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        initViews()
        setupDateRangeSelector()
        setupClickListeners()
        setupRealTimeListeners()
    }

    private fun initViews() {
        txtTotalRevenue = findViewById(R.id.txtTotalRevenue)
        txtTotalSales = findViewById(R.id.txtTotalSales)
        txtAverageOrder = findViewById(R.id.txtAverageOrder)
        txtConversionRate = findViewById(R.id.txtConversionRate)
        tvDateRange = findViewById(R.id.tvDateRange)
        
        tvRevenueTrend = findViewById(R.id.tvRevenueTrend)
        tvSalesTrend = findViewById(R.id.tvSalesTrend)
    }

    private fun setupDateRangeSelector() {
        val dateRangeCard = findViewById<MaterialCardView>(R.id.dateRangeCard)
        dateRangeCard?.setOnClickListener {
            showDateRangeDialog()
        }

        // Initialize with last 30 days
        currentEndDate.time = Date()
        currentStartDate = Calendar.getInstance()
        currentStartDate.add(Calendar.DAY_OF_YEAR, -30)
        updateDateRangeDisplay()
    }

    private fun setupClickListeners() {
        // Detail Navigation
        findViewById<MaterialCardView>(R.id.cardRevenue)?.setOnClickListener {
            startActivity(Intent(this, SalesActivity::class.java))
        }
        
        findViewById<MaterialCardView>(R.id.cardSales)?.setOnClickListener {
            startActivity(Intent(this, SalesActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardAverageOrder)?.setOnClickListener {
            Toast.makeText(this, "Order analysis summary", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialCardView>(R.id.cardConversion)?.setOnClickListener {
            Toast.makeText(this, "Conversion & Customer insights", Toast.LENGTH_SHORT).show()
        }

        // Quick Actions
        findViewById<MaterialCardView>(R.id.btnQuickSale)?.setOnClickListener {
            startActivity(Intent(this, POSActivity::class.java))
        }
        
        findViewById<MaterialCardView>(R.id.btnQuickExpense)?.setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
        }
        
        findViewById<MaterialCardView>(R.id.btnQuickExport)?.setOnClickListener {
            Toast.makeText(this, "Exporting report...", Toast.LENGTH_SHORT).show()
        }

        // View All Reports
        findViewById<MaterialButton>(R.id.btnViewAllReports)?.setOnClickListener {
            Toast.makeText(this, "Showing detailed business analytics", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.tvViewDetails)?.setOnClickListener {
            startActivity(Intent(this, SalesActivity::class.java))
        }
    }

    private fun showDateRangeDialog() {
        val options = arrayOf("Today", "Yesterday", "Last 7 Days", "Last 30 Days", "This Month", "Last Month", "Custom Range")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Date Range")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> setTodayRange()
                    1 -> setYesterdayRange()
                    2 -> setLast7DaysRange()
                    3 -> setLast30DaysRange()
                    4 -> setThisMonthRange()
                    5 -> setLastMonthRange()
                    6 -> showCustomDateRangePicker()
                }
            }
            .show()
    }

    private fun setTodayRange() {
        currentStartDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        currentEndDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        updateDateRangeDisplay()
        refreshData()
    }

    private fun setYesterdayRange() {
        currentStartDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        currentEndDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        updateDateRangeDisplay()
        refreshData()
    }

    private fun setLast7DaysRange() {
        currentEndDate = Calendar.getInstance()
        currentStartDate = Calendar.getInstance()
        currentStartDate.add(Calendar.DAY_OF_YEAR, -7)
        updateDateRangeDisplay()
        refreshData()
    }

    private fun setLast30DaysRange() {
        currentEndDate = Calendar.getInstance()
        currentStartDate = Calendar.getInstance()
        currentStartDate.add(Calendar.DAY_OF_YEAR, -30)
        updateDateRangeDisplay()
        refreshData()
    }

    private fun setThisMonthRange() {
        currentStartDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        currentEndDate = Calendar.getInstance()
        updateDateRangeDisplay()
        refreshData()
    }

    private fun setLastMonthRange() {
        currentStartDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        currentEndDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        updateDateRangeDisplay()
        refreshData()
    }

    private fun showCustomDateRangePicker() {
        Toast.makeText(this, "Custom range selection active", Toast.LENGTH_SHORT).show()
    }

    private fun updateDateRangeDisplay() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val startDateStr = dateFormat.format(currentStartDate.time)
        val endDateStr = dateFormat.format(currentEndDate.time)

        tvDateRange.text = if (startDateStr == endDateStr) {
            startDateStr
        } else {
            "$startDateStr - $endDateStr"
        }
    }

    private fun setupRealTimeListeners() {
        val userId = auth.currentUser?.uid ?: return

        val startTimestamp = currentStartDate.timeInMillis
        val endTimestamp = currentEndDate.timeInMillis

        // Listen to sales
        salesListener = db.collection("sales")
            .whereEqualTo("ownerId", userId)
            .whereGreaterThanOrEqualTo("createdAt", startTimestamp)
            .whereLessThanOrEqualTo("createdAt", endTimestamp)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                salesList.clear()
                snapshot?.toObjects(Sale::class.java)?.let { salesList.addAll(it) }
                
                updateSalesMetrics()
                updateRecentTransactions()
            }

        // Listen to expenses
        expensesListener = db.collection("expenses")
            .whereEqualTo("ownerId", userId)
            .whereGreaterThanOrEqualTo("createdAt", startTimestamp)
            .whereLessThanOrEqualTo("createdAt", endTimestamp)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                expensesList.clear()
                snapshot?.toObjects(Expense::class.java)?.let { expensesList.addAll(it) }
                
                updateRecentTransactions()
            }

        // Listen to products
        productsListener = db.collection("inventory")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                productsList.clear()
                snapshot?.toObjects(Product::class.java)?.let { productsList.addAll(it) }
                updateConversionRate()
            }
    }

    private fun updateSalesMetrics() {
        val totalRevenue = salesList.sumOf { it.totalAmount }
        val totalSalesCount = salesList.size
        val averageOrderValue = if (totalSalesCount > 0) totalRevenue / totalSalesCount else 0.0

        val formatter = NumberFormat.getNumberInstance(Locale.US)

        txtTotalRevenue.text = "UGX ${formatter.format(totalRevenue.roundToInt())}"
        txtTotalSales.text = formatter.format(totalSalesCount)
        txtAverageOrder.text = "UGX ${formatter.format(averageOrderValue.roundToInt())}"

        calculateTrends()
        updateWeeklyChart()
    }

    private fun updateWeeklyChart() {
        val weeklySales = MutableList(7) { 0.0 }
        val calendar = Calendar.getInstance()
        
        salesList.forEach { sale ->
            calendar.timeInMillis = sale.createdAt
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val index = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
            if (index in 0..6) {
                weeklySales[index] += sale.totalAmount
            }
        }

        val maxSales = weeklySales.maxOrNull() ?: 1.0
        val bars = listOf(
            R.id.barMonday, R.id.barTuesday, R.id.barWednesday, 
            R.id.barThursday, R.id.barFriday, R.id.barSaturday, R.id.barSunday
        )

        bars.forEachIndexed { i, resId ->
            findViewById<View>(resId)?.let { bar ->
                val params = bar.layoutParams
                val heightPx = ((weeklySales[i] / maxSales) * 80).toInt()
                params.height = (heightPx * resources.displayMetrics.density).toInt()
                bar.layoutParams = params
            }
        }
    }

    private fun updateConversionRate() {
        val totalSales = salesList.size
        val totalProducts = productsList.size

        val conversionRate = if (totalProducts > 0) {
            (totalSales.toDouble() / (totalProducts * 10).toDouble()) * 100 // Mock logic: assume 10 views per product
        } else {
            0.0
        }
        txtConversionRate.text = String.format("%.1f%%", conversionRate)
    }

    private fun calculateTrends() {
        // Compare current period with previous period of same length
        val diff = currentEndDate.timeInMillis - currentStartDate.timeInMillis
        val prevStart = currentStartDate.timeInMillis - diff
        val prevEnd = currentStartDate.timeInMillis

        db.collection("sales")
            .whereEqualTo("ownerId", auth.currentUser?.uid)
            .whereGreaterThanOrEqualTo("createdAt", prevStart)
            .whereLessThanOrEqualTo("createdAt", prevEnd)
            .get()
            .addOnSuccessListener { snapshot ->
                val prevSales = snapshot.toObjects(Sale::class.java)
                val prevRevenue = prevSales.sumOf { it.totalAmount }
                val currentRevenue = salesList.sumOf { it.totalAmount }
                
                val revenueChange = if (prevRevenue > 0) ((currentRevenue - prevRevenue) / prevRevenue) * 100 else 0.0
                tvRevenueTrend.text = String.format("%+.1f%%", revenueChange)
                tvRevenueTrend.setTextColor(getColor(if (revenueChange >= 0) R.color.success else R.color.danger))

                val prevCount = prevSales.size
                val currentCount = salesList.size
                val countChange = if (prevCount > 0) ((currentCount - prevCount).toDouble() / prevCount) * 100 else 0.0
                tvSalesTrend.text = String.format("%+.1f%%", countChange)
                tvSalesTrend.setTextColor(getColor(if (countChange >= 0) R.color.success else R.color.danger))
            }
    }

    private fun updateRecentTransactions() {
        val recentTransactionsLayout = findViewById<LinearLayout>(R.id.recentTransactionsLayout)
        recentTransactionsLayout?.removeAllViews()

        val allTransactions = mutableListOf<TransactionItem>()

        salesList.forEach { sale ->
            allTransactions.add(
                TransactionItem(
                    title = "Sale #${sale.id.takeLast(6).uppercase()}",
                    amount = sale.totalAmount,
                    type = "sale",
                    status = sale.status,
                    timestamp = sale.createdAt
                )
            )
        }

        expensesList.forEach { expense ->
            allTransactions.add(
                TransactionItem(
                    title = expense.name.ifBlank { "Expense" },
                    amount = expense.amount,
                    type = "expense",
                    status = "Paid",
                    timestamp = expense.createdAt
                )
            )
        }

        allTransactions.sortByDescending { it.timestamp }

        allTransactions.take(4).forEachIndexed { index, transaction ->
            val view = layoutInflater.inflate(R.layout.item_transaction, recentTransactionsLayout, false)
            
            val iconCircle = view.findViewById<LinearLayout>(R.id.iconCircle)
            val icon = view.findViewById<ImageView>(R.id.icon)
            val titleText = view.findViewById<TextView>(R.id.title)
            val timeText = view.findViewById<TextView>(R.id.time)
            val amountText = view.findViewById<TextView>(R.id.amount)
            val statusText = view.findViewById<TextView>(R.id.status)

            titleText.text = transaction.title
            timeText.text = getRelativeTime(transaction.timestamp)

            val formatter = NumberFormat.getNumberInstance(Locale.US)

            if (transaction.type == "sale") {
                icon.setImageResource(R.drawable.ic_sale)
                iconCircle.setBackgroundResource(R.drawable.circle_success)
                amountText.text = "+ UGX ${formatter.format(transaction.amount.roundToInt())}"
                amountText.setTextColor(getColor(R.color.success))
                statusText.text = transaction.status
                statusText.setTextColor(getColor(R.color.success))
            } else {
                icon.setImageResource(R.drawable.ic_expenses)
                iconCircle.setBackgroundResource(R.drawable.circle_warning)
                amountText.text = "- UGX ${formatter.format(transaction.amount.roundToInt())}"
                amountText.setTextColor(getColor(R.color.danger))
                statusText.text = "Completed"
                statusText.setTextColor(getColor(R.color.success))
            }

            recentTransactionsLayout?.addView(view)

            if (index < 3 && index < allTransactions.size - 1) {
                val divider = View(this)
                divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2).apply {
                    setMargins(70, 0, 0, 0)
                }
                divider.setBackgroundColor(getColor(R.color.border))
                recentTransactionsLayout?.addView(divider)
            }
        }
    }

    private fun getRelativeTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            days == 1L -> "Yesterday"
            else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
        }
    }

    private fun refreshData() {
        salesListener?.remove()
        expensesListener?.remove()
        productsListener?.remove()
        setupRealTimeListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        salesListener?.remove()
        expensesListener?.remove()
        productsListener?.remove()
    }

    data class TransactionItem(
        val title: String,
        val amount: Double,
        val type: String,
        val status: String,
        val timestamp: Long
    )
}
