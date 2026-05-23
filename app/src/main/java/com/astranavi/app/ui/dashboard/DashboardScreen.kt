package com.astranavi.app.ui.dashboard

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import com.astranavi.app.ui.components.LocalBackgroundScrollState
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.decode.SvgDecoder
import com.astranavi.app.ui.components.DashaCircularItem
import com.astranavi.app.data.model.*
import java.util.*
import com.astranavi.app.util.ZodiacMapper
import com.astranavi.app.ui.components.ShimmerBlock
import com.astranavi.app.ui.components.shimmerEffect
import com.astranavi.app.ui.components.shimmerSweepEffect
import com.astranavi.app.R
import com.astranavi.app.ui.theme.AstroColors
import com.astranavi.app.ui.components.ScoreColors
import com.astranavi.app.ui.components.floatingCard
import com.astranavi.app.ui.components.responsiveMetrics
import com.astranavi.app.LocalBottomBarHeight
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
import com.astranavi.app.ui.components.PaywallCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.astranavi.app.util.LocaleFormatter
import com.astranavi.app.util.currentAppLocale
import com.astranavi.app.util.nextLocalHourMillis
import kotlinx.coroutines.isActive

private const val CARD_SURFACE_ALPHA = 0.68f
private const val CARD_SURFACE_LIGHT_ALPHA = 0.5f

