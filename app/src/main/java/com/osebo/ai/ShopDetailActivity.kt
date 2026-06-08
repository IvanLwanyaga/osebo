package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.adapters.StaffAdapter
import com.osebo.ai.databinding.ActivityShopDetailBinding
import com.osebo.ai.models.Shop
import com.osebo.ai.models.User

class ShopDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private val staffList = mutableListOf<User>()
    private lateinit var staffAdapter: StaffAdapter
    private var shopId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        shopId = intent.getStringExtra("SHOP_ID")
        
        setupUI()
        loadShopDetails()
        loadStaff()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        staffAdapter = StaffAdapter(staffList)
        binding.recyclerStaff.layoutManager = LinearLayoutManager(this)
        binding.recyclerStaff.adapter = staffAdapter

        binding.btnViewShopOps.setOnClickListener {
            val intent = Intent(this, ShopOperationalActivity::class.java)
            intent.putExtra("SHOP_ID", shopId)
            startActivity(intent)
        }

        binding.btnEditShop.setOnClickListener {
            val intent = Intent(this, CreateShopActivity::class.java)
            intent.putExtra("SHOP_ID", shopId)
            intent.putExtra("IS_EDIT", true)
            startActivity(intent)
        }
    }

    private fun loadShopDetails() {
        shopId?.let { id ->
            db.collection("shops").document(id).get()
                .addOnSuccessListener { doc ->
                    val shop = doc.toObject(Shop::class.java)
                    if (shop != null) {
                        binding.txtDetailShopName.text = shop.name
                        binding.txtDetailLocation.text = shop.address
                        binding.txtDetailCategory.text = shop.category
                    }
                }
        }
    }

    private fun loadStaff() {
        shopId?.let { id ->
            db.collection("users")
                .whereEqualTo("shopId", id)
                .get()
                .addOnSuccessListener { query ->
                    staffList.clear()
                    query.documents.forEach { doc ->
                        doc.toObject(User::class.java)?.let { staffList.add(it) }
                    }
                    staffAdapter.notifyDataSetChanged()
                }
        }
    }
}
