package com.astranavi.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.R
import com.astranavi.app.ui.theme.AstroColors
import com.astranavi.app.util.LocaleFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun DashaTimelineItem(
    title: String,
    planetName: String,
    startDate: String?,
    endDate: String?,
    interpretation: String,
    isPrimary: Boolean = false,
    modifier: Modifier = Modifier
) {
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
    var displayStartDate by remember { mutableStateOf(startDate ?: "") }
    var displayEndDate by remember { mutableStateOf(endDate ?: "") }

    LaunchedEffect(startDate, endDate) {
        try {
            if (startDate != null && endDate != null) {
                val locale = Locale.getDefault()
                val start = LocalDate.parse(startDate.take(10), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val end = LocalDate.parse(endDate.take(10), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
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
            }
        } catch (e: Exception) {
            progress = 0.5f
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isPrimary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = planetRes),
                        contentDescription = planetName,
                        modifier = Modifier.size(36.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    if (planetName.lowercase() == "rahu" || planetName.lowercase() == "ketu") {
                        Box(modifier = Modifier.size(36.dp).background(AstroColors.getPlanetaryColor(planetName).copy(alpha = 0.4f), CircleShape))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, color = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(displayStartDate, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text(daysLeftText, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                Text(displayEndDate, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                Box(modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(AstroColors.getPlanetaryColor(planetName), CircleShape)
                    .shimmerSweepEffect()
                )
            }

            if (interpretation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(interpretation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun DashaCircularItem(
    title: String,
    planetName: String,
    startDate: String?,
    endDate: String?,
    isPrimary: Boolean = false,
    modifier: Modifier = Modifier
) {
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

    LaunchedEffect(startDate, endDate) {
        try {
            if (startDate != null && endDate != null) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val start = LocalDate.parse(startDate.take(10), formatter)
                val end = LocalDate.parse(endDate.take(10), formatter)
                val today = LocalDate.now()
                val totalDays = ChronoUnit.DAYS.between(start, end)
                val daysPassed = ChronoUnit.DAYS.between(start, today)
                progress = if (totalDays > 0) (daysPassed.toFloat() / totalDays).coerceIn(0f, 1f) else 0f
            }
        } catch (_: Exception) {
            progress = 0f
        }
    }

    val planetColor = AstroColors.getPlanetaryColor(planetName)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "dasha_circular_progress")
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isPrimary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp)) {
                Canvas(modifier = Modifier.size(54.dp)) {
                    val strokeWidth = 4.dp.toPx()
                    val inset = strokeWidth / 2f
                    val arcSize = androidx.compose.ui.geometry.Size(
                        size.width - strokeWidth,
                        size.height - strokeWidth
                    )
                    val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = planetColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Image(
                    painter = painterResource(id = planetRes),
                    contentDescription = planetName,
                    modifier = Modifier.size(34.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                if (planetName.lowercase() == "rahu" || planetName.lowercase() == "ketu") {
                    Box(modifier = Modifier.size(34.dp).background(planetColor.copy(alpha = 0.4f), CircleShape))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
