package com.astranavi.app.ui.dashboard

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.decode.SvgDecoder
import com.astranavi.app.ui.components.DashaTimelineItem
import com.astranavi.app.data.model.*
import java.util.*
import com.astranavi.app.util.ZodiacMapper
import com.astranavi.app.ui.components.shimmerEffect
import com.astranavi.app.ui.components.shimmerSweepEffect
import com.astranavi.app.ui.theme.AstroColors
import com.astranavi.app.ui.components.ScoreColors
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.min

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.nativeCanvas
import com.astranavi.app.BuildConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToRashis: (String?) -> Unit = {},
    onNavigateToExperts: () -> Unit = {},
    onNavigateToChat: (String?) -> Unit = {},
    onNavigateToKundli: () -> Unit = {},
    onNavigateToMatch: () -> Unit = {},
    onNavigateToMatchHistory: () -> Unit = {},
    onNavigateToNakshatras: () -> Unit = {},
    onNavigateToPlanets: () -> Unit = {},
    onNavigateToConsult: () -> Unit = {},
    onNavigateToForecast: (String?) -> Unit = {}
) {
    val uiState = viewModel.uiState.value
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.fetchDashboardData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // High-performance background with parallax tie-in
        com.astranavi.app.ui.components.ParticleBackground(
            scrollState = scrollState,
            particleCount = 150
        )

        when (uiState) {
            is DashboardState.Loading -> {
                DashboardSkeleton()
            }
            is DashboardState.Success -> {
                DashboardContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    scrollState = scrollState,
                    onNavigateToExperts = onNavigateToExperts,
                    onNavigateToChat = onNavigateToChat,
                    onNavigateToKundli = onNavigateToKundli,
                    onNavigateToRashis = onNavigateToRashis,
                    onNavigateToMatch = onNavigateToMatch,
                    onNavigateToMatchHistory = onNavigateToMatchHistory,
                    onNavigateToNakshatras = onNavigateToNakshatras,
                    onNavigateToPlanets = onNavigateToPlanets,
                    onNavigateToConsult = onNavigateToConsult,
                    onNavigateToForecast = onNavigateToForecast
                )
            }
            is DashboardState.Error -> {
                ErrorView(message = uiState.message, onRetry = { viewModel.fetchDashboardData() })
            }
        }
    }
}

