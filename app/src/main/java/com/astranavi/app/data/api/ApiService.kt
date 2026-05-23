package com.astranavi.app.data.api

import com.astranavi.app.data.model.*
import com.astranavi.app.data.model.BalanceResponse
import com.astranavi.app.data.model.CatalogResponse
import com.astranavi.app.data.model.ConsumeRequest
import com.astranavi.app.data.model.ConsumeResponse
import com.astranavi.app.data.model.PacksListResponse
import com.astranavi.app.data.model.PaywallCheckResponse
import com.astranavi.app.data.model.PaywallFeaturesResponse
import com.astranavi.app.data.model.SubscriptionResponse
import com.astranavi.app.data.model.UsageHistoryResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<okhttp3.ResponseBody>

    @GET("api/auth/me")
    suspend fun authMe(): Response<Unit>

    // Personalized Horoscope (Home Dashboard)
    @GET("api/daily-horoscope")
    suspend fun getDailyHoroscope(
        @Query("lang") lang: String = "en"
    ): Response<HoroscopeResponse>

    // General Horoscope (Rashi Page)
    @GET("api/horoscope/{sign}")
    suspend fun getGeneralHoroscope(
        @Path("sign") sign: String
    ): Response<HoroscopeResponse>

    @GET("api/forecast/{area}")
    suspend fun getForecast(
        @Path("area") area: String,
        @Query("days_back") daysBack: Int? = null,
        @Query("days_forward") daysForward: Int? = null,
        @Query("lang") lang: String? = null
    ): Response<ForecastResponse>

    @GET("api/forecast/{area}/weekly")
    suspend fun getWeeklyForecast(
        @Path("area") area: String,
        @Query("date") date: String? = null,
        @Query("lang") lang: String? = null,
        @Query("chart_context") chartContext: String? = null
    ): Response<WeeklyForecastResponse>

    @GET("api/forecast/{area}/monthly")
    suspend fun getMonthlyForecast(
        @Path("area") area: String,
        @Query("month") month: String? = null,
        @Query("lang") lang: String? = null,
        @Query("chart_context") chartContext: String? = null
    ): Response<MonthlyForecastResponse>

    @GET("api/forecast/{area}/yearly")
    suspend fun getYearlyForecast(
        @Path("area") area: String,
        @Query("year") year: Int? = null,
        @Query("lang") lang: String? = null,
        @Query("chart_context") chartContext: String? = null
    ): Response<YearlyForecastResponse>

    // Kundli / Full Analysis
    @POST("api/analyze-full")
    suspend fun analyzeFull(
        @Body request: AnalyzeFullRequest
    ): Response<okhttp3.ResponseBody>

    // Match Making
    @POST("api/match")
    suspend fun calculateMatch(
        @Query("narrative") narrative: Boolean = true,
        @Body request: MatchRequest
    ): Response<MatchResponse>

    @GET("api/match/history")
    suspend fun getMatchHistory(
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int = 1
    ): Response<MatchHistoryResponse>

    @GET("api/match/{matchId}")
    suspend fun getMatchResult(
        @Path("matchId") matchId: String
    ): Response<MatchRecord>

    @DELETE("api/match/{matchId}")
    suspend fun deleteMatch(
        @Path("matchId") matchId: String
    ): Response<Map<String, String>>

    // AI Chat
    @GET("api/chats")
    suspend fun listChats(
        @Query("limit") limit: Int = 50,
        @Query("cursor") cursor: String? = null
    ): Response<ChatHistoryWrapper>

    @POST("api/chats")
    suspend fun createChat(
        @Body request: ChatCreateRequest
    ): Response<ChatDetailResponse>

    @GET("api/chats/{chatId}")
    suspend fun getChatHistory(
        @Path("chatId") chatId: String
    ): Response<ChatDetailResponse>

    @DELETE("api/chats/{chatId}")
    suspend fun deleteChat(
        @Path("chatId") chatId: String
    ): Response<Map<String, String>>

    @POST("api/chats/{chatId}/messages")
    @Streaming
    suspend fun sendMessage(
        @Path("chatId") chatId: String,
        @Body request: ChatRequest
    ): okhttp3.ResponseBody

    @GET("api/chat/avatars")
    suspend fun getChatAvatars(): Response<ChatAvatarCatalog>

    @PUT("api/chats/{chatId}/messages/{msgId}/rate")
    suspend fun rateMessage(
        @Path("chatId") chatId: String,
        @Path("msgId") msgId: String,
        @Body request: RatingRequest
    ): Response<Map<String, String>>

    // Guided Consultation
    @GET("api/consult/tree")
    suspend fun getConsultTree(
        @Query("age") age: Int,
        @Query("lang") lang: String? = null,
        @Query("gender") gender: String? = null,
        @Query("marital_status") maritalStatus: String? = null,
        @Query("occupation") occupation: String? = null
    ): Response<ConsultTreeWrapper>

    @POST("api/consult")
    @Streaming
    suspend fun generateConsultation(
        @Body request: ConsultRequest
    ): okhttp3.ResponseBody

    @GET("api/consult/history")
    suspend fun getConsultHistory(
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int = 1
    ): Response<ConsultHistoryResponse>

    // Profile
    @GET("api/user/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PUT("api/user/profile")
    suspend fun updateProfile(
        @Body request: ProfileUpdateRequest
    ): Response<ProfileResponse>

    @DELETE("api/user")
    suspend fun deleteUser(): Response<Map<String, String>>

    @GET("api/locations/search")
    suspend fun searchLocations(
        @Query("q") query: String
    ): Response<LocationSearchResponse>

    // Entitlements
    @GET("api/entitlements/balance")
    suspend fun getBalance(): Response<BalanceResponse>

    @GET("api/entitlements/subscription")
    suspend fun getSubscription(): Response<SubscriptionResponse>

    @GET("api/entitlements/packs")
    suspend fun getPacks(): Response<PacksListResponse>

    @GET("api/entitlements/history")
    suspend fun getUsageHistory(
        @Query("action") action: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<UsageHistoryResponse>

    @POST("api/entitlements/consume")
    suspend fun consumeAction(@Body request: ConsumeRequest): Response<ConsumeResponse>

    @GET("api/entitlements/catalog")
    suspend fun getCatalog(
        @Query("product_type") productType: String? = null,
        @Query("lang") lang: String? = null
    ): Response<CatalogResponse>

    // Paywall
    @GET("api/entitlements/paywall")
    suspend fun checkPaywall(
        @Query("feature") feature: String
    ): Response<PaywallCheckResponse>

    @GET("api/entitlements/paywall/features")
    suspend fun checkAllPaywallFeatures(): Response<PaywallFeaturesResponse>
}
