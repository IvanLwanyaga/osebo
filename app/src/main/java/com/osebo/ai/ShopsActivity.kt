package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.osebo.ai.adapters.ShopAdapter
import com.osebo.ai.databinding.ActivityShopsBinding
import com.osebo.ai.models.Shop

class ShopsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopsBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val shopList = mutableListOf<Shop>()
    private lateinit var shopAdapter: ShopAdapter
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        startShopsListener()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        shopAdapter = ShopAdapter(shopList)
        binding.recyclerShops.layoutManager = LinearLayoutManager(this)
        binding.recyclerShops.adapter = shopAdapter

        binding.btnAddShop.setOnClickListener {
            startActivity(Intent(this, CreateShopActivity::class.java))
        }
    }

    private fun startShopsListener() {
        val userId = auth.currentUser?.uid ?: return

        listener = db.collection("shops")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                shopList.clear()
                value?.documents?.forEach { doc ->
                    val shop = doc.toObject(Shop::class.java)
                    if (shop != null) {
                        shopList.add(shop)
                    }
                }
                
                shopAdapter.notifyDataSetChanged()
                
                binding.txtShopCount.text = "${shopList.size} branches registered"
                binding.txtEmpty.visibility = if (shopList.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}
