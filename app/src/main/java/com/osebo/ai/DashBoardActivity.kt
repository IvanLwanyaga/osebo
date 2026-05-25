package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val btnAddShop = findViewById<MaterialButton>(R.id.btnAddShop)
        val search = findViewById<EditText>(R.id.etSearch)

        btnAddShop.setOnClickListener {
            startActivity(
                Intent(this, CreateShopActivity::class.java)
            )
        }

        search.setOnClickListener {
            Toast.makeText(this, "Search activated", Toast.LENGTH_SHORT).show()
        }
    }
}