package com.osebo.ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.osebo.ai.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
    }

    private fun setupRecyclerViews() {
        // Shops RecyclerView
        binding.recyclerShops.layoutManager = LinearLayoutManager(this)
        val shopAdapter = ShopAdapter(getSampleShops())
        binding.recyclerShops.adapter = shopAdapter

        // Recent Activity RecyclerView
        binding.recyclerRecent.layoutManager = LinearLayoutManager(this)
        val activityAdapter = RecentActivityAdapter(getSampleActivities())
        binding.recyclerRecent.adapter = activityAdapter
    }

    private fun getSampleShops(): List<Shop> {
        return listOf(
            Shop("Main Street Store", "48 products • 12 staff", "KSh 8,120", "↑ 8%", "#F3E8FF"),
            Shop("Fashion Hub", "32 products • 6 staff", "KSh 5,470", "↑ 3.5%", "#FCE7F3"),
            Shop("Fresh Butchery", "20 items • 4 staff", "KSh 4,860", "↑ 1.2%", "#E0F2FE")
        )
    }

    private fun getSampleActivities(): List<RecentActivity> {
        return listOf(
            RecentActivity("Sale — Main Street", "10:34 AM • Shoes x2", "+KSh 3,200", true),
            RecentActivity("Expense — Rent", "09:15 AM • Fashion Hub", "-KSh 1,500", false),
            RecentActivity("Sale — Butchery", "08:02 AM • Beef x5kg", "+KSh 750", true)
        )
    }
}

// Data Models
data class Shop(
    val name: String,
    val info: String,
    val sales: String,
    val growth: String,
    val color: String
)

data class RecentActivity(
    val title: String,
    val time: String,
    val amount: String,
    val isPositive: Boolean
)