package com.astranavi.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.astranavi.app.ui.components.responsiveFontSizes

/**
 * AstraNavi Semantic Typography System
 *
 * "One Truth Only" - Each semantic style contains:
 * - fontSize (responsive)
 * - fontWeight
 * - lineHeight
 * - letterSpacing
 * - fontFamily
 *
 * Screen code should NEVER override fontSize.
 * Use: Text(text = "79", style = typography.heroScore)
 */
data class AppTypography(
    val heroScore: TextStyle,
    val pageTitle: TextStyle,
    val sectionHeader: TextStyle,
    val cardTitle: TextStyle,
    val cardBody: TextStyle,
    val cardValue: TextStyle,
    val microLabel: TextStyle,
    val badgeText: TextStyle,
    val buttonLabel: TextStyle,
    val chartLabel: TextStyle,
    val bottomNavLabel: TextStyle,
    val technicalText: TextStyle
)

@Composable
fun rememberAppTypography(): AppTypography {
    val sizes = responsiveFontSizes()

    return remember(sizes) {
        AppTypography(
            // Hero scores (Dashboard: 79, 85, etc.)
            heroScore = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Black,
                fontSize = sizes.heroScore,
                lineHeight = (sizes.heroScore.value * 1.15f).sp,
                letterSpacing = 0.sp
            ),

            // Page titles (Screen headers, major titles)
            pageTitle = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = sizes.pageTitle,
                lineHeight = (sizes.pageTitle.value * 1.2f).sp,
                letterSpacing = 0.sp
            ),

            // Section headers (TODAY OVERVIEW, DOMINANT INFLUENCE)
            sectionHeader = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.SemiBold,
                fontSize = sizes.sectionHeader,
                lineHeight = (sizes.sectionHeader.value * 1.3f).sp,
                letterSpacing = 1.sp
            ),

            // Card titles (Love, Career, Health)
            cardTitle = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = sizes.cardTitle,
                lineHeight = (sizes.cardTitle.value * 1.25f).sp,
                letterSpacing = 0.sp
            ),

            // Card body text (Descriptions, paragraphs)
            cardBody = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = sizes.cardBody,
                lineHeight = (sizes.cardBody.value * 1.4f).sp,
                letterSpacing = 0.25.sp
            ),

            // Card values (85%, 3 days, scores)
            cardValue = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.ExtraBold,
                fontSize = sizes.cardValue,
                lineHeight = (sizes.cardValue.value * 1.15f).sp,
                letterSpacing = 0.sp
            ),

            // Micro labels (Small uppercase labels, decorative)
            microLabel = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = sizes.microLabel,
                lineHeight = (sizes.microLabel.value * 1.3f).sp,
                letterSpacing = 1.sp
            ),

            // Badge text (Chips, badges like Rahu Kaal)
            badgeText = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.SemiBold,
                fontSize = sizes.badgeText,
                lineHeight = (sizes.badgeText.value * 1.25f).sp,
                letterSpacing = 0.5.sp
            ),

            // Button labels (Ask Navi, CTA buttons)
            buttonLabel = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = sizes.buttonLabel,
                lineHeight = (sizes.buttonLabel.value * 1.2f).sp,
                letterSpacing = 0.5.sp
            ),

            // Chart labels (Chart annotations, axis labels)
            chartLabel = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = sizes.chartLabel,
                lineHeight = (sizes.chartLabel.value * 1.2f).sp,
                letterSpacing = 0.sp
            ),

            // Bottom nav labels (Navigation text)
            bottomNavLabel = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = sizes.bottomNavLabel,
                lineHeight = (sizes.bottomNavLabel.value * 1.2f).sp,
                letterSpacing = 0.5.sp
            ),

            // Technical text (Timestamps, metadata, system info)
            technicalText = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = sizes.technicalText,
                lineHeight = (sizes.technicalText.value * 1.35f).sp,
                letterSpacing = 0.25.sp
            )
        )
    }
}

val LocalAppTypography = staticCompositionLocalOf<AppTypography> {
    error("No AppTypography provided. Wrap your content with AstraNaviTheme.")
}
