package com.astranavi.app.ui.entitlement

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.model.BalanceResponse
import com.astranavi.app.data.model.PaywallProduct
import com.astranavi.app.data.model.SubscriptionDetail
import com.astranavi.app.data.model.PackDetail
import com.astranavi.app.data.model.UsageLedgerEntry
import com.astranavi.app.data.repository.EntitlementRepository
import kotlinx.coroutines.launch

sealed class PlansUiState {
    object Loading : PlansUiState()
    data class Success(
        val balance: BalanceResponse,
        val subscription: SubscriptionDetail?,
        val packs: List<PackDetail>,
        val catalog: List<PaywallProduct>,
        val usageHistory: List<UsageLedgerEntry>
    ) : PlansUiState()
    data class Error(val message: String) : PlansUiState()
}

class PlansViewModel(
    private val repository: EntitlementRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<PlansUiState>(PlansUiState.Loading)
    val uiState: State<PlansUiState> = _uiState

    init {
        loadPlansData()
    }

    fun loadPlansData() {
        _uiState.value = PlansUiState.Loading
        viewModelScope.launch {
            try {
                val balanceRes = repository.getBalance()
                val subscriptionRes = repository.getSubscription()
                val packsRes = repository.getPacks()
                val catalogRes = repository.getCatalog()
                val historyRes = repository.getUsageHistory(limit = 50)

                _uiState.value = PlansUiState.Success(
                    balance = balanceRes.body() ?: BalanceResponse(),
                    subscription = subscriptionRes.body()?.subscription,
                    packs = packsRes.body()?.packs ?: emptyList(),
                    catalog = catalogRes.body()?.products ?: emptyList(),
                    usageHistory = historyRes.body()?.entries ?: emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = PlansUiState.Error("Failed to load plans data")
            }
        }
    }

    fun loadUsageHistory(action: String? = null) {
        viewModelScope.launch {
            try {
                val historyRes = repository.getUsageHistory(action = action, limit = 50)
                val current = _uiState.value
                if (current is PlansUiState.Success) {
                    _uiState.value = current.copy(usageHistory = historyRes.body()?.entries ?: emptyList())
                }
            } catch (_: Exception) {
            }
        }
    }
}