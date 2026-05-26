package com.osebo.ai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.osebo.ai.databinding.ActivityContactUsBinding

class ContactUsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {

        val phone = "+256758123456"
        val email = "support@oseboai.com"

        // CALL BUTTON
        binding.btnCall.setOnClickListener {

            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phone")

            startActivity(intent)
        }

        // EMAIL BUTTON
        binding.btnEmail.setOnClickListener {

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:$email")

            startActivity(intent)
        }

        // SEND MESSAGE BUTTON
        binding.btnSendMessage.setOnClickListener {

            val name = binding.etName.text.toString().trim()
            val userEmail = binding.etEmail.text.toString().trim()
            val subject = binding.etSubject.text.toString().trim()
            val message = binding.etMessage.text.toString().trim()

            if (name.isEmpty() ||
                userEmail.isEmpty() ||
                subject.isEmpty() ||
                message.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Message Sent Successfully!",
                    Toast.LENGTH_LONG
                ).show()

                // CLEAR FIELDS
                binding.etName.text?.clear()
                binding.etEmail.text?.clear()
                binding.etSubject.text?.clear()
                binding.etMessage.text?.clear()
            }
        }
    }
}