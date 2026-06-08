package com.osebo.ai

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.adapters.POSProductAdapter
import com.osebo.ai.adapters.CartAdapter
import com.osebo.ai.databinding.ActivityPosBinding
import com.osebo.ai.models.Product
import com.osebo.ai.models.CartItem
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.util.*

class POSActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPosBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val productList = mutableListOf<Product>()
    private val filteredList = mutableListOf<Product>()
    private val cart = mutableMapOf<String, CartItem>()
    
    private lateinit var productAdapter: POSProductAdapter
    private lateinit var cartAdapter: CartAdapter

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            binding.etSearchProduct.setText(result.contents)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadProducts()
    }

    private fun setupUI() {
        productAdapter = POSProductAdapter(filteredList) { product ->
            addToCart(product)
        }
        binding.recyclerProducts.layoutManager = LinearLayoutManager(this)
        binding.recyclerProducts.adapter = productAdapter
        
        cartAdapter = CartAdapter(cart.values.toList()) {
            updateCartUI()
        }
        binding.recyclerCart.layoutManager = LinearLayoutManager(this)
        binding.recyclerCart.adapter = cartAdapter

        binding.etSearchProduct.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnScanBarcodeCard.setOnClickListener {
            barcodeLauncher.launch(ScanOptions().setPrompt("Scan product barcode"))
        }

        binding.btnScanBarcode.setOnClickListener {
            barcodeLauncher.launch(ScanOptions().setPrompt("Scan product barcode"))
        }

        binding.btnViewCart.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }

        binding.btnCheckout.setOnClickListener {
            completeSale()
        }
    }

    private fun loadProducts() {
        val shopId = intent.getStringExtra("SHOP_ID") ?: ""
        db.collection("inventory")
            .whereEqualTo("shopId", shopId)
            .get()
            .addOnSuccessListener { result ->
                productList.clear()
                for (doc in result) {
                    val p = doc.toObject(Product::class.java)
                    productList.add(p)
                }
                filterProducts("")
            }
    }

    private fun filterProducts(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(productList)
        } else {
            filteredList.addAll(productList.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.barcode == query || it.sku == query
            })
        }
        productAdapter.notifyDataSetChanged()
    }

    private fun addToCart(product: Product) {
        val existing = cart[product.id]
        if (existing != null) {
            existing.quantity += 1
        } else {
            cart[product.id] = CartItem(
                id = product.id,
                name = product.name,
                price = product.price,
                costPrice = product.costPrice,
                quantity = 1
            )
        }
        updateCartUI()
        Toast.makeText(this, "${product.name} added", Toast.LENGTH_SHORT).show()
    }

    private fun updateCartUI() {
        val cartItems = cart.values.toList()
        val total = cartItems.sumOf { it.price * it.quantity }
        val count = cartItems.sumOf { it.quantity }
        
        binding.tvItemCount.text = "$count Items"
        binding.tvTotalAmount.text = "UGX %,.0f".format(total)
        
        cartAdapter = CartAdapter(cartItems) { updateCartUI() }
        binding.recyclerCart.adapter = cartAdapter
    }

    private fun completeSale() {
        if (cart.isEmpty()) return
        
        val saleId = UUID.randomUUID().toString()
        val total = cart.values.sumOf { it.price * it.quantity }
        
        val saleData = hashMapOf(
            "id" to saleId,
            "shopId" to (intent.getStringExtra("SHOP_ID") ?: ""),
            "totalAmount" to total,
            "paymentMethod" to if (binding.rbCash.isChecked) "Cash" else "Mobile Money",
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("sales").document(saleId).set(saleData)
            .addOnSuccessListener {
                Toast.makeText(this, "Sale Completed!", Toast.LENGTH_LONG).show()
                cart.clear()
                updateCartUI()
                binding.drawerLayout.closeDrawer(GravityCompat.END)
                finish()
            }
    }
}
