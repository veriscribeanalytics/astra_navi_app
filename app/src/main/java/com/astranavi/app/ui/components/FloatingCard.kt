package com.astranavi.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.astranavi.app.ui.theme.AstroColors

/**
 * Soft ivory cosmic floating card style:
 * - subtle wide spot-shadow (no harsh elevation)
 * - thin theme-aware gold/onyx border
 * - rounded corners + clipped surface fill
 *
 * Pass an accent color to tint the spot-shadow (e.g. score tone, planet color).
 * Pass null to use the default floating glow token.
 *
 * Theme detection uses the resolved colorScheme.background luminance so the
 * in-app theme preference (light/dark/system) is always honored, not just
 * the OS setting.
 */
@Composable
fun Modifier.floatingCard(
    accent: Color? = null,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 6.dp,
    glowAlpha: Float = 0.22f,
    borderAlpha: Float = 0.30f,
    surfaceAlpha: Float = 0.80f,
): Modifier {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val glowColor = (accent ?: if (dark) AstroColors.DarkFloatingGlow else AstroColors.LightFloatingGlow)
        .copy(alpha = glowAlpha)
    val borderColor = (accent ?: if (dark) AstroColors.DarkFloatingBorder else AstroColors.LightFloatingBorder)
        .copy(alpha = borderAlpha)
    val surface = (if (dark) AstroColors.DarkFloatingSurface else AstroColors.LightFloatingSurface)
        .copy(alpha = surfaceAlpha)
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color.Transparent,
            spotColor = glowColor
        )
        .clip(shape)
        .background(surface, shape)
        .border(BorderStroke(1.dp, borderColor), shape)
}
