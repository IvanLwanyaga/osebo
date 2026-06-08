package com.osebo.ai

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.osebo.ai.databinding.ActivityNotificationsBinding

class NotificationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        
        // Show empty state for now
        binding.txtEmpty.visibility = View.VISIBLE
    }
}