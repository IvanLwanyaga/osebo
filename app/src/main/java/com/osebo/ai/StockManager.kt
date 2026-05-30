package com.osebo.ai

import com.google.firebase.firestore.FirebaseFirestore

class StockManager {

    private val db =
        FirebaseFirestore.getInstance()

    fun reduceStock(
        productId: String,
        qty: Int
    ) {

        val ref =
            db.collection("products")
                .document(productId)

        db.runTransaction {

            val snapshot =
                it.get(ref)

            val stock =
                snapshot.getLong(
                    "stockQuantity"
                ) ?: 0

            it.update(
                ref,
                "stockQuantity",
                stock - qty
            )
        }
    }
}