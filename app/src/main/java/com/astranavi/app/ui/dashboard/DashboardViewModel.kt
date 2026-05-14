package com.astranavi.app.ui.dashboard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.model.*
import com.astranavi.app.data.model.PaywallCardData
import com.astranavi.app.data.repository.DashboardRepository
import com.astranavi.app.data.repository.AstrologyRepository
import com.astranavi.app.data.repository.AuthRepository
import com.astranavi.app.util.ErrorSanitizer
import com.astranavi.app.util.SessionManager
import com.astranavi.app.util.ZodiacMapper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(
        val horoscope: HoroscopeResponse,
        val forecast: ForecastResponse? = null,
        val kundliPreview: AnalyzeFullResponse? = null,
        val recentConsultations: List<ConsultRecord>? = null,
        val moonSign: String? = null,
        val sunSign: String? = null,
        val lagnaSign: String? = null,
        val mahadasha: String? = null,
        val userName: String? = null,
        val userEmail: String? = null,
        val accessToken: String? = null,
        val activeSidebarTab: String = "chat",
        val paywall: PaywallCardData? = null
    ) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel(
    private val repository: DashboardRepository,
    private val authRepository: AuthRepository,
    private val astrologyRepository: AstrologyRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = mutableStateOf<DashboardState>(DashboardState.Loading)
    val uiState: State<DashboardState> = _uiState

    init {
        fetchDashboardData()
    }

    fun setSidebarTab(tab: String) {
        val current = _uiState.value
        if (current is DashboardState.Success) {
            _uiState.value = current.copy(activeSidebarTab = tab)
        }
    }

    fun fetchDashboardData() {
        _uiState.value = DashboardState.Loading
        viewModelScope.launch {
            var result: DashboardState = DashboardState.Error("Failed to load dashboard")
            for (attempt in 0 until 2) {
                try {
                    result = fetchDashboard()
                    if (result is DashboardState.Success) break
                } catch (e: Exception) {
                    result = DashboardState.Error(ErrorSanitizer.sanitize(e))
                }
            }
            _uiState.value = result
        }
    }

    private suspend fun fetchDashboard(): DashboardState {
        val localMoonSign = sessionManager.moonSign.first()
        val localSunSign = sessionManager.sunSign.first()
        val localLagnaSign = sessionManager.lagnaSign.first()
        val localUserName = sessionManager.userName.first()
        val userEmail = sessionManager.userEmail.first()
        val accessToken = sessionManager.accessToken.first()

        var moonSign: String? = localMoonSign
        var sunSign: String? = localSunSign
        var lagnaSignFromProfile: String? = localLagnaSign
        var mahadasha: String? = null
        var userName: String? = localUserName
        var user: User? = null

        val profileResponse = authRepository.getProfile()
        user = profileResponse.body()?.user
        moonSign = user?.moonSign ?: localMoonSign
        sunSign = user?.sunSign ?: localSunSign
        lagnaSignFromProfile = user?.lagnaSign ?: localLagnaSign
        mahadasha = user?.astrologyData?.dasha?.active?.find { it.type == "Mahadasha" }?.planet
        userName = user?.name ?: localUserName

        if (moonSign.isNullOrBlank()) {
            val analyzeResponse = repository.analyzeFull(AnalyzeFullRequest(force_refresh = false))
            if (analyzeResponse.isSuccessful) {
                val kundliData = analyzeResponse.body()?.astrologyData
                if (kundliData != null) {
                    val ascendantSign = kundliData.ascendant?.sign
                    if (!ascendantSign.isNullOrBlank()) {
                        lagnaSignFromProfile = ZodiacMapper.getEnglishName(ascendantSign)
                    }
                    val moonPlanet = kundliData.planets?.find { it.planet.lowercase() == "moon" }
                    if (moonPlanet != null) {
                        moonSign = ZodiacMapper.getEnglishName(moonPlanet.sign)
                    }
                    val sunPlanet = kundliData.planets?.find { it.planet.lowercase() == "sun" }
                    if (sunPlanet != null) {
                        sunSign = ZodiacMapper.getEnglishName(sunPlanet.sign)
                    }
                    val activeMahadasha = kundliData.dasha?.active?.find { it.type == "Mahadasha" }?.planet
                    if (activeMahadasha != null) {
                        mahadasha = activeMahadasha
                    }
                }
            }
        }

        var horoscope: HoroscopeResponse? = null
        if (!moonSign.isNullOrBlank()) {
            val horoscopeResponse = repository.getDailyHoroscope(moonSign)
            if (horoscopeResponse.isSuccessful) {
                horoscope = horoscopeResponse.body()
            }
        }

        if (horoscope != null) {
            if (moonSign != null || sunSign != null || lagnaSignFromProfile != null) {
                sessionManager.updateSigns(
                    moonSign = moonSign ?: localMoonSign,
                    sunSign = sunSign ?: localSunSign,
                    lagnaSign = lagnaSignFromProfile ?: localLagnaSign
                )
                sessionManager.updateProfileData(
                    dob = user?.dob,
                    tob = user?.tob,
                    pob = user?.pob
                )
            }

            val (forecastRes, kundliRes, consultHistoryRes) = coroutineScope {
                val forecastDeferred = async { repository.getForecast("general", daysBack = 0, daysForward = 7) }
                val kundliDeferred = async { repository.analyzeFull(AnalyzeFullRequest(force_refresh = false)) }
                val consultHistoryDeferred = async { astrologyRepository.getConsultHistory(limit = 5) }

                Triple(forecastDeferred.await(), kundliDeferred.await(), consultHistoryDeferred.await())
            }

            val kundliPaywall = if (kundliRes.isSuccessful) kundliRes.body()?.paywall else null

            return DashboardState.Success(
                horoscope = horoscope,
                forecast = if (forecastRes.isSuccessful) forecastRes.body() else null,
                kundliPreview = if (kundliRes.isSuccessful) kundliRes.body()?.astrologyData else null,
                recentConsultations = if (consultHistoryRes.isSuccessful) {
                    val body = consultHistoryRes.body()
                    body?.results ?: body?.results_alt ?: emptyList()
                } else null,
                moonSign = moonSign,
                sunSign = sunSign,
                lagnaSign = lagnaSignFromProfile,
                mahadasha = mahadasha,
                userName = userName,
                userEmail = userEmail,
                accessToken = accessToken,
                paywall = horoscope.paywall ?: kundliPaywall
            )
        } else {
            return DashboardState.Error(
                if (moonSign.isNullOrBlank()) "Please complete your profile to see your daily horoscope"
                else "Failed to load your cosmic guidance"
            )
        }
    }
}