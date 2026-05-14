package com.astranavi.app.ui.forecast

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.model.ForecastResponse
import com.astranavi.app.data.repository.DashboardRepository
import com.astranavi.app.util.ErrorSanitizer
import com.astranavi.app.util.SessionManager
import kotlinx.coroutines.launch

sealed class ForecastUiState {
    object Loading : ForecastUiState()
    data class Success(val forecast: ForecastResponse) : ForecastUiState()
    data class Error(val message: String) : ForecastUiState()
}

enum class ForecastRange {
    PAST_MONTH, WEEK, MONTH
}

class ForecastViewModel(
    private val repository: DashboardRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = mutableStateOf<ForecastUiState>(ForecastUiState.Loading)
    val uiState: State<ForecastUiState> = _uiState

    private val _selectedArea = mutableStateOf("general")
    val selectedArea: State<String> = _selectedArea

    private val _selectedRange = mutableStateOf(ForecastRange.WEEK)
    val selectedRange: State<ForecastRange> = _selectedRange

    private var isInitialized = false

    fun selectArea(area: String) {
        if (_selectedArea.value != area || !isInitialized) {
            _selectedArea.value = area
            isInitialized = true
            fetchForecast()
        }
    }

    fun selectRange(range: ForecastRange) {
        if (_selectedRange.value != range) {
            _selectedRange.value = range
            fetchForecast()
        }
    }

    fun fetchForecast() {
        val area = _selectedArea.value
        val range = _selectedRange.value
        
        val (daysBack, daysForward) = when (range) {
            ForecastRange.PAST_MONTH -> 30 to 0
            ForecastRange.WEEK -> 0 to 7
            ForecastRange.MONTH -> 0 to 30
        }

        _uiState.value = ForecastUiState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getForecast(area, daysBack = daysBack, daysForward = daysForward)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = ForecastUiState.Success(response.body()!!)
                } else {
                    _uiState.value = ForecastUiState.Error("Failed to load forecast data: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = ForecastUiState.Error(ErrorSanitizer.sanitize(e))
            }
        }
    }
}