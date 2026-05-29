package com.osebo.ai.utils

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseHelper {

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}