package com.example.osebo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hbb20.CountryCodePicker

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        val etSurname = findViewById<EditText>(R.id.editTextText)
        val etLastname = findViewById<EditText>(R.id.editTextText2)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val ccp = findViewById<CountryCodePicker>(R.id.ccp)
        val etPhone = findViewById<EditText>(R.id.editTextPhone)
        val etPassword = findViewById<EditText>(R.id.editTextTextPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.editTextTextPassword2)

        val btnSignUp = findViewById<Button>(R.id.button)

        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnSignUp.setOnClickListener {

            if (validateForm(
                    etSurname,
                    etLastname,
                    etEmail,
                    etPhone,
                    etPassword,
                    etConfirmPassword
                )
            ) {

                val fullPhoneNumber =
                    ccp.selectedCountryCodeWithPlus +
                            etPhone.text.toString().trim()

                Toast.makeText(
                    this,
                    "Account Created Successfully!",
                    Toast.LENGTH_LONG
                ).show()

                Toast.makeText(
                    this,
                    fullPhoneNumber,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        tvLogin.setOnClickListener {

            startActivity(
                Intent(this, LoginActivity::class.java)
            )

            finish()
        }
    }

    private fun validateForm(
        surname: EditText,
        lastname: EditText,
        email: EditText,
        phone: EditText,
        password: EditText,
        confirmPassword: EditText
    ): Boolean {

        if (surname.text.toString().trim().isEmpty()) {
            surname.error = "Surname is required"
            return false
        }

        if (lastname.text.toString().trim().isEmpty()) {
            lastname.error = "Last name is required"
            return false
        }

        if (email.text.toString().trim().isEmpty()) {
            email.error = "Email is required"
            return false
        }

        if (phone.text.toString().trim().isEmpty()) {
            phone.error = "Phone number is required"
            return false
        }

        if (password.text.toString().length < 6) {
            password.error = "Password must be at least 6 characters"
            return false
        }

        if (password.text.toString() != confirmPassword.text.toString()) {
            confirmPassword.error = "Passwords do not match"
            return false
        }

        return true
    }
}