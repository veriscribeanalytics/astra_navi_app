package com.astranavi.app.ui.forecast

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.astranavi.app.LocalTopBarTitle
import com.astranavi.app.data.model.*
import com.astranavi.app.ui.components.ScoreColors
import com.astranavi.app.ui.components.responsiveMetrics
import com.astranavi.app.ui.components.ShimmerBlock
import com.astranavi.app.ui.components.shimmerEffect
import com.astranavi.app.ui.components.titleCase
import com.astranavi.app.ui.dashboard.SectionHeader
import com.astranavi.app.ui.theme.AstroColors
import com.astranavi.app.util.LocaleFormatter
import com.astranavi.app.util.currentAppLocale
import com.astranavi.app.util.nextLocalMidnightMillis
import androidx.compose.ui.res.stringResource
import com.astranavi.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.*

fun getAreaStringResId(area: String): Int = when(area.lowercase()) {
    "general" -> R.string.dashboard_general
    "love" -> R.string.dashboard_love
    "career" -> R.string.dashboard_career
    "finance" -> R.string.dashboard_finance
    "health" -> R.string.dashboard_health
    "spiritual" -> R.string.consult_tone_spiritual
    else -> R.string.dashboard_general
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    viewModel: ForecastViewModel,
    initialArea: String? = null,
    onBack: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val uiState = viewModel.uiState.value
    val selectedArea = viewModel.selectedArea.value
    val selectedPeriod = viewModel.selectedPeriod.value
    val themeColor = getAreaColor(selectedArea)
    val setTopBarTitle = LocalTopBarTitle.current
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() <= 0.5f
    val success = uiState as? ForecastUiState.Success
    val glowScore = when (selectedPeriod) {
        ForecastPeriod.WEEKLY -> success?.weekly?.summary?.average_score?.toInt()
        ForecastPeriod.MONTHLY -> success?.monthly?.summary?.average_score?.toInt()
        ForecastPeriod.YEARLY -> success?.yearly?.summary?.average_score?.toInt()
    } ?: 70
    val glowPalette = com.astranavi.app.ui.components.ScoreColors.paletteFor(
        area = selectedArea,
        score = glowScore,
        isDarkTheme = isDarkTheme
    )

    // Initialize area
    LaunchedEffect(initialArea) {
        val area = if (initialArea.isNullOrEmpty()) "general" else initialArea
        viewModel.selectArea(area)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchForecast(silent = true)
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
            viewModel.fetchForecast(silent = true)
        }
    }

    // Update global title when area changes
    val areaLocalizedName = stringResource(getAreaStringResId(selectedArea))
    val forecastTitle = stringResource(R.string.forecast_title_format, areaLocalizedName)
    LaunchedEffect(forecastTitle) {
        setTopBarTitle?.invoke(forecastTitle)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        com.astranavi.app.ui.components.ApplyRootGlow(
            com.astranavi.app.ui.components.GlowColors(
                accent = glowPalette.glow,
                deep = glowPalette.main,
                radial = glowPalette.glow
            )
        )
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Cosmic Area Tabs (General, Love, Career, Health, Finance)
            CosmicAreaTabs(
                selectedArea = selectedArea,
                onAreaSelected = { viewModel.selectArea(it) }
            )

            // 2. Main Content Wrapper
            var isRefreshing by remember { mutableStateOf(false) }
            LaunchedEffect(uiState) {
                if (uiState !is ForecastUiState.Loading) {
                    isRefreshing = false
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.fetchForecast(forceRefresh = true)
                },
                modifier = Modifier.fillMaxSize()
            ) {
                when (uiState) {
                    is ForecastUiState.Loading -> ForecastSkeleton()
                    is ForecastUiState.Success -> {
                        val weeklyData = uiState.weekly
                        val monthlyData = uiState.monthly
                        val yearlyData = uiState.yearly

                        val activeDasha = weeklyData?.active_dasha
                        val moonSign = weeklyData?.moon_sign
                        val lagnaSign = weeklyData?.lagna_sign

                        // Compact Shared Header Details
                        Column(modifier = Modifier.fillMaxSize()) {
                            SharedForecastHeader(
                                area = selectedArea,
                                dasha = activeDasha,
                                moonSign = moonSign,
                                lagnaSign = lagnaSign,
                                themeColor = themeColor
                            )

                            // Period Tabs (Weekly, Monthly, Yearly)
                            CosmicPeriodTabs(
                                selectedPeriod = selectedPeriod,
                                themeColor = themeColor,
                                onPeriodSelected = { viewModel.selectPeriod(it) }
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                when (selectedPeriod) {
                                    ForecastPeriod.WEEKLY -> {
                                        if (weeklyData != null) {
                                            WeeklyForecastTabContent(
                                                data = weeklyData,
                                                themeColor = themeColor,
                                                selectedArea = selectedArea,
                                                onNavigateToChat = onNavigateToChat
                                            )
                                        } else {
                                            ErrorView(stringResource(R.string.forecast_error_weekly_failed)) {
                                                viewModel.fetchForecast(forceRefresh = true)
                                            }
                                        }
                                    }
                                    ForecastPeriod.MONTHLY -> {
                                        if (monthlyData != null) {
                                            MonthlyForecastTabContent(
                                                data = monthlyData,
                                                themeColor = themeColor,
                                                selectedArea = selectedArea,
                                                onNavigateToChat = onNavigateToChat
                                            )
                                        } else {
                                            ErrorView(stringResource(R.string.forecast_error_monthly_failed)) {
                                                viewModel.fetchForecast(forceRefresh = true)
                                            }
                                        }
                                    }
                                    ForecastPeriod.YEARLY -> {
                                        if (yearlyData != null) {
                                            YearlyForecastTabContent(
                                                data = yearlyData,
                                                themeColor = themeColor,
                                                selectedArea = selectedArea,
                                                onNavigateToChat = onNavigateToChat
                                            )
                                        } else {
                                            ErrorView(stringResource(R.string.forecast_error_yearly_failed)) {
                                                viewModel.fetchForecast(forceRefresh = true)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is ForecastUiState.Error -> ErrorView(uiState.message) { viewModel.fetchForecast(forceRefresh = true) }
                }
            }
        }
    }
}

private val FORECAST_AREAS = listOf("general", "love", "career", "health", "finance", "spiritual")
private val FORECAST_AREA_POSITIONS = listOf(-2, -1, 0, 1, 2, 3)

@Composable
fun CosmicAreaTabs(selectedArea: String, onAreaSelected: (String) -> Unit) {
    val metrics = responsiveMetrics()
    val selectedIdx = FORECAST_AREAS.indexOf(selectedArea).coerceAtLeast(0)
    val total = FORECAST_AREAS.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = metrics.forecastSectionGap / 2)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = metrics.pagePadding),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FORECAST_AREA_POSITIONS.forEach { pos ->
                val area = FORECAST_AREAS[(selectedIdx + pos + total) % total]
                val isCenter = pos == 0
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = area,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220)) +
                                slideInHorizontally(animationSpec = tween(220)) { it / 3 })
                                .togetherWith(
                                    fadeOut(animationSpec = tween(160)) +
                                        slideOutHorizontally(animationSpec = tween(160)) { -it / 3 }
                                )
                        },
                        label = "area_chip_$pos"
                    ) { displayedArea ->
                        AreaTabChip(
                            area = displayedArea,
                            isSelected = isCenter,
                            onClick = { onAreaSelected(displayedArea) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FORECAST_AREAS.forEachIndexed { idx, _ ->
                val isActive = idx == selectedIdx
                val dotSize by animateFloatAsState(if (isActive) 6f else 4f, label = "dot_size")
                val dotAlpha by animateFloatAsState(if (isActive) 1f else 0.3f, label = "dot_alpha")
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(dotSize.dp)
                        .background(
                            color = getAreaColor(FORECAST_AREAS[selectedIdx]).copy(alpha = dotAlpha),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun AreaTabChip(
    area: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = getAreaColor(area)
    val emoji = getAreaEmoji(area)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "tab_press_scale")

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.12f) else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, color.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = if (isSelected) 14.sp else 12.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = stringResource(getAreaStringResId(area)).take(3).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                fontSize = if (isSelected) 10.sp else 9.sp,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SharedForecastHeader(
    area: String,
    dasha: String?,
    moonSign: String?,
    lagnaSign: String?,
    themeColor: Color
) {
    val metrics = responsiveMetrics()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = metrics.pagePadding, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, themeColor.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(metrics.cardPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.forecast_title_format, stringResource(getAreaStringResId(area))),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = themeColor
                )

                if (!moonSign.isNullOrBlank() || !lagnaSign.isNullOrBlank()) {
                    val moonLabel = if (!moonSign.isNullOrBlank()) "🌙 " + stringResource(R.string.dashboard_label_moon_suffix, moonSign) else ""
                    val lagnaLabel = if (!lagnaSign.isNullOrBlank()) "🌅 " + stringResource(R.string.dashboard_label_lagna_suffix, lagnaSign) else ""
                    val signText = listOfNotNull(
                        moonLabel.takeIf { it.isNotEmpty() },
                        lagnaLabel.takeIf { it.isNotEmpty() }
                    ).joinToString(" • ")
                    Text(
                        text = signText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!dasha.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = themeColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = dasha,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun CosmicPeriodTabs(
    selectedPeriod: ForecastPeriod,
    themeColor: Color,
    onPeriodSelected: (ForecastPeriod) -> Unit
) {
    val metrics = responsiveMetrics()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = metrics.pagePadding, vertical = 6.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val periods = listOf(
            ForecastPeriod.WEEKLY to stringResource(R.string.forecast_tab_weekly),
            ForecastPeriod.MONTHLY to stringResource(R.string.forecast_tab_monthly),
            ForecastPeriod.YEARLY to stringResource(R.string.forecast_tab_yearly)
        )

        periods.forEach { (period, label) ->
            val isSelected = selectedPeriod == period
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(metrics.buttonHeight * 0.75f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) themeColor else Color.Transparent)
                    .clickable { onPeriodSelected(period) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = if (metrics.isVeryCompactWidth || metrics.isLargeFont) 9.sp else 10.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun WeeklyForecastTabContent(
    data: WeeklyForecastResponse,
    themeColor: Color,
    selectedArea: String,
    onNavigateToChat: (String) -> Unit
) {
    val metrics = responsiveMetrics()
    var selectedDate by remember(data.days) {
        mutableStateOf(data.days.find { it.is_today }?.date ?: data.days.firstOrNull()?.date ?: "")
    }
    val selectedDay = data.days.find { day -> day.date == selectedDate }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(metrics.forecastSectionGap)
    ) {
        // 1. Weekly Snapshot + Chart (2-column on tablet/fold)
        if (metrics.isMediumWidth) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(metrics.forecastSectionGap)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        WeeklySnapshotCard(
                            days = data.days,
                            summary = data.summary,
                            overview = data.overview,
                            themeColor = themeColor
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(metrics.cardPadding)) {
                                Text(
                                    text = stringResource(R.string.forecast_weekly_comparison),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = themeColor.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(metrics.forecastSectionGap))

                                WeeklyForecastBarChart(
                                    days = data.days,
                                    selectedDate = selectedDate,
                                    area = selectedArea,
                                    themeColor = themeColor,
                                    onDaySelected = { selectedDate = it }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Mobile: Stack vertically
            item {
                WeeklySnapshotCard(
                    days = data.days,
                    summary = data.summary,
                    overview = data.overview,
                    themeColor = themeColor
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(metrics.cardPadding)) {
                        Text(
                            text = stringResource(R.string.forecast_weekly_comparison),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = themeColor.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(metrics.forecastSectionGap))

                        WeeklyForecastBarChart(
                            days = data.days,
                            selectedDate = selectedDate,
                            area = selectedArea,
                            themeColor = themeColor,
                            onDaySelected = { selectedDate = it }
                        )
                    }
                }
            }
        }

        // 2. Selected Day Card Details
        if (selectedDay != null) {
            item {
                DetailedDayPreviewCard(
                    day = selectedDay,
                    area = selectedArea,
                    themeColor = themeColor,
                    onNavigateToChat = onNavigateToChat
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(metrics.heroBottomPadding))
        }
    }
}

@Composable
fun MonthlyForecastTabContent(
    data: MonthlyForecastResponse,
    themeColor: Color,
    selectedArea: String,
    onNavigateToChat: (String) -> Unit
) {
    val metrics = responsiveMetrics()
    var selectedDate by remember(data.days) {
        mutableStateOf(data.days.find { it.is_today }?.date ?: data.days.firstOrNull()?.date ?: "")
    }
    val selectedDay = data.days.find { day -> day.date == selectedDate }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(metrics.forecastSectionGap)
    ) {
        // 1. Monthly Overview + Month Flow Chart (2-column on tablet/fold)
        if (metrics.isMediumWidth) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(metrics.forecastSectionGap)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        MonthlyOverviewCard(
                            data = data,
                            themeColor = themeColor
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(metrics.cardPadding)) {
                                Text(
                                    text = stringResource(R.string.forecast_month_flow),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = themeColor.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(metrics.forecastSectionGap / 2))

                                MonthlyTrendLineChart(
                                    days = data.days,
                                    themeColor = themeColor,
                                    selectedDate = selectedDate
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Mobile: Stack vertically
            item {
                MonthlyOverviewCard(
                    data = data,
                    themeColor = themeColor
                )
            }
        }

        // 2. Calendar Heatmap + Selected Day Preview (2-column on tablet/fold)
        if (metrics.isMediumWidth) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(metrics.forecastSectionGap)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(metrics.cardPadding)) {
                                Text(
                                    text = stringResource(R.string.forecast_monthly_grid),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = themeColor.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                MonthlyCalendarHeatmap(
                                    days = data.days,
                                    selectedDate = selectedDate,
                                    area = selectedArea,
                                    themeColor = themeColor,
                                    onDateSelected = { selectedDate = it }
                                )
                            }
                        }
                    }
                    if (selectedDay != null) {
                        Box(modifier = Modifier.weight(1f)) {
                            DetailedDayPreviewCard(
                                day = selectedDay,
                                area = selectedArea,
                                themeColor = themeColor,
                                onNavigateToChat = onNavigateToChat
                            )
                        }
                    }
                }
            }
        } else {
            // Mobile: Stack vertically
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(metrics.cardPadding)) {
                        Text(
                            text = stringResource(R.string.forecast_monthly_grid),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = themeColor.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        MonthlyCalendarHeatmap(
                            days = data.days,
                            selectedDate = selectedDate,
                            area = selectedArea,
                            themeColor = themeColor,
                            onDateSelected = { selectedDate = it }
                        )
                    }
                }
            }

            if (selectedDay != null) {
                item {
                    DetailedDayPreviewCard(
                        day = selectedDay,
                        area = selectedArea,
                        themeColor = themeColor,
                        onNavigateToChat = onNavigateToChat
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(metrics.cardPadding)) {
                        Text(
                            text = stringResource(R.string.forecast_month_flow),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = themeColor.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(metrics.forecastSectionGap / 2))

                        MonthlyTrendLineChart(
                            days = data.days,
                            themeColor = themeColor,
                            selectedDate = selectedDate
                        )
                    }
                }
            }
        }

        // 3. Weekly Breakdowns (Expandable List)
        if (!data.weeks.isNullOrEmpty()) {
            item {
                SectionHeader(stringResource(R.string.forecast_weekly_breakdowns))
            }
            itemsIndexed(data.weeks, key = { _, w -> w.week_start ?: w.week_end ?: "${w.score}" }) { index, week ->
                MonthlyWeekBreakdownCard(
                    week = week,
                    themeColor = themeColor,
                    index = index
                )
            }
        }

        // 4. Ask Navi CTA
        item {
            val bestDayAppLocale = currentAppLocale()
            val bestDayLabel = LocaleFormatter.displayMonthDay(data.summary.best_day, bestDayAppLocale, full = false)

            AskNaviCTA(
                title = stringResource(R.string.forecast_ask_navi_period, data.period_label ?: stringResource(R.string.forecast_period_this_month)),
                suggestions = listOf(
                    stringResource(R.string.forecast_suggestion_monthly_1, bestDayLabel),
                    stringResource(R.string.forecast_suggestion_monthly_2),
                    stringResource(R.string.forecast_suggestion_monthly_3)
                ),
                onNavigateToChat = onNavigateToChat
            )
        }

        item {
            Spacer(modifier = Modifier.height(metrics.heroBottomPadding))
        }
    }
}

@Composable
fun YearlyForecastTabContent(
    data: YearlyForecastResponse,
    themeColor: Color,
    selectedArea: String,
    onNavigateToChat: (String) -> Unit
) {
    val metrics = responsiveMetrics()
    var selectedMonthLabel by remember { mutableStateOf("") }
    val selectedMonth = data.months.find { it.label == selectedMonthLabel } ?: data.months.firstOrNull()

    LaunchedEffect(data.months) {
        if (selectedMonthLabel.isEmpty()) {
            selectedMonthLabel = data.months.find { it.is_current }?.label ?: data.months.firstOrNull()?.label ?: ""
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(metrics.forecastSectionGap)
    ) {
        // 1. Year Overview + Year Trend Chart (2-column on tablet/fold)
        if (metrics.isMediumWidth) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(metrics.forecastSectionGap)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        YearlyOverviewCard(
                            data = data,
                            themeColor = themeColor,
                            selectedArea = selectedArea
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(metrics.cardPadding)) {
                                Text(
                                    text = stringResource(R.string.forecast_year_flow_trend),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = themeColor.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(metrics.forecastSectionGap / 2))

                                YearlyTrendLineChart(
                                    months = data.months,
                                    themeColor = themeColor
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Mobile: Stack vertically
            item {
                YearlyOverviewCard(
                    data = data,
                    themeColor = themeColor,
                    selectedArea = selectedArea
                )
            }
        }

        // 2. 12-Month Score Grid + Selected Month Detail (2-column on tablet/fold)
        if (metrics.isMediumWidth) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(metrics.forecastSectionGap)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(metrics.cardPadding)) {
                                Text(
                                    text = stringResource(R.string.forecast_12_month_matrix),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = themeColor.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                YearlyScoreGrid(
                                    months = data.months,
                                    selectedMonthLabel = selectedMonthLabel,
                                    area = selectedArea,
                                    themeColor = themeColor,
                                    onMonthSelected = { selectedMonthLabel = it }
                                )
                            }
                        }
                    }
                    if (selectedMonth != null) {
                        Box(modifier = Modifier.weight(1f)) {
                            YearlyMonthDetailCard(
                                month = selectedMonth,
                                area = selectedArea,
                                themeColor = themeColor,
                                onNavigateToChat = onNavigateToChat
                            )
                        }
                    }
                }
            }
        } else {
            // Mobile: Stack vertically
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(metrics.cardPadding)) {
                        Text(
                            text = stringResource(R.string.forecast_12_month_matrix),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = themeColor.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        YearlyScoreGrid(
                            months = data.months,
                            selectedMonthLabel = selectedMonthLabel,
                            area = selectedArea,
                            themeColor = themeColor,
                            onMonthSelected = { selectedMonthLabel = it }
                        )
                    }
                }
            }

            if (selectedMonth != null) {
                item {
                    YearlyMonthDetailCard(
                        month = selectedMonth,
                        area = selectedArea,
                        themeColor = themeColor,
                        onNavigateToChat = onNavigateToChat
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(metrics.cardPadding)) {
                        Text(
                            text = stringResource(R.string.forecast_year_flow_trend),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = themeColor.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(metrics.forecastSectionGap / 2))

                        YearlyTrendLineChart(
                            months = data.months,
                            themeColor = themeColor
                        )
                    }
                }
            }
        }

        // 3. Quarterly breakdown
        item {
            SectionHeader(stringResource(R.string.forecast_quarterly_journey))
        }
        item {
            YearlyQuarterlyJourney(
                months = data.months,
                themeColor = themeColor
            )
        }

        // 4. Ask Navi CTA
        item {
            AskNaviCTA(
                title = stringResource(R.string.forecast_ask_navi_period, data.period_label ?: stringResource(R.string.forecast_period_this_year)),
                suggestions = listOf(
                    stringResource(R.string.forecast_suggestion_yearly_1),
                    stringResource(R.string.forecast_suggestion_yearly_2),
                    stringResource(R.string.forecast_suggestion_yearly_3)
                ),
                onNavigateToChat = onNavigateToChat
            )
        }

        item {
            Spacer(modifier = Modifier.height(metrics.heroBottomPadding))
        }
    }
}

