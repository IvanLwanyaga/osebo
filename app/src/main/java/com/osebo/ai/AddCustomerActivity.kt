package com.osebo.ai

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.databinding.ActivityAddCustomerBinding
import com.osebo.ai.models.Customer

class AddCustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCustomerBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveCustomer() }
    }

    private fun saveCustomer() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val customer = Customer(
            name = name,
            phone = phone,
            email = email,
            address = address
        )

        val customerMap = hashMapOf(
            "name" to customer.name,
            "phone" to customer.phone,
            "email" to customer.email,
            "address" to customer.address,
            "ownerId" to userId,
            "createdAt" to customer.createdAt
        )

        binding.btnSave.isEnabled = false
        db.collection("customers").add(customerMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Customer added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error adding customer", Toast.LENGTH_SHORT).show()
                binding.btnSave.isEnabled = true
            }
    }
}
