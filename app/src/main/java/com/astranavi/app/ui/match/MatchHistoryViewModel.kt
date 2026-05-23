package com.astranavi.app.ui.match

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateMapOf
import com.astranavi.app.data.model.MatchRecord
import com.astranavi.app.data.model.MatchResponse
import com.astranavi.app.data.repository.AstrologyRepository
import com.astranavi.app.util.ErrorSanitizer
import com.astranavi.app.util.LocaleManager
import com.astranavi.app.util.SessionManager
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class MatchHistoryState {
    object Loading : MatchHistoryState()
    data class Success(val history: List<MatchRecord>) : MatchHistoryState()
    data class Error(val message: String) : MatchHistoryState()
}

class MatchHistoryViewModel(
    private val repository: AstrologyRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = mutableStateOf<MatchHistoryState>(MatchHistoryState.Loading)
    val uiState: State<MatchHistoryState> = _uiState

    // Map to hold details for expanded items: recordId -> MatchResponse
    val expandedDetails = mutableStateMapOf<String, MatchResponse>()
    val loadingDetails = mutableStateMapOf<String, Boolean>()

    init {
        fetchHistory()
        viewModelScope.launch {
            LocaleManager.localeVersion.drop(1).collect {
                expandedDetails.clear()
                fetchHistory()
            }
        }
    }

    fun fetchHistory(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent || _uiState.value !is MatchHistoryState.Success) {
                _uiState.value = MatchHistoryState.Loading
            }
            try {
                val response = repository.getMatchHistory()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val records = body.results ?: body.history ?: emptyList()
                    _uiState.value = MatchHistoryState.Success(records)
                } else if (!silent || _uiState.value !is MatchHistoryState.Success) {
                    _uiState.value = MatchHistoryState.Error("Failed to fetch history")
                }
            } catch (e: Exception) {
                if (!silent || _uiState.value !is MatchHistoryState.Success) {
                    _uiState.value = MatchHistoryState.Error(ErrorSanitizer.sanitize(e))
                }
            }
        }
    }

    fun fetchMatchDetails(matchId: String) {
        if (expandedDetails.containsKey(matchId)) return
        
        viewModelScope.launch {
            loadingDetails[matchId] = true
            try {
                val response = repository.getMatchResult(matchId)
                if (response.isSuccessful && response.body() != null) {
                    val record = response.body()!!
                    expandedDetails[matchId] = record.resultData ?: MatchResponse()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                loadingDetails[matchId] = false
            }
        }
    }

    fun deleteMatch(matchId: String) {
        viewModelScope.launch {
            try {
                val response = repository.deleteMatch(matchId)
                if (response.isSuccessful) {
                    // Remove from list locally if success
                    val currentState = _uiState.value
                    if (currentState is MatchHistoryState.Success) {
                        _uiState.value = MatchHistoryState.Success(
                            currentState.history.filter { it.id != matchId }
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
