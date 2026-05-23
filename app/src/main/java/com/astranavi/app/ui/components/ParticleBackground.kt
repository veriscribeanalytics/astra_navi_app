package com.astranavi.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import com.astranavi.app.ui.theme.LocalSemanticColors
import kotlinx.coroutines.isActive
import kotlin.math.sin
import kotlin.random.Random

val LocalBackgroundScrollState = staticCompositionLocalOf<ScrollState?> { null }

@Composable
fun ParticleBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 180
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgColor = MaterialTheme.colorScheme.background
    val scrollState = LocalBackgroundScrollState.current
    
    val systemAlpha = remember { Animatable(1f) }
    
    Box(modifier = modifier.fillMaxSize().graphicsLayer { alpha = systemAlpha.value }) {
        if (isDark) {
            NebulaGradients()
        } else {
            val semanticColors = LocalSemanticColors.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                semanticColors.heroGradientStart.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = Offset(0f, 600f)
                        )
                    )
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                semanticColors.heroGradientEnd.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            center = Offset(1000f, 1000f)
                        )
                    )
            )
        }
        
        ParticleCanvas(particleCount, isDark, bgColor, scrollState)
    }
}

@Composable
private fun NebulaGradients() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A1230).copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(0f, 0f)
                )
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2E1A47).copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(1000f, 1000f)
                )
            )
    )
}

@Composable
private fun ParticleCanvas(
    particleCount: Int, 
    isDark: Boolean, 
    bgColor: Color,
    scrollState: ScrollState?
) {
    val gold = Color(0xFFC8880A)
    val white = Color.White
    val darkPurple = Color(0xFF2A1A4A)
    val lavender = Color(0xFFB8A9E8)
    
    val particles = remember { mutableListOf<Particle>() }
    var timeMillis by remember { mutableStateOf(0L) }

    LaunchedEffect(isDark) {
        if (particles.isNotEmpty()) {
            particles.forEachIndexed { index, p ->
                p.color = if (isDark) {
                    if (index % 2 == 0) white else gold
                } else {
                    if (index % 2 == 0) darkPurple else gold
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameMillis { frameTime ->
                timeMillis = frameTime
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val currentTime = timeMillis / 1000f
        val width = size.width
        val height = size.height
        val scrollOffset = scrollState?.value ?: 0

        if (width <= 0f || height <= 0f) return@Canvas

        if (particles.isEmpty()) {
            repeat(particleCount) {
                val color = if (isDark) {
                    if (it % 2 == 0) white else gold
                } else {
                    if (it % 2 == 0) darkPurple else gold
                }

                // Random depth from 0.2 to 1.0 (parallax multiplier)
                val depth = Random.nextFloat() * 0.8f + 0.2f

                particles.add(
                    Particle(
                        x = Random.nextFloat() * width,
                        y = Random.nextFloat() * height,
                        vx = (Random.nextFloat() - 0.5f) * 0.4f, // Slower drift
                        vy = (Random.nextFloat() - 0.5f) * 0.4f,
                        radius = (Random.nextFloat() * 4.0f + 1.5f) * depth,
                        baseAlpha = (Random.nextFloat() * 0.4f + 0.1f) * depth,
                        phase = Random.nextFloat() * 20f,
                        blinkSpeed = Random.nextFloat() * 1.2f + 0.4f,
                        color = color,
                        depth = depth
                    )
                )
            }
        }

        particles.forEach { p ->
            // Drift
            p.x += p.vx
            p.y += p.vy

            // Continuous wrap-around
            if (p.x < 0) p.x = width
            if (p.x > width) p.x = 0f
            if (p.y < 0) p.y = height
            if (p.y > height) p.y = 0f

            val blink = sin(currentTime * p.blinkSpeed + p.phase).toFloat()
            val currentAlpha = (p.baseAlpha + 0.15f * blink).coerceIn(0.05f, 1f)
            
            // Apply parallax offset based on scroll
            val parallaxY = (p.y - scrollOffset * p.depth) % height
            val finalY = if (parallaxY < 0) parallaxY + height else parallaxY

            drawCircle(
                color = p.color.copy(alpha = currentAlpha),
                radius = p.radius,
                center = Offset(p.x, finalY)
            )
        }
    }
}

private class Particle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float,
    val baseAlpha: Float,
    val phase: Float,
    val blinkSpeed: Float,
    var color: Color,
    val depth: Float
)

