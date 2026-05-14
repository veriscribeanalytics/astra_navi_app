package com.astranavi.app.ui.theme

import androidx.compose.ui.graphics.Color

// Light Mode Colors (Ivory Theme)
val LightBackground = Color(0xFFFAF7F2)
val LightForeground = Color(0xFF2A1A4A)
val LightSurface = Color(0xFFF1E4DB)
val LightSurfaceVariant = Color(0xFFE6D8E0)
val LightPrimary = Color(0xFF2A1A4A)
val LightSecondary = Color(0xFFC8880A)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF2A1A4A)
val LightOnSurfaceVariant = Color(0xFF4A3B6A)
val LightOutlineVariant = Color(0xFFD1B8C6)

// Dark Mode Colors (Midnight Theme)
val DarkBackground = Color(0xFF0B071A)
val DarkForeground = Color(0xFFFAF7F2)
val DarkSurface = Color(0xFF0B071A)
val DarkSurfaceVariant = Color(0xFF231942)
val DarkPrimary = Color(0xFFFAF7F2)
val DarkSecondary = Color(0xFFC8880A)
val DarkOnPrimary = Color(0xFF0B071A)
val DarkOnSecondary = Color(0xFF0B071A)
val DarkOnSurface = Color(0xFFFAF7F2)
val DarkOnSurfaceVariant = Color(0xFFB9A8D9)
val DarkOutlineVariant = Color(0xFF3D2A63)

// Semantic Theme-Aware Colors
val LightExalted = Color(0xFF2E7D32)
val LightDebilitated = Color(0xFFC62828)
val LightNormal = Color(0xFF616161)
val LightRetrograde = Color(0xFFF57F17)
val LightGlassSurface = Color(0xCCFFFFFF) // Milky white, 80% opacity
val LightGlassBorder = Color(0x33000000)
val LightHeroGradientStart = Color(0xFFFFE0B2) // Soft peach
val LightHeroGradientEnd = Color(0xFFBBDEFB)  // Pale blue

val DarkExalted = Color(0xFF69F0AE)
val DarkDebilitated = Color(0xFFFF5252)
val DarkNormal = Color(0xFF9E9E9E)
val DarkRetrograde = Color(0xFFFFD740)
val DarkGlassSurface = Color(0x990B071A) // Deep obsidian, 60% opacity
val DarkGlassBorder = Color(0x4DFFFFFF) // Faint gold/white glow
val DarkHeroGradientStart = Color(0xFF1A237E) // Deep navy
val DarkHeroGradientEnd = Color(0xFF000000)  // Black

object AstroColors {
    val Sun = Color(0xFFF57C00) // Orange/Gold
    val Moon = Color(0xFFE0E0E0) // Silver/White
    val Mars = Color(0xFFD32F2F) // Red
    val Mercury = Color(0xFF388E3C) // Green
    val Jupiter = Color(0xFFFBC02D) // Yellow
    val Venus = Color(0xFFF48FB1) // Pink/Diamond
    val Saturn = Color(0xFF512DA8) // Dark Blue/Black
    val Rahu = Color(0xFF455A64) // Dark Grey/Smoke
    val Ketu = Color(0xFF795548) // Brown/Earth
    val Default = Color(0xFF7C3AED) // Default Purple

    fun getPlanetaryColor(planet: String?): Color {
        if (planet == null) return Default
        return when (planet.lowercase()) {
            "sun" -> Sun
            "moon" -> Moon
            "mars" -> Mars
            "mercury" -> Mercury
            "jupiter" -> Jupiter
            "venus" -> Venus
            "saturn" -> Saturn
            "rahu" -> Rahu
            "ketu" -> Ketu
            else -> Default
        }
    }
}
