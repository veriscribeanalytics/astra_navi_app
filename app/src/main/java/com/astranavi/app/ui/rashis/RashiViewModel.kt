package com.astranavi.app.ui.rashis

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.model.HoroscopeResponse
import com.astranavi.app.data.model.Rashi
import com.astranavi.app.data.repository.AstrologyRepository
import com.astranavi.app.data.repository.RashiData
import com.astranavi.app.util.SessionManager
import kotlinx.coroutines.launch

sealed class RashiViewMode {
    object Encyclopedia : RashiViewMode()
    data class Detail(val rashi: Rashi) : RashiViewMode()
}

class RashiViewModel(
    private val repository: AstrologyRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _viewMode = mutableStateOf<RashiViewMode>(RashiViewMode.Encyclopedia)
    val viewMode: State<RashiViewMode> = _viewMode

    private val _horoscopeData = mutableStateOf<HoroscopeResponse?>(null)
    val horoscopeData: State<HoroscopeResponse?> = _horoscopeData

    private val _isHoroscopeLoading = mutableStateOf(false)
    val isHoroscopeLoading: State<Boolean> = _isHoroscopeLoading

    val allRashis = RashiData.rashis

    fun selectRashi(rashi: Rashi) {
        _viewMode.value = RashiViewMode.Detail(rashi)
        fetchGeneralHoroscope(rashi.nameEn)
    }

    fun selectRashiById(rashiId: String) {
        val rashi = allRashis.find { it.id.toString().equals(rashiId, true) }
        if (rashi != null) {
            selectRashi(rashi)
        }
    }

    fun backToEncyclopedia() {
        _viewMode.value = RashiViewMode.Encyclopedia
        _horoscopeData.value = null
    }

    private fun fetchGeneralHoroscope(sign: String) {
        viewModelScope.launch {
            _isHoroscopeLoading.value = true
            try {
                val response = repository.getGeneralHoroscope(sign)
                if (response.isSuccessful) {
                    _horoscopeData.value = response.body()
                }
            } catch (e: Exception) {
                // Silently fail or log
            } finally {
                _isHoroscopeLoading.value = false
            }
        }
    }
}
