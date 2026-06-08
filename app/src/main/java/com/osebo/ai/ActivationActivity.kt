package com.osebo.ai

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.databinding.ActivityActivationBinding
import java.util.*

class ActivationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActivationBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private var packageName: String? = null
    private var unitPrice: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActivationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        packageName = intent.getStringExtra("PACKAGE_NAME")
        unitPrice = intent.getDoubleExtra("PACKAGE_PRICE", 0.0)

        setupUI()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.txtPackageName.text = "$packageName Package"
        binding.txtPackagePrice.text = "UGX ${formatAmount(unitPrice)} / month"
        
        updateTotal()

        binding.etMonths.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateTotal()
            }
        })

        binding.btnActivate.setOnClickListener {
            activateSubscription()
        }
    }

    private fun updateTotal() {
        val months = binding.etMonths.text.toString().toIntOrNull() ?: 1
        val total = unitPrice * months
        binding.txtTotalPayable.text = "Total: UGX ${formatAmount(total)}"
    }

    private fun activateSubscription() {
        val uid = auth.currentUser?.uid ?: return
        val phone = binding.etPhone.text.toString().trim()
        val months = binding.etMonths.text.toString().toIntOrNull() ?: 1

        if (phone.isEmpty()) {
            binding.etPhone.error = "Phone number required"
            return
        }

        binding.btnActivate.isEnabled = false
        binding.btnActivate.text = "Activating..."

        // Mock activation logic: Update user's subscription in Firestore
        // In a real app, this would trigger a Mobile Money prompt (MTN/Airtel API)
        
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 14) // Free trial
        calendar.add(Calendar.MONTH, months) // Plus paid months
        
        val expiryDate = calendar.timeInMillis

        val subData = mapOf(
            "subscriptionStatus" to "ACTIVATED",
            "subscriptionPackage" to packageName,
            "subscriptionExpiry" to expiryDate,
            "subscriptionMonths" to months,
            "paymentPhone" to phone
        )

        db.collection("users").document(uid).update(subData)
            .addOnSuccessListener {
                Toast.makeText(this, "Subscription Activated Successfully!", Toast.LENGTH_LONG).show()
                // Redirect to dashboard or billing
                finish()
            }
            .addOnFailureListener {
                binding.btnActivate.isEnabled = true
                binding.btnActivate.text = "Activate Trial & Subscribe"
                Toast.makeText(this, "Activation failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatAmount(amount: Double): String {
        return String.format("%,.0f", amount)
    }
}