package com.osebo.ai.data.repositories

import com.osebo.ai.models.SubscriptionRequest
import com.osebo.ai.models.PackageType
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getAvailablePackages(): Flow<List<PackageType>>
    fun getSubscriptionHistory(): Flow<List<SubscriptionRequest>>
    suspend fun requestSubscription(request: SubscriptionRequest)
}
