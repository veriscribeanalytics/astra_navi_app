package com.astranavi.app.ui.chat

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * All chat-screen tints derive from a single accent color sent by the backend
 * (`ChatAvatar.accentColor`). Brighter / softer / darker variants are produced
 * here so the server stays the single source of truth.
 */
data class ChatAvatarPalette(
    val accent: Color,
    val deep: Color,
    val radial: Color,
    val bubble: Color,
    val onBubble: Color
)

private val DefaultAccent = Color(0xFF6366F1) // matches Navi's server accent

fun chatAvatarPalette(accentHex: String?): ChatAvatarPalette {
    val accent = parseHexColor(accentHex) ?: DefaultAccent
    return ChatAvatarPalette(
        accent = accent,
        deep = accent.darken(0.55f),
        radial = accent.copy(alpha = 0.60f),
        bubble = accent,
        onBubble = if (accent.luminance() > 0.55f) Color(0xFF1A1003) else Color.White
    )
}

private fun parseHexColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    val cleaned = hex.trim().removePrefix("#")
    val normalized = when (cleaned.length) {
        6 -> "FF$cleaned"
        8 -> cleaned
        else -> return null
    }
    return runCatching { Color(normalized.toLong(16)) }.getOrNull()
}

/** Pulls a color toward black by [factor] (0f = unchanged, 1f = black). */
private fun Color.darken(factor: Float): Color {
    val k = 1f - factor.coerceIn(0f, 1f)
    return Color(red = red * k, green = green * k, blue = blue * k, alpha = alpha)
}