fun titleCase(text: String?): String {
    if (text.isNullOrBlank()) return ""
    return text.trim().split("\\s+".toRegex()).joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPlans: () -> Unit = {},
    onNavigateToRashis: (String?) -> Unit = {},
    onNavigateToExperts: () -> Unit = {},
    onNavigateToChat: (String?, String?) -> Unit = { _, _ -> },
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
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState !is DashboardState.Loading) {
            isRefreshing = false
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchDashboardData(silent = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            val target = nextLocalHourMillis()
            val sleep = (target - System.currentTimeMillis() + 1_000L).coerceAtLeast(1_000L)
            delay(sleep)
            viewModel.fetchDashboardData(silent = true)
        }
    }

    CompositionLocalProvider(LocalBackgroundScrollState provides scrollState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.fetchDashboardData(forceRefresh = true)
            },
            modifier = Modifier.fillMaxSize()
        ) {
            // Note: Box is needed as a direct child of PullToRefreshBox to anchor scrollable content
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    is DashboardState.Loading -> {
                        DashboardSkeleton()
                    }
                    is DashboardState.Success -> {
                        androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
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
                    }
                    is DashboardState.Error -> {
                        ErrorView(message = uiState.message, onRetry = { viewModel.fetchDashboardData(forceRefresh = true) })
                    }
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    uiState: DashboardState.Success,
    viewModel: DashboardViewModel,
    scrollState: ScrollState,
    onNavigateToExperts: () -> Unit = {},
    onNavigateToChat: (String?, String?) -> Unit = { _, _ -> },
    onNavigateToKundli: () -> Unit = {},
    onNavigateToRashis: (String?) -> Unit = {},
    onNavigateToMatch: () -> Unit = {},
    onNavigateToMatchHistory: () -> Unit = {},
    onNavigateToNakshatras: () -> Unit = {},
    onNavigateToPlanets: () -> Unit = {},
    onNavigateToConsult: () -> Unit = {},
    onNavigateToForecast: (String?) -> Unit = {}
) {
    val horoscope = uiState.horoscope
    val isLightMode = MaterialTheme.colorScheme.background.luminance() > 0.5f

    val context = LocalContext.current
    val homeUiState = remember(uiState, isLightMode, context) {
        DailyHomeUiMapper.map(
            horoscope = horoscope,
            moonSign = uiState.moonSign,
            sunSign = uiState.sunSign,
            lagnaSign = uiState.lagnaSign,
            userName = uiState.userName,
            isDarkTheme = !isLightMode,
            context = context
        )
    }

    val generalPalette = remember(horoscope.score.overall, isLightMode) {
        ScoreColors.paletteFor("general", horoscope.score.overall, isDarkTheme = !isLightMode)
    }
    val themeColor = if (isLightMode) generalPalette.main else generalPalette.glow
    val weeklyForecastColor = remember(uiState.forecast, isLightMode) {
        if (uiState.forecast != null) {
            val pal = ScoreColors.paletteFor("forecast", 80, isDarkTheme = !isLightMode)
            if (isLightMode) pal.main else pal.glow
        } else themeColor
    }

    // Bottom Sheet / Side Drawer State
    var activeBottomSheet by remember { mutableStateOf<DashboardBottomSheetState>(DashboardBottomSheetState.None) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val responsive = responsiveMetrics()

    val familyMembers = remember(context) {
        listOf(
            FamilyMemberUiModel(
                id = "mother",
                name = context.getString(R.string.dashboard_relation_mother),
                relation = context.getString(R.string.dashboard_relation_mother),
                score = 85,
                bondingStatus = context.getString(R.string.dashboard_relation_status_harmonious),
                communicationLevel = context.getString(R.string.dashboard_relation_level_excellent),
                emotionalConnection = context.getString(R.string.dashboard_relation_bond_strong),
                advice = context.getString(R.string.dashboard_relation_advice_mother),
                avatarId = 1
            ),
            FamilyMemberUiModel(
                id = "father",
                name = context.getString(R.string.dashboard_relation_father),
                relation = context.getString(R.string.dashboard_relation_father),
                score = 72,
                bondingStatus = context.getString(R.string.dashboard_relation_status_stable),
                communicationLevel = context.getString(R.string.dashboard_relation_level_moderate),
                emotionalConnection = context.getString(R.string.dashboard_relation_bond_warm),
                advice = context.getString(R.string.dashboard_relation_advice_father),
                avatarId = 2
            ),
            FamilyMemberUiModel(
                id = "partner",
                name = context.getString(R.string.dashboard_relation_partner),
                relation = context.getString(R.string.dashboard_relation_partner),
                score = 91,
                bondingStatus = context.getString(R.string.dashboard_relation_status_harmonious),
                communicationLevel = context.getString(R.string.dashboard_relation_level_deep_intimate),
                emotionalConnection = context.getString(R.string.dashboard_relation_bond_extremely_close),
                advice = context.getString(R.string.dashboard_relation_advice_partner),
                avatarId = 3
            ),
            FamilyMemberUiModel(
                id = "children",
                name = context.getString(R.string.dashboard_relation_children),
                relation = context.getString(R.string.dashboard_relation_child),
                score = 64,
                bondingStatus = context.getString(R.string.dashboard_relation_status_needs_attention),
                communicationLevel = context.getString(R.string.dashboard_relation_level_distracted),
                emotionalConnection = context.getString(R.string.dashboard_relation_bond_affectionate),
                advice = context.getString(R.string.dashboard_relation_advice_child),
                avatarId = 4
            ),
            FamilyMemberUiModel(
                id = "friends",
                name = context.getString(R.string.dashboard_relation_best_friend),
                relation = context.getString(R.string.dashboard_relation_friend),
                score = 78,
                bondingStatus = context.getString(R.string.dashboard_relation_status_stable),
                communicationLevel = context.getString(R.string.dashboard_relation_level_good),
                emotionalConnection = context.getString(R.string.dashboard_relation_bond_steady),
                advice = context.getString(R.string.dashboard_relation_advice_friend),
                avatarId = 5
            )
        )
    }

    // Alert surface intentionally hidden; data still produced by VM.
    // Cosmic Hour content now lives inside DailyHeroCard.
    // Restore as in-app notification surface when notifications land. See next steps.md.
    val showAlertRow = false

    // Renders the main dashboard scrollable content
    val mainContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = responsive.pagePadding)
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Greeting + Daily Chips (with inline compact weekly forecast on fold/tablet)
            if (responsive.isMediumWidth && uiState.forecast != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        GreetingAndChips(
                            header = homeUiState.header,
                            chips = homeUiState.chips,
                            onChipClick = { activeBottomSheet = DashboardBottomSheetState.AstroDetails }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        WeeklyForecastCompact(
                            forecast = uiState.forecast,
                            themeColor = weeklyForecastColor,
                            onNavigateToForecast = onNavigateToForecast
                        )
                    }
                }
            } else {
                GreetingAndChips(
                    header = homeUiState.header,
                    chips = homeUiState.chips,
                    onChipClick = { activeBottomSheet = DashboardBottomSheetState.AstroDetails }
                )
            }

            // 2. Daily Hero Card (now hosts Cosmic Hour content + dominant-planet visual)
            DailyHeroCard(
                hero = homeUiState.hero,
                cosmicHour = homeUiState.cosmicHour,
                themeColor = themeColor,
                onScoreClick = { activeBottomSheet = DashboardBottomSheetState.FullEnergy },
                onCosmicHourClick = { activeBottomSheet = DashboardBottomSheetState.CosmicHourDetails }
            )

            // 3. Today's Life Areas Carousel
            SectionHeader(title = stringResource(R.string.dashboard_header_life_areas))
            LifeAreasCarousel(
                areas = homeUiState.lifeAreas,
                onAreaClick = { area -> activeBottomSheet = DashboardBottomSheetState.LifeAreaDetails(area.id) }
            )

            // 4. Weekly Forecast (mobile only — fold/tablet shows compact variant in top row)
            if (uiState.forecast != null && !responsive.isMediumWidth) {
                SectionHeader(stringResource(R.string.dashboard_header_current_week))
                WeeklyForecastSection(uiState.forecast, weeklyForecastColor, onNavigateToForecast)
            }

            // 5. Kundli Quick Peek
            if (uiState.kundliPreview != null) {
                SectionHeader(stringResource(R.string.dashboard_header_kundli))
                KundliPeekCard(
                    data = uiState.kundliPreview,
                    userEmail = uiState.userEmail,
                    accessToken = uiState.accessToken,
                    onClick = onNavigateToKundli
                )
            }

            // 6. Family & Relationships
            SectionHeader(title = stringResource(R.string.dashboard_header_family))
            FamilyRelationshipsSection(
                members = familyMembers,
                isTablet = responsive.isTabletWidth,
                onMemberClick = { member -> activeBottomSheet = DashboardBottomSheetState.FamilyMemberDetails(member.id) }
            )

            // 7. Alert (gated off; data preserved)
            if (showAlertRow) {
                SectionHeader(title = stringResource(R.string.dashboard_header_astro_focus))
                AlertAndCosmicHourRow(
                    alert = homeUiState.alert,
                    cosmicHour = homeUiState.cosmicHour,
                    onAlertClick = { activeBottomSheet = DashboardBottomSheetState.AlertDetails },
                    onCosmicClick = { activeBottomSheet = DashboardBottomSheetState.CosmicHourDetails }
                )
            }

            // 8. Consult Teaser
            if (!uiState.recentConsultations.isNullOrEmpty()) {
                SectionHeader(stringResource(R.string.dashboard_header_recent_consultation))
                ConsultTeaserCard(uiState.recentConsultations.first(), onNavigateToConsult)
            }

            Spacer(modifier = Modifier.height(LocalBottomBarHeight.current + 24.dp))
        }
    }

    // Determine Layout Structure based on Screen Class
    if (responsive.isTabletWidth) {
        // Tablet Side-by-Side Split View
        Row(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
            ) {
                mainContent()
            }

            // Right-side Slide-in Detail Drawer
            AnimatedVisibility(
                visible = activeBottomSheet != DashboardBottomSheetState.None,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                TabletDetailDrawer(
                    state = activeBottomSheet,
                    homeUiState = homeUiState,
                    familyMembers = familyMembers,
                    onNavigateToChat = onNavigateToChat,
                    onDismiss = { activeBottomSheet = DashboardBottomSheetState.None }
                )
            }
        }
    } else {
        // Mobile Single Column View
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
        ) {
            mainContent()
        }

        // Native Modal Bottom Sheet
        if (activeBottomSheet != DashboardBottomSheetState.None) {
            ModalBottomSheet(
                onDismissRequest = { activeBottomSheet = DashboardBottomSheetState.None },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)) }
            ) {
                BottomSheetContentSelector(
                    state = activeBottomSheet,
                    homeUiState = homeUiState,
                    familyMembers = familyMembers,
                    onNavigateToChat = onNavigateToChat,
                    onDismiss = { activeBottomSheet = DashboardBottomSheetState.None }
                )
            }
        }
    }
}

