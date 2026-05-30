package com.osebo.ai

import android.content.Context

class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences(
            "osebo_session",
            Context.MODE_PRIVATE
        )

    fun saveUserData(
        uid: String,
        role: String,
        shopId: String
    ) {
        prefs.edit()
            .putString("uid", uid)
            .putString("role", role)
            .putString("shopId", shopId)
            .apply()
    }

    fun getUid(): String {
        return prefs.getString("uid", "") ?: ""
    }

    fun getRole(): String {
        return prefs.getString("role", "CASHIER")
            ?: "CASHIER"
    }

    fun getShopId(): String {
        return prefs.getString("shopId", "")
            ?: ""
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}