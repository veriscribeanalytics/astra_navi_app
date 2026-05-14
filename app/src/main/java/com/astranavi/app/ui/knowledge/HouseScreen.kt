package com.astranavi.app.ui.knowledge

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.LocalTopBarTitle
import com.astranavi.app.data.model.House
import com.astranavi.app.data.repository.houses
import com.astranavi.app.ui.components.*

@Composable
fun HouseScreen(onBack: () -> Unit = {}) {
    var selectedHouse by remember { mutableStateOf<House?>(null) }
    val houses = houses
    val setTitle = LocalTopBarTitle.current

    LaunchedEffect(selectedHouse) {
        setTitle?.invoke(selectedHouse?.nameEn ?: "Houses")
    }

    BackHandler(enabled = true) {
        if (selectedHouse != null) selectedHouse = null else onBack()
    }

    Scaffold(containerColor = Color.Transparent, contentWindowInsets = WindowInsets(0.dp)) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.Transparent)) {
            ParticleBackground()
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
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(houses) { house ->
            val accentColor = MaterialTheme.colorScheme.primary
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(house) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(64.dp).background(accentColor, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Text(house.id.toString(), fontWeight = FontWeight.Black, color = Color.White, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(house.nameEn, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                    Text(house.element, style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun HouseDetail(house: House) {
    val accentColor = MaterialTheme.colorScheme.primary
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 64.dp)) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(colors = listOf(accentColor.copy(alpha = 0.2f), Color.Transparent))).padding(top = 48.dp, bottom = 32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = house.id.toString(), fontSize = 56.sp, fontWeight = FontWeight.Black, color = accentColor)
                Text(text = house.nameEn.uppercase(), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 6.sp)
                Spacer(modifier = Modifier.height(24.dp))
                FlowRow(horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 24.dp)) {
                    InfoChip(house.naturalSign, accentColor)
                    InfoChip(house.element, Color.Gray)
                    InfoChip(house.type, MaterialTheme.colorScheme.tertiary)
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
            SectionHeader("Core Meaning", accentColor)
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.1f))) {
                Text(text = house.coreMeaning, modifier = Modifier.padding(24.dp), style = MaterialTheme.typography.bodyLarge, lineHeight = 30.sp)
            }

            SectionHeader("Identity Grid", accentColor)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IdentityTile("Natural Sign", house.naturalSign, Modifier.weight(1f))
                    IdentityTile("Karaka", house.naturalKaraka, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IdentityTile("Classification", house.classification, Modifier.weight(1f))
                    IdentityTile("Body Part", house.bodyPart, Modifier.weight(1f))
                }
            }
            
            SectionHeader("Significations", accentColor)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                house.significations.forEach { item ->
                    Surface(color = accentColor.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))) {
                        Text(item, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
