package com.astranavi.app.ui.kundli

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import com.astranavi.app.ui.components.PreviewMultiDevice
import com.astranavi.app.ui.theme.AstraNaviTheme
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.astranavi.app.R
import com.astranavi.app.data.model.*
import com.astranavi.app.ui.components.DashaTimelineItem
import com.astranavi.app.ui.components.GlassCard
import com.astranavi.app.ui.components.ShimmerBlock
import com.astranavi.app.ui.components.shimmerEffect
import com.astranavi.app.ui.components.shimmerSweepEffect
import com.astranavi.app.ui.components.responsiveMetrics
import com.astranavi.app.ui.theme.AstroColors
import com.astranavi.app.ui.theme.LocalSemanticColors
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import com.astranavi.app.util.LocaleFormatter
import com.astranavi.app.util.currentAppLocale

import androidx.compose.ui.composed
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.astranavi.app.util.nextLocalMidnightMillis
import kotlinx.coroutines.isActive

class CoordinatesHolder {
    var coords: LayoutCoordinates? = null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KundliScreen(
    viewModel: KundliViewModel,
    onOpenDrawer: () -> Unit = {},
    onBack: () -> Unit = {},
    onNavigateToChat: (String?) -> Unit = {}
) {
    com.astranavi.app.util.SecureScreen()
    val uiState = viewModel.uiState.value
    val userEmail = viewModel.userEmail.value
    val accessToken = viewModel.accessToken.value
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState !is KundliState.Loading) {
            isRefreshing = false
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchKundli(silent = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            val target = nextLocalMidnightMillis()
            val sleep = (target - System.currentTimeMillis() + 1_000L).coerceAtLeast(1_000L)
            delay(sleep)
            viewModel.fetchKundli(silent = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.fetchKundli(forceRefresh = true)
            },
            modifier = Modifier.fillMaxSize()
        ) {
            // Note: Box is needed as direct child of PullToRefreshBox to anchor scrollable content
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    is KundliState.Loading -> KundliSkeleton()
                    is KundliState.Error -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.message, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                    is KundliState.Success -> {
                        androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
                            KundliContent(uiState.data, userEmail, accessToken, viewModel, onNavigateToChat)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KundliContent(
    data: AnalyzeFullResponse,
    userEmail: String?,
    accessToken: String?,
    viewModel: KundliViewModel,
    onNavigateToChat: (String?) -> Unit
) {
    val localUserName = viewModel.userName.value
    val listState = rememberLazyListState()
    val leftListState = rememberLazyListState()
    val rightListState = rememberLazyListState()
    val houseState = viewModel.houseUiState
    val planetState = viewModel.planetUiState
    val responsive = responsiveMetrics()

    BackHandler(enabled = houseState is HouseUiState.Detail || planetState is PlanetUiState.Detail) {
        if (houseState is HouseUiState.Detail) {
            viewModel.onHouseBack()
        } else if (planetState is PlanetUiState.Detail) {
            viewModel.onPlanetBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (responsive.useTwoPane) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(if (houseState is HouseUiState.Detail || planetState is PlanetUiState.Detail) responsive.kundliSectionGap * 0.3125f else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(responsive.twoPaneGap)
            ) {
                LazyColumn(
                    state = leftListState,
                    modifier = Modifier.weight(responsive.twoPaneLeftWeight).fillMaxHeight(),
                    contentPadding = PaddingValues(bottom = responsive.kundliBottomPadding)
                ) {
                    item { Box(Modifier.sacredParallax(0, leftListState)) { Zone1Hero(data, userEmail, accessToken, localUserName, leftListState) } }
                    item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                    item { Box(Modifier.sacredParallax(4, leftListState)) { Zone2Ascendant(data.ascendant) } }
                    item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                    item { Box(Modifier.sacredParallax(6, leftListState)) { Zone3Dasha(data.dasha?.current) } }
                }
                LazyColumn(
                    state = rightListState,
                    modifier = Modifier.weight(responsive.twoPaneRightWeight).fillMaxHeight(),
                    contentPadding = PaddingValues(bottom = responsive.kundliBottomPadding)
                ) {
                    item { Box(Modifier.sacredParallax(0, rightListState)) { Zone5Strengths(data.planet_strength_ranking, data.planets, data.houses, data.ashtakavarga, rightListState, 0) } }
                    item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                    item { 
                        Box(Modifier.sacredParallax(2, rightListState)) { 
                            Zone4Planets(data.planets) { planetName, circleInfo ->
                                viewModel.onPlanetClick(planetName, circleInfo)
                            }
                        } 
                    }
                    item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                    item { 
                        Box(Modifier.sacredParallax(4, rightListState)) {
                            Zone6HouseAnalysis(data.ashtakavarga, data.houses) { houseNum, circleInfo ->
                                viewModel.onHouseClick(houseNum, circleInfo)
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                    item { Box(Modifier.sacredParallax(6, rightListState)) { Zone9Transits(data.transits) } }
                    item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                    item { Box(Modifier.sacredParallax(8, rightListState)) { Zone10Timeline(data.dasha?.timeline, data.dasha?.current, onNavigateToChat) } }
                    item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                    item { Box(Modifier.sacredParallax(10, rightListState)) { Zone8KeyThemes(data.key_themes, onNavigateToChat) } }
                    item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                    item { Box(Modifier.sacredParallax(12, rightListState)) { Zone11NaviAI(data, onNavigateToChat) } }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(if (houseState is HouseUiState.Detail || planetState is PlanetUiState.Detail) responsive.kundliSectionGap * 0.3125f else 0.dp),
                contentPadding = PaddingValues(bottom = responsive.kundliBottomPadding)
            ) {
                item { Box(Modifier.sacredParallax(0, listState)) { Zone1Hero(data, userEmail, accessToken, localUserName, listState) } }
                item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                item { Box(Modifier.sacredParallax(4, listState)) { Zone2Ascendant(data.ascendant) } }
                item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                item { Box(Modifier.sacredParallax(6, listState)) { Zone3Dasha(data.dasha?.current) } }
                item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                item { Box(Modifier.sacredParallax(8, listState)) { Zone5Strengths(data.planet_strength_ranking, data.planets, data.houses, data.ashtakavarga, listState, 8) } }
                item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                item { 
                    Box(Modifier.sacredParallax(10, listState)) { 
                        Zone4Planets(data.planets) { planetName, circleInfo ->
                            viewModel.onPlanetClick(planetName, circleInfo)
                        } 
                    } 
                }
                item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                item { 
                    Box(Modifier.sacredParallax(12, listState)) {
                        Zone6HouseAnalysis(data.ashtakavarga, data.houses) { houseNum, circleInfo ->
                            viewModel.onHouseClick(houseNum, circleInfo)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                item { Box(Modifier.sacredParallax(14, listState)) { Zone9Transits(data.transits) } }
                item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                item { Box(Modifier.sacredParallax(16, listState)) { Zone10Timeline(data.dasha?.timeline, data.dasha?.current, onNavigateToChat) } }
                item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                item { Box(Modifier.sacredParallax(18, listState)) { Zone8KeyThemes(data.key_themes, onNavigateToChat) } }
                item { Spacer(modifier = Modifier.height(responsive.kundliSectionGap)) }
                item { Box(Modifier.sacredParallax(20, listState)) { Zone11NaviAI(data, onNavigateToChat) } }
            }
        }

        // Expansion Overlays

        if (houseState is HouseUiState.Detail) {
            val selectedHouse = data.houses?.find { it.house == houseState.houseId }
            if (selectedHouse != null) {
                val transitingPlanetsForHouse = data.transits?.planets?.filter { it.current_house_in_natal == selectedHouse.house } ?: emptyList()
                HouseExpansionOverlay(
                    house = selectedHouse,
                    transitingPlanets = transitingPlanetsForHouse,
                    circleInfo = houseState.circleInfo,
                    onClose = { viewModel.onHouseBack() },
                    onNavigateToChat = onNavigateToChat
                )
            }
        }

        if (planetState is PlanetUiState.Detail) {
            val selectedPlanet = data.planets?.find { it.planet.lowercase() == planetState.planetName.lowercase() }
            if (selectedPlanet != null) {
                PlanetExpansionOverlay(
                    planet = selectedPlanet,
                    circleInfo = planetState.circleInfo,
                    onClose = { viewModel.onPlanetBack() },
                    onNavigateToChat = onNavigateToChat
                )
            }
        }
    }
}

@Composable
fun HouseExpansionOverlay(
    house: HouseData,
    transitingPlanets: List<TransitPlanet>,
    circleInfo: CircleInfo,
    onClose: () -> Unit,
    onNavigateToChat: (String?) -> Unit
) {
    val density = LocalDensity.current
    val responsive = responsiveMetrics()
    val screenWidth = responsive.screenWidth
    val screenHeight = responsive.screenHeight

    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        isExpanded = true
    }

    val initialHeight = with(density) { circleInfo.size.height.toDp() }
    val initialWidth = with(density) { circleInfo.size.width.toDp() }

    // Card targets: ~90% width, wrap height (capped)
    val cardWidth = screenWidth * 0.9f
    val cardMaxHeight = screenHeight * 0.7f

    // Calculate offset required to move from center to the exact circle position
    val circleCenterX = circleInfo.offset.x + circleInfo.size.width / 2f
    val circleCenterY = circleInfo.offset.y + circleInfo.size.height / 2f
    
    val screenCenterX = with(density) { screenWidth.toPx() / 2f }
    val screenCenterY = with(density) { screenHeight.toPx() / 2f }
    
    val startOffsetX = with(density) { (circleCenterX - screenCenterX).toDp() }
    val startOffsetY = with(density) { (circleCenterY - screenCenterY).toDp() }

    val targetWidth = if (isExpanded) cardWidth else initialWidth
    val targetOffsetX = if (isExpanded) 0.dp else startOffsetX
    val targetOffsetY = if (isExpanded) 0.dp else startOffsetY
    val targetCorner = if (isExpanded) responsive.kundliOverlayCorner else responsive.kundliOverlayCorner * 1.8f

    val contentAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(450, delayMillis = 200), label = "alpha"
    )

    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "width"
    )
    val animatedOffsetX by animateDpAsState(
        targetValue = targetOffsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "offsetX"
    )
    val animatedOffsetY by animateDpAsState(
        targetValue = targetOffsetY,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "offsetY"
    )
    val animatedCorner by animateDpAsState(
        targetValue = targetCorner,
        animationSpec = tween(600), label = "corner"
    )

    // Semi-transparent scrim — blurred kundli page shows through
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = contentAlpha * 0.5f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                isExpanded = false
                onClose()
            }
    )

    // Floating Card Wrapper — handles absolute centering
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // The actual expanding card
        Box(
            modifier = Modifier
                .offset(x = animatedOffsetX, y = animatedOffsetY)
                .width(animatedWidth)
                .then(if (isExpanded) Modifier.heightIn(max = cardMaxHeight) else Modifier.height(initialHeight))
                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
                .clip(RoundedCornerShape(animatedCorner))
                .background(MaterialTheme.colorScheme.surface)
        ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(300, delayMillis = 100)) + slideInVertically(tween(300, delayMillis = 100)) { it / 6 },
            exit = fadeOut(tween(150))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Compact header with close button + house identity
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = responsive.kundliSectionGap * 0.5f,
                            end = responsive.kundliSectionGap * 0.25f,
                            top = responsive.kundliSectionGap * 0.25f,
                            bottom = responsive.kundliSectionGap * 0.125f
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "H${house.house}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        isExpanded = false
                        onClose()
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Scrollable detail content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    HouseDetailSheet(house, transitingPlanets, onNavigateToChat)
                }
            }
        }
    }
    }
}

@Composable
fun Zone1Hero(
    data: AnalyzeFullResponse,
    userEmail: String?,
    accessToken: String?,
    localUserName: String?,
    listState: LazyListState
) {
    val context = LocalContext.current
    val responsive = responsiveMetrics()
    
    // Parallax logic: Faintly visible as a "ghost" until 40% scroll depth
    val scrollOffset = remember { derivedStateOf { 
        if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset else 1200 
    } }
    val parallaxOffset = scrollOffset.value.toFloat() * 0.4f // Slower movement for depth
    val chartAlphaBase = (1f - scrollOffset.value.toFloat() / 1800f).coerceIn(0.12f, 1f)

    // 2-stage reveal logic (Sacred Observatory Reveal)
    var revealStage by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        delay(300)
        revealStage = 1 // Phase 1: Cosmic Reveal
        delay(1200)
        revealStage = 2 // Phase 2: Orbital Assembly
    }

