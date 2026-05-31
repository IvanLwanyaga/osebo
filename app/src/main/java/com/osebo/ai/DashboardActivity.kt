package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
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

        // Security Check
        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        setCurrentDate()
        setupRecyclerView()
        loadShops()
        setupDashboardActions()
        setupBottomNavigation()
        setupNavigationDrawer()
        
        // Open drawer on menu click
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupNavigationDrawer() {
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    // Already here
                }
                R.id.nav_all_shops -> {
                    startActivity(Intent(this, ShopsActivity::class.java))
                }
                R.id.nav_account -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_contact -> {
                    startActivity(Intent(this, ContactUsActivity::class.java))
                }
                R.id.nav_admin -> {
                    // Map to HR or custom Admin page if exists
                    startActivity(Intent(this, HRActivity::class.java))
                }
                R.id.nav_billing -> {
                    startActivity(Intent(this, FinanceActivity::class.java))
                }
                R.id.nav_logout -> {
                    logout()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupDashboardActions() {
        binding.btnCreateShop.setOnClickListener {
            startActivity(Intent(this, CreateShopActivity::class.java))
        }

        binding.btnNewSale.setOnClickListener {
            startActivity(Intent(this, CreateSaleActivity::class.java))
        }

        binding.btnViewShops.setOnClickListener {
            startActivity(Intent(this, ShopsActivity::class.java))
        }

        binding.btnSeeAll.setOnClickListener {
            startActivity(Intent(this, ShopsActivity::class.java))
        }
        
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, CreateSaleActivity::class.java))
        }
        
        binding.imgAvatar.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
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
                R.id.nav_more -> {
                    startActivity(Intent(this, MoreActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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

    private fun setupRecyclerView() {
        shopAdapter = ShopAdapter(shopList)
        binding.recyclerShops.layoutManager = LinearLayoutManager(this)
        binding.recyclerShops.adapter = shopAdapter
    }

    private fun loadShops() {
        listener = db.collection("shops")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                shopList.clear()
                value?.documents?.forEach { doc ->
                    val shop = doc.toObject(Shop::class.java)
                    if (shop != null) shopList.add(shop)
                }
                shopAdapter.notifyDataSetChanged()
                binding.txtTotalShops.text = shopList.size.toString()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}