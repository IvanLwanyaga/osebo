package com.osebo.ai

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.databinding.ActivityAddProductBinding
import com.osebo.ai.models.Product
import com.osebo.ai.models.Shop
import java.util.*

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private var shopList = mutableListOf<Shop>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchShops()

        binding.btnSaveProduct.setOnClickListener {
            saveProduct()
        }
    }

    private fun fetchShops() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("shops")
            .whereEqualTo("ownerId", userId)
            .get()
            .addOnSuccessListener { documents ->
                shopList.clear()
                val shopNames = mutableListOf<String>()
                for (document in documents) {
                    val shop = document.toObject(Shop::class.java)
                    shopList.add(shop)
                    shopNames.add(shop.name)
                }
                
                if (shopNames.isEmpty()) {
                    Toast.makeText(this, "Please create a shop first", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val shopAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, shopNames)
                    binding.spinnerShop.adapter = shopAdapter
                }
            }
    }

    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val qty = binding.etQuantity.text.toString().toIntOrNull() ?: 0
        val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val minStock = binding.etMinStock.text.toString().toIntOrNull() ?: 5
        
        val selectedShopIndex = binding.spinnerShop.selectedItemPosition
        if (selectedShopIndex < 0) return
        val selectedShop = shopList[selectedShopIndex]

        if (name.isEmpty() || qty <= 0 || price <= 0) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSaveProduct.isEnabled = false
        binding.btnSaveProduct.text = "Adding..."

        val productId = UUID.randomUUID().toString()
        val product = Product(
            id = productId,
            name = name,
            quantity = qty,
            unitPrice = price,
            minStock = minStock,
            shopId = selectedShop.id,
            ownerId = auth.currentUser?.uid ?: ""
        )

        db.collection("inventory").document(productId)
            .set(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Product added to inventory", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                binding.btnSaveProduct.isEnabled = true
                binding.btnSaveProduct.text = "Add Product"
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}