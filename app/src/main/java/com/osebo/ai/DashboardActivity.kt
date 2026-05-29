package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.osebo.ai.activities.ShopsActivity
import com.osebo.ai.adapters.ShopAdapter
import com.osebo.ai.databinding.ActivityDashboardBinding
import com.osebo.ai.models.Shop
import com.osebo.ai.utils.FirebaseHelper

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val shopList = mutableListOf<Shop>()
    private lateinit var shopAdapter: ShopAdapter
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadShops()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        shopAdapter = ShopAdapter(shopList)
        binding.recyclerShops.layoutManager = LinearLayoutManager(this)
        binding.recyclerShops.adapter = shopAdapter
    }

    private fun loadShops() {
        listener = FirebaseHelper.firestore.collection("shops")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                shopList.clear()
                value?.documents?.forEach { document ->
                    val shop = document.toObject(Shop::class.java)
                    if (shop != null) {
                        shopList.add(shop)
                    }
                }
                shopAdapter.notifyDataSetChanged()
                binding.txtTotalShops.text = "(${shopList.size})"
            }
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_all_shops -> {
                    startActivity(Intent(this, ShopsActivity::class.java))
                    true
                }
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

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}