package com.astranavi.app.ui.knowledge

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.LocalTopBarTitle
import com.astranavi.app.R
import com.astranavi.app.data.model.House
import com.astranavi.app.data.repository.housesFor
import com.astranavi.app.ui.components.*
import com.astranavi.app.util.currentAppLocale

@Composable
fun HouseScreen(onBack: () -> Unit = {}) {
    var selectedHouse by remember { mutableStateOf<House?>(null) }
    val currentLocale = currentAppLocale()
    val houses = housesFor(currentLocale.language)
    val setTitle = LocalTopBarTitle.current
    val defaultTitle = stringResource(R.string.knowledge_title_houses)

    LaunchedEffect(selectedHouse) {
        setTitle?.invoke(selectedHouse?.nameEn ?: defaultTitle)
    }

    BackHandler(enabled = true) {
        if (selectedHouse != null) selectedHouse = null else onBack()
    }

    Scaffold(containerColor = Color.Transparent, contentWindowInsets = WindowInsets(0.dp)) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.Transparent)) {
            if (selectedHouse == null) {
                HouseGrid(houses) { selectedHouse = it }
            } else {
                HouseDetail(selectedHouse!!)
            }
        }
    }
}

@Composable
fun HouseGrid(houses: List<House>, onSelect: (House) -> Unit) {
    val metrics = responsiveMetrics()

    LazyVerticalGrid(
        columns = responsiveGridCells(),
        contentPadding = PaddingValues(
            start = metrics.pagePadding,
            top = metrics.pagePadding,
            end = metrics.pagePadding,
            bottom = metrics.listBottomPadding
        ),
        horizontalArrangement = Arrangement.spacedBy(if (metrics.isCompactWidth) 12.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (metrics.isCompactHeight) 12.dp else 16.dp)
    ) {
        items(houses, key = { it.id }) { house ->
            val accentColor = MaterialTheme.colorScheme.primary
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(house) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(metrics.cardPadding), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(64.dp).background(accentColor, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Text(house.id.toString(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(house.nameEn, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                    Text(house.element, style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.Black, maxLines = 2)
                }
            }
        }
    }
}

@Composable
fun HouseDetail(house: House) {
    val accentColor = MaterialTheme.colorScheme.primary
    val metrics = responsiveMetrics()
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = metrics.listBottomPadding)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(colors = listOf(accentColor.copy(alpha = 0.2f), Color.Transparent)))
                .padding(top = if (metrics.isCompactHeight) 32.dp else 48.dp, bottom = metrics.heroBottomPadding, start = metrics.pagePadding, end = metrics.pagePadding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.knowledge_house_prefix, house.id), fontSize = metrics.heroPrimaryFontSize, lineHeight = metrics.heroPrimaryLineHeight, fontWeight = FontWeight.Black, color = accentColor)
                Text(text = house.nameEn.titleCase(), fontSize = metrics.heroTitleFontSize, fontWeight = FontWeight.ExtraBold, letterSpacing = metrics.heroLetterSpacing, maxLines = 2)
                Spacer(modifier = Modifier.height(if (metrics.isCompactHeight) 16.dp else 24.dp))
                FlowRow(horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    InfoChip(house.naturalSign, accentColor)
                    InfoChip(house.element, MaterialTheme.colorScheme.onSurfaceVariant)
                    InfoChip(house.type, MaterialTheme.colorScheme.tertiary)
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = metrics.pagePadding), verticalArrangement = Arrangement.spacedBy(if (metrics.isCompactHeight) 24.dp else 32.dp)) {
            SectionHeader(stringResource(R.string.knowledge_section_core_meaning), accentColor)
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.1f))) {
                Text(text = house.coreMeaning, modifier = Modifier.padding(metrics.cardPadding), style = MaterialTheme.typography.bodyLarge, lineHeight = 30.sp)
            }

            SectionHeader(stringResource(R.string.knowledge_section_identity_grid), accentColor)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                IdentityTile(stringResource(R.string.knowledge_tile_label_natural_sign), house.naturalSign, Modifier.weight(1f))
                IdentityTile(stringResource(R.string.knowledge_tile_label_karaka), house.naturalKaraka, Modifier.weight(1f))
                IdentityTile(stringResource(R.string.knowledge_tile_label_classification), house.classification, Modifier.weight(1f))
                IdentityTile(stringResource(R.string.knowledge_tile_label_body_part), house.bodyPart, Modifier.weight(1f))
            }

            SectionHeader(stringResource(R.string.knowledge_section_significations), accentColor)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                house.significations.forEach { item ->
                    Surface(color = accentColor.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))) {
                        Text(item, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
