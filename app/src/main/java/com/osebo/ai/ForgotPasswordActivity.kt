package com.osebo.ai

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.osebo.ai.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnReset.setOnClickListener {
            handleResetPassword()
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun handleResetPassword() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter a valid email"
            return
        }

        setLoading(true)

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    showSuccessState(email)
                } else {
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun showSuccessState(email: String) {
        binding.btnReset.visibility = View.GONE
        binding.etEmail.isEnabled = false
        binding.layoutSuccess.visibility = View.VISIBLE
        binding.txtSuccessDesc.text = "We've sent a password reset link to:\n$email\n\nPlease check your inbox and spam folder."
    }

    private fun setLoading(loading: Boolean) {
        binding.btnReset.isEnabled = !loading
        binding.btnReset.text = if (loading) "Sending..." else "Send Reset Link"
    }
}
