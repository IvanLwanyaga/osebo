package com.osebo.ai

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // =========================
        // FIREBASE INIT
        // =========================
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        sharedPreferences = getSharedPreferences("OseboSession", MODE_PRIVATE)

        // =========================
        // VIEWS
        // =========================
        val rbPhone = findViewById<RadioButton>(R.id.rbPhone)
        val rbEmail = findViewById<RadioButton>(R.id.rbEmail)
        val phoneLayout = findViewById<LinearLayout>(R.id.phoneLayout)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.button)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val cbRememberMe = findViewById<CheckBox>(R.id.cbRememberMe)
        val logo = findViewById<ImageView>(R.id.imageView2)

        // =========================
        // FLOATING LOGO ANIMATION
        // =========================
        logo.animate()
            .translationYBy(-20f)
            .setDuration(1200)
            .withEndAction {
                logo.animate()
                    .translationYBy(20f)
                    .setDuration(1200)
                    .start()
            }
            .start()

        // =========================
        // DEFAULT STATE
        // =========================
        rbPhone.isChecked = true
        phoneLayout.visibility = View.VISIBLE
        etEmail.visibility = View.GONE

        rbPhone.setOnClickListener {
            phoneLayout.visibility = View.VISIBLE
            etEmail.visibility = View.GONE
        }

        rbEmail.setOnClickListener {
            phoneLayout.visibility = View.GONE
            etEmail.visibility = View.VISIBLE
        }

        // =========================
        // LOGIN BUTTON
        // =========================
        btnLogin.setOnClickListener {

            val password = etPassword.text.toString().trim()

            if (password.isEmpty()) {
                etPassword.error = "Enter password"
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPassword.error = "Minimum 6 characters"
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            btnLogin.text = "Signing In..."

            // =========================
            // EMAIL LOGIN ONLY (Firebase supports email/password)
            // =========================
            val email = etEmail.text.toString().trim()

            if (rbEmail.isChecked) {

                if (email.isEmpty()) {
                    etEmail.error = "Enter email"
                    resetButton(btnLogin)
                    return@setOnClickListener
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.error = "Invalid email"
                    resetButton(btnLogin)
                    return@setOnClickListener
                }

                firebaseLogin(email, password, cbRememberMe, btnLogin)
            } else {

                // PHONE LOGIN (Firebase needs conversion OR OTP system)
                val phone = etPhone.text.toString().trim()

                if (phone.isEmpty()) {
                    etPhone.error = "Enter phone number"
                    resetButton(btnLogin)
                    return@setOnClickListener
                }

                Toast.makeText(
                    this,
                    "Phone login requires OTP authentication (not email/password)",
                    Toast.LENGTH_LONG
                ).show()

                resetButton(btnLogin)
            }
        }

        // =========================
        // REGISTER SCREEN
        // =========================
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // =========================
        // FORGOT PASSWORD
        // =========================
        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter email first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Reset link sent to email", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
        }
    }

    // =========================
    // FIREBASE LOGIN FUNCTION
    // =========================
    private fun firebaseLogin(
        email: String,
        password: String,
        cbRememberMe: CheckBox,
        btnLogin: MaterialButton
    ) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val uid = auth.currentUser?.uid

                    if (uid != null) {

                        // Fetch user profile (role, shop, etc.)
                        db.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener { doc ->

                                val role = doc.getString("role") ?: "CASHIER"

                                // Save session if needed
                                if (cbRememberMe.isChecked) {
                                    sharedPreferences.edit()
                                        .putBoolean("isLoggedIn", true)
                                        .putString("uid", uid)
                                        .putString("role", role)
                                        .apply()
                                }

                                Toast.makeText(
                                    this,
                                    "Login Successful",
                                    Toast.LENGTH_SHORT
                                ).show()

                                Handler(Looper.getMainLooper()).postDelayed({

                                    val intent = when (role) {
                                        "ADMIN" -> Intent(this, DashboardActivity::class.java)
                                        "MANAGER" -> Intent(this, DashboardActivity::class.java)
                                        else -> Intent(this, DashboardActivity::class.java)
                                    }

                                    startActivity(intent)
                                    finish()

                                }, 800)
                            }
                    }

                } else {
                    Toast.makeText(
                        this,
                        "Login Failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                resetButton(btnLogin)
            }
    }

    // =========================
    // RESET BUTTON UI
    // =========================
    private fun resetButton(btn: MaterialButton) {
        btn.isEnabled = true
        btn.text = "Login"
    }
}