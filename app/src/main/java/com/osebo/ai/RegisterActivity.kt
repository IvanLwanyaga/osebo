package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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

        // =========================
        // Window Insets
        // =========================

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // =========================
        // Initialize Views
        // =========================

        val spinnerTitle =
            findViewById<Spinner>(R.id.spinnerTitle)

        val etFirstName =
            findViewById<EditText>(R.id.editTextText)

        val etLastName =
            findViewById<EditText>(R.id.editTextText2)

        val etEmail =
            findViewById<EditText>(R.id.etEmail)

        val etPhone =
            findViewById<EditText>(R.id.editTextPhone)

        val etPassword =
            findViewById<EditText>(R.id.editTextTextPassword)

        val etConfirmPassword =
            findViewById<EditText>(R.id.editTextTextPassword2)

        val ccp =
            findViewById<CountryCodePicker>(R.id.ccp)

        val cbTerms =
            findViewById<CheckBox>(R.id.cbTerms)

        val btnSendOtp =
            findViewById<MaterialButton>(R.id.button)

        val tvLogin =
            findViewById<TextView>(R.id.editTextText5)

        // =========================
        // Spinner Setup
        // =========================

        val titles = arrayOf(
            "Mr",
            "Mrs",
            "Miss",
            "Dr",
            "Prof",
            "Rev",
            "Other"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            titles
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerTitle.adapter = adapter

        // Default Title = Mr
        spinnerTitle.setSelection(0)

        // =========================
        // Send OTP Button
        // =========================

        btnSendOtp.setOnClickListener {

            val selectedTitle =
                spinnerTitle.selectedItem.toString()

            val firstName =
                etFirstName.text.toString().trim()

            val lastName =
                etLastName.text.toString().trim()

            val email =
                etEmail.text.toString().trim()

            val phone =
                etPhone.text.toString().trim()

            val password =
                etPassword.text.toString().trim()

            val confirmPassword =
                etConfirmPassword.text.toString().trim()

            val fullPhoneNumber =
                ccp.selectedCountryCodeWithPlus + phone

            // =========================
            // Validation
            // =========================

            if (firstName.isEmpty()) {

                etFirstName.error =
                    "First name is required"

                etFirstName.requestFocus()
                return@setOnClickListener
            }

            if (lastName.isEmpty()) {

                etLastName.error =
                    "Last name is required"

                etLastName.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {

                etEmail.error =
                    "Email is required"

                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()
            ) {

                etEmail.error =
                    "Enter a valid email address"

                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (phone.isEmpty()) {

                etPhone.error =
                    "Phone number is required"

                etPhone.requestFocus()
                return@setOnClickListener
            }

            if (phone.length < 9) {

                etPhone.error =
                    "Enter a valid phone number"

                etPhone.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {

                etPassword.error =
                    "Password is required"

                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {

                etPassword.error =
                    "Password must be at least 6 characters"

                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {

                etConfirmPassword.error =
                    "Please confirm your password"

                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmPassword) {

                etConfirmPassword.error =
                    "Passwords do not match"

                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            // =========================
            // Terms & Conditions
            // =========================

            if (!cbTerms.isChecked) {

                Toast.makeText(
                    this,
                    "Please accept Terms and Conditions",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // =========================
            // Success Message
            // =========================

            Toast.makeText(
                this,
                "OTP sent to $fullPhoneNumber",
                Toast.LENGTH_LONG
            ).show()

            // =========================
            // Example Welcome Message
            // =========================

            Toast.makeText(
                this,
                "Welcome $selectedTitle $firstName",
                Toast.LENGTH_SHORT
            ).show()

            // =========================
            // TODO:
            // Navigate to OTP Screen
            // =========================

            /*
            val intent =
                Intent(this, VerifyOtpActivity::class.java)

            intent.putExtra("phone", fullPhoneNumber)

            startActivity(intent)
            */

        }

        // =========================
        // Go To Login Screen
        // =========================

        tvLogin.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finish()
        }
    }
}