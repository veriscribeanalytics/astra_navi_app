package com.astranavi.app.data.cache

import com.astranavi.app.data.api.JsonConfig
import com.astranavi.app.data.model.AnalyzeFullRequest
import com.astranavi.app.util.SessionManager
import com.astranavi.app.util.nextLocalHourMillis
import com.astranavi.app.util.nextLocalMidnightMillis
import kotlinx.coroutines.flow.first
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import retrofit2.Response
import java.util.Locale

sealed class ApiCachePolicy {
    data class ForDuration(val millis: Long) : ApiCachePolicy()
    object UntilNextLocalHour : ApiCachePolicy()
    object UntilNextLocalMidnight : ApiCachePolicy()
    object UntilNextSixHourMilestone : ApiCachePolicy()
}

data class CacheMeta(
    val cachedAtMillis: Long,
    val expiresAtMillis: Long,
    val fromCache: Boolean
) {
    companion object {
        /**
         * Combine multiple per-section meta values into one screen-level meta.
         * Uses the oldest cachedAt + soonest expiresAt, and marks fromCache=true
         * if any of the contributing sections was served from cache.
         */
        fun combine(metas: List<CacheMeta?>): CacheMeta? {
            val present = metas.filterNotNull()
            if (present.isEmpty()) return null
            return CacheMeta(
                cachedAtMillis = present.minOf { it.cachedAtMillis },
                expiresAtMillis = present.minOf { it.expiresAtMillis },
                fromCache = present.any { it.fromCache }
            )
        }
    }
}

@Serializable
private data class StoredApiResponse(
    val cachedAtMillis: Long,
    val expiresAtMillis: Long,
    val payload: String
)