@Composable
fun DashboardContent(
    uiState: DashboardState.Success,
    viewModel: DashboardViewModel,
    scrollState: ScrollState,
    onNavigateToExperts: () -> Unit,
    onNavigateToChat: (String?) -> Unit,
    onNavigateToKundli: () -> Unit,
    onNavigateToRashis: (String?) -> Unit,
    onNavigateToMatch: () -> Unit,
    onNavigateToMatchHistory: () -> Unit,
    onNavigateToNakshatras: () -> Unit,
    onNavigateToPlanets: () -> Unit,
    onNavigateToConsult: () -> Unit,
    onNavigateToForecast: (String?) -> Unit
) {
    val horoscope = uiState.horoscope

    // --- ANIMATION STATE FOR ENTRY SEQUENCE ---
    val headerAlpha = remember { Animatable(0f) }
    val headerSlide = remember { Animatable(30f) }
    val zodiacScale = remember { Animatable(0.92f) }
    val orbitExpansion = remember { Animatable(0.85f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Staggered cinematic sequence - Expedited
        launch {
            delay(100)
            headerAlpha.animateTo(1f, tween(400, easing = EaseOutCubic))
        }
        launch {
            delay(100)
            headerSlide.animateTo(0f, tween(500, easing = EaseOutCubic))
        }
        launch {
            delay(200)
            zodiacScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }
        launch {
            delay(300)
            orbitExpansion.animateTo(1f, tween(600, easing = EaseInOutQuart))
        }
        launch {
            delay(100)
            contentAlpha.animateTo(1f, tween(400))
        }
    }

    val isLightMode = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val generalPalette = remember(horoscope.score.overall, isLightMode) {
        ScoreColors.paletteFor("general", horoscope.score.overall, isDarkTheme = !isLightMode)
    }
    val themeColor = if (isLightMode) generalPalette.main else generalPalette.glow
    val weeklyForecastColor = remember(uiState.forecast, isLightMode) {
        val forecast = uiState.forecast
        val todayAreaScore = forecast?.today_scores?.get(forecast.area)
        if (forecast != null && todayAreaScore != null) {
            val palette = ScoreColors.paletteFor(forecast.area, todayAreaScore, isDarkTheme = !isLightMode)
            if (isLightMode) palette.main else palette.glow
        } else {
            null
        }
    } ?: themeColor

    Column(modifier = Modifier.fillMaxSize()) {
        if (horoscope.engagement?.streak != null) {
            StreakBar(streak = horoscope.engagement.streak)
        }

        var overlayState by remember { mutableStateOf<HomeOverlayState>(HomeOverlayState.None) }
        val isExpanded = overlayState is HomeOverlayState.Expanded

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header Section with Entry & Parallax
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val scrollValue = scrollState.value.toFloat()
                            // Fade out and scale down as it exits top
                            alpha = (headerAlpha.value * (1f - (scrollValue / 600f))).coerceIn(0f, 1f)
                            scaleX = (1f - (scrollValue / 5000f)).coerceIn(0.95f, 1f)
                            scaleY = (1f - (scrollValue / 5000f)).coerceIn(0.95f, 1f)
                            translationY = headerSlide.value + (scrollValue * 0.15f) // Parallax
                        }
                ) {
                    HeaderSection(
                        metaDate = horoscope.meta?.date_display,
                        userName = horoscope.user?.name ?: uiState.userName,
                        moonSign = uiState.moonSign,
                        sunSign = uiState.sunSign,
                        planetary = horoscope.planetary,
                        isPersonalized = horoscope.system?.is_personalized == true,
                        themeColor = themeColor,
                        zodiacScale = zodiacScale.value,
                        selectedBadgeLabel = (overlayState as? HomeOverlayState.RashiDetail)?.label,
                        onBadgeClick = { signId, signName, label, color ->
                            overlayState = if ((overlayState as? HomeOverlayState.RashiDetail)?.label == label) {
                                HomeOverlayState.None
                            } else {
                                HomeOverlayState.RashiDetail(signId, signName, label, color)
                            }
                        },
                        onReadMoreClick = { onNavigateToRashis(it) }
                    )
                }

                // Main body wrapper for remaining elements with entrance fade
                Column(
                    modifier = Modifier
                        .graphicsLayer { alpha = contentAlpha.value }
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 2. TOP HIGHLIGHTS (Transit + Lucky)
                    TopHighlightsRow(horoscope = horoscope)

                    // 3. YOUR DAY TODAY (Centered)
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "YOUR DAY TODAY",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    // 4. TIP & NAVI (Golden Action)
                    TipNaviRow(horoscope = horoscope, onChat = onNavigateToChat)

                    // 5. INTERACTION HINT (Text only)
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Tap an orbit to explore today's energy",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            letterSpacing = 0.5.sp
                        )
                    }

                    // 6. YOUR COSMIC ORBIT with expansion animation
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = orbitExpansion.value
                                scaleY = orbitExpansion.value
                                alpha = orbitExpansion.value
                            }
                    ) {
                        OrbitSystem(
                            horoscope = horoscope,
                            themeColor = themeColor,
                            overlayState = overlayState,
                            onOverlayStateChange = { overlayState = it },
                            onActionClick = { action, data ->
                                when (action) {
                                    "chat" -> onNavigateToChat(data as? String)
                                    "forecast" -> onNavigateToForecast(data as? String)
                                }
                            }
                        )
                    }

                    // --- NEW SECTIONS BELOW ---

                    // 7. Weekly Forecast
                    if (uiState.forecast != null) {
                        Column(
                            modifier = Modifier.graphicsLayer {
                                val scrollValue = scrollState.value.toFloat()
                                val triggerPos = 500f
                                alpha = if (scrollValue > triggerPos) ((scrollValue - triggerPos) / 200f).coerceIn(0f, 1f) else 0f
                                translationY = (15f * (1f - alpha)).coerceAtLeast(0f)
                            }
                        ) {
                            SectionHeader("WEEK AHEAD")
                            WeeklyForecastSection(uiState.forecast, weeklyForecastColor, onNavigateToForecast)
                        }
                    }

                    // 8. Kundli Quick Peek
                    if (uiState.kundliPreview != null) {
                        Column(
                            modifier = Modifier.graphicsLayer {
                                val scrollValue = scrollState.value.toFloat()
                                val triggerPos = 900f
                                alpha = if (scrollValue > triggerPos) ((scrollValue - triggerPos) / 200f).coerceIn(0f, 1f) else 0f
                                translationY = (15f * (1f - alpha)).coerceAtLeast(0f)
                            }
                        ) {
                            KundliPeekCard(
                                data = uiState.kundliPreview, 
                                userEmail = uiState.userEmail,
                                accessToken = uiState.accessToken,
                                onClick = onNavigateToKundli
                            )
                        }
                    }

                    // 11. Consult Teaser
                    if (!uiState.recentConsultations.isNullOrEmpty()) {
                        Column(
                            modifier = Modifier.graphicsLayer {
                                val scrollValue = scrollState.value.toFloat()
                                val triggerPos = 1600f
                                alpha = if (scrollValue > triggerPos) ((scrollValue - triggerPos) / 200f).coerceIn(0f, 1f) else 0f
                                translationY = (15f * (1f - alpha)).coerceAtLeast(0f)
                            }
                        ) {
                            ConsultTeaserCard(uiState.recentConsultations.first(), onNavigateToConsult)
                        }
                    }

                    if (horoscope.engagement?.streak != null) {
                        StreakCard(streak = horoscope.engagement.streak)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // 5. POPUP OVERLAY (Top Layer - NO BLUR)
            HomePopupOverlay(
                overlayState = overlayState,
                horoscope = horoscope,
                onDismiss = { overlayState = HomeOverlayState.None },
                onChatClick = onNavigateToChat,
                onForecastClick = onNavigateToForecast,
                onNavigateToRashis = onNavigateToRashis
            )
        }
    }
}

@Composable
fun HeaderSection(
    metaDate: String?,
    userName: String?,
    moonSign: String?,
    sunSign: String?,
    planetary: PlanetaryData?,
    isPersonalized: Boolean,
    themeColor: Color,
    zodiacScale: Float = 1f,
    selectedBadgeLabel: String? = null,
    onBadgeClick: (String, String, String, Color) -> Unit = { _, _, _, _ -> },
    onReadMoreClick: (String) -> Unit = {}
) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..20 -> "Good Evening"
        else -> "Good Night"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(16.dp).height(2.dp).background(themeColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = (metaDate ?: "TODAY").uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 2.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$greeting,",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            val nameGradientBrush = Brush.horizontalGradient(
                colors = listOf(
                    themeColor,
                    lerp(themeColor, Color.White, 0.35f)
                )
            )
            Text(
                text = userName ?: "Seeker",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    brush = nameGradientBrush
                )
            )
            if (isPersonalized) {
                Surface(
                    color = themeColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "✨ PERSONALIZED",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = themeColor,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Box(modifier = Modifier.graphicsLayer { 
            scaleX = zodiacScale
            scaleY = zodiacScale
        }) {
            IdentityBadges(moonSign, sunSign, planetary, selectedBadgeLabel, onBadgeClick, onReadMoreClick)
        }
    }
}


@Composable
fun IdentityBadges(
    moonSign: String?, 
    sunSign: String?, 
    planetary: PlanetaryData?,
    selectedBadgeLabel: String?,
    onBadgeClick: (String, String, String, Color) -> Unit,
    onReadMoreClick: (String) -> Unit
) {
    val dominantColor = planetary?.dominant_planet?.let { AstroColors.getPlanetaryColor(it) } ?: AstroColors.Moon
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        BadgeCircle(
            label = "Moon", 
            sign = moonSign, 
            textColor = AstroColors.Moon, 
            ringColor = dominantColor,
            isSelected = selectedBadgeLabel == "Moon",
            onReadMoreClick = { moonSign?.let { onReadMoreClick(it) } }
        ) {
            if (moonSign != null) onBadgeClick(moonSign, ZodiacMapper.getDisplayName(moonSign), "Moon", AstroColors.Moon)
        }
        BadgeCircle(
            label = "Sun", 
            sign = sunSign, 
            textColor = AstroColors.Sun, 
            ringColor = AstroColors.Sun,
            isSelected = selectedBadgeLabel == "Sun",
            onReadMoreClick = { sunSign?.let { onReadMoreClick(it) } }
        ) {
            if (sunSign != null) onBadgeClick(sunSign, ZodiacMapper.getDisplayName(sunSign), "Sun", AstroColors.Sun)
        }
    }
}

