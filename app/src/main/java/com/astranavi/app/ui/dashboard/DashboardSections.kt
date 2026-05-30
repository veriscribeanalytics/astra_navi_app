package com.astranavi.app.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.astranavi.app.ui.components.responsiveFontSizes
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.astranavi.app.BuildConfig
import com.astranavi.app.R
import com.astranavi.app.data.model.TimeTrigger
import com.astranavi.app.data.model.ChatAvatar
import com.astranavi.app.ui.chat.ChatAvatarImage
import com.astranavi.app.ui.chat.chatAvatarPalette
import com.astranavi.app.ui.components.floatingCard
import com.astranavi.app.ui.components.responsiveMetrics
import com.astranavi.app.ui.theme.AstroColors
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import com.astranavi.app.ui.components.ScoreColors
import com.astranavi.app.util.LocaleFormatter
import com.astranavi.app.util.currentAppLocale
import com.astranavi.app.util.ZodiacMapper
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2
import kotlin.math.round
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

private fun parseToMinutesStatic(timeStr: String?): Int? {
    if (timeStr.isNullOrBlank()) return null
    return try {
        val parts = timeStr.split(":")
        parts[0].trim().toInt() * 60 + parts[1].trim().split(" ")[0].toInt()
    } catch (e: Exception) { null }
}

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
        score < 35 -> Color(0xFFB91C1C)
        score < 50 -> Color(0xFFD97706)
        score < 65 -> AstroColors.Jupiter
        score < 80 -> Color(0xFF159957)
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

/**
 * Name text that fits gracefully regardless of length:
 *  • short name → full size, 1 line
 *  • medium name → wraps to 2 lines at full size
 *  • very long name → shrinks font down to [minFontSize] floor, then ellipsis
 */
@Composable
private fun ResponsiveNameText(
    name: String,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    color: Color,
    modifier: Modifier = Modifier
) {
    var fontSize by remember(name, maxFontSize, minFontSize) { mutableStateOf(maxFontSize) }
    var readyToDraw by remember(name, maxFontSize, minFontSize) { mutableStateOf(false) }

    Text(
        text = name,
        fontSize = fontSize,
        lineHeight = (fontSize.value * 1.15f).sp,
        fontWeight = FontWeight.Black,
        color = color,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { result ->
            if (!readyToDraw) {
                if ((result.didOverflowHeight || result.didOverflowWidth) &&
                    fontSize.value > minFontSize.value
                ) {
                    fontSize = (fontSize.value - 1f).coerceAtLeast(minFontSize.value).sp
                } else {
                    readyToDraw = true
                }
            }
        },
        modifier = modifier.drawWithContent { if (readyToDraw) drawContent() }
    )
}

