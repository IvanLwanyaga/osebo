package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.osebo.ai.activities.ShopsActivity
import com.osebo.ai.databinding.ActivityDashboardBinding
import com.osebo.ai.models.Shop
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    private val shopList = mutableListOf<Shop>()
    private lateinit var shopAdapter: ShopAdapter
    private var listener: ListenerRegistration? = null

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // -------------------------
        // SECURITY CHECK
        // -------------------------
        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        setCurrentDate()
        setupRecyclerView()
        loadShops()
        setupDashboardCards()
        setupBottomNavigation()
        setupLogout()
    }

    // -------------------------
    // LOGOUT (SAFE IMPLEMENTATION)
    // -------------------------
    private fun setupLogout() {

        // OPTION 1: If you add btn_logout in XML
//        try {
//            binding.btnLogout.setOnClickListener {
//                logout()
//            }
//        } catch (e: Exception) {
//            // Ignore if button does not exist in XML
//        }

        // OPTION 2: Drawer logout (recommended)
        binding.navView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_logout -> {
                    logout()
                    true
                }

                else -> false
            }
        }
    }

    private fun logout() {

        auth.signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    // -------------------------
    // DATE
    // -------------------------
    private fun setCurrentDate() {

        val date = SimpleDateFormat(
            "EEEE, dd MMM yyyy",
            Locale.getDefault()
        ).format(Date())

        binding.txtDate.text = date
    }

    // -------------------------
    // RECYCLER
    // -------------------------
    private fun setupRecyclerView() {

        shopAdapter = ShopAdapter(shopList)

        binding.recyclerShops.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerShops.adapter =
            shopAdapter
    }

    private fun loadShops() {

        listener = db.collection("shops")
            .addSnapshotListener { value, error ->

                if (error != null) return@addSnapshotListener

                shopList.clear()

                value?.documents?.forEach { doc ->

                    val shop = doc.toObject(Shop::class.java)

                    if (shop != null) {
                        shopList.add(shop)
                    }
                }

                shopAdapter.notifyDataSetChanged()

                binding.txtTotalShops.text =
                    shopList.size.toString()
            }
    }

    // -------------------------
    // DASHBOARD CARDS
    // -------------------------
    private fun setupDashboardCards() {

        binding.btnCreateShop.setOnClickListener {
            startActivity(Intent(this, CreateShopActivity::class.java))
        }

        binding.btnNewSale.setOnClickListener {
            startActivity(Intent(this, CreateSaleActivity::class.java))
        }

        binding.btnViewShops.setOnClickListener {
            startActivity(Intent(this, ShopsActivity::class.java))
        }
    }

    // -------------------------
    // BOTTOM NAVIGATION
    // -------------------------
    private fun setupBottomNavigation() {

        binding.bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_home -> true

                R.id.nav_sales -> {
                    startActivity(Intent(this, CreateSaleActivity::class.java))
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

                R.id.nav_more -> {
                    startActivity(Intent(this, MoreActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    // -------------------------
    // CLEANUP
    // -------------------------
    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}