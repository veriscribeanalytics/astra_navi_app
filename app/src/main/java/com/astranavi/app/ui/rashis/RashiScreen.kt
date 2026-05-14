package com.astranavi.app.ui.rashis

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.astranavi.app.LocalTopBarColor
import com.astranavi.app.LocalTopBarTitle
import com.astranavi.app.data.model.Rashi
import com.astranavi.app.ui.components.AtmosphericGlowLayer
import com.astranavi.app.ui.components.ParticleBackground
import com.astranavi.app.ui.components.getIconResId
import com.astranavi.app.ui.theme.AstroColors

@Composable
fun RashiScreen(viewModel: RashiViewModel, onBack: () -> Unit = {}) {
    val viewMode by viewModel.viewMode
    val setTitle = LocalTopBarTitle.current
    val setTopBarColor = LocalTopBarColor.current
    val topBarColor = MaterialTheme.colorScheme.onBackground

    LaunchedEffect(viewMode, topBarColor) {
        when (val mode = viewMode) {
            is RashiViewMode.Encyclopedia -> {
                setTitle?.invoke("Rashis")
                setTopBarColor?.invoke(null)
            }
            is RashiViewMode.Detail -> {
                setTitle?.invoke(mode.rashi.nameEn)
                setTopBarColor?.invoke(topBarColor)
            }
        }
    }

    BackHandler(enabled = true) {
        if (viewMode is RashiViewMode.Detail) viewModel.backToEncyclopedia() else onBack()
    }

    Scaffold(containerColor = Color.Transparent, contentWindowInsets = WindowInsets(0.dp)) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.Transparent)) {
            ParticleBackground()
            when (viewMode) {
                is RashiViewMode.Encyclopedia -> RashiGrid(viewModel.allRashis) { viewModel.selectRashi(it) }
                is RashiViewMode.Detail -> RashiDetail(rashi = (viewMode as RashiViewMode.Detail).rashi)
            }
        }
    }
}

@Composable
fun RashiGrid(rashis: List<Rashi>, onSelect: (Rashi) -> Unit) {
    val context = LocalContext.current
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 24.dp, top = 96.dp, end = 24.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(rashis) { rashi ->
            val accentColor = AstroColors.getPlanetaryColor(rashi.rulingPlanet)
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(rashi) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val iconRes = getIconResId(rashi.nameEn)
                    Box(modifier = Modifier.size(64.dp).background(accentColor, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        if (iconRes != 0) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(iconRes).crossfade(true).build(),
                                contentDescription = rashi.nameEn,
                                modifier = Modifier.size(40.dp)
                            )
                        } else {
                            Text(rashi.nameHi.take(1), fontWeight = FontWeight.Black, color = Color.White, fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(rashi.nameEn, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                    Text(rashi.rulingPlanet, style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun RashiDetail(rashi: Rashi) {
    val accentColor = AstroColors.getPlanetaryColor(rashi.rulingPlanet)
    
    Box(modifier = Modifier.fillMaxSize()) {
        AtmosphericGlowLayer(accentColor = accentColor, modifier = Modifier.align(Alignment.TopCenter))

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 64.dp)) {
            Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(colors = listOf(accentColor.copy(alpha = 0.2f), Color.Transparent))).padding(top = 112.dp, bottom = 32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val iconRes = getIconResId(rashi.nameEn)
                    if (iconRes != 0) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(iconRes).crossfade(true).build(),
                            contentDescription = rashi.nameEn,
                            modifier = Modifier.size(120.dp)
                        )
                    } else {
                        Text(text = rashi.nameHi, fontSize = 56.sp, fontWeight = FontWeight.Black, color = accentColor)
                    }
                    Text(text = rashi.nameEn.uppercase(), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 6.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    FlowRow(horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 24.dp)) {
                        InfoChip(rashi.rulingPlanet, accentColor)
                        InfoChip(rashi.element, Color.Gray)
                        InfoChip(rashi.guna, MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
                SectionHeader("Core Nature", accentColor)
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.1f))) {
                    Text(text = rashi.coreNature, modifier = Modifier.padding(24.dp), style = MaterialTheme.typography.bodyLarge, lineHeight = 30.sp)
                }

                SectionHeader("Identity Grid", accentColor)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IdentityTile("Ruler", rashi.rulingPlanet, Modifier.weight(1f))
                        IdentityTile("Element", rashi.element, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IdentityTile("Quality", rashi.quality, Modifier.weight(1f))
                        IdentityTile("Caste", rashi.caste, Modifier.weight(1f))
                    }
                }

                TraitSection("Positive Traits", rashi.positiveTraits, Color(0xFF4CAF50))
                TraitSection("Challenging Traits", rashi.challengingTraits, Color(0xFFF44336))
                
                SectionHeader("Career Resonance", accentColor)
                rashi.careers.forEach { career ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(accentColor, CircleShape))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(career, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun InfoChip(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.1f), shape = CircleShape, border = BorderStroke(1.dp, color.copy(alpha = 0.4f))) {
        Text(text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
fun IdentityTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(width = 6.dp, height = 28.dp).background(color, RoundedCornerShape(3.dp)))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
    }
}

@Composable
fun TraitSection(label: String, traits: List<String>, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = color)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            traits.forEach { trait ->
                Surface(color = color.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.2f))) {
                    Text(trait, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
