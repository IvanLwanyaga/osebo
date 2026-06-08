package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.osebo.ai.databinding.ActivityMoreBinding

class MoreActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardAccount.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.cardSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.cardContact.setOnClickListener {
            startActivity(Intent(this, ContactUsActivity::class.java))
        }

        binding.cardBusiness.setOnClickListener {
            startActivity(Intent(this, HRActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}