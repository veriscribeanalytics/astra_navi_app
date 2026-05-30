package com.astranavi.app.ui.forecast

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.cache.CacheMeta
import com.astranavi.app.data.model.MonthlyForecastResponse
import com.astranavi.app.data.model.WeeklyForecastResponse
import com.astranavi.app.data.model.YearlyForecastResponse
import com.astranavi.app.data.repository.DashboardRepository
import com.astranavi.app.util.ErrorSanitizer
import com.astranavi.app.util.LocaleManager
import com.astranavi.app.util.ProfileChangeBus
import com.astranavi.app.util.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ForecastUiState {
    object Loading : ForecastUiState()
    data class Success(
        val weekly: WeeklyForecastResponse? = null,
        val monthly: MonthlyForecastResponse? = null,
        val yearly: YearlyForecastResponse? = null,
        val meta: CacheMeta? = null
    ) : ForecastUiState()
    data class Error(val message: String) : ForecastUiState()
}

enum class ForecastPeriod {
    WEEKLY,
    MONTHLY,
    YEARLY
}

class ForecastViewModel(
    private val repository: DashboardRepository,
    @Suppress("unused") private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = mutableStateOf<ForecastUiState>(ForecastUiState.Loading)
    val uiState: State<ForecastUiState> = _uiState

    private val _selectedArea = mutableStateOf("general")
    val selectedArea: State<String> = _selectedArea

    private val _selectedPeriod = mutableStateOf(ForecastPeriod.WEEKLY)
    val selectedPeriod: State<ForecastPeriod> = _selectedPeriod

    private var isInitialized = false

    init {
        viewModelScope.launch {
            LocaleManager.localeVersion.drop(1).collect {
                if (isInitialized) fetchForecast(forceRefresh = true)
            }
        }
        viewModelScope.launch {
            ProfileChangeBus.version.drop(1).collect {
                if (isInitialized) fetchForecast(forceRefresh = true, silent = true)
            }
        }
    }

    fun selectArea(area: String) {
        val normalizedArea = normalizeArea(area)
        if (_selectedArea.value != normalizedArea || !isInitialized) {
            _selectedArea.value = normalizedArea
            isInitialized = true
            fetchForecast()
        }
    }

    fun selectPeriod(period: ForecastPeriod) {
        if (_selectedPeriod.value != period) {
            _selectedPeriod.value = period
        }
    }

    fun fetchForecast(forceRefresh: Boolean = false, silent: Boolean = false) {
        val area = _selectedArea.value

        if (!silent || _uiState.value !is ForecastUiState.Success) {
            _uiState.value = ForecastUiState.Loading
        }
        viewModelScope.launch {
            try {
                var weeklyMeta: CacheMeta? = null
                var monthlyMeta: CacheMeta? = null
                var yearlyMeta: CacheMeta? = null

                // Fetch weekly, monthly, and yearly forecasts in parallel
                val weeklyDeferred = async {
                    repository.getWeeklyForecast(
                        area, date = currentDate(), forceRefresh = forceRefresh,
                        metaConsumer = { weeklyMeta = it }
                    )
                }
                val monthlyDeferred = async {
                    repository.getMonthlyForecast(
                        area, month = currentMonth(), forceRefresh = forceRefresh,
                        metaConsumer = { monthlyMeta = it }
                    )
                }
                val yearlyDeferred = async {
                    repository.getYearlyForecast(
                        area, year = currentYear(), forceRefresh = forceRefresh,
                        metaConsumer = { yearlyMeta = it }
                    )
                }

                val weeklyResponse = weeklyDeferred.await()
                val monthlyResponse = monthlyDeferred.await()
                val yearlyResponse = yearlyDeferred.await()

                val weekly = if (weeklyResponse.isSuccessful) weeklyResponse.body() else null
                val monthly = if (monthlyResponse.isSuccessful) monthlyResponse.body() else null
                val yearly = if (yearlyResponse.isSuccessful) yearlyResponse.body() else null

                if (weekly != null || monthly != null || yearly != null) {
                    // Show whatever periods loaded; a single failed period no longer fails the screen.
                    _uiState.value = ForecastUiState.Success(
                        weekly = weekly,
                        monthly = monthly,
                        yearly = yearly,
                        meta = CacheMeta.combine(listOf(weeklyMeta, monthlyMeta, yearlyMeta))
                    )
                } else if (!silent || _uiState.value !is ForecastUiState.Success) {
                    val failedCode = listOf(weeklyResponse, monthlyResponse, yearlyResponse)
                        .firstOrNull { !it.isSuccessful }
                        ?.code()
                    _uiState.value = ForecastUiState.Error(
                        failedCode?.let { ErrorSanitizer.sanitizeHttpCode(it) }
                            ?: "Forecast could not be loaded. Please try again."
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                if (!silent || _uiState.value !is ForecastUiState.Success) {
                    _uiState.value = ForecastUiState.Error(ErrorSanitizer.sanitize(e))
                }
            }
        }
    }

    private fun normalizeArea(area: String): String {
        return when (area.lowercase(Locale.US).trim()) {
            "career", "love", "health", "finance", "spiritual" -> area.lowercase(Locale.US).trim()
            else -> "general"
        }
    }

    private fun currentDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun currentMonth(): String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    private fun currentYear(): Int = SimpleDateFormat("yyyy", Locale.US).format(Date()).toInt()
}
