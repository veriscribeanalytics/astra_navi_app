package com.astranavi.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.setValue

data class GlowColors(
    val accent: Color,
    val deep: Color,
    val radial: Color
)

val NeutralGlowColors = GlowColors(
    accent = Color.Transparent,
    deep = Color.Transparent,
    radial = Color.Transparent
)

val IvoryGlowColors = GlowColors(
    accent = Color(0xFFC8880A),
    deep = Color(0xFFD8A84A),
    radial = Color(0xFFC8880A)
)

val MidnightGlowColors = GlowColors(
    accent = Color(0xFF1A237E),
    deep = Color(0xFF1A237E),
    radial = Color(0xFF1A237E)
)

class GlowRegistry {
    private val _count: MutableState<Int> = mutableStateOf(0)
    private val _colors: MutableState<GlowColors> = mutableStateOf(NeutralGlowColors)

    var count by _count
    var colors by _colors

    fun register(initialColors: GlowColors) {
        count++
        colors = initialColors
    }

    fun unregister() {
        count--
        if (count <= 0) {
            count = 0
            colors = NeutralGlowColors
        }
    }
}

val LocalGlowRegistry = compositionLocalOf<GlowRegistry> {
    error("No GlowRegistry provided")
}

@Composable
fun ApplyRootGlow(colors: GlowColors) {
    val registry = LocalGlowRegistry.current
    DisposableEffect(registry, colors) {
        registry.register(colors)
        onDispose { registry.unregister() }
    }
}

/**
 * Animated full-screen atmospheric glow. Same recipe as [AtmosphericGlowLayer]
 * (vertical gradient from top + radial bloom) but the three colors cross-fade
 * so palette swaps (Forecast area change, chat avatar switch) recolor the page
 * smoothly instead of jump-cutting. Default 200 ms gives a snappy
 * page-transition fade that lingers briefly after leaving a glowing screen.
 *
 * Alphas are halved in light theme so the glow doesn't bleach the chrome.
 */
@Composable
fun AnimatedAtmosphericGlow(
    accentColor: Color,
    modifier: Modifier = Modifier,
    deepColor: Color = accentColor,
    radialColor: Color = accentColor,
    durationMillis: Int = 200
) {
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val fallback = if (isLightTheme) IvoryGlowColors else MidnightGlowColors
    val effectiveAccent = if (accentColor == Color.Transparent) fallback.accent else accentColor
    val effectiveDeep = if (deepColor == Color.Transparent) fallback.deep else deepColor
    val effectiveRadial = if (radialColor == Color.Transparent) fallback.radial else radialColor

    val accent by animateColorAsState(effectiveAccent, tween(durationMillis), label = "glowAccent")
    val deep by animateColorAsState(effectiveDeep, tween(durationMillis), label = "glowDeep")
    val radial by animateColorAsState(effectiveRadial, tween(durationMillis), label = "glowRadial")
    val topAlphaA = if (isLightTheme) 0.25f else 0.50f
    val topAlphaB = if (isLightTheme) 0.14f else 0.28f
    val deepAlpha = if (isLightTheme) 0.06f else 0.12f
    val radialAlphaA = if (isLightTheme) 0.12f else 0.22f
    val radialAlphaB = if (isLightTheme) 0.04f else 0.08f

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to accent.copy(alpha = topAlphaA),
                            0.18f to accent.copy(alpha = topAlphaB),
                            0.42f to deep.copy(alpha = deepAlpha),
                            0.65f to Color.Transparent,
                            1.00f to Color.Transparent
                        )
                    )
                )
                val r = size.width * 0.55f
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            radial.copy(alpha = radialAlphaA),
                            radial.copy(alpha = radialAlphaB),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height * 0.22f),
                        radius = r
                    )
                )
            }
    )
}
