package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.osebo.ai.databinding.ActivityDashboardBinding
import com.osebo.ai.models.Product
import com.osebo.ai.models.Sale
import com.osebo.ai.models.Shop
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val shopList = mutableListOf<Shop>()
    private lateinit var shopAdapter: ShopAdapter
    
    private var shopsListener: ListenerRegistration? = null
    private var salesListener: ListenerRegistration? = null
    private var inventoryListener: ListenerRegistration? = null

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
    }

    private fun fetchUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("displayName") ?: "Osebo User"
                    binding.txtUsername.text = name
                    
                    // Also update drawer header if possible
                    val headerView = binding.navView.getHeaderView(0)
                    val txtHeaderName = headerView.findViewById<android.widget.TextView>(R.id.txt_header_name)
                    txtHeaderName?.text = name
                }
            }
    }

    private fun setupUI() {
        setCurrentDate()
        
        // Setup RecyclerView
        shopAdapter = ShopAdapter(shopList)
        binding.recyclerShops.layoutManager = LinearLayoutManager(this)
        binding.recyclerShops.adapter = shopAdapter

        // Navigation
        binding.btnMenu.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
        setupNavigationDrawer()
        setupDashboardActions()
        setupBottomNavigation()
    }

    private fun startRealtimeListeners() {
        val userId = auth.currentUser?.uid ?: return

        // 1. Listen for SHOPS (Real-time & isolated to this user)
        shopsListener = db.collection("shops")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                shopList.clear()
                val userShopIds = mutableListOf<String>()
                
                value?.documents?.forEach { doc ->
                    val shop = doc.toObject(Shop::class.java)
                    if (shop != null) {
                        shopList.add(shop)
                        userShopIds.add(shop.id)
                    }
                }
                
                shopAdapter.notifyDataSetChanged()
                binding.txtTotalShops.text = shopList.size.toString()
                
                // Once we have the user's shops, listen for their SALES and INVENTORY
                if (userShopIds.isNotEmpty()) {
                    listenForSales(userShopIds)
                    listenForInventory(userShopIds)
                } else {
                    resetStats()
                }
            }
    }

    private fun listenForInventory(shopIds: List<String>) {
        inventoryListener?.remove()
        inventoryListener = db.collection("inventory")
            .whereIn("shopId", shopIds)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                var totalProducts = 0
                value?.documents?.forEach { doc ->
                    val product = doc.toObject(Product::class.java)
                    if (product != null) {
                        totalProducts += product.quantity
                    }
                }
                binding.txtInventoryStats.text = "Inventory: $totalProducts items"
            }
    }

    private fun listenForSales(shopIds: List<String>) {
        salesListener?.remove()
        
        // 2. Listen for SALES (Real-time & isolated to user's shops)
        // Note: Firestore 'in' query supports up to 30 items. 
        // For professional scale, we might need a different indexing strategy if a user has 30+ shops.
        salesListener = db.collection("sales")
            .whereIn("shopId", shopIds)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                var totalRevenue = 0.0
                var salesCount = 0
                val now = System.currentTimeMillis()
                val oneDayMillis = 24 * 60 * 60 * 1000L
                var todaySales = 0.0

                value?.documents?.forEach { doc ->
                    val sale = doc.toObject(Sale::class.java)
                    if (sale != null) {
                        totalRevenue += sale.totalAmount
                        salesCount++
                        
                        // Calculate today's sales
                        if (now - sale.createdAt < oneDayMillis) {
                            todaySales += sale.totalAmount
                        }
                    }
                }

                updateDashboardStats(totalRevenue, salesCount, todaySales)
            }
    }

    private fun updateDashboardStats(totalRevenue: Double, salesCount: Int, todaySales: Double) {
        binding.txtBalance.text = "UGX %,.0f".format(totalRevenue)
        binding.txtTotalRevenue.text = "Revenue: UGX %,.0f".format(totalRevenue)
        binding.txtTotalTransactions.text = "Sales: $salesCount"
        binding.txtDailySales.text = "UGX %,.0f".format(todaySales)
        
        // Update Profit (Mock logic: 20% margin for now)
        val mockProfit = totalRevenue * 0.2
        binding.txtTotalProfit.text = "Profit: UGX %,.0f".format(mockProfit)
        binding.txtProfitLoss.text = "Profit: UGX %,.0f | Loss: UGX 0".format(mockProfit)
    }

    private fun resetStats() {
        binding.txtBalance.text = "UGX 0"
        binding.txtTotalRevenue.text = "Revenue: UGX 0"
        binding.txtTotalTransactions.text = "Sales: 0"
        binding.txtDailySales.text = "UGX 0"
        binding.txtTotalProfit.text = "Profit: UGX 0"
    }

    private fun setupNavigationDrawer() {
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_all_shops -> startActivity(Intent(this, ShopsActivity::class.java))
                R.id.nav_account -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_contact -> startActivity(Intent(this, ContactUsActivity::class.java))
                R.id.nav_logout -> logout()
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupDashboardActions() {
        binding.btnCreateShop.setOnClickListener { startActivity(Intent(this, CreateShopActivity::class.java)) }
        binding.btnNewSale.setOnClickListener { startActivity(Intent(this, CreateSaleActivity::class.java)) }
        binding.btnViewShops.setOnClickListener { startActivity(Intent(this, ShopsActivity::class.java)) }
        binding.btnSeeAll.setOnClickListener { startActivity(Intent(this, ShopsActivity::class.java)) }
        binding.fabAdd.setOnClickListener { startActivity(Intent(this, CreateSaleActivity::class.java)) }
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_sales -> { startActivity(Intent(this, SalesActivity::class.java)); true }
                R.id.nav_inventory -> { startActivity(Intent(this, InventoryActivity::class.java)); true }
                R.id.nav_reports -> { startActivity(Intent(this, ReportsActivity::class.java)); true }
                R.id.nav_more -> { startActivity(Intent(this, MoreActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setCurrentDate() {
        val date = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())
        binding.txtDate.text = date
    }

    override fun onDestroy() {
        super.onDestroy()
        shopsListener?.remove()
        salesListener?.remove()
        inventoryListener?.remove()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}