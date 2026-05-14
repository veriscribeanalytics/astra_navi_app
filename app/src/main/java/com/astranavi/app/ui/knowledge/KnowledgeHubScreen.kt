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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KnowledgeHubScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToRashis: () -> Unit,
    onNavigateToPlanets: () -> Unit,
    onNavigateToNakshatras: () -> Unit,
    onNavigateToHouses: () -> Unit,
    onNavigateToYogas: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Vedic Wisdom", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text("Explore the ancient science of light through our structured encyclopedia.", color = Color.Gray)
        
        Spacer(modifier = Modifier.height(16.dp))

        KnowledgeCategoryCard("Zodiac Rashis", "The 12 signs of the zodiac.", Icons.Default.PlayArrow, Color(0xFF60A5FA), onNavigateToRashis)
        KnowledgeCategoryCard("The Navagraha", "Planetary forces and their impact.", Icons.Default.BrightnessLow, Color(0xFFF59E0B), onNavigateToPlanets)
        KnowledgeCategoryCard("27 Nakshatras", "Deep-dive into lunar mansions.", Icons.Default.Stars, Color(0xFFA78BFA), onNavigateToNakshatras)
        KnowledgeCategoryCard("Bhava Chakra", "The 12 houses of your life.", Icons.Default.ViewQuilt, Color(0xFF10B981), onNavigateToHouses)
        KnowledgeCategoryCard("Cosmic Yogas", "Sacred planetary combinations.", Icons.Default.AutoAwesome, Color(0xFFF472B6), onNavigateToYogas)
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun KnowledgeCategoryCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    subtitle, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color.Gray, 
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
