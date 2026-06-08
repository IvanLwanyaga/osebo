package com.osebo.ai.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.osebo.ai.*
import com.osebo.ai.adapters.*
import com.osebo.ai.databinding.FragmentShopDashboardBinding
import com.osebo.ai.models.*
import java.text.SimpleDateFormat
import java.util.*

class ShopDashboardFragment : Fragment() {

    private var _binding: FragmentShopDashboardBinding? = null
    private val binding get() = _binding!!

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentShopDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        fetchUserData()
        startRealtimeListeners()
        setupRecyclerView()
    }

    private fun setupUI() {
        val date = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date())
        binding.txtDate.text = date

        binding.btnMenu.setOnClickListener {
            (activity as? DashboardActivity)?.openDrawer()
        }
        binding.btnRefresh.setOnClickListener { startRealtimeListeners() }
        
        binding.btnNotification.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }

        binding.btnNewSale.setOnClickListener {
            findNavController().navigate(R.id.nav_sales)
        }
        binding.btnAddProduct.setOnClickListener {
            findNavController().navigate(R.id.nav_inventory)
        }
        binding.btnCreateShop.setOnClickListener {
            startActivity(Intent(requireContext(), CreateShopActivity::class.java))
        }
        binding.btnAddEmployee.setOnClickListener {
            findNavController().navigate(R.id.nav_admin)
        }
        binding.btnReports.setOnClickListener {
            findNavController().navigate(R.id.nav_reports)
        }
        binding.btnSeeAll.setOnClickListener {
            startActivity(Intent(requireContext(), ShopsActivity::class.java))
        }
        binding.imgAvatar.setOnClickListener {
            findNavController().navigate(R.id.nav_account)
        }
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
                if (error != null) return@addSnapshotListener
                allSales.clear()
                value?.documents?.forEach { doc ->
                    doc.toObject(Sale::class.java)?.let { allSales.add(it) }
                }
                binding.txtSalesCount.text = allSales.size.toString()
                binding.txtOrders.text = allSales.size.toString()
                updateDashboardStats()
            }

        expensesListener = db.collection("expenses")
            .whereIn("shopId", chunk)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                allExpenses.clear()
                value?.documents?.forEach { doc ->
                    doc.toObject(Expense::class.java)?.let { allExpenses.add(it) }
                }
                updateDashboardStats()
            }

        productsListener = db.collection("inventory")
            .whereIn("shopId", chunk)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val count = value?.size() ?: 0
                binding.txtProducts.text = count.toString()
            }

        customersListener = db.collection("customers")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val count = value?.size() ?: 0
                binding.txtCustomers.text = count.toString()
            }
    }

    private fun updateDashboardStats() {
        val totalRevenue = allSales.sumOf { it.totalAmount.toDouble() }
        val totalExp = allExpenses.sumOf { it.cost.toDouble() }
        val totalCost = allSales.sumOf { it.totalCostPrice.toDouble() }
        val totalProfit = (totalRevenue - totalCost - totalExp).coerceAtLeast(0.0)
        
        binding.txtBalance.text = "UGX ${formatAmount(totalRevenue)}"
        binding.txtMiniProfit.text = "UGX ${formatAmount(totalProfit)}"
        binding.txtMiniTransactions.text = allSales.size.toString()
        
        binding.txtGrowth.text = "Total"
        binding.txtGrowth.setBackgroundResource(R.drawable.badge_green)

        updatePerformanceList()
        updateRecentActivities()
    }

    private fun updatePerformanceList() {
        performanceList.clear()
        shopList.forEach { shop ->
            val sales = allSales.filter { it.shopId == shop.id }.sumOf { it.totalAmount.toDouble() }
            val expenses = allExpenses.filter { it.shopId == shop.id }.sumOf { it.cost.toDouble() }
            performanceList.add(ShopPerformance(shop.name, sales, expenses))
        }
        performanceAdapter.notifyDataSetChanged()
    }

    private fun updateRecentActivities() {
        recentList.clear()
        allSales.sortedByDescending { it.timestamp }.take(5).forEach { sale ->
            recentList.add(RecentActivity(
                title = "Sale — ${sale.shopName}",
                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(sale.timestamp)),
                amount = "+UGX ${formatAmount(sale.totalAmount)}",
                isPositive = true
            ))
        }
        recentAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        shopAdapter = ShopAdapter(shopList)
        binding.recyclerShops.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shopAdapter
            isNestedScrollingEnabled = false
        }

        performanceAdapter = ShopPerformanceAdapter(performanceList)
        binding.recyclerShopPerformance.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = performanceAdapter
            isNestedScrollingEnabled = false
        }

        recentAdapter = RecentActivityAdapter(recentList)
        binding.recyclerRecentActivities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentAdapter
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

    private fun formatAmount(amount: Double): String = String.format("%,.0f", amount)

    override fun onDestroyView() {
        super.onDestroyView()
        shopsListener?.remove()
        salesListener?.remove()
        expensesListener?.remove()
        _binding = null
    }
}
