package com.osebo.ai

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.hbb20.CountryCodePicker
import com.google.firebase.firestore.FirebaseFirestore

class CreateShopActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_shop)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // =======================
        // VIEW BINDING
        // =======================
        val etShopName = findViewById<EditText>(R.id.etShopName)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerBusinessType)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etLocation = findViewById<EditText>(R.id.etLocation)
        val etDescription = findViewById<EditText>(R.id.etDescription)

        val ccp = findViewById<CountryCodePicker>(R.id.ccp)
        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val btnCreateShop = findViewById<MaterialButton>(R.id.btnCreateShop)

        // =======================
        // SPINNER SETUP
        // =======================
        val categories = arrayOf(
            "Select Category",
            "Fashion",
            "Electronics",
            "Groceries",
            "Beauty",
            "Restaurant",
            "Furniture",
            "Pharmacy",
            "Books",
            "Other"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        spinnerCategory.adapter = adapter
        spinnerCategory.setSelection(0)

        // =======================
        // CREATE SHOP BUTTON
        // =======================
        btnCreateShop.setOnClickListener {

            val shopName = etShopName.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val phone = etPhone.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val fullPhone = ccp.selectedCountryCodeWithPlus + phone

            // =======================
            // VALIDATION
            // =======================

            if (shopName.isEmpty()) {
                etShopName.error = "Shop name required"
                etShopName.requestFocus()
                return@setOnClickListener
            }

            if (category == "Select Category") {
                Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.isEmpty() || phone.length < 9) {
                etPhone.error = "Valid phone required"
                etPhone.requestFocus()
                return@setOnClickListener
            }

            if (location.isEmpty()) {
                etLocation.error = "Location required"
                etLocation.requestFocus()
                return@setOnClickListener
            }

            if (!cbTerms.isChecked) {
                Toast.makeText(this, "Accept terms to continue", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // =======================
            // LOADING STATE
            // =======================
            btnCreateShop.isEnabled = false
            btnCreateShop.text = "Creating..."

            // =======================
            // FIREBASE DATA
            // =======================
            val shopData = hashMapOf(
                "name" to shopName,
                "category" to category,
                "phone" to fullPhone,
                "location" to location,
                "description" to description,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("shops")
                .add(shopData)
                .addOnSuccessListener {

                    btnCreateShop.isEnabled = true
                    btnCreateShop.text = "Create Shop"

                    Toast.makeText(
                        this,
                        "Shop created successfully!",
                        Toast.LENGTH_LONG
                    ).show()

                    finish() // go back to dashboard
                }
                .addOnFailureListener { e ->

                    btnCreateShop.isEnabled = true
                    btnCreateShop.text = "Create Shop"

                    Toast.makeText(
                        this,
                        "Failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}