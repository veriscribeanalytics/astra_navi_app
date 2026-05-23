package com.astranavi.app.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LocaleManager {
    private val _localeVersion = MutableStateFlow(0)
    val localeVersion: StateFlow<Int> = _localeVersion.asStateFlow()

    fun apply(languageCode: String) {
        val appLocales = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocales)
        _localeVersion.value = _localeVersion.value + 1
    }

    fun current(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) {
            return "en"
        }
        return locales.get(0)?.language ?: "en"
    }

    fun currentLanguageTag(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) {
            return "en"
        }
        return locales.get(0)?.toLanguageTag() ?: "en"
    }
}
