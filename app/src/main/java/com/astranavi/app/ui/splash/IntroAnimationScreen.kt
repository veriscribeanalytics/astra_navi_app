package com.astranavi.app.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.R
import com.astranavi.app.ui.theme.LocalSemanticColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IntroAnimationScreen(
    isLoggedIn: Boolean = false,
    isFirstLaunch: Boolean = true,
    dashboardLoadReady: Boolean = false,
    onFetchData: () -> Unit = {},
    onIntroComplete: () -> Unit
) {
    val timeSecAnim = remember { Animatable(0f) }
    val totalDurationSec = when {
        isFirstLaunch && isLoggedIn -> 8.0f
        isFirstLaunch -> 6.0f
        isLoggedIn -> 3.0f
        else -> 2.0f
    }

    LaunchedEffect(Unit) {
        if (isLoggedIn) {
            onFetchData()
        }
        timeSecAnim.animateTo(
            targetValue = totalDurationSec,
            animationSpec = tween(durationMillis = (totalDurationSec * 1000).toInt(), easing = LinearEasing)
        )
        onIntroComplete()
    }

    val timeSec = timeSecAnim.value

    // Responsive Sizing
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val wheelSize = when {
        screenWidth < 360.dp -> screenWidth * 0.88f
        screenWidth < 600.dp -> screenWidth * 0.90f
        else -> 500.dp
    }
    val iconSize = when {
        screenWidth < 360.dp -> 38.dp
        screenWidth < 600.dp -> 44.dp
        else -> 48.dp
    }
    val glyphFontSize = when {
        screenWidth < 360.dp -> 14.sp
        screenWidth < 600.dp -> 16.sp
        else -> 18.sp
    }
    val brandFontSize = when {
        screenWidth < 360.dp -> 44f
        screenWidth < 600.dp -> 52f
        else -> 56f
    }
    val bottomPadding = when {
        screenHeight < 640.dp -> 48.dp
        else -> 72.dp
    }

    // Sub-progress definitions (Master Timeline)
    // First launch: full 6/8s cinematic staggered reveal.
    // Repeat launch: 2/3s with wheel + dual zodiac + chart geometry all revealing in parallel.
    val seedProgress: Float
    val wheelFormsProgress: Float
    val dualZodiacProgress: Float
    val chartEnergyProgress: Float
    val rashiFadeProgress: Float
    val revealProgress: Float
    val rotationProgress: Float
    val brandRevealStart: Float

    if (isFirstLaunch) {
        seedProgress = getSubProgress(timeSec, 0.0f, 0.8f)
        wheelFormsProgress = getSubProgress(timeSec, 0.8f, 1.8f)
        dualZodiacProgress = getSubProgress(timeSec, 1.8f, 3.4f)
        chartEnergyProgress = getSubProgress(timeSec, 3.4f, 4.7f)
        rashiFadeProgress = getSubProgress(timeSec, 4.7f, 5.1f)
        revealProgress = getSubProgress(timeSec, 5.1f, 6.1f)
        rotationProgress = getSubProgress(timeSec, 0.8f, totalDurationSec)
        brandRevealStart = 5.1f
    } else {
        // Compressed parallel reveal
        val combinedStart = 0.3f
        val combinedEnd = 1.2f
        seedProgress = getSubProgress(timeSec, 0.0f, 0.3f)
        wheelFormsProgress = getSubProgress(timeSec, combinedStart, combinedEnd)
        dualZodiacProgress = getSubProgress(timeSec, combinedStart, combinedEnd)
        chartEnergyProgress = getSubProgress(timeSec, combinedStart, combinedEnd)
        rashiFadeProgress = getSubProgress(timeSec, 1.2f, 1.5f)
        revealProgress = getSubProgress(timeSec, 1.2f, 2.2f)
        rotationProgress = getSubProgress(timeSec, combinedStart, totalDurationSec)
        brandRevealStart = 1.2f
    }

    // Custom Easing functions
    val EaseOutCubic = Easing { fraction ->
        val f = fraction - 1f
        f * f * f + 1f
    }
    val EaseInOutCubic = Easing { fraction ->
        if (fraction < 0.5f) {
            4f * fraction * fraction * fraction
        } else {
            val f = ((2f * fraction) - 2f)
            0.5f * f * f * f + 1f
        }
    }
    val EaseOutBack = Easing { fraction ->
        val s = 1.70158f
        val t = fraction - 1f
        t * t * ((s + 1f) * t + s) + 1f
    }

    val rotationAngle = 15f * rotationProgress

    val semanticColors = LocalSemanticColors.current
    val antiqueOrangishGold = semanticColors.antiqueGold
    val highlightGold = semanticColors.highlightGold
    val shadowGold = semanticColors.shadowGold
    val softGlowGold = semanticColors.softGlowGold

    // Wheel transitions to a 20% opacity background mark during reveal
    val wheelAlpha = if (revealProgress > 0f) {
        1.0f - (0.8f * EaseInOutCubic.transform(revealProgress))
    } else {
        1.0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onIntroComplete() },
        contentAlignment = Alignment.Center
    ) {
        // Outer wheel container (holds Canvas and Rashi icons)
        Box(
            modifier = Modifier
                .size(wheelSize)
                .graphicsLayer {
                    alpha = wheelAlpha
                },
            contentAlignment = Alignment.Center
        ) {
            // 1. Drawing geometric lines, rings, and dots
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)

                // Responsive Radii based on wheel size
                val outerRadius = size.width * 0.42f
                val middleRadius = size.width * 0.34f
                val innerRadius = size.width * 0.24f

                // A. Center Seed (0.0s - 0.8s)
                val pulse = sin(seedProgress * Math.PI.toFloat()).toFloat()
                val seedSize = (6.dp.toPx() + 18.dp.toPx() * pulse)
                val seedAlpha = if (chartEnergyProgress > 0f) 1.0f - chartEnergyProgress else 1.0f

                if (seedAlpha > 0f) {
                    // Central glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(softGlowGold.copy(alpha = 0.6f * seedAlpha), Color.Transparent),
                            center = center,
                            radius = seedSize * 2.5f
                        ),
                        radius = seedSize * 2.5f,
                        center = center
                    )

                    // Seed center point
                    drawCircle(
                        color = highlightGold.copy(alpha = seedAlpha),
                        radius = seedSize,
                        center = center
                    )
                }

                // B. Outer Wheel Draws (0.8s - 1.8s)
                if (wheelFormsProgress > 0f) {
                    val sweepAngle = 360f * EaseInOutCubic.transform(wheelFormsProgress)

                    // Outer Ring
                    drawArc(
                        color = antiqueOrangishGold,
                        startAngle = -90f + rotationAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - middleRadius, center.y - middleRadius),
                        size = Size(middleRadius * 2f, middleRadius * 2f),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Inner ring fade in
                    drawCircle(
                        color = antiqueOrangishGold.copy(alpha = wheelFormsProgress * 0.4f),
                        radius = innerRadius,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Divider lines (staggered with circle draw)
                    for (i in 0 until 12) {
                        val dividerAngle = (360f / 12f) * (i + 0.5f) - 90f + rotationAngle
                        val rad = Math.toRadians(dividerAngle.toDouble())
                        val startOffset = Offset(
                            center.x + innerRadius * cos(rad).toFloat(),
                            center.y + innerRadius * sin(rad).toFloat()
                        )
                        val endOffset = Offset(
                            center.x + middleRadius * cos(rad).toFloat(),
                            center.y + middleRadius * sin(rad).toFloat()
                        )

                        drawLine(
                            color = antiqueOrangishGold.copy(alpha = wheelFormsProgress * 0.35f),
                            start = startOffset,
                            end = endOffset,
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }

                // C. Guidance Lines & Aspect Geometry (3.4s - 4.7s)
                if (chartEnergyProgress > 0f) {
                    val geomAlpha = chartEnergyProgress * 0.3f

                    // Triangle 1: 0, 120, 240
                    val p1 = getPoint(center, innerRadius * 0.6f, 0f + rotationAngle)
                    val p2 = getPoint(center, innerRadius * 0.6f, 120f + rotationAngle)
                    val p3 = getPoint(center, innerRadius * 0.6f, 240f + rotationAngle)

                    drawLine(antiqueOrangishGold.copy(alpha = geomAlpha), p1, p2, strokeWidth = 1.5.dp.toPx())
                    drawLine(antiqueOrangishGold.copy(alpha = geomAlpha), p2, p3, strokeWidth = 1.5.dp.toPx())
                    drawLine(antiqueOrangishGold.copy(alpha = geomAlpha), p3, p1, strokeWidth = 1.5.dp.toPx())

                    // Triangle 2 (Purple aspect lines): 60, 180, 300
                    val softPurple = Color(0xFFC084FC)
                    val p4 = getPoint(center, innerRadius * 0.6f, 60f + rotationAngle)
                    val p5 = getPoint(center, innerRadius * 0.6f, 180f + rotationAngle)
                    val p6 = getPoint(center, innerRadius * 0.6f, 300f + rotationAngle)

                    drawLine(softPurple.copy(alpha = geomAlpha), p4, p5, strokeWidth = 1.5.dp.toPx())
                    drawLine(softPurple.copy(alpha = geomAlpha), p5, p6, strokeWidth = 1.5.dp.toPx())
                    drawLine(softPurple.copy(alpha = geomAlpha), p6, p4, strokeWidth = 1.5.dp.toPx())

                    // 4 Planet dots
                    val dotR = 4.5.dp.toPx() * EaseOutBack.transform(chartEnergyProgress)
                    val dotAngles = listOf(30f, 150f, 210f, 330f)
                    dotAngles.forEach { ang ->
                        val pt = getPoint(center, innerRadius * 0.6f, ang + rotationAngle)
                        drawCircle(highlightGold.copy(alpha = chartEnergyProgress), radius = dotR, center = pt)
                        drawCircle(softGlowGold.copy(alpha = chartEnergyProgress), radius = dotR * 0.5f, center = pt)
                    }
                }
            }

            // 2. Dual Zodiac Reveal (1.8s - 3.4s) & Clean Fade (4.7s - 5.1s)
            val context = LocalContext.current
            val rashiNames = remember {
                listOf(
                    "aries", "taurus", "gemini", "cancer", "leo", "virgo",
                    "libra", "scorpio", "sagittarius", "capricorn", "aquarius", "pisces"
                )
            }
            val rashiResIds = remember {
                rashiNames.map { name ->
                    context.resources.getIdentifier(name, "drawable", context.packageName)
                }
            }
            
            // Outer Rashi Icons (Clockwise Reveal on first launch; lockstep on repeat)
            rashiResIds.forEachIndexed { i, resId ->
                if (resId != 0 && dualZodiacProgress > 0f) {
                    val baseAngle = (360f / 12f) * i - 90f
                    val angleRad = Math.toRadians((baseAngle + rotationAngle).toDouble())

                    val iconIndividualProgress = if (isFirstLaunch) {
                        val staggerFraction = 0.4f
                        val step = (1.0f - staggerFraction) / 11f
                        val iconStart = i * step
                        val iconEnd = iconStart + staggerFraction
                        ((dualZodiacProgress - iconStart) / (iconEnd - iconStart)).coerceIn(0f, 1f)
                    } else {
                        dualZodiacProgress
                    }

                    val baseAlpha = EaseOutCubic.transform(iconIndividualProgress)
                    // Fade to ~20% during rashiFadeProgress (keeps icons ghosted behind brand reveal)
                    val finalAlpha = baseAlpha * (1.0f - 0.8f * rashiFadeProgress)
                    val iconScale = 0.6f + 0.4f * EaseOutBack.transform(iconIndividualProgress)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val radiusPx = (size.width * 0.42f)
                                translationX = radiusPx * cos(angleRad).toFloat()
                                translationY = radiusPx * sin(angleRad).toFloat()
                                scaleX = iconScale
                                scaleY = iconScale
                                alpha = finalAlpha
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            modifier = Modifier.size(iconSize),
                            colorFilter = ColorFilter.tint(antiqueOrangishGold, BlendMode.SrcAtop)
                        )
                    }
                }
            }

            // Inner Zodiac Glyphs (Anti-Clockwise Reveal)
            val zodiacGlyphs = remember {
                listOf("♈", "♉", "♊", "♋", "♌", "♍", "♎", "♏", "♐", "♑", "♒", "♓")
            }

            zodiacGlyphs.forEachIndexed { i, glyph ->
                if (dualZodiacProgress > 0f) {
                    val baseAngle = (360f / 12f) * i - 90f
                    val angleRad = Math.toRadians((baseAngle + rotationAngle).toDouble())

                    val glyphIndividualProgress = if (isFirstLaunch) {
                        val glyphStaggerDelay = 0.4f
                        val glyphStep = (1.0f - glyphStaggerDelay) / 11f
                        // Anti-clockwise stagger: index 0, 11, 10, 9...
                        val antiClockwiseIndex = if (i == 0) 0 else 12 - i
                        val glyphStart = antiClockwiseIndex * glyphStep
                        val glyphEnd = glyphStart + glyphStaggerDelay
                        ((dualZodiacProgress - glyphStart) / (glyphEnd - glyphStart)).coerceIn(0f, 1f)
                    } else {
                        dualZodiacProgress
                    }

                    val baseAlpha = EaseOutCubic.transform(glyphIndividualProgress)
                    // Fade to ~20% during rashiFadeProgress (keeps glyphs ghosted behind brand reveal)
                    val finalAlpha = baseAlpha * (1.0f - 0.8f * rashiFadeProgress)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val radiusPx = (size.width * 0.20f) // just inside inner ring
                                translationX = radiusPx * cos(angleRad).toFloat()
                                translationY = radiusPx * sin(angleRad).toFloat()
                                alpha = finalAlpha
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = glyph,
                            color = highlightGold,
                            fontSize = glyphFontSize,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Center Brand Reveal text
        AnimatedVisibility(
            visible = timeSec >= brandRevealStart,
            enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.85f, animationSpec = tween(500)),
            exit = fadeOut(tween(400))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                BrandMark(
                    brandColor = antiqueOrangishGold,
                    shadowColor = shadowGold,
                    fontSize = brandFontSize,
                    letterSpacing = 4f,
                    taglineSpacer = 10f
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                // Rotating loading text for Logged-In first launch (5.1s to 8.0s)
                // Single status string for repeat launches or logged-out.
                val statusText = when {
                    !isLoggedIn -> stringResource(R.string.intro_opening_login)
                    !isFirstLaunch -> stringResource(R.string.intro_aligning_dashboard)
                    dashboardLoadReady -> stringResource(R.string.intro_aligning_dashboard)
                    timeSec < 5.8f -> stringResource(R.string.intro_reading_chart)
                    timeSec < 6.5f -> stringResource(R.string.intro_checking_transits)
                    else -> stringResource(R.string.splash_default_loading)
                }

                Crossfade(
                    targetState = statusText,
                    animationSpec = tween(400),
                    label = "loading_text_fade"
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Light
                        ),
                        color = highlightGold.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 4. Bottom Poetic Text
        val bottomText = if (isFirstLaunch) {
            when {
                timeSec < 0.8f -> stringResource(R.string.intro_sky_waiting)
                timeSec < 1.8f -> stringResource(R.string.intro_sign_rhythm)
                timeSec < 3.4f -> stringResource(R.string.intro_cosmic_pattern)
                timeSec < 5.1f -> stringResource(R.string.intro_summary_guidance)
                else -> ""
            }
        } else {
            when {
                timeSec < brandRevealStart -> stringResource(R.string.intro_summary_guidance)
                else -> ""
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomPadding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = bottomText,
                animationSpec = tween(400),
                label = "intro_text_fade"
            ) { text ->
                if (text.isNotEmpty()) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 1.sp,
                            shadow = Shadow(
                                color = shadowGold.copy(alpha = 0.4f),
                                offset = Offset(0f, 2f),
                                blurRadius = 4f
                            )
                        ),
                        color = LocalSemanticColors.current.brandTextColor.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Helpers
private fun getSubProgress(timeSec: Float, startSec: Float, endSec: Float): Float {
    if (timeSec < startSec) return 0f
    if (timeSec > endSec) return 1f
    if (endSec == startSec) return 1f
    return (timeSec - startSec) / (endSec - startSec)
}

private fun getPoint(center: Offset, radius: Float, angleDegrees: Float): Offset {
    val rad = Math.toRadians(angleDegrees.toDouble())
    return Offset(
        (center.x + radius * cos(rad)).toFloat(),
        (center.y + radius * sin(rad)).toFloat()
    )
}
