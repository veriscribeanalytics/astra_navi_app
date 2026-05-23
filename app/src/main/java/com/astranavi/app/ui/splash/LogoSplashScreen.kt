package com.astranavi.app.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.R
import com.astranavi.app.ui.theme.LocalSemanticColors
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LogoSplashScreen(
    isAuthLoading: Boolean = false,
    loadingText: String = "Preparing your first guidance…",
    onSplashComplete: () -> Unit
) {
    var minSplashTimeElapsed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000) // Splash duration: 2.0s
        minSplashTimeElapsed = true
    }

    LaunchedEffect(minSplashTimeElapsed, isAuthLoading) {
        if (minSplashTimeElapsed && !isAuthLoading) {
            onSplashComplete()
        }
    }

    val semanticColors = LocalSemanticColors.current
    val antiqueGold = semanticColors.antiqueGold
    val shadowGold = semanticColors.shadowGold
    val highlightGold = semanticColors.highlightGold

    val EaseInOutCubic = Easing { fraction ->
        if (fraction < 0.5f) {
            4f * fraction * fraction * fraction
        } else {
            val f = ((2f * fraction) - 2f)
            0.5f * f * f * f + 1f
        }
    }
    val EaseOutQuad = Easing { fraction -> fraction * (2f - fraction) }
    val EaseInOutQuad = Easing { fraction ->
        if (fraction < 0.5f) 2f * fraction * fraction
        else -1f + (4f - 2f * fraction) * fraction
    }

    val ringRotation = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "logoGlow")
    val logoGlow by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoGlow"
    )

    LaunchedEffect(Unit) {
        ringRotation.animateTo(360f, tween(2000, easing = EaseInOutCubic))
    }

    val glowColor = MaterialTheme.colorScheme.secondary

    val displayText = if (loadingText == "Preparing your first guidance…") {
        stringResource(R.string.splash_default_loading)
    } else {
        loadingText
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Canvas for rotating ring, outline, dots, and soft theme glow
        Canvas(modifier = Modifier.size(260.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width * 0.38f

            // Theme-aware soft glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = logoGlow * 0.35f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.6f
                ),
                radius = radius * 1.6f,
                center = center
            )

            // Small golden wheel outline (faint premium mark at 20% opacity)
            drawCircle(
                color = antiqueGold.copy(alpha = 0.20f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 12 tiny dots around the wheel outline (faint premium mark at 20% opacity)
            for (i in 0 until 12) {
                val angle = (360f / 12f) * i - 90f
                val rad = Math.toRadians(angle.toDouble())
                val dotPos = Offset(
                    center.x + radius * cos(rad).toFloat(),
                    center.y + radius * sin(rad).toFloat()
                )
                drawCircle(
                    color = antiqueGold.copy(alpha = 0.20f),
                    radius = 2.dp.toPx(),
                    center = dotPos
                )
            }

            // One rotating gold arc (visible gold)
            drawArc(
                color = antiqueGold.copy(alpha = 0.9f),
                startAngle = ringRotation.value - 90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // App Logo / Text in Center
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BrandMark(
                brandColor = antiqueGold,
                shadowColor = shadowGold,
                fontSize = 42f,
                letterSpacing = 2f,
                taglineSpacer = 6f
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = displayText,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Light
                ),
                color = highlightGold.copy(alpha = 0.7f)
            )
        }
    }
}
