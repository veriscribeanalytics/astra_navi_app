package com.astranavi.app.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Configuration for the top app bar behavior and content.
 */
data class TopAppBarConfig(
    val visible: Boolean = true,
    val titleMode: TitleMode = TitleMode.Static,
    val showBackButton: Boolean = true,
    val showLanguageChip: Boolean = true,
    val rightAction: RightAction = RightAction.MENU,
    val scrollBehavior: ScrollBehavior = ScrollBehavior.HIDE_ON_SCROLL,
    val drawsBehind: Boolean = false,
    val customTopPadding: Dp? = null
)

sealed class TitleMode {
    object Static : TitleMode()
    object ChatAvatar : TitleMode()
}

enum class RightAction {
    MENU,           // Hamburger menu (default for top-level)
    HISTORY,        // History icon (Consult, Match)
    CHAT_ACTIONS,   // Chat-specific: History + NewChat
    NONE            // No action (just credit badge)
}

enum class ScrollBehavior {
    NONE,              // No scroll behavior (login, intro, splash)
    HIDE_ON_SCROLL,    // Hide bars on scroll down (default)
    PIN                // Always visible (chat)
}

/**
 * CompositionLocal to allow screens to dynamically override top bar config.
 * Useful for multi-step flows like ConsultScreen where config changes per step.
 */
val LocalTopBarConfigOverride = compositionLocalOf<TopAppBarConfig?> { null }
