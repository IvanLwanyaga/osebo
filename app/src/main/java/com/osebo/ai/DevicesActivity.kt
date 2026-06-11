package com.osebo.ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.osebo.ai.databinding.ActivityDevicesBinding

class DevicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDevicesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // Logic for listing devices would go here
    }
}
