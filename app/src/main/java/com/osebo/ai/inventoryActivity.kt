package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.osebo.ai.databinding.ActivityInventoryBinding
import com.osebo.ai.models.Product

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val productList = mutableListOf<Product>()
    private lateinit var productAdapter: ProductAdapter
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        startRealtimeListener()
    }

    private fun setupUI() {
        productAdapter = ProductAdapter(productList)
        binding.recyclerInventory.layoutManager = LinearLayoutManager(this)
        binding.recyclerInventory.adapter = productAdapter

        binding.fabAddProduct.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }

        binding.etSearchInventory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter logic
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun startRealtimeListener() {
        val userId = auth.currentUser?.uid ?: return

        listener = db.collection("inventory")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                productList.clear()
                var totalItems = 0
                var lowStock = 0
                
                value?.documents?.forEach { doc ->
                    val product = doc.toObject(Product::class.java)
                    if (product != null) {
                        productList.add(product)
                        totalItems += product.quantity
                        if (product.quantity <= product.minStock) {
                            lowStock++
                        }
                    }
                }
                
                productAdapter.notifyDataSetChanged()
                binding.tvTotalItems.text = totalItems.toString()
                binding.tvLowStock.text = lowStock.toString()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}