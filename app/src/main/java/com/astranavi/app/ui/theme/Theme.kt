package com.astranavi.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class SemanticColors(
    val exalted: Color,
    val debilitated: Color,
    val normal: Color,
    val retrograde: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val antiqueGold: Color,
    val highlightGold: Color,
    val shadowGold: Color,
    val softGlowGold: Color,
    val brandTextColor: Color
)

val LightSemanticColors = SemanticColors(
    exalted = LightExalted,
    debilitated = LightDebilitated,
    normal = LightNormal,
    retrograde = LightRetrograde,
    glassSurface = LightGlassSurface,
    glassBorder = LightGlassBorder,
    heroGradientStart = LightHeroGradientStart,
    heroGradientEnd = LightHeroGradientEnd,
    antiqueGold = LightAntiqueGold,
    highlightGold = LightHighlightGold,
    shadowGold = LightShadowGold,
    softGlowGold = LightSoftGlowGold,
    brandTextColor = LightBrandTextColor
)

val DarkSemanticColors = SemanticColors(
    exalted = DarkExalted,
    debilitated = DarkDebilitated,
    normal = DarkNormal,
    retrograde = DarkRetrograde,
    glassSurface = DarkGlassSurface,
    glassBorder = DarkGlassBorder,
    heroGradientStart = DarkHeroGradientStart,
    heroGradientEnd = DarkHeroGradientEnd,
    antiqueGold = DarkAntiqueGold,
    highlightGold = DarkHighlightGold,
    shadowGold = DarkShadowGold,
    softGlowGold = DarkSoftGlowGold,
    brandTextColor = DarkBrandTextColor
)

val LocalSemanticColors = staticCompositionLocalOf { LightSemanticColors }

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    background = DarkBackground,
    onBackground = DarkForeground,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outlineVariant = DarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    background = LightBackground,
    onBackground = LightForeground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outlineVariant = LightOutlineVariant
)

@Composable
fun AstraNaviTheme(
    themePreference: String? = "system",
    darkTheme: Boolean = when (themePreference) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    },
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val semanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors
    val appTypography = rememberAppTypography()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalSemanticColors provides semanticColors,
        LocalAppTypography provides appTypography
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
