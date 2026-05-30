package com.astranavi.app.data.api

import com.astranavi.app.BuildConfig
import com.astranavi.app.data.model.PaywallCardData
import com.astranavi.app.data.model.RefreshTokenRequest
import com.astranavi.app.data.model.RefreshTokenResponse
import com.astranavi.app.util.LocaleManager
import com.astranavi.app.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.veriscribeanalytics.com/"
    private val API_KEY = BuildConfig.API_KEY

    private var sessionManager: SessionManager? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedRefreshToken: String? = null

    val paywallEvent = MutableStateFlow<PaywallCardData?>(null)

    fun init(manager: SessionManager) {
        sessionManager = manager
        scope.launch {
            manager.accessToken.collectLatest { token ->
                cachedAccessToken = token
            }
        }
        scope.launch {
            manager.refreshToken.collectLatest { token ->
                cachedRefreshToken = token
            }
        }
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
            .addHeader("X-API-Key", API_KEY)
            .addHeader("Accept-Language", LocaleManager.currentLanguageTag())

        val token = cachedAccessToken
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        chain.proceed(requestBuilder.build())
    }

    private val tokenAuthenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            if (response.code != 401) return null

            // Bound retries: if we have already retried this request once, give up
            // rather than looping forever on a token the server keeps rejecting.
            if (responseCount(response) >= 2) {
                cachedAccessToken = null
                cachedRefreshToken = null
                try {
                    kotlinx.coroutines.runBlocking { sessionManager?.clearSession() }
                } catch (_: Exception) { }
                return null
            }

            val refreshToken = cachedRefreshToken
            if (refreshToken.isNullOrEmpty()) return null

            synchronized(this) {
                val latestToken = cachedAccessToken
                val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

                if (latestToken != requestToken) {
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $latestToken")
                        .build()
                }

                val refreshSuccess = try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                        .build()
                    val body = JsonConfig.json
                        .encodeToString(RefreshTokenRequest(refreshToken))
                        .toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url("${BASE_URL}api/auth/refresh")
                        .post(body)
                        .addHeader("X-API-Key", API_KEY)
                        .build()

                    client.newCall(request).execute().use { refreshResponse ->
                        if (refreshResponse.isSuccessful) {
                            val responseBody = refreshResponse.body?.string().orEmpty()
                            val responseData = JsonConfig.json.decodeFromString(
                                RefreshTokenResponse.serializer(),
                                responseBody
                            )
                            cachedAccessToken = responseData.accessToken
                            cachedRefreshToken = responseData.refreshToken ?: refreshToken
                            try {
                                kotlinx.coroutines.runBlocking {
                                    sessionManager?.updateTokens(responseData.accessToken, responseData.refreshToken)
                                }
                            } catch (_: Exception) { }
                            true
                        } else {
                            false
                        }
                    }
                } catch (_: Exception) {
                    false
                }

                if (refreshSuccess) {
                    val newToken = cachedAccessToken
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                } else {
                    cachedAccessToken = null
                    cachedRefreshToken = null
                    try {
                        kotlinx.coroutines.runBlocking { sessionManager?.clearSession() }
                    } catch (_: Exception) { }
                }
            }
            return null
        }
    }

    val json = JsonConfig.json

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .authenticator(tokenAuthenticator)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}
