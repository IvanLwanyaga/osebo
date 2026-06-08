package com.osebo.ai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.databinding.ActivityContactUsBinding
import com.osebo.ai.utils.ToastHelper

class ContactUsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactUsBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val phone = "+256751367938"
        val email = "support@oseboai.com"

        binding.btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phone")
            startActivity(intent)
        }

        binding.btnEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:$email")
            startActivity(intent)
        }

        binding.btnWhatsapp.setOnClickListener {
            val url = "https://api.whatsapp.com/send?phone=$phone"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        binding.btnSendMessage.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val name = binding.etName.text.toString().trim()
        val userEmail = binding.etEmail.text.toString().trim()
        val subject = binding.etSubject.text.toString().trim()
        val message = binding.etMessage.text.toString().trim()

        if (name.isEmpty() || userEmail.isEmpty() || subject.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSendMessage.isEnabled = false
        binding.btnSendMessage.text = "Sending..."

        val feedback = hashMapOf(
            "name" to name,
            "email" to userEmail,
            "subject" to subject,
            "message" to message,
            "userId" to (FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"),
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("feedback")
            .add(feedback)
            .addOnSuccessListener {
                ToastHelper.showCustomToast(this, "Message Sent Successfully!")
                binding.etName.text?.clear()
                binding.etEmail.text?.clear()
                binding.etSubject.text?.clear()
                binding.etMessage.text?.clear()
                binding.btnSendMessage.isEnabled = true
                binding.btnSendMessage.text = "Send Message"
            }
            .addOnFailureListener {
                binding.btnSendMessage.isEnabled = true
                binding.btnSendMessage.text = "Send Message"
                ToastHelper.showCustomToast(this, "Failed to send message!")
            }
    }
}