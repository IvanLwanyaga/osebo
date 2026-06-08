package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.adapters.SaleItemAdapter
import com.osebo.ai.databinding.ActivityCreateSaleBinding
import com.osebo.ai.models.CartItem

class CreateSaleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateSaleBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val cartItems = mutableListOf<CartItem>()
    private lateinit var summaryAdapter: SaleItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateSaleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Stepper tabs (Inner)
        binding.stepperTabs.getTabAt(0)?.select()

        setupUI()
    }

    private fun setupUI() {
        // Summary list
        summaryAdapter = SaleItemAdapter(cartItems) {
            updateSummary()
        }
        binding.recyclerSummaryItems.apply {
            layoutManager = LinearLayoutManager(this@CreateSaleActivity)
            adapter = summaryAdapter
        }

        // Setup Customer Types
        val types = arrayOf("Registered", "Walk-in", "Wholesale", "VIP")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)
        binding.spinCustType.adapter = adapter

        updateSummary()
    }

    private fun updateSummary() {
        val total = cartItems.sumOf { it.price * it.quantity }
        binding.txtSubtotal.text = "UGX ${String.format("%,.0f", total)}"
        // Logic for VAT and discounts can be added here
    }
}
