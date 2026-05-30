package com.astranavi.app.data.repository

import com.astranavi.app.data.api.ApiService
import com.astranavi.app.data.cache.ApiCachePolicy
import com.astranavi.app.data.cache.ApiResponseCache
import com.astranavi.app.data.cache.CacheMeta
import com.astranavi.app.data.model.AnalyzeFullRequest
import com.astranavi.app.data.model.AnalyzeFullWrapper
import com.astranavi.app.data.model.ChatHistoryWrapper
import com.astranavi.app.data.model.DailyHoroscopeTimingsResponse
import com.astranavi.app.data.model.MonthlyForecastResponse
import com.astranavi.app.data.model.HoroscopeResponse
import com.astranavi.app.data.model.ProfileResponse
import com.astranavi.app.data.model.WeeklyForecastResponse
import com.astranavi.app.data.model.YearlyForecastResponse
import com.astranavi.app.util.LocaleManager
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

class DashboardRepository(
    private val apiService: ApiService,
    private val apiCache: ApiResponseCache? = null
) {
    suspend fun getDailyHoroscope(forceRefresh: Boolean = false, metaConsumer: ((CacheMeta) -> Unit)? = null): Response<HoroscopeResponse> {
        val lang = LocaleManager.current()
        android.util.Log.d("DailyHoroscope", "fetch start lang=$lang forceRefresh=$forceRefresh")
        val response = apiCache?.getOrFetch(
            logicalKey = apiCache.dailyHoroscopeKey(lang),
            responseClass = HoroscopeResponse::class.java,
            policy = ApiCachePolicy.UntilNextSixHourMilestone,
            bypassRead = forceRefresh,
            onMeta = { meta ->
                android.util.Log.d("DailyHoroscope", "cache meta fromCache=${meta.fromCache} key=${apiCache.dailyHoroscopeKey(lang)}")
                metaConsumer?.invoke(meta)
            }
        ) {
            android.util.Log.d("DailyHoroscope", "calling network lang=$lang")
            apiService.getDailyHoroscope(lang)
        } ?: run {
            android.util.Log.d("DailyHoroscope", "no cache layer; direct network call lang=$lang")
            apiService.getDailyHoroscope(lang)
        }
        if (response.isSuccessful) {
            val body = response.body()
            android.util.Log.d(
                "DailyHoroscope",
                "success code=${response.code()} bodyPreview=${body?.toString()?.take(400)}"
            )
        } else {
            android.util.Log.w(
                "DailyHoroscope",
                "failure code=${response.code()} message=${response.message()}"
            )
        }
        return response
    }

    suspend fun analyzeFull(request: AnalyzeFullRequest, metaConsumer: ((CacheMeta) -> Unit)? = null): Response<AnalyzeFullWrapper> {
        return apiCache?.getOrFetch(
            logicalKey = apiCache.kundliKey(request),
            responseClass = AnalyzeFullWrapper::class.java,
            policy = ApiCachePolicy.UntilNextLocalMidnight,
            bypassRead = request.force_refresh,
            shouldCache = { it.success && it.astrologyData != null },
            onMeta = metaConsumer
        ) {
            analyzeFullNetwork(request)
        } ?: analyzeFullNetwork(request)
    }

    suspend fun getDailyHoroscopeTimings(
        sign: String? = null,
        name: String? = null,
        lang: String? = null,
        forceRefresh: Boolean = false,
        metaConsumer: ((CacheMeta) -> Unit)? = null
    ): Response<DailyHoroscopeTimingsResponse> {
        val effectiveLang = lang ?: LocaleManager.current()
        val isPersonalized = (sign == null && name == null)
        return if (isPersonalized) {
            apiCache?.getOrFetch(
                logicalKey = apiCache.dailyHoroscopeTimingsKey(effectiveLang),
                responseClass = DailyHoroscopeTimingsResponse::class.java,
                policy = ApiCachePolicy.UntilNextLocalHour,
                bypassRead = forceRefresh,
                onMeta = metaConsumer
            ) {
                apiService.getDailyHoroscopeTimings(sign, name, effectiveLang)
            } ?: apiService.getDailyHoroscopeTimings(sign, name, effectiveLang)
        } else {
            apiService.getDailyHoroscopeTimings(sign, name, effectiveLang)
        }
    }

    suspend fun getWeeklyForecast(area: String, date: String? = null, forceRefresh: Boolean = false, metaConsumer: ((CacheMeta) -> Unit)? = null): Response<WeeklyForecastResponse> {
        val lang = LocaleManager.current()
        return apiCache?.getOrFetch(
            logicalKey = apiCache.forecastPeriodKey(area, "weekly", date, lang),
            responseClass = WeeklyForecastResponse::class.java,
            policy = ApiCachePolicy.UntilNextLocalMidnight,
            bypassRead = forceRefresh,
            onMeta = metaConsumer
        ) {
            apiService.getWeeklyForecast(area, date = date, lang = lang)
        } ?: apiService.getWeeklyForecast(area, date = date, lang = lang)
    }

    suspend fun getMonthlyForecast(area: String, month: String? = null, forceRefresh: Boolean = false, metaConsumer: ((CacheMeta) -> Unit)? = null): Response<MonthlyForecastResponse> {
        val lang = LocaleManager.current()
        return apiCache?.getOrFetch(
            logicalKey = apiCache.forecastPeriodKey(area, "monthly", month, lang),
            responseClass = MonthlyForecastResponse::class.java,
            policy = ApiCachePolicy.UntilNextLocalMidnight,
            bypassRead = forceRefresh,
            onMeta = metaConsumer
        ) {
            apiService.getMonthlyForecast(area, month = month, lang = lang)
        } ?: apiService.getMonthlyForecast(area, month = month, lang = lang)
    }

    suspend fun getYearlyForecast(area: String, year: Int? = null, forceRefresh: Boolean = false, metaConsumer: ((CacheMeta) -> Unit)? = null): Response<YearlyForecastResponse> {
        val lang = LocaleManager.current()
        return apiCache?.getOrFetch(
            logicalKey = apiCache.forecastPeriodKey(area, "yearly", year?.toString(), lang),
            responseClass = YearlyForecastResponse::class.java,
            policy = ApiCachePolicy.UntilNextLocalMidnight,
            bypassRead = forceRefresh,
            onMeta = metaConsumer
        ) {
            apiService.getYearlyForecast(area, year = year, lang = lang)
        } ?: apiService.getYearlyForecast(area, year = year, lang = lang)
    }

    private suspend fun analyzeFullNetwork(request: AnalyzeFullRequest): Response<AnalyzeFullWrapper> {
        val response = apiService.analyzeFull(request)
        val body = response.body()
        return if (response.isSuccessful && body != null) {
            Response.success(com.astranavi.app.data.api.AnalyzeFullJsonAdapter.decode(body.string()))
        } else {
            Response.error(
                response.code(),
                response.errorBody() ?: "".toResponseBody(null)
            )
        }
    }
}
