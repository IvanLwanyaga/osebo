package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.osebo.ai.adapters.SaleAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.osebo.ai.databinding.ActivitySalesBinding
import com.osebo.ai.models.Sale
import com.osebo.ai.models.SaleStatus
import com.osebo.ai.models.PaymentMethod
import com.osebo.ai.models.CustomerType
import java.text.SimpleDateFormat
import java.util.*

class SalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalesBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var salesAdapter: SaleAdapter
    private var salesList = mutableListOf<Sale>()
    private var filteredSalesList = mutableListOf<Sale>()

    private var selectedInterval = "All"
    private var selectedPayment = "All"
    private var selectedStatus = "All"
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Firebase.firestore

        setupToolbar()
        setupRecyclerView()
        setupFilters()
        setupSearch()
        loadSales()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        salesAdapter = SaleAdapter(filteredSalesList) { sale ->
            val intent = Intent(this, SaleReceiptActivity::class.java)
            intent.putExtra("SALE_ID", sale.id)
            startActivity(intent)
        }

        binding.recyclerSales.apply {
            layoutManager = LinearLayoutManager(this@SalesActivity)
            adapter = salesAdapter
        }
    }

    private fun setupFilters() {
        val intervals = arrayOf("All", "Today", "This Week", "This Month")
        val adapterInterval = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, intervals)
        binding.autoInterval.setAdapter(adapterInterval)
        binding.autoInterval.setOnItemClickListener { _, _, position, _ ->
            selectedInterval = intervals[position]
            applyFilters()
        }

        val payments = listOf("All") + PaymentMethod.values().map { it.displayName }
        val adapterPayment = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, payments)
        binding.autoPayment.setAdapter(adapterPayment)
        binding.autoPayment.setOnItemClickListener { _, _, position, _ ->
            selectedPayment = payments[position]
            applyFilters()
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadSales() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("sales")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                salesList.clear()
                value?.documents?.forEach { doc ->
                    doc.toObject(Sale::class.java)?.let { salesList.add(it.copy(id = doc.id)) }
                }
                applyFilters()
            }
    }

    private fun applyFilters() {
        filteredSalesList.clear()
        filteredSalesList.addAll(salesList.filter { sale ->
            val matchSearch = sale.id.contains(searchQuery, true) || sale.customerName.contains(searchQuery, true)
            val matchPayment = selectedPayment == "All" || sale.paymentMethod == selectedPayment
            matchSearch && matchPayment
        })
        salesAdapter.updateSales(filteredSalesList)
        
        binding.txtEmpty.visibility = if (filteredSalesList.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerSales.visibility = if (filteredSalesList.isEmpty()) View.GONE else View.VISIBLE
    }
}