@Composable
fun BadgeCircle(
    label: String, 
    sign: String?, 
    textColor: Color, 
    ringColor: Color, 
    isSelected: Boolean = false,
    onReadMoreClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val englishName = ZodiacMapper.getEnglishName(sign)
    val displayName = ZodiacMapper.getDisplayName(sign)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = if (isSelected) onReadMoreClick else onClick
            )
    ) {
        // Label first — MOON / SUN
        Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(3.dp))
        // Symbol circle
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(
                    width = if (isSelected) 3.dp else 2.dp, 
                    color = if (isSelected) ringColor else ringColor.copy(alpha = 0.5f), 
                    shape = CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Background Image/Icon (blurred if selected)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .blur(if (isSelected) 6.dp else 0.dp),
                contentAlignment = Alignment.Center
            ) {
                if (englishName != null) {
                    val resId = context.resources.getIdentifier(englishName, "drawable", context.packageName)
                    if (resId != 0) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(resId).crossfade(true).build(),
                            contentDescription = displayName,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(displayName.take(1), fontWeight = FontWeight.Bold, color = textColor)
                    }
                } else {
                    Icon(Icons.Default.Star, contentDescription = null, tint = ringColor.copy(alpha = 0.3f))
                }
            }
            
            // Selected Overlay (READ MORE)
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "READ\nMORE",
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 13.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        // Name last — e.g. "Mithun"
        Text(displayName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textColor.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun TipSection(tip: TipData) {
    val isPositive = tip.type == "positive"
    val color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF59E0B)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = tip.text,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TopHighlightsRow(horoscope: HoroscopeResponse) {
    val activeTrigger = remember(horoscope.time_triggers) {
        findActiveOrUpcomingTrigger(horoscope.time_triggers)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (activeTrigger != null) {
            ActiveGuidanceCard(trigger = activeTrigger)
        }

        if (horoscope.lucky != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lucky Color Badge
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎨", fontSize = 10.sp)
                        Text(
                            horoscope.lucky.color.uppercase(),
                            modifier = Modifier.padding(start = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = AstroColors.Jupiter
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Lucky Number Badge
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎲", fontSize = 10.sp)
                        Text(
                            "${horoscope.lucky.number}",
                            modifier = Modifier.padding(start = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = AstroColors.Sun
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TipNaviRow(horoscope: HoroscopeResponse, onChat: (String?) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "navi_button_scale")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.weight(2.2f)) {
            if (horoscope.tip != null) {
                Surface(
                    modifier = Modifier
                        .shadow(
                            elevation = 1.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = Color.Transparent,
                            spotColor = Color.Black.copy(alpha = 0.18f)
                        ),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💡", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            horoscope.tip.text,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        
        Button(
            onClick = { onChat("Give me cosmic guidance for my day.") },
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .align(Alignment.CenterVertically)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shimmerSweepEffect(),
            interactionSource = interactionSource,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AstroColors.Sun),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(6.dp))
            Text("ASK NAVI", fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun findActiveOrUpcomingTrigger(triggers: List<TimeTrigger>?): TimeTrigger? {
    if (triggers == null) return null
    val now = Calendar.getInstance()
    val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    
    fun parseToMinutes(timeStr: String): Int {
        return try {
            val parts = timeStr.split(":")
            parts[0].trim().toInt() * 60 + parts[1].trim().split(" ")[0].toInt()
        } catch (e: Exception) { 0 }
    }

    // 1. Current
    val active = triggers.find {
        val start = parseToMinutes(it.start)
        val end = parseToMinutes(it.end)
        currentMinutes in start..end
    }
    if (active != null) return active
    
    // 2. Upcoming
    return triggers.filter { parseToMinutes(it.start) > currentMinutes }
                   .minByOrNull { parseToMinutes(it.start) }
}



@Composable
fun LuckyStatsRow(lucky: LuckyData) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Lucky Color
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🎨", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("COLOR", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(lucky.color, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = AstroColors.Jupiter)
                }
            }
        }
        
        // Lucky Number
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🎲", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("NUMBER", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${lucky.number}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = AstroColors.Sun)
                }
            }
        }
    }
}


@Composable
fun ActiveGuidanceCard(trigger: TimeTrigger) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Transparent,
                spotColor = Color.Black.copy(alpha = 0.18f)
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(trigger.label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${trigger.start} - ${trigger.end}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            
            Text(
                text = trigger.advice.uppercase(),
                modifier = Modifier.weight(1.5f).padding(start = 8.dp),
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                textAlign = TextAlign.End,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun InsightList(horoscope: HoroscopeResponse) {
    val explanations = horoscope.astro_explanations?.items?.take(2)
    val secondaryAlerts = horoscope.alerts?.secondary?.take(2)
    
    if (explanations == null && secondaryAlerts == null) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader("TRANSITS & TRENDS")
        
        explanations?.forEach { item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Text("🔭", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(item.importance.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Text(item.technical, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(item.simple, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        
        secondaryAlerts?.forEach { alert ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(alert.simple, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 3.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 16.dp),
            fontWeight = FontWeight.Black
        )
        Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
    }
}

@Composable
fun StreakBar(streak: StreakData) {
    val progress = min(streak.current / 7f, 1f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(AstroColors.Sun.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .border(1.dp, AstroColors.Sun.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🔥", fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text("COSMIC STREAK", style = MaterialTheme.typography.labelSmall, color = AstroColors.Sun, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = AstroColors.Sun,
            trackColor = AstroColors.Sun.copy(alpha = 0.1f)
        )

        Spacer(modifier = Modifier.width(12.dp))
        Text("${streak.current} / 7", style = MaterialTheme.typography.labelSmall, color = AstroColors.Sun, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun StreakCard(streak: StreakData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Transparent,
                spotColor = Color.Black.copy(alpha = 0.18f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, AstroColors.Sun.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔥", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text("YOUR COSMIC STREAK", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = AstroColors.Sun)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("${streak.current} Days", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black))

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val days = listOf("M", "T", "W", "T", "F", "S", "S")
                days.forEachIndexed { index, day ->
                    val isCompleted = index < streak.current
                    val bgColor = if (isCompleted) AstroColors.Sun else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    val textColor = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

                    Box(
                        modifier = Modifier.size(32.dp).background(bgColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(day, fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (streak.reward != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(color = AstroColors.Sun.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        streak.reward,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        color = AstroColors.Sun,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class OrbitItem(
    val id: String,
    val label: String,
    val icon: String,
    val color: Color,
    val size: Dp = 70.dp,
    val glowColor: Color = color,
    val pulse: Boolean = false,
    val score: Int? = null,
    val subtitle: String? = null, // short descriptor shown in bubble
    val data: Any? = null
)

sealed class HomeOverlayState {
    object None : HomeOverlayState()
    data class Expanded(
        val item: OrbitItem,
        val origin: Offset
    ) : HomeOverlayState()
    data class RashiDetail(
        val signId: String,
        val signName: String,
        val label: String,
        val color: Color
    ) : HomeOverlayState()
}

@Composable
fun OrbitBubble(item: OrbitItem, isSelected: Boolean = false, isFaded: Boolean = false, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 1. FAST SCALE FEEDBACK (120ms)
    val targetScale = when {
        isSelected -> 1.05f
        isPressed -> 0.94f
        isFaded -> 0.96f
        else -> 1f
    }
    val scale by animateFloatAsState(targetScale, animationSpec = tween(120), label = "bubble_scale")
    val fadeAlpha by animateFloatAsState(if (isFaded) 0.4f else 1f, animationSpec = tween(250), label = "bubble_fade")

    // 2. MICRO-FLOATING MOTION (Staggered by ID)
    val floatDuration = remember(item.id) { 
        when(item.id) {
            "love" -> 2600
            "health" -> 2900
            "career" -> 3200
            "finance" -> 2750
            else -> 3000
        }
    }
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_motion")
    val translationY by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(floatDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    // Pulse for specific items (Alerts)
    val pulseAlpha by if (item.pulse) {
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val orbitColor = item.color
    val orbitGlow = item.glowColor
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(item.size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = fadeAlpha
                this.translationY = translationY
            }
            .drawBehind {
                // Radial glow around bubble
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            orbitGlow.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    )
                )
            }
            // OPAQUE FILL
            .background(orbitColor, CircleShape)
            // SHADOW UNDER ORBIT — bottom-right only
            .shadow(
                elevation = 2.dp,
                shape = CircleShape,
                ambientColor = Color.Transparent,
                spotColor = orbitGlow.copy(alpha = 0.35f)
            )
            // INNER MAIN RING
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = if (item.pulse) pulseAlpha else 0.25f),
                shape = CircleShape
            )
            // OUTER FAINT RING
            .padding(2.dp)
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(6.dp)
        ) {
            // Icon - Hidden for life areas (Career, Love, Health, Finance) per request
            val showIcon = item.id !in listOf("career", "love", "health", "finance")
            if (showIcon && item.icon.isNotEmpty()) {
                Text(
                    text = item.icon,
                    fontSize = (item.size.value * 0.28f).sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            
            // Label — white on opaque fill
            Text(
                text = item.label.uppercase(),
                fontSize = (item.size.value * 0.11f).sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            // Score — white on opaque fill
            if (item.score != null) {
                Text(
                    text = "${item.score}",
                    fontSize = (item.size.value * 0.22f).sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            } else if (item.subtitle != null) {
                Text(
                    text = item.subtitle,
                    fontSize = (item.size.value * 0.11f).sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─── ORBIT RING ──────────────────────────────────────────────────────────────
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun OrbitRing(
    items: List<OrbitItem>,
    radius: Dp,
    rotation: Float,
    selectedId: String? = null,
    onClick: (OrbitItem, Offset) -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val radiusPx = with(density) { radius.toPx() }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val centerX = with(density) { maxWidth.toPx() } / 2f
        val centerY = with(density) { maxHeight.toPx() } / 2f

        items.forEachIndexed { index, item ->
            val bubbleSizePx = with(density) { item.size.toPx() }
            val offset = getOrbitOffset(index, items.size, radiusPx, centerX, centerY, rotation)

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (offset.x - bubbleSizePx / 2f).toInt(),
                            (offset.y - bubbleSizePx / 2f).toInt()
                        )
                    }
                    .size(item.size)
            ) {
                OrbitBubble(
                    item = item,
                    isSelected = selectedId == item.id,
                    isFaded = selectedId != null && selectedId != item.id,
                    onClick = { onClick(item, offset) }
                )
            }
        }
    }
}

fun getOrbitOffset(
    index: Int,
    total: Int,
    radiusPx: Float,
    centerX: Float,
    centerY: Float,
    rotation: Float
): Offset {
    val angle = (360f / total) * index + rotation
    val rad = Math.toRadians(angle.toDouble())
    val x = centerX + radiusPx * cos(rad)
    val y = centerY + radiusPx * sin(rad)
    return Offset(x.toFloat(), y.toFloat())
}

// ─── SCORE CORE ──────────────────────────────────────────────────────────────
@Composable
fun ScoreCore(score: Int, mood: String, energy: String, themeColor: Color, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_pulse")
    
    // Animated score number (0 to actual)
    val animatedScore = remember { Animatable(0f) }
    LaunchedEffect(score) {
        delay(1000) // Start after orbit expansion begins
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 2000, easing = EaseOutCubic)
        )
    }

    // 1. BREATHING: scale 0.97 ↔ 1.03 (2000ms)
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // 2. ENERGY-BASED PULSE
    // high: sharp (800ms), medium: soft (1500ms), low: slow (3000ms)
    val pulseDuration = when(energy.lowercase()) {
        "high" -> 800
        "low" -> 3000
        else -> 1500
    }
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDuration, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "energy_pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(160.dp)
            .graphicsLayer { 
                scaleX = breatheScale
                scaleY = breatheScale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.5.dp, themeColor.copy(alpha = glowAlpha), CircleShape)
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize(0.92f)
                .background(themeColor.copy(alpha = 0.08f), CircleShape)
                .border(1.dp, themeColor.copy(alpha = 0.4f), CircleShape)
        ) {
            Text(
                text = "${animatedScore.value.toInt()}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black, 
                    fontSize = 52.sp,
                    letterSpacing = (-2).sp
                ),
                color = themeColor
            )
            Text(
                text = mood.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = themeColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                color = themeColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = energy.uppercase(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = themeColor
                )
            }
        }
    }
}

// ─── ORBIT RING PATH (Clean animated ring) ──────────────────────────────────
@Composable
fun OrbitRingPath(
    radius: Dp,
    themeColor: Color,
    rotationFraction: Float
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val radiusPx = with(density) { radius.toPx() }
    val glowStrokePx = with(density) { 8.dp.toPx() }
    val ringStrokePx = with(density) { 1.5.dp.toPx() }
    val arcStrokePx = with(density) { 2.5.dp.toPx() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)

        // 1. Soft outer glow — wide faint halo
        drawCircle(
            color = themeColor.copy(alpha = 0.07f),
            radius = radiusPx,
            center = center,
            style = Stroke(width = glowStrokePx)
        )

        // 2. Solid orbit ring — thin, clean line
        drawCircle(
            color = themeColor.copy(alpha = 0.25f),
            radius = radiusPx,
            center = center,
            style = Stroke(width = ringStrokePx)
        )

        // 3. Bright leading arc — 60° that follows the orbit rotation
        val sweepStart = rotationFraction * 360f
        val arcPath = Path().apply {
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    center.x - radiusPx, center.y - radiusPx,
                    center.x + radiusPx, center.y + radiusPx
                ),
                startAngleDegrees = sweepStart - 90f,
                sweepAngleDegrees = 60f,
                forceMoveTo = true
            )
        }
        drawPath(
            path = arcPath,
            color = themeColor.copy(alpha = 0.6f),
            style = Stroke(width = arcStrokePx, cap = StrokeCap.Round)
        )
    }
}

// ─── ORBIT SYSTEM ─────────────────────────────────────────────────────────────
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun OrbitSystem(
    horoscope: HoroscopeResponse,
    themeColor: Color,
    overlayState: HomeOverlayState,
    onOverlayStateChange: (HomeOverlayState) -> Unit,
    onActionClick: (String, Any?) -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // 1. Life areas use category-specific score ramps.
    val innerItems = remember(horoscope, isDarkTheme) {
        val areas = mutableListOf<OrbitItem>()
        horoscope.areas_text?.love?.let {
            val score = horoscope.score.areas?.get("love")?.value
            val palette = ScoreColors.paletteFor("love", score ?: horoscope.score.overall, isDarkTheme)
            areas.add(OrbitItem("love", "Love", "❤️", palette.main, 84.dp, glowColor = palette.glow, score = score, data = it))
        }
        horoscope.areas_text?.health?.let {
            val score = horoscope.score.areas?.get("health")?.value
            val palette = ScoreColors.paletteFor("health", score ?: horoscope.score.overall, isDarkTheme)
            areas.add(OrbitItem("health", "Health", "🌿", palette.main, 84.dp, glowColor = palette.glow, score = score, data = it))
        }
        horoscope.areas_text?.career?.let {
            val score = horoscope.score.areas?.get("career")?.value
            val palette = ScoreColors.paletteFor("career", score ?: horoscope.score.overall, isDarkTheme)
            areas.add(OrbitItem("career", "Career", "💼", palette.main, 84.dp, glowColor = palette.glow, score = score, data = it))
        }
        horoscope.areas_text?.finance?.let {
            val score = horoscope.score.areas?.get("finance")?.value
            val palette = ScoreColors.paletteFor("finance", score ?: horoscope.score.overall, isDarkTheme)
            areas.add(OrbitItem("finance", "Finance", "💰", palette.main, 84.dp, glowColor = palette.glow, score = score, data = it))
        }
        areas
    }

    val infinite = rememberInfiniteTransition(label = "orbit_rotation")
    
    // 25s rotation — fast enough to be perceptible on 60fps/battery-saver devices
    // (45s was too slow; at 60fps that's only 0.48°/frame — invisible)
    val rotationInner by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(25000, easing = LinearEasing)),
        label = "inner_rot"
    )

    val effectiveRotationInner = rotationInner

    Column(modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().height(340.dp),
            contentAlignment = Alignment.Center
        ) {
            val systemWidth = maxWidth
            
            // Responsive Radii optimized for 4 items
            val orbitRadius = (systemWidth * 0.32f).coerceAtMost(140.dp)

            // Orbit canvas
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val expandedItemId = (overlayState as? HomeOverlayState.Expanded)?.item?.id

                // Visible glowing orbit ring path
                OrbitRingPath(
                    radius = orbitRadius,
                    themeColor = themeColor,
                    rotationFraction = effectiveRotationInner / 360f
                )

                OrbitRing(
                    items = innerItems,
                    radius = orbitRadius,
                    rotation = effectiveRotationInner,
                    selectedId = expandedItemId,
                    onClick = { item, offset -> onOverlayStateChange(HomeOverlayState.Expanded(item, offset)) }
                )
                ScoreCore(
                    score = horoscope.score.overall,
                    mood = horoscope.mood?.value ?: "Social",
                    energy = horoscope.current_state?.energy ?: "Medium",
                    themeColor = themeColor,
                    onClick = {
                        val scoreItem = OrbitItem("score", "Intelligence", "✨", themeColor, 160.dp, glowColor = themeColor, score = horoscope.score.overall, data = horoscope)
                        onOverlayStateChange(HomeOverlayState.Expanded(scoreItem, Offset.Zero))
                    }
                )
            }
        }
    }
}

// ─── POPUP OVERLAY LAYER ───────────────────────────────────────────────────
@Composable
fun HomePopupOverlay(
    overlayState: HomeOverlayState,
    horoscope: HoroscopeResponse,
    onDismiss: () -> Unit,
    onChatClick: (String?) -> Unit,
    onForecastClick: (String?) -> Unit,
    onNavigateToRashis: (String?) -> Unit = {}
) {
    val expandedState = overlayState as? HomeOverlayState.Expanded
    val scrollState = rememberScrollState()

    // 1. LIGHTWEIGHT PREMIUM TRANSITION (Scale + Fade + Slide)
    AnimatedVisibility(
        visible = overlayState is HomeOverlayState.Expanded,
        enter = fadeIn(animationSpec = tween(250)) + 
                scaleIn(initialScale = 0.92f, animationSpec = tween(250)) +
                slideInVertically(initialOffsetY = { it / 12 }),
        exit = fadeOut(animationSpec = tween(200)) + 
               scaleOut(targetScale = 0.95f, animationSpec = tween(200))
    ) {
        val state = overlayState as? HomeOverlayState.Expanded
        if (state != null) {
            val naviPrompt: String = when (state.item.id) {
                "ai"    -> "Explain my mood ${horoscope.mood?.value} and score ${horoscope.score.overall}. Guide me for today."
                "score" -> "Give me a deep reading about my score ${horoscope.score.overall} and current mood."
                else    -> "Ask Navi about ${state.item.label}"
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.22f))
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() }
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                // 3. FLOATING GLASS CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.86f) // Floating feel (not full width)
                        .padding(bottom = 20.dp)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                    ),
                    border = BorderStroke(1.dp, state.item.color.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(state.item.color.copy(alpha = 0.12f), CircleShape)
                                    .border(1.dp, state.item.color.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text(state.item.icon, fontSize = 22.sp) }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = state.item.label.uppercase(), 
                                    style = MaterialTheme.typography.labelSmall, 
                                    fontWeight = FontWeight.Black, 
                                    color = state.item.color,
                                    letterSpacing = 1.sp
                                )
                                if (state.item.score != null) {
                                    Text(
                                        text = "${state.item.score}", 
                                        style = MaterialTheme.typography.headlineSmall, 
                                        fontWeight = FontWeight.Black, 
                                        color = state.item.color
                                    )
                                }
                            }
                            
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        OrbitItemDetailContent(state.item) { action: String, data: Any? ->
                            when (action) {
                                "forecast" -> {
                                    onForecastClick(data as? String)
                                    onDismiss()
                                }
                                "chat" -> {
                                    onChatClick(data as? String)
                                    onDismiss()
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = { onChatClick(naviPrompt); onDismiss() },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = state.item.color),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "ASK NAVI ABOUT ${state.item.label.uppercase()}", 
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── POPUP DETAIL CONTENT (icons shown here, not in bubbles) ──────────────────
@Composable
fun OrbitItemDetailContent(item: OrbitItem, onActionClick: (String, Any?) -> Unit) {
    when (item.id) {
        "score" -> {
            val horoscope = item.data as? HoroscopeResponse
            if (horoscope != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "COSMIC SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Large Score Percentage
                    Text(
                        text = "${horoscope.score?.overall ?: 0}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "WHY TODAY FEELS THIS WAY",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Main Advice
                    Text(
                        horoscope.alerts?.primary?.simple
                            ?: horoscope.current_state?.advice_now
                            ?: "A steady day for alignment.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Detailed Transits
                    horoscope.astro_explanations?.items?.forEach { item ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(item.importance.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Text(item.technical, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(item.simple, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        "career", "love", "health", "finance" -> {
            val insight = item.data as? AreaInsight
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    insight?.insight ?: "",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { onActionClick("forecast", item.id) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("WEEKLY FORECAST")
                }
            }
        }

        "lucky" -> {
            val lucky = item.data as? LuckyData
            if (lucky != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(56.dp).background(AstroColors.Jupiter.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text("🎨", fontSize = 28.sp) }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("COLOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(lucky.color, fontWeight = FontWeight.ExtraBold, color = AstroColors.Jupiter)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(56.dp).background(AstroColors.Sun.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text("🎲", fontSize = 28.sp) }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("NUMBER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${lucky.number}", fontWeight = FontWeight.ExtraBold, color = AstroColors.Sun)
                    }
                }
            }
        }

        "why" -> {
            val explanations = item.data as? AstroExplanationsData
            val items = explanations?.items?.take(3)
            if (items != null) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (astroItem in items) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text("✨", modifier = Modifier.padding(top = 2.dp), fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(astroItem.technical, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Text(astroItem.simple, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        "time" -> {
            val triggers = item.data as? List<TimeTrigger>
            if (triggers != null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    triggers.forEachIndexed { index, trigger ->
                        val isActive = index == 0
                        val statusColor = if (isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(statusColor.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                .border(1.dp, statusColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (trigger.type == "communication") "💬" else "🤝", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(if (isActive) "NOW ACTIVE" else "UPCOMING", fontSize = 9.sp, fontWeight = FontWeight.Black, color = statusColor)
                                Text(trigger.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${trigger.start} – ${trigger.end}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        "alerts" -> {
            val alerts = item.data as? AlertsData
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                alerts?.primary?.let { alert ->
                    val color = if (alert.type == "warning") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(if (alert.type == "warning") "⚠️" else "✨", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(alert.simple, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        if (alert.technical != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(alert.technical, fontSize = 11.sp, color = color.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                        }
                    }
                }
                alerts?.secondary?.take(2)?.forEach { alert ->
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("•", fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(alert.simple, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        "ai" -> {
            Text(
                item.data as String,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Retry Connection", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DashboardSkeleton() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Box(modifier = Modifier.width(120.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.width(150.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.width(200.dp).height(36.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(2) {
                    Box(modifier = Modifier.size(64.dp).clip(CircleShape).shimmerEffect())
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(24.dp)).shimmerEffect()) {}
        Column(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(24.dp)).shimmerEffect()) {}
        Spacer(modifier = Modifier.height(32.dp))
        }
        }

        // ─── NEW DASHBOARD SECTIONS ──────────────────────────────────────────────────

        @Composable
fun WeeklyForecastSection(forecast: ForecastResponse, themeColor: Color, onNavigateToForecast: (String?) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToForecast(forecast.area) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, themeColor.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val bestDayLabel = remember(forecast.summary.best_day) {
                try {
                    val sdfIn = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val sdfOut = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                    val date = sdfIn.parse(forecast.summary.best_day)
                    date?.let { sdfOut.format(it).uppercase() } ?: "---"
                } catch (e: Exception) {
                    forecast.summary.best_day.take(3).uppercase()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📈", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "WEEK AHEAD • BEST: $bestDayLabel",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = themeColor,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    "SEE ALL →",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = themeColor.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            WeeklyForecastGraph(
                days = forecast.days,
                themeColor = themeColor,
                area = forecast.area,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                showLabels = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            val today = forecast.days.find { it.is_today }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "☀ TODAY ${today?.score ?: "--"}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = themeColor
                )
                Text(
                    "  •  ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                Text(
                    text = "MOOD: ${today?.mood?.value?.uppercase() ?: "BALANCED"}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "  •  ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                Text(
                    text = "🔮 DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = themeColor
                )
            }
        }
    }
}

@Composable
fun WeeklyForecastGraph(
    days: List<ForecastDay>,
    themeColor: Color,
    area: String = "general",
    modifier: Modifier = Modifier,
    showLabels: Boolean = false,
    selectedDate: String? = null,
    onDayClick: ((String) -> Unit)? = null
) {
    val scores = days.map { it.score.toFloat() }
    if (scores.isEmpty()) return
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val dayPalettes = remember(days, area, isDarkTheme) {
        days.map { day -> ScoreColors.paletteFor(area, day.score, isDarkTheme) }
    }

    val entranceProgress = remember { Animatable(0f) }
    LaunchedEffect(days) {
        entranceProgress.animateTo(1f, tween(600, easing = EaseOutCubic))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val todayPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "today_dot"
    )

    Canvas(
        modifier = modifier
            .pointerInput(days, onDayClick) {
                if (onDayClick != null) {
                    detectTapGestures { offset ->
                        val spacing = size.width / (days.size - 1)
                        val index = (offset.x / spacing).roundToInt().coerceIn(0, days.size - 1)
                        onDayClick(days[index].date)
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val spacing = width / (scores.size - 1)
        val maxScore = 100f
        
        val points = scores.mapIndexed { index, score ->
            Offset(
                x = index * spacing,
                y = height - (score / maxScore * height)
            )
        }

        // --- 1. AREA FILL (GRADIENT) ---
        val fillPath = Path().apply {
            moveTo(points.first().x, height)
            points.forEachIndexed { index, point ->
                if (index == 0) lineTo(point.x, point.y)
                else {
                    // Slight smoothing
                    val prev = points[index - 1]
                    cubicTo(
                        (prev.x + point.x) / 2, prev.y,
                        (prev.x + point.x) / 2, point.y,
                        point.x, point.y
                    )
                }
            }
            lineTo(points.last().x, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(themeColor.copy(alpha = 0.25f), Color.Transparent),
                startY = 0f,
                endY = height
            ),
            alpha = entranceProgress.value
        )

        // --- 2. MAIN LINE ---
        val linePath = Path().apply {
            points.forEachIndexed { index, point ->
                if (index == 0) moveTo(point.x, point.y)
                else {
                    val prev = points[index - 1]
                    cubicTo(
                        (prev.x + point.x) / 2, prev.y,
                        (prev.x + point.x) / 2, point.y,
                        point.x, point.y
                    )
                }
            }
        }

        val pathMeasure = android.graphics.PathMeasure(linePath.asAndroidPath(), false)
        val partialPath = Path()
        pathMeasure.getSegment(0f, pathMeasure.length * entranceProgress.value, partialPath.asAndroidPath(), true)

        drawPath(
            path = partialPath,
            color = themeColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // --- 3. DATA DOTS & LABELS ---
        if (entranceProgress.value > 0.8f) {
            val maxScoreDay = days.maxByOrNull { it.score }
            
            days.forEachIndexed { index, day ->
                val point = points[index]
                val isToday = day.is_today
                val isSelected = day.date == selectedDate
                val isBest = day == maxScoreDay
                val palette = dayPalettes[index]
                val dotColor = if (isDarkTheme) palette.glow else palette.main
                
                // Draw dot
                val dotRadius = when {
                    isSelected -> 6.dp.toPx()
                    isToday -> 5.dp.toPx() * todayPulse
                    else -> 3.dp.toPx()
                }

                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = point
                )
                
                if (isSelected || isToday) {
                    drawCircle(
                        color = palette.glow.copy(alpha = 0.32f),
                        radius = dotRadius * 2,
                        center = point
                    )
                }

                // Labels for Today, Selected, and Best Day (or all if showLabels)
                if (isToday || isSelected || isBest || showLabels) {
                    val scoreText = day.score.toString()
                    val textPaint = android.graphics.Paint().apply {
                        color = dotColor.toArgb()
                        textSize = 11.sp.toPx()
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    
                    drawContext.canvas.nativeCanvas.drawText(
                        scoreText,
                        point.x,
                        point.y - 12.dp.toPx(),
                        textPaint
                    )

                    if (showLabels) {
                        val dayLabel = try {
                            val sdfIn = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                            val sdfOut = java.text.SimpleDateFormat("EEE", java.util.Locale.US)
                            val date = sdfIn.parse(day.date)
                            date?.let { sdfOut.format(it).uppercase() } ?: day.date.take(3).uppercase()
                        } catch (e: Exception) {
                            day.date.take(3).uppercase()
                        }
                        
                        textPaint.textSize = 9.sp.toPx()
                        textPaint.color = (if (isToday || isSelected) dotColor else dotColor.copy(alpha = 0.55f)).toArgb()
                        
                        drawContext.canvas.nativeCanvas.drawText(
                            dayLabel,
                            point.x,
                            height - 4.dp.toPx(),
                            textPaint
                        )
                    }
                }
            }
        }
    }
}

        @Composable
fun KundliPeekCard(
    data: AnalyzeFullResponse, 
    userEmail: String?, 
    accessToken: String?, 
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "kundli_peek_scale")

    val currentMahadasha = data.dasha?.current?.mahadasha
    val planetName = currentMahadasha?.planet ?: "Jupiter"
    val themeColor = AstroColors.getPlanetaryColor(planetName)
    
    val lagna = data.ascendant?.sign ?: "Unknown"
    val dashaLabel = "$planetName MD"

    // --- ANIMATIONS ---
    val revealScale = remember { Animatable(0.9f) }
    LaunchedEffect(Unit) {
        revealScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale * revealScale.value
                scaleY = scale * revealScale.value
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.05f)),
        border = BorderStroke(1.5.dp, themeColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: SVG + Identity
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Birth Chart SVG (Mini)
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.2f))
                ) {
                    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
                    if (!userEmail.isNullOrBlank() && !accessToken.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data("https://api.veriscribeanalytics.com/api/profile/svg?style=north&theme=${if (isDarkTheme) "dark" else "light"}")
                                .decoderFactory(SvgDecoder.Factory())
                                .addHeader("X-API-Key", BuildConfig.API_KEY)
                                .addHeader("X-User-Email", userEmail)
                                .addHeader("Authorization", "Bearer $accessToken")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Vedic Birth Chart",
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = themeColor,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "YOUR KUNDLI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = themeColor,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "$lagna Lagna • $dashaLabel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = themeColor.copy(alpha = 0.4f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Strongest Planet Insight
            val strongestPlanet = data.planet_strength_ranking?.minByOrNull { it.rank }
            if (strongestPlanet != null) {
                val planetData = data.planets?.find { it.planet == strongestPlanet.planet }
                Text(
                    text = "Your strongest planet is ${strongestPlanet.planet} in the ${planetData?.house ?: "?"} house.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Dasha Timelines (Mahadasha & Antardasha)
            data.dasha?.current?.mahadasha?.let { md ->
                DashaTimelineItem(
                    title = "Mahadasha: ${md.planet}",
                    planetName = md.planet,
                    startDate = md.start,
                    endDate = md.end,
                    interpretation = "",
                    isPrimary = true
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            data.dasha?.current?.antardasha?.let { ad ->
                DashaTimelineItem(
                    title = "Antardasha: ${ad.planet}",
                    planetName = ad.planet,
                    startDate = ad.start,
                    endDate = ad.end,
                    interpretation = "",
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(50.dp).shimmerSweepEffect(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
            ) {
                Text("EXPLORE FULL ANALYSIS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

        @Composable
fun CosmicAlertBanner(alerts: AlertsData, themeColor: Color, onClick: () -> Unit) {
    val allAlerts = remember(alerts) {
        val list = mutableListOf<AlertItem>()
        alerts.primary?.let { list.add(it) }
        alerts.secondary?.let { list.addAll(it) }
        list
    }
    if (allAlerts.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(allAlerts.size) {
        while (true) {
            delay(5000)
            currentIndex = (currentIndex + 1) % allAlerts.size
        }
    }

    val alert = allAlerts[currentIndex]
    val isWarning = alert.type == "warning"
    val color = if (isWarning) MaterialTheme.colorScheme.error else themeColor

    val infiniteTransition = rememberInfiniteTransition(label = "alert_pulse")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "icon_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AnimatedContent(
                targetState = alert,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
                },
                label = "alert_cycle"
            ) { currentAlert ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.graphicsLayer { scaleX = iconScale; scaleY = iconScale }) {
                        Text(if (currentAlert.type == "warning") "⚠️" else "✨", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            currentAlert.simple,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "LEARN MORE →",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = color,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            if (allAlerts.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(allAlerts.size) { index ->
                        val isActive = index == currentIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .size(if (isActive) 6.dp else 4.dp)
                                .background(
                                    if (isActive) color else color.copy(alpha = 0.2f),
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

        @Composable
fun DeeperWisdomSection(dominantPlanet: String?, onNavigateToPlanets: () -> Unit, onNavigateToNakshatras: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        KnowledgeMicroCard(
            title = "${dominantPlanet ?: "Sun"} Wisdom",
            subtitle = "Planetary insights",
            emoji = "🪐",
            color = dominantPlanet?.let { AstroColors.getPlanetaryColor(it) } ?: AstroColors.Jupiter,
            modifier = Modifier.weight(1f),
            index = 0,
            onClick = onNavigateToPlanets
        )
        KnowledgeMicroCard(
            title = "Lunar Mansions",
            subtitle = "Your Nakshatra",
            emoji = "☾",
            color = AstroColors.Moon,
            modifier = Modifier.weight(1f),
            index = 1,
            onClick = onNavigateToNakshatras
        )
    }
}

@Composable
fun KnowledgeMicroCard(title: String, subtitle: String, emoji: String, color: Color, modifier: Modifier, index: Int = 0, onClick: () -> Unit) {
    val entranceAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 150L)
        entranceAlpha.animateTo(1f, tween(600))
    }

    val floatDuration = if (index == 0) 2600 else 2900
    val infiniteTransition = rememberInfiniteTransition(label = "micro_card_idle")
    val idleFloat by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(floatDuration, easing = LinearEasing), RepeatMode.Reverse),
        label = "idle"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "micro_card_scale")

    Card(
        modifier = modifier
            .graphicsLayer {
                alpha = entranceAlpha.value
                translationY = idleFloat
                scaleX = scale
                scaleY = scale
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun ConsultTeaserCard(record: ConsultRecord, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "consult_teaser_scale")

    val infiniteTransition = rememberInfiniteTransition(label = "consult_float")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "ball"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .graphicsLayer { translationY = floatY }
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🔮", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("LAST CONSULTATION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text(
                    "${record.primary_category?.replace("_", " ") ?: "Guidance"} • ${record.secondary_category?.replace("_", " ") ?: "Inquiry"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(36.dp).shimmerSweepEffect(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("ASK AGAIN", fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
