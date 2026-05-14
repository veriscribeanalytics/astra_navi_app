package com.astranavi.app.ui.kundli

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.astranavi.app.R
import com.astranavi.app.data.model.*
import com.astranavi.app.ui.components.DashaTimelineItem
import com.astranavi.app.ui.components.GlassCard
import com.astranavi.app.ui.components.shimmerEffect
import com.astranavi.app.ui.components.shimmerSweepEffect
import com.astranavi.app.ui.theme.AstroColors
import com.astranavi.app.ui.theme.LocalSemanticColors
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

import androidx.compose.ui.composed
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KundliScreen(viewModel: KundliViewModel, onOpenDrawer: () -> Unit = {}, onBack: () -> Unit = {}) {
    val uiState = viewModel.uiState.value
    val userEmail = viewModel.userEmail.value
    val accessToken = viewModel.accessToken.value

    LaunchedEffect(Unit) {
        viewModel.fetchKundli()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        when (uiState) {
            is KundliState.Loading -> KundliSkeleton()
            is KundliState.Error -> Text(uiState.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            is KundliState.Success -> {
                KundliContent(uiState.data, userEmail, accessToken, viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KundliContent(data: AnalyzeFullResponse, userEmail: String?, accessToken: String?, viewModel: KundliViewModel) {
    val listState = rememberLazyListState()
    val houseState = viewModel.houseUiState

    BackHandler(enabled = houseState is HouseUiState.Detail) {
        viewModel.onHouseBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .blur(if (houseState is HouseUiState.Detail) 10.dp else 0.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { Box(Modifier.sacredParallax(0, listState)) { Zone1Hero(data, userEmail, accessToken, listState) } }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { Box(Modifier.sacredParallax(2, listState)) { Zone2Ascendant(data.ascendant) } }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { Box(Modifier.sacredParallax(4, listState)) { Zone3Dasha(data.dasha?.current) } }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item { Box(Modifier.sacredParallax(6, listState)) { Zone5Strengths(data.planet_strength_ranking, listState, 6) } }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item { Box(Modifier.sacredParallax(8, listState)) { Zone4Planets(data.planets) } }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item { 
                Box(Modifier.sacredParallax(10, listState)) {
                    Zone6HouseAnalysis(data.ashtakavarga, data.houses) { houseNum, circleInfo ->
                        viewModel.onHouseClick(houseNum, circleInfo)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item { Box(Modifier.sacredParallax(12, listState)) { Zone9Transits(data.transits) } }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item { Box(Modifier.sacredParallax(14, listState)) { Zone10Timeline(data.dasha?.timeline, data.dasha?.current) } }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item { Box(Modifier.sacredParallax(16, listState)) { Zone8KeyThemes(data.key_themes) } }
        }

        // Expansion Overlay

        if (houseState is HouseUiState.Detail) {
            val selectedHouse = data.houses?.find { it.house == houseState.houseId }
            if (selectedHouse != null) {
                HouseExpansionOverlay(
                    house = selectedHouse,
                    circleInfo = houseState.circleInfo,
                    onClose = { viewModel.onHouseBack() }
                )
            }
        }
    }
}

@Composable
fun HouseExpansionOverlay(
    house: HouseData,
    circleInfo: CircleInfo,
    onClose: () -> Unit
) {
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    val screenHeight = config.screenHeightDp.dp

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
    val targetCorner = if (isExpanded) 28.dp else 50.dp

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
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
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
                    HouseDetailSheet(house)
                }
            }
        }
    }
    }
}

@Composable
fun Zone1Hero(data: AnalyzeFullResponse, userEmail: String?, accessToken: String?, listState: LazyListState) {
    val context = LocalContext.current
    val semanticColors = LocalSemanticColors.current
    
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
        targetValue = if (scrollOffset.value > 200) (scrollOffset.value / 100).dp.coerceAtMost(8.dp) else 0.dp,
        animationSpec = tween(500),
        label = "scrollBlur"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                translationY = parallaxOffset
                alpha = revealAlpha * chartAlphaBase
                renderEffect = if (blurAmount.toPx() > 0) {
                    // Note: Blur is only supported on Android 12+ (S) via RenderEffect,
                    // otherwise we use Modifier.blur which triggers recomposition.
                    // For now, we use Modifier.blur on the child box for safety.
                    null
                } else null
            },
        shape = RoundedCornerShape(32.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .blur(blurAmount)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // SACRED OBSERVATORY CHART REVEAL
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .scale(revealScale)
                    .alpha(revealAlpha), 
                    contentAlignment = Alignment.Center
                ) {
                    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
                    if (userEmail != null && accessToken != null) {
                        AsyncImage(
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
                        )
                    } else {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Staggered Identity Pills
                AnimatedVisibility(
                    visible = revealStage >= 2,
                    enter = fadeIn(tween(800)) + slideInVertically { it / 2 }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val lagna = data.ascendant?.sign ?: data.houses?.find { it.house == 1 }?.sign ?: "Lagna"
                        val maha = data.dasha?.current?.mahadasha?.planet?.plus(" MD") ?: "Mahadasha"
                        val antar = data.dasha?.current?.antardasha?.planet?.plus(" AD") ?: "Antardasha"

                        IdentityPill(lagna)
                        IdentityPill(maha)
                        IdentityPill(antar)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                AnimatedVisibility(
                    visible = revealStage >= 2,
                    enter = fadeIn(tween(1000, delayMillis = 400))
                ) {
                    data.chart_summary?.let { summary ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (!summary.headline.isNullOrEmpty()) {
                                Text(summary.headline, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            if (!summary.overview.isNullOrEmpty()) {
                                Text(summary.overview, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            if (!summary.overall_tone.isNullOrEmpty()) {
                                Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp)) {
                                    Text("Overall Tone: ${summary.overall_tone}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                                summary.strengths?.forEach { str ->
                                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 4.dp)) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = semanticColors.exalted, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(str, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                                summary.challenges?.forEach { chl ->
                                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 4.dp)) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = semanticColors.debilitated, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(chl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(8.dp))
                data.identity?.let { id ->
                    Text("${id.name ?: "Unknown"}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("${id.birth_details ?: ""} · ${id.ayanamsa ?: ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun IdentityPill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
    ) {
        Text(
            text, 
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun Zone2Ascendant(ascendant: AscendantData?) {
    if (ascendant == null) return
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Ascendant: ${ascendant.sign}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${ascendant.degree}° ${ascendant.nakshatra?.let { "· $it" } ?: ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(ascendant.interpretation ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun Zone3Dasha(currentDasha: CurrentDasha?) {
    if (currentDasha == null) return
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionHeader("Current Dasha Period", "Planetary timeline")
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                Spacer(modifier = Modifier.height(8.dp))
                currentDasha.antardasha?.let { ad ->
                    DashaTimelineItem(
                        title = "Antardasha: ${ad.planet}",
                        planetName = ad.planet,
                        startDate = ad.start,
                        endDate = ad.end,
                        interpretation = ad.interpretation ?: "",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                currentDasha.pratyantardasha?.let { pd ->
                    DashaTimelineItem(
                        title = "Pratyantardasha: ${pd.planet}",
                        planetName = pd.planet,
                        startDate = pd.start,
                        endDate = pd.end,
                        interpretation = "",
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { /* Navigate to full timeline */ }) {
                    Text("View Full Timeline →", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun Zone4Planets(planets: List<PlanetData>?) {
    if (planets == null || planets.isEmpty()) return
    
    Column(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
            Text("Planetary Gallery", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text("Swipe to explore each planet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        }
        
        val validPlanets = planets.take(9)
        val pagerState = rememberPagerState(
            initialPage = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % validPlanets.size)
        ) {
            Int.MAX_VALUE
        }
        
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 72.dp),
            pageSpacing = 8.dp,
            modifier = Modifier.fillMaxWidth().height(500.dp)
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
                    .blur(radius = (4.dp * pageOffset).coerceAtLeast(0.dp))
            ) {
                PlanetGalleryCard(planet)
            }
        }
    }
}

@Composable
fun PlanetGalleryCard(planet: PlanetData) {
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

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = planetRes),
                        contentDescription = planet.planet,
                        modifier = Modifier.size(72.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    if (planet.planet.lowercase() == "rahu" || planet.planet.lowercase() == "ketu") {
                        Box(modifier = Modifier.size(72.dp).background(AstroColors.getPlanetaryColor(planet.planet).copy(alpha = 0.4f), CircleShape))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(planet.planet, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        if (planet.combust) {
                            Text("🔥", modifier = Modifier.padding(start = 4.dp))
                        }
                        if (planet.retrograde) {
                            Text(" ℞", modifier = Modifier.padding(start = 4.dp), color = semanticColors.retrograde)
                        }
                    }
                    Text("${planet.sign} · House ${planet.house} · ${planet.degree}°", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = finalColor.copy(alpha = 0.2f), shape = CircleShape) {
                        Text(planet.dignity, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = finalColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Strength (Shadbala): ${planet.shadbala_percent}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                Box(modifier = Modifier.fillMaxWidth(planet.shadbala_percent / 100f).fillMaxHeight().background(finalColor, CircleShape))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!planet.dignity_interpretation.isNullOrEmpty()) {
                Text(planet.dignity_interpretation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            if (!planet.nakshatra.isNullOrEmpty()) {
                Text("Nakshatra: ${planet.nakshatra} (Pada ${planet.nakshatra_pada ?: "?"})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text("Lord: ${planet.nakshatra_lord ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
                if (!planet.nakshatra_interpretation.isNullOrEmpty()) {
                    Text(planet.nakshatra_interpretation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            if (!planet.house_placement_interpretation.isNullOrEmpty()) {
                Text("Placement Effect", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(planet.house_placement_interpretation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            if (!planet.conjunctions.isNullOrEmpty()) {
                Text("Conjunctions: ${planet.conjunctions.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                planet.conjunction_interpretations?.forEach {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            val aspects = planet.aspects_given?.joinToString(", ") ?: "None"
            val lordOf = planet.lord_of?.joinToString(", ") ?: "None"
            Text("Aspects Given: Houses $aspects", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text("Lord of: Houses $lordOf", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun Zone5Strengths(rankings: List<PlanetStrengthRank>?, listState: LazyListState, itemIndex: Int) {
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

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionHeader("Planet Strength Ranking", "Shadbala power analysis")
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${rank.rank}.", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.width(24.dp))
                                Text(rank.planet, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                                Text("${(rank.shadbala * 100).toInt() / 100f} Rupa", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val targetProgress = if(isRowVisible) (rank.shadbala.toFloat() / 10f).coerceIn(0f, 1f) else 0f
                            val progress by animateFloatAsState(targetValue = targetProgress, animationSpec = tween(1000), label = "rankProgress")
                            
                            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                                Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(AstroColors.getPlanetaryColor(rank.planet), CircleShape))
                            }
                            Text("${rank.dignity} · ${rank.summary}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
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

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionHeader("House Analysis", "Click a house to explore details")
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 4x3 Grid of houses
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 0 until 3) {
                            val houseNum = row * 3 + col + 1
                            val score = av.house_scores[houseNum - 1]
                            HouseCircle(
                                houseNum = houseNum,
                                score = score,
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
                    if (row < 3) Spacer(modifier = Modifier.height(16.dp))
                }

                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                
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
fun HouseCircle(
    houseNum: Int, 
    score: Int, 
    isDimmed: Boolean = false,
    isSelected: Boolean = false,
    isPulsing: Boolean = false,
    onClick: (CircleInfo) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val scale by animateFloatAsState(
        targetValue = if (isPulsing) 1.12f else if (isSelected) 1.22f else 1f,
        animationSpec = if (isPulsing) tween(180) else spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isDimmed) 0.35f else 1f,
        animationSpec = tween(300),
        label = "alpha"
    )

    val animatedSweep by animateFloatAsState(
        targetValue = (score.toFloat() / 45f).coerceIn(0f, 1f) * 360f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "sweep"
    )
    
    val scoreColor = when {
        score > 30 -> Color(0xFF4CAF50)
        score > 25 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .onGloballyPositioned { coords = it }
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
                coords?.let {
                    onClick(CircleInfo(houseNum, it.positionInRoot(), it.size))
                }
            }
    ) {

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
            // Animated Ring
            Canvas(modifier = Modifier.size(56.dp)) {
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = scoreColor,
                    startAngle = -90f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            // House Number Circle
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "H$houseNum",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        Text(
            "$score",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = scoreColor
        )
    }
}

@Composable
fun HouseDetailSheet(house: HouseData) {
    val semanticColors = LocalSemanticColors.current
    val strengthColor = when(house.strength_assessment?.lowercase()) {
        "strong" -> Color(0xFF4CAF50)
        "weak" -> Color(0xFFF44336)
        else -> Color(0xFFFFC107)
    }

    Column(modifier = Modifier.padding(24.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) { Text("${house.house}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${house.name} · ${house.sign}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Areas of Life", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(house.areas.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(16.dp))

        Text("House Lord (Owner)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = lordPlanetRes),
                    contentDescription = house.lord,
                    modifier = Modifier.size(24.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                if (house.lord?.lowercase() == "rahu" || house.lord?.lowercase() == "ketu") {
                    Box(modifier = Modifier.size(24.dp).background(AstroColors.getPlanetaryColor(house.lord).copy(alpha = 0.4f), CircleShape))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Lord: ${house.lord} ${if(house.lord_house != null) "(Placed in House ${house.lord_house})" else ""} ${house.lord_dignity?.let { "- $it" } ?: ""}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        if (!house.lord_interpretation.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(house.lord_interpretation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (house.occupants.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Planets in House", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = planetRes),
                            contentDescription = occupant.planet,
                            modifier = Modifier.size(20.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        if (occupant.planet.lowercase() == "rahu" || occupant.planet.lowercase() == "ketu") {
                            Box(modifier = Modifier.size(20.dp).background(AstroColors.getPlanetaryColor(occupant.planet).copy(alpha = 0.4f), CircleShape))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${occupant.planet} (${occupant.dignity})", style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (house.aspects_received?.isNotEmpty() == true) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Aspects Received From", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(house.aspects_received.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Zone9Transits(transits: TransitData?) {
    if (transits == null || transits.planets == null) return
    
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SectionHeader("Current Transits", "Planetary movement as of ${transits.date ?: "Today"}")
        }
        
        val listState = rememberLazyListState()
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            items(transits.planets) { planet ->
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
                        .width(180.dp)
                        .height(180.dp)
                        .graphicsLayer {
                            this.rotationY = rotationY.value
                            cameraDistance = 8 * density
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(32.dp).background(AstroColors.getPlanetaryColor(planet.planet).copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(planet.planet.take(1), fontWeight = FontWeight.Black, color = AstroColors.getPlanetaryColor(planet.planet))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(planet.planet, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("${planet.current_sign} · House ${planet.current_house_in_natal}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(planet.transit_interpretation ?: "", style = MaterialTheme.typography.labelSmall, maxLines = 4, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Zone10Timeline(timeline: DashaTimeline?, current: CurrentDasha?) {
    if (timeline == null || timeline.mahadashas == null) return
    
    var showFullTimeline by remember { mutableStateOf(false) }

    val sortedMDs = timeline.mahadashas.toList().sortedBy { it.second.start }
    val currentMDIndex = sortedMDs.indexOfFirst { it.first == current?.mahadasha?.planet }
    
    // Filter logic
    val displayMDs = if (showFullTimeline) {
        // Full view: 1 Past + Current + All Future
        val startIndex = if (currentMDIndex > 0) currentMDIndex - 1 else 0
        sortedMDs.subList(startIndex, sortedMDs.size)
    } else {
        // Condensed view: Current + Next (Upcoming)
        if (currentMDIndex >= 0) {
            val endIdx = (currentMDIndex + 2).coerceAtMost(sortedMDs.size)
            sortedMDs.subList(currentMDIndex, endIdx)
        } else sortedMDs.take(2)
    }
    
    val initialPage = if (showFullTimeline) (if (currentMDIndex > 0) 1 else 0) else 0

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            SectionHeader("Dasha Timeline", if (showFullTimeline) "Full life chapters" else "Current & upcoming")
            TextButton(onClick = { showFullTimeline = !showFullTimeline }) {
                Text(if (showFullTimeline) "Show Less" else "View Full Timeline", style = MaterialTheme.typography.labelMedium)
            }
        }

        val pagerState = rememberPagerState(
            initialPage = initialPage
        ) {
            displayMDs.size
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 48.dp),
            pageSpacing = 16.dp,
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        
                        // Spatial depth
                        shadowElevation = (1f - pageOffset.coerceIn(0f, 1f)) * 10f
                        rotationX = (pagerState.currentPage - page + pagerState.currentPageOffsetFraction) * 10f
                        cameraDistance = 10f * density
                    }
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(AstroColors.getPlanetaryColor(planet).copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(planet.take(1), fontWeight = FontWeight.Black, color = AstroColors.getPlanetaryColor(planet), fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("$planet Mahadasha", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = AstroColors.getPlanetaryColor(planet))
                                Text("${md.start} - ${md.end}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                            Surface(
                                color = if (tag == "CURRENT") AstroColors.getPlanetaryColor(planet) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ) {
                                Text(tag, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = if (tag == "CURRENT") Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Zone8KeyThemes(themes: List<KeyTheme>?) {
    if (themes == null) return
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionHeader("Key Insights", "Direct chart themes from backend")
        
        themes.forEach { theme ->
            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(theme.title ?: theme.theme ?: "Insight", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(theme.interpretation ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun KundliSkeleton() {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // Hero Chart Skeleton
        Box(modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(32.dp))
            .shimmerEffect()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Staggered Constellation Rows
        repeat(3) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier
                    .weight(if (rowIndex % 2 == 0) 2f else 1f)
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmerEffect()
                )
                Box(modifier = Modifier
                    .weight(if (rowIndex % 2 == 0) 1f else 2f)
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmerEffect()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Large content blocks
        Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(24.dp)).shimmerEffect())
    }
}

/**
 * Sacred Parallax: Cinematic layered scroll effect.
 * Upper content scales down to 0.96 and fades; lower content rises slowly.
 */
fun Modifier.sacredParallax(index: Int, listState: LazyListState): Modifier = composed {
    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
    
    graphicsLayer {
        val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == index }
        if (visibleItem != null) {
            val offset = visibleItem.offset.toFloat()
            val viewportHeight = layoutInfo.viewportEndOffset.toFloat()
            
            // Item is at the top (scrolling out)
            if (offset < 0) {
                val progress = (-offset / visibleItem.size.toFloat()).coerceIn(0f, 1f)
                scaleX = lerp(1f, 0.96f, progress)
                scaleY = lerp(1f, 0.96f, progress)
                alpha = lerp(1f, 0.4f, progress)
            } 
            // Item is at the bottom (scrolling in)
            else if (offset + visibleItem.size > viewportHeight) {
                val progress = ((offset + visibleItem.size - viewportHeight) / visibleItem.size.toFloat()).coerceIn(0f, 1f)
                translationY = progress * 60f
                alpha = lerp(1f, 0.6f, progress)
            }
        } else {
            // Not visible, keep faint to avoid sudden pops
            alpha = 0f
        }
    }
}
