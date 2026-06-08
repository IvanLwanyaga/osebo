package com.osebo.ai

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.databinding.ActivityCreateUserBinding
import com.osebo.ai.models.User

class CreateUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateUserBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        val roles = listOf("Staff", "Admin", "Manager")
        binding.autoRole.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles))

        binding.btnCreateUser.setOnClickListener {
            createUser()
        }
    }

    private fun createUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val role = binding.autoRole.text.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Note: Creating a separate Firebase User for staff requires using Firebase Admin SDK 
        // or a Cloud Function to avoid logging out the current admin.
        // For this MVP, we will save the staff profile to Firestore.
        
        val userId = db.collection("users").document().id
        val newUser = User(
            id = userId,
            displayName = name,
            email = email,
            role = role,
            ownerId = auth.currentUser?.uid ?: ""
        )

        db.collection("users").document(userId).set(newUser)
            .addOnSuccessListener {
                Toast.makeText(this, "User $name created successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
