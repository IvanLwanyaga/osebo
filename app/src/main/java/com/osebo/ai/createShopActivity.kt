package com.osebo.ai

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.databinding.ActivityCreateShopBinding
import com.osebo.ai.models.Shop
import java.util.UUID

class CreateShopActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateShopBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()

        binding.btnCreateShop.setOnClickListener {
            createShop()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupSpinner() {
        val categories = arrayOf(
            "Select Category", "Fashion", "Electronics", "Groceries", 
            "Beauty", "Restaurant", "Furniture", "Pharmacy", "Books", "Other"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerBusinessType.adapter = adapter
    }

    private fun createShop() {
        val userId = auth.currentUser?.uid ?: return
        val shopName = binding.etShopName.text.toString().trim()
        val category = binding.spinnerBusinessType.selectedItem.toString()
        val phone = binding.etPhone.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (shopName.isEmpty()) {
            binding.etShopName.error = "Required"
            return
        }
        if (category == "Select Category") {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }
        if (location.isEmpty()) {
            binding.etLocation.error = "Required"
            return
        }
        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, "Please accept terms", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnCreateShop.isEnabled = false
        binding.btnCreateShop.text = "Creating..."

        val shopId = UUID.randomUUID().toString()
        val shop = Shop(
            id = shopId,
            name = shopName,
            category = category,
            address = location,
            phoneNumber = binding.ccp.selectedCountryCodeWithPlus + phone,
            description = description,
            ownerId = userId,
            createdAt = System.currentTimeMillis()
        )

        db.collection("shops").document(shopId)
            .set(shop)
            .addOnSuccessListener {
                Toast.makeText(this, "Shop created successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnCreateShop.isEnabled = true
                binding.btnCreateShop.text = "Create Shop"
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}