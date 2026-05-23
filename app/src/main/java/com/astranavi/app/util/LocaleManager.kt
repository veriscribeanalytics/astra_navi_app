package com.astranavi.app.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LocaleManager {
    private val _localeVersion = MutableStateFlow(0)
    val localeVersion: StateFlow<Int> = _localeVersion.asStateFlow()

    // ComponentActivity (not AppCompatActivity) doesn't reliably round-trip the
    // applied locale through AppCompatDelegate.getApplicationLocales(), so we
    // also keep an in-process copy of the language tag last passed to apply().
    @Volatile
    private var currentTag: String = "en"

    fun apply(languageCode: String) {
        val previous = currentTag
        val appLocales = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocales)
        currentTag = languageCode
        if (!previous.equals(languageCode, ignoreCase = true)) {
            _localeVersion.value = _localeVersion.value + 1
        }
    }

    fun current(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return locales.get(0)?.language ?: currentTag.substringBefore('-').ifBlank { "en" }
    }

    fun currentLanguageTag(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return locales.get(0)?.toLanguageTag() ?: currentTag.ifBlank { "en" }
    }
}
