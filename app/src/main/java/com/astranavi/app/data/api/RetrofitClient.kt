package com.astranavi.app.data.api

import com.astranavi.app.data.model.AnalyzeFullResponse
import com.astranavi.app.data.model.PaywallCardData
import com.astranavi.app.data.model.PaywallErrorResponse
import com.astranavi.app.data.model.RefreshTokenResponse
import com.astranavi.app.util.SessionManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.veriscribeanalytics.com/"
    private val API_KEY = com.astranavi.app.BuildConfig.API_KEY

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
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
            .addHeader("X-API-Key", API_KEY)

        val token = cachedAccessToken
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        chain.proceed(requestBuilder.build())
    }

    private val tokenAuthenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            if (response.code != 401) return null

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
                    val client = OkHttpClient()
                    val body = "{\"refreshToken\":\"$refreshToken\"}".toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url("${BASE_URL}api/auth/refresh")
                        .post(body)
                        .addHeader("X-API-Key", API_KEY)
                        .build()

                    val refreshResponse = client.newCall(request).execute()
                    if (refreshResponse.isSuccessful) {
                        val responseData = gson.fromJson(refreshResponse.body?.string(), RefreshTokenResponse::class.java)
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

    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(AnalyzeFullResponse::class.java, AnalyzeFullDeserializer(AnalyzeFullResponse::class.java))
        .create()

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}