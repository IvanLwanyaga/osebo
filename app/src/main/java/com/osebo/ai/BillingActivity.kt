package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.adapters.BillingAdapter
import com.osebo.ai.databinding.ActivityBillingBinding
import com.osebo.ai.models.Shop

class BillingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBillingBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val shopList = mutableListOf<Shop>()
    private lateinit var adapter: BillingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadShops()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = BillingAdapter(shopList) {
            // On Renew click
            startActivity(Intent(this, PackageSelectionActivity::class.java))
        }
        binding.recyclerBilling.layoutManager = LinearLayoutManager(this)
        binding.recyclerBilling.adapter = adapter
    }

    private fun loadShops() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("shops")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { query ->
                shopList.clear()
                query.documents.forEach { doc ->
                    doc.toObject(Shop::class.java)?.let { shopList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
    }
}