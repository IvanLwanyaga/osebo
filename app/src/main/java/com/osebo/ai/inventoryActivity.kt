package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private var inventoryListener: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityInventoryBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupUI()
            startInventoryListener()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupUI() {
        try {
            // Initialize adapter with the filtered list
            adapter = InventoryAdapter(filteredList) { product ->
                val intent = Intent(this, AddProductActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            }

            // Setup RecyclerView
            binding.recyclerInventory.apply {
                layoutManager = LinearLayoutManager(this@InventoryActivity)
                adapter = this@InventoryActivity.adapter
                setHasFixedSize(true)
            }

            // Setup Add Product button
            binding.btnAddProduct.setOnClickListener {
                startActivity(Intent(this, AddProductActivity::class.java))
            }

            // Setup search functionality
            binding.etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    filterInventory(s.toString())
                }
                override fun afterTextChanged(s: Editable?) {}
            })

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error setting up UI: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startInventoryListener() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please login to view inventory", Toast.LENGTH_LONG).show()
            return
        }

        try {
            // Remove any existing listener
            inventoryListener?.remove()

            // Start listening to Firestore
            inventoryListener = db.collection("inventory")
                .whereEqualTo("ownerId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (snapshot == null) {
                        Toast.makeText(this, "No inventory data found", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    productList.clear()
                    var totalValue = 0.0
                    var lowStockCount = 0
                    var outOfStockCount = 0

                    snapshot.documents.forEach { document ->
                        try {
                            val product = document.toObject(Product::class.java)
                            if (product != null && product.isActive) {
                                val productWithId = product.copy(id = document.id)
                                productList.add(productWithId)

                                // Calculate total stock value (using selling price)
                                totalValue += (product.price * product.stock)

                                // Count stock status
                                if (product.stock <= 0) {
                                    outOfStockCount++
                                } else if (product.stock <= product.lowStockAlert) {
                                    lowStockCount++
                                }
                            }
                        } catch (e: Exception) {
                            // Skip problematic documents
                            e.printStackTrace()
                        }
                    }

                    // Update UI on main thread
                    runOnUiThread {
                        try {
                            binding.tvTotalItems.text = productList.size.toString()
                            binding.tvLowStock.text = lowStockCount.toString()
                            binding.tvOutOfStock.text = outOfStockCount.toString()
                            binding.tvStockValue.text = "UGX ${String.format("%,.0f", totalValue)}"

                            // Apply current filter
                            val currentQuery = binding.etSearch.text.toString()
                            filterInventory(currentQuery)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to connect: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun filterInventory(query: String) {
        try {
            filteredList.clear()
            if (query.isEmpty()) {
                filteredList.addAll(productList)
            } else {
                val q = query.lowercase().trim()
                filteredList.addAll(productList.filter { product ->
                    product.name.lowercase().contains(q) ||
                            product.category.lowercase().contains(q) ||
                            product.sku.lowercase().contains(q) ||
                            product.supplier.lowercase().contains(q)
                })
            }
            adapter.updateData(filteredList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up listener to prevent memory leaks
        inventoryListener?.remove()
    }
}