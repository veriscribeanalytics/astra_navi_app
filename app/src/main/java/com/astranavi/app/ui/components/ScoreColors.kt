package com.astranavi.app.ui.components

import androidx.compose.ui.graphics.Color

enum class ScorePhase {
    BAD,
    WEAK,
    MIXED,
    GOOD,
    EXCELLENT
}

data class ScorePalette(
    val phase: ScorePhase,
    val main: Color,
    val glow: Color,
    val surface: Color,
    val border: Color
)

object ScoreColors {

    fun phaseFor(score: Int): ScorePhase {
        val boundedScore = score.coerceIn(0, 100)

        return when {
            boundedScore < 40 -> ScorePhase.BAD
            boundedScore < 55 -> ScorePhase.WEAK
            boundedScore < 70 -> ScorePhase.MIXED
            boundedScore < 85 -> ScorePhase.GOOD
            else -> ScorePhase.EXCELLENT
        }
    }

    fun paletteFor(
        area: String?,
        score: Int,
        isDarkTheme: Boolean
    ): ScorePalette {

        val phase = phaseFor(score)
        val swatch = swatchFor(area, phase)

        val surfaceAlpha = if (isDarkTheme) 0.16f else 0.10f
        val borderAlpha = if (isDarkTheme) 0.44f else 0.28f

        return ScorePalette(
            phase = phase,
            main = swatch.main,
            glow = swatch.glow,
            surface = swatch.glow.copy(alpha = surfaceAlpha),
            border = swatch.glow.copy(alpha = borderAlpha)
        )
    }

    fun categoryBase(area: String?): Color {
        return when (area.normalizedArea()) {
            "love" -> Color(0xFFEC4899)
            "career" -> Color(0xFF3B82F6)
            "finance" -> Color(0xFF10B981)
            "health" -> Color(0xFF14B8A6)
            else -> Color(0xFFF59E0B)
        }
    }

    private fun swatchFor(
        area: String?,
        phase: ScorePhase
    ): Swatch {

        return when (area.normalizedArea()) {
            "love" -> loveSwatch(phase)
            "career" -> careerSwatch(phase)
            "finance" -> financeSwatch(phase)
            "health" -> healthSwatch(phase)
            else -> generalSwatch(phase)
        }
    }

    /**
     * GENERAL
     * Universal score understanding
     * Red -> Orange -> Amber -> Green
     */
    private fun generalSwatch(phase: ScorePhase): Swatch = when (phase) {

        ScorePhase.BAD ->
            Swatch(
                main = Color(0xFFDC2626),
                glow = Color(0xFFF87171)
            )

        ScorePhase.WEAK ->
            Swatch(
                main = Color(0xFFF97316),
                glow = Color(0xFFFDBA74)
            )

        ScorePhase.MIXED ->
            Swatch(
                main = Color(0xFFF59E0B),
                glow = Color(0xFFFBBF24)
            )

        ScorePhase.GOOD ->
            Swatch(
                main = Color(0xFF16A34A),
                glow = Color(0xFF4ADE80)
            )

        ScorePhase.EXCELLENT ->
            Swatch(
                main = Color(0xFF166534),
                glow = Color(0xFF22C55E)
            )
    }

    /**
     * LOVE
     * Romantic Venusian pink family
     */
    private fun loveSwatch(phase: ScorePhase): Swatch = when (phase) {

        ScorePhase.BAD ->
            Swatch(
                main = Color(0xFFBE123C),
                glow = Color(0xFFFB7185)
            )

        ScorePhase.WEAK ->
            Swatch(
                main = Color(0xFFE11D48),
                glow = Color(0xFFFB7185)
            )

        ScorePhase.MIXED ->
            Swatch(
                main = Color(0xFFF43F5E),
                glow = Color(0xFFFDA4AF)
            )

        ScorePhase.GOOD ->
            Swatch(
                main = Color(0xFFDB2777),
                glow = Color(0xFFF472B6)
            )

        ScorePhase.EXCELLENT ->
            Swatch(
                main = Color(0xFFBE185D),
                glow = Color(0xFFF9A8D4)
            )
    }

    /**
     * CAREER
     * Royal blue / indigo progression
     * Ambition + intellect + structure
     */
    private fun careerSwatch(phase: ScorePhase): Swatch = when (phase) {

        ScorePhase.BAD ->
            Swatch(
                main = Color(0xFF334155),
                glow = Color(0xFF64748B)
            )

        ScorePhase.WEAK ->
            Swatch(
                main = Color(0xFF475569),
                glow = Color(0xFF94A3B8)
            )

        ScorePhase.MIXED ->
            Swatch(
                main = Color(0xFF2563EB),
                glow = Color(0xFF60A5FA)
            )

        ScorePhase.GOOD ->
            Swatch(
                main = Color(0xFF3B82F6),
                glow = Color(0xFF93C5FD)
            )

        ScorePhase.EXCELLENT ->
            Swatch(
                main = Color(0xFF6366F1),
                glow = Color(0xFFA5B4FC)
            )
    }

    /**
     * FINANCE
     * Emerald + gold prosperity palette
     */
    private fun financeSwatch(phase: ScorePhase): Swatch = when (phase) {

        ScorePhase.BAD ->
            Swatch(
                main = Color(0xFF78716C),
                glow = Color(0xFFD6D3D1)
            )

        ScorePhase.WEAK ->
            Swatch(
                main = Color(0xFFA16207),
                glow = Color(0xFFFACC15)
            )

        ScorePhase.MIXED ->
            Swatch(
                main = Color(0xFF059669),
                glow = Color(0xFF34D399)
            )

        ScorePhase.GOOD ->
            Swatch(
                main = Color(0xFF10B981),
                glow = Color(0xFF6EE7B7)
            )

        ScorePhase.EXCELLENT ->
            Swatch(
                main = Color(0xFF065F46),
                glow = Color(0xFF34D399)
            )
    }

    /**
     * HEALTH
     * Healing teal + vitality greens
     */
    private fun healthSwatch(phase: ScorePhase): Swatch = when (phase) {

        ScorePhase.BAD ->
            Swatch(
                main = Color(0xFF4B5563),
                glow = Color(0xFF9CA3AF)
            )

        ScorePhase.WEAK ->
            Swatch(
                main = Color(0xFF4D7C0F),
                glow = Color(0xFFA3E635)
            )

        ScorePhase.MIXED ->
            Swatch(
                main = Color(0xFF0F766E),
                glow = Color(0xFF2DD4BF)
            )

        ScorePhase.GOOD ->
            Swatch(
                main = Color(0xFF10B981),
                glow = Color(0xFF6EE7B7)
            )

        ScorePhase.EXCELLENT ->
            Swatch(
                main = Color(0xFF14B8A6),
                glow = Color(0xFF5EEAD4)
            )
    }

    private data class Swatch(
        val main: Color,
        val glow: Color
    )

    private fun String?.normalizedArea(): String {
        return this?.lowercase()?.trim().orEmpty()
    }
}