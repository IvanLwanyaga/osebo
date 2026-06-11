package com.osebo.ai

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.osebo.ai.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSavePassword.setOnClickListener {
            updatePassword()
        }
    }

    private fun updatePassword() {
        val currentPass = binding.etCurrentPassword.text.toString()
        val newPass = binding.etNewPassword.text.toString()
        val confirmPass = binding.etConfirmPassword.text.toString()

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass != confirmPass) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)

            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Re-authentication failed. Check your current password.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
