package com.osebo.ai.repository

import com.osebo.ai.models.Shop
import com.osebo.ai.utils.FirebaseHelper

class ShopRepository {

    private val shopsRef = FirebaseHelper.firestore.collection("shops")

    fun addShop(shop: Shop, onResult: (Boolean) -> Unit) {

        shopsRef
            .document(shop.id)
            .set(shop)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}