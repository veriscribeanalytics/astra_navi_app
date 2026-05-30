package com.astranavi.app.data.repository

import com.astranavi.app.data.api.ApiService
import com.astranavi.app.data.cache.ApiCachePolicy
import com.astranavi.app.data.cache.ApiResponseCache
import com.astranavi.app.data.cache.CacheMeta
import com.astranavi.app.data.model.*
import com.astranavi.app.util.LocaleManager
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

class AstrologyRepository(
    private val apiService: ApiService,
    private val apiCache: ApiResponseCache? = null
) {
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

    suspend fun calculateMatch(request: MatchRequest): Response<MatchResponse> {
        return apiService.calculateMatch(true, request)
    }

    suspend fun getMatchHistory(): Response<MatchHistoryResponse> {
        return apiService.getMatchHistory()
    }

    suspend fun getMatchResult(matchId: String): Response<MatchRecord> {
        return apiService.getMatchResult(matchId)
    }

    suspend fun deleteMatch(matchId: String): Response<Map<String, String>> {
        return apiService.deleteMatch(matchId)
    }

    suspend fun listChats(limit: Int = 50, cursor: String? = null): Response<ChatHistoryWrapper> {
        return apiService.listChats(limit, cursor)
    }

    suspend fun createChat(title: String, language: String = "english"): Response<ChatDetailResponse> {
        return apiService.createChat(ChatCreateRequest(title, language))
    }

    suspend fun deleteChat(chatId: String): Response<Map<String, String>> {
        return apiService.deleteChat(chatId)
    }

    suspend fun sendMessage(chatId: String, text: String, language: String = "english", avatarId: String? = null): okhttp3.ResponseBody {
        return apiService.sendMessage(chatId, ChatRequest(text, language, avatarId))
    }

    suspend fun getChatAvatars(): Response<ChatAvatarCatalog> {
        return apiService.getChatAvatars()
    }

    suspend fun getChatHistory(chatId: String): Response<ChatDetailResponse> {
        return apiService.getChatHistory(chatId)
    }

    suspend fun rateMessage(chatId: String, msgId: String, rating: Int, feedbackTags: List<String>? = null, feedbackComment: String? = null): Response<Map<String, String>> {
        return apiService.rateMessage(chatId, msgId, RatingRequest(rating, feedbackTags, feedbackComment))
    }

    suspend fun getConsultTree(
        age: Int,
        lang: String? = null,
        gender: String? = null,
        maritalStatus: String? = null,
        occupation: String? = null
    ): Response<ConsultTreeWrapper> {
        val effectiveLang = lang ?: LocaleManager.current()
        return apiService.getConsultTree(age, effectiveLang, gender, maritalStatus, occupation)
    }

    suspend fun generateConsultation(request: ConsultRequest): okhttp3.ResponseBody {
        return apiService.generateConsultation(request)
    }

    suspend fun getConsultHistory(limit: Int = 10, page: Int = 1): Response<ConsultHistoryResponse> {
        return apiService.getConsultHistory(limit, page)
    }



    suspend fun getGeneralHoroscope(sign: String): Response<HoroscopeResponse> {
        return apiCache?.getOrFetch(
            logicalKey = apiCache.generalHoroscopeKey(sign),
            responseClass = HoroscopeResponse::class.java,
            policy = ApiCachePolicy.UntilNextLocalHour
        ) {
            apiService.getGeneralHoroscope(sign)
        } ?: apiService.getGeneralHoroscope(sign)
    }

    private suspend fun analyzeFullNetwork(request: AnalyzeFullRequest): Response<AnalyzeFullWrapper> {
        val response = apiService.analyzeFull(request)
        val body = response.body()
        return if (response.isSuccessful && body != null) {
            body.use {
                Response.success(com.astranavi.app.data.api.AnalyzeFullJsonAdapter.decode(it.string()))
            }
        } else {
            Response.error(
                response.code(),
                response.errorBody() ?: "".toResponseBody(null)
            )
        }
    }
}
