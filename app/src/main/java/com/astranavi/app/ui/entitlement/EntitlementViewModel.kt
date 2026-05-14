package com.astranavi.app.ui.entitlement

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.model.BalanceResponse
import com.astranavi.app.data.model.PaywallCardData
import com.astranavi.app.data.model.PaywallCheckResponse
import com.astranavi.app.data.model.PackDetail
import com.astranavi.app.data.model.SubscriptionDetail
import com.astranavi.app.data.repository.EntitlementRepository
import kotlinx.coroutines.launch

sealed class EntitlementUiState {
    object Loading : EntitlementUiState()
    data class Success(
        val balance: BalanceResponse,
        val subscription: SubscriptionDetail?,
        val packs: List<PackDetail>,
        val paywallStates: Map<String, PaywallCheckResponse>
    ) : EntitlementUiState()
    data class Error(val message: String) : EntitlementUiState()
}

class EntitlementViewModel(
    private val repository: EntitlementRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<EntitlementUiState>(EntitlementUiState.Loading)
    val uiState: State<EntitlementUiState> = _uiState

    private val _activePaywall = mutableStateOf<PaywallCardData?>(null)
    val activePaywall: State<PaywallCardData?> = _activePaywall

    init {
        refreshAll()
    }

    fun refreshBalance() {
        viewModelScope.launch {
            try {
                val response = repository.getBalance()
                if (response.isSuccessful && response.body() != null) {
                    val balance = response.body()!!
                    val current = _uiState.value
                    if (current is EntitlementUiState.Success) {
                        _uiState.value = current.copy(balance = balance)
                    } else {
                        _uiState.value = EntitlementUiState.Success(
                            balance = balance,
                            subscription = null,
                            packs = emptyList(),
                            paywallStates = emptyMap()
                        )
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun refreshAll() {
        _uiState.value = EntitlementUiState.Loading
        viewModelScope.launch {
            try {
                val balanceRes = repository.getBalance()
                val subscriptionRes = repository.getSubscription()
                val packsRes = repository.getPacks()
                val featuresRes = repository.checkAllPaywallFeatures()

                val balance = balanceRes.body() ?: BalanceResponse()
                val subscription = subscriptionRes.body()?.subscription
                val packs = packsRes.body()?.packs ?: emptyList()
                val paywallStates = featuresRes.body()?.features?.associateBy { it.featureKey ?: "" } ?: emptyMap()

                _uiState.value = EntitlementUiState.Success(
                    balance = balance,
                    subscription = subscription,
                    packs = packs,
                    paywallStates = paywallStates
                )
            } catch (e: Exception) {
                _uiState.value = EntitlementUiState.Error("Failed to load entitlement data")
            }
        }
    }

    fun checkFeature(featureKey: String) {
        viewModelScope.launch {
            try {
                val response = repository.checkPaywall(featureKey)
                if (response.isSuccessful && response.body() != null) {
                    val check = response.body()!!
                    val current = _uiState.value
                    if (current is EntitlementUiState.Success) {
                        val updatedStates = current.paywallStates + (featureKey to check)
                        _uiState.value = current.copy(paywallStates = updatedStates)
                    }
                    if (!check.accessible && check.paywall != null) {
                        _activePaywall.value = check.paywall
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun showPaywall(paywall: PaywallCardData) {
        _activePaywall.value = paywall
    }

    fun dismissPaywall() {
        _activePaywall.value = null
    }

    fun handle402Error(errorBody: String?) {
        val paywall = repository.parsePaywallFrom402Body(errorBody)
        if (paywall != null) {
            _activePaywall.value = paywall
            refreshBalance()
        }
    }

    fun isFeatureAccessible(featureKey: String): Boolean {
        val current = _uiState.value
        if (current is EntitlementUiState.Success) {
            val check = current.paywallStates[featureKey]
            return check?.accessible ?: true
        }
        return true
    }

    fun getCurrentCredits(): Int {
        val current = _uiState.value
        return if (current is EntitlementUiState.Success) current.balance.credits else 0
    }

    fun getCurrentTier(): String {
        val current = _uiState.value
        return if (current is EntitlementUiState.Success) current.balance.tier else "free"
    }
}