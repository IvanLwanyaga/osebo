package com.osebo.erp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.osebo.erp.R

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val inventoryCard = findViewById<LinearLayout>(R.id.inventoryCard)
        val salesCard = findViewById<LinearLayout>(R.id.salesCard)
        val financeCard = findViewById<LinearLayout>(R.id.financeCard)
        val hrCard = findViewById<LinearLayout>(R.id.hrCard)

        inventoryCard.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        salesCard.setOnClickListener {
            startActivity(Intent(this, SalesActivity::class.java))
        }

        financeCard.setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
        }

        hrCard.setOnClickListener {
            startActivity(Intent(this, HRActivity::class.java))
        }

    }
}