    val revealScale by animateFloatAsState(
        targetValue = when(revealStage) {
            0 -> 0.82f
            1 -> 1.06f
            else -> 1f
        },
        animationSpec = if (revealStage == 1) tween(1200, easing = EaseOutCubic) 
                        else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "revealScale"
    )

    val revealAlpha by animateFloatAsState(
        targetValue = if (revealStage >= 1) 1f else 0f,
        animationSpec = tween(1500, easing = EaseInOutCubic),
        label = "revealAlpha"
    )

    val blurAmount by animateDpAsState(
        targetValue = if (scrollOffset.value > 200) (scrollOffset.value / 100).dp.coerceAtMost(responsive.kundliSectionGap * 0.25f) else 0.dp,
        animationSpec = tween(500),
        label = "scrollBlur"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = responsive.kundliCardPadding)
            .graphicsLayer {
                translationY = parallaxOffset
                alpha = revealAlpha * chartAlphaBase
                renderEffect = null
            },
        shape = RoundedCornerShape(responsive.kundliOverlayCorner)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(responsive.kundliCardInnerPadding)
            .blur(blurAmount)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Ascendant Identity Line (Top)
                Text(
                    text = "${data.ascendant?.sign ?: "Lagna"} Ascendant • ${data.ascendant?.nakshatra ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.35f))
                
                // SACRED OBSERVATORY CHART REVEAL
                Box(modifier = Modifier
                    .size(responsive.kundliWheelSize)
                    .aspectRatio(1f)
                    .scale(revealScale)
                    .alpha(revealAlpha), 
                    contentAlignment = Alignment.Center
                ) {
                    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
                    
                    // 1. Ambient Glow Background
                    val dashaPlanet = data.dasha?.current?.mahadasha?.planet ?: "Jupiter"
                    val dashaColor = AstroColors.getPlanetaryColor(dashaPlanet)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(dashaColor.copy(alpha = 0.12f), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )
                    
                    // 2. Dasha Pulse Ring Indicator
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.92f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2500, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2500, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = dashaColor,
                            radius = (size.minDimension / 2f) * pulseScale,
                            style = Stroke(width = 1.5.dp.toPx()),
                            alpha = pulseAlpha
                        )
                    }

                    // 3. Cosmic Particles Overlay
                    val particleTime by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(18000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "particles"
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val angleRad = particleTime * Math.PI.toFloat() / 180f
                        val particles = listOf(
                            Offset(0.25f, 0.3f), Offset(0.75f, 0.2f), Offset(0.3f, 0.75f), Offset(0.7f, 0.8f),
                            Offset(0.5f, 0.18f), Offset(0.82f, 0.42f), Offset(0.18f, 0.58f), Offset(0.62f, 0.82f)
                        )
                        particles.forEachIndexed { i, p ->
                            val x = p.x * size.width
                            val y = p.y * size.height
                            val breathingAlpha = ((kotlin.math.sin(angleRad + i) + 1f) / 2f * 0.4f + 0.1f)
                            drawCircle(
                                color = Color.White,
                                radius = 1.2.dp.toPx(),
                                center = Offset(x, y),
                                alpha = breathingAlpha
                            )
                        }
                    }