// --- TABLET DETAIL DRAWER COMPONENT ---
@Composable
fun TabletDetailDrawer(
    state: DashboardBottomSheetState,
    homeUiState: DailyHomeUiState,
    familyMembers: List<FamilyMemberUiModel>,
    onNavigateToChat: (String?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxHeight()
            .width(420.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_btn_cosmic_detail),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.dashboard_btn_close_detail))
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            Box(modifier = Modifier.weight(1f)) {
                BottomSheetContentSelector(
                    state = state,
                    homeUiState = homeUiState,
                    familyMembers = familyMembers,
                    onNavigateToChat = onNavigateToChat,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

// --- BOTTOM SHEET SELECTOR ---
@Composable
fun BottomSheetContentSelector(
    state: DashboardBottomSheetState,
    homeUiState: DailyHomeUiState,
    familyMembers: List<FamilyMemberUiModel>,
    onNavigateToChat: (String?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    when (state) {
        is DashboardBottomSheetState.AstroDetails -> {
            AstroDetailsBottomSheetContent(panchanga = homeUiState.panchanga, onAskNavi = { prompt -> onNavigateToChat(prompt, null) }, onDismiss = onDismiss)
        }
        is DashboardBottomSheetState.FullEnergy -> {
            FullEnergyBottomSheetContent(hero = homeUiState.hero, alert = homeUiState.alert, onAskNavi = { prompt -> onNavigateToChat(prompt, null) }, onDismiss = onDismiss)
        }
        is DashboardBottomSheetState.LifeAreaDetails -> {
            val area = homeUiState.lifeAreas.find { it.id == state.areaId }
            if (area != null) {
                LifeAreaBottomSheetContent(area = area, hero = homeUiState.hero, onAskNavi = { prompt -> onNavigateToChat(prompt, area.id) }, onDismiss = onDismiss)
            }
        }
        is DashboardBottomSheetState.AlertDetails -> {
            AlertBottomSheetContent(alert = homeUiState.alert, onAskNavi = { prompt -> onNavigateToChat(prompt, null) }, onDismiss = onDismiss)
        }
        is DashboardBottomSheetState.CosmicHourDetails -> {
            CosmicHourBottomSheetContent(cosmicHour = homeUiState.cosmicHour, onDismiss = onDismiss)
        }
        is DashboardBottomSheetState.FamilyMemberDetails -> {
            val member = familyMembers.find { it.id == state.memberId }
            if (member != null) {
                FamilyMemberBottomSheetContent(member = member, onAskNavi = { prompt -> onNavigateToChat(prompt, null) }, onDismiss = onDismiss)
            }
        }
        else -> {}
    }
}

// ─── DASHBOARD SUB-COMPONENTS ───────────────────────────────────────────────

@Composable
fun IdentityBadges(
    moonSign: String?, 
    sunSign: String?, 
    planetary: PlanetaryData?,
    selectedBadgeLabel: String?,
    onBadgeClick: (String, String, String, Color) -> Unit,
    onReadMoreClick: (String) -> Unit
) {
    val responsive = responsiveMetrics()
    val dominantColor = planetary?.dominant_planet?.let { AstroColors.getPlanetaryColor(it) } ?: AstroColors.Moon
    val moonBadge: @Composable () -> Unit = {
        BadgeCircle(
            label = stringResource(R.string.dashboard_moon), 
            sign = moonSign, 
            textColor = AstroColors.Moon, 
            ringColor = dominantColor,
            isSelected = selectedBadgeLabel == "Moon",
            onReadMoreClick = { moonSign?.let { onReadMoreClick(it) } }
        ) {
            if (moonSign != null) onBadgeClick(moonSign, ZodiacMapper.getDisplayName(moonSign), "Moon", AstroColors.Moon)
        }
    }
    val sunBadge: @Composable () -> Unit = {
        BadgeCircle(
            label = stringResource(R.string.dashboard_sun), 
            sign = sunSign, 
            textColor = AstroColors.Sun, 
            ringColor = AstroColors.Sun,
            isSelected = selectedBadgeLabel == "Sun",
            onReadMoreClick = { sunSign?.let { onReadMoreClick(it) } }
        ) {
            if (sunSign != null) onBadgeClick(sunSign, ZodiacMapper.getDisplayName(sunSign), "Sun", AstroColors.Sun)
        }
    }

    if (responsive.isCompactWidth || responsive.isLargeFont) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            moonBadge()
            sunBadge()
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.End) {
            moonBadge()
            sunBadge()
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
    val responsive = responsiveMetrics()
    val englishName = ZodiacMapper.getEnglishName(sign)
    val displayName = ZodiacMapper.getDisplayName(sign)
    val engDisp = englishName?.replaceFirstChar { it.uppercase() } ?: ""
    val fullDisplayName = if (engDisp.isNotEmpty() && engDisp != displayName) "$displayName ($engDisp)" else displayName
    val badgeHeight = if (responsive.isVeryCompactWidth || responsive.isLargeFont) 44.dp else 48.dp
    val zodiacIconSize = if (responsive.isVeryCompactWidth || responsive.isLargeFont) 21.dp else 24.dp

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) ringColor.copy(alpha = 0.75f) else ringColor.copy(alpha = 0.28f)
        ),
        modifier = Modifier
            .width(responsive.identityBadgeWidth)
            .height(badgeHeight)
            .shadow(
                elevation = if (isSelected) 4.dp else 1.dp,
                shape = RoundedCornerShape(999.dp),
                ambientColor = Color.Transparent,
                spotColor = ringColor.copy(alpha = 0.22f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = if (isSelected) onReadMoreClick else onClick
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (responsive.isVeryCompactWidth || responsive.isLargeFont) 10.dp else 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = if (responsive.isVeryCompactWidth || responsive.isLargeFont) 9.sp else 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.sp
                )
                Text(
                    text = fullDisplayName,
                    fontSize = if (responsive.isVeryCompactWidth || responsive.isLargeFont) 11.sp else if (responsive.isCompactWidth) 12.sp else 14.sp,
                    lineHeight = if (responsive.isVeryCompactWidth || responsive.isLargeFont) 13.sp else if (responsive.isCompactWidth) 14.sp else 16.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.sp
                )
            }

            if (englishName != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(zodiacIconSize)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.62f), CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AstroAssetImage(
                        assetName = englishName,
                        contentDescription = fullDisplayName,
                        modifier = Modifier.fillMaxSize().padding(3.dp),
                        fallbackTint = textColor
                    )
                }
            }
        }
    }
}

