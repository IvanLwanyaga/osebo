package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.osebo.ai.databinding.ActivityPackageSelectionBinding

class PackageSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPackageSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnSelectBasic.setOnClickListener {
            val intent = Intent(this, ActivationActivity::class.java)
            intent.putExtra("PACKAGE_NAME", "Basic")
            intent.putExtra("PACKAGE_PRICE", 20000.0)
            startActivity(intent)
        }

        binding.btnSelectPro.setOnClickListener {
            val intent = Intent(this, ActivationActivity::class.java)
            intent.putExtra("PACKAGE_NAME", "Pro")
            intent.putExtra("PACKAGE_PRICE", 50000.0)
            startActivity(intent)
        }
    }
}