package com.osebo.ai.data.repositories

import com.osebo.ai.models.TimeSeriesData
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    fun getRevenueStats(): Flow<List<TimeSeriesData>>
    fun getProfitStats(): Flow<List<TimeSeriesData>>
}