private fun resolvePlanetDrawableName(planet: String?): String? {
    return when (planet?.trim()?.lowercase()) {
        "sun", "surya" -> "sun"
        "moon", "chandra" -> "moon"
        "mars", "mangal" -> "mars"
        "mercury", "budh" -> "mercury"
        "jupiter", "guru" -> "jupiter"
        "venus", "shukra" -> "venus"
        "saturn", "shani" -> "saturn"
        else -> null
    }
}

private fun normalizePlanetName(planet: String?): String? {
    return when (planet?.trim()?.lowercase()) {
        "ke", "ketu" -> "ketu"
        "ra", "rahu" -> "rahu"
        else -> planet
    }
}

data class TriggerStyle(val color: Color, val asset: String?)

@Composable
fun triggerStyleFor(type: String): TriggerStyle {
    return when (type.lowercase()) {
        "social" -> TriggerStyle(AstroColors.Venus, "venus")
        "emotional" -> TriggerStyle(AstroColors.Moon, "moon")
        "energy" -> TriggerStyle(AstroColors.Mars, "mars")
        else -> TriggerStyle(MaterialTheme.colorScheme.primary, null)
    }
}

@Composable
fun AstroAssetImage(
    assetName: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackTint: Color = Color.Unspecified
) {
    val context = LocalContext.current
    val resolvedFallbackTint = if (fallbackTint == Color.Unspecified) {
        MaterialTheme.colorScheme.primary
    } else {
        fallbackTint
    }
    val resId = remember(assetName, context.packageName) {
        assetName?.let { context.resources.getIdentifier(it, "drawable", context.packageName) } ?: 0
    }

    if (resId != 0) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context).data(resId).crossfade(true).build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit
        ) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Empty) {
                Box(modifier = modifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = resolvedFallbackTint,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else if (state is AsyncImagePainter.State.Error) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = contentDescription,
                    modifier = modifier,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            } else {
                SubcomposeAsyncImageContent()
            }
        }
    } else {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = contentDescription,
            modifier = modifier,
            tint = resolvedFallbackTint
        )
    }
}

