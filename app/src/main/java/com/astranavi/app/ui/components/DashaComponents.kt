package com.astranavi.app.ui.components

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astranavi.app.R
import com.astranavi.app.ui.theme.AstroColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                
                val start = LocalDate.parse(startDate.take(10), formatter)
                val end = LocalDate.parse(endDate.take(10), formatter)
                val today = LocalDate.now()
                
                displayStartDate = start.format(displayFormatter)
                displayEndDate = end.format(displayFormatter)
                
                val totalDays = ChronoUnit.DAYS.between(start, end)
                val daysPassed = ChronoUnit.DAYS.between(start, today)
                val left = ChronoUnit.DAYS.between(today, end)
                
                progress = if (totalDays > 0) (daysPassed.toFloat() / totalDays).coerceIn(0f, 1f) else 0f
                daysLeftText = if (left > 0) "$left days left (${(progress * 100).toInt()}%)" else "Completed"
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
                Text(displayStartDate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(daysLeftText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(displayEndDate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
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
