package com.osebo.ai.data.repositories

import com.osebo.ai.models.User

interface AuthRepository {
    fun getCurrentUser(): User?
    suspend fun logout()
}
