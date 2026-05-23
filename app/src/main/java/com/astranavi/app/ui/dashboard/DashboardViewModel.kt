package com.astranavi.app.ui.dashboard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.model.*
import com.astranavi.app.data.model.PaywallCardData
import com.astranavi.app.data.cache.CacheMeta
import com.astranavi.app.data.repository.DashboardRepository
import com.astranavi.app.data.repository.AstrologyRepository
import com.astranavi.app.data.repository.AuthRepository
import com.astranavi.app.util.ErrorSanitizer
import com.astranavi.app.util.LocaleManager
import com.astranavi.app.util.ProfileChangeBus
import com.astranavi.app.util.SessionManager
import com.astranavi.app.util.ZodiacMapper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Response

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(
        val horoscope: HoroscopeResponse,
        val forecast: WeeklyForecastResponse? = null,
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
        val paywall: PaywallCardData? = null,
        val meta: CacheMeta? = null
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
        viewModelScope.launch {
            LocaleManager.localeVersion.drop(1).collect {
                fetchDashboardData(forceRefresh = true)
            }
        }
        viewModelScope.launch {
            ProfileChangeBus.version.drop(1).collect {
                fetchDashboardData(forceRefresh = true, silent = true)
            }
        }
    }

    fun setSidebarTab(tab: String) {
        val current = _uiState.value
        if (current is DashboardState.Success) {
            _uiState.value = current.copy(activeSidebarTab = tab)
        }
    }

    fun fetchDashboardData(forceRefresh: Boolean = false, silent: Boolean = false) {
        if (!silent || _uiState.value !is DashboardState.Success) {
            _uiState.value = DashboardState.Loading
        }
        viewModelScope.launch {
            var result: DashboardState = DashboardState.Error("Failed to load dashboard")
            for (attempt in 0 until 2) {
                try {
                    result = fetchDashboard(forceRefresh)
                    if (result is DashboardState.Success) break
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    result = DashboardState.Error(ErrorSanitizer.sanitize(e))
                }
            }
            if (silent && result !is DashboardState.Success && _uiState.value is DashboardState.Success) {
                return@launch
            }
            _uiState.value = result
        }
    }

    private suspend fun fetchDashboard(forceRefresh: Boolean): DashboardState {
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

        val profileResponse = authRepository.getProfile(forceRefresh)
        user = profileResponse.body()?.user
        moonSign = user?.moonSign ?: localMoonSign
        sunSign = user?.sunSign ?: localSunSign
        lagnaSignFromProfile = user?.lagnaSign ?: localLagnaSign
        mahadasha = user?.astrologyData?.dasha?.active?.find { it.type == "Mahadasha" }?.planet
        userName = user?.name ?: localUserName

        if (moonSign.isNullOrBlank()) {
            val analyzeResponse = repository.analyzeFull(AnalyzeFullRequest(force_refresh = forceRefresh))
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
        var horoscopeMeta: CacheMeta? = null
        val horoscopeResponse = repository.getDailyHoroscope(forceRefresh, metaConsumer = { horoscopeMeta = it })
        if (horoscopeResponse.isSuccessful) {
            horoscope = horoscopeResponse.body()
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

            data class DashboardParallelResults(
                val forecast: Response<WeeklyForecastResponse>,
                val kundli: Response<AnalyzeFullWrapper>,
                val consultHistory: Response<ConsultHistoryResponse>,
                val forecastMeta: CacheMeta?,
                val kundliMeta: CacheMeta?
            )

            val parallelResults = coroutineScope {
                val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                var forecastMeta: CacheMeta? = null
                var kundliMeta: CacheMeta? = null
                val forecastDeferred = async {
                    repository.getWeeklyForecast(
                        "general",
                        date = currentDate,
                        forceRefresh = forceRefresh,
                        metaConsumer = { forecastMeta = it }
                    )
                }
                val kundliDeferred = async {
                    repository.analyzeFull(
                        AnalyzeFullRequest(force_refresh = forceRefresh),
                        metaConsumer = { kundliMeta = it }
                    )
                }
                val consultHistoryDeferred = async { astrologyRepository.getConsultHistory(limit = 5) }

                val forecastRes = forecastDeferred.await()
                val kundliRes = kundliDeferred.await()
                val consultHistoryRes = consultHistoryDeferred.await()

                DashboardParallelResults(
                    forecast = forecastRes,
                    kundli = kundliRes,
                    consultHistory = consultHistoryRes,
                    forecastMeta = forecastMeta,
                    kundliMeta = kundliMeta
                )
            }

            val forecastRes = parallelResults.forecast
            val kundliRes = parallelResults.kundli
            val consultHistoryRes = parallelResults.consultHistory

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
                paywall = horoscope.paywall ?: kundliPaywall,
                meta = CacheMeta.combine(listOf(horoscopeMeta, parallelResults.forecastMeta, parallelResults.kundliMeta))
            )
        } else {
            return DashboardState.Error("Failed to load your cosmic guidance. Please ensure your profile is complete.")
        }
    }
}