package com.osebo.ai.data.repositories

import com.osebo.ai.models.SalesData
import com.osebo.ai.models.RecentActivity
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getSalesSummary(): Flow<SalesData>
    fun getRecentActivities(): Flow<List<RecentActivity>>
}
