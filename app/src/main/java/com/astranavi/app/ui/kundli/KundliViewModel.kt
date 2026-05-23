package com.astranavi.app.ui.kundli

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.cache.CacheMeta
import com.astranavi.app.data.model.AnalyzeFullRequest
import com.astranavi.app.data.model.AnalyzeFullResponse
import com.astranavi.app.data.model.LockedContent
import com.astranavi.app.data.model.PaywallCardData
import com.astranavi.app.data.repository.AstrologyRepository
import com.astranavi.app.util.ErrorSanitizer
import com.astranavi.app.util.LocaleManager
import com.astranavi.app.util.ProfileChangeBus
import com.astranavi.app.util.SessionManager
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class KundliState {
    object Loading : KundliState()
    data class Success(
        val data: AnalyzeFullResponse,
        val lockedSections: Map<String, LockedContent> = emptyMap(),
        val paywall: PaywallCardData? = null,
        val meta: CacheMeta? = null
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

sealed class PlanetUiState {
    object Gallery : PlanetUiState()
    data class Detail(val planetName: String, val circleInfo: CircleInfo) : PlanetUiState()
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

    private val _userName = mutableStateOf<String?>(null)
    val userName: State<String?> = _userName

    var houseUiState by mutableStateOf<HouseUiState>(HouseUiState.Grid)
        private set

    var planetUiState by mutableStateOf<PlanetUiState>(PlanetUiState.Gallery)
        private set

    init {
        viewModelScope.launch {
            _userEmail.value = sessionManager.userEmail.first()
            _accessToken.value = sessionManager.accessToken.first()
            _userName.value = sessionManager.userName.first()
        }
        fetchKundli()
        viewModelScope.launch {
            LocaleManager.localeVersion.drop(1).collect {
                fetchKundli(forceRefresh = true)
            }
        }
        viewModelScope.launch {
            ProfileChangeBus.version.drop(1).collect {
                fetchKundli(forceRefresh = true, silent = true)
            }
        }
    }

    fun onHouseClick(houseId: Int, circleInfo: CircleInfo) {
        houseUiState = HouseUiState.Detail(houseId, circleInfo)
    }

    fun onHouseBack() {
        houseUiState = HouseUiState.Grid
    }

    fun onPlanetClick(planetName: String, circleInfo: CircleInfo) {
        planetUiState = PlanetUiState.Detail(planetName, circleInfo)
    }

    fun onPlanetBack() {
        planetUiState = PlanetUiState.Gallery
    }

    fun fetchKundli(forceRefresh: Boolean = false, silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent || _uiState.value !is KundliState.Success) {
                _uiState.value = KundliState.Loading
            }
            try {
                var meta: CacheMeta? = null
                val response = repository.analyzeFull(
                    AnalyzeFullRequest(force_refresh = forceRefresh),
                    metaConsumer = { meta = it }
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.astrologyData
                    val paywall = response.body()?.paywall
                    val lockedSections = data?.lockedSections ?: emptyMap()
                    if (data != null) {
                        _uiState.value = KundliState.Success(data, lockedSections, paywall, meta)
                    } else if (!silent || _uiState.value !is KundliState.Success) {
                        _uiState.value = KundliState.Error("No astrology data found in response")
                    }
                } else if (!silent || _uiState.value !is KundliState.Success) {
                    _uiState.value = KundliState.Error("Failed to fetch Kundli data")
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                if (!silent || _uiState.value !is KundliState.Success) {
                    _uiState.value = KundliState.Error(ErrorSanitizer.sanitize(e))
                }
            }
        }
    }
}
