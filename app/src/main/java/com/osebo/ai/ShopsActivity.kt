package com.osebo.ai.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.osebo.ai.databinding.ActivityShopsBinding
import com.osebo.ai.models.Shop
import com.osebo.ai.repository.ShopRepository
import java.util.UUID

class ShopsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopsBinding

    private val repository = ShopRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShopsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnAddShop.setOnClickListener {

            val shopName = binding.edtShopName.text.toString()
            val location = binding.edtLocation.text.toString()

            val shop = Shop(
                id = UUID.randomUUID().toString(),
                shopName = shopName,
                ownerName = "James",
                location = location
            )

            repository.addShop(shop) {

                if (it) {
                    Toast.makeText(this, "Shop Added", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}