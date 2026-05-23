package com.astranavi.app.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.astranavi.app.R
import com.astranavi.app.data.model.TimeTrigger
import com.astranavi.app.ui.components.floatingCard
import com.astranavi.app.ui.components.responsiveMetrics
import com.astranavi.app.ui.theme.AstroColors
import com.astranavi.app.util.LocaleFormatter
import com.astranavi.app.util.currentAppLocale
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

// ─── Bottom Sheet State ───────────────────────────────────────────────────────

sealed class DashboardBottomSheetState {
    object None : DashboardBottomSheetState()
    object AstroDetails : DashboardBottomSheetState()
    object FullEnergy : DashboardBottomSheetState()
    data class LifeAreaDetails(val areaId: String) : DashboardBottomSheetState()
    object AlertDetails : DashboardBottomSheetState()
    object CosmicHourDetails : DashboardBottomSheetState()
    data class FamilyMemberDetails(val memberId: String) : DashboardBottomSheetState()
}

// ─── Small Local Utilities ────────────────────────────────────────────────────

private fun cleanAreaLabels(text: String?): String {
    if (text.isNullOrBlank()) return ""
    var result = text
        .replace("area_label.vitality", "vitality")
        .replace("area_label.income", "income")
        .replace("area_label.home", "home life")
        .replace("area_label.romance", "romance")
        .replace("area_label.wealth", "wealth")
        .replace("area_label.self", "self-confidence")
    result = result.replace(Regex("area_label\\.([A-Za-z_]+)")) { match ->
        match.groupValues[1].replace("_", " ").replaceFirstChar { it.lowercase() }
    }
    return result
}

private fun formatTimeRange12h(start: String, end: String): String {
    fun fmt(time: String): String {
        return try {
            val parts = time.split(":")
            val hour = parts[0].trim().toInt()
            val minute = parts[1].trim().split(" ")[0].toInt()
            val ampm = if (hour >= 12) "PM" else "AM"
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            "$displayHour:${if (minute < 10) "0$minute" else "$minute"} $ampm"
        } catch (e: Exception) { time }
    }
    return "${fmt(start)} - ${fmt(end)}"
}

@Composable
private fun scoreToneColor(score: Int): Color {
    return when {
        score < 40 -> Color(0xFFB91C1C)
        score < 55 -> Color(0xFFD97706)
        score < 70 -> AstroColors.Jupiter
        score < 85 -> Color(0xFF159957)
        else -> Color(0xFF0F766E)
    }
}

// ─── Header Inline Chip (used inside DashboardHeader for tablet) ──────────────

