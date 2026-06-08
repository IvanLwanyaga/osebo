package com.osebo.ai.data.repositories

import com.osebo.ai.models.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAccountInfo(): Flow<Account>
    suspend fun updateAccount(account: Account)
}
