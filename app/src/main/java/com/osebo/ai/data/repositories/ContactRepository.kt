package com.osebo.ai.data.repositories

import com.osebo.ai.models.SupportMessage

interface ContactRepository {
    suspend fun sendSupportMessage(message: SupportMessage)
}
