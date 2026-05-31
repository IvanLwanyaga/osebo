package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.osebo.ai.databinding.ActivitySalesBinding
import com.osebo.ai.models.Sale
import com.osebo.ai.models.Shop

class SalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalesBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val salesList = mutableListOf<Sale>()
    private lateinit var saleAdapter: SaleAdapter
    
    private var shopsListener: ListenerRegistration? = null
    private var salesListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        startRealtimeListeners()
    }

    private fun setupUI() {
        saleAdapter = SaleAdapter(salesList)
        binding.recyclerSales.layoutManager = LinearLayoutManager(this)
        binding.recyclerSales.adapter = saleAdapter

        binding.btnNewSale.setOnClickListener {
            startActivity(Intent(this, CreateSaleActivity::class.java))
        }
    }

    private fun startRealtimeListeners() {
        val userId = auth.currentUser?.uid ?: return

        // 1. Get user's shops first to filter sales
        shopsListener = db.collection("shops")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                val userShopIds = value?.documents?.mapNotNull { it.id } ?: emptyList()
                
                if (userShopIds.isNotEmpty()) {
                    listenForSales(userShopIds)
                } else {
                    binding.txtEmpty.visibility = View.VISIBLE
                    salesList.clear()
                    saleAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun listenForSales(shopIds: List<String>) {
        salesListener?.remove()
        
        salesListener = db.collection("sales")
            .whereIn("shopId", shopIds)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                salesList.clear()
                value?.documents?.forEach { doc ->
                    val sale = doc.toObject(Sale::class.java)
                    if (sale != null) salesList.add(sale)
                }
                
                saleAdapter.notifyDataSetChanged()
                binding.txtEmpty.visibility = if (salesList.isEmpty()) View.VISIBLE else View.GONE
                binding.txtSaleSummary.text = "${salesList.size} transactions total"
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        shopsListener?.remove()
        salesListener?.remove()
    }
}