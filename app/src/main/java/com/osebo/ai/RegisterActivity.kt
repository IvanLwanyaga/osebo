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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hbb20.CountryCodePicker

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firebase init
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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
        val btnRegister = findViewById<MaterialButton>(R.id.button)
        val tvLogin = findViewById<TextView>(R.id.editTextText5)

        // Spinner setup
        val titles = arrayOf("Mr", "Mrs", "Miss", "Dr", "Prof", "Rev", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, titles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTitle.adapter = adapter

        // =========================
        // REGISTER BUTTON
        // =========================
        btnRegister.setOnClickListener {

            val selectedTitle = spinnerTitle.selectedItem.toString()
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val fullPhone = ccp.selectedCountryCodeWithPlus + phone

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
                etEmail.error = "Enter valid email"
                return@setOnClickListener
            }

            if (phone.isEmpty()) {
                etPhone.error = "Phone is required"
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPassword.error = "Min 6 characters"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            if (!cbTerms.isChecked) {
                Toast.makeText(this, "Accept Terms & Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // =========================
            // FIREBASE REGISTRATION
            // =========================
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val firebaseUser = auth.currentUser
                        val uid = firebaseUser?.uid

                        val userData = hashMapOf(
                            "id" to uid,
                            "displayName" to "$selectedTitle $firstName $lastName",
                            "email" to email,
                            "phoneNumber" to fullPhone,
                            "photoUrl" to null,
                            "role" to "CASHIER",
                            "shopId" to "shop_001",
                            "isActive" to true,
                            "createdAt" to System.currentTimeMillis(),
                            "lastLoginAt" to null
                        )

                        if (uid != null) {
                            db.collection("users")
                                .document(uid)
                                .set(userData)
                                .addOnSuccessListener {

                                    Toast.makeText(
                                        this,
                                        "Account created successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    Handler(Looper.getMainLooper()).postDelayed({
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    }, 1000)

                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Saved auth but DB failed: ${it.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }

                    } else {
                        Toast.makeText(
                            this,
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
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