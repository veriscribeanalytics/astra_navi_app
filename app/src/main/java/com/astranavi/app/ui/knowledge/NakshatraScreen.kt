package com.astranavi.app.ui.knowledge

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.LocalTopBarTitle
import com.astranavi.app.LocalTopBarColor
import com.astranavi.app.data.model.Nakshatra
import com.astranavi.app.data.repository.NakshatraData
import com.astranavi.app.ui.theme.AstroColors
import com.astranavi.app.ui.components.*

@Composable
fun NakshatraScreen(onBack: () -> Unit) {
    var selectedNak by remember { mutableStateOf<Nakshatra?>(null) }
    val nakshatras = NakshatraData.nakshatras
    val setTitle = LocalTopBarTitle.current
    val setTopBarColor = LocalTopBarColor.current

    LaunchedEffect(selectedNak) {
        setTitle?.invoke(selectedNak?.nameEn ?: "Nakshatras")
        val color = selectedNak?.let { AstroColors.getPlanetaryColor(it.ruler) }
        setTopBarColor?.invoke(color)
    }

    BackHandler(enabled = true) {
        if (selectedNak != null) selectedNak = null else onBack()
    }

    Scaffold(containerColor = Color.Transparent, contentWindowInsets = WindowInsets(0.dp)) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.Transparent)) {
            ParticleBackground()
            if (selectedNak == null) {
                NakshatraList(nakshatras) { selectedNak = it }
            } else {
                NakshatraDetail(selectedNak!!)
            }
        }
    }
}

@Composable
fun NakshatraList(nakshatras: List<Nakshatra>, onSelect: (Nakshatra) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(nakshatras) { nakshatra ->
            val accentColor = AstroColors.getPlanetaryColor(nakshatra.ruler)
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(nakshatra) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(accentColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text(nakshatra.nameEn.take(1), fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(nakshatra.nameEn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(nakshatra.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = accentColor)
                }
            }
        }
    }
}

@Composable
fun NakshatraDetail(nakshatra: Nakshatra) {
    val accentColor = AstroColors.getPlanetaryColor(nakshatra.ruler)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 64.dp)) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(colors = listOf(accentColor.copy(alpha = 0.15f), Color.Transparent))).padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = nakshatra.nameHi, fontSize = 56.sp, fontWeight = FontWeight.Black, color = accentColor, textAlign = TextAlign.Center, lineHeight = 64.sp)
                Text(text = nakshatra.nameEn.uppercase(), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 6.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = nakshatra.summary, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                FlowRow(horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    InfoChip(nakshatra.ruler, accentColor)
                    InfoChip(nakshatra.span, Color.Gray)
                    InfoChip(nakshatra.purushartha, MaterialTheme.colorScheme.primary)
                    InfoChip(nakshatra.gana, MaterialTheme.colorScheme.tertiary)
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
            SectionHeader("Core Nature", accentColor)
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.1f))) {
                Text(text = nakshatra.coreNature, modifier = Modifier.padding(24.dp), style = MaterialTheme.typography.bodyLarge, lineHeight = 30.sp, fontWeight = FontWeight.Medium)
            }

            SectionHeader("The Four Padas", accentColor)
            val pagerState = rememberPagerState(pageCount = { 4 })
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(220.dp), pageSpacing = 16.dp) { page ->
                val pada = nakshatra.padas.getOrNull(page)
                if (pada != null) {
                    Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.05f)), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))) {
                        Column(modifier = Modifier.padding(28.dp)) {
                            Text("PADA ${pada.number}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = accentColor, letterSpacing = 2.sp)
                            Text(pada.navamsha + " Navamsha", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                            Text(pada.essence, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(pada.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}
