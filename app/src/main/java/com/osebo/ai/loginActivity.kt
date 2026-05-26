package com.osebo.ai

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        // =========================
        // SHARED PREFERENCES
        // =========================

        sharedPreferences = getSharedPreferences(
            "OseboSession",
            MODE_PRIVATE
        )

        // =========================
        // VIEWS
        // =========================

        val rbPhone =
            findViewById<RadioButton>(R.id.rbPhone)

        val rbEmail =
            findViewById<RadioButton>(R.id.rbEmail)

        val phoneLayout =
            findViewById<LinearLayout>(R.id.phoneLayout)

        val etEmail =
            findViewById<EditText>(R.id.etEmail)

        val etPhone =
            findViewById<EditText>(R.id.etPhone)

        val etPassword =
            findViewById<EditText>(R.id.etPassword)

        val btnLogin =
            findViewById<MaterialButton>(R.id.button)

        val tvRegister =
            findViewById<TextView>(R.id.tvRegister)

        val tvForgotPassword =
            findViewById<TextView>(R.id.tvForgotPassword)

        val cbRememberMe =
            findViewById<CheckBox>(R.id.cbRememberMe)

        val logo =
            findViewById<ImageView>(R.id.imageView2)

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

        // =========================
        // SWITCH LOGIN TYPE
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
        // LOGIN BUTTON
        // =========================

        btnLogin.setOnClickListener {

            val password =
                etPassword.text.toString().trim()

            // PHONE LOGIN
            if (rbPhone.isChecked) {

                val phone =
                    etPhone.text.toString().trim()

                if (phone.isEmpty()) {

                    etPhone.error =
                        "Enter phone number"

                    etPhone.requestFocus()

                    return@setOnClickListener
                }

            } else {

                // EMAIL LOGIN
                val email =
                    etEmail.text.toString().trim()

                if (email.isEmpty()) {

                    etEmail.error =
                        "Enter email"

                    etEmail.requestFocus()

                    return@setOnClickListener
                }

                if (!android.util.Patterns.EMAIL_ADDRESS
                        .matcher(email)
                        .matches()
                ) {

                    etEmail.error =
                        "Invalid email"

                    etEmail.requestFocus()

                    return@setOnClickListener
                }
            }

            // PASSWORD VALIDATION
            if (password.isEmpty()) {

                etPassword.error =
                    "Enter password"

                etPassword.requestFocus()

                return@setOnClickListener
            }

            if (password.length < 6) {

                etPassword.error =
                    "Minimum 6 characters"

                etPassword.requestFocus()

                return@setOnClickListener
            }

            // =========================
            // LOADING
            // =========================

            btnLogin.isEnabled = false

            btnLogin.text = "Signing In..."

            Handler(Looper.getMainLooper()).postDelayed({

                // =========================
                // SAVE SESSION ONLY
                // IF REMEMBER ME IS CHECKED
                // =========================

                if (cbRememberMe.isChecked) {

                    sharedPreferences.edit()
                        .putBoolean(
                            "isLoggedIn",
                            true
                        )
                        .apply()
                }

                Toast.makeText(
                    this,
                    "Login Successful ",
                    Toast.LENGTH_SHORT
                ).show()

                // =========================
                // GO TO DASHBOARD
                // =========================

                startActivity(
                    Intent(
                        this,
                        DashboardActivity::class.java
                    )
                )

                finish()

            }, 1500)
        }

        // =========================
        // FORGOT PASSWORD
        // =========================

        tvForgotPassword.setOnClickListener {

            Toast.makeText(
                this,
                "Forgot Password Coming Soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        // =========================
        // REGISTER SCREEN
        // =========================

        tvRegister.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )
        }
    }
}