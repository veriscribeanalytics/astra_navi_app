package com.astranavi.app.util

import com.astranavi.app.data.model.LockedContent

object PaywallUtils {
    fun isDashaLocked(
        interpretation: String?,
        lockedSections: Map<String, LockedContent>?
    ): Boolean {
        if (lockedSections?.get("dasha")?.locked == true) return true
        if (interpretation?.lowercase()?.contains("locked") == true) return true
        if (interpretation.isNullOrEmpty()) return true
        return false
    }
}