                    // 4. The actual SVG chart
                    if (userEmail != null && accessToken != null) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data("https://api.veriscribeanalytics.com/api/profile/svg?style=north&theme=${if (isDarkTheme) "dark" else "light"}")
                                .decoderFactory(SvgDecoder.Factory())
                                .addHeader("X-API-Key", com.astranavi.app.BuildConfig.API_KEY)
                                .addHeader("X-User-Email", userEmail)
                                .addHeader("Authorization", "Bearer $accessToken")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Vedic Birth Chart",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        ) {
                            val state = painter.state
                            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Empty) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            } else if (state is AsyncImagePainter.State.Error) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.BrokenImage,
                                            contentDescription = "Chart loading failed",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.kundli_label_failed_to_load_chart),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            } else {
                                SubcomposeAsyncImageContent()
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Sign in to view chart",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.kundli_label_sign_in_to_view),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.4f))
                
                // Bottom Chips Row
                val strongestPlanet = data.planet_strength_ranking?.minByOrNull { it.rank }?.planet ?: "Jupiter"
                val mahadasha = data.dasha?.current?.mahadasha?.planet ?: "Jupiter"
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroChip(label = "$strongestPlanet ✦", color = AstroColors.getPlanetaryColor(strongestPlanet))
                    Spacer(modifier = Modifier.width(8.dp))
                    HeroChip(label = "$mahadasha MD", color = AstroColors.getPlanetaryColor(mahadasha))
                    Spacer(modifier = Modifier.width(8.dp))
                    HeroChip(label = stringResource(R.string.kundli_label_focus_career_self), color = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.35f))

                // CTA
                Text(
                    text = stringResource(R.string.kundli_label_tap_to_explore),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.3f))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.25f))
                
                val identity = data.identity
                val displayName = identity?.name?.takeIf {
                    it.isNotBlank() && !it.equals("user", ignoreCase = true)
                } ?: localUserName?.takeIf { it.isNotBlank() } ?: "User"

                Text(displayName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (identity != null) {
                    Text("${identity.birth_details ?: ""} • ${identity.ayanamsa ?: ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun IdentityPill(text: String) {
    val responsive = responsiveMetrics()
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
    ) {
        Text(
            text, 
            modifier = Modifier.padding(horizontal = responsive.kundliGridSpacing, vertical = responsive.kundliSectionGap * 0.1875f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun Zone2Ascendant(ascendant: AscendantData?) {
    if (ascendant == null) return
    val responsive = responsiveMetrics()
    Column(modifier = Modifier.padding(horizontal = responsive.kundliCardPadding)) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(responsive.kundliCardInnerPadding), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(responsive.kundliSmallIconSize))
                Spacer(modifier = Modifier.width(responsive.kundliSectionGap * 0.5f))
                Column {
                    Text("Ascendant: ${ascendant.sign}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${ascendant.degree}° ${ascendant.nakshatra?.let { "• $it" } ?: ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.125f))
                    Text(ascendant.interpretation ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun Zone3Dasha(currentDasha: CurrentDasha?) {
    if (currentDasha == null) return
    val responsive = responsiveMetrics()
    Column(modifier = Modifier.padding(horizontal = responsive.kundliCardPadding)) {
        SectionHeader(stringResource(R.string.kundli_section_current_dasha), stringResource(R.string.kundli_subtitle_planetary_timeline))
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(responsive.kundliCardInnerPadding)) {
                currentDasha.mahadasha?.let { md ->
                    DashaTimelineItem(
                        title = "Mahadasha: ${md.planet}",
                        planetName = md.planet,
                        startDate = md.start,
                        endDate = md.end,
                        interpretation = md.interpretation ?: "",
                        isPrimary = true
                    )
                }
                Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.25f))
                currentDasha.antardasha?.let { ad ->
                    DashaTimelineItem(
                        title = "Antardasha: ${ad.planet}",
                        planetName = ad.planet,
                        startDate = ad.start,
                        endDate = ad.end,
                        interpretation = ad.interpretation ?: "",
                        modifier = Modifier.padding(start = responsive.kundliSectionGap * 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.25f))
                currentDasha.pratyantardasha?.let { pd ->
                    DashaTimelineItem(
                        title = "Pratyantardasha: ${pd.planet}",
                        planetName = pd.planet,
                        startDate = pd.start,
                        endDate = pd.end,
                        interpretation = "",
                        modifier = Modifier.padding(start = responsive.kundliSectionGap)
                    )
                }
                
                Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))
                TextButton(onClick = { /* Navigate to full timeline */ }) {
                    Text(stringResource(R.string.kundli_btn_view_full_timeline), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun Zone4Planets(planets: List<PlanetData>?, onPlanetClick: (String, CircleInfo) -> Unit) {
    if (planets == null || planets.isEmpty()) return
    val responsive = responsiveMetrics()
    
    Column(modifier = Modifier.padding(top = responsive.kundliCardInnerPadding, bottom = responsive.kundliCardInnerPadding)) {
        Column(modifier = Modifier.padding(start = responsive.kundliCardPadding, end = responsive.kundliCardPadding, bottom = responsive.kundliCardInnerPadding)) {
            Text(stringResource(R.string.kundli_section_planetary_gallery), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.kundli_subtitle_swipe_to_explore), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        }
        
        val validPlanets = planets.take(9)
        val pagerState = rememberPagerState(
            initialPage = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % validPlanets.size)
        ) {
            Int.MAX_VALUE
        }
        
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = responsive.kundliPagerContentPadding),
            pageSpacing = responsive.kundliSectionGap * 0.25f,
            modifier = Modifier.fillMaxWidth().height(responsive.kundliPagerHeight * 0.58f)
        ) { page ->
            val actualIndex = page % validPlanets.size
            val planet = validPlanets[actualIndex]
            
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            
            val scale = 0.85f + (1f - 0.85f) * (1f - pageOffset.coerceIn(0f, 1f))
            val alpha = 0.6f + (1f - 0.6f) * (1f - pageOffset.coerceIn(0f, 1f))
 
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        
                        // Tilt effect
                        rotationY = (pagerState.currentPage - page + pagerState.currentPageOffsetFraction) * -15f
                        // Curved swipe path
                        translationY = pageOffset * 30f
                        cameraDistance = 12f * density
                    }
                    .blur(radius = (responsive.kundliSectionGap * 0.125f * pageOffset).coerceAtLeast(0.dp))
            ) {
                PlanetCarouselCard(planet) { circleInfo ->
                    onPlanetClick(planet.planet, circleInfo)
                }
            }
        }
    }
}

@Composable
fun PlanetCarouselCard(
    planet: PlanetData,
    onClick: (CircleInfo) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coordsHolder = remember { CoordinatesHolder() }
    val semanticColors = LocalSemanticColors.current
    val responsive = responsiveMetrics()
    
    val dignityColor = when (planet.dignity.lowercase()) {
        "exalted" -> semanticColors.exalted
        "debilitated" -> semanticColors.debilitated
        else -> semanticColors.normal
    }
    val finalColor = if (planet.retrograde) semanticColors.retrograde else dignityColor
 
    val planetRes = when (planet.planet.lowercase()) {
        "sun" -> R.drawable.sun
        "moon" -> R.drawable.moon
        "mars" -> R.drawable.mars
        "mercury" -> R.drawable.mercury
        "jupiter" -> R.drawable.jupiter
        "venus" -> R.drawable.venus
        "saturn" -> R.drawable.saturn
        else -> R.drawable.logo 
    }
 
    val interpretationText = planet.house_placement_interpretation?.takeIf { it.isNotEmpty() }
        ?: planet.nakshatra_interpretation?.takeIf { it.isNotEmpty() }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(responsive.kundliPagerHeight * 0.55f)
            .onGloballyPositioned { coordsHolder.coords = it }
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                coordsHolder.coords?.let {
                    onClick(CircleInfo(0, it.positionInRoot(), it.size))
                }
            },
        border = BorderStroke(1.dp, finalColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(responsive.kundliCardInnerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = planet.planet,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (planet.combust) {
                        Text("🔥", modifier = Modifier.padding(start = 4.dp))
                    }
                    if (planet.retrograde) {
                        Text(" ℞", modifier = Modifier.padding(start = 4.dp), color = semanticColors.retrograde, fontWeight = FontWeight.Bold)
                    }
                }
                
                Surface(
                    color = finalColor.copy(alpha = 0.12f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, finalColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = planet.dignity.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = finalColor,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape)
                        .border(1.5.dp, finalColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = planetRes),
                        contentDescription = planet.planet,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    if (planet.planet.lowercase() == "rahu" || planet.planet.lowercase() == "ketu") {
                        Box(modifier = Modifier.size(72.dp).background(AstroColors.getPlanetaryColor(planet.planet).copy(alpha = 0.4f), CircleShape))
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${planet.sign} • House ${planet.house ?: ""}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val formattedDegree = planet.degree?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "0.00"
                    val nakshatraInfo = buildString {
                        append("$formattedDegree°")
                        if (!planet.nakshatra.isNullOrEmpty()) {
                            append(" • ${planet.nakshatra}")
                            if (planet.nakshatra_pada != null) {
                                append(" (Pada ${planet.nakshatra_pada})")
                            }
                        }
                    }
                    Text(
                        text = nakshatraInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    if (planet.shadbala_percent > 0.0 || !planet.nakshatra_lord.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val extraInfo = buildString {
                            if (planet.shadbala_percent > 0.0) {
                                append("Shadbala: ${planet.shadbala_percent.toInt()}%")
                            }
                            if (!planet.nakshatra_lord.isNullOrEmpty()) {
                                if (this.isNotEmpty()) append(" • ")
                                append("Lord: ${planet.nakshatra_lord}")
                            }
                        }
                        Text(
                            text = extraInfo,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (interpretationText != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = interpretationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun Zone5Strengths(
    rankings: List<PlanetStrengthRank>?,
    planets: List<PlanetData>?,
    houses: List<HouseData>?,
    av: AshtakavargaData?,
    listState: LazyListState,
    itemIndex: Int
) {
    if (rankings == null || rankings.isEmpty()) return
    
    val isActuallyVisible by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.any { it.index == itemIndex }
        }
    }

    var hasTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(isActuallyVisible) {
        if (isActuallyVisible) {
            hasTriggered = true
        }
    }

    val responsive = responsiveMetrics()
    val semanticColors = LocalSemanticColors.current

    Column(modifier = Modifier.padding(horizontal = responsive.kundliCardPadding)) {
        SectionHeader(stringResource(R.string.kundli_section_planet_strength), stringResource(R.string.kundli_subtitle_shadbala_analysis))
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(responsive.kundliCardInnerPadding)) {
                rankings.sortedBy { it.rank }.forEachIndexed { index, rank ->
                    var isRowVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(hasTriggered) {
                        if (hasTriggered) {
                            delay(index * 150L)
                            isRowVisible = true
                        }
                    }
                    
                    AnimatedVisibility(
                        visible = isRowVisible,
                        enter = slideInHorizontally(animationSpec = spring()) { -50 } + fadeIn()
                    ) {
                        Column(modifier = Modifier.padding(vertical = responsive.kundliSectionGap * 0.25f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${rank.rank}.", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.width(responsive.kundliSmallIconSize))
                                Text(rank.planet, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                                Text("${(rank.shadbala * 100).toInt() / 100f} Rupa", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.125f))
                            val targetProgress = if(isRowVisible) (rank.shadbala.toFloat() / 10f).coerceIn(0f, 1f) else 0f
                            val progress by animateFloatAsState(targetValue = targetProgress, animationSpec = tween(1000), label = "rankProgress")
                            
                            Box(modifier = Modifier.fillMaxWidth().height(responsive.kundliProgressBarHeight).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                                Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(AstroColors.getPlanetaryColor(rank.planet), CircleShape))
                            }
                            Text("${rank.dignity} • ${rank.summary}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = responsive.kundliSectionGap * 0.125f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(responsive.kundliSectionGap))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(responsive.kundliGridSpacing)
        ) {
            // Your Strengths (Left Column)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, semanticColors.exalted.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(responsive.kundliCardInnerPadding)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.kundli_label_your_strengths),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = semanticColors.exalted
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val strengthItems = remember { mutableStateListOf<String>() }
                        LaunchedEffect(planets, av) {
                            strengthItems.clear()
                            planets?.filter { it.dignity.lowercase() in listOf("exalted", "own sign", "great friend") }
                                ?.take(2)
                                ?.forEach { planet ->
                                    strengthItems.add("${planet.planet} in House ${planet.house ?: "?"}")
                                }
                            av?.strongest_houses?.take(2)?.forEach { houseScore ->
                                val areaName = houses?.find { it.house == houseScore.house }?.name ?: "Life Area"
                                strengthItems.add("Strong House ${houseScore.house} ($areaName)")
                            }
                            if (strengthItems.isEmpty()) {
                                strengthItems.add("Strong focus & purpose")
                                strengthItems.add("Intuitive indicators")
                            }
                        }
                        
                        strengthItems.forEach { item ->
                            Text(
                                text = "• $item",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp),
                                maxLines = 2
                            )
                        }
                    }
                }
            }
 
            // Growth Areas (Right Column)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, semanticColors.retrograde.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(responsive.kundliCardInnerPadding)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌱", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.kundli_label_growth_areas),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = semanticColors.retrograde
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val challengeItems = remember { mutableStateListOf<String>() }
                        LaunchedEffect(planets, av) {
                            challengeItems.clear()
                            planets?.filter { it.dignity.lowercase() == "debilitated" || it.retrograde }
                                ?.take(2)
                                ?.forEach { planet ->
                                    challengeItems.add("Refine ${planet.planet} in House ${planet.house ?: "?"}")
                                }
                            av?.weakest_houses?.take(2)?.forEach { houseScore ->
                                val areaName = houses?.find { it.house == houseScore.house }?.name ?: "Life Area"
                                challengeItems.add("Nurture House ${houseScore.house} ($areaName)")
                            }
                            if (challengeItems.isEmpty()) {
                                challengeItems.add("Patience in career timing")
                                challengeItems.add("Inner reflection cycles")
                            }
                        }
                        
                        challengeItems.forEach { item ->
                            Text(
                                text = "• $item",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp),
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Zone6HouseAnalysis(av: AshtakavargaData?, houses: List<HouseData>?, onHouseClick: (Int, CircleInfo) -> Unit) {
    if (av == null || av.house_scores == null) return
    
    val scope = rememberCoroutineScope()
    var selectedId by remember { mutableStateOf<Int?>(null) }
    var pulseId by remember { mutableStateOf<Int?>(null) }
    val responsive = responsiveMetrics()

    Column(modifier = Modifier.padding(horizontal = responsive.kundliCardPadding)) {
        SectionHeader(stringResource(R.string.kundli_section_house_analysis), stringResource(R.string.kundli_subtitle_click_house_to_explore))
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(responsive.kundliCardInnerPadding)) {
                // 4x3 Grid of houses
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 0 until 3) {
                            val houseNum = row * 3 + col + 1
                            val score = av.house_scores[houseNum - 1]
                            val houseData = houses?.find { it.house == houseNum }
                            HouseCapsule(
                                houseNum = houseNum,
                                score = score,
                                houseData = houseData,
                                isDimmed = selectedId != null && selectedId != houseNum,
                                isSelected = selectedId == houseNum,
                                isPulsing = pulseId == houseNum,
                                onClick = { circleInfo -> 
                                    scope.launch {
                                        pulseId = houseNum
                                        delay(180) // Stage 1: Pulse
                                        pulseId = null
                                        selectedId = houseNum
                                        delay(200) // Stage 2: Orbit Freeze
                                        onHouseClick(houseNum, circleInfo) // Stage 3: Expansion (Overlay)
                                        delay(500)
                                        selectedId = null
                                    }
                                }
                            )
                        }
                    }
                    if (row < 3) Spacer(modifier = Modifier.height(responsive.kundliGridSpacing))
                }

                Spacer(modifier = Modifier.height(responsive.kundliSectionGap))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))
                
                av.strongest_houses?.firstOrNull()?.let {
                    Text("Strongest: House ${it.house} ${it.area} (${it.score} pts)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }
                av.weakest_houses?.firstOrNull()?.let {
                    Text("Weakest: House ${it.house} ${it.area} (${it.score} pts)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HouseEnergyBar(
    av: AshtakavargaData?, 
    houses: List<HouseData>?, 
    onHouseClick: (Int, CircleInfo) -> Unit
) {
    if (av == null || av.house_scores == null) return
    val responsive = responsiveMetrics()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = responsive.kundliSectionGap * 0.5f)
    ) {
        Column(modifier = Modifier.padding(horizontal = responsive.kundliCardPadding)) {
            Text(
                text = stringResource(R.string.kundli_section_house_strengths),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Ashtakavarga energy distribution (average is 28)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = responsive.kundliCardPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(12, key = { "house_${it + 1}" }) { index ->
                val houseNum = index + 1
                val score = av.house_scores.getOrNull(index) ?: 0
                val houseData = houses?.find { it.house == houseNum }
                
                HouseEnergyBarItem(
                    houseNum = houseNum,
                    score = score,
                    houseData = houseData,
                    onHouseClick = onHouseClick
                )
            }
        }
    }
}

@Composable
fun HouseEnergyBarItem(
    houseNum: Int,
    score: Int,
    houseData: HouseData?,
    onHouseClick: (Int, CircleInfo) -> Unit
) {
    val coordsHolder = remember { CoordinatesHolder() }
    val responsive = responsiveMetrics()
    val scope = rememberCoroutineScope()
    var isPulsing by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPulsing) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val (gradientColors, strengthLabel) = when {
        score > 30 -> Pair(
            listOf(Color(0xFF00C9FF), Color(0xFF92FE9D)),
            "Strong"
        )
        score >= 25 -> Pair(
            listOf(Color(0xFF3a7bd5), Color(0xFF3a6073)),
            "Average"
        )
        else -> Pair(
            listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)),
            "Weak"
        )
    }

    GlassCard(
        modifier = Modifier
            .width(88.dp)
            .height(150.dp)
            .onGloballyPositioned { coordsHolder.coords = it }
            .scale(scale)
            .clickable {
                scope.launch {
                    isPulsing = true
                    delay(120)
                    isPulsing = false
                    val rootOffset = coordsHolder.coords?.positionInRoot() ?: Offset.Zero
                    val size = coordsHolder.coords?.size ?: IntSize(0, 0)
                    onHouseClick(houseNum, CircleInfo(houseNum, rootOffset, size))
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "H$houseNum",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = houseData?.name?.take(7) ?: "House",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.BottomCenter
            ) {
                val scorePercent = (score / 48f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(scorePercent)
                        .background(Brush.verticalGradient(gradientColors), RoundedCornerShape(7.dp))
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score pts",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = strengthLabel,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = gradientColors.first()
                )
            }
        }
    }
}

@Composable
fun HouseCapsule(
    houseNum: Int,
    score: Int,
    houseData: HouseData?,
    isDimmed: Boolean = false,
    isSelected: Boolean = false,
    isPulsing: Boolean = false,
    onClick: (CircleInfo) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coordsHolder = remember { CoordinatesHolder() }
    val responsive = responsiveMetrics()

    val scale by animateFloatAsState(
        targetValue = if (isPulsing) 1.08f else if (isSelected) 1.15f else 1f,
        animationSpec = if (isPulsing) tween(180) else spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isDimmed) 0.35f else 1f,
        animationSpec = tween(300),
        label = "alpha"
    )

    val scoreColor = when {
        score > 30 -> Color(0xFF4CAF50)
        score > 25 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    GlassCard(
        modifier = Modifier
            .width(100.dp)
            .height(110.dp)
            .onGloballyPositioned { coordsHolder.coords = it }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                coordsHolder.coords?.let {
                    onClick(CircleInfo(houseNum, it.positionInRoot(), it.size))
                }
            },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: House Identity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "H$houseNum",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Small AV Score Badge
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(scoreColor.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, scoreColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = scoreColor
                    )
                }
            }

            // Middle: Sign & Lord Symbol
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val signShort = houseData?.sign?.take(4) ?: "Sign"
                Text(
                    text = signShort,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                
                val lordShort = houseData?.lord?.take(3) ?: ""
                if (lordShort.isNotEmpty()) {
                    Text(
                        text = "Lord: $lordShort",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = AstroColors.getPlanetaryColor(houseData?.lord ?: "").copy(alpha = 0.85f),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Bottom: Occupants Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(8.dp)
            ) {
                val occupants = houseData?.occupants ?: emptyList()
                if (occupants.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
                    )
                } else {
                    occupants.take(4).forEach { occupant ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(AstroColors.getPlanetaryColor(occupant.planet), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HouseDetailSheet(
    house: HouseData,
    transitingPlanets: List<TransitPlanet>,
    onNavigateToChat: (String?) -> Unit
) {
    val semanticColors = LocalSemanticColors.current
    val responsive = responsiveMetrics()
    val strengthColor = when(house.strength_assessment?.lowercase()) {
        "strong" -> Color(0xFF4CAF50)
        "weak" -> Color(0xFFF44336)
        else -> Color(0xFFFFC107)
    }

    Column(modifier = Modifier.padding(responsive.kundliCardInnerPadding).fillMaxWidth().verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(responsive.kundliPlanetIconTiny), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) { Text("${house.house}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer) }
            }
            Spacer(modifier = Modifier.width(responsive.kundliSectionGap * 0.5f))
            Column(modifier = Modifier.weight(1f)) {
                Text("${house.name} • ${house.sign}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                if (!house.sanskrit_name.isNullOrEmpty()) {
                    Text(house.sanskrit_name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (house.ashtakavarga_score != null) {
                    Text("AV Score: ${house.ashtakavarga_score}", fontWeight = FontWeight.Bold, color = strengthColor)
                }
                if (!house.strength_assessment.isNullOrEmpty()) {
                    Text(house.strength_assessment, style = MaterialTheme.typography.labelSmall, color = strengthColor)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(responsive.kundliSectionGap))
        
        Text(stringResource(R.string.kundli_label_areas_of_life), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(house.areas.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        
        Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))

        Text(stringResource(R.string.kundli_label_house_lord), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        val lordPlanetRes = when (house.lord?.lowercase()) {
            "sun" -> R.drawable.sun
            "moon" -> R.drawable.moon
            "mars" -> R.drawable.mars
            "mercury" -> R.drawable.mercury
            "jupiter" -> R.drawable.jupiter
            "venus" -> R.drawable.venus
            "saturn" -> R.drawable.saturn
            else -> R.drawable.logo
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = responsive.kundliSectionGap * 0.125f)) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = lordPlanetRes),
                    contentDescription = house.lord,
                    modifier = Modifier.size(responsive.kundliSmallIconSize * 0.75f).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                if (house.lord?.lowercase() == "rahu" || house.lord?.lowercase() == "ketu") {
                    Box(modifier = Modifier.size(responsive.kundliSmallIconSize * 0.75f).background(AstroColors.getPlanetaryColor(house.lord).copy(alpha = 0.4f), CircleShape))
                }
            }
            Spacer(modifier = Modifier.width(responsive.kundliGridSpacing * 0.75f))
            Text("Lord: ${house.lord} ${if(house.lord_house != null) "(Placed in House ${house.lord_house})" else ""} ${house.lord_dignity?.let { "- $it" } ?: ""}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        if (!house.lord_interpretation.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.125f))
            Text(house.lord_interpretation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (house.occupants.isNotEmpty()) {
            Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))
            Text(stringResource(R.string.kundli_label_planets_in_house), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            house.occupants.forEach { occupant ->
                val color = if (occupant.dignity.lowercase() == "exalted") semanticColors.exalted else MaterialTheme.colorScheme.onSurface
                
                val planetRes = when (occupant.planet.lowercase()) {
                    "sun" -> R.drawable.sun
                    "moon" -> R.drawable.moon
                    "mars" -> R.drawable.mars
                    "mercury" -> R.drawable.mercury
                    "jupiter" -> R.drawable.jupiter
                    "venus" -> R.drawable.venus
                    "saturn" -> R.drawable.saturn
                    else -> R.drawable.logo 
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = responsive.kundliSectionGap * 0.125f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = planetRes),
                            contentDescription = occupant.planet,
                            modifier = Modifier.size(responsive.kundliSmallIconSize * 0.625f).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        if (occupant.planet.lowercase() == "rahu" || occupant.planet.lowercase() == "ketu") {
                            Box(modifier = Modifier.size(responsive.kundliSmallIconSize * 0.625f).background(AstroColors.getPlanetaryColor(occupant.planet).copy(alpha = 0.4f), CircleShape))
                        }
                    }
                    Spacer(modifier = Modifier.width(responsive.kundliSectionGap * 0.25f))
                    Text("${occupant.planet} (${occupant.dignity})", style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Transit Activation section
        if (transitingPlanets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))
            Text(stringResource(R.string.kundli_label_current_transit_activation), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            transitingPlanets.forEach { tp ->
                val trColor = AstroColors.getPlanetaryColor(tp.planet)
                val trPlanetRes = when (tp.planet.lowercase()) {
                    "sun" -> R.drawable.sun
                    "moon" -> R.drawable.moon
                    "mars" -> R.drawable.mars
                    "mercury" -> R.drawable.mercury
                    "jupiter" -> R.drawable.jupiter
                    "venus" -> R.drawable.venus
                    "saturn" -> R.drawable.saturn
                    else -> R.drawable.logo 
                }
                Column(modifier = Modifier.padding(vertical = responsive.kundliSectionGap * 0.125f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = trPlanetRes),
                                contentDescription = tp.planet,
                                modifier = Modifier.size(responsive.kundliSmallIconSize * 0.625f).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            if (tp.planet.lowercase() == "rahu" || tp.planet.lowercase() == "ketu") {
                                Box(modifier = Modifier.size(responsive.kundliSmallIconSize * 0.625f).background(trColor.copy(alpha = 0.4f), CircleShape))
                            }
                        }
                        Spacer(modifier = Modifier.width(responsive.kundliSectionGap * 0.25f))
                        Text(
                            text = "${tp.planet} transiting in ${tp.current_sign}", 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = MaterialTheme.colorScheme.primary, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!tp.transit_interpretation.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tp.transit_interpretation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (house.aspects_received?.isNotEmpty() == true) {
            Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))
            Text(stringResource(R.string.kundli_label_aspects_received), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(house.aspects_received.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.height(responsive.kundliSectionGap))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))

        // AI Chat Prompts section
        Text(stringResource(R.string.kundli_btn_ask_navi_about_house, house.house), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(responsive.kundliGridSpacing))
        
        val housePrompts = listOf(
            "What does House ${house.house} in ${house.sign} say about my life?",
            "How does the lord of my ${house.house} House (${house.lord}) placement affect my chart?",
            "What transits are currently active in my ${house.house} House?"
        )
        
        housePrompts.forEach { prompt ->
            OutlinedCard(
                onClick = { onNavigateToChat(prompt) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ask Navi",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(responsive.kundliSectionGap))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Zone9Transits(transits: TransitData?) {
    if (transits == null || transits.planets == null) return
    val responsive = responsiveMetrics()
    
    Column(modifier = Modifier.padding(top = responsive.kundliCardInnerPadding)) {
        Column(modifier = Modifier.padding(horizontal = responsive.kundliCardPadding)) {
            SectionHeader(stringResource(R.string.kundli_section_current_transits), "Planetary movement as of ${transits.date ?: "Today"}")
        }
        
        val listState = rememberLazyListState()
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = responsive.kundliCardPadding),
            horizontalArrangement = Arrangement.spacedBy(responsive.kundliGridSpacing),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            items(transits.planets, key = { it.planet }) { planet ->
                val index = transits.planets.indexOf(planet)
                
                val rotationY = remember {
                    derivedStateOf {
                        val layoutInfo = listState.layoutInfo
                        val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
                        if (itemInfo != null) {
                            val center = layoutInfo.viewportEndOffset / 2f
                            val itemCenter = itemInfo.offset + itemInfo.size / 2f
                            val dist = (itemCenter - center) / center
                            dist * 15f
                        } else 0f
                    }
                }

                GlassCard(
                    modifier = Modifier
                        .width(responsive.kundliWheelSize * 0.6f)
                        .height(responsive.kundliWheelSize * 0.6f)
                        .graphicsLayer {
                            this.rotationY = rotationY.value
                            cameraDistance = 8 * density
                        }
                ) {
                    Column(modifier = Modifier.padding(responsive.kundliCardInnerPadding)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(responsive.kundliPlanetIconTiny).background(AstroColors.getPlanetaryColor(planet.planet).copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(planet.planet.take(1), fontWeight = FontWeight.Black, color = AstroColors.getPlanetaryColor(planet.planet))
                            }
                            Spacer(modifier = Modifier.width(responsive.kundliSectionGap * 0.25f))
                            Text(planet.planet, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(responsive.kundliGridSpacing))
                        Text("${planet.current_sign} • House ${planet.current_house_in_natal}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.125f))
                        Text(planet.transit_interpretation ?: "", style = MaterialTheme.typography.labelSmall, maxLines = 4, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Zone10Timeline(timeline: DashaTimeline?, current: CurrentDasha?, onNavigateToChat: (String?) -> Unit) {
    if (timeline == null || timeline.mahadashas == null) return
    
    var showFullTimeline by remember { mutableStateOf(false) }
    var selectedDasha by remember { mutableStateOf<DashaPeriod?>(null) }
    var clickedCircleInfo by remember { mutableStateOf<CircleInfo?>(null) }

    val sortedMDs = timeline.mahadashas.toList().sortedBy { it.second.start }
    val currentMDIndex = sortedMDs.indexOfFirst { it.first == current?.mahadasha?.planet }
    
    val displayMDs = if (showFullTimeline) {
        val startIndex = if (currentMDIndex > 0) currentMDIndex - 1 else 0
        sortedMDs.subList(startIndex, sortedMDs.size)
    } else {
        if (currentMDIndex >= 0) {
            val endIdx = (currentMDIndex + 2).coerceAtMost(sortedMDs.size)
            sortedMDs.subList(currentMDIndex, endIdx)
        } else sortedMDs.take(2)
    }
    
    val initialPage = if (showFullTimeline) (if (currentMDIndex > 0) 1 else 0) else 0

    val responsive = responsiveMetrics()
    Column(modifier = Modifier.padding(top = responsive.kundliCardInnerPadding)) {
        Row(modifier = Modifier.padding(start = responsive.kundliCardPadding, end = responsive.kundliCardPadding, bottom = responsive.kundliCardInnerPadding), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            SectionHeader(stringResource(R.string.kundli_section_dasha_timeline), if (showFullTimeline) stringResource(R.string.kundli_subtitle_full_life_chapters) else stringResource(R.string.kundli_subtitle_current_upcoming))
            TextButton(onClick = { showFullTimeline = !showFullTimeline }) {
                Text(if (showFullTimeline) stringResource(R.string.kundli_btn_show_less) else stringResource(R.string.kundli_btn_view_full_timeline), style = MaterialTheme.typography.labelMedium)
            }
        }

        val pagerState = rememberPagerState(
            initialPage = initialPage
        ) {
            displayMDs.size
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = responsive.kundliPagerContentPadding),
            pageSpacing = responsive.kundliSectionGap * 0.5f,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val (planet, md) = displayMDs[page]
            val isCurrentMD = planet == current?.mahadasha?.planet
            val originalIndex = sortedMDs.indexOfFirst { it.first == planet }
            
            val tag = when {
                originalIndex < currentMDIndex -> "PREVIOUS"
                originalIndex == currentMDIndex -> "CURRENT"
                else -> "UPCOMING"
            }
            
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            val scale = 0.75f + (1f - 0.75f) * (1f - pageOffset.coerceIn(0f, 1f))
            val alpha = 0.6f + (1f - 0.6f) * (1f - pageOffset.coerceIn(0f, 1f))

            val coordsHolder = remember { CoordinatesHolder() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordsHolder.coords = it }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        
                        shadowElevation = (1f - pageOffset.coerceIn(0f, 1f)) * 10f
                        rotationX = (pagerState.currentPage - page + pagerState.currentPageOffsetFraction) * 10f
                        cameraDistance = 10f * density
                    }
                    .clickable {
                        val rootOffset = coordsHolder.coords?.positionInRoot() ?: Offset.Zero
                        val size = coordsHolder.coords?.size ?: IntSize(0, 0)
                        clickedCircleInfo = CircleInfo(originalIndex, rootOffset, size)
                        selectedDasha = DashaPeriod(planet, md.start, md.end, "")
                    }
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(responsive.kundliCardInnerPadding)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(responsive.kundliPlanetIconTiny).background(AstroColors.getPlanetaryColor(planet).copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(planet.take(1), fontWeight = FontWeight.Black, color = AstroColors.getPlanetaryColor(planet), fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(responsive.kundliSectionGap * 0.5f))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("$planet Mahadasha", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = AstroColors.getPlanetaryColor(planet))
                                Text("${md.start} - ${md.end}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                            Surface(
                                color = if (tag == "CURRENT") AstroColors.getPlanetaryColor(planet) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ) {
                                val tagLabel = when (tag) {
                                    "CURRENT" -> stringResource(R.string.kundli_label_current_tag)
                                    "PREVIOUS" -> stringResource(R.string.kundli_label_previous_tag)
                                    else -> stringResource(R.string.kundli_label_upcoming_tag)
                                }
                                Text(tagLabel, modifier = Modifier.padding(horizontal = responsive.kundliSectionGap * 0.25f, vertical = responsive.kundliSectionGap * 0.125f), style = MaterialTheme.typography.labelSmall, color = if (tag == "CURRENT") MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedDasha != null && clickedCircleInfo != null) {
        DashaExpansionOverlay(
            planetName = selectedDasha!!.planet,
            startDate = selectedDasha!!.start,
            endDate = selectedDasha!!.end,
            interpretation = selectedDasha!!.interpretation ?: "",
            circleInfo = clickedCircleInfo!!,
            onClose = {
                selectedDasha = null
                clickedCircleInfo = null
            },
            onNavigateToChat = onNavigateToChat
        )
    }
}

@Composable
fun DashaExpansionOverlay(
    planetName: String,
    startDate: String,
    endDate: String,
    interpretation: String,
    circleInfo: CircleInfo,
    onClose: () -> Unit,
    onNavigateToChat: (String?) -> Unit
) {
    val density = LocalDensity.current
    val responsive = responsiveMetrics()
    val screenWidth = responsive.screenWidth
    val screenHeight = responsive.screenHeight

    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        isExpanded = true
    }

    val initialHeight = with(density) { circleInfo.size.height.toDp() }
    val initialWidth = with(density) { circleInfo.size.width.toDp() }

    val cardWidth = screenWidth * 0.9f
    val cardMaxHeight = screenHeight * 0.7f

    val circleCenterX = circleInfo.offset.x + circleInfo.size.width / 2f
    val circleCenterY = circleInfo.offset.y + circleInfo.size.height / 2f
    
    val screenCenterX = with(density) { screenWidth.toPx() / 2f }
    val screenCenterY = with(density) { screenHeight.toPx() / 2f }
    
    val startOffsetX = with(density) { (circleCenterX - screenCenterX).toDp() }
    val startOffsetY = with(density) { (circleCenterY - screenCenterY).toDp() }

    val targetWidth = if (isExpanded) cardWidth else initialWidth
    val targetOffsetX = if (isExpanded) 0.dp else startOffsetX
    val targetOffsetY = if (isExpanded) 0.dp else startOffsetY
    val targetCorner = if (isExpanded) responsive.kundliOverlayCorner else responsive.kundliOverlayCorner * 1.8f

    val contentAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(450, delayMillis = 200), label = "alpha"
    )

    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "width"
    )
    val animatedOffsetX by animateDpAsState(
        targetValue = targetOffsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "offsetX"
    )
    val animatedOffsetY by animateDpAsState(
        targetValue = targetOffsetY,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "offsetY"
    )
    val animatedCorner by animateDpAsState(
        targetValue = targetCorner,
        animationSpec = tween(600), label = "corner"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = contentAlpha * 0.5f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                isExpanded = false
                onClose()
            }
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset(x = animatedOffsetX, y = animatedOffsetY)
                .width(animatedWidth)
                .heightIn(max = cardMaxHeight)
                .clip(RoundedCornerShape(animatedCorner))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(300, delayMillis = 100)) + slideInVertically(tween(300, delayMillis = 100)) { it / 6 },
                    exit = fadeOut(tween(150))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = responsive.kundliSectionGap * 0.5f,
                                    end = responsive.kundliSectionGap * 0.25f,
                                    top = responsive.kundliSectionGap * 0.25f,
                                    bottom = responsive.kundliSectionGap * 0.125f
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$planetName Mahadasha",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                isExpanded = false
                                onClose()
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                        ) {
                            DashaDetailSheet(
                                planetName = planetName,
                                startDate = startDate,
                                endDate = endDate,
                                interpretation = interpretation,
                                onNavigateToChat = onNavigateToChat
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashaDetailSheet(
    planetName: String,
    startDate: String,
    endDate: String,
    interpretation: String,
    onNavigateToChat: (String?) -> Unit
) {
    val responsive = responsiveMetrics()
    val planetaryColor = AstroColors.getPlanetaryColor(planetName)

    val planetRes = when (planetName.lowercase()) {
        "sun" -> R.drawable.sun
        "moon" -> R.drawable.moon
        "mars" -> R.drawable.mars
        "mercury" -> R.drawable.mercury
        "jupiter" -> R.drawable.jupiter
        "venus" -> R.drawable.venus
        "saturn" -> R.drawable.saturn
        else -> R.drawable.logo
    }

    var progress by remember { mutableStateOf(0f) }
    var daysLeftText by remember { mutableStateOf("") }
    var displayStartDate by remember { mutableStateOf(startDate) }
    var displayEndDate by remember { mutableStateOf(endDate) }

    val locale = currentAppLocale()

    LaunchedEffect(startDate, endDate, locale) {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val start = LocalDate.parse(startDate.take(10), formatter)
            val end = LocalDate.parse(endDate.take(10), formatter)
            val today = LocalDate.now()

            displayStartDate = LocaleFormatter.displayDate(startDate, locale, "dd MMM yyyy")
            displayEndDate = LocaleFormatter.displayDate(endDate, locale, "dd MMM yyyy")
            
            val totalDays = ChronoUnit.DAYS.between(start, end)
            val daysPassed = ChronoUnit.DAYS.between(start, today)
            val left = ChronoUnit.DAYS.between(today, end)
            
            progress = if (totalDays > 0) (daysPassed.toFloat() / totalDays).coerceIn(0f, 1f) else 0f
            val percent = (progress * 100).toInt()
            
            daysLeftText = if (left > 0) {
                val yearsLeft = left / 365
                val monthsLeft = left / 30
                val timeLeftStr = when {
                    yearsLeft > 0 -> "$yearsLeft years left"
                    monthsLeft > 0 -> "$monthsLeft months left"
                    else -> "$left days left"
                }
                "$percent% Complete • $timeLeftStr"
            } else {
                "Completed"
            }
        } catch (e: Exception) {
            progress = 0.5f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(responsive.kundliCardInnerPadding)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(responsive.kundliPlanetIconLarge * 1.3f)
                        .background(planetaryColor.copy(alpha = 0.15f), CircleShape)
                )
                Image(
                    painter = painterResource(id = planetRes),
                    contentDescription = planetName,
                    modifier = Modifier
                        .size(responsive.kundliPlanetIconLarge)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(responsive.kundliSectionGap * 0.5f))
            
            Column {
                Text(
                    text = "$planetName Mahadasha",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$displayStartDate - $displayEndDate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(responsive.kundliSectionGap))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))

        Text(daysLeftText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(responsive.kundliProgressBarHeight).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
            Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(planetaryColor, CircleShape))
        }

        Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))

        val desc = interpretation.ifEmpty {
            when (planetName.lowercase()) {
                "sun" -> "A period of authority, self-discovery, career advancement, and learning to shine your light."
                "moon" -> "A phase of emotional growth, focus on home and family, intuition, and mental peace."
                "mars" -> "A high-energy time of drive, ambition, dynamic action, but requiring patience to avoid conflicts."
                "mercury" -> "A time of communication, intellect, business focus, study, and logical coordination."
                "jupiter" -> "A period of expansion, wisdom, grace, learning, spirituality, and good fortune."
                "venus" -> "A creative phase highlighting relationships, luxury, fine arts, harmony, and joy."
                "saturn" -> "A karmic cycle of discipline, hard work, restructuring, endurance, and deep life lessons."
                "rahu" -> "A time of ambition, unconventional pursuits, intense desires, and worldly shifts."
                "ketu" -> "A phase of detachment, spiritual awakening, introspection, and letting go of the old."
                else -> "A major life cycle influencing your career path, personal transformation, and spiritual journey."
            }
        }
        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(responsive.kundliSectionGap))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))

        Text("Ask Navi AI about this period", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(responsive.kundliGridSpacing))

        val dashaPrompts = listOf(
            "What is the spiritual lesson of my $planetName Mahadasha?",
            "How does $planetName period affect my career and relationships?",
            "What transit periods should I watch out for during this Dasha?"
        )

        dashaPrompts.forEach { prompt ->
            OutlinedCard(
                onClick = { onNavigateToChat(prompt) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ask Navi",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(responsive.kundliSectionGap))
    }
}

@Composable
fun Zone8KeyThemes(themes: List<KeyTheme>?, onNavigateToChat: (String?) -> Unit) {
    if (themes == null) return
    val responsive = responsiveMetrics()
    Column(modifier = Modifier.padding(horizontal = responsive.kundliCardPadding)) {
        SectionHeader(stringResource(R.string.kundli_section_key_insights), stringResource(R.string.kundli_subtitle_key_themes))
        
        themes.forEach { theme ->
            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = responsive.kundliGridSpacing)) {
                Column(modifier = Modifier.padding(responsive.kundliCardInnerPadding)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(responsive.kundliGridSpacing))
                        Text(theme.title ?: theme.theme ?: "Insight", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.25f))
                    Text(theme.interpretation ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    
                    Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.25f))
                    TextButton(
                        onClick = {
                            val topic = theme.title ?: theme.theme ?: "insights"
                            onNavigateToChat("Tell me more about the theme: '$topic' from my chart.")
                        },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.kundli_btn_explore_deeper),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Zone11NaviAI(data: AnalyzeFullResponse, onNavigateToChat: (String?) -> Unit) {
    val responsive = responsiveMetrics()
    
    // Dynamically calculate the highest-ranked planet
    val strongestPlanet = data.planet_strength_ranking?.minByOrNull { it.rank }?.planet ?: "Jupiter"
    
    // Dynamically check for retrograde or debilitated planets
    val specialPlanet = data.planets?.find { it.retrograde || it.dignity.lowercase() == "debilitated" }?.planet
    
    val dynamicPrompts = remember(strongestPlanet, specialPlanet) {
        val list = mutableListOf(
            "Why is $strongestPlanet the strongest planet in my birth chart?",
            "What are the main career indicators based on my 10th house?"
        )
        if (specialPlanet != null) {
            list.add("How should I balance the influence of my $specialPlanet?")
        } else {
            list.add("What does my ascendant sign tell me about my destiny?")
        }
        list.add("Explain the intensity and timing of my current Dasha cycle.")
        list
    }

    Column(modifier = Modifier.padding(horizontal = responsive.kundliCardPadding)) {
        SectionHeader(stringResource(R.string.kundli_section_ask_navi), stringResource(R.string.kundli_subtitle_contextual_questions))
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(responsive.kundliCardInnerPadding)) {
                Text(
                    text = stringResource(R.string.kundli_label_select_prompt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                dynamicPrompts.forEach { prompt ->
                    OutlinedCard(
                        onClick = { onNavigateToChat(prompt) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Ask Navi",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    val responsive = responsiveMetrics()
    Column(modifier = Modifier.padding(bottom = responsive.kundliCardInnerPadding)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun KundliSkeleton() {
    val responsive = responsiveMetrics()
    Column(modifier = Modifier.fillMaxSize().padding(responsive.kundliSectionGap * 0.75f)) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(responsive.kundliOverlayCorner))
            .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(responsive.kundliSectionGap))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(4) { ShimmerBlock(height = 110.dp, modifier = Modifier.width(120.dp), cornerRadius = 16.dp) }
        }

        Spacer(modifier = Modifier.height(responsive.kundliSectionGap))

        ShimmerBlock(height = 100.dp, cornerRadius = 20.dp)

        ShimmerBlock(height = 140.dp, cornerRadius = 20.dp)

        ShimmerBlock(height = 240.dp, cornerRadius = 20.dp)

        ShimmerBlock(height = 140.dp, cornerRadius = 20.dp)

        ShimmerBlock(height = 140.dp, cornerRadius = 20.dp)
    }
}

/**
 * Sacred Parallax: Cinematic layered scroll effect.
 * Upper content scales down to 0.96 and fades; lower content rises slowly.
 */
fun Modifier.sacredParallax(index: Int, listState: LazyListState): Modifier = this

// QuickInsightsBar and InsightCard functions removed to optimize layout

@Composable
fun HeroChip(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
fun PlanetExpansionOverlay(
    planet: PlanetData,
    circleInfo: CircleInfo,
    onClose: () -> Unit,
    onNavigateToChat: (String?) -> Unit
) {
    val density = LocalDensity.current
    val responsive = responsiveMetrics()
    val screenWidth = responsive.screenWidth
    val screenHeight = responsive.screenHeight

    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        isExpanded = true
    }

    val initialHeight = with(density) { circleInfo.size.height.toDp() }
    val initialWidth = with(density) { circleInfo.size.width.toDp() }

    // Card targets: ~90% width, wrap height (capped)
    val cardWidth = screenWidth * 0.9f
    val cardMaxHeight = screenHeight * 0.75f

    // Calculate offset required to move from center to the exact position
    val circleCenterX = circleInfo.offset.x + circleInfo.size.width / 2f
    val circleCenterY = circleInfo.offset.y + circleInfo.size.height / 2f
    
    val screenCenterX = with(density) { screenWidth.toPx() / 2f }
    val screenCenterY = with(density) { screenHeight.toPx() / 2f }
    
    val startOffsetX = with(density) { (circleCenterX - screenCenterX).toDp() }
    val startOffsetY = with(density) { (circleCenterY - screenCenterY).toDp() }

    val targetWidth = if (isExpanded) cardWidth else initialWidth
    val targetOffsetX = if (isExpanded) 0.dp else startOffsetX
    val targetOffsetY = if (isExpanded) 0.dp else startOffsetY
    val targetCorner = if (isExpanded) responsive.kundliOverlayCorner else responsive.kundliOverlayCorner * 1.8f

    val contentAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(450, delayMillis = 200), label = "alpha"
    )

    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "width"
    )
    val animatedOffsetX by animateDpAsState(
        targetValue = targetOffsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "offsetX"
    )
    val animatedOffsetY by animateDpAsState(
        targetValue = targetOffsetY,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "offsetY"
    )
    val animatedCorner by animateDpAsState(
        targetValue = targetCorner,
        animationSpec = tween(600), label = "corner"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = contentAlpha * 0.5f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                isExpanded = false
                onClose()
            }
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset(x = animatedOffsetX, y = animatedOffsetY)
                .width(animatedWidth)
                .then(if (isExpanded) Modifier.heightIn(max = cardMaxHeight) else Modifier.height(initialHeight))
                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
                .clip(RoundedCornerShape(animatedCorner))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(300, delayMillis = 100)) + slideInVertically(tween(300, delayMillis = 100)) { it / 6 },
                exit = fadeOut(tween(150))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = responsive.kundliSectionGap * 0.5f,
                                end = responsive.kundliSectionGap * 0.25f,
                                top = responsive.kundliSectionGap * 0.25f,
                                bottom = responsive.kundliSectionGap * 0.125f
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = planet.planet,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            isExpanded = false
                            onClose()
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        PlanetDetailSheet(planet, onNavigateToChat)
                    }
                }
            }
        }
    }
}

@Composable
fun PlanetDetailSheet(planet: PlanetData, onNavigateToChat: (String?) -> Unit) {
    val responsive = responsiveMetrics()
    val semanticColors = LocalSemanticColors.current
    val dignityColor = when (planet.dignity.lowercase()) {
        "exalted" -> semanticColors.exalted
        "debilitated" -> semanticColors.debilitated
        else -> semanticColors.normal
    }
    val finalColor = if (planet.retrograde) semanticColors.retrograde else dignityColor

    val planetRes = when (planet.planet.lowercase()) {
        "sun" -> R.drawable.sun
        "moon" -> R.drawable.moon
        "mars" -> R.drawable.mars
        "mercury" -> R.drawable.mercury
        "jupiter" -> R.drawable.jupiter
        "venus" -> R.drawable.venus
        "saturn" -> R.drawable.saturn
        else -> R.drawable.logo 
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(responsive.kundliCardInnerPadding)
            .verticalScroll(rememberScrollState())
    ) {
        // Top section with glow and details
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                // Subtle planetary color glow
                Box(
                    modifier = Modifier
                        .size(responsive.kundliPlanetIconLarge * 1.3f)
                        .background(finalColor.copy(alpha = 0.15f), CircleShape)
                )
                Image(
                    painter = painterResource(id = planetRes),
                    contentDescription = planet.planet,
                    modifier = Modifier
                        .size(responsive.kundliPlanetIconLarge)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                if (planet.planet.lowercase() == "rahu" || planet.planet.lowercase() == "ketu") {
                    Box(modifier = Modifier.size(responsive.kundliPlanetIconLarge).background(AstroColors.getPlanetaryColor(planet.planet).copy(alpha = 0.4f), CircleShape))
                }
            }
            
            Spacer(modifier = Modifier.width(responsive.kundliSectionGap * 0.5f))
            
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = planet.planet,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (planet.combust) {
                        Text("🔥", modifier = Modifier.padding(start = 4.dp))
                    }
                    if (planet.retrograde) {
                        Text(" ℞", modifier = Modifier.padding(start = 4.dp), color = semanticColors.retrograde, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "${planet.sign} • House ${planet.house} • ${planet.degree}°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                Surface(color = finalColor.copy(alpha = 0.12f), shape = CircleShape) {
                    Text(
                        text = planet.dignity,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = finalColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(responsive.kundliSectionGap))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))
        
        // Strength
        Text(stringResource(R.string.kundli_label_strength_shadbala, planet.shadbala_percent.toInt()), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(responsive.kundliProgressBarHeight).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
            Box(modifier = Modifier.fillMaxWidth((planet.shadbala_percent / 100.0).toFloat().coerceIn(0f, 1f)).fillMaxHeight().background(finalColor, CircleShape))
        }
        
        Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))
        
        if (!planet.dignity_interpretation.isNullOrEmpty()) {
            Text(planet.dignity_interpretation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))
        }
        
        // Nakshatra Info
        if (!planet.nakshatra.isNullOrEmpty()) {
            Text(stringResource(R.string.kundli_label_nakshatra_lord), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${planet.nakshatra} (Pada ${planet.nakshatra_pada ?: "?"}) • Lord: ${planet.nakshatra_lord ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            if (!planet.nakshatra_interpretation.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(planet.nakshatra_interpretation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))
        }
        
        // Placement
        if (!planet.house_placement_interpretation.isNullOrEmpty()) {
            Text(stringResource(R.string.kundli_label_placement_effect), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(planet.house_placement_interpretation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))
        }
        
        // Conjunctions
        if (!planet.conjunctions.isNullOrEmpty()) {
            Text(stringResource(R.string.kundli_label_conjunctions), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Conjunctions: ${planet.conjunctions.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            planet.conjunction_interpretations?.forEach { interp ->
                Text(interp, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))
        }
        
        // Aspects & Lordship
        val aspects = planet.aspects_given?.joinToString(", ") ?: "None"
        val lordOf = planet.lord_of?.joinToString(", ") ?: "None"
        Text(stringResource(R.string.kundli_label_aspects_ownership), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Aspects Given to Houses: $aspects", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Lord of Houses: $lordOf", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(responsive.kundliSectionGap))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(responsive.kundliSectionGap * 0.5f))

        // AI Chat Prompts section
        Text(stringResource(R.string.kundli_btn_ask_navi_planet, planet.planet), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(responsive.kundliGridSpacing))
        
        val planetPrompts = listOf(
            "What does ${planet.planet} in house ${planet.house} mean for my chart?",
            "Explain the conjunctions of my ${planet.planet}",
            "How does ${planet.planet} in ${planet.sign} nakshatra ${planet.nakshatra} affect my career?"
        )
        
        planetPrompts.forEach { prompt ->
            OutlinedCard(
                onClick = { onNavigateToChat(prompt) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ask Navi",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(responsive.kundliSectionGap))
    }
}

@PreviewMultiDevice
@Composable
fun SectionHeaderPreview() {
    AstraNaviTheme {
        Surface {
            SectionHeader(
                title = "Planetary Positions",
                subtitle = "Vedic celestial map at exact birth moment"
            )
        }
    }
}
