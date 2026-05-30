package com.astranavi.app.ui.test

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.repository.DashboardRepository
import com.astranavi.app.util.LocaleManager
import kotlinx.coroutines.launch

class TestViewModel(
    private val repository: DashboardRepository
) : ViewModel() {
    private val _sign = mutableStateOf("")
    val sign: State<String> = _sign

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _lang = mutableStateOf("")
    val lang: State<String> = _lang

    private val _jsonResponse = mutableStateOf("")
    val jsonResponse: State<String> = _jsonResponse

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf("")
    val error: State<String> = _error

    init {
        // Automatically fetch timings on init, behaving exactly like daily-horoscope
        fetchTimings()
    }

    fun onSignChange(newSign: String) {
        _sign.value = newSign
    }

    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun onLangChange(newLang: String) {
        _lang.value = newLang
    }

    fun fetchTimings() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""
            _jsonResponse.value = ""
            try {
                val effectiveSign = _sign.value.ifBlank { null }
                val effectiveName = _name.value.ifBlank { null }
                val effectiveLang = _lang.value.ifBlank { LocaleManager.current() }
                
                val response = repository.getDailyHoroscopeTimings(
                    sign = effectiveSign,
                    name = effectiveName,
                    lang = effectiveLang
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val format = kotlinx.serialization.json.Json { prettyPrint = true }
                        val prettyJson = format.encodeToString(com.astranavi.app.data.model.DailyHoroscopeTimingsResponse.serializer(), body)
                        _jsonResponse.value = prettyJson
                    } else {
                        _jsonResponse.value = "Empty body response"
                    }
                } else {
                    _error.value = "Error code: ${response.code()}\n${response.errorBody()?.string() ?: ""}"
                }
            } catch (e: Exception) {
                _error.value = "Exception: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
