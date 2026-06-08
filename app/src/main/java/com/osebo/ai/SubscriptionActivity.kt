package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.osebo.ai.databinding.ActivitySubscriptionBinding

class SubscriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubscriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupPlanButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupPlanButtons() {
        // Basic Plan → Wizard
        binding.btnBasicPlan.setOnClickListener {
            navigateToSubscriptionWizard("BASIC")
        }

        // Pro Plan → Wizard
        binding.btnProPlan.setOnClickListener {
            navigateToSubscriptionWizard("PRO")
        }

        // Business Plan → Wizard
        binding.btnBusinessPlan.setOnClickListener {
            navigateToSubscriptionWizard("BUSINESS")
        }
    }

    private fun navigateToSubscriptionWizard(planType: String) {
        val intent = Intent(this, PackageSelectionActivity::class.java).apply {
            putExtra("SELECTED_PLAN", planType)
        }
        startActivity(intent)
    }
}