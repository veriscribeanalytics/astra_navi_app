package com.astranavi.app.ui.chat

import androidx.compose.ui.graphics.Color

/**
 * Color triple driving [com.astranavi.app.ui.components.AnimatedAtmosphericGlow]
 * for the chat screen, plus the tint used on the user's message bubble so the
 * conversation visually belongs to the chosen guide.
 *
 * One palette per Navi avatar — colors are pulled to roughly match the
 * avatar's portrait art so the screen mood stays coherent.
 */
data class ChatAvatarPalette(
    val accent: Color,
    val deep: Color,
    val radial: Color,
    val bubble: Color,
    val onBubble: Color
)

private val NaviPalette = ChatAvatarPalette(
    accent = Color(0xFFFFC36B),    // warm amber
    deep = Color(0xFFB8741A),
    radial = Color(0xFFFFD89B),
    bubble = Color(0xFFE89A3C),
    onBubble = Color(0xFF1F1303)
)

private val AryaPalette = ChatAvatarPalette(
    accent = Color(0xFF8AA9FF),    // strategic indigo
    deep = Color(0xFF1E3A8A),
    radial = Color(0xFFA5B4FC),
    bubble = Color(0xFF4F46E5),
    onBubble = Color.White
)

private val MeeraPalette = ChatAvatarPalette(
    accent = Color(0xFFF4A8C7),    // tender rose
    deep = Color(0xFF9D174D),
    radial = Color(0xFFFBCFE8),
    bubble = Color(0xFFDB2777),
    onBubble = Color.White
)

private val AnandPalette = ChatAvatarPalette(
    accent = Color(0xFFE5B45A),    // sandalwood saffron
    deep = Color(0xFF7C2D12),
    radial = Color(0xFFFCD980),
    bubble = Color(0xFFCA8A04),
    onBubble = Color(0xFF1A1003)
)

private val RishiPalette = ChatAvatarPalette(
    accent = Color(0xFFB498F0),    // deep sage violet
    deep = Color(0xFF4C1D95),
    radial = Color(0xFFD9C6F8),
    bubble = Color(0xFF6D28D9),
    onBubble = Color.White
)

fun chatAvatarPalette(avatarId: String?): ChatAvatarPalette = when (avatarId) {
    "career_mentor" -> AryaPalette
    "relationship_guide" -> MeeraPalette
    "spiritual_guide" -> AnandPalette
    "astro_sage" -> RishiPalette
    else -> NaviPalette
}
