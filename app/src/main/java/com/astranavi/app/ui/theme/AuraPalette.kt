package com.astranavi.app.ui.theme

import androidx.compose.ui.graphics.Color

data class AuraPalette(
    val topGlow: Color,
    val deepGlow: Color,
    val radialGlow: Color,
    val accent: Color
)

object RashiPalettes {
    private val Default = AuraPalette(
        topGlow = Color(0xFF6E365D),
        deepGlow = Color(0xFF4A244A),
        radialGlow = Color(0xFFD8B45A),
        accent = Color(0xFFC07A9B)
    )

    private val table: Map<String, AuraPalette> = mapOf(
        // Aries — Mars ember, crimson aura
        "aries" to AuraPalette(
            topGlow = Color(0xFF8A2B2B),
            deepGlow = Color(0xFF4A1414),
            radialGlow = Color(0xFFF06A3A),
            accent = Color(0xFFE25C5C)
        ),
        // Taurus — Venus plum + earth gold
        "taurus" to AuraPalette(
            topGlow = Color(0xFF6E365D),
            deepGlow = Color(0xFF4A244A),
            radialGlow = Color(0xFFD8B45A),
            accent = Color(0xFFC07A9B)
        ),
        // Gemini — Mercury airy violet + soft teal
        "gemini" to AuraPalette(
            topGlow = Color(0xFF5B4B8A),
            deepGlow = Color(0xFF2E2552),
            radialGlow = Color(0xFF8FD0C2),
            accent = Color(0xFFA89BE0)
        ),
        // Cancer — Moon silver + pearl blue
        "cancer" to AuraPalette(
            topGlow = Color(0xFF3E4F7A),
            deepGlow = Color(0xFF1E2742),
            radialGlow = Color(0xFFD4E1F2),
            accent = Color(0xFF9FB6D9)
        ),
        // Leo — Sun gold + amber royal
        "leo" to AuraPalette(
            topGlow = Color(0xFF8A5418),
            deepGlow = Color(0xFF4A2C0A),
            radialGlow = Color(0xFFF5C24A),
            accent = Color(0xFFE89A3C)
        ),
        // Virgo — Mercury moss + olive earth
        "virgo" to AuraPalette(
            topGlow = Color(0xFF4A6340),
            deepGlow = Color(0xFF243320),
            radialGlow = Color(0xFFC8B868),
            accent = Color(0xFF8FA877)
        ),
        // Libra — Venus rose + sky harmony
        "libra" to AuraPalette(
            topGlow = Color(0xFF7A4A6E),
            deepGlow = Color(0xFF3E2440),
            radialGlow = Color(0xFFE8C0CC),
            accent = Color(0xFFD49AB8)
        ),
        // Scorpio — Mars-deep crimson + obsidian
        "scorpio" to AuraPalette(
            topGlow = Color(0xFF5A1838),
            deepGlow = Color(0xFF2A0A1C),
            radialGlow = Color(0xFFB23A5A),
            accent = Color(0xFF8E2A4A)
        ),
        // Sagittarius — Jupiter saffron + indigo
        "sagittarius" to AuraPalette(
            topGlow = Color(0xFF6B4A8C),
            deepGlow = Color(0xFF301E4A),
            radialGlow = Color(0xFFE8A24A),
            accent = Color(0xFFB89AE0)
        ),
        // Capricorn — Saturn slate + cold earth
        "capricorn" to AuraPalette(
            topGlow = Color(0xFF3A4258),
            deepGlow = Color(0xFF1A1F2E),
            radialGlow = Color(0xFFA8B0C2),
            accent = Color(0xFF6E7A94)
        ),
        // Aquarius — Saturn-Rahu electric violet + cyan spark
        "aquarius" to AuraPalette(
            topGlow = Color(0xFF3A4E7A),
            deepGlow = Color(0xFF1A2342),
            radialGlow = Color(0xFF6FD0E0),
            accent = Color(0xFF8AAEDC)
        ),
        // Pisces — Jupiter-ocean teal + dream lavender
        "pisces" to AuraPalette(
            topGlow = Color(0xFF2E5A6A),
            deepGlow = Color(0xFF132E38),
            radialGlow = Color(0xFFB8D4CC),
            accent = Color(0xFF7AA9B5)
        )
    )

    fun forSign(nameEn: String?): AuraPalette {
        val key = nameEn?.lowercase()?.trim() ?: return Default
        return table[key] ?: Default
    }
}

object PlanetPalettes {
    private val Default = AuraPalette(
        topGlow = Color(0xFF4A3070),
        deepGlow = Color(0xFF231942),
        radialGlow = Color(0xFFB89AE0),
        accent = Color(0xFF7C3AED)
    )

    private val table: Map<String, AuraPalette> = mapOf(
        "sun" to AuraPalette(
            topGlow = Color(0xFF8A5418),
            deepGlow = Color(0xFF4A2C0A),
            radialGlow = Color(0xFFF5C24A),
            accent = Color(0xFFE89A3C)
        ),
        "moon" to AuraPalette(
            topGlow = Color(0xFF3E4F7A),
            deepGlow = Color(0xFF1E2742),
            radialGlow = Color(0xFFD4E1F2),
            accent = Color(0xFF9FB6D9)
        ),
        "mars" to AuraPalette(
            topGlow = Color(0xFF8A2B2B),
            deepGlow = Color(0xFF4A1414),
            radialGlow = Color(0xFFF06A3A),
            accent = Color(0xFFE25C5C)
        ),
        "mercury" to AuraPalette(
            topGlow = Color(0xFF2E6A4A),
            deepGlow = Color(0xFF143324),
            radialGlow = Color(0xFF8FD0A8),
            accent = Color(0xFF5AA478)
        ),
        "jupiter" to AuraPalette(
            topGlow = Color(0xFF6B4A8C),
            deepGlow = Color(0xFF301E4A),
            radialGlow = Color(0xFFE8A24A),
            accent = Color(0xFFFBC02D)
        ),
        "venus" to AuraPalette(
            topGlow = Color(0xFF6E365D),
            deepGlow = Color(0xFF4A244A),
            radialGlow = Color(0xFFD8B45A),
            accent = Color(0xFFC07A9B)
        ),
        "saturn" to AuraPalette(
            topGlow = Color(0xFF3A3458),
            deepGlow = Color(0xFF1A172E),
            radialGlow = Color(0xFF8A82B0),
            accent = Color(0xFF6E5AA4)
        ),
        "rahu" to AuraPalette(
            topGlow = Color(0xFF2E3640),
            deepGlow = Color(0xFF12161C),
            radialGlow = Color(0xFF7A6A8E),
            accent = Color(0xFF5A6B7A)
        ),
        "ketu" to AuraPalette(
            topGlow = Color(0xFF5A3E2A),
            deepGlow = Color(0xFF2A1C12),
            radialGlow = Color(0xFFB89070),
            accent = Color(0xFF8C6A48)
        )
    )

    fun forPlanet(nameEn: String?): AuraPalette {
        val key = nameEn?.lowercase()?.trim() ?: return Default
        return table[key] ?: Default
    }
}
