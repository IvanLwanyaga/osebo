package com.osebo.ai.data.repositories

import com.osebo.ai.models.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getCustomers(): Flow<List<Customer>>
    suspend fun addCustomer(customer: Customer)
}
