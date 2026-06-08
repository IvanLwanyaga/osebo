package com.osebo.ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.osebo.ai.databinding.FragmentEmployeeBinding

class EmployeeActivity : AppCompatActivity() {

    private lateinit var binding: FragmentEmployeeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentEmployeeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabAddEmployee.setOnClickListener {
            // Show Add Employee Dialog
        }
    }
}
