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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.LocalTopBarTitle
import com.astranavi.app.data.model.*
import com.astranavi.app.ui.theme.AstroColors
import com.astranavi.app.ui.components.ScoreColors
import com.astranavi.app.ui.components.shimmerEffect
import com.astranavi.app.ui.components.shimmerSweepEffect
import com.astranavi.app.ui.dashboard.SectionHeader
import com.astranavi.app.ui.dashboard.WeeklyForecastGraph
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ForecastScreen(
    viewModel: ForecastViewModel,
    initialArea: String? = null,
    onBack: () -> Unit
) {
    val uiState = viewModel.uiState.value
    val selectedArea = viewModel.selectedArea.value
    val selectedRange = viewModel.selectedRange.value
    val themeColor = getAreaColor(selectedArea)
    val setTopBarTitle = LocalTopBarTitle.current

    // Initialize area and title
    LaunchedEffect(initialArea) {
        val area = if (initialArea.isNullOrEmpty()) "general" else initialArea
        viewModel.selectArea(area)
    }

    // Update global title when area changes
    LaunchedEffect(selectedArea) {
        setTopBarTitle?.invoke("${selectedArea.uppercase()} FORECAST")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. COSMIC SEGMENTED CONTROL (TABS)
            CosmicAreaTabs(
                selectedArea = selectedArea,
                onAreaSelected = { viewModel.selectArea(it) }
            )

            // 2. RANGE SELECTOR
            CosmicRangeSelector(
                selectedRange = selectedRange,
                themeColor = themeColor,
                onRangeSelected = { viewModel.selectRange(it) }
            )

            // 3. CONTENT
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    is ForecastUiState.Loading -> ForecastSkeleton()
                    is ForecastUiState.Success -> {
                        var selectedDate by remember(uiState.forecast) {
                            mutableStateOf(uiState.forecast.days.find { it.is_today }?.date ?: uiState.forecast.days.firstOrNull()?.date ?: "")
                        }
                        ForecastContent(
                            forecast = uiState.forecast,
                            selectedArea = selectedArea,
                            themeColor = themeColor,
                            selectedRange = selectedRange,
                            selectedDate = selectedDate,
                            onDateSelected = { selectedDate = it }
                        )
                    }
                    is ForecastUiState.Error -> ErrorView(uiState.message) { viewModel.fetchForecast() }
                }
            }
        }
    }
}

