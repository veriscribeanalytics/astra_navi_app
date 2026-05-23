package com.astranavi.app.ui.knowledge

import android.graphics.Color as AndroidColor
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.astranavi.app.LocalTopBarColor
import com.astranavi.app.LocalTopBarTitle
import com.astranavi.app.R
import com.astranavi.app.data.model.Planet
import com.astranavi.app.data.repository.planetsFor
import com.astranavi.app.ui.components.*
import com.astranavi.app.util.currentAppLocale
import com.astranavi.app.ui.theme.AstroColors
import com.astranavi.app.ui.theme.PlanetPalettes
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset

@Composable
fun PlanetScreen(onBack: () -> Unit = {}) {
    var selectedPlanet by remember { mutableStateOf<Planet?>(null) }
    val planets = planetsFor(currentAppLocale().language)
    val setTitle = LocalTopBarTitle.current
    val setTopBarColor = LocalTopBarColor.current
    val topBarColor = MaterialTheme.colorScheme.onBackground
    val defaultTitle = stringResource(R.string.knowledge_title_planets)

    LaunchedEffect(selectedPlanet, topBarColor) {
        setTitle?.invoke(selectedPlanet?.nameEn ?: defaultTitle)
        setTopBarColor?.invoke(if (selectedPlanet == null) null else topBarColor)
    }

    BackHandler(enabled = true) {
        if (selectedPlanet != null) selectedPlanet = null else onBack()
    }

    Scaffold(containerColor = Color.Transparent, contentWindowInsets = WindowInsets(0.dp)) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.Transparent)) {
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
    val metrics = responsiveMetrics()

    LazyVerticalGrid(
        columns = responsiveGridCells(),
        contentPadding = PaddingValues(
            start = metrics.pagePadding,
            top = metrics.listTopPadding,
            end = metrics.pagePadding,
            bottom = metrics.listBottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(if (metrics.isCompactHeight) 12.dp else 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(planets, key = { it.nameEn }) { planet ->
            val accentColor = Color(planet.colorHex)
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(planet) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(metrics.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                    val iconRes = getIconResId(planet.nameEn)
                    Box(modifier = Modifier.size(48.dp).background(accentColor, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        if (iconRes != 0) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current).data(iconRes).crossfade(true).build(),
                                contentDescription = planet.nameEn,
                                modifier = Modifier.size(32.dp)
                            ) {
                                val state = painter.state
                                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Empty) {
                                    CircularProgressIndicator(color = accentColor, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else if (state is AsyncImagePainter.State.Error) {
Text(planet.nameEn.take(1), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp)
                                } else {
                                    SubcomposeAsyncImageContent()
                                }
                            }
                        } else {
                            Text(planet.nameEn.take(1), fontWeight = FontWeight.Black, color = Color.White, fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(planet.nameEn, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                        Text(planet.nature, style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.Black, maxLines = 2)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = accentColor)
                }
            }
        }
    }
}

@Composable
fun PlanetDetail(planet: Planet) {
    val palette = PlanetPalettes.forPlanet(planet.nameEn)
    val accentColor = palette.accent
    val metrics = responsiveMetrics()

    Box(modifier = Modifier.fillMaxSize()) {
        ApplyRootGlow(
            GlowColors(
                accent = palette.topGlow,
                deep = palette.deepGlow,
                radial = palette.radialGlow
            )
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = metrics.listBottomPadding)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = metrics.heroTopPadding, bottom = metrics.heroBottomPadding, start = metrics.pagePadding, end = metrics.pagePadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val iconRes = getIconResId(planet.nameEn)
                    Box(
                        modifier = Modifier
                            .size(metrics.heroIconSize)
                            .drawBehind {
                                val r = size.minDimension * 0.95f
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            palette.radialGlow.copy(alpha = 0.24f),
                                            palette.accent.copy(alpha = 0.10f),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width / 2f, size.height / 2f),
                                        radius = r
                                    ),
                                    radius = r,
                                    center = Offset(size.width / 2f, size.height / 2f)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (iconRes != 0) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current).data(iconRes).crossfade(true).build(),
                                contentDescription = planet.nameEn,
                                modifier = Modifier.size(metrics.heroIconSize)
                            ) {
                                val state = painter.state
                                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Empty) {
                                    CircularProgressIndicator(color = accentColor, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else if (state is AsyncImagePainter.State.Error) {
                                    Text(text = planet.nameHi, fontSize = metrics.heroPrimaryFontSize, lineHeight = metrics.heroPrimaryLineHeight, fontWeight = FontWeight.Black, color = accentColor, textAlign = TextAlign.Center)
                                } else {
                                    SubcomposeAsyncImageContent()
                                }
                            }
                        } else {
                            Text(text = planet.nameHi, fontSize = metrics.heroPrimaryFontSize, lineHeight = metrics.heroPrimaryLineHeight, fontWeight = FontWeight.Black, color = accentColor, textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(modifier = Modifier.height(if (metrics.isCompactHeight) 12.dp else 16.dp))
                    FlowRow(horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        InfoChip(planet.nature, accentColor)
                        InfoChip(planet.element, MaterialTheme.colorScheme.onSurfaceVariant)
                        InfoChip(planet.guna, MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = metrics.pagePadding).padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(if (metrics.isCompactHeight) 20.dp else 28.dp)) {
                SectionHeader(stringResource(R.string.knowledge_section_core_nature), accentColor)
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.1f))) {
                    Text(
                        text = planet.coreNature,
                        modifier = Modifier.padding(metrics.cardPadding),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 26.sp,
                        letterSpacing = 0.sp
                    )
                }

                SectionHeader(stringResource(R.string.knowledge_section_identity_grid), accentColor)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IdentityTile(stringResource(R.string.knowledge_tile_label_nature), planet.nature, Modifier.weight(1f))
                    IdentityTile(stringResource(R.string.knowledge_tile_label_element), planet.element, Modifier.weight(1f))
                    IdentityTile(stringResource(R.string.knowledge_tile_label_caste), planet.caste, Modifier.weight(1f))
                    IdentityTile(stringResource(R.string.knowledge_tile_label_direction), planet.direction, Modifier.weight(1f))
                }

                SectionHeader(stringResource(R.string.knowledge_section_planetary_indications), accentColor)
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))) {
                    Column(modifier = Modifier.padding(metrics.cardPadding)) {
                        AttributeRow(stringResource(R.string.knowledge_planet_exaltation), planet.exaltation)
                        AttributeRow(stringResource(R.string.knowledge_planet_debilitation), planet.debilitation)
                        AttributeRow(stringResource(R.string.knowledge_planet_own_sign), planet.ownSign, isLast = true)
                    }
                }

                SectionHeader(stringResource(R.string.knowledge_section_karakatvas), accentColor)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    planet.karakatvas.forEach { item ->
                        Surface(color = accentColor.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))) {
                            Text(item, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