@Composable
fun KundliChartTile(
    userEmail: String?,
    accessToken: String?,
    tileSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .size(tileSize)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
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
                contentDescription = stringResource(R.string.dashboard_label_vedic_birth_chart),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .graphicsLayer {
                        scaleX = 1.10f
                        scaleY = 1.10f
                    }
            ) {
                val state = painter.state
                when (state) {
                    is AsyncImagePainter.State.Loading,
                    is AsyncImagePainter.State.Empty -> Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = accent,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    is AsyncImagePainter.State.Error -> Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = stringResource(R.string.dashboard_label_chart_failed),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    else -> SubcomposeAsyncImageContent()
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.dashboard_label_sign_in_chart),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, accent.copy(alpha = 0.32f))
                    ),
                    shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                )
                .clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp).align(Alignment.Center),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_label_full_readings),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = accent,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun InteractiveZodiacWheel(
    activeMoonSign: String?,
    onNavigateToRashis: (String?) -> Unit,
    modifier: Modifier = Modifier,
    wheelSize: Dp = 160.dp
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val zodiacSigns = remember {
        listOf(
            "aries", "taurus", "gemini", "cancer",
            "leo", "virgo", "libra", "scorpio",
            "sagittarius", "capricorn", "aquarius", "pisces"
        )
    }
    
    val activeEnglishSign = remember(activeMoonSign) {
        ZodiacMapper.getEnglishName(activeMoonSign)?.lowercase()
    }
    
    val initialAngle = remember(activeEnglishSign) {
        val index = zodiacSigns.indexOf(activeEnglishSign).takeIf { it != -1 } ?: 0
        -90f - index * 30f
    }
    
    var rotationAngle by remember(initialAngle) { mutableStateOf(initialAngle) }

    // Slow continuous autorotation, additive to user-driven rotationAngle.
    // Visual only via graphicsLayer — leaves snap-on-click logic untouched.
    val spinTransition = rememberInfiniteTransition(label = "zodiacWheelSpin")
    val autoSpin by spinTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 90_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinOffset"
    )

    // Proportions calibrated against a 160dp reference wheel — scale with wheelSize.
    val halfWheelSizeDp = wheelSize * 0.5f
    val radiusDp = wheelSize * 0.3875f       // 62/160
    val centerIconSize = wheelSize * 0.2875f // 46/160
    val outerIconSize = wheelSize * 0.1375f  // 22/160
    val halfOuterIconSizeDp = outerIconSize * 0.5f
    val wheelGold = Color(0xFFE9B247) // Richer cosmic gold
    
    Box(
        modifier = modifier
            .size(wheelSize)
            .pointerInput(Unit) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        val prevPos = change.position - dragAmount
                        val currPos = change.position
                        val prevAngle = Math.toDegrees(atan2((prevPos.y - centerOffset.y).toDouble(), (prevPos.x - centerOffset.x).toDouble())).toFloat()
                        val currAngle = Math.toDegrees(atan2((currPos.y - centerOffset.y).toDouble(), (currPos.x - centerOffset.x).toDouble())).toFloat()
                        var delta = currAngle - prevAngle
                        if (delta > 180f) delta -= 360f
                        if (delta < -180f) delta += 360f
                        
                        rotationAngle += delta
                    },
                    onDragEnd = {
                        val currentRotation = rotationAngle
                        val alignedRotation = round((currentRotation + 90f) / 30f) * 30f - 90f
                        coroutineScope.launch {
                            androidx.compose.animation.core.animate(
                                initialValue = rotationAngle,
                                targetValue = alignedRotation,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ) { value, _ ->
                                rotationAngle = value
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotationAngle + autoSpin }
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rMax = size.width / 2f
            
            drawCircle(
                color = wheelGold.copy(alpha = 0.62f),
                radius = rMax - 2.dp.toPx(),
                style = Stroke(width = 1.1.dp.toPx())
            )

            drawCircle(
                color = wheelGold.copy(alpha = 0.38f),
                radius = rMax - 9.dp.toPx(),
                style = Stroke(
                    width = 0.9.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(4.dp.toPx(), 4.dp.toPx()),
                        0f
                    )
                )
            )

            drawCircle(
                color = wheelGold.copy(alpha = 0.42f),
                radius = rMax - 34.dp.toPx(),
                style = Stroke(width = 0.8.dp.toPx())
            )

            drawCircle(
                color = wheelGold.copy(alpha = 0.5f),
                radius = rMax - 50.dp.toPx(),
                style = Stroke(width = 0.8.dp.toPx())
            )

            for (i in 0 until 12) {
                val angleRad = Math.toRadians((i * 30.0))
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()

                drawLine(
                    color = wheelGold.copy(alpha = 0.26f),
                    start = Offset(cx + (rMax - 50.dp.toPx()) * cosA, cy + (rMax - 50.dp.toPx()) * sinA),
                    end = Offset(cx + (rMax - 9.dp.toPx()) * cosA, cy + (rMax - 9.dp.toPx()) * sinA),
                    strokeWidth = 0.6.dp.toPx()
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotationAngle + autoSpin }
        ) {
            zodiacSigns.forEachIndexed { index, sign ->
                val angleRad = Math.toRadians((index * 30.0))
                val cosVal = cos(angleRad).toFloat()
                val sinVal = sin(angleRad).toFloat()
                val xDp = halfWheelSizeDp + (radiusDp * cosVal) - halfOuterIconSizeDp
                val yDp = halfWheelSizeDp + (radiusDp * sinVal) - halfOuterIconSizeDp
                
                val isSelected = sign == activeEnglishSign
                val tintColor = if (isSelected) wheelGold else wheelGold.copy(alpha = 0.72f)
                
                Box(
                    modifier = Modifier
                        .offset(x = xDp, y = yDp)
                        .size(outerIconSize)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            val targetRotation = -90f - index * 30f
                            var diff = (targetRotation - rotationAngle) % 360f
                            if (diff > 180f) diff -= 360f
                            if (diff < -180f) diff += 360f
                            
                            coroutineScope.launch {
                                androidx.compose.animation.core.animate(
                                    initialValue = rotationAngle,
                                    targetValue = rotationAngle + diff,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) { value, _ ->
                                    rotationAngle = value
                                }
                                onNavigateToRashis(sign)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val imageResId = remember(sign) {
                        context.resources.getIdentifier(sign, "drawable", context.packageName)
                    }
                    if (imageResId != 0) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = sign,
                            colorFilter = ColorFilter.tint(tintColor),
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationZ = -(rotationAngle + autoSpin) }
                        )
                    }
                }
            }
        }
        
        Surface(
            modifier = Modifier
                .size(centerIconSize)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = Color.Transparent,
                    spotColor = wheelGold.copy(alpha = 0.55f)
                ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            border = BorderStroke(1.2.dp, wheelGold.copy(alpha = 0.78f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (activeEnglishSign != null) {
                    val centerImageResId = remember(activeEnglishSign) {
                        context.resources.getIdentifier(activeEnglishSign, "drawable", context.packageName)
                    }
                    if (centerImageResId != 0) {
                        Image(
                            painter = painterResource(id = centerImageResId),
                            contentDescription = activeMoonSign,
                            colorFilter = ColorFilter.tint(wheelGold),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = wheelGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GreetingAndChips(
    header: HeaderUiModel,
    chips: List<ChipUiModel>,
    moonSign: String?,
    userEmail: String?,
    accessToken: String?,
    onNavigateToRashis: (String?) -> Unit,
    onNavigateToKundli: () -> Unit,
    onChipClick: () -> Unit
) {
    val responsive = responsiveMetrics()
    val context = LocalContext.current
    val panchangLabel = context.getString(R.string.dashboard_chip_today_panchang)
    val panchangChip = chips.find { it.label == panchangLabel }
    val remainingChips = chips.filter { it.label != panchangLabel }

    if (responsive.isTabletWidth) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = header.metaDate.uppercase(),
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.44f),
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${header.greeting},",
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    ResponsiveNameText(
                        name = header.name,
                        maxFontSize = 24.sp,
                        minFontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                KundliChartTile(
                    userEmail = userEmail,
                    accessToken = accessToken,
                    tileSize = 132.dp,
                    onClick = onNavigateToKundli,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                panchangChip?.let { chip ->
                    Box(modifier = Modifier.weight(1.2f)) {
                        DailyChip(chip = chip, onClick = onChipClick)
                    }
                }
                remainingChips.forEach { chip ->
                    Box(modifier = Modifier.weight(1f)) {
                        DailyChip(chip = chip, onClick = onChipClick)
                    }
                }
            }
        }
    } else {
        val moonChip = remainingChips.find { it.label == context.getString(R.string.dashboard_moon) }
            ?: remainingChips.firstOrNull()
        val sunChip = remainingChips.find { it.label == context.getString(R.string.dashboard_sun) }
            ?: remainingChips.getOrNull(1)

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            val tight = responsive.isVeryCompactWidth || responsive.isLargeFont
            val wheelSize = if (tight) 132.dp else 140.dp
            val textColWidth = if (tight) 200.dp else 230.dp
            val chipRowWidth = if (tight) 200.dp else 220.dp
            val chipHeight = if (tight) 44.dp else 48.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(wheelSize)
            ) {
                KundliChartTile(
                    userEmail = userEmail,
                    accessToken = accessToken,
                    tileSize = wheelSize,
                    onClick = onNavigateToKundli,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 14.dp, y = 4.dp)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .width(textColWidth)
                ) {
                    Text(
                        text = header.metaDate.uppercase(),
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.44f),
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${header.greeting},",
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    ResponsiveNameText(
                        name = header.name,
                        maxFontSize = 24.sp,
                        minFontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(if (tight) 10.dp else 12.dp))

                    if (moonChip != null && sunChip != null) {
                        Row(
                            modifier = Modifier.width(chipRowWidth),
                            horizontalArrangement = Arrangement.spacedBy(if (tight) 8.dp else 10.dp)
                        ) {
                            CompactMoonSunChip(
                                chip = moonChip,
                                icon = Icons.Default.DarkMode,
                                onClick = onChipClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(chipHeight)
                            )
                            CompactMoonSunChip(
                                chip = sunChip,
                                icon = Icons.Default.WbSunny,
                                onClick = onChipClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(chipHeight)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.width(chipRowWidth),
                            horizontalArrangement = Arrangement.spacedBy(if (tight) 8.dp else 10.dp)
                        ) {
                            remainingChips.forEach { chip ->
                                DailyChip(
                                    chip = chip,
                                    onClick = onChipClick,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(chipHeight)
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
private fun DailyChip(
    chip: ChipUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = chip.color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, chip.color.copy(alpha = 0.28f)),
        modifier = modifier.clickable(onClick = onClick)
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
            if (chip.label == context.getString(R.string.dashboard_chip_today_panchang)) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = chip.color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Compact Moon/Sun pill — Material glyph + label + sign name. Replaces zodiac
// drawable variant that caused the value text to truncate ("Ge...").
@Composable
private fun CompactMoonSunChip(
    chip: ChipUiModel,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val responsive = responsiveMetrics()
    val tight = responsive.isVeryCompactWidth || responsive.isLargeFont
    val iconSize = if (tight) 18.dp else 20.dp
    val horizontalPad = if (tight) 8.dp else 10.dp
    val verticalPad = if (tight) 3.dp else 4.dp
    val iconTextGap = if (tight) 6.dp else 8.dp
    val labelFontSize = if (tight) 8.sp else 9.sp
    val valueFontSize = when {
        tight -> 12.sp
        responsive.isCompactWidth -> 13.sp
        else -> 14.sp
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, chip.color.copy(alpha = 0.3f)),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = horizontalPad, vertical = verticalPad),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = chip.color.copy(alpha = 0.88f),
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(iconTextGap))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chip.label.uppercase(),
                    fontSize = labelFontSize,
                    lineHeight = (labelFontSize.value + 2f).sp,
                    fontWeight = FontWeight.Black,
                    color = chip.color.copy(alpha = 0.7f),
                    letterSpacing = 0.6.sp,
                    maxLines = 1
                )
                Text(
                    text = chip.value,
                    fontSize = valueFontSize,
                    lineHeight = (valueFontSize.value + 2f).sp,
                    fontWeight = FontWeight.Black,
                    color = chip.color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─── 2. Today Overview Card (score + mood + dominant influence + timing) ──────

private enum class TimingDialogType { BEST, CAUTION }

@Composable
fun TodayOverviewCard(
    hero: HeroCardUiModel,
    cosmicHour: CosmicHourUiModel,
    themeColor: Color,
    onScoreClick: () -> Unit,
    onTimingClick: () -> Unit
) {
    val responsive = responsiveMetrics()
    val toneColor = themeColor
    val scoreRingSize = if (responsive.isCompactWidth) 80.dp else 96.dp

    // Timing colours + state-aware labels (Good Time / Caution Window)
    val bestColor = Color(0xFF159957)
    val cautionColorUpcoming = Color(0xFFD97706)
    val cautionColorActive = Color(0xFFEF4444)
    val cautionColorEnded = Color(0xFF6B7280)
    var dialogType by remember { mutableStateOf<TimingDialogType?>(null) }

    fun parseToMinutes(timeStr: String?): Int? {
        if (timeStr.isNullOrBlank()) return null
        return try {
            val parts = timeStr.split(":")
            parts[0].trim().toInt() * 60 + parts[1].trim().split(" ")[0].toInt()
        } catch (e: Exception) { null }
    }

    val bestLabelRes = remember(cosmicHour.goodTimeStartRaw, cosmicHour.goodTimeEndRaw) {
        val start = parseToMinutes(cosmicHour.goodTimeStartRaw)
        val end = parseToMinutes(cosmicHour.goodTimeEndRaw)
        val cal = java.util.Calendar.getInstance()
        val nowMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
        when {
            start == null || end == null -> R.string.dashboard_label_best_time
            nowMinutes < start -> R.string.dashboard_label_good_time_upcoming
            nowMinutes < end -> R.string.dashboard_label_good_time_current
            else -> R.string.dashboard_label_good_time
        }
    }
    val cautionLabelRes = remember(cosmicHour.rahukaalStartRaw, cosmicHour.rahukaalEndRaw) {
        val start = parseToMinutes(cosmicHour.rahukaalStartRaw)
        val end = parseToMinutes(cosmicHour.rahukaalEndRaw)
        val cal = java.util.Calendar.getInstance()
        val nowMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
        when {
            start == null || end == null -> R.string.dashboard_label_caution_window
            nowMinutes < start -> R.string.dashboard_label_rahu_kaal_upcoming
            nowMinutes < end -> R.string.dashboard_label_rahu_kaal_active
            else -> R.string.dashboard_label_rahu_kaal_ended
        }
    }
    val cautionAccent = when (cautionLabelRes) {
        R.string.dashboard_label_rahu_kaal_active -> cautionColorActive
        R.string.dashboard_label_rahu_kaal_ended -> cautionColorEnded
        else -> cautionColorUpcoming
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .floatingCard(accent = toneColor, cornerRadius = 22.dp, elevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(responsive.cardPadding / 2)) {
            Text(
                text = stringResource(R.string.dashboard_header_today_overview),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Top: score arc-ring + mood + subtext + dominant influence (inline planet badge) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onScoreClick),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(scoreRingSize)
                        .background(toneColor.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                        .padding(7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val stroke = 5.dp.toPx()
                        val inset = stroke / 2f
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        val topLeft = Offset(inset, inset)
                        // track
                        drawArc(
                            color = toneColor.copy(alpha = 0.15f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        // progress
                        val sweep = (hero.overallScore.coerceIn(0, 100) / 100f) * 360f
                        drawArc(
                            color = toneColor,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${hero.overallScore}",
                            fontSize = if (responsive.isCompactWidth) 26.sp else 32.sp,
                            lineHeight = if (responsive.isCompactWidth) 28.sp else 34.sp,
                            fontWeight = FontWeight.Black,
                            color = toneColor
                        )
                        Text(
                            text = stringResource(R.string.dashboard_label_your_day),
                            fontSize = 7.sp,
                            lineHeight = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = toneColor.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.dashboard_mood_day, titleCase(hero.moodValue)),
                        fontSize = if (responsive.isCompactWidth) 18.sp else 20.sp,
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
                    if (hero.dominantPlanet.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.dashboard_label_dominant_influence_inline),
                                fontSize = 13.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = hero.dominantPlanet,
                                fontSize = 14.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = hero.dominantPlanetColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        hero.dominantPlanetColor.copy(alpha = 0.12f),
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        hero.dominantPlanetColor.copy(alpha = 0.35f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                HeroPlanetVisual(
                                    planet = hero.dominantPlanet,
                                    planetColor = hero.dominantPlanetColor,
                                    size = 22.dp
                                )
                            }
                        }
                    }
                }
            }

            // ── Divider ──
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f))
            Spacer(modifier = Modifier.height(14.dp))

            // ── Bottom: Good Time + Caution Window ──
            if (responsive.isMediumWidth) {
                // Tablet/Fold: Vertical stack with more spacing
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimingInline(
                        label = stringResource(bestLabelRes),
                        time = cosmicHour.goodTime,
                        icon = Icons.Default.Star,
                        accent = bestColor,
                        onClick = { dialogType = TimingDialogType.BEST },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TimingInline(
                        label = stringResource(cautionLabelRes),
                        time = cosmicHour.rahukaal,
                        icon = Icons.Default.Warning,
                        accent = cautionAccent,
                        onClick = { dialogType = TimingDialogType.CAUTION },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // Mobile: Row layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimingInline(
                        label = stringResource(bestLabelRes),
                        time = cosmicHour.goodTime,
                        icon = Icons.Default.Star,
                        accent = bestColor,
                        onClick = { dialogType = TimingDialogType.BEST },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    TimingInline(
                        label = stringResource(cautionLabelRes),
                        time = cosmicHour.rahukaal,
                        icon = Icons.Default.Warning,
                        accent = cautionAccent,
                        onClick = { dialogType = TimingDialogType.CAUTION },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    dialogType?.let { type ->
        val isCaution = type == TimingDialogType.CAUTION
        val accent = if (isCaution) cautionAccent else bestColor
        val icon = if (isCaution) Icons.Default.Warning else Icons.Default.Star
        val title = if (isCaution) stringResource(cautionLabelRes) else stringResource(bestLabelRes)
        val time = if (isCaution) cosmicHour.rahukaal else cosmicHour.goodTime
        val advice = if (isCaution) cosmicHour.rahukaalAdvice else cosmicHour.goodTimeAdvice

        androidx.compose.ui.window.Dialog(onDismissRequest = { dialogType = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .floatingCard(accent = accent, cornerRadius = 20.dp, elevation = 8.dp)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(accent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
                }
                Text(text = title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(text = time, fontSize = 20.sp, fontWeight = FontWeight.Black,
                    color = accent, textAlign = TextAlign.Center)
                if (advice.isNotBlank()) {
                    Text(text = advice, fontSize = 14.sp, textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { dialogType = null; onTimingClick() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                ) {
                    Text(stringResource(R.string.dashboard_label_view_full_schedule), color = Color.White)
                }
                TextButton(onClick = { dialogType = null }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.dashboard_action_dismiss),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
    val isPlaceholder = key.isBlank()
    val scale = if (key == "rahu" || key == "ketu") 0.78f else 0.95f
    Box(
        modifier = Modifier
            .size(size * scale),
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
                modifier = Modifier.fillMaxSize(),
                fallbackTint = planetColor
            )
        }
    }
}

// ─── 2b. Inline timing item (used inside TodayOverviewCard) ──────────────────

@Composable
private fun TimingInline(
    label: String,
    time: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.06f))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(accent.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Black,
                color = accent.copy(alpha = 0.85f),
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = time,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─── 3. Life Areas Carousel ───────────────────────────────────────────────────

@Composable
fun LifeAreasCarousel(
    areas: List<LifeAreaCardUiModel>,
    onAreaClick: (LifeAreaCardUiModel) -> Unit,
    scrollHintKey: Int = 0
) {
    if (areas.isEmpty()) return

    val displayOrder = listOf("finance", "love", "general", "career", "health")
    val ordered = displayOrder
        .mapNotNull { id -> areas.firstOrNull { it.id == id } }
        .let { primary -> primary + areas.filterNot { it.id in displayOrder } }
        .ifEmpty { areas }

    val startIndex = ordered.indexOfFirst { it.id == "general" }.takeIf { it >= 0 } ?: 0
    val pagerState = rememberPagerState(initialPage = startIndex) { ordered.size }
    val scope = rememberCoroutineScope()

    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(1)
    )

    LaunchedEffect(scrollHintKey) {
        if (scrollHintKey == 0) return@LaunchedEffect
        val next = (pagerState.currentPage + 1).coerceAtMost(ordered.size - 1)
        pagerState.animateScrollToPage(next)
        pagerState.animateScrollToPage(pagerState.currentPage - 1)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Full-bleed, symmetric by construction: force the carousel to the real device
        // screen width and center it, regardless of whatever horizontal padding the
        // parent column applies. This guarantees identical left/right edges instead of
        // guessing the gutter (which drifts and clips one side).
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        BoxWithConstraints(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val target = screenWidth.roundToPx()
                    val placeable = measurable.measure(
                        constraints.copy(minWidth = target, maxWidth = target)
                    )
                    // Center the over-wide content on the (narrower) parent slot so the
                    // overflow is split evenly between both screen edges.
                    val dx = (constraints.maxWidth - target) / 2
                    layout(constraints.maxWidth, placeable.height) {
                        placeable.place(dx, 0)
                    }
                }
        ) {
            val cardWidth = 132.dp
            val horizontalPadding = (maxWidth - cardWidth) / 2

            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fixed(cardWidth),
                pageSpacing = 9.dp,
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                flingBehavior = flingBehavior,
                beyondViewportPageCount = 2,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                // Central card: full size, fully opaque. Side cards: 10% smaller, 20% transparent.
                val scale = lerp(0.90f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                val alpha = lerp(0.80f, 1f, 1f - pageOffset.coerceIn(0f, 1f))

                LifeAreaCard(
                    area = ordered[page],
                    modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha },
                    onClick = { onAreaClick(ordered[page]) }
                )
            }
        }

        // dot indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ordered.forEachIndexed { index, _ ->
                val isActive = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isActive) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
                        .clickable { scope.launch { pagerState.animateScrollToPage(index) } }
                )
            }
        }
    }
}

// ─── Ask AI Astrologers (dashboard teaser: first 3 guides + View All) ──────────

@Composable
fun AskAstrologersRow(
    astrologers: List<ChatAvatar>,
    onAstrologerClick: (ChatAvatar) -> Unit
) {
    if (astrologers.isEmpty()) return
    // Show up to 3 guides side-by-side, sharing the row width equally.
    val shown = astrologers.take(3)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        shown.forEach { avatar ->
            AskAstrologerCard(
                avatar = avatar,
                modifier = Modifier.weight(1f),
                onClick = { onAstrologerClick(avatar) }
            )
        }
    }
}

@Composable
private fun AskAstrologerCard(
    avatar: ChatAvatar,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val responsive = responsiveMetrics()
    val fonts = responsiveFontSizes()
    val accent = chatAvatarPalette(avatar.accentColor).accent
    val compact = responsive.isVeryCompactWidth || responsive.isCompactWidth || responsive.isLargeFont
    val avatarSize = when {
        responsive.isVeryCompactWidth || responsive.isLargeFont -> 44.dp
        responsive.isCompactWidth -> 48.dp
        responsive.isMediumWidth -> 64.dp
        else -> 52.dp
    }
    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.10f)),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChatAvatarImage(
                    avatar = avatar,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .border(2.dp, accent.copy(alpha = 0.55f), CircleShape)
                )
                Text(
                    text = avatar.name,
                    fontSize = fonts.cardBody,
                    fontWeight = FontWeight.Black,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = avatar.title,
                    fontSize = fonts.microLabel,
                    fontWeight = FontWeight.Medium,
                    lineHeight = fonts.microLabel * 1.25f,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.height(fonts.microLabel.value.dp * 2.5f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accent.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClick)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 7.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_astrologer_ask_now),
                            fontSize = if (compact) fonts.badgeText else fonts.buttonLabel,
                            fontWeight = FontWeight.Black,
                            color = accent,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = accent
                        )
                    }
                }
            }
        }
        // Credit cost as a small badge pinned to the card's top-end corner.
        Surface(
            shape = RoundedCornerShape(bottomStart = 12.dp, topEnd = 18.dp),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = LocaleFormatter.number(avatar.creditCost, currentAppLocale()),
                    fontSize = fonts.microLabel,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}


@Composable
private fun LifeAreaCard(
    area: LifeAreaCardUiModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val scorePalette = remember(area.id, area.score, isDarkTheme) {
        ScoreColors.paletteFor(area.id, area.score, isDarkTheme)
    }
    val scoreColor = if (isDarkTheme) scorePalette.glow else scorePalette.main

    Box(
        modifier = modifier
            .width(132.dp)
            .floatingCard(accent = scorePalette.glow, cornerRadius = 16.dp, elevation = 4.dp)
            .clickable(onClick = onClick)
            .drawBehind {
                val glowRadius = size.minDimension * 0.85f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(scorePalette.glow.copy(alpha = if (isDarkTheme) 0.16f else 0.10f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(scoreColor.copy(alpha = 0.32f), Color.Transparent)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = area.icon, fontSize = 22.sp)
            }
            Text(
                text = area.label,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = LocaleFormatter.number(area.score, currentAppLocale()),
                fontSize = 24.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Black,
                color = scoreColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = area.status,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Black,
                color = scoreColor.copy(alpha = 0.9f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
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
    val rahuKaalLabelRes = remember(cosmic.rahukaalStartRaw, cosmic.rahukaalEndRaw) {
        val start = parseToMinutesStatic(cosmic.rahukaalStartRaw)
        val end = parseToMinutesStatic(cosmic.rahukaalEndRaw)
        val cal = java.util.Calendar.getInstance()
        val nowMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
        when {
            start == null || end == null -> R.string.dashboard_label_caution_window
            nowMinutes < start -> R.string.dashboard_label_rahu_kaal_upcoming
            nowMinutes < end -> R.string.dashboard_label_rahu_kaal_active
            else -> R.string.dashboard_label_rahu_kaal_ended
        }
    }

    val rahuAccent = when (rahuKaalLabelRes) {
        R.string.dashboard_label_rahu_kaal_active -> Color(0xFFEF4444)
        R.string.dashboard_label_rahu_kaal_ended -> Color(0xFF6B7280)
        else -> AstroColors.Rahu
    }

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
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GOOD HORA",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = AstroColors.Jupiter,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = cosmic.goodTime,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(rahuKaalLabelRes),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = rahuAccent,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = cosmic.rahukaal,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rahuKaalLabelRes == R.string.dashboard_label_rahu_kaal_ended)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
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
    panchanga: PanchangaUiModel,
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
            label = stringResource(R.string.dashboard_label_general_outlook),
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

        InfoCard(
            title = "LUCKY INFLUENCES",
            accent = AstroColors.Jupiter,
            leadingIcon = Icons.Default.Star
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LUCKY COLOR",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val displayColor = when(panchanga.luckyColor.lowercase()) {
                                "turquoise" -> Color(0xFF40E0D0)
                                "red" -> AstroColors.Mars
                                "green" -> AstroColors.Mercury
                                "yellow" -> AstroColors.Jupiter
                                "pink" -> AstroColors.Venus
                                "blue" -> AstroColors.Saturn
                                "orange" -> AstroColors.Sun
                                "white" -> Color.White
                                else -> AstroColors.Default
                            }
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(displayColor, CircleShape)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            )
                            Text(
                                text = panchanga.luckyColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LUCKY NUMBER",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${panchanga.luckyNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = AstroColors.Jupiter
                        )
                    }
                }
            }
        }
        
        InfoCard(
            title = "COSMIC TRANSIT CONTEXT",
            accent = hero.dominantPlanetColor,
            leadingIcon = Icons.Default.AutoAwesome
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (planetName.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dominant Planet",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = planetName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = hero.dominantPlanetColor
                        )
                    }
                }
                if (activeTransit.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Current Period (Dasha)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = activeTransit,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End
                        )
                    }
                }
                if (panchanga.retrogradePlanets.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Retrograde Planets",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = panchanga.retrogradePlanets.joinToString(", "),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AstroColors.Mars
                        )
                    }
                }
            }
        }
        if (alert.secondaryAlerts.isNotEmpty()) {
            InfoCard(
                title = "PLANETARY ALERTS",
                accent = AstroColors.Mars,
                leadingIcon = Icons.Default.Info
            ) {
                BulletList(items = alert.secondaryAlerts, accent = AstroColors.Mars)
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
        if (technical.isNotBlank() && technical != whatsHappening) {
            InfoCard(
                title = "TECHNICAL ASTRO DETAILS",
                accent = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                leadingIcon = Icons.Default.Psychology
            ) {
                Text(
                    text = technical,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
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
    val fullInsightRaw = cleanAreaLabels(area.fullInsight.ifBlank { area.shortInsight })
    val oneLine = cleanAreaLabels(area.shortInsight.ifBlank { fullInsightRaw })
    val oneLineCleaned = oneLine.trim().removeSuffix(".")
    val fullInsight = if (oneLineCleaned.isNotEmpty() && fullInsightRaw.trim().startsWith(oneLineCleaned, ignoreCase = true)) {
        fullInsightRaw.trim().substring(oneLineCleaned.length).trim().removePrefix(".").trim()
    } else {
        fullInsightRaw
    }
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
        // 1. Auspicious Hora (Good Time)
        InfoCard(
            title = if (cosmicHour.goodTimeLabel.isNotEmpty()) {
                "Auspicious Hora: ${cosmicHour.goodTimeLabel}"
            } else {
                "Auspicious Hora"
            },
            accent = AstroColors.Jupiter,
            leadingIcon = Icons.Default.Star
        ) {
            Text(
                text = cosmicHour.goodTime,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AstroColors.Jupiter
            )
            if (cosmicHour.goodTimeAdvice.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cosmicHour.goodTimeAdvice,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                )
            }
        }

        // 2. Rahu Kaal (Bad Time)
        val rahuStateLabel = remember(cosmicHour.rahukaalStartRaw, cosmicHour.rahukaalEndRaw) {
            val start = parseToMinutesStatic(cosmicHour.rahukaalStartRaw)
            val end = parseToMinutesStatic(cosmicHour.rahukaalEndRaw)
            val cal = java.util.Calendar.getInstance()
            val nowMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
            when {
                start == null || end == null -> "Rahu Kaal (Inauspicious Period)"
                nowMinutes < start -> "Rahu Kaal — Upcoming"
                nowMinutes < end -> "Rahu Kaal — Active Now"
                else -> "Rahu Kaal — Ended"
            }
        }
        val rahuAccent = remember(cosmicHour.rahukaalStartRaw, cosmicHour.rahukaalEndRaw) {
            val start = parseToMinutesStatic(cosmicHour.rahukaalStartRaw)
            val end = parseToMinutesStatic(cosmicHour.rahukaalEndRaw)
            val cal = java.util.Calendar.getInstance()
            val nowMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
            when {
                start == null || end == null -> AstroColors.Rahu
                nowMinutes < start -> Color(0xFFD97706)
                nowMinutes < end -> Color(0xFFEF4444)
                else -> Color(0xFF6B7280)
            }
        }
        InfoCard(
            title = rahuStateLabel,
            accent = rahuAccent,
            leadingIcon = Icons.Default.Warning
        ) {
            Text(
                text = cosmicHour.rahukaal,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = rahuAccent
            )
            if (cosmicHour.rahukaalAdvice.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cosmicHour.rahukaalAdvice,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Section Label for the other triggers
        Text(
            text = "Hourly Cosmic Triggers",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(vertical = 4.dp)
        )

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
    "rahu" -> "rahu"
    "ketu" -> "ketu"
    else -> null
}