// ─── WEEKLY COMPONENT DETAILS ──────────────────────────────────────────────

@Composable
fun WeeklySnapshotCard(
    days: List<ForecastDay>,
    summary: ForecastSummary,
    overview: ForecastOverview?,
    themeColor: Color
) {
    val metrics = responsiveMetrics()
    val appLocale = currentAppLocale()
    val bestDayLabel = remember(summary.best_day, appLocale) {
        formatShortDate(summary.best_day, appLocale)
    }
    val worstDayLabel = remember(summary.worst_day, appLocale) {
        formatShortDate(summary.worst_day, appLocale)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, themeColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.forecast_weekly_snapshot),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = themeColor.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "${summary.average_score.toInt()}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = themeColor
                        )
                        Text(
                            text = stringResource(R.string.forecast_score_max),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                // Trend Badge
                Surface(
                    color = themeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(99.dp),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = summary.trend.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = themeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = themeColor.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(12.dp))

            // Best / Challenging Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.forecast_best_day), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = themeColor.copy(alpha = 0.6f))
                    Text(bestDayLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.forecast_reflective_day), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                    Text(worstDayLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
                }
            }

            if (overview != null && !overview.text.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = overview.text,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyForecastBarChart(
    days: List<ForecastDay>,
    selectedDate: String,
    area: String,
    themeColor: Color,
    onDaySelected: (String) -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val appLocale = currentAppLocale()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        val maxScoreDay = days.maxByOrNull { it.score }
        val minScoreDay = days.minByOrNull { it.score }

        days.forEach { day ->
            val isSelected = day.date == selectedDate
            val isToday = day.is_today
            val scorePalette = remember(day.score, isDarkTheme) {
                ScoreColors.paletteFor(area, day.score, isDarkTheme)
            }
            val barColor = if (isSelected) themeColor else scorePalette.main.copy(alpha = 0.7f)
            val dayLabel = remember(day.date, appLocale) {
                try {
                    LocaleFormatter.displayDayOfWeek(day.date, appLocale, full = false).uppercase()
                } catch (e: Exception) {
                    day.date.take(3).uppercase()
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onDaySelected(day.date) }
                    .padding(horizontal = 2.dp)
            ) {
                Text(
                    text = LocaleFormatter.number(day.score, currentAppLocale()),
                    fontSize = 9.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                    color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp * (day.score / 100f).coerceIn(0.1f, 1f))
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                        .background(
                            if (isSelected) {
                                Brush.verticalGradient(
                                    colors = listOf(themeColor, themeColor.copy(alpha = 0.4f))
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(barColor.copy(alpha = 0.8f), barColor.copy(alpha = 0.3f))
                                )
                            }
                        )
                        .then(
                            if (isSelected) Modifier.border(1.5.dp, themeColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp)) else Modifier
                        )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dayLabel,
                        fontSize = 8.sp,
                        fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Medium,
                        color = if (isSelected) themeColor else if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (day == maxScoreDay) {
                        Text("⭐", fontSize = 6.sp, modifier = Modifier.padding(start = 1.dp))
                    } else if (day == minScoreDay) {
                        Text("⚠️", fontSize = 6.sp, modifier = Modifier.padding(start = 1.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedDayPreviewCard(
    day: ForecastDay,
    area: String,
    themeColor: Color,
    onNavigateToChat: (String) -> Unit
) {
    val metrics = responsiveMetrics()
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val scorePalette = remember(day.score, isDarkTheme) {
        ScoreColors.paletteFor(area, day.score, isDarkTheme)
    }
    val scoreColor = if (isDarkTheme) scorePalette.glow else scorePalette.main

    var showTransitSheet by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (day.is_today) 1.5.dp else 1.dp, if (day.is_today) scorePalette.border else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding)) {
            val dayAppLocale = currentAppLocale()
            val fullDateFormatted = remember(day.date, dayAppLocale) {
                LocaleFormatter.displayDate(day.date, dayAppLocale, "EEEE, MMMM d")
            }
            val chatQuery = stringResource(R.string.forecast_chat_query_day_format, area, day.score, fullDateFormatted)
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular score
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(scorePalette.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = LocaleFormatter.number(day.score, currentAppLocale()),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = scoreColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fullDateFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        if (day.is_today) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = scoreColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(99.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.forecast_label_today),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black,
                                    color = scoreColor
                                )
                            }
                        }
                    }
                    Text(
                        text = day.mood?.value ?: stringResource(R.string.forecast_default_mood),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            // Main Interpretation text
            Text(
                text = day.text,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )

            // Statistics Row (Adjustment, Lucky, Planet)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MicroStatCell(
                    label = stringResource(R.string.forecast_adjust),
                    value = if ((day.personal_adjustment ?: 0) >= 0) "+${day.personal_adjustment}" else "${day.personal_adjustment}",
                    emoji = "⚡",
                    themeColor = scoreColor,
                    modifier = Modifier.weight(1f)
                )
                MicroStatCell(
                    label = stringResource(R.string.forecast_lucky),
                    value = day.lucky_color ?: stringResource(R.string.forecast_lucky_color_gold),
                    emoji = "🎨",
                    themeColor = scoreColor,
                    modifier = Modifier.weight(1f)
                )
                MicroStatCell(
                    label = stringResource(R.string.forecast_dominant),
                    value = day.dominant_planet.titleCase(),
                    emoji = "🪐",
                    themeColor = scoreColor,
                    modifier = Modifier.weight(1.2f)
                )
            }

            // Expandable Influences / Alerts
            if (!day.personalized_alerts.isNullOrEmpty() || !day.alerts.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.forecast_cosmic_influences),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = scoreColor.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                val allAlerts = (day.personalized_alerts ?: emptyList()) + (day.alerts ?: emptyList())
                allAlerts.distinctBy { it.simple }.forEach { alert ->
                    ExpandableInfluenceRow(alert = alert, themeColor = themeColor)
                }
            }

            // Compact Top Transits Row
            if (!day.transits.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.forecast_top_transits),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = scoreColor.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    TextButton(
                        onClick = { showTransitSheet = true },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(stringResource(R.string.forecast_view_all_arrow), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = themeColor)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                
                // Show first 3 transits in a compact row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    day.transits.entries.take(3).forEach { (planet, transit) ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(planet.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = AstroColors.getPlanetaryColor(planet))
                                Text("${transit.sign} (H${transit.house_from_lagna})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Ask Navi CTA for selected day
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    onNavigateToChat(chatQuery)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = scoreColor)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.forecast_ask_navi_day), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
    }

    if (showTransitSheet && !day.transits.isNullOrEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showTransitSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.forecast_all_planetary_transits), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    IconButton(onClick = { showTransitSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.forecast_desc_close))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                ) {
                    items(day.transits.entries.toList(), key = { (planet, _) -> planet }) { (planet, transit) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(AstroColors.getPlanetaryColor(planet).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(planet.take(2).uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Black, color = AstroColors.getPlanetaryColor(planet))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = planet.titleCase(),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = stringResource(R.string.forecast_transit_sign_house, transit.sign ?: "", "${transit.house_from_moon ?: transit.house_from_lagna}"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (transit.retrograde == true) {
                                        Text(
                                            text = stringResource(R.string.forecast_retrograde_motion),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(top = 2.dp)
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

@Composable
fun MicroStatCell(label: String, value: String, emoji: String, themeColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(0.5.dp, themeColor.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = themeColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ExpandableInfluenceRow(alert: PersonalizedAlert, themeColor: Color) {
    var isExpanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "chev")
    
    val accentColor = when(alert.type?.lowercase()) {
        "positive" -> Color(0xFF10B981)
        "warning" -> Color(0xFFF59E0B)
        else -> themeColor
    }

    val icon = when(alert.type?.lowercase()) {
        "positive" -> "✨"
        "warning" -> "⚠️"
        else -> "💡"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(icon, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = alert.simple,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(chevronRotation),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = alert.technical ?: stringResource(R.string.forecast_default_technical_influence),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

// ─── MONTHLY COMPONENT DETAILS ─────────────────────────────────────────────

@Composable
fun MonthlyOverviewCard(
    data: MonthlyForecastResponse,
    themeColor: Color
) {
    val metrics = responsiveMetrics()
    val appLocaleForDates = currentAppLocale()
    val bestDayLabel = remember(data.summary.best_day, appLocaleForDates) {
        formatShortDate(data.summary.best_day, appLocaleForDates)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, themeColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = (data.period_label ?: stringResource(R.string.forecast_month_overview)).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = themeColor.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "${data.summary.average_score.toInt()}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = themeColor
                        )
                        Text(
                            text = stringResource(R.string.forecast_score_max),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                Surface(
                    color = themeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(99.dp),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = data.summary.trend.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = themeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = themeColor.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(12.dp))

            // Stat Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Best Day Chip
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.5.dp, themeColor.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(stringResource(R.string.forecast_label_best_day), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = themeColor)
                        Text(bestDayLabel, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Average Chip
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.5.dp, themeColor.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(stringResource(R.string.forecast_label_avg_score), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = themeColor)
                        Text("${data.summary.average_score.toInt()}", fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Today score chip
                val todayScore = data.days.find { it.is_today }?.score ?: 75
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.5.dp, themeColor.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(stringResource(R.string.forecast_label_today), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = themeColor)
                        Text("$todayScore", fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            if (data.overview != null && !data.overview.text.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = data.overview.text,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun MonthlyCalendarHeatmap(
    days: List<ForecastDay>,
    selectedDate: String,
    area: String,
    themeColor: Color,
    onDateSelected: (String) -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    if (days.isEmpty()) return

    val parsedDays = remember(days) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        days.mapNotNull { day ->
            try {
                val date = sdf.parse(day.date)
                val cal = Calendar.getInstance()
                cal.time = date
                val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                day to (dayOfWeek to dayOfMonth)
            } catch (e: Exception) {
                null
            }
        }
    }

    if (parsedDays.isEmpty()) return

    val firstDayOfWeek = parsedDays.first().second.first
    val monthDays = parsedDays.map { it.first }
    val maxScoreDay = monthDays.maxByOrNull { it.score }
    val minScoreDay = monthDays.minByOrNull { it.score }

    val appLocaleForCal = currentAppLocale()
    val daysOfWeek = remember(appLocaleForCal) {
        val sdf = SimpleDateFormat("EEE", appLocaleForCal)
        val cal = Calendar.getInstance()
        listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY).map { dow ->
            cal.set(Calendar.DAY_OF_WEEK, dow)
            sdf.format(cal.time).uppercase()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val totalCells = firstDayOfWeek + monthDays.size
        val rows = (totalCells + 6) / 7

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until 7) {
                        val index = row * 7 + col
                        if (index < firstDayOfWeek || index >= totalCells) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val dayIndex = index - firstDayOfWeek
                            val day = monthDays[dayIndex]
                            val isSelected = day.date == selectedDate
                            val isToday = day.is_today
                            val scorePalette = remember(day.score, isDarkTheme) {
                                ScoreColors.paletteFor(area, day.score, isDarkTheme)
                            }
                            val isBest = day == maxScoreDay
                            val isWorst = day == minScoreDay

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(scorePalette.surface)
                                    .then(
                                        if (isSelected) Modifier.border(1.5.dp, themeColor, CircleShape)
                                        else if (isToday) Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                                        else Modifier
                                    )
                                    .clickable { onDateSelected(day.date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    val parts = day.date.split("-")
                                    val dateNum = parts.lastOrNull()?.toIntOrNull() ?: (dayIndex + 1)
                                    Text(
                                        text = "$dateNum",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Bold,
                                        color = if (isSelected) themeColor else scorePalette.main
                                    )
                                    if (isBest) {
                                        Text("⭐", fontSize = 6.sp, modifier = Modifier.height(8.dp))
                                    } else if (isWorst) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 1.dp)
                                                .size(3.dp)
                                                .background(MaterialTheme.colorScheme.error, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val legendPhases = listOf(
                stringResource(R.string.forecast_legend_critical) to Color(0xFFDC2626),
                stringResource(R.string.forecast_legend_weak) to Color(0xFFF97316),
                stringResource(R.string.forecast_legend_mixed) to Color(0xFFF59E0B),
                stringResource(R.string.forecast_legend_good) to Color(0xFF16A34A),
                stringResource(R.string.forecast_legend_excellent) to Color(0xFF166534)
            )
            legendPhases.forEachIndexed { idx, (label, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(label, fontSize = 7.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                if (idx < legendPhases.lastIndex) {
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }
    }
}

@Composable
fun MonthlyTrendLineChart(
    days: List<ForecastDay>,
    themeColor: Color,
    selectedDate: String
) {
    val scores = days.map { it.score.toFloat() }
    if (scores.isEmpty()) return

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp)
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

        // Fill Path
        val fillPath = Path().apply {
            moveTo(points.first().x, height)
            points.forEachIndexed { index, point ->
                if (index == 0) lineTo(point.x, point.y)
                else {
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
                colors = listOf(themeColor.copy(alpha = 0.15f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Line Path
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

        drawPath(
            path = linePath,
            color = themeColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Highlight selected
        val selectedIdx = days.indexOfFirst { it.date == selectedDate }
        if (selectedIdx != -1) {
            val point = points[selectedIdx]
            drawCircle(
                color = themeColor,
                radius = 5.dp.toPx(),
                center = point
            )
            drawCircle(
                color = themeColor.copy(alpha = 0.25f),
                radius = 10.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
fun MonthlyWeekBreakdownCard(
    week: ForecastWeek,
    themeColor: Color,
    index: Int
) {
    var isExpanded by remember { mutableStateOf(false) }
    val metrics = responsiveMetrics()
    val chevronRotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "chev")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, themeColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(themeColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${week.score}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = themeColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val appLocaleForWeek = currentAppLocale()
                    val formattedStart = remember(week.week_start, appLocaleForWeek) { formatShortDate(week.week_start, appLocaleForWeek) }
                    val formattedEnd = remember(week.week_end, appLocaleForWeek) { formatShortDate(week.week_end, appLocaleForWeek) }
                    Text(
                        text = stringResource(R.string.forecast_week_label_format, index + 1, formattedStart, formattedEnd),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = week.text ?: stringResource(R.string.forecast_default_week_text),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(chevronRotation),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = week.text ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    if (!week.alerts.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.forecast_weekly_alerts),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = themeColor.copy(alpha = 0.6f)
                        )
                        week.alerts.forEach { alert ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(4.dp).background(themeColor, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(alert.simple, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── YEARLY COMPONENT DETAILS ──────────────────────────────────────────────

@Composable
fun YearlyOverviewCard(
    data: YearlyForecastResponse,
    themeColor: Color,
    selectedArea: String
) {
    val metrics = responsiveMetrics()
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val yearlyScore = data.summary.average_score.toInt()
    val scorePalette = remember(yearlyScore, selectedArea, isDarkTheme) {
        ScoreColors.paletteFor(selectedArea, yearlyScore, isDarkTheme)
    }
    val scoreColor = if (isDarkTheme) scorePalette.glow else scorePalette.main

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, themeColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = (data.period_label ?: stringResource(R.string.forecast_year_at_a_glance)).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = themeColor.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "$yearlyScore",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = scoreColor
                        )
                        Text(
                            text = stringResource(R.string.forecast_score_max),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                Surface(
                    color = themeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(99.dp),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = data.summary.trend.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = themeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = themeColor.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(12.dp))

            // Stat Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Best Month
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.forecast_best_month), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = themeColor.copy(alpha = 0.6f))
                    Text(data.summary.best_month.titleCase(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
                }
                // Caution Month
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.forecast_caution_month), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                    Text(data.summary.worst_month.titleCase(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
                }
            }

            if (data.overview != null && !data.overview.text.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = data.overview.text,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun YearlyScoreGrid(
    months: List<ForecastMonth>,
    selectedMonthLabel: String,
    area: String,
    themeColor: Color,
    onMonthSelected: (String) -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val rows = (months.size + 2) / 3
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (c in 0 until 3) {
                    val idx = r * 3 + c
                    if (idx < months.size) {
                        val m = months[idx]
                        val isSelected = m.label == selectedMonthLabel
                        val isCurrent = m.is_current
                        val scorePalette = remember(m.score, isDarkTheme) {
                            ScoreColors.paletteFor(area, m.score, isDarkTheme)
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onMonthSelected(m.label) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) scorePalette.surface else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else if (isCurrent) 1.dp else 0.5.dp,
                                color = if (isSelected) themeColor else if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = m.label.take(3).uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${m.score}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = scorePalette.main
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun YearlyMonthDetailCard(
    month: ForecastMonth,
    area: String = "general",
    themeColor: Color,
    onNavigateToChat: (String) -> Unit
) {
    val metrics = responsiveMetrics()
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val scorePalette = remember(month.score, isDarkTheme) {
        ScoreColors.paletteFor(area, month.score, isDarkTheme)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(scorePalette.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${month.score}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = scorePalette.main
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = month.label.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Cosmic Monthly Outlook",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = month.text,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            if (!month.alerts.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "KEY ALERTS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = themeColor.copy(alpha = 0.6f)
                )
                month.alerts.forEach { alert ->
                    ExpandableInfluenceRow(alert = alert, themeColor = themeColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    onNavigateToChat("Explain my trends for the month of ${month.label} which has a score of ${month.score}.")
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = scorePalette.main)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ask Navi about ${month.label}", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun YearlyQuarterlyJourney(
    months: List<ForecastMonth>,
    themeColor: Color
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val quarters = remember(months) {
        val chunked = months.chunked(3)
        chunked.mapIndexed { idx, qMonths ->
            val avg = if (qMonths.isEmpty()) 0.0 else qMonths.map { it.score }.average()
            val text = when (idx) {
                0 -> "Focus on setting foundations, structural development, and starting annual plans."
                1 -> "Active execution phase. Expect transits that drive communication and network expansion."
                2 -> "Mid-year reflection. A period of stabilization, re-assessing targets and inner energy."
                else -> "Harvest period. Finalizing projects, financial reviews, and closing the yearly cycle."
            }
            "Q${idx + 1}" to (avg.toInt() to text)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        quarters.forEach { (label, data) ->
            val (score, desc) = data

            // Get score-based color palette
            val scorePalette = remember(score, isDarkTheme) {
                ScoreColors.paletteFor("general", score, isDarkTheme)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                border = BorderStroke(0.5.dp, scorePalette.border)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = scorePalette.main)
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .background(scorePalette.surface, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("AVG $score", fontSize = 8.sp, fontWeight = FontWeight.Black, color = scorePalette.main)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = desc,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ─── REUSABLE UI UTILS ──────────────────────────────────────────────────────

@Composable
fun AskNaviCTA(
    title: String,
    suggestions: List<String>,
    onNavigateToChat: (String) -> Unit
) {
    val metrics = responsiveMetrics()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✨", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            suggestions.forEach { prompt ->
                OutlinedButton(
                    onClick = { onNavigateToChat(prompt) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

fun getAreaColor(area: String): Color = ScoreColors.categoryBase(area)

fun getAreaEmoji(area: String): String = when(area.lowercase()) {
    "love" -> "💕"
    "health" -> "🏥"
    "career" -> "💼"
    "finance" -> "💰"
    "spiritual" -> "🕉️"
    else -> "✨"
}

fun formatShortDate(dateStr: String?, locale: Locale): String {
    if (dateStr.isNullOrBlank()) return "---"
    val formatted = LocaleFormatter.displayMonthDay(dateStr, locale, full = false)
    return if (formatted.isEmpty()) dateStr else formatted
}

fun formatFullDate(dateStr: String, locale: Locale): String {
    val formatted = LocaleFormatter.displayDate(dateStr, locale, "EEEE, MMMM d")
    return if (formatted.isEmpty()) dateStr else formatted
}

@Composable
fun ForecastSkeleton() {
    val metrics = responsiveMetrics()

    Column(modifier = Modifier.fillMaxSize().padding(metrics.pagePadding), verticalArrangement = Arrangement.spacedBy(metrics.forecastSectionGap)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(metrics.forecastSectionGap)
        ) {
            items(5) { ShimmerBlock(height = 36.dp, modifier = Modifier.width(80.dp), cornerRadius = 12.dp) }
        }

        ShimmerBlock(height = 110.dp, cornerRadius = 24.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(metrics.forecastSectionGap)
        ) {
            repeat(3) { ShimmerBlock(height = 36.dp, modifier = Modifier.weight(1f).widthIn(min = 80.dp), cornerRadius = 12.dp) }
        }

        ShimmerBlock(height = 150.dp, cornerRadius = 28.dp)

        ShimmerBlock(height = metrics.forecastChartHeight, cornerRadius = 24.dp)

        ShimmerBlock(height = 320.dp, cornerRadius = 24.dp)

        ShimmerBlock(height = 140.dp, cornerRadius = 24.dp)

        ShimmerBlock(height = 140.dp, cornerRadius = 24.dp)
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    val metrics = responsiveMetrics()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(metrics.pagePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Oops!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(metrics.forecastSectionGap / 2))
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(metrics.forecastSectionGap))
        Button(onClick = onRetry) {
            Text("TRY AGAIN")
        }
    }
}

@Composable
fun YearlyTrendLineChart(
    months: List<ForecastMonth>,
    themeColor: Color
) {
    val scores = months.map { it.score.toFloat() }
    if (scores.isEmpty()) return

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp)
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

        // Fill Path
        val fillPath = Path().apply {
            moveTo(points.first().x, height)
            points.forEachIndexed { index, point ->
                if (index == 0) lineTo(point.x, point.y)
                else {
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
                colors = listOf(themeColor.copy(alpha = 0.15f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Line Path
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

        drawPath(
            path = linePath,
            color = themeColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
