package com.astranavi.app.data.cache

import com.astranavi.app.data.model.AnalyzeFullRequest
import com.astranavi.app.util.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

sealed class ApiCachePolicy {
    data class ForDuration(val millis: Long) : ApiCachePolicy()
    object UntilNextLocalHour : ApiCachePolicy()
    object UntilNextLocalMidnight : ApiCachePolicy()
}

private data class StoredApiResponse(
    val cachedAtMillis: Long,
    val expiresAtMillis: Long,
    val payload: String
)

class ApiResponseCache(
    private val sessionManager: SessionManager,
    private val gson: Gson = Gson()
) {
    companion object {
        const val PROFILE_TTL_MILLIS = 5 * 60 * 1000L
    }

    suspend fun <T : Any> getOrFetch(
        logicalKey: String,
        responseClass: Class<T>,
        policy: ApiCachePolicy,
        bypassRead: Boolean = false,
        shouldCache: (T) -> Boolean = { true },
        fetch: suspend () -> Response<T>
    ): Response<T> {
        val scopedKey = scopedKey(logicalKey)

        if (!bypassRead) {
            readValid(scopedKey, responseClass)?.let { cachedBody ->
                return Response.success(cachedBody)
            }
        }

        val response = fetch()
        val body = response.body()
        if (response.isSuccessful && body != null && shouldCache(body)) {
            put(scopedKey, body, policy)
        }
        return response
    }

    fun profileKey(): String = "profile"

    fun dailyHoroscopeKey(sign: String?, lang: String = "English"): String {
        return "daily-horoscope:sign:${keyPart(sign)}:lang:${keyPart(lang)}"
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

    suspend fun kundliKey(request: AnalyzeFullRequest): String {
        return "kundli:birth:${birthFingerprint()}:context:${keyPart(request.chart_context)}"
    }

    suspend fun invalidateCurrentUserProfileAndAstrology() {
        val prefix = currentUserPrefix()
        listOf(
            "profile",
            "daily-horoscope",
            "general-horoscope",
            "forecast",
            "kundli"
        ).forEach { section ->
            sessionManager.removeApiCacheEntriesStartingWith(prefix + section)
        }
    }

    suspend fun clearAll() {
        sessionManager.clearApiCache()
    }

    private suspend fun <T : Any> readValid(scopedKey: String, responseClass: Class<T>): T? {
        val raw = sessionManager.getApiCacheEntry(scopedKey) ?: return null
        val stored = try {
            gson.fromJson(raw, StoredApiResponse::class.java)
        } catch (e: Exception) {
            sessionManager.removeApiCacheEntry(scopedKey)
            return null
        } ?: return null

        if (System.currentTimeMillis() >= stored.expiresAtMillis) {
            sessionManager.removeApiCacheEntry(scopedKey)
            return null
        }

        return try {
            gson.fromJson(stored.payload, responseClass)
        } catch (e: Exception) {
            sessionManager.removeApiCacheEntry(scopedKey)
            null
        }
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
            payload = gson.toJson(body)
        )
        sessionManager.putApiCacheEntry(scopedKey, gson.toJson(stored))
    }

    private suspend fun scopedKey(logicalKey: String): String {
        return currentUserPrefix() + logicalKey
    }

    private suspend fun currentUserPrefix(): String {
        val userId = sessionManager.userId.first()
        val email = sessionManager.userEmail.first()
        return "user:${keyPart(userId ?: email ?: "anonymous")}:"
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
        }
    }

    private fun nextLocalHourMillis(nowMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.HOUR_OF_DAY, 1)
        }.timeInMillis
    }

    private fun nextLocalMidnightMillis(nowMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
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