internal fun dailyToneColor(type: String?, fallback: Color): Color {
    return when (type?.lowercase()) {
        "positive" -> Color(0xFF159957)
        "warning", "challenging" -> Color(0xFFD97706)
        "negative" -> Color(0xFFB91C1C)
        else -> fallback
    }
}

private fun firstSentence(text: String?, maxChars: Int = 118): String {
    val resolved = resolveAreaLabels(text.orEmpty()).trim()
    if (resolved.isEmpty()) return ""
    val sentence = resolved.split(".").firstOrNull()?.trim().orEmpty()
    val candidate = if (sentence.isNotEmpty()) "$sentence." else resolved
    return if (candidate.length <= maxChars) {
        candidate
    } else {
        candidate.take(maxChars).trimEnd('.', ',', ' ') + "..."
    }
}

// ─── DAILY SNAPSHOT CARD (unified: Hero + Focus + Trigger + Lucky) ─────────
@Composable
fun DailySnapshotCard(
    horoscope: HoroscopeResponse,
    themeColor: Color,
    onChat: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val responsive = responsiveMetrics()
    val compact = responsive.isCompactWidth || responsive.isLargeFont
    val mood = horoscope.mood?.value?.trim().takeUnless { it.isNullOrBlank() } ?: "Aligned"
    val moodColor = dailyToneColor(horoscope.mood?.type, themeColor)
    val technical = resolveAreaLabels(horoscope.alerts?.primary?.technical.orEmpty()).trim()
    val dominantPlanet = horoscope.planetary?.dominant_planet
    val normalizedPlanet = normalizePlanetName(dominantPlanet)
    val planetColor = AstroColors.getPlanetaryColor(normalizedPlanet)
    val planetAsset = resolvePlanetDrawableName(dominantPlanet)
    val heroPlanetLabel = normalizedPlanet?.replaceFirstChar { it.uppercase() }
    val focusText = firstSentence(
        horoscope.tip?.text ?: horoscope.current_state?.advice_now,
        maxChars = if (compact) 74 else if (responsive.isMediumWidth) 118 else 90
    )
    val activeTrigger = remember(horoscope.time_triggers) {
        findActiveOrUpcomingTrigger(horoscope.time_triggers)
    }
    val activeDashaText = horoscope.planetary?.active_dasha
        ?.replace("Mahadasha", "MD", ignoreCase = true)
        ?.replace("Antardasha", "AD", ignoreCase = true)
        ?.replace("/", " / ")
    val leadingColumnWidth = when {
        compact -> 72.dp
        responsive.isMediumWidth -> 104.dp
        else -> 82.dp
    }
    val dominantIconSize = when {
        compact -> 44.dp
        responsive.isMediumWidth -> 60.dp
        else -> 56.dp
    }
    val scoreFontSize = responsive.snapshotScoreFontSize
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color.Transparent,
                spotColor = moodColor.copy(alpha = 0.18f)
            ),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = CARD_SURFACE_ALPHA),
        border = BorderStroke(1.dp, moodColor.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(responsive.cardPadding)) {
            val leftContent = @Composable {
                Column(
                    modifier = Modifier.width(if (compact) 72.dp else leadingColumnWidth),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "DOMINANT",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = planetColor.copy(alpha = 0.5f),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(responsive.snapshotImageSize)
                            .background(planetColor.copy(alpha = 0.11f), CircleShape)
                            .border(1.dp, planetColor.copy(alpha = 0.22f), CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AstroAssetImage(
                            assetName = planetAsset,
                            contentDescription = heroPlanetLabel ?: "Dominant planet",
                            modifier = Modifier.fillMaxSize().padding(if (compact) 8.dp else 10.dp),
                            fallbackTint = planetColor
                        )
                    }

                    if (!heroPlanetLabel.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = planetColor.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, planetColor.copy(alpha = 0.18f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(planetColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    heroPlanetLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = planetColor
                                )
                            }
                        }
                    }
                }
            }

            val rightContent = @Composable {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${horoscope.score.overall}",
                            fontSize = scoreFontSize,
                            lineHeight = (scoreFontSize.value + 4).sp,
                            fontWeight = FontWeight.Black,
                            color = moodColor,
                            letterSpacing = 0.sp
                        )
                        Spacer(modifier = Modifier.width(if (compact) 9.dp else 12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${titleCase(mood)} Day",
                                fontSize = responsive.snapshotTitleFontSize,
                                lineHeight = if (compact) 16.sp else 18.sp,
                                fontWeight = FontWeight.Black,
                                color = moodColor,
                                letterSpacing = 0.sp,
                                maxLines = if (compact) 2 else 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (focusText.isNotEmpty()) {
                                Text(
                                    text = focusText,
                                    fontSize = if (compact) 10.sp else 11.sp,
                                    lineHeight = if (compact) 14.sp else 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (technical.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AstroAssetImage(
                                        assetName = resolveTransitPlanetAsset(technical),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        fallbackTint = moodColor
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = technical,
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = moodColor,
                                        maxLines = if (compact) 2 else 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    if (!activeDashaText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    "DASHA",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        activeDashaText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Button(
                                onClick = { onChat(context.getString(R.string.dashboard_prompt_cosmic_guidance)) },
                                modifier = Modifier.height(if (compact) 32.dp else 30.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AstroColors.Sun),
                                contentPadding = PaddingValues(horizontal = if (compact) 8.dp else 10.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.dashboard_btn_ask_navi), fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                leftContent()
                Spacer(modifier = Modifier.width(if (compact) 12.dp else 16.dp))
                rightContent()
            }

            // Transit / power hour section
            if (activeTrigger != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // RIGHT COLUMN: current/upcoming time trigger
                    if (activeTrigger != null) {
                        val style = triggerStyleFor(activeTrigger.type)
                        val triggerColor = style.color
                        val triggerAsset = style.asset
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                "TRANSIT",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                AstroAssetImage(
                                    assetName = triggerAsset,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp).padding(top = 1.dp),
                                    fallbackTint = triggerColor.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeTrigger.label,
                                        fontSize = 12.sp,
                                        lineHeight = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = triggerColor,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = formatTimeRange(activeTrigger.start, activeTrigger.end),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = resolveAreaLabels(activeTrigger.advice),
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                                        maxLines = if (compact) 3 else 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!activeTrigger.reason.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = resolveAreaLabels(activeTrigger.reason),
                                            fontSize = 9.sp,
                                            lineHeight = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.44f),
                                            maxLines = if (compact) 2 else 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



private fun resolveTransitPlanetAsset(text: String): String? {
    return listOf("sun", "moon", "mars", "mercury", "jupiter", "venus", "saturn")
        .firstOrNull { text.contains(it, ignoreCase = true) }
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
                text = resolveAreaLabels(tip.text),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
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

private val AREA_LABEL_MAP = mapOf(
    "vitality" to "Vitality",
    "income" to "Income",
    "home" to "Home",
    "romance" to "Romance",
    "wealth" to "Wealth",
    "self" to "Self",
    "health" to "Health",
    "career" to "Career",
    "love" to "Love",
    "finance" to "Finance",
    "communication" to "Communication",
    "spirituality" to "Spirituality",
    "family" to "Family"
)

private fun resolveAreaLabels(text: String): String {
    var result = text
    AREA_LABEL_MAP.forEach { (key, label) ->
        result = result.replace("area_label.$key", label)
    }
    result = result.replace(Regex("area_label\\.([A-Za-z_]+)")) { match ->
        match.groupValues[1]
            .replace("_", " ")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
    return result
}

private fun formatTimeRange(start: String, end: String): String {
    fun format12h(time: String): String {
        return try {
            val parts = time.split(":")
            val hour = parts[0].trim().toInt()
            val minute = parts[1].trim().split(" ")[0].toInt()
            val ampm = if (hour >= 12) "PM" else "AM"
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            "$displayHour:${if (minute < 10) "0$minute" else "$minute"} $ampm"
        } catch (e: Exception) { time }
    }
    return "${format12h(start)} - ${format12h(end)}"
}



@Composable
fun ActiveGuidanceCard(
    trigger: TimeTrigger,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val style = triggerStyleFor(trigger.type)
    val triggerColor = style.color
    val triggerAsset = style.asset
    val cleanLabel = trigger.label
        .replace("✨", "")
        .trim()

    Surface(
        modifier = modifier
            .heightIn(min = 138.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Transparent,
                spotColor = triggerColor.copy(alpha = 0.16f)
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = CARD_SURFACE_ALPHA),
        border = BorderStroke(1.dp, triggerColor.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(triggerColor.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, triggerColor.copy(alpha = 0.22f), CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AstroAssetImage(
                        assetName = triggerAsset,
                        contentDescription = cleanLabel,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        fallbackTint = triggerColor
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titleCase(cleanLabel),
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = triggerColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = 0.sp
                    )
                    Text(
                        text = formatTimeRange(trigger.start, trigger.end),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Visible
                    )
                }
                if (onClick != null) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "View all",
                        tint = triggerColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = trigger.advice.trim()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() },
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (!trigger.reason.isNullOrBlank()) {
                Text(
                    text = trigger.reason,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTriggersBottomSheet(
    triggers: List<TimeTrigger>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.dashboard_label_time_triggers),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            triggers.forEach { trigger ->
                val style = triggerStyleFor(trigger.type)
                val triggerColor = style.color
                val triggerAsset = style.asset

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = triggerColor.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, triggerColor.copy(alpha = 0.18f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(triggerColor.copy(alpha = 0.1f), CircleShape)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            AstroAssetImage(
                                assetName = triggerAsset,
                                contentDescription = trigger.label,
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                fallbackTint = triggerColor
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                trigger.label.replace("\u2728", "").trim(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                trigger.advice.trim()
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() },
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!trigger.reason.isNullOrBlank()) {
                                Text(
                                    trigger.reason,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Surface(
                            color = triggerColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                formatTimeRange(trigger.start, trigger.end),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = triggerColor
                            )
                        }
                    }
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

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    val metrics = responsiveMetrics()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(metrics.pagePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(if (metrics.isCompactHeight || metrics.isLargeFont) 52.dp else 64.dp),
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
    val metrics = responsiveMetrics()
    val gap = if (metrics.isCompactHeight || metrics.isLargeFont) 16.dp else 24.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(gap)
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
                    Box(modifier = Modifier.size(if (metrics.isCompactHeight || metrics.isLargeFont) 52.dp else 64.dp).clip(CircleShape).shimmerEffect())
                }
            }
        }

        ShimmerBlock(height = 200.dp, cornerRadius = 24.dp)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(3) { ShimmerBlock(height = 200.dp, modifier = Modifier.width(168.dp), cornerRadius = 20.dp) }
        }

        ShimmerBlock(height = 220.dp, cornerRadius = 24.dp)

        ShimmerBlock(height = 300.dp, cornerRadius = 24.dp)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(4) { ShimmerBlock(height = 160.dp, modifier = Modifier.width(120.dp), cornerRadius = 16.dp) }
        }

        ShimmerBlock(height = 70.dp, cornerRadius = 16.dp)

        Spacer(modifier = Modifier.height(LocalBottomBarHeight.current + 24.dp))
    }
}

@Composable
fun WeeklyForecastSection(forecast: WeeklyForecastResponse, themeColor: Color, onNavigateToForecast: (String?) -> Unit) {
    val metrics = responsiveMetrics()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .floatingCard(accent = themeColor, cornerRadius = 24.dp, elevation = 6.dp)
            .clickable { onNavigateToForecast(forecast.area) }
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding)) {
            val bestDayLabel = remember(forecast.summary.best_day) {
                try {
                    LocaleFormatter.displayDayOfWeek(forecast.summary.best_day, Locale.US, full = false).uppercase()
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
                        "CURRENT WEEK • BEST: $bestDayLabel",
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
                    .height(if (metrics.isCompactHeight || metrics.isLargeFont) 88.dp else 100.dp),
                showLabels = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            val today = forecast.days.find { it.is_today }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "☀️ TODAY ${today?.score ?: "--"}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = themeColor
                )
                Text(
                    "  •  ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                Text(
                    text = "Mood: ${titleCase(today?.mood?.value ?: "Balanced")}",
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
fun WeeklyForecastCompact(
    forecast: WeeklyForecastResponse,
    themeColor: Color,
    onNavigateToForecast: (String?) -> Unit
) {
    val today = forecast.days.find { it.is_today }
    val bestDayLabel = remember(forecast.summary.best_day) {
        try {
            LocaleFormatter.displayDayOfWeek(forecast.summary.best_day, Locale.US, full = false).uppercase()
        } catch (e: Exception) {
            forecast.summary.best_day.take(3).uppercase()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .floatingCard(accent = themeColor, cornerRadius = 14.dp, elevation = 4.dp)
            .clickable { onNavigateToForecast(forecast.area) }
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WEEK • BEST $bestDayLabel",
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = themeColor,
                    letterSpacing = 0.8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${today?.score ?: "--"}",
                    fontSize = 13.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = themeColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            WeeklyForecastGraph(
                days = forecast.days,
                themeColor = themeColor,
                area = forecast.area,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                showLabels = false
            )
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

    val peakSuffix = stringResource(R.string.dashboard_label_peak)
    val reflectiveSuffix = stringResource(R.string.dashboard_label_reflective)

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
            val minScoreDay = days.minByOrNull { it.score }
            
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
                if (isToday || isSelected || isBest || day == minScoreDay || showLabels) {
                    var scoreText = day.score.toString()
                    if (day == maxScoreDay && day.score >= 75) scoreText += peakSuffix
                    else if (day == minScoreDay && day.score <= 65) scoreText += reflectiveSuffix
                    val textPaint = android.graphics.Paint().apply {
                        color = dotColor.toArgb()
                        textSize = 11.sp.toPx()
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    
                    // For semantic labels, we need slightly more width, so adjust text position slightly if it goes out of bounds
                    // But usually it's fine. We use a smaller text size if it's not just the number.
                    if (scoreText != day.score.toString()) {
                        textPaint.textSize = 9.sp.toPx()
                    }
                    
                    drawContext.canvas.nativeCanvas.drawText(
                        scoreText,
                        point.x,
                        point.y - 12.dp.toPx(),
                        textPaint
                    )

                    if (showLabels) {
                        val dayLabel = try {
                            LocaleFormatter.displayDayOfWeek(day.date, Locale.US, full = false).uppercase()
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

    // --- ANIMATIONS ---
    val revealScale = remember { Animatable(0.9f) }
    LaunchedEffect(Unit) {
        revealScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale * revealScale.value
                scaleY = scale * revealScale.value
            }
            .floatingCard(accent = themeColor, cornerRadius = 28.dp, elevation = 8.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: SVG + Identity
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Birth Chart SVG (Mini)
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = CARD_SURFACE_LIGHT_ALPHA),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.2f))
                ) {
                    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
                    if (!userEmail.isNullOrBlank() && !accessToken.isNullOrBlank()) {
                        SubcomposeAsyncImage(
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
                        ) {
                            val state = painter.state
                            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Empty) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        color = themeColor,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            } else if (state is AsyncImagePainter.State.Error) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.BrokenImage,
                                        contentDescription = "Chart loading failed",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                SubcomposeAsyncImageContent()
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Sign in to view chart",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "YOUR KUNDLI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = themeColor,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val rashiAsset = rashiAssetFor(lagna)
                        if (rashiAsset != null) {
                            AstroAssetImage(
                                assetName = rashiAsset,
                                contentDescription = "$lagna sign",
                                modifier = Modifier.size(22.dp).clip(CircleShape),
                                fallbackTint = themeColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            "$lagna Lagna",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = themeColor.copy(alpha = 0.4f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            val mahadasha = data.dasha?.current?.mahadasha
            val antardasha = data.dasha?.current?.antardasha
            if (mahadasha != null || antardasha != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (mahadasha != null) {
                        MiniDashaChip(
                            label = "Mahadasha",
                            planetName = mahadasha.planet,
                            startDate = mahadasha.start,
                            endDate = mahadasha.end,
                            isPrimary = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (antardasha != null) {
                        MiniDashaChip(
                            label = "Antardasha",
                            planetName = antardasha.planet,
                            startDate = antardasha.start,
                            endDate = antardasha.end,
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(50.dp).shimmerSweepEffect(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
            ) {
                Text("EXPLORE FULL ANALYSIS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }
}

private fun rashiAssetFor(sign: String?): String? {
    val key = sign?.trim()?.lowercase()?.takeIf { it.isNotEmpty() && it != "unknown" } ?: return null
    return when (key) {
        "aries", "taurus", "gemini", "cancer", "leo", "virgo",
        "libra", "scorpio", "sagittarius", "capricorn", "aquarius", "pisces" -> key
        else -> null
    }
}

private fun planetAssetFor(planet: String?): String? {
    val key = planet?.trim()?.lowercase() ?: return null
    return when (key) {
        "sun", "moon", "mars", "mercury", "jupiter", "venus", "saturn" -> key
        else -> null
    }
}

@Composable
private fun MiniDashaChip(
    label: String,
    planetName: String,
    startDate: String?,
    endDate: String?,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    val planetColor = AstroColors.getPlanetaryColor(planetName)
    val containerColor = if (isPrimary) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = if (isPrimary) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val asset = planetAssetFor(planetName)
    val dateRange = remember(startDate, endDate) {
        val s = startDate?.take(10)?.takeIf { it.isNotBlank() }
        val e = endDate?.take(10)?.takeIf { it.isNotBlank() }
        when {
            s != null && e != null -> "$s → $e"
            s != null -> s
            e != null -> "→ $e"
            else -> ""
        }
    }

    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AstroAssetImage(
                assetName = asset,
                contentDescription = planetName,
                modifier = Modifier.size(64.dp),
                fallbackTint = planetColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = planetColor,
                letterSpacing = 0.8.sp
            )
            Text(
                planetName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )
            if (dateRange.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    dateRange,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .floatingCard(accent = MaterialTheme.colorScheme.primary, cornerRadius = 20.dp, elevation = 6.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
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
