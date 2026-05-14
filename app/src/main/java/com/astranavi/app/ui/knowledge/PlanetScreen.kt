package com.astranavi.app.ui.knowledge

import android.graphics.Color as AndroidColor
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.astranavi.app.LocalTopBarColor
import com.astranavi.app.LocalTopBarTitle
import com.astranavi.app.data.model.Planet
import com.astranavi.app.data.repository.PlanetData
import com.astranavi.app.ui.components.*
import com.astranavi.app.ui.theme.AstroColors

@Composable
fun PlanetScreen(onBack: () -> Unit = {}) {
    var selectedPlanet by remember { mutableStateOf<Planet?>(null) }
    val planets = PlanetData.planets
    val setTitle = LocalTopBarTitle.current
    val setTopBarColor = LocalTopBarColor.current
    val topBarColor = MaterialTheme.colorScheme.onBackground

    LaunchedEffect(selectedPlanet, topBarColor) {
        setTitle?.invoke(selectedPlanet?.nameEn ?: "Planets")
        setTopBarColor?.invoke(if (selectedPlanet == null) null else topBarColor)
    }

    BackHandler(enabled = true) {
        if (selectedPlanet != null) selectedPlanet = null else onBack()
    }

    Scaffold(containerColor = Color.Transparent, contentWindowInsets = WindowInsets(0.dp)) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.Transparent)) {
            ParticleBackground()
            if (selectedPlanet == null) {
                PlanetList(planets) { selectedPlanet = it }
            } else {
                PlanetDetail(selectedPlanet!!)
            }
        }
    }
}

@Composable
fun PlanetList(planets: List<Planet>, onSelect: (Planet) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(start = 24.dp, top = 96.dp, end = 24.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(planets) { planet ->
            val accentColor = Color(planet.colorHex)
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(planet) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    val iconRes = getIconResId(planet.nameEn)
                    Box(modifier = Modifier.size(48.dp).background(accentColor, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        if (iconRes != 0) {
                            AsyncImage(
                                model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current).data(iconRes).crossfade(true).build(),
                                contentDescription = planet.nameEn,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Text(planet.nameEn.take(1), fontWeight = FontWeight.Black, color = Color.White, fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(planet.nameEn, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        Text(planet.nature, style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.Black)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = accentColor)
                }
            }
        }
    }
}

@Composable
fun PlanetDetail(planet: Planet) {
    val accentColor = Color(planet.colorHex)
    
    Box(modifier = Modifier.fillMaxSize()) {
        AtmosphericGlowLayer(accentColor = accentColor, modifier = Modifier.align(Alignment.TopCenter))

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 64.dp)) {
            Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(colors = listOf(accentColor.copy(alpha = 0.2f), Color.Transparent))).padding(top = 112.dp, bottom = 32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val iconRes = getIconResId(planet.nameEn)
                    if (iconRes != 0) {
                        AsyncImage(
                            model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current).data(iconRes).crossfade(true).build(),
                            contentDescription = planet.nameEn,
                            modifier = Modifier.size(120.dp)
                        )
                    } else {
                        Text(text = planet.nameHi, fontSize = 56.sp, fontWeight = FontWeight.Black, color = accentColor)
                    }
                    Text(text = planet.nameEn.uppercase(), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 6.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    FlowRow(horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 24.dp)) {
                        InfoChip(planet.nature, accentColor)
                        InfoChip(planet.element, Color.Gray)
                        InfoChip(planet.guna, MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
                SectionHeader("Core Nature", accentColor)
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.1f))) {
                    Text(text = planet.coreNature, modifier = Modifier.padding(24.dp), style = MaterialTheme.typography.bodyLarge, lineHeight = 30.sp)
                }

                SectionHeader("Identity Grid", accentColor)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IdentityTile("Nature", planet.nature, Modifier.weight(1f))
                        IdentityTile("Element", planet.element, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IdentityTile("Caste", planet.caste, Modifier.weight(1f))
                        IdentityTile("Direction", planet.direction, Modifier.weight(1f))
                    }
                }

                SectionHeader("Planetary Indications", accentColor)
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        AttributeRow("Exaltation", planet.exaltation)
                        AttributeRow("Debilitation", planet.debilitation)
                        AttributeRow("Own Sign", planet.ownSign, isLast = true)
                    }
                }

                SectionHeader("Karakatvas", accentColor)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    planet.karakatvas.forEach { item ->
                        Surface(color = accentColor.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))) {
                            Text(item, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
