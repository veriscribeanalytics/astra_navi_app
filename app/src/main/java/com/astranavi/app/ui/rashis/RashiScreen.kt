package com.astranavi.app.ui.rashis

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.astranavi.app.LocalTopBarColor
import com.astranavi.app.LocalTopBarTitle
import com.astranavi.app.R
import com.astranavi.app.util.currentAppLocale
import com.astranavi.app.data.model.Rashi
import com.astranavi.app.ui.components.ApplyRootGlow
import com.astranavi.app.ui.components.GlowColors
import com.astranavi.app.ui.components.ParticleBackground
import com.astranavi.app.ui.components.getIconResId
import com.astranavi.app.ui.components.responsiveGridCells
import com.astranavi.app.ui.components.responsiveMetrics
import com.astranavi.app.ui.components.titleCase
import com.astranavi.app.ui.theme.AstroColors
import com.astranavi.app.ui.theme.RashiPalettes

@Composable
fun RashiScreen(viewModel: RashiViewModel, onBack: () -> Unit = {}) {
    val viewMode by viewModel.viewMode
    val setTitle = LocalTopBarTitle.current
    val setTopBarColor = LocalTopBarColor.current
    val topBarColor = MaterialTheme.colorScheme.onBackground
    val rashisTitle = stringResource(R.string.knowledge_title_rashis)
    val locale = currentAppLocale().language
    val localizedRashis = viewModel.localizedRashis(locale)

    LaunchedEffect(viewMode, topBarColor) {
        when (val mode = viewMode) {
            is RashiViewMode.Encyclopedia -> {
                setTitle?.invoke(rashisTitle)
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
            when (viewMode) {
                is RashiViewMode.Encyclopedia -> RashiGrid(localizedRashis) { viewModel.selectRashi(it) }
                is RashiViewMode.Detail -> RashiDetail(rashi = (viewMode as RashiViewMode.Detail).rashi)
            }
        }
    }
}

@Composable
fun RashiGrid(rashis: List<Rashi>, onSelect: (Rashi) -> Unit) {
    val metrics = responsiveMetrics()
    
    LazyVerticalGrid(
        columns = responsiveGridCells(),
        contentPadding = PaddingValues(
            start = metrics.pagePadding,
            top = metrics.listTopPadding,
            end = metrics.pagePadding,
            bottom = metrics.listBottomPadding
        ),
        horizontalArrangement = Arrangement.spacedBy(if (metrics.isCompactWidth) 12.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (metrics.isCompactHeight) 12.dp else 16.dp)
    ) {
        items(rashis, key = { it.id }) { rashi ->
            val accentColor = AstroColors.getPlanetaryColor(rashi.rulingPlanet)
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(rashi) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(metrics.cardPadding), horizontalAlignment = Alignment.CenterHorizontally) {
                    val iconRes = getIconResId(rashi.nameEn)
                    Box(modifier = Modifier.size(64.dp).background(accentColor, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        if (iconRes != 0) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(iconRes).crossfade(true).build(),
                                contentDescription = rashi.nameEn,
                                modifier = Modifier.size(40.dp)
                            ) {
                                val state = painter.state
                                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Empty) {
                                    CircularProgressIndicator(color = accentColor, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else if (state is AsyncImagePainter.State.Error) {
Text(rashi.nameHi.take(1), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp)
                                } else {
                                    SubcomposeAsyncImageContent()
                                }
                            }
                        } else {
                            Text(rashi.nameHi.take(1), fontWeight = FontWeight.Black, color = Color.White, fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(rashi.nameEn, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, maxLines = 2)
                    Text(rashi.rulingPlanet, style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 2)
                }
            }
        }
    }
}

@Composable
fun RashiDetail(rashi: Rashi) {
    val palette = RashiPalettes.forSign(rashi.nameEn)
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
                    val iconRes = getIconResId(rashi.nameEn)
                    Box(
                        modifier = Modifier
                            .size(metrics.heroIconSize)
                            .drawBehind {
                                val r = size.minDimension * 0.95f
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            palette.radialGlow.copy(alpha = 0.26f),
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
                                model = ImageRequest.Builder(LocalContext.current).data(iconRes).crossfade(true).build(),
                                contentDescription = rashi.nameEn,
                                modifier = Modifier.size(metrics.heroIconSize)
                            ) {
                                val state = painter.state
                                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Empty) {
                                    CircularProgressIndicator(color = accentColor, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else if (state is AsyncImagePainter.State.Error) {
                                    Text(text = rashi.nameHi, fontSize = metrics.heroPrimaryFontSize, lineHeight = metrics.heroPrimaryLineHeight, fontWeight = FontWeight.Black, color = accentColor, textAlign = TextAlign.Center)
                                } else {
                                    SubcomposeAsyncImageContent()
                                }
                            }
                        } else {
                            Text(text = rashi.nameHi, fontSize = metrics.heroPrimaryFontSize, lineHeight = metrics.heroPrimaryLineHeight, fontWeight = FontWeight.Black, color = accentColor, textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(modifier = Modifier.height(if (metrics.isCompactHeight) 12.dp else 16.dp))
                    FlowRow(horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        InfoChip(rashi.rulingPlanet, accentColor)
                        InfoChip(rashi.element, MaterialTheme.colorScheme.onSurfaceVariant)
                        InfoChip(rashi.guna, MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = metrics.pagePadding).padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(if (metrics.isCompactHeight) 20.dp else 28.dp)) {
                SectionHeader(stringResource(R.string.knowledge_section_core_nature), accentColor)
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.1f))) {
                    Text(
                        text = rashi.coreNature,
                        modifier = Modifier.padding(metrics.cardPadding),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 26.sp,
                        letterSpacing = 0.sp
                    )
                }

                SectionHeader(stringResource(R.string.knowledge_section_identity_grid), accentColor)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IdentityTile(stringResource(R.string.knowledge_tile_label_ruler), rashi.rulingPlanet, Modifier.weight(1f))
                    IdentityTile(stringResource(R.string.knowledge_tile_label_element), rashi.element, Modifier.weight(1f))
                    IdentityTile(stringResource(R.string.knowledge_tile_label_quality), rashi.quality, Modifier.weight(1f))
                    IdentityTile(stringResource(R.string.knowledge_tile_label_caste), rashi.caste, Modifier.weight(1f))
                }

                TraitSection(stringResource(R.string.knowledge_section_positive_traits), rashi.positiveTraits, Color(0xFF4CAF50))
                TraitSection(stringResource(R.string.knowledge_section_challenging_traits), rashi.challengingTraits, Color(0xFFF44336))

                SectionHeader(stringResource(R.string.knowledge_section_career_resonance), accentColor)
                rashi.careers.forEach { career ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(accentColor, CircleShape))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(career, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun InfoChip(text: String, color: Color) {
    val metrics = responsiveMetrics()
    Surface(color = color.copy(alpha = 0.1f), shape = CircleShape, border = BorderStroke(1.dp, color.copy(alpha = 0.4f))) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = if (metrics.isCompactWidth) 12.dp else 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun IdentityTile(label: String, value: String, modifier: Modifier = Modifier) {
    val metrics = responsiveMetrics()
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))) {
        Column(modifier = Modifier.padding(metrics.cardPadding)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black, letterSpacing = if (metrics.isCompactWidth || metrics.isLargeFont) 0.5.sp else 1.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(width = 6.dp, height = 28.dp).background(color, RoundedCornerShape(3.dp)))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
