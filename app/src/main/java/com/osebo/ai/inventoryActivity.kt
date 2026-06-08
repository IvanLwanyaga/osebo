package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.adapters.InventoryAdapter
import com.osebo.ai.databinding.ActivityInventoryBinding
import com.osebo.ai.models.Product

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val productList = mutableListOf<Product>()
    private val filteredList = mutableListOf<Product>()
    private lateinit var adapter: InventoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        startInventoryListener()
    }

    private fun setupUI() {
        adapter = InventoryAdapter(filteredList) { product ->
            val intent = Intent(this, AddProductActivity::class.java)
            intent.putExtra("PRODUCT_ID", product.id)
            startActivity(intent)
        }

        binding.recyclerInventory.apply {
            layoutManager = LinearLayoutManager(this@InventoryActivity)
            this.adapter = this@InventoryActivity.adapter
        }

        binding.btnAddProduct.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterInventory(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun startInventoryListener() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("inventory")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Error listening for inventory", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                productList.clear()
                var totalValue = 0.0
                var lowStockCount = 0

                value?.documents?.forEach { doc ->
                    doc.toObject(Product::class.java)?.let { p ->
                        val product = p.copy(id = doc.id)
                        productList.add(product)
                        
                        totalValue += (product.price * product.stock)
                        if (product.stock <= product.lowStockAlert) lowStockCount++
                    }
                }

                binding.tvTotalItems.text = productList.size.toString()
                binding.tvLowStock.text = lowStockCount.toString()
                binding.tvStockValue.text = "UGX ${String.format("%,.0f", totalValue)}"

                filterInventory(binding.etSearch.text.toString())
            }
    }

    private fun filterInventory(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(productList)
        } else {
            val q = query.lowercase()
            filteredList.addAll(productList.filter { 
                it.name.lowercase().contains(q) || it.category.lowercase().contains(q) 
            })
        }
        adapter.notifyDataSetChanged()
    }
}
