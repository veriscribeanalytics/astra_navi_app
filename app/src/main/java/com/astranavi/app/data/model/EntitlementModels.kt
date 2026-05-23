package com.astranavi.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BalanceResponse(
    val credits: Int = 0,
    val tier: String = "free",
    @SerialName("monthly_credits") val monthlyCredits: Int? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null,
    @SerialName("active_packs_count") val activePacksCount: Int? = null,
    @SerialName("monthly_reset_date") val monthlyResetDate: String? = null,
    @SerialName("total_credits_remaining") val totalCreditsRemaining: Int = 0,
    @SerialName("subscription_credits_remaining") val subscriptionCreditsRemaining: Int = 0,
    @SerialName("pack_credits_remaining") val packCreditsRemaining: Int = 0,
    @SerialName("subscription_cycle_end") val subscriptionCycleEnd: String? = null
)

@Serializable
data class SubscriptionDetail(
    @SerialName("subscription_id") val subscriptionId: String? = null,
    @SerialName("product_id") val productId: String? = null,
    val tier: String? = null,
    val status: String? = null,
    @SerialName("current_period_start") val currentPeriodStart: String? = null,
    @SerialName("current_period_end") val currentPeriodEnd: String? = null,
    @SerialName("credits_per_month") val creditsPerMonth: Int? = null,
    @SerialName("credits_remaining") val creditsRemaining: Int? = null
)

@Serializable
data class SubscriptionResponse(
    val subscription: SubscriptionDetail? = null,
    val message: String? = null
)

@Serializable
data class PackDetail(
    @SerialName("pack_id") val packId: String? = null,
    @SerialName("product_id") val productId: String? = null,
    val name: String? = null,
    @SerialName("name_hi") val nameHi: String? = null,
    @SerialName("credits_total") val creditsTotal: Int? = null,
    @SerialName("credits_remaining") val creditsRemaining: Int? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("purchased_at") val purchasedAt: String? = null,
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
    @SerialName("feature_key") val featureKey: String? = null,
    @SerialName("credits_delta") val credits_delta: Int? = null,
    val creditsDelta: Int? = null,
    @SerialName("balance_after") val balance_after: Int? = null,
    val balanceAfter: Int? = null,
    val timestamp: String? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("created_at") val createdAtAlt: String? = null,
    val metadata: JsonElement? = null
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
    @SerialName("product_id") val productId: String? = null,
    @SerialName("product_type") val productType: String? = null,
    val name: String? = null,
    val description: String? = null,
    @SerialName("name_en") val nameEn: String? = null,
    @SerialName("name_hi") val nameHi: String? = null,
    @SerialName("description_en") val descriptionEn: String? = null,
    @SerialName("description_hi") val descriptionHi: String? = null,
    val credits: Int? = null,
    val tier: String? = null,
    val category: String? = null,
    @SerialName("price_inr") val priceInr: Double? = null,
    @SerialName("price_usd") val priceUsd: Double? = null,
    val currency: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val metadata: JsonElement? = null
)

@Serializable
data class CatalogResponse(
    val products: List<PaywallProduct>? = null,
    val message: String? = null
)

@Serializable
data class PaywallCardData(
    @SerialName("feature_key") val featureKey: String? = null,
    val title: String? = null,
    @SerialName("title_hi") val titleHi: String? = null,
    val description: String? = null,
    @SerialName("description_hi") val descriptionHi: String? = null,
    val icon: String? = null,
    @SerialName("is_soft") val isSoft: Boolean = false,
    val color: String? = null,
    val badge: String? = null,
    @SerialName("suggested_products") val suggestedProducts: List<PaywallProduct>? = null
)

@Serializable
data class PaywallCheckResponse(
    val accessible: Boolean = false,
    @SerialName("feature_key") val featureKey: String? = null,
    val reason: String? = null,
    @SerialName("current_tier") val currentTier: String? = null,
    @SerialName("min_tier") val minTier: String? = null,
    @SerialName("required_credits") val requiredCredits: Int? = null,
    @SerialName("available_credits") val availableCredits: Int? = null,
    val paywall: PaywallCardData? = null
)

@Serializable
data class PaywallFeaturesResponse(
    val features: List<PaywallCheckResponse>? = null
)

@Serializable
data class ConsumeRequest(
    val action: String,
    @SerialName("idempotency_key") val idempotencyKey: String? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class ConsumeResponse(
    val success: Boolean? = null,
    @SerialName("credits_consumed") val creditsConsumed: Int? = null,
    @SerialName("balance_after") val balanceAfter: Int? = null,
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
    @SerialName("feature_key") val featureKey: String? = null,
    val reason: String? = null,
    @SerialName("required_credits") val requiredCredits: Int? = null,
    @SerialName("available_credits") val availableCredits: Int? = null,
    @SerialName("min_tier") val minTier: String? = null,
    @SerialName("is_soft") val isSoft: Boolean? = null,
    val message: String? = null,
    val paywall: PaywallCardData? = null
)

fun PackDetail.getLocalizedName(): String {
    val lang = com.astranavi.app.util.LocaleManager.current()
    return if (lang == "hi" && !nameHi.isNullOrEmpty()) nameHi else name ?: ""
}

fun PaywallProduct.getLocalizedName(): String {
    val lang = com.astranavi.app.util.LocaleManager.current()
    return when (lang) {
        "hi" -> nameHi ?: nameEn ?: name ?: ""
        else -> nameEn ?: name ?: ""
    }
}

fun PaywallProduct.getLocalizedDescription(): String {
    val lang = com.astranavi.app.util.LocaleManager.current()
    return when (lang) {
        "hi" -> descriptionHi ?: descriptionEn ?: description ?: ""
        else -> descriptionEn ?: description ?: ""
    }
}

fun PaywallCardData.getLocalizedTitle(): String {
    val lang = com.astranavi.app.util.LocaleManager.current()
    return if (lang == "hi" && !titleHi.isNullOrEmpty()) titleHi else title ?: ""
}

fun PaywallCardData.getLocalizedDescription(): String {
    val lang = com.astranavi.app.util.LocaleManager.current()
    return if (lang == "hi" && !descriptionHi.isNullOrEmpty()) descriptionHi else description ?: ""
}