@Composable
fun CosmicAreaTabs(selectedArea: String, onAreaSelected: (String) -> Unit) {
    val areas = listOf("general", "love", "career", "health", "finance")
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(areas) { area ->
            val isSelected = selectedArea == area
            val color = getAreaColor(area)
            val emoji = getAreaEmoji(area)

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "tab_scale")

            Column(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onAreaSelected(area) }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent,
                    border = if (isSelected) BorderStroke(1.dp, color.copy(alpha = 0.2f)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(emoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = area.take(3).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
                
                // Animated Underline
                AnimatedVisibility(
                    visible = isSelected,
                    enter = expandHorizontally(expandFrom = Alignment.CenterHorizontally) + fadeIn(),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally) + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .width(24.dp)
                            .height(3.dp)
                            .background(color, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun CosmicRangeSelector(
    selectedRange: ForecastRange,
    themeColor: Color,
    onRangeSelected: (ForecastRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val ranges = listOf(
            ForecastRange.PAST_MONTH to "PAST MONTH",
            ForecastRange.WEEK to "NEXT 7 DAYS",
            ForecastRange.MONTH to "NEXT 30 DAYS"
        )

        ranges.forEach { (range, label) ->
            val isSelected = selectedRange == range
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) themeColor else Color.Transparent)
                    .clickable { onRangeSelected(range) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun ForecastContent(
    forecast: ForecastResponse,
    selectedArea: String,
    themeColor: Color,
    selectedRange: ForecastRange,
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (selectedRange != ForecastRange.PAST_MONTH) {
            item {
                TodaySnapshotCard(forecast, selectedArea)
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🕰️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("HISTORICAL REFLECTION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = themeColor)
                            Text("Looking back at the past 30 days of cosmic alignment.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.03f)),
                border = BorderStroke(1.dp, themeColor.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        if (selectedRange == ForecastRange.PAST_MONTH) "PAST TREND" else "FUTURE TREND",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = themeColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WeeklyForecastGraph(
                        days = forecast.days,
                        themeColor = themeColor,
                        area = selectedArea,
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        showLabels = selectedRange == ForecastRange.WEEK,
                        selectedDate = selectedDate,
                        onDayClick = onDateSelected
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap a day to see details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        item {
            SectionHeader(if (selectedRange == ForecastRange.PAST_MONTH) "PAST DETAILS" else "DAY DETAILS")
        }

        val selectedDay = forecast.days.find { it.date == selectedDate }
        if (selectedDay != null) {
            item {
                DetailedForecastDayCard(
                    day = selectedDay,
                    area = selectedArea,
                    themeColor = themeColor,
                    index = 0
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TodaySnapshotCard(forecast: ForecastResponse, area: String) {
    val today = forecast.days.find { it.is_today } ?: return
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val palette = remember(area, today.score, isDarkTheme) {
        ScoreColors.paletteFor(area, today.score, isDarkTheme)
    }
    val color = if (isDarkTheme) palette.glow else palette.main
    
    val infiniteTransition = rememberInfiniteTransition(label = "hero_pulse")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        border = BorderStroke(1.dp, palette.border)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Large Score Arc
                Box(
                    contentAlignment = Alignment.Center, 
                    modifier = Modifier
                        .size(86.dp)
                        .graphicsLayer { scaleX = breatheScale; scaleY = breatheScale }
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = palette.glow.copy(alpha = 0.14f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx())
                        )
                        drawArc(
                            color = color,
                            startAngle = -90f,
                            sweepAngle = (today.score / 100f) * 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        "${today.score}", 
                        fontSize = 28.sp, 
                        fontWeight = FontWeight.Black, 
                        color = color
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column {
                    Text(
                        "TODAY'S SNAPSHOT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = color.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        today.mood?.value ?: "Steady Energy",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        today.dominant_planet_meaning ?: "Universal alignment",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = color.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // Stats Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MicroStat(
                    label = "ADJUST",
                    value = if ((today.personal_adjustment ?: 0) >= 0) "+${today.personal_adjustment}" else "${today.personal_adjustment}",
                    icon = "⚡",
                    color = color,
                    modifier = Modifier.weight(1f)
                )
                MicroStat(
                    label = "LUCKY",
                    value = today.lucky_color?.take(6) ?: "Gold",
                    icon = "🎨",
                    color = color,
                    modifier = Modifier.weight(1f)
                )
                MicroStat(
                    label = "DASHA",
                    value = forecast.active_dasha?.substringBefore(" ") ?: "Sun",
                    icon = "🔮",
                    color = color,
                    modifier = Modifier.weight(1.2f)
                )
            }
        }
    }
}

@Composable
fun MicroStat(label: String, value: String, icon: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = color.copy(alpha = 0.6f))
            }
            Text(value.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun DetailedForecastDayCard(day: ForecastDay, area: String, themeColor: Color, index: Int) {
    var isExpanded by remember { mutableStateOf(day.is_today) }
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val palette = remember(area, day.score, isDarkTheme) {
        ScoreColors.paletteFor(area, day.score, isDarkTheme)
    }
    val scoreColor = if (isDarkTheme) palette.glow else palette.main
    
    val entranceAlpha = remember { Animatable(0f) }
    val entranceSlide = remember { Animatable(30f) }
    val chevronRotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "chevron")

    LaunchedEffect(Unit) {
        delay(index * 80L)
        launch { entranceAlpha.animateTo(1f, tween(500)) }
        launch { entranceSlide.animateTo(0f, tween(500, easing = EaseOutCubic)) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = entranceAlpha.value
                translationY = entranceSlide.value
            }
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (day.is_today) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = if (day.is_today) 1.5.dp else 1.dp,
            color = if (day.is_today) palette.border else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
        )
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score dot + number
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            palette.surface,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${day.score}", 
                        fontSize = 13.sp, 
                        fontWeight = FontWeight.Black, 
                        color = scoreColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val dateLabel = try {
                        val parts = day.date.split("-")
                        if (parts.size == 3) "${parts[2]}/${parts[1]}" else day.date
                    } catch (e: Exception) { day.date }

                    val dayOfWeek = try {
                        val sdfIn = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        val sdfOut = java.text.SimpleDateFormat("EEE", java.util.Locale.US)
                        val dateObj = sdfIn.parse(day.date)
                        dateObj?.let { sdfOut.format(it).uppercase() } ?: day.date.take(3).uppercase()
                    } catch (e: Exception) {
                        day.date.take(3).uppercase()
                    }

                    Text(
                        "$dayOfWeek • $dateLabel",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (day.is_today) scoreColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        day.mood?.value ?: "Reflective",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(chevronRotation),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = spring(Spring.DampingRatioLowBouncy)) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Left Accent Bar
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(IntrinsicSize.Min)
                            .background(if (day.is_today) scoreColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            .align(Alignment.CenterVertically)
                    )

                    Column(modifier = Modifier.padding(start = 12.dp, end = 16.dp, bottom = 20.dp)) {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            day.text,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LuckyPillCompact("🎨 COLOR", day.lucky_color ?: "N/A", scoreColor)
                            LuckyPillCompact("🎲 NUM", "${day.lucky_number ?: "N/A"}", scoreColor)
                            LuckyPillCompact("⚡ ADJ", "${day.personal_adjustment ?: 0}", scoreColor)
                        }

                        if (!day.personalized_alerts.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("⚠️ ALERTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = scoreColor.copy(alpha = 0.6f))
                            day.personalized_alerts.forEach { alert ->
                                AlertRow(alert)
                            }
                        }

                        if (!day.transits.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("🪐 TRANSITS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = scoreColor.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(8.dp))
                            TransitRow(day.transits)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LuckyPillCompact(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = color.copy(alpha = 0.6f))
            Text(value.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun AlertRow(alert: PersonalizedAlert) {
    val color = when(alert.type) {
        "warning" -> MaterialTheme.colorScheme.error
        "positive" -> Color(0xFF10B981)
        else -> AstroColors.Jupiter
    }
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.padding(top = 4.dp).size(4.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(10.dp))
        Text(alert.simple, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
    }
}

@Composable
fun TransitRow(transits: Map<String, ForecastTransit>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(transits.entries.toList()) { (planet, transit) ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Text(planet.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = AstroColors.getPlanetaryColor(planet))
                    Text("${transit.sign} (H${transit.house_from_lagna})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ForecastSkeleton() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(28.dp)).shimmerEffect())
        Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(24.dp)).shimmerEffect())
        repeat(5) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(20.dp)).shimmerEffect())
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Oops!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("TRY AGAIN")
        }
    }
}

fun getAreaColor(area: String): Color = ScoreColors.categoryBase(area)

fun getAreaEmoji(area: String): String = when(area.lowercase()) {
    "love" -> "💕"
    "health" -> "🏥"
    "career" -> "💼"
    "finance" -> "💰"
    else -> "✨"
}