class ApiResponseCache(
    private val sessionManager: SessionManager
) {
    companion object {
        private const val CACHE_VERSION_PREFIX = "v2:"
    }

    suspend fun <T : Any> getOrFetch(
        logicalKey: String,
        responseClass: Class<T>,
        policy: ApiCachePolicy,
        bypassRead: Boolean = false,
        shouldCache: (T) -> Boolean = { true },
        onMeta: ((CacheMeta) -> Unit)? = null,
        fetch: suspend () -> Response<T>
    ): Response<T> {
        val scopedKey = scopedKey(logicalKey)

        if (!bypassRead) {
            readValidWithMeta(scopedKey, responseClass)?.let { (cachedBody, cachedAt, expiresAt) ->
                onMeta?.invoke(CacheMeta(cachedAt, expiresAt, fromCache = true))
                return Response.success(cachedBody)
            }
        }

        val response = fetch()
        val body = response.body()
        if (response.isSuccessful && body != null && shouldCache(body)) {
            val now = System.currentTimeMillis()
            val expiresAt = expiresAtMillis(policy, now)
            put(scopedKey, body, policy)
            onMeta?.invoke(CacheMeta(now, expiresAt, fromCache = false))
        }
        return response
    }

    fun profileKey(): String = "profile"

    fun dailyHoroscopeKey(lang: String = "en"): String {
        return "daily-horoscope:lang:${keyPart(lang)}"
    }

    fun dailyHoroscopeTimingsKey(lang: String = "en"): String {
        return "daily-horoscope-timings:lang:${keyPart(lang)}"
    }

    fun generalHoroscopeKey(sign: String): String {
        return "general-horoscope:sign:${keyPart(sign)}"
    }

    suspend fun forecastKey(
        area: String,
        daysBack: Int?,
        daysForward: Int?,
        lang: String? = null
    ): String {
        return "forecast:area:${keyPart(area)}:back:${keyPart(daysBack?.toString())}" +
            ":forward:${keyPart(daysForward?.toString())}:lang:${keyPart(lang)}:birth:${birthFingerprint()}"
    }

    suspend fun forecastPeriodKey(
        area: String,
        period: String,
        anchor: String?,
        lang: String? = null
    ): String {
        return "forecast-period:area:${keyPart(area)}:period:${keyPart(period)}" +
            ":anchor:${keyPart(anchor)}:lang:${keyPart(lang)}:birth:${birthFingerprint()}"
    }

    suspend fun kundliKey(request: AnalyzeFullRequest): String {
        return "kundli:birth:${birthFingerprint()}:context:${keyPart(request.chart_context)}"
    }

    suspend fun invalidateCurrentUserProfileAndAstrology() {
        val prefix = currentUserPrefix()
        listOf(
            "profile",
            "daily-horoscope",
            "daily-horoscope-timings",
            "general-horoscope",
            "forecast",
            "forecast-period",
            "kundli"
        ).forEach { section ->
            sessionManager.removeApiCacheEntriesStartingWith(prefix + section)
        }
    }

    suspend fun clearAll() {
        sessionManager.clearApiCache()
    }

    private suspend fun <T : Any> readValidWithMeta(
        scopedKey: String,
        responseClass: Class<T>
    ): Triple<T, Long, Long>? {
        val raw = sessionManager.getApiCacheEntry(scopedKey) ?: return null
        val stored = try {
            JsonConfig.json.decodeFromString(StoredApiResponse.serializer(), raw)
        } catch (e: Exception) {
            sessionManager.removeApiCacheEntry(scopedKey)
            return null
        }

        if (System.currentTimeMillis() >= stored.expiresAtMillis) {
            sessionManager.removeApiCacheEntry(scopedKey)
            return null
        }

        val body = try {
            JsonConfig.json.decodeFromJsonElement(
                deserializer = serializerFor(responseClass),
                element = JsonConfig.json.parseToJsonElement(stored.payload)
            )
        } catch (e: Exception) {
            sessionManager.removeApiCacheEntry(scopedKey)
            return null
        }
        return Triple(body, stored.cachedAtMillis, stored.expiresAtMillis)
    }

    private suspend fun <T : Any> put(
        scopedKey: String,
        body: T,
        policy: ApiCachePolicy
    ) {
        val now = System.currentTimeMillis()
        val stored = StoredApiResponse(
            cachedAtMillis = now,
            expiresAtMillis = expiresAtMillis(policy, now),
            payload = JsonConfig.json.encodeToString(serializerFor(body.javaClass), body)
        )
        sessionManager.putApiCacheEntry(
            scopedKey,
            JsonConfig.json.encodeToString(StoredApiResponse.serializer(), stored)
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> serializerFor(responseClass: Class<T>): KSerializer<T> {
        return when (responseClass) {
            com.astranavi.app.data.model.AnalyzeFullWrapper::class.java -> com.astranavi.app.data.model.AnalyzeFullWrapper.serializer()
            com.astranavi.app.data.model.ForecastResponse::class.java -> com.astranavi.app.data.model.ForecastResponse.serializer()
            com.astranavi.app.data.model.WeeklyForecastResponse::class.java -> com.astranavi.app.data.model.WeeklyForecastResponse.serializer()
            com.astranavi.app.data.model.MonthlyForecastResponse::class.java -> com.astranavi.app.data.model.MonthlyForecastResponse.serializer()
            com.astranavi.app.data.model.YearlyForecastResponse::class.java -> com.astranavi.app.data.model.YearlyForecastResponse.serializer()
            com.astranavi.app.data.model.HoroscopeResponse::class.java -> com.astranavi.app.data.model.HoroscopeResponse.serializer()
            com.astranavi.app.data.model.DailyHoroscopeTimingsResponse::class.java -> com.astranavi.app.data.model.DailyHoroscopeTimingsResponse.serializer()
            com.astranavi.app.data.model.ProfileResponse::class.java -> com.astranavi.app.data.model.ProfileResponse.serializer()
            else -> error("No cache serializer registered for ${responseClass.name}")
        } as KSerializer<T>
    }

    private suspend fun scopedKey(logicalKey: String): String {
        return currentUserPrefix() + logicalKey
    }

    private suspend fun currentUserPrefix(): String {
        val userId = sessionManager.userId.first()
        val email = sessionManager.userEmail.first()
        return CACHE_VERSION_PREFIX + "user:${keyPart(userId ?: email ?: "anonymous")}:"
    }

    private suspend fun birthFingerprint(): String {
        val dob = sessionManager.userDob.first()
        val tob = sessionManager.userTob.first()
        val pob = sessionManager.userPob.first()
        return "${keyPart(dob)}_${keyPart(tob)}_${keyPart(pob)}"
    }

    private fun expiresAtMillis(policy: ApiCachePolicy, nowMillis: Long): Long {
        return when (policy) {
            is ApiCachePolicy.ForDuration -> nowMillis + policy.millis
            ApiCachePolicy.UntilNextLocalHour -> nextLocalHourMillis(nowMillis)
            ApiCachePolicy.UntilNextLocalMidnight -> nextLocalMidnightMillis(nowMillis)
            ApiCachePolicy.UntilNextSixHourMilestone -> nextSixHourMilestoneMillis(nowMillis)
        }
    }

    private fun nextSixHourMilestoneMillis(nowMillis: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = nowMillis
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val nextMilestoneHour = when {
            currentHour < 6 -> 6
            currentHour < 12 -> 12
            currentHour < 18 -> 18
            else -> 24
        }
        if (nextMilestoneHour == 24) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        } else {
            calendar.set(java.util.Calendar.HOUR_OF_DAY, nextMilestoneHour)
        }
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun keyPart(value: String?): String {
        val normalized = value
            ?.trim()
            ?.lowercase(Locale.US)
            ?.takeIf { it.isNotEmpty() }
            ?: "none"
        return normalized.replace(Regex("[^a-z0-9._-]"), "_")
    }
}
