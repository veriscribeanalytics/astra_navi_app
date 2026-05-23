package com.astranavi.app.ui.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.R
import com.astranavi.app.ui.components.responsiveMetrics

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KnowledgeHubScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToRashis: () -> Unit,
    onNavigateToPlanets: () -> Unit,
    onNavigateToNakshatras: () -> Unit,
    onNavigateToHouses: () -> Unit,
    onNavigateToYogas: () -> Unit
) {
    val metrics = responsiveMetrics()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(if (metrics.isCompactHeight) 12.dp else 16.dp)
    ) {
        Text(
            text = stringResource(R.string.knowledge_hub_intro),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(metrics.twoPaneGap),
            verticalArrangement = Arrangement.spacedBy(metrics.twoPaneGap)
        ) {
            Box(modifier = Modifier.weight(1f).widthIn(min = 280.dp)) { KnowledgeCategoryCard(stringResource(R.string.knowledge_hub_rashis), stringResource(R.string.knowledge_hub_rashis_desc), Icons.Default.PlayArrow, Color(0xFF60A5FA), onNavigateToRashis) }
            Box(modifier = Modifier.weight(1f).widthIn(min = 280.dp)) { KnowledgeCategoryCard(stringResource(R.string.knowledge_hub_planets), stringResource(R.string.knowledge_hub_planets_desc), Icons.Default.BrightnessLow, Color(0xFFF59E0B), onNavigateToPlanets) }
            Box(modifier = Modifier.weight(1f).widthIn(min = 280.dp)) { KnowledgeCategoryCard(stringResource(R.string.knowledge_hub_nakshatras), stringResource(R.string.knowledge_hub_nakshatras_desc), Icons.Default.Stars, Color(0xFFA78BFA), onNavigateToNakshatras) }
            Box(modifier = Modifier.weight(1f).widthIn(min = 280.dp)) { KnowledgeCategoryCard(stringResource(R.string.knowledge_hub_houses), stringResource(R.string.knowledge_hub_houses_desc), Icons.AutoMirrored.Filled.ViewQuilt, Color(0xFF10B981), onNavigateToHouses) }
            Box(modifier = Modifier.weight(1f).widthIn(min = 280.dp)) { KnowledgeCategoryCard(stringResource(R.string.knowledge_hub_yogas), stringResource(R.string.knowledge_hub_yogas_desc), Icons.Default.AutoAwesome, Color(0xFFF472B6), onNavigateToYogas) }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun KnowledgeCategoryCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    val metrics = responsiveMetrics()

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(if (metrics.isCompactWidth || metrics.isLargeFont) 24.dp else 32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(metrics.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(if (metrics.isCompactWidth || metrics.isLargeFont) 48.dp else 56.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(if (metrics.isCompactWidth || metrics.isLargeFont) 24.dp else 28.dp))
            }
            Spacer(modifier = Modifier.width(if (metrics.isCompactWidth || metrics.isLargeFont) 14.dp else 20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    subtitle, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), 
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
