package com.astranavi.app.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Broadcasts when the current user's profile (name, birth details, language, etc.) has changed
 * locally on this device. Screens that render profile-derived data subscribe to refetch.
 *
 * Mirrors [LocaleManager.localeVersion] — bump() increments a monotonic counter; collectors
 * use `.drop(1)` to skip the initial value.
 */
object ProfileChangeBus {
    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version.asStateFlow()

    fun bump() {
        _version.value = _version.value + 1
    }
}
