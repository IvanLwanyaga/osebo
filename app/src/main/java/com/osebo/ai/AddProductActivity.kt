package com.osebo.ai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.osebo.ai.databinding.ActivityAddProductBinding
import com.osebo.ai.models.Product
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private var selectedImageUri: Uri? = null
    private var imageUrl: String = ""

    // Barcode scanner launcher
    private val barcodeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val barcode = result.data?.getStringExtra("SCAN_RESULT")
            if (!barcode.isNullOrEmpty()) {
                binding.etBarcode.setText(barcode)
                searchProductByBarcode(barcode)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        setupToolbar()
        setupSpinners()
        setupClickListeners()
        setupBarcodeScanning()
        generateAutoSku()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupSpinners() {
        val categories = arrayOf(
            "Electronics", "Clothing", "Food & Drinks",
            "Household", "Beauty", "Pharmacy", "Stationery",
            "Sports", "Toys", "Automotive", "Other"
        )
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        (binding.autoCategory as? AutoCompleteTextView)?.setAdapter(categoryAdapter)

        val units = arrayOf(
            "Piece", "Kg", "Gram", "Liter", "ml",
            "Dozen", "Pack", "Box", "Carton", "Meter",
            "Roll", "Set", "Pair", "Bottle", "Can"
        )
        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, units)
        (binding.autoUnit as? AutoCompleteTextView)?.setAdapter(unitAdapter)
    }

    private fun setupClickListeners() {
        binding.cardProductImage.setOnClickListener { openImagePicker() }
        binding.btnUploadImage.setOnClickListener { openImagePicker() }
        binding.btnSave.setOnClickListener { saveProduct() }
        binding.btnCancel.setOnClickListener { finish() }

        binding.tilSku.setEndIconOnClickListener {
            generateAutoSku()
        }
    }

    private fun setupBarcodeScanning() {
        binding.tilBarcode.setEndIconOnClickListener {
            // TODO: Start actual scanner
            Toast.makeText(this, "Scanner coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchProductByBarcode(barcode: String) {
        db.collection("inventory")
            .whereEqualTo("barcode", barcode)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val product = documents.first().toObject(Product::class.java)
                    binding.etName.setText(product.name)
                    binding.autoCategory.setText(product.category, false)
                    binding.etSellingPrice.setText(product.price.toString())
                    binding.etCostPrice.setText(product.costPrice.toString())
                    binding.etStock.setText(product.stock.toString())
                    binding.etDescription.setText(product.description)
                    Snackbar.make(binding.root, "Product details loaded", Snackbar.LENGTH_LONG).show()
                }
            }
    }

    private fun generateAutoSku() {
        val timestamp = SimpleDateFormat("yyMMddHHmm", Locale.getDefault()).format(Date())
        val random = Random.nextInt(100, 999)
        binding.etSku.setText("SKU-$timestamp-$random")
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let {
                binding.ivProductImage.setImageURI(it)
                uploadImageToStorage()
            }
        }
    }

    private fun uploadImageToStorage() {
        selectedImageUri?.let { uri ->
            val userId = auth.currentUser?.uid ?: "unknown"
            val filename = "products/$userId/${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference.child(filename)

            binding.btnSave.isEnabled = false
            storageRef.putFile(uri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUrl = downloadUri.toString()
                    binding.btnSave.isEnabled = true
                    Snackbar.make(binding.root, "Image ready", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveProduct() {
        val name = binding.etName.text.toString().trim()
        val category = binding.autoCategory.text.toString().trim()
        val sellingPrice = binding.etSellingPrice.text.toString().toDoubleOrNull() ?: 0.0
        val costPrice = binding.etCostPrice.text.toString().toDoubleOrNull() ?: 0.0
        val stock = binding.etStock.text.toString().toIntOrNull() ?: 0
        val unit = binding.autoUnit.text.toString().trim()
        val lowStock = binding.etLowStock.text.toString().toIntOrNull() ?: 5
        val reorderQty = binding.etReorderQty.text.toString().toIntOrNull() ?: 0
        val taxRate = binding.etTaxRate.text.toString().toDoubleOrNull() ?: 0.0
        val weight = binding.etWeight.text.toString().toDoubleOrNull() ?: 0.0
        val sku = binding.etSku.text.toString().trim()
        val barcode = binding.etBarcode.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val supplier = binding.etSupplier.text.toString().trim()

        if (name.isEmpty() || category.isEmpty() || sellingPrice <= 0) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val product = Product(
            name = name,
            price = sellingPrice,
            costPrice = costPrice,
            category = category,
            barcode = barcode,
            imageUrl = imageUrl,
            stock = stock,
            lowStockAlert = lowStock,
//            reorderQuantity = reorderQty,
//            unit = unit,
            taxRate = taxRate,
            weight = weight,
            sku = sku,
            supplier = supplier,
            description = description,
            ownerId = userId,
            updatedAt = System.currentTimeMillis()
        )

        binding.btnSave.isEnabled = false
        db.collection("inventory").add(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Product Registered", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                binding.btnSave.isEnabled = true
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
