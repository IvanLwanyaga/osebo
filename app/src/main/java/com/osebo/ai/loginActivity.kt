package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.osebo.ai.databinding.ActivityLoginBinding
import com.osebo.ai.utils.ToastHelper
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var currentPhoneNumber: String = ""
    private var isOtpSent = false

    private var resendTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupUI()
    }

    private fun setupUI() {
        // Default states
        binding.etOtp.visibility = View.GONE
        binding.tvResendOtp.visibility = View.GONE
        binding.tvForgotPassword.visibility = View.GONE

        // Toggle between Email and Phone Login
        binding.radioGroupLogin.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbPhone) {
                binding.phoneLayout.visibility = View.VISIBLE
                binding.etEmail.visibility = View.GONE
                binding.button.text = "Send OTP"
                binding.tvForgotPassword.visibility = View.GONE
            } else {
                binding.phoneLayout.visibility = View.GONE
                binding.etEmail.visibility = View.VISIBLE
                binding.button.text = "Sign In"
                binding.tvForgotPassword.visibility = View.VISIBLE
            }
            resetOtpState()
        }

        // Main Button Action
        binding.button.setOnClickListener {
            if (binding.rbPhone.isChecked) {
                if (!isOtpSent) sendOtp() else verifyOtp()
            } else {
                emailLogin()
            }
        }

        // Resend OTP
        binding.tvResendOtp.setOnClickListener {
            if (currentPhoneNumber.isNotEmpty()) resendOtp()
        }

        // Forgot Password
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Register
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // ========================= OTP SEND =========================
    private fun sendOtp() {
        val rawPhone = binding.etPhone.text.toString().trim()
        if (rawPhone.isEmpty() || rawPhone.length < 9) {
            binding.etPhone.error = "Enter valid phone number"
            return
        }

        currentPhoneNumber = binding.ccp.selectedCountryCodeWithPlus + rawPhone
        setLoading(true, "Sending OTP...")

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(currentPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(phoneAuthCallbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // ========================= RESEND OTP =========================
    private fun resendOtp() {
        setLoading(true, "Resending OTP...")

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(currentPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(phoneAuthCallbacks)
            .setForceResendingToken(resendToken!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // ========================= OTP VERIFY =========================
    private fun verifyOtp() {
        val code = binding.etOtp.text.toString().trim()
        if (code.length < 6) {
            binding.etOtp.error = "Enter 6-digit OTP"
            return
        }

        setLoading(true, "Verifying OTP...")
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithCredential(credential)
    }

    // ========================= PHONE AUTH CALLBACKS =========================
    private val phoneAuthCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            setLoading(false, "Send OTP")
            Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            verificationId = id
            resendToken = token
            isOtpSent = true

            setLoading(false, "Verify OTP")
            binding.etOtp.visibility = View.VISIBLE
            binding.tvResendOtp.visibility = View.VISIBLE

            startResendTimer()
            Toast.makeText(this@LoginActivity, "OTP sent successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                setLoading(false, if (isOtpSent) "Verify OTP" else "Sign In")
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    goToDashboard()
                } else {
                    Toast.makeText(this, "Verification failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // ========================= EMAIL LOGIN =========================
    private fun emailLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true, "Signing in...")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                setLoading(false, "Sign In")
                if (task.isSuccessful) {
                    goToDashboard()
                } else {
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // ========================= RESEND TIMER =========================
    private fun startResendTimer() {
        resendTimer?.cancel()
        binding.tvResendOtp.isEnabled = false

        resendTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvResendOtp.text = "Resend in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                binding.tvResendOtp.text = "Resend OTP"
                binding.tvResendOtp.isEnabled = true
            }
        }.start()
    }

    // ========================= HELPERS =========================
    private fun resetOtpState() {
        isOtpSent = false
        verificationId = null
        binding.etOtp.visibility = View.GONE
        binding.tvResendOtp.visibility = View.GONE
        resendTimer?.cancel()
    }

    private fun setLoading(loading: Boolean, buttonText: String) {
        binding.button.isEnabled = !loading
        binding.button.text = buttonText
    }

    private fun goToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }
}