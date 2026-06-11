package com.osebo.ai

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.osebo.ai.R
import com.osebo.ai.databinding.ActivitySettingsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    // ─── View Binding ──────────────────────────────────────────────────────────
    private lateinit var binding: ActivitySettingsBinding

    // ─── Firebase ──────────────────────────────────────────────────────────────
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    // ─── State ─────────────────────────────────────────────────────────────────
    private var isEditMode = false
    private var selectedPhotoUri: Uri? = null

    // ─── Photo Pickers ─────────────────────────────────────────────────────────
    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedPhotoUri = result.data?.data
                selectedPhotoUri?.let { uri ->
                    binding.imgProfile.setImageURI(uri)
                }
            }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? android.graphics.Bitmap
                imageBitmap?.let {
                    binding.imgProfile.setImageBitmap(it)
                    // Save bitmap to a temporary file to get a URI for uploading
                    val path = MediaStore.Images.Media.insertImage(contentResolver, it, "ProfileTemp", null)
                    selectedPhotoUri = Uri.parse(path)
                }
            }
        }

    // ─── Dropdown Data ─────────────────────────────────────────────────────────
    private val genderOptions = listOf("Male", "Female", "Non-binary", "Prefer not to say")
    private val countryOptions = Locale.getISOCountries().map { 
        Locale("", it).displayCountry 
    }.sorted()
    
    private val cityOptions = listOf(
        "Kampala", "Entebbe", "Jinye", "Mbarara", "Gulu", "Nairobi", "Mombasa", "Dar es Salaam", "Kigali", "Addis Ababa"
    )
    
    private val languageOptions = listOf(
        "English" to "en",
        "Swahili" to "sw",
        "French" to "fr",
        "Arabic" to "ar"
    )

    // ──────────────────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ccp.registerCarrierNumberEditText(binding.etPhone)

        setupDropdowns()
        loadUserData()
        setupClickListeners()
    }

    // ─── Dropdowns ─────────────────────────────────────────────────────────────
    private fun setupDropdowns() {
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        (binding.etGender as? AutoCompleteTextView)?.setAdapter(genderAdapter)

        val countryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countryOptions)
        (binding.etCountry as? AutoCompleteTextView)?.setAdapter(countryAdapter)

        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cityOptions)
        (binding.etCity as? AutoCompleteTextView)?.setAdapter(cityAdapter)
    }

    // ─── Load User Data ────────────────────────────────────────────────────────
    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val name = doc.getString("displayName") ?: ""
                    val username = doc.getString("username") ?: ""
                    val email = doc.getString("email") ?: auth.currentUser?.email ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val dob = doc.getString("dateOfBirth") ?: ""
                    val gender = doc.getString("gender") ?: ""
                    val bio = doc.getString("bio") ?: ""
                    val country = doc.getString("country") ?: ""
                    val city = doc.getString("city") ?: ""
                    val address = doc.getString("address") ?: ""
                    val is2FAEnabled = doc.getBoolean("twoFactorEnabled") ?: false

                    // Populate fields
                    binding.etDisplayName.setText(name)
                    binding.etUsername.setText(username)
                    binding.etEmail.setText(email)
                    binding.etPhone.setText(phone)
                    binding.etDob.setText(dob)
                    binding.etGender.setText(gender, false)
                    binding.etBio.setText(bio)
                    binding.etCountry.setText(country, false)
                    binding.etCity.setText(city, false)
                    binding.etAddress.setText(address)

                    // CCP setup
                    if (phone.contains("+")) {
                        binding.ccp.fullNumber = phone
                    }

                    // Header
                    binding.txtName.text = name.ifBlank { "Osebo User" }
                    binding.txtEmailPreview.text = email

                    // Security status
                    binding.txt2faStatus.text = if (is2FAEnabled) "On" else "Off"
                    binding.txt2faStatus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            if (is2FAEnabled) R.color.success else R.color.text_secondary
                        )
                    )

                    // Notification & privacy prefs
                    binding.switchPush.isChecked = doc.getBoolean("notifPush") ?: true
                    binding.switchEmail.isChecked = doc.getBoolean("notifEmail") ?: true
                    binding.switchSms.isChecked = doc.getBoolean("notifSms") ?: false
                    binding.switchPublicProfile.isChecked = doc.getBoolean("publicProfile") ?: true
                    binding.switchActivity.isChecked = doc.getBoolean("showActivity") ?: false
                }
            }
            .addOnFailureListener {
                showSnackbar("Failed to load account data.")
            }
    }

    // ─── Click Listeners ───────────────────────────────────────────────────────
    private fun setupClickListeners() {

        // Back button
        binding.btnBack.setOnClickListener { finish() }

        // Edit / Cancel toggle
        binding.btnEditToggle.setOnClickListener {
            isEditMode = !isEditMode
            setEditMode(isEditMode)
        }

        // Save button
        binding.btnSavePersonal.setOnClickListener {
            if (validateFields()) saveUserData()
        }

        // Photo picker
        binding.fabEditPhoto.setOnClickListener { showPhotoSelectionDialog() }
        binding.imgProfile.setOnClickListener { if (isEditMode) showPhotoSelectionDialog() }

        // Date of birth picker
        binding.etDob.setOnClickListener { if (isEditMode) showDatePicker() }
        binding.tilDob.setEndIconOnClickListener { if (isEditMode) showDatePicker() }

        // Security rows
        binding.rowChangePassword.setOnClickListener { 
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
        binding.rowTwoFactor.setOnClickListener { showTwoFactorDialog() }
        binding.rowDevices.setOnClickListener {
            startActivity(Intent(this, DevicesActivity::class.java))
        }

        // Language preference
        binding.rowLanguage.setOnClickListener { showLanguagePicker() }

        // Dark mode toggle
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        // Notification & privacy switches (auto-save on change)
        listOf(
            binding.switchPush,
            binding.switchEmail,
            binding.switchSms,
            binding.switchPublicProfile,
            binding.switchActivity
        ).forEach { switch ->
            switch.setOnCheckedChangeListener { _, _ -> savePreferences() }
        }

        // Log Out
        binding.btnLogout.setOnClickListener { confirmLogout() }

        // Delete Account
        binding.btnDeleteAccount.setOnClickListener { confirmDeleteAccount() }
    }

    // ─── Edit Mode Toggle ──────────────────────────────────────────────────────
    private fun setEditMode(enabled: Boolean) {
        val fields = listOf(
            binding.etDisplayName, binding.etUsername, binding.etEmail,
            binding.etPhone, binding.etBio, binding.etAddress
        )
        val dropdowns = listOf(binding.etGender, binding.etCountry, binding.etCity)

        fields.forEach { it.isEnabled = enabled }
        dropdowns.forEach {
            it.isEnabled = enabled
            it.isFocusable = enabled
        }

        binding.ccp.setCcpClickable(enabled)

        binding.etDob.isEnabled = enabled
        binding.fabEditPhoto.visibility = if (enabled) View.VISIBLE else View.GONE
        binding.btnSavePersonal.visibility = if (enabled) View.VISIBLE else View.GONE
        binding.btnEditToggle.text = if (enabled) "Cancel" else "Edit"

        if (!enabled) {
            // Discard unsaved changes — reload from Firestore
            loadUserData()
        }
    }

    // ─── Validation ────────────────────────────────────────────────────────────
    private fun validateFields(): Boolean {
        var valid = true

        val name = binding.etDisplayName.text?.toString()?.trim() ?: ""
        val email = binding.etEmail.text?.toString()?.trim() ?: ""

        if (name.isBlank()) {
            binding.tilDisplayName.error = "Full name is required"
            valid = false
        } else binding.tilDisplayName.error = null

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email address"
            valid = false
        } else binding.tilEmail.error = null

        return valid
    }

    // ─── Save Personal Data ────────────────────────────────────────────────────
    private fun saveUserData() {
        val uid = auth.currentUser?.uid ?: return

        val updates = mutableMapOf<String, Any>(
            "displayName" to binding.etDisplayName.text.toString().trim(),
            "username" to binding.etUsername.text.toString().trim(),
            "email" to binding.etEmail.text.toString().trim(),
            "phone" to binding.ccp.fullNumberWithPlus,
            "dateOfBirth" to binding.etDob.text.toString().trim(),
            "gender" to binding.etGender.text.toString().trim(),
            "bio" to binding.etBio.text.toString().trim(),
            "country" to binding.etCountry.text.toString().trim(),
            "city" to binding.etCity.text.toString().trim(),
            "address" to binding.etAddress.text.toString().trim()
        )

        if (selectedPhotoUri != null) {
            uploadProfilePhoto(uid, updates)
        } else {
            pushToFirestore(uid, updates)
        }
    }

    private fun uploadProfilePhoto(uid: String, updates: Map<String, Any>) {
        val ref = storage.reference.child("profile_photos/$uid.jpg")
        ref.putFile(selectedPhotoUri!!)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception!!
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                val updatesWithPhoto = updates.toMutableMap()
                updatesWithPhoto["photoUrl"] = downloadUri.toString()
                pushToFirestore(uid, updatesWithPhoto)
            }
            .addOnFailureListener {
                showSnackbar("Photo upload failed. Other changes will still be saved.")
                pushToFirestore(uid, updates)
            }
    }

    private fun pushToFirestore(uid: String, updates: Map<String, Any>) {
        firestore.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                showSnackbar("Account updated successfully!")
                binding.txtName.text = updates["displayName"] as? String ?: ""
                binding.txtEmailPreview.text = updates["email"] as? String ?: ""
                isEditMode = false
                setEditMode(false)
            }
            .addOnFailureListener {
                showSnackbar("Update failed. Please try again.")
            }
    }

    // ─── Save Preferences (switches) ──────────────────────────────────────────
    private fun savePreferences() {
        val uid = auth.currentUser?.uid ?: return
        val prefs = mapOf(
            "notifPush" to binding.switchPush.isChecked,
            "notifEmail" to binding.switchEmail.isChecked,
            "notifSms" to binding.switchSms.isChecked,
            "publicProfile" to binding.switchPublicProfile.isChecked,
            "showActivity" to binding.switchActivity.isChecked
        )
        firestore.collection("users").document(uid).update(prefs)
    }

    // ─── Photo Selection Dialog ────────────────────────────────────────────────
    private fun showPhotoSelectionDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePhotoLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhotoLauncher.launch(intent)
    }

    // ─── Date Picker ───────────────────────────────────────────────────────────
    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val formatted = "%02d / %02d / %04d".format(day, month + 1, year)
                binding.etDob.text = Editable.Factory.getInstance().newEditable(formatted)
            },
            cal.get(Calendar.YEAR) - 18,  // default to 18 years ago
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
            show()
        }
    }

    // ─── Two-Factor Auth Dialog ────────────────────────────────────────────────
    private fun showTwoFactorDialog() {
        val uid = auth.currentUser?.uid ?: return
        val is2FA = binding.txt2faStatus.text == "On"
        AlertDialog.Builder(this)
            .setTitle("Two-Factor Authentication")
            .setMessage(
                if (is2FA) "Disable two-factor authentication?\nThis will make your account less secure."
                else "Enable two-factor authentication for extra security."
            )
            .setPositiveButton(if (is2FA) "Disable" else "Enable") { _, _ ->
                val newState = !is2FA
                firestore.collection("users").document(uid)
                    .update("twoFactorEnabled", newState)
                    .addOnSuccessListener {
                        binding.txt2faStatus.text = if (newState) "On" else "Off"
                        binding.txt2faStatus.setTextColor(
                            ContextCompat.getColor(this, if (newState) R.color.success else R.color.text_secondary)
                        )
                        showSnackbar("2FA ${if (newState) "enabled" else "disabled"}.")
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Language Picker ───────────────────────────────────────────────────────
    private fun showLanguagePicker() {
        val displayNames = languageOptions.map { it.first }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setItems(displayNames) { _, which ->
                val (name, code) = languageOptions[which]
                setLocale(code)
            }
            .show()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        
        // Restart activity to apply changes
        val intent = intent
        finish()
        startActivity(intent)
    }

    // ─── Logout Confirmation ───────────────────────────────────────────────────
    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                auth.signOut()
                finishAffinity()
                // startActivity(Intent(this, LoginActivity::class.java))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Delete Account Confirmation ───────────────────────────────────────────
    private fun confirmDeleteAccount() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("This action is permanent and cannot be undone. All your data will be erased.")
            .setPositiveButton("Delete Account") { _, _ ->
                val uid = auth.currentUser?.uid ?: return@setPositiveButton
                firestore.collection("users").document(uid).delete()
                    .addOnSuccessListener {
                        auth.currentUser?.delete()
                            ?.addOnSuccessListener {
                                showSnackbar("Account deleted.")
                                finishAffinity()
                            }
                            ?.addOnFailureListener {
                                showSnackbar("Failed to delete account. Please re-authenticate and try again.")
                            }
                    }
                    .addOnFailureListener {
                        showSnackbar("Could not delete account data. Try again later.")
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Snackbar Helper ───────────────────────────────────────────────────────
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
