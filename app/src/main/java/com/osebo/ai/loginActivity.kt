package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // =========================
        // Views (FIXED NAMES)
        // =========================

        val rbPhone = findViewById<RadioButton>(R.id.rbPhone)
        val rbEmail = findViewById<RadioButton>(R.id.rbEmail)

        val phoneLayout = findViewById<LinearLayout>(R.id.phoneLayout)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        val btnLogin = findViewById<MaterialButton>(R.id.button)

        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // =========================
        // Default state
        // =========================

        rbPhone.isChecked = true
        phoneLayout.visibility = View.VISIBLE
        etEmail.visibility = View.GONE

        // =========================
        // Switch Login Method
        // =========================

        rbPhone.setOnClickListener {
            phoneLayout.visibility = View.VISIBLE
            etEmail.visibility = View.GONE
        }

        rbEmail.setOnClickListener {
            phoneLayout.visibility = View.GONE
            etEmail.visibility = View.VISIBLE
        }

        // =========================
        // Clear errors
        // =========================

        fun clearError(view: EditText) {
            view.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    view.error = null
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        clearError(etEmail)
        clearError(etPhone)
        clearError(etPassword)

        // =========================
        // Login Button
        // =========================

        btnLogin.setOnClickListener {

            if (validateLogin(rbPhone, etEmail, etPhone, etPassword)) {

                btnLogin.isEnabled = false
                btnLogin.text = "Signing In..."

                Handler(Looper.getMainLooper()).postDelayed({

                    Toast.makeText(this, "Login Successful! 🎉", Toast.LENGTH_SHORT).show()

                    btnLogin.isEnabled = true
                    btnLogin.text = "Sign In"

                }, 1500)
            }
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password feature coming soon", Toast.LENGTH_SHORT).show()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    // =========================
    // VALIDATION (FIXED)
    // =========================

    private fun validateLogin(
        rbPhone: RadioButton,
        etEmail: EditText,
        etPhone: EditText,
        etPassword: EditText
    ): Boolean {

        var valid = true

        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (rbPhone.isChecked) {

            if (phone.isEmpty()) {
                etPhone.error = "Phone number is required"
                valid = false

            } else if (!phone.matches(Regex("^\\+?[0-9]{9,15}$"))) {
                etPhone.error = "Enter a valid phone number"
                valid = false
            }

        } else {

            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                valid = false

            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Enter a valid email"
                valid = false
            }
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            valid = false

        } else if (password.length < 6) {
            etPassword.error = "Minimum 6 characters required"
            valid = false
        }

        return valid
    }
}