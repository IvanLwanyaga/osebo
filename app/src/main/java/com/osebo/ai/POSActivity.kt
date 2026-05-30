package com.osebo.ai

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class POSActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private val cart = mutableListOf<CartItem>()
    private lateinit var listView: ListView
    private lateinit var tvTotal: TextView
    private lateinit var btnCheckout: Button

    private var totalAmount = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos)

        db = FirebaseFirestore.getInstance()

        listView = findViewById(R.id.listViewProducts)
        tvTotal = findViewById(R.id.tvTotal)
        btnCheckout = findViewById(R.id.btnCheckout)

        loadProducts()

        btnCheckout.setOnClickListener {
            checkout()
        }
    }

    // =========================
    // LOAD PRODUCTS
    // =========================
    private fun loadProducts() {

        db.collection("products")
            .get()
            .addOnSuccessListener { result ->

                val productNames = mutableListOf<String>()

                for (doc in result) {
                    val name = doc.getString("name") ?: ""
                    val price = doc.getDouble("price") ?: 0.0
                    val stock = doc.getLong("stockQuantity") ?: 0

                    productNames.add("$name - UGX $price (Stock: $stock)")
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    productNames
                )

                listView.adapter = adapter

                listView.setOnItemClickListener { _, _, position, _ ->

                    val doc = result.documents[position]

                    val product = CartItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        quantity = 1
                    )

                    addToCart(product)
                }
            }
    }

    // =========================
    // ADD TO CART
    // =========================
    private fun addToCart(item: CartItem) {

        val existing = cart.find { it.id == item.id }

        if (existing != null) {
            existing.quantity += 1
        } else {
            cart.add(item)
        }

        calculateTotal()
        Toast.makeText(this, "${item.name} added", Toast.LENGTH_SHORT).show()
    }

    // =========================
    // CALCULATE TOTAL
    // =========================
    private fun calculateTotal() {

        totalAmount = cart.sumOf {
            it.price * it.quantity
        }

        tvTotal.text = "Total: UGX $totalAmount"
    }

    // =========================
    // CHECKOUT
    // =========================
    private fun checkout() {

        if (cart.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val saleId = UUID.randomUUID().toString()

        val sale = hashMapOf(
            "id" to saleId,
            "totalAmount" to totalAmount,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("sales")
            .document(saleId)
            .set(sale)
            .addOnSuccessListener {

                // Update stock
                for (item in cart) {
                    reduceStock(item.id, item.quantity)
                }

                cart.clear()
                calculateTotal()

                Toast.makeText(
                    this,
                    "Sale completed successfully",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // =========================
    // STOCK REDUCTION
    // =========================
    private fun reduceStock(productId: String, qty: Int) {

        val ref = db.collection("products").document(productId)

        db.runTransaction { transaction ->

            val snapshot = transaction.get(ref)

            val currentStock = snapshot.getLong("stockQuantity") ?: 0
            val newStock = currentStock - qty

            transaction.update(ref, "stockQuantity", newStock)
        }
    }
}