package com.astranavi.app.data.repository

import com.astranavi.app.data.api.ApiService
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
import com.google.gson.Gson
import retrofit2.Response

class EntitlementRepository(
    private val apiService: ApiService,
    private val gson: Gson = Gson()
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

    suspend fun getCatalog(productType: String? = null): Response<CatalogResponse> {
        return apiService.getCatalog(productType)
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
            val response = gson.fromJson(errorBody, PaywallErrorResponse::class.java)
            response.paywall
        } catch (_: Exception) {
            try {
                val json = com.google.gson.JsonParser.parseString(errorBody)
                if (json.isJsonObject) {
                    val paywallElement = json.asJsonObject.get("detail")
                    if (paywallElement != null && paywallElement.isJsonObject) {
                        gson.fromJson(paywallElement.asJsonObject.get("paywall"), PaywallCardData::class.java)
                    } else {
                        gson.fromJson(json.asJsonObject.get("paywall"), PaywallCardData::class.java)
                    }
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }
}