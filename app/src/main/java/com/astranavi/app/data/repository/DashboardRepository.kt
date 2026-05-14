package com.astranavi.app.data.repository

import com.astranavi.app.data.api.ApiService
import com.astranavi.app.data.cache.ApiCachePolicy
import com.astranavi.app.data.cache.ApiResponseCache
import com.astranavi.app.data.model.AnalyzeFullRequest
import com.astranavi.app.data.model.AnalyzeFullWrapper
import com.astranavi.app.data.model.ChatHistoryWrapper
import com.astranavi.app.data.model.ForecastResponse
import com.astranavi.app.data.model.HoroscopeResponse
import com.astranavi.app.data.model.ProfileResponse
import retrofit2.Response

class DashboardRepository(
    private val apiService: ApiService,
    private val apiCache: ApiResponseCache? = null
) {
    suspend fun getDailyHoroscope(sign: String?): Response<HoroscopeResponse> {
        return apiCache?.getOrFetch(
            logicalKey = apiCache.dailyHoroscopeKey(sign),
            responseClass = HoroscopeResponse::class.java,
            policy = ApiCachePolicy.UntilNextLocalHour
        ) {
            apiService.getDailyHoroscope(sign)
        } ?: apiService.getDailyHoroscope(sign)
    }

    suspend fun analyzeFull(request: AnalyzeFullRequest): Response<AnalyzeFullWrapper> {
        return apiCache?.getOrFetch(
            logicalKey = apiCache.kundliKey(request),
            responseClass = AnalyzeFullWrapper::class.java,
            policy = ApiCachePolicy.UntilNextLocalMidnight,
            bypassRead = request.force_refresh,
            shouldCache = { it.success && it.astrologyData != null }
        ) {
            apiService.analyzeFull(request)
        } ?: apiService.analyzeFull(request)
    }

    suspend fun getForecast(area: String, daysBack: Int? = 3, daysForward: Int? = 3): Response<ForecastResponse> {
        return apiCache?.getOrFetch(
            logicalKey = apiCache.forecastKey(area, daysBack, daysForward),
            responseClass = ForecastResponse::class.java,
            policy = ApiCachePolicy.UntilNextLocalMidnight
        ) {
            apiService.getForecast(area, daysBack, daysForward)
        } ?: apiService.getForecast(area, daysBack, daysForward)
    }
}
