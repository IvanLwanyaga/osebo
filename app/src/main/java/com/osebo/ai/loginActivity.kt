package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login2)   // ← Keep your layout name

        // Apply window insets (your original code)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Views
        val emailLayout = findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.passwordLayout)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)

        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // Real-time error clearing
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { emailLayout.error = null }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { passwordLayout.error = null }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Login Button
        btnLogin.setOnClickListener {
            if (validateLogin()) {
                btnLogin.isEnabled = false
                btnLogin.text = "Signing In..."

                // Simulate API call delay
                Handler(Looper.getMainLooper()).postDelayed({
                    Toast.makeText(this, "Login Successful! 🎉", Toast.LENGTH_SHORT).show()

                    // TODO: Navigate to your Main/Home Activity
                    // startActivity(Intent(this, MainActivity::class.java))
                    // finishAffinity()

                    btnLogin.isEnabled = true
                    btnLogin.text = "Sign In"
                }, 1800)
            }
        }

        // Forgot Password
        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password feature coming soon", Toast.LENGTH_SHORT).show()
            // Start ForgotPasswordActivity here later
        }

        // Go to Register
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun validateLogin(): Boolean {
        var isValid = true

        val emailInput = findViewById<TextInputEditText>(R.id.etEmail).text.toString().trim()
        val passwordInput = findViewById<TextInputEditText>(R.id.etPassword).text.toString().trim()

        val emailLayout = findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.passwordLayout)

        if (emailInput.isEmpty()) {
            emailLayout.error = "Email or Phone Number is required"
            isValid = false
        } else if (!isValidEmailOrPhone(emailInput)) {
            emailLayout.error = "Please enter a valid email or phone number"
            isValid = false
        } else {
            emailLayout.error = null
        }

        if (passwordInput.isEmpty()) {
            passwordLayout.error = "Password is required"
            isValid = false
        } else if (passwordInput.length < 6) {
            passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordLayout.error = null
        }

        return isValid
    }

    private fun isValidEmailOrPhone(input: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches() ||
                input.matches(Regex("^\\+?[0-9]{9,15}$"))
    }
}