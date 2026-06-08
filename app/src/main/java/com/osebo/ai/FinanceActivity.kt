package com.osebo.ai

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.osebo.ai.databinding.ActivityFinanceBinding

class FinanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFinanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBackFinance.setOnClickListener {
            finish()
        }

        binding.cardAddIncome.setOnClickListener {
            Toast.makeText(this, "Add Income feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.cardAddExpense.setOnClickListener {
            Toast.makeText(this, "Add Expense feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.cardStatements.setOnClickListener {
            Toast.makeText(this, "Statements feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.cardTax.setOnClickListener {
            Toast.makeText(this, "Tax Reports feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}