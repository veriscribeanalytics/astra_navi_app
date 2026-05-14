package com.astranavi.app.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class BalanceResponse(
    val credits: Int = 0,
    val tier: String = "free",
    @SerializedName("monthly_credits") val monthlyCredits: Int? = null,
    @SerializedName("credits_used") val creditsUsed: Int? = null,
    @SerializedName("active_packs_count") val activePacksCount: Int? = null,
    @SerializedName("monthly_reset_date") val monthlyResetDate: String? = null
)

@Serializable
data class SubscriptionDetail(
    @SerializedName("subscription_id") val subscriptionId: String? = null,
    @SerializedName("product_id") val productId: String? = null,
    val tier: String? = null,
    val status: String? = null,
    @SerializedName("current_period_start") val currentPeriodStart: String? = null,
    @SerializedName("current_period_end") val currentPeriodEnd: String? = null,
    @SerializedName("credits_per_month") val creditsPerMonth: Int? = null,
    @SerializedName("credits_remaining") val creditsRemaining: Int? = null
)

@Serializable
data class SubscriptionResponse(
    val subscription: SubscriptionDetail? = null,
    val message: String? = null
)

@Serializable
data class PackDetail(
    @SerializedName("pack_id") val packId: String? = null,
    @SerializedName("product_id") val productId: String? = null,
    val name: String? = null,
    @SerializedName("name_hi") val nameHi: String? = null,
    @SerializedName("credits_total") val creditsTotal: Int? = null,
    @SerializedName("credits_remaining") val creditsRemaining: Int? = null,
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("purchased_at") val purchasedAt: String? = null,
    val status: String? = null
)

@Serializable
data class PacksListResponse(
    val packs: List<PackDetail>? = null,
    val message: String? = null
)

@Serializable
data class UsageLedgerEntry(
    val id: String? = null,
    val action: String? = null,
    @SerializedName("feature_key") val featureKey: String? = null,
    @SerializedName("credits_delta") val creditsDelta: Int? = null,
    @SerializedName("balance_after") val balanceAfter: Int? = null,
    val timestamp: String? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class UsageHistoryResponse(
    val entries: List<UsageLedgerEntry>? = null,
    val total: Int? = null,
    val offset: Int? = null,
    val limit: Int? = null
)

@Serializable
data class PaywallProduct(
    val productId: String? = null,
    val productType: String? = null,
    val nameEn: String? = null,
    val nameHi: String? = null,
    val credits: Int? = null,
    val tier: String? = null,
    val priceInr: Double? = null,
    val priceUsd: Double? = null,
    val currency: String? = null,
    val icon: String? = null,
    val color: String? = null
)

@Serializable
data class CatalogResponse(
    val products: List<PaywallProduct>? = null,
    val message: String? = null
)

@Serializable
data class PaywallCardData(
    val featureKey: String? = null,
    val title: String? = null,
    val titleHi: String? = null,
    val description: String? = null,
    val descriptionHi: String? = null,
    val icon: String? = null,
    val isSoft: Boolean = false,
    val color: String? = null,
    val badge: String? = null,
    val suggestedProducts: List<PaywallProduct>? = null
)

@Serializable
data class PaywallCheckResponse(
    val accessible: Boolean = false,
    @SerializedName("feature_key") val featureKey: String? = null,
    val reason: String? = null,
    @SerializedName("current_tier") val currentTier: String? = null,
    @SerializedName("min_tier") val minTier: String? = null,
    @SerializedName("required_credits") val requiredCredits: Int? = null,
    @SerializedName("available_credits") val availableCredits: Int? = null,
    val paywall: PaywallCardData? = null
)

@Serializable
data class PaywallFeaturesResponse(
    val features: List<PaywallCheckResponse>? = null
)

@Serializable
data class ConsumeRequest(
    val action: String,
    @SerializedName("idempotency_key") val idempotencyKey: String? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class ConsumeResponse(
    val success: Boolean? = null,
    @SerializedName("credits_consumed") val creditsConsumed: Int? = null,
    @SerializedName("balance_after") val balanceAfter: Int? = null,
    val message: String? = null
)

@Serializable
data class LockedContent(
    val locked: Boolean = true,
    val message: String? = null
)

@Serializable
data class PaywallErrorResponse(
    val error: String? = null,
    val featureKey: String? = null,
    val reason: String? = null,
    val requiredCredits: Int? = null,
    val availableCredits: Int? = null,
    val minTier: String? = null,
    val isSoft: Boolean? = null,
    val message: String? = null,
    val paywall: PaywallCardData? = null
)