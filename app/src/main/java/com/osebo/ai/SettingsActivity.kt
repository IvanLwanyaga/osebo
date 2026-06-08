package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadUserData()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnUpdate.setOnClickListener {
            updateAccount()
        }
        
        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.cardChangePassword.setOnClickListener {
            changePassword()
        }

        binding.fabEditPhoto.setOnClickListener {
            Toast.makeText(this, "Profile photo update coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("displayName") ?: "Osebo User"
                    binding.txtName.text = name
                    binding.etDisplayName.setText(name)
                    binding.etEmail.setText(doc.getString("email") ?: user.email)
                    binding.etPhone.setText(doc.getString("phoneNumber") ?: "")
                    
                    val status = doc.getString("status") ?: "Active"
                    binding.txtAccountStatus.text = "$status Account"
                }
            }
    }

    private fun updateAccount() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        val newName = binding.etDisplayName.text.toString().trim()
        val newPhone = binding.etPhone.text.toString().trim()
        val newEmail = binding.etEmail.text.toString().trim()

        if (newName.isEmpty()) {
            binding.etDisplayName.error = "Name is required"
            return
        }

        binding.btnUpdate.isEnabled = false
        binding.btnUpdate.text = "Saving..."

        // Update Auth Email if changed
        if (newEmail != user.email && newEmail.isNotEmpty()) {
            user.updateEmail(newEmail).addOnFailureListener {
                Toast.makeText(this, "Email update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        val updates = mapOf(
            "displayName" to newName,
            "phoneNumber" to newPhone,
            "email" to newEmail
        )

        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                binding.btnUpdate.isEnabled = true
                binding.btnUpdate.text = "Save Changes"
                binding.txtName.text = newName
                Toast.makeText(this, "Account updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                binding.btnUpdate.isEnabled = true
                binding.btnUpdate.text = "Save Changes"
                Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun changePassword() {
        val email = auth.currentUser?.email
        if (email != null) {
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}