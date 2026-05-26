package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.hbb20.CountryCodePicker

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Views
        val spinnerTitle = findViewById<Spinner>(R.id.spinnerTitle)
        val etFirstName = findViewById<EditText>(R.id.editTextText)
        val etLastName = findViewById<EditText>(R.id.editTextText2)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.editTextPhone)
        val etPassword = findViewById<EditText>(R.id.editTextTextPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.editTextTextPassword2)
        val ccp = findViewById<CountryCodePicker>(R.id.ccp)
        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val btnSendOtp = findViewById<MaterialButton>(R.id.button)
        val tvLogin = findViewById<TextView>(R.id.editTextText5)

        // Spinner setup
        val titles = arrayOf("Mr", "Mrs", "Miss", "Dr", "Prof", "Rev", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, titles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTitle.adapter = adapter
        spinnerTitle.setSelection(0)

        // =========================
        // REGISTER BUTTON
        // =========================

        btnSendOtp.setOnClickListener {

            val selectedTitle = spinnerTitle.selectedItem.toString()
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val fullPhoneNumber = ccp.selectedCountryCodeWithPlus + phone

            // VALIDATION
            if (firstName.isEmpty()) {
                etFirstName.error = "First name is required"
                return@setOnClickListener
            }

            if (lastName.isEmpty()) {
                etLastName.error = "Last name is required"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Enter a valid email address"
                return@setOnClickListener
            }

            if (phone.isEmpty()) {
                etPhone.error = "Phone number is required"
                return@setOnClickListener
            }

            if (phone.length < 9) {
                etPhone.error = "Enter a valid phone number"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password is required"
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            if (!cbTerms.isChecked) {
                Toast.makeText(this, "Please accept Terms and Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // SUCCESS
            Toast.makeText(this, "OTP sent to $fullPhoneNumber", Toast.LENGTH_SHORT).show()
            Toast.makeText(this, "Welcome $selectedTitle $firstName", Toast.LENGTH_SHORT).show()

            // =========================
            // NAVIGATE TO LOGIN (FIXED)
            // =========================

            Handler(Looper.getMainLooper()).postDelayed({

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // prevents back navigation to register

            }, 1000)
        }

        // =========================
        // GO TO LOGIN
        // =========================

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}