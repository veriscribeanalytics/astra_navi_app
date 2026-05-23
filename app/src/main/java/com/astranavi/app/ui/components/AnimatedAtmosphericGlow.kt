package com.astranavi.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.MaterialTheme

/**
 * Palette consumed by the root-level [AnimatedAtmosphericGlow] in MainActivity.
 * Screens publish their desired glow via [LocalSetGlowColors] so the glow can
 * paint edge-to-edge (behind the top app bar / status bar) rather than being
 * clipped to the Scaffold content area.
 */
data class GlowColors(
    val accent: Color,
    val deep: Color,
    val radial: Color
)

val LocalSetGlowColors = compositionLocalOf<((GlowColors?) -> Unit)?> { null }

/**
 * Publishes [colors] to the root glow while this composition is active, and
 * clears it on dispose. Use from any screen that wants a full-screen tinted glow.
 */
@Composable
fun ApplyRootGlow(colors: GlowColors) {
    val setter = LocalSetGlowColors.current
    DisposableEffect(setter, colors) {
        setter?.invoke(colors)
        onDispose { setter?.invoke(null) }
    }
}

/**
 * Animated full-screen atmospheric glow. Same recipe as [AtmosphericGlowLayer]
 * (vertical gradient from top + radial bloom) but the three colors cross-fade
 * over 400 ms so palette swaps (Forecast area change, chat avatar switch)
 * recolor the page smoothly instead of jump-cutting.
 *
 * Alphas are halved in light theme so the glow doesn't bleach the chrome.
 */
@Composable
fun AnimatedAtmosphericGlow(
    accentColor: Color,
    modifier: Modifier = Modifier,
    deepColor: Color = accentColor,
    radialColor: Color = accentColor,
    durationMillis: Int = 400
) {
    val accent by animateColorAsState(accentColor, tween(durationMillis), label = "glowAccent")
    val deep by animateColorAsState(deepColor, tween(durationMillis), label = "glowDeep")
    val radial by animateColorAsState(radialColor, tween(durationMillis), label = "glowRadial")

    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
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
