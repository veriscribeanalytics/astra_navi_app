package com.astranavi.app.ui.match

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.model.MatchRequest
import com.astranavi.app.data.model.MatchResponse
import com.astranavi.app.data.model.PaywallCardData
import com.astranavi.app.data.model.PaywallErrorResponse
import com.astranavi.app.data.model.PersonDetail
import com.astranavi.app.data.repository.AstrologyRepository
import com.astranavi.app.data.repository.EntitlementRepository
import com.astranavi.app.util.ErrorSanitizer
import com.astranavi.app.util.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class MatchState {
    object Idle : MatchState()
    object Loading : MatchState()
    data class Success(val data: MatchResponse) : MatchState()
    data class PaywallBlocked(val paywall: PaywallCardData) : MatchState()
    data class Error(val message: String) : MatchState()
}

class MatchViewModel(
    private val repository: AstrologyRepository,
    private val sessionManager: SessionManager,
    private val entitlementRepository: EntitlementRepository? = null
) : ViewModel() {
    private val _uiState = mutableStateOf<MatchState>(MatchState.Idle)
    val uiState: State<MatchState> = _uiState

    fun calculateMatch(person1: PersonDetail, person2: PersonDetail) {
        viewModelScope.launch {
            _uiState.value = MatchState.Loading
            try {
                val response = repository.calculateMatch(MatchRequest(person1, person2))
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = MatchState.Success(response.body()!!)
                } else if (response.code() == 402) {
                    val errorBody = response.errorBody()?.string()
                    val paywall = entitlementRepository?.parsePaywallFrom402Body(errorBody)
                    if (paywall != null) {
                        _uiState.value = MatchState.PaywallBlocked(paywall)
                    } else {
                        _uiState.value = MatchState.Error("Insufficient credits for match report")
                    }
                } else {
                    _uiState.value = MatchState.Error("Failed to calculate match")
                }
            } catch (e: Exception) {
                _uiState.value = MatchState.Error(ErrorSanitizer.sanitize(e))
            }
        }
    }

    fun resetToIdle() {
        _uiState.value = MatchState.Idle
    }
}
