package com.osebo.ai

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.databinding.ActivitySaleReceiptBinding
import com.osebo.ai.models.Sale
import java.text.SimpleDateFormat
import java.util.*

class SaleReceiptActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaleReceiptBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaleReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val saleId = intent.getStringExtra("SALE_ID")
        if (saleId != null) {
            loadSale(saleId)
        } else {
            finish()
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadSale(id: String) {
        db.collection("sales").document(id).get()
            .addOnSuccessListener { doc ->
                val sale = doc.toObject(Sale::class.java)
                if (sale != null) {
                    displayReceipt(sale)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load receipt", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayReceipt(sale: Sale) {
        binding.apply {
            txtShopName.text = sale.shopName
            txtDate.text = "Date: " + SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(sale.createdAt))
            txtSoldTo.text = "Sold to: " + sale.customerName
            txtPaymentMethod.text = "Payment: " + sale.paymentMethod
            txtTotalAmount.text = "UGX ${String.format("%,.0f", sale.totalAmount)}"
            txtServedBy.text = "Served by: " + sale.cashierName
        }
    }
}
