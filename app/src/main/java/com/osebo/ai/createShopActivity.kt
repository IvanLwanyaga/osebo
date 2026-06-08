package com.osebo.ai

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.hbb20.CountryCodePicker
import com.osebo.ai.models.Shop
import java.util.UUID

class CreateShopActivity : AppCompatActivity() {

    private lateinit var tilShopName: TextInputLayout
    private lateinit var tilBusinessType: TextInputLayout
    private lateinit var tilPhone: TextInputLayout
    private lateinit var tilLocation: TextInputLayout
    private lateinit var tilTaxNumber: TextInputLayout
    private lateinit var tilDescription: TextInputLayout

    private lateinit var etShopName: TextInputEditText
    private lateinit var spinnerBusinessType: AutoCompleteTextView
    private lateinit var etPhone: TextInputEditText
    private lateinit var etLocation: TextInputEditText
    private lateinit var etTaxNumber: TextInputEditText
    private lateinit var etDescription: TextInputEditText

    private lateinit var ccp: CountryCodePicker
    private lateinit var chipGroupHours: ChipGroup
    private lateinit var cbTerms: CheckBox
    private lateinit var btnCreateShop: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var loadingOverlay: View

    private lateinit var cvShopLogo: MaterialCardView
    private lateinit var ivShopLogo: ImageView

    private var selectedImageUri: Uri? = null
    private var shopImageUrl: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                ivShopLogo.setImageURI(uri)
                ivShopLogo.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_shop)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        initializeViews()
        setupToolbar()
        setupBusinessTypesDropdown()
        setupCountryCodePicker()
        setupValidationWatchers()
        setupListeners()
        setupKeyboardHandling()
    }

    private fun initializeViews() {
        tilShopName = findViewById(R.id.tilShopName)
        tilBusinessType = findViewById(R.id.tilBusinessType)
        tilPhone = findViewById(R.id.tilPhone)
        tilLocation = findViewById(R.id.tilLocation)
        tilTaxNumber = findViewById(R.id.tilTaxNumber)
        tilDescription = findViewById(R.id.tilDescription)

        etShopName = findViewById(R.id.etShopName)
        spinnerBusinessType = findViewById(R.id.spinnerBusinessType)
        etPhone = findViewById(R.id.etPhone)
        etLocation = findViewById(R.id.etLocation)
        etTaxNumber = findViewById(R.id.etTaxNumber)
        etDescription = findViewById(R.id.etDescription)

        ccp = findViewById(R.id.ccp)
        chipGroupHours = findViewById(R.id.chipGroupHours)
        cbTerms = findViewById(R.id.cbTerms)
        btnCreateShop = findViewById(R.id.btnCreateShop)
        btnCancel = findViewById(R.id.btnCancel)
        loadingOverlay = findViewById(R.id.loadingOverlay)

        cvShopLogo = findViewById(R.id.cvShopLogo)
        ivShopLogo = findViewById(R.id.ivShopLogo)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupBusinessTypesDropdown() {
        val businessTypes = arrayOf("Retail", "Wholesale", "Service", "Restaurant", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, businessTypes)
        spinnerBusinessType.setAdapter(adapter)
    }

    private fun setupCountryCodePicker() {
        ccp.registerCarrierNumberEditText(etPhone)
    }

    private fun setupValidationWatchers() {
        etShopName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length < 3) tilShopName.error = "Name too short"
                else tilShopName.error = null
            }
        })
    }

    private fun setupListeners() {
        btnCreateShop.setOnClickListener {
            if (validateForm()) createShop()
        }
        btnCancel.setOnClickListener { finish() }
        cvShopLogo.setOnClickListener { openImagePicker() }
    }

    private fun setupKeyboardHandling() {
        val nestedScrollView = findViewById<androidx.core.widget.NestedScrollView>(R.id.nestedScrollView)
        ViewCompat.setOnApplyWindowInsetsListener(nestedScrollView) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(0, 0, 0, imeInsets.bottom)
            insets
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun validateForm(): Boolean {
        if (etShopName.text.isNullOrBlank()) {
            tilShopName.error = "Required"
            return false
        }
        if (!cbTerms.isChecked) {
            Toast.makeText(this, "Accept terms", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun createShop() {
        showLoading(true)
        val shopId = UUID.randomUUID().toString()
        if (selectedImageUri != null) {
            uploadImageAndSave(shopId)
        } else {
            saveShopToFirestore(shopId, null)
        }
    }

    private fun uploadImageAndSave(shopId: String) {
        val ref = storage.reference.child("shops/$shopId.jpg")
        ref.putFile(selectedImageUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { uri ->
                saveShopToFirestore(shopId, uri.toString())
            }
        }.addOnFailureListener {
            showLoading(false)
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveShopToFirestore(shopId: String, imageUrl: String?) {
        val shop = Shop(
            id = shopId,
            name = etShopName.text.toString().trim(),
            category = spinnerBusinessType.text.toString(),
            phoneNumber = ccp.fullNumberWithPlus,
            address = etLocation.text.toString().trim(),
            ownerId = auth.currentUser?.uid ?: "",
            createdAt = System.currentTimeMillis()
        )

        firestore.collection("shops").document(shopId).set(shop)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Shop created", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }
}
