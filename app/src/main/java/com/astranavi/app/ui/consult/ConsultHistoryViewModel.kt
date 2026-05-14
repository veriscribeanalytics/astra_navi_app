package com.astranavi.app.ui.consult

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.model.ConsultRecord
import com.astranavi.app.data.repository.AstrologyRepository
import com.astranavi.app.util.ErrorSanitizer
import com.astranavi.app.util.SessionManager
import kotlinx.coroutines.launch

sealed class ConsultHistoryState {
    object Loading : ConsultHistoryState()
    data class Success(val history: List<ConsultRecord>) : ConsultHistoryState()
    data class Error(val message: String) : ConsultHistoryState()
}

class ConsultHistoryViewModel(
    private val repository: AstrologyRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = mutableStateOf<ConsultHistoryState>(ConsultHistoryState.Loading)
    val uiState: State<ConsultHistoryState> = _uiState

    init {
        fetchHistory()
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _uiState.value = ConsultHistoryState.Loading
            try {
                val response = repository.getConsultHistory()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body == null) {
                        _uiState.value = ConsultHistoryState.Error("Parsing failed. Please contact support.")
                    } else {
                        val records = body.results ?: body.results_alt ?: emptyList()
                        _uiState.value = ConsultHistoryState.Success(records)
                    }
                } else {
                    _uiState.value = ConsultHistoryState.Error("Failed to fetch history (Code: ${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = ConsultHistoryState.Error(ErrorSanitizer.sanitize(e))
            }
        }
    }
}
