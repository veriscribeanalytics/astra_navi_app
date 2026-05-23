package com.astranavi.app.data.repository

import com.astranavi.app.data.api.ApiService
import com.astranavi.app.data.api.JsonConfig
import com.astranavi.app.data.model.BalanceResponse
import com.astranavi.app.data.model.CatalogResponse
import com.astranavi.app.data.model.ConsumeRequest
import com.astranavi.app.data.model.ConsumeResponse
import com.astranavi.app.data.model.PacksListResponse
import com.astranavi.app.data.model.PaywallCheckResponse
import com.astranavi.app.data.model.PaywallErrorResponse
import com.astranavi.app.data.model.PaywallFeaturesResponse
import com.astranavi.app.data.model.SubscriptionResponse
import com.astranavi.app.data.model.UsageHistoryResponse
import com.astranavi.app.data.model.PaywallCardData
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.JsonObject
import retrofit2.Response

class EntitlementRepository(
    private val apiService: ApiService
) {
    suspend fun getBalance(): Response<BalanceResponse> {
        return apiService.getBalance()
    }

    suspend fun getSubscription(): Response<SubscriptionResponse> {
        return apiService.getSubscription()
    }

    suspend fun getPacks(): Response<PacksListResponse> {
        return apiService.getPacks()
    }

    suspend fun getUsageHistory(
        action: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Response<UsageHistoryResponse> {
        return apiService.getUsageHistory(action, limit, offset)
    }

    suspend fun getCatalog(productType: String? = null, lang: String? = null): Response<CatalogResponse> {
        return apiService.getCatalog(productType, lang)
    }

    suspend fun checkPaywall(feature: String): Response<PaywallCheckResponse> {
        return apiService.checkPaywall(feature)
    }

    suspend fun checkAllPaywallFeatures(): Response<PaywallFeaturesResponse> {
        return apiService.checkAllPaywallFeatures()
    }

    suspend fun consumeAction(request: ConsumeRequest): Response<ConsumeResponse> {
        return apiService.consumeAction(request)
    }

    fun parsePaywallFrom402Body(errorBody: String?): PaywallCardData? {
        if (errorBody == null) return null
        return try {
            val response = JsonConfig.json.decodeFromString(PaywallErrorResponse.serializer(), errorBody)
            response.paywall
        } catch (_: Exception) {
            try {
                val jsonObject = JsonConfig.json.parseToJsonElement(errorBody) as? JsonObject ?: return null
                val detail = jsonObject["detail"] as? JsonObject
                val paywallElement = detail?.get("paywall") ?: jsonObject["paywall"] ?: return null
                JsonConfig.json.decodeFromJsonElement(PaywallCardData.serializer(), paywallElement)
            } catch (_: Exception) {
                null
            }
        }
    }
}
