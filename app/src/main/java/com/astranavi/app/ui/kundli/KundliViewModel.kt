package com.astranavi.app.ui.kundli

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.model.AnalyzeFullRequest
import com.astranavi.app.data.model.AnalyzeFullResponse
import com.astranavi.app.data.model.LockedContent
import com.astranavi.app.data.model.PaywallCardData
import com.astranavi.app.data.repository.AstrologyRepository
import com.astranavi.app.util.ErrorSanitizer
import com.astranavi.app.util.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class KundliState {
    object Loading : KundliState()
    data class Success(
        val data: AnalyzeFullResponse,
        val lockedSections: Map<String, LockedContent> = emptyMap(),
        val paywall: PaywallCardData? = null
    ) : KundliState()
    data class Error(val message: String) : KundliState()
}

data class CircleInfo(
    val id: Int,
    val offset: Offset,
    val size: IntSize
)

sealed class HouseUiState {
    object Grid : HouseUiState()
    data class Detail(val houseId: Int, val circleInfo: CircleInfo) : HouseUiState()
}

class KundliViewModel(
    private val repository: AstrologyRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = mutableStateOf<KundliState>(KundliState.Loading)
    val uiState: State<KundliState> = _uiState

    private val _userEmail = mutableStateOf<String?>(null)
    val userEmail: State<String?> = _userEmail

    private val _accessToken = mutableStateOf<String?>(null)
    val accessToken: State<String?> = _accessToken

    var houseUiState by mutableStateOf<HouseUiState>(HouseUiState.Grid)
        private set

    init {
        viewModelScope.launch {
            _userEmail.value = sessionManager.userEmail.first()
            _accessToken.value = sessionManager.accessToken.first()
        }
        fetchKundli()
    }

    fun onHouseClick(houseId: Int, circleInfo: CircleInfo) {
        houseUiState = HouseUiState.Detail(houseId, circleInfo)
    }

    fun onHouseBack() {
        houseUiState = HouseUiState.Grid
    }

    fun fetchKundli(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = KundliState.Loading
            try {
                val response = repository.analyzeFull(AnalyzeFullRequest(force_refresh = forceRefresh))
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.astrologyData
                    val paywall = response.body()?.paywall
                    val lockedSections = data?.lockedSections ?: emptyMap()
                    if (data != null) {
                        _uiState.value = KundliState.Success(data, lockedSections, paywall)
                    } else {
                        _uiState.value = KundliState.Error("No astrology data found in response")
                    }
                } else {
                    _uiState.value = KundliState.Error("Failed to fetch Kundli data")
                }
            } catch (e: Exception) {
                _uiState.value = KundliState.Error(ErrorSanitizer.sanitize(e))
            }
        }
    }
}
