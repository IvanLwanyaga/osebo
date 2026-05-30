package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        val rbPhone = findViewById<RadioButton>(R.id.rbPhone)
        val rbEmail = findViewById<RadioButton>(R.id.rbEmail)

        val phoneLayout = findViewById<LinearLayout>(R.id.phoneLayout)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        val btnLogin = findViewById<MaterialButton>(R.id.button)

        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvForgotPassword =
            findViewById<TextView>(R.id.tvForgotPassword)

        val cbRememberMe =
            findViewById<CheckBox>(R.id.cbRememberMe)

        val logo =
            findViewById<ImageView>(R.id.imageView2)

        // Logo animation
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

        // Default = Email Login
        rbEmail.isChecked = true
        etEmail.visibility = View.VISIBLE
        phoneLayout.visibility = View.GONE

        rbEmail.setOnClickListener {

            etEmail.visibility = View.VISIBLE
            phoneLayout.visibility = View.GONE
        }

        rbPhone.setOnClickListener {

            phoneLayout.visibility = View.VISIBLE
            etEmail.visibility = View.GONE
        }

        // LOGIN BUTTON
        btnLogin.setOnClickListener {

            val password =
                etPassword.text.toString().trim()

            if (password.isEmpty()) {

                etPassword.error =
                    "Password required"

                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {

                etPassword.error =
                    "Minimum 6 characters"

                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (rbEmail.isChecked) {

                val email =
                    etEmail.text.toString().trim()

                if (email.isEmpty()) {

                    etEmail.error =
                        "Email required"

                    etEmail.requestFocus()
                    return@setOnClickListener
                }

                if (!Patterns.EMAIL_ADDRESS
                        .matcher(email)
                        .matches()
                ) {

                    etEmail.error =
                        "Invalid email"

                    etEmail.requestFocus()
                    return@setOnClickListener
                }

                btnLogin.isEnabled = false
                btnLogin.text = "Signing In..."

                loginUser(
                    email,
                    password,
                    cbRememberMe,
                    btnLogin
                )

            } else {

                Toast.makeText(
                    this,
                    "Phone login requires Firebase OTP setup",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Register
        tvRegister.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )
        }

        // Forgot Password
        tvForgotPassword.setOnClickListener {

            val email =
                etEmail.text.toString().trim()

            if (email.isEmpty()) {

                Toast.makeText(
                    this,
                    "Enter your email first",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Password reset email sent",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener {

                    Toast.makeText(
                        this,
                        it.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun loginUser(
        email: String,
        password: String,
        cbRememberMe: CheckBox,
        btnLogin: MaterialButton
    ) {

        auth.signInWithEmailAndPassword(
            email,
            password
        )
            .addOnSuccessListener {

                val uid =
                    auth.currentUser?.uid

                if (uid == null) {

                    resetButton(btnLogin)
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { doc ->

                        val role =
                            doc.getString("role")
                                ?: "CASHIER"

                        val shopId =
                            doc.getString("shopId")
                                ?: ""

                        if (cbRememberMe.isChecked) {

                            sessionManager.saveUserData(
                                uid,
                                role,
                                shopId
                            )
                        }

                        Toast.makeText(
                            this,
                            "Welcome Back",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent =
                            Intent(
                                this,
                                DashboardActivity::class.java
                            )

                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {

                        Toast.makeText(
                            this,
                            "Failed to load user profile",
                            Toast.LENGTH_LONG
                        ).show()

                        resetButton(btnLogin)
                    }

            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Login Failed: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()

                resetButton(btnLogin)
            }
    }

    private fun resetButton(
        btnLogin: MaterialButton
    ) {

        btnLogin.isEnabled = true
        btnLogin.text = "Login"
    }
}