@Composable
fun HeaderInlineChip(
    chip: ChipUiModel,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = chip.color.copy(alpha = 0.1f),
        border = BorderStroke(0.8.dp, chip.color.copy(alpha = 0.3f)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = chip.label.uppercase(),
                    fontSize = 8.sp,
                    lineHeight = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = chip.color.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = chip.value,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = chip.color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─── 1. Greeting + Daily Chips (Mobile-only) ──────────────────────────────────

@Composable
fun GreetingAndChips(
    header: HeaderUiModel,
    chips: List<ChipUiModel>,
    onChipClick: () -> Unit
) {
    val responsive = responsiveMetrics()

    if (responsive.isTabletWidth) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(
                    text = "${header.greeting},",
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = header.name,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chips.forEach { chip ->
                    DailyChip(chip = chip, onClick = onChipClick)
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${header.greeting},",
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = header.name,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.wrapContentWidth().padding(start = 8.dp)
            ) {
                chips.chunked(2).forEach { rowChips ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowChips.forEach { chip ->
                            DailyChip(chip = chip, onClick = onChipClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyChip(
    chip: ChipUiModel,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = chip.color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, chip.color.copy(alpha = 0.28f)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val imageResId = remember(chip.iconName) {
                if (!chip.iconName.isNullOrBlank()) {
                    context.resources.getIdentifier(chip.iconName, "drawable", context.packageName).takeIf { it != 0 }
                } else null
            }
            if (imageResId != null) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
            }
            Column {
                Text(
                    text = chip.label.uppercase(),
                    fontSize = 8.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = chip.color.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = chip.value,
                    fontSize = 13.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = chip.color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─── 2. Daily Hero Card ───────────────────────────────────────────────────────

@Composable
fun DailyHeroCard(
    hero: HeroCardUiModel,
    cosmicHour: CosmicHourUiModel,
    themeColor: Color,
    onScoreClick: () -> Unit,
    onCosmicHourClick: () -> Unit
) {
    val responsive = responsiveMetrics()
    val toneColor = scoreToneColor(hero.overallScore)
    val planetSize = when {
        responsive.isVeryCompactWidth -> 84.dp
        responsive.isCompactWidth -> 100.dp
        responsive.isMediumWidth && !responsive.isTabletWidth -> 124.dp
        else -> 140.dp
    }
    val scoreRingSize = if (responsive.isCompactWidth) 64.dp else 76.dp
    val scoreInnerSize = if (responsive.isCompactWidth) 54.dp else 64.dp
    var showPlanetPopup by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .floatingCard(accent = toneColor, cornerRadius = 22.dp, elevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            hero.dominantPlanetColor.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        radius = 280f
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(responsive.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onScoreClick)
                ) {
                    Box(
                        modifier = Modifier.size(scoreRingSize),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .border(1.dp, toneColor.copy(alpha = 0.18f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(scoreInnerSize)
                                .background(toneColor.copy(alpha = 0.10f), CircleShape)
                                .border(2.dp, toneColor.copy(alpha = 0.55f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${hero.overallScore}",
                                fontSize = if (responsive.isCompactWidth) 22.sp else 28.sp,
                                fontWeight = FontWeight.Black,
                                color = toneColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.dashboard_mood_day, titleCase(hero.moodValue)),
                            fontSize = if (responsive.isCompactWidth) 16.sp else 18.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = toneColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (hero.subtext.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cleanAreaLabels(hero.subtext),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onCosmicHourClick)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = cosmicHour.activeColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = stringResource(R.string.dashboard_label_cosmic_hour),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = cosmicHour.activeColor,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = cosmicHour.activeLabel,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = cosmicHour.activeTime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cosmicHour.activeColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (cosmicHour.activeAdvice.isNotBlank()) {
                        Text(
                            text = cleanAreaLabels(cosmicHour.activeAdvice),
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Box(
                modifier = Modifier.clickable { showPlanetPopup = !showPlanetPopup }
            ) {
                HeroPlanetVisual(
                    planet = hero.dominantPlanet,
                    planetColor = hero.dominantPlanetColor,
                    size = planetSize
                )
                if (showPlanetPopup) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.32f), CircleShape)
                            .clickable { showPlanetPopup = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "DOMINANT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White.copy(alpha = 0.78f),
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = hero.dominantPlanet,
                                fontSize = 18.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroPlanetVisual(
    planet: String,
    planetColor: Color,
    size: androidx.compose.ui.unit.Dp
) {
    val key = planet.trim().lowercase()
    val isPlaceholder = key == "rahu" || key == "ketu" || key.isBlank()
    Box(
        modifier = Modifier
            .size(size)
            .background(planetColor.copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isPlaceholder) {
            Icon(
                Icons.Default.Brightness2,
                contentDescription = if (planet.isNotBlank()) "Dominant planet $planet" else null,
                tint = planetColor.copy(alpha = 0.55f),
                modifier = Modifier.size(size * 0.6f)
            )
        } else {
            AstroAssetImage(
                assetName = key,
                contentDescription = "Dominant planet $planet",
                modifier = Modifier.size(size * 0.78f),
                fallbackTint = planetColor
            )
        }
    }
}

// ─── 3. Life Areas Carousel ───────────────────────────────────────────────────

@Composable
fun LifeAreasCarousel(
    areas: List<LifeAreaCardUiModel>,
    onAreaClick: (LifeAreaCardUiModel) -> Unit
) {
    if (areas.isEmpty()) return

    val displayOrder = listOf("finance", "love", "general", "career", "health")
    val ordered = displayOrder
        .mapNotNull { id -> areas.firstOrNull { it.id == id } }
        .ifEmpty { areas }

    val generalIndex = ordered.indexOfFirst { it.id == "general" }
        .let { if (it < 0) ordered.size / 2 else it }
    val initialPage = (Int.MAX_VALUE / 2) -
        ((Int.MAX_VALUE / 2) % ordered.size) + generalIndex

    val pagerState = rememberPagerState(initialPage = initialPage) { Int.MAX_VALUE }
    val scope = rememberCoroutineScope()
    val cardWidth = 168.dp

    Column(modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val sidePad = ((maxWidth - cardWidth) / 2).coerceAtLeast(16.dp)
            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fixed(cardWidth),
                pageSpacing = 12.dp,
                contentPadding = PaddingValues(horizontal = sidePad),
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val area = ordered[page % ordered.size]
                val rawOffset =
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val offset = rawOffset.absoluteValue.coerceIn(0f, 1f)
                val scale = lerp(0.72f, 1f, 1f - offset)
                val cardAlpha = lerp(0.75f, 1f, 1f - offset)
                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = cardAlpha
                    }
                ) {
                    LifeAreaCard(
                        area = area,
                        onClick = {
                            if (page == pagerState.currentPage) {
                                onAreaClick(area)
                            } else {
                                scope.launch { pagerState.animateScrollToPage(page) }
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val activePageIndex = pagerState.currentPage % ordered.size
        val activeColor = ordered.getOrNull(activePageIndex)?.color
            ?: MaterialTheme.colorScheme.primary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ordered.forEachIndexed { index, _ ->
                val isActive = index == activePageIndex
                Box(
                    modifier = Modifier
                        .size(if (isActive) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) activeColor
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
                        )
                )
            }
        }
    }
}

@Composable
private fun LifeAreaCard(
    area: LifeAreaCardUiModel,
    onClick: () -> Unit
) {
    val toneColor = scoreToneColor(area.score)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .floatingCard(accent = area.color, cornerRadius = 16.dp, elevation = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = area.icon, fontSize = 18.sp)
                Text(
                    text = LocaleFormatter.number(area.score, currentAppLocale()),
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = toneColor
                )
            }
            Text(
                text = area.label,
                fontSize = 13.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Status pill
            Box(
                modifier = Modifier
                    .background(toneColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .border(0.5.dp, toneColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = area.status,
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = toneColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (area.shortInsight.isNotBlank()) {
                Text(
                    text = cleanAreaLabels(area.shortInsight),
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─── 4. Alert + Cosmic Hour Row ───────────────────────────────────────────────

@Composable
fun AlertAndCosmicHourRow(
    alert: AlertUiModel,
    cosmicHour: CosmicHourUiModel,
    onAlertClick: () -> Unit,
    onCosmicClick: () -> Unit
) {
    val responsive = responsiveMetrics()
    if (responsive.isCompactWidth) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AlertMiniCard(alert = alert, onClick = onAlertClick, modifier = Modifier.fillMaxWidth())
            CosmicHourMiniCard(cosmic = cosmicHour, onClick = onCosmicClick, modifier = Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AlertMiniCard(alert = alert, onClick = onAlertClick, modifier = Modifier.weight(1f))
            CosmicHourMiniCard(cosmic = cosmicHour, onClick = onCosmicClick, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AlertMiniCard(
    alert: AlertUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = when (alert.importance.lowercase()) {
        "high" -> AstroColors.Mars
        "medium" -> AstroColors.Jupiter
        else -> AstroColors.Default
    }
    Box(
        modifier = modifier
            .floatingCard(accent = accent, cornerRadius = 14.dp, elevation = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "ALERT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = accent,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = alert.title,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = cleanAreaLabels(alert.simpleExplanation),
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CosmicHourMiniCard(
    cosmic: CosmicHourUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .floatingCard(accent = cosmic.activeColor, cornerRadius = 14.dp, elevation = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = cosmic.activeColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "COSMIC HOUR",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = cosmic.activeColor,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = cosmic.activeLabel,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = cosmic.activeTime,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = cosmic.activeColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = cleanAreaLabels(cosmic.activeAdvice),
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── 5. Family Relationships Section ──────────────────────────────────────────

@Composable
fun FamilyRelationshipsSection(
    members: List<FamilyMemberUiModel>,
    isTablet: Boolean,
    onMemberClick: (FamilyMemberUiModel) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(members, key = { it.id }) { member ->
            FamilyMemberAvatarCard(member = member, onClick = { onMemberClick(member) })
        }
    }
}

@Composable
private fun FamilyMemberAvatarCard(
    member: FamilyMemberUiModel,
    onClick: () -> Unit
) {
    val toneColor = scoreToneColor(member.score)
    Box(
        modifier = Modifier
            .width(96.dp)
            .floatingCard(accent = toneColor, cornerRadius = 14.dp, elevation = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(toneColor.copy(alpha = 0.12f), CircleShape)
                    .border(1.5.dp, toneColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = member.name,
                    tint = toneColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = member.name,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = LocaleFormatter.number(member.score, currentAppLocale()),
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Black,
                color = toneColor
            )
            Text(
                text = member.bondingStatus,
                fontSize = 8.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── 6. Bottom Sheet Content: Astro Details (Panchanga) ───────────────────────

@Composable
fun AstroDetailsBottomSheetContent(
    panchanga: PanchangaUiModel,
    onAskNavi: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    BottomSheetScaffold(
        title = stringResource(R.string.dashboard_title_astro_details),
        subtitle = stringResource(R.string.dashboard_subtitle_panchanga_lucky),
        askNaviAction = {
            AskNaviPill(accent = AstroColors.Jupiter, onClick = {
                onAskNavi(context.getString(R.string.dashboard_prompt_ask_navi_astro))
            })
        }
    ) {
        InfoCard(
            title = stringResource(R.string.popup_section_tithi_nakshatra),
            accent = AstroColors.Moon,
            leadingIcon = Icons.Default.NightsStay
        ) {
            PanchangaRow(label = stringResource(R.string.dashboard_label_tithi), value = panchanga.tithi, color = AstroColors.Moon)
            PanchangaRow(label = stringResource(R.string.dashboard_label_nakshatra), value = panchanga.nakshatra, color = AstroColors.Mercury)
        }
        InfoCard(
            title = stringResource(R.string.popup_section_yoga_karana_vaara),
            accent = AstroColors.Jupiter,
            leadingIcon = Icons.Default.AutoAwesome
        ) {
            PanchangaRow(label = stringResource(R.string.dashboard_label_yoga), value = panchanga.yoga, color = AstroColors.Jupiter)
            PanchangaRow(label = stringResource(R.string.dashboard_label_karana), value = panchanga.karana, color = AstroColors.Saturn)
            PanchangaRow(label = stringResource(R.string.dashboard_label_vaara), value = panchanga.vaara, color = AstroColors.Sun)
        }
        InfoCard(
            title = stringResource(R.string.popup_section_lucky_today),
            accent = AstroColors.Venus,
            leadingIcon = Icons.Default.Star
        ) {
            PanchangaRow(label = stringResource(R.string.popup_label_lucky_color), value = panchanga.luckyColor, color = AstroColors.Venus)
            PanchangaRow(label = stringResource(R.string.popup_label_lucky_number), value = panchanga.luckyNumber.toString(), color = AstroColors.Sun)
        }
        if (panchanga.retrogradePlanets.isNotEmpty()) {
            InfoCard(
                title = stringResource(R.string.popup_section_retrograde_planets),
                accent = AstroColors.Mars,
                leadingIcon = Icons.Default.SwapHoriz
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    panchanga.retrogradePlanets.forEach { planet ->
                        val asset = planetAssetFor(planet)
                        val color = AstroColors.getPlanetaryColor(planet)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = color.copy(alpha = 0.10f),
                            border = BorderStroke(0.6.dp, color.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (asset != null) {
                                    AstroAssetImage(
                                        assetName = asset,
                                        contentDescription = planet,
                                        modifier = Modifier.size(18.dp),
                                        fallbackTint = color
                                    )
                                }
                                Text(
                                    text = titleCase(planet),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanchangaRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─── 7. Bottom Sheet Content: Full Energy ─────────────────────────────────────

@Composable
fun FullEnergyBottomSheetContent(
    hero: HeroCardUiModel,
    alert: AlertUiModel,
    onAskNavi: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val tip = cleanAreaLabels(hero.subtext)
    val whatsHappening = cleanAreaLabels(alert.simpleExplanation)
    val whatToDo = cleanAreaLabels(alert.whatToDo)
    val technical = cleanAreaLabels(alert.technicalReason)
    val planetName = hero.dominantPlanet
    val planetAsset = hero.dominantPlanetAsset ?: planetAssetFor(planetName)
    val activeTransit = cleanAreaLabels(hero.activeTransit)
    BottomSheetScaffold(
        title = stringResource(R.string.dashboard_title_full_energy),
        subtitle = stringResource(R.string.dashboard_subtitle_full_energy, titleCase(hero.moodValue), hero.overallScore),
        askNaviAction = {
            AskNaviPill(accent = AstroColors.Sun, onClick = {
                onAskNavi(context.getString(R.string.dashboard_prompt_ask_navi_today, hero.overallScore, hero.moodValue))
            })
        }
    ) {
        MiniScoreCard(
            accent = hero.dominantPlanetColor,
            label = stringResource(R.string.dashboard_label_overall_energy),
            score = hero.overallScore,
            statusText = titleCase(hero.moodValue),
            oneLine = tip
        ) {
            if (planetAsset != null) {
                AstroAssetImage(
                    assetName = planetAsset,
                    contentDescription = planetName,
                    modifier = Modifier.size(40.dp),
                    fallbackTint = hero.dominantPlanetColor
                )
            } else {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = hero.dominantPlanetColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        if (tip.isNotBlank()) {
            InfoCard(
                title = stringResource(R.string.popup_section_today_message),
                accent = hero.dominantPlanetColor,
                leadingIcon = Icons.Default.ChatBubbleOutline
            ) {
                Text(
                    text = tip,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )
            }
        }
        if (whatToDo.isNotBlank()) {
            InfoCard(
                title = stringResource(R.string.popup_section_what_to_do),
                accent = AstroColors.Sun,
                leadingIcon = Icons.Default.TaskAlt
            ) {
                Text(
                    text = whatToDo,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )
            }
        }
        if (planetName.isNotBlank() || whatsHappening.isNotBlank()) {
            InfoCard(
                title = stringResource(R.string.popup_section_why_this_is_happening),
                accent = hero.dominantPlanetColor,
                leadingIcon = Icons.Default.Brightness2
            ) {
                if (planetName.isNotBlank()) {
                    PlanetInfluenceRow(
                        planetName = titleCase(planetName),
                        planetAsset = planetAsset,
                        planetColor = hero.dominantPlanetColor,
                        effect = activeTransit
                    )
                }
                if (whatsHappening.isNotBlank()) {
                    Text(
                        text = whatsHappening,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                    )
                }
            }
        }
        if (technical.isNotBlank() || activeTransit.isNotBlank()) {
            ExpandableSection(
                title = stringResource(R.string.popup_section_technical_astrology),
                accent = hero.dominantPlanetColor.copy(alpha = 0.7f),
                leadingIcon = Icons.Default.AutoAwesome
            ) {
                if (technical.isNotBlank()) {
                    DetailBlock(label = stringResource(R.string.dashboard_label_technical_reason), value = technical)
                }
                if (activeTransit.isNotBlank()) {
                    DetailBlock(label = stringResource(R.string.dashboard_label_active_dasha), value = activeTransit)
                }
            }
        }
    }
}

// ─── 8. Bottom Sheet Content: Life Area ───────────────────────────────────────

@Composable
fun LifeAreaBottomSheetContent(
    area: LifeAreaCardUiModel,
    hero: HeroCardUiModel,
    onAskNavi: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val fullInsight = cleanAreaLabels(area.fullInsight.ifBlank { area.shortInsight })
    val oneLine = cleanAreaLabels(area.shortInsight.ifBlank { fullInsight })
    val actionItems = area.personalNotes.map { cleanAreaLabels(it) }.filter { it.isNotBlank() }
    val planetName = hero.dominantPlanet
    val planetAsset = hero.dominantPlanetAsset ?: planetAssetFor(planetName)
    val activeTransit = cleanAreaLabels(hero.activeTransit)
    val showTechnical = planetName.isNotBlank() || activeTransit.isNotBlank()
    BottomSheetScaffold(
        title = stringResource(R.string.dashboard_title_area_energy_today, area.label),
        subtitle = stringResource(R.string.dashboard_subtitle_area_energy_today, area.score, area.status),
        askNaviAction = {
            AskNaviPill(accent = area.color, onClick = {
                onAskNavi(context.getString(R.string.dashboard_prompt_ask_navi_area, area.label, area.score, area.status))
            })
        }
    ) {
        MiniScoreCard(
            accent = area.color,
            label = area.label,
            score = area.score,
            statusText = area.status,
            oneLine = oneLine
        ) {
            Text(text = area.icon, fontSize = 26.sp)
        }
        if (fullInsight.isNotBlank()) {
            InfoCard(
                title = stringResource(R.string.popup_section_today_message),
                accent = area.color,
                leadingIcon = Icons.Default.ChatBubbleOutline
            ) {
                Text(
                    text = fullInsight,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )
            }
        }
        if (actionItems.isNotEmpty()) {
            InfoCard(
                title = stringResource(R.string.popup_section_what_to_do),
                accent = area.color,
                leadingIcon = Icons.Default.TaskAlt
            ) {
                BulletList(items = actionItems, accent = area.color)
            }
        }
        if (planetName.isNotBlank()) {
            InfoCard(
                title = stringResource(R.string.popup_section_why_this_is_happening),
                accent = area.color,
                leadingIcon = Icons.Default.Brightness2
            ) {
                PlanetInfluenceRow(
                    planetName = titleCase(planetName),
                    planetAsset = planetAsset,
                    planetColor = hero.dominantPlanetColor,
                    effect = activeTransit
                )
            }
        }
        if (showTechnical) {
            ExpandableSection(
                title = stringResource(R.string.popup_section_technical_astrology),
                accent = area.color.copy(alpha = 0.7f),
                leadingIcon = Icons.Default.AutoAwesome
            ) {
                if (planetName.isNotBlank()) {
                    DetailBlock(
                        label = stringResource(R.string.dashboard_label_dominant_planet),
                        value = titleCase(planetName),
                        accent = hero.dominantPlanetColor
                    )
                }
                if (activeTransit.isNotBlank()) {
                    DetailBlock(
                        label = stringResource(R.string.dashboard_label_active_dasha),
                        value = activeTransit
                    )
                }
            }
        }
    }
}

// ─── 9. Bottom Sheet Content: Alert Details ───────────────────────────────────

@Composable
fun AlertBottomSheetContent(
    alert: AlertUiModel,
    onAskNavi: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val importanceColor = when (alert.importance.lowercase()) {
        "high" -> AstroColors.Mars
        "medium" -> AstroColors.Jupiter
        else -> AstroColors.Default
    }
    val simple = cleanAreaLabels(alert.simpleExplanation)
    val technical = cleanAreaLabels(alert.technicalReason)
    val whatToDo = cleanAreaLabels(alert.whatToDo)
    val whatToAvoid = cleanAreaLabels(alert.whatToAvoid)
    BottomSheetScaffold(
        title = alert.title,
        subtitle = stringResource(R.string.dashboard_subtitle_alert, alert.importance, alert.impactArea),
        askNaviAction = {
            AskNaviPill(accent = importanceColor, onClick = {
                onAskNavi(context.getString(R.string.dashboard_prompt_ask_navi_alert, alert.title))
            })
        }
    ) {
        MiniScoreCard(
            accent = importanceColor,
            label = alert.title,
            score = null,
            statusText = alert.importance.uppercase(),
            oneLine = simple
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = importanceColor,
                modifier = Modifier.size(28.dp)
            )
        }
        if (simple.isNotBlank()) {
            InfoCard(
                title = stringResource(R.string.popup_section_today_message),
                accent = importanceColor,
                leadingIcon = Icons.Default.ChatBubbleOutline
            ) {
                Text(
                    text = simple,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )
            }
        }
        if (whatToDo.isNotBlank()) {
            InfoCard(
                title = stringResource(R.string.popup_section_what_to_do),
                accent = Color(0xFF159957),
                leadingIcon = Icons.Default.TaskAlt
            ) {
                Text(
                    text = whatToDo,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )
            }
        }
        if (whatToAvoid.isNotBlank()) {
            InfoCard(
                title = stringResource(R.string.popup_section_what_to_avoid),
                accent = AstroColors.Mars,
                leadingIcon = Icons.Default.Block
            ) {
                Text(
                    text = whatToAvoid,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )
            }
        }
        if (technical.isNotBlank()) {
            ExpandableSection(
                title = stringResource(R.string.popup_section_technical_astrology),
                accent = importanceColor.copy(alpha = 0.7f),
                leadingIcon = Icons.Default.AutoAwesome
            ) {
                Text(
                    text = technical,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )
            }
        }
        if (alert.secondaryAlerts.isNotEmpty()) {
            ExpandableSection(
                title = stringResource(R.string.popup_section_other_alerts),
                accent = AstroColors.Default,
                leadingIcon = Icons.Default.Notifications
            ) {
                BulletList(items = alert.secondaryAlerts.map { cleanAreaLabels(it) }, accent = AstroColors.Default)
            }
        }
    }
}

// ─── 10. Bottom Sheet Content: Cosmic Hour ────────────────────────────────────

@Composable
fun CosmicHourBottomSheetContent(
    cosmicHour: CosmicHourUiModel,
    onDismiss: () -> Unit
) {
    BottomSheetScaffold(
        title = stringResource(R.string.dashboard_title_cosmic_hours),
        subtitle = stringResource(R.string.dashboard_subtitle_cosmic_hours)
    ) {
        if (cosmicHour.allTriggers.isEmpty()) {
            InfoCard(
                title = stringResource(R.string.dashboard_label_time_triggers),
                accent = AstroColors.Default,
                leadingIcon = Icons.Default.Schedule
            ) {
                Text(
                    text = stringResource(R.string.dashboard_msg_no_cosmic_hours),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                )
            }
        } else {
            cosmicHour.allTriggers.forEach { trigger ->
                CosmicTriggerCard(trigger = trigger)
            }
        }
    }
}

@Composable
private fun CosmicTriggerCard(trigger: TimeTrigger) {
    val color = when (trigger.type.lowercase()) {
        "social" -> AstroColors.Venus
        "emotional" -> AstroColors.Moon
        "energy" -> AstroColors.Mars
        else -> AstroColors.Default
    }
    val icon = when (trigger.type.lowercase()) {
        "social" -> Icons.Default.Favorite
        "emotional" -> Icons.Default.NightsStay
        "energy" -> Icons.Default.LocalFireDepartment
        else -> Icons.Default.Schedule
    }
    InfoCard(
        title = trigger.label.ifBlank { titleCase(trigger.type) },
        accent = color,
        leadingIcon = icon
    ) {
        Text(
            text = formatTimeRange12h(trigger.start, trigger.end),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        if (trigger.advice.isNotBlank()) {
            Text(
                text = cleanAreaLabels(trigger.advice),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
        }
        if (!trigger.reason.isNullOrBlank()) {
            Text(
                text = cleanAreaLabels(trigger.reason),
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

// ─── 11. Bottom Sheet Content: Family Member ──────────────────────────────────

@Composable
fun FamilyMemberBottomSheetContent(
    member: FamilyMemberUiModel,
    onAskNavi: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val toneColor = scoreToneColor(member.score)
    BottomSheetScaffold(
        title = member.name,
        subtitle = "${member.relation} · Score ${member.score}",
        stickyBottom = {
            StickyAskNaviButton(
                label = "Ask Navi About ${member.name}",
                color = toneColor,
                onClick = { onAskNavi("Tell me more about my relationship with ${member.name} (${member.relation}). Score: ${member.score}.") }
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(toneColor.copy(alpha = 0.12f), CircleShape)
                    .border(2.dp, toneColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = member.name,
                    tint = toneColor,
                    modifier = Modifier.size(40.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "RELATIONSHIP SCORE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = LocaleFormatter.number(member.score, currentAppLocale()),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = toneColor
                )
                Text(
                    text = member.bondingStatus,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = toneColor
                )
            }
        }

        SectionDivider()

        DetailBlock(label = "BONDING", value = member.bondingStatus)
        DetailBlock(label = "COMMUNICATION", value = member.communicationLevel)
        DetailBlock(label = "EMOTIONAL CONNECTION", value = member.emotionalConnection)

        SectionDivider()

        DetailBlock(label = "ADVICE", value = cleanAreaLabels(member.advice))
    }
}

// ─── Shared Scaffolding for Bottom Sheets ─────────────────────────────────────

@Composable
private fun BottomSheetScaffold(
    title: String,
    subtitle: String,
    stickyBottom: (@Composable () -> Unit)? = null,
    askNaviAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = if (stickyBottom != null) 12.dp else 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (askNaviAction != null) {
                    askNaviAction()
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
        if (stickyBottom != null) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                stickyBottom()
            }
        }
    }
}

@Composable
private fun StickyAskNaviButton(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 10.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    )
}

@Composable
private fun DetailBlock(label: String, value: String, accent: Color? = null) {
    if (value.isBlank()) return
    Column(modifier = Modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (accent != null) {
                Box(modifier = Modifier.size(6.dp).background(accent, CircleShape))
            }
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 1.5.sp
            )
        }
        Text(
            text = value,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
        )
    }
}

// ─── Popup Building Blocks (card-based redesign) ──────────────────────────────

@Composable
private fun AskNaviPill(accent: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = accent,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = stringResource(R.string.popup_pill_ask_navi),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.4.sp
            )
        }
    }
}

@Composable
private fun MiniScoreCard(
    accent: Color,
    label: String,
    score: Int?,
    statusText: String?,
    oneLine: String,
    iconContent: @Composable () -> Unit
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .floatingCard(accent = accent, cornerRadius = 16.dp, elevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(accent.copy(alpha = 0.14f), CircleShape)
                    .border(1.5.dp, accent.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    letterSpacing = 1.4.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (score != null) {
                        val tone = scoreToneColor(score)
                        Text(
                            text = LocaleFormatter.number(score, currentAppLocale()),
                            fontSize = 26.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = tone
                        )
                        if (!statusText.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .background(tone.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .border(0.5.dp, tone.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = tone,
                                    maxLines = 1
                                )
                            }
                        }
                    } else if (!statusText.isNullOrBlank()) {
                        Text(
                            text = statusText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = accent
                        )
                    }
                }
                if (oneLine.isNotBlank()) {
                    Text(
                        text = oneLine,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    accent: Color = AstroColors.Default,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .floatingCard(accent = accent, cornerRadius = 14.dp, elevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (leadingIcon != null) {
                    Icon(
                        leadingIcon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = accent,
                    letterSpacing = 0.4.sp
                )
            }
            content()
        }
    }
}

@Composable
private fun BulletList(items: List<String>, accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach { item ->
            if (item.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(5.dp)
                            .background(accent, CircleShape)
                    )
                    Text(
                        text = item,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanetInfluenceRow(
    planetName: String,
    planetAsset: String?,
    planetColor: Color,
    effect: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(planetColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (planetAsset != null) {
                AstroAssetImage(
                    assetName = planetAsset,
                    contentDescription = planetName,
                    modifier = Modifier.size(24.dp),
                    fallbackTint = planetColor
                )
            } else {
                Icon(
                    Icons.Default.Brightness2,
                    contentDescription = planetName,
                    tint = planetColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = planetName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = planetColor
            )
            if (effect.isNotBlank()) {
                Text(
                    text = effect,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                )
            }
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    accent: Color = AstroColors.Default,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "expand-rotate")
    Box(modifier = Modifier
        .fillMaxWidth()
        .floatingCard(accent = accent, cornerRadius = 14.dp, elevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (leadingIcon != null) {
                    Icon(
                        leadingIcon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = accent,
                    letterSpacing = 0.4.sp
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(rotation)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
            }
        }
    }
}

private fun planetAssetFor(name: String?): String? = when (name?.trim()?.lowercase()) {
    "sun", "surya" -> "sun"
    "moon", "chandra" -> "moon"
    "mars", "mangal" -> "mars"
    "mercury", "budh" -> "mercury"
    "jupiter", "guru" -> "jupiter"
    "venus", "shukra" -> "venus"
    "saturn", "shani" -> "saturn"
    else -> null
}
