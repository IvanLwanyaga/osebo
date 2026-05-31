package com.osebo.ai

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.databinding.ActivityCreateSalesBinding
import com.osebo.ai.models.Sale
import com.osebo.ai.models.Shop
import java.util.UUID

class CreateSaleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateSalesBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private var shopList = mutableListOf<Shop>()
    private val paymentMethods = arrayOf("Cash", "Mobile Money", "Bank Transfer", "Credit")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateSalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        fetchShops()
        setupCalculationLogic()

        binding.btnSaveSale.setOnClickListener {
            saveSale()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupSpinners() {
        val paymentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, paymentMethods)
        binding.spinnerPayment.adapter = paymentAdapter
    }

    private fun fetchShops() {
        val userId = auth.currentUser?.uid ?: return
        
        // Fetch shops owned by this user
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
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching shops: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupCalculationLogic() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateTotal()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etQty.addTextChangedListener(watcher)
        binding.etPrice.addTextChangedListener(watcher)
    }

    private fun calculateTotal() {
        val qty = binding.etQty.text.toString().toDoubleOrNull() ?: 0.0
        val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val total = qty * price
        binding.txtTotal.text = "UGX %,.0f".format(total)
    }

    private fun saveSale() {
        val userId = auth.currentUser?.uid ?: return
        val productName = binding.etProduct.text.toString().trim()
        val qtyString = binding.etQty.text.toString().trim()
        val priceString = binding.etPrice.text.toString().trim()

        if (productName.isEmpty() || qtyString.isEmpty() || priceString.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val qty = qtyString.toDouble()
        val price = priceString.toDouble()
        val total = qty * price
        
        val selectedShopIndex = binding.spinnerShop.selectedItemPosition
        if (selectedShopIndex < 0 || selectedShopIndex >= shopList.size) return
        
        val selectedShop = shopList[selectedShopIndex]
        val paymentMethod = binding.spinnerPayment.selectedItem.toString()

        binding.btnSaveSale.isEnabled = false
        binding.btnSaveSale.text = "Saving..."

        val saleId = UUID.randomUUID().toString()
        val sale = Sale(
            id = saleId,
            shopId = selectedShop.id,
            cashierId = userId,
            ownerId = userId,
            totalAmount = total,
            paymentMethod = paymentMethod,
            createdAt = System.currentTimeMillis()
        )

        // In a real app, we'd also save the line items (Product, Qty, Unit Price)
        // For now, we save the summary to the "sales" collection
        db.collection("sales").document(saleId)
            .set(sale)
            .addOnSuccessListener {
                Toast.makeText(this, "Sale recorded successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnSaveSale.isEnabled = true
                binding.btnSaveSale.text = "Confirm Sale"
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}