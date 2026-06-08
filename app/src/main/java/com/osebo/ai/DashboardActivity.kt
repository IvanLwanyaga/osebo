package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.osebo.ai.adapters.ShopAdapter
import com.osebo.ai.adapters.ShopPerformanceAdapter
import com.osebo.ai.adapters.RecentActivityAdapter
import com.osebo.ai.databinding.ActivityDashboardBinding
import com.osebo.ai.models.*
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val shopList = mutableListOf<Shop>()
    private lateinit var shopAdapter: ShopAdapter

    private val performanceList = mutableListOf<ShopPerformance>()
    private lateinit var performanceAdapter: ShopPerformanceAdapter

    private val recentList = mutableListOf<RecentActivity>()
    private lateinit var recentAdapter: RecentActivityAdapter

    private var allSales = mutableListOf<Sale>()
    private var allExpenses = mutableListOf<Expense>()

    private var shopsListener: ListenerRegistration? = null
    private var salesListener: ListenerRegistration? = null
    private var expensesListener: ListenerRegistration? = null
    private var productsListener: ListenerRegistration? = null
    private var customersListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        setupUI()
        fetchUserData()
        startRealtimeListeners()
        setupRecyclerView()
    }

    private fun setupUI() {
        val date = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date())
        binding.txtDate.text = date

        binding.btnMenu.setOnClickListener { openDrawer() }
        binding.btnRefresh.setOnClickListener { startRealtimeListeners() }
        binding.btnNotification.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        binding.btnNewSale.setOnClickListener      { startActivity(Intent(this, CreateSaleActivity::class.java)) }
        binding.btnAddProduct.setOnClickListener   { startActivity(Intent(this, AddProductActivity::class.java)) }
        binding.btnCreateShop.setOnClickListener   { startActivity(Intent(this, CreateShopActivity::class.java)) }
        binding.btnAddEmployee.setOnClickListener  { startActivity(Intent(this, EmployeeActivity::class.java)) }
        binding.btnReports.setOnClickListener      { startActivity(Intent(this, ReportsActivity::class.java)) }
        binding.btnSeeAll.setOnClickListener       { startActivity(Intent(this, ShopsActivity::class.java)) }
        binding.btnCustomers.setOnClickListener    { startActivity(Intent(this, CustomersActivity::class.java)) }

        binding.imgAvatar.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        setupBottomNavigation()
        setupNavigationDrawer()
    }

    private fun startRealtimeListeners() {
        val userId = auth.currentUser?.uid ?: return

        shopsListener = db.collection("shops")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { value, error ->
                if (error != null || value == null) return@addSnapshotListener

                shopList.clear()
                value.documents.forEach { doc ->
                    doc.toObject(Shop::class.java)?.let {
                        it.id = doc.id
                        shopList.add(it)
                    }
                }
                shopAdapter.notifyDataSetChanged()
                binding.txtTotalShops.text = shopList.size.toString()

                if (shopList.isNotEmpty()) {
                    listenForData(shopList.map { it.id })
                } else {
                    allSales.clear()
                    allExpenses.clear()
                    updateDashboardStats()
                    
                    binding.txtProducts.text = "0"
                    binding.txtCustomers.text = "0"
                    binding.txtSalesCount.text = "0"
                    binding.txtOrders.text = "0"
                }
            }
    }

    private fun listenForData(shopIds: List<String>) {
        salesListener?.remove()
        expensesListener?.remove()
        productsListener?.remove()
        customersListener?.remove()

        val userId = auth.currentUser?.uid ?: return
        val chunk = shopIds.take(30)

        salesListener = db.collection("sales")
            .whereIn("shopId", chunk)
            .addSnapshotListener { value, error ->
                if (error != null || value == null) return@addSnapshotListener
                allSales.clear()
                value.documents.forEach { doc ->
                    val sale = doc.toObject(Sale::class.java) ?: return@forEach
                    allSales.add(sale)
                }
                binding.txtSalesCount.text = allSales.size.toString()
                binding.txtOrders.text = allSales.size.toString()
                updateDashboardStats()
            }

        expensesListener = db.collection("expenses")
            .whereIn("shopId", chunk)
            .addSnapshotListener { value, error ->
                if (error != null || value == null) return@addSnapshotListener
                allExpenses.clear()
                value.documents.forEach { doc ->
                    doc.toObject(Expense::class.java)?.let { allExpenses.add(it) }
                }
                updateDashboardStats()
            }

        productsListener = db.collection("inventory")
            .whereIn("shopId", chunk)
            .addSnapshotListener { value, error ->
                if (error != null || value == null) return@addSnapshotListener
                binding.txtProducts.text = value.size().toString()
            }

        customersListener = db.collection("customers")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { value, error ->
                if (error != null || value == null) return@addSnapshotListener
                binding.txtCustomers.text = value.size().toString()
            }
    }

    private fun updateDashboardStats() {
        val totalRevenue = allSales.sumOf { it.totalAmount.toDouble() }
        val totalExp     = allExpenses.sumOf { it.cost.toDouble() }
        val totalCost    = allSales.sumOf { it.totalCostPrice.toDouble() }
        val totalProfit  = (totalRevenue - totalCost - totalExp)
            .coerceAtLeast(0.0)

        binding.txtBalance.text          = "UGX ${formatAmount(totalRevenue)}"
        binding.txtMiniProfit.text       = "UGX ${formatAmount(totalProfit)}"
        binding.txtMiniTransactions.text = allSales.size.toString()

        setupWeeklySalesChart(allSales)
        setupRevenueChart(allSales)

        updatePerformanceList()
        updateRecentActivities()
    }

    private fun setupWeeklySalesChart(sales: List<Sale>) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val weekStart = cal.timeInMillis

        val dailyTotals = FloatArray(7) { 0f }
        val saleCal = Calendar.getInstance()

        sales.filter { it.timestamp >= weekStart }.forEach { sale ->
            saleCal.timeInMillis = sale.timestamp
            val index = (saleCal.get(Calendar.DAY_OF_WEEK) + 5) % 7
            dailyTotals[index] += sale.totalAmount.toFloat()
        }

        val entries = dailyTotals.mapIndexed { i, v -> Entry(i.toFloat(), v) }

        val dataSet = LineDataSet(entries, "This week's sales").apply {
            lineWidth        = 3f
            circleRadius     = 4f
            setDrawValues(false)
            mode             = LineDataSet.Mode.CUBIC_BEZIER
            color            = android.graphics.Color.BLUE
            setCircleColor(android.graphics.Color.BLUE)
        }

        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        binding.weeklySalesChart.apply {
            data        = LineData(dataSet)
            description.isEnabled = false
            axisRight.isEnabled   = false
            xAxis.apply {
                position       = XAxis.XAxisPosition.BOTTOM
                granularity    = 1f
                valueFormatter = IndexAxisValueFormatter(dayLabels)
                setDrawGridLines(false)
            }
            axisLeft.setDrawGridLines(false)
            animateX(500)
            invalidate()
        }
    }

    private fun setupRevenueChart(sales: List<Sale>) {
        data class MonthBucket(val year: Int, val month: Int, var total: Float = 0f)

        val cal = Calendar.getInstance()
        val buckets = (5 downTo 0).map { offset ->
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.MONTH, -offset)
            MonthBucket(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        }

        val saleCal = Calendar.getInstance()
        sales.forEach { sale ->
            saleCal.timeInMillis = sale.timestamp
            val y = saleCal.get(Calendar.YEAR)
            val m = saleCal.get(Calendar.MONTH)
            buckets.find { it.year == y && it.month == m }?.let {
                it.total += sale.totalAmount.toFloat()
            }
        }

        val entries = buckets.mapIndexed { i, b -> BarEntry(i.toFloat(), b.total) }

        val dataSet = BarDataSet(entries, "Monthly revenue").apply {
            setDrawValues(false)
            color = android.graphics.Color.CYAN
        }

        val monthLabels = buckets.map { b ->
            val tmp = Calendar.getInstance().apply { set(b.year, b.month, 1) }
            SimpleDateFormat("MMM", Locale.getDefault()).format(tmp.time)
        }

        binding.monthlyRevenueChart.apply {
            data        = BarData(dataSet)
            description.isEnabled = false
            axisRight.isEnabled   = false
            xAxis.apply {
                position       = XAxis.XAxisPosition.BOTTOM
                granularity    = 1f
                valueFormatter = IndexAxisValueFormatter(monthLabels)
                setDrawGridLines(false)
            }
            axisLeft.setDrawGridLines(false)
            animateY(500)
            invalidate()
        }
    }

    private fun updatePerformanceList() {
        performanceList.clear()
        shopList.forEach { shop ->
            val sales    = allSales.filter    { it.shopId == shop.id }.sumOf { it.totalAmount.toDouble() }
            val expenses = allExpenses.filter { it.shopId == shop.id }.sumOf { it.cost.toDouble() }
            performanceList.add(ShopPerformance(shop.name, sales, expenses))
        }
        performanceAdapter.notifyDataSetChanged()
    }

    private fun updateRecentActivities() {
        recentList.clear()
        allSales.sortedByDescending { it.timestamp }.take(5).forEach { sale ->
            recentList.add(
                RecentActivity(
                    title      = "Sale — ${sale.shopName}",
                    time       = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(sale.timestamp)),
                    amount     = "+UGX ${formatAmount(sale.totalAmount)}",
                    isPositive = true
                )
            )
        }
        recentAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        shopAdapter = ShopAdapter(shopList)
        binding.recyclerShops.apply {
            layoutManager          = LinearLayoutManager(this@DashboardActivity)
            adapter                = shopAdapter
            isNestedScrollingEnabled = false
        }

        performanceAdapter = ShopPerformanceAdapter(performanceList)
        binding.recyclerShopPerformance.apply {
            layoutManager          = LinearLayoutManager(this@DashboardActivity)
            adapter                = performanceAdapter
            isNestedScrollingEnabled = false
        }

        recentAdapter = RecentActivityAdapter(recentList)
        binding.recyclerRecentActivities.apply {
            layoutManager          = LinearLayoutManager(this@DashboardActivity)
            adapter                = recentAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.txtUsername.text = doc.getString("displayName") ?: "User"
                }
            }
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_sales -> {
                    startActivity(Intent(this, SalesActivity::class.java))
                    true
                }
                R.id.nav_inventory -> {
                    startActivity(Intent(this, InventoryActivity::class.java))
                    true
                }
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNavigationDrawer() {
        binding.navView.setNavigationItemSelectedListener { item ->
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_logout -> {
                    auth.signOut()
                    goToLogin()
                    true
                }
                else -> false
            }
        }
    }

    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun formatAmount(amount: Double): String = String.format("%,.0f", amount)

    override fun onDestroy() {
        super.onDestroy()
        shopsListener?.remove()
        salesListener?.remove()
        expensesListener?.remove()
        productsListener?.remove()
        customersListener?.remove()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
