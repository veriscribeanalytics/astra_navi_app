package com.astranavi.app.ui.knowledge

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.astranavi.app.LocalTopBarTitle
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Yoga(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val sanskrit: String,
    val represents: String,
    val planet: String,
    val condition: String,
    val results: String,
    val deepDive: String,
    val classification: String,
    val color: Color,
    val icon: ImageVector
)

val yogas = listOf(
    Yoga(
        id = "raja",
        nameEn = "Raja Yoga",
        nameHi = "राज योग",
        sanskrit = "Raja Yoga",
        represents = "Power, authority, royal status, success in high positions, leadership.",
        planet = "9th & 10th Lords",
        condition = "Union of Kendra (Action) & Trikona (Luck)",
        results = "Highest worldly success and social status.",
        deepDive = "Raja Yoga is the ultimate combination for worldly success. It is formed when the lords of the angular houses (Kendra) meet the lords of the trinal houses (Trikona). This union blends effort with divine grace, leading to an elevated social position.",
        classification = "Kendra-Trikona Union",
        color = Color(0xFFF59E0B),
        icon = Icons.Default.Star
    ),
    Yoga(
        id = "dhana",
        nameEn = "Dhana Yoga",
        nameHi = "धन योग",
        sanskrit = "Dhana Yoga",
        represents = "Wealth accumulation, financial stability, material success, abundance.",
        planet = "2nd & 11th Lords",
        condition = "Connection between Earnings (2nd) & Savings (11th)",
        results = "Strong capacity for earnings and assets.",
        deepDive = "Dhana Yoga focuses purely on material and financial gains. It occurs when the planets ruling the 2nd (wealth) and 11th (gains) houses form a relationship with the 5th or 9th houses.",
        classification = "Wealth Union",
        color = Color(0xFF10B981),
        icon = Icons.Default.MonetizationOn
    ),
    Yoga(
        id = "gajakesari",
        nameEn = "Gajakesari",
        nameHi = "गजकेसरी योग",
        sanskrit = "Gajakesari Yoga",
        represents = "Wisdom, lasting reputation, eloquence, victory over enemies.",
        planet = "Jupiter & Moon",
        condition = "Jupiter in Kendra from Moon",
        results = "Wisdom, virtue, and long-lasting fame.",
        deepDive = "Represented by the Elephant (Gaja) and the Lion (Kesari), this yoga brings together the wisdom of Jupiter and the mental strength of the Moon. It makes the native deeply respected and often leads to an abundance of intellectual brilliance.",
        classification = "Jup-Moon Relation",
        color = Color(0xFF3B82F6),
        icon = Icons.Default.AutoAwesome
    ),
    Yoga(
        id = "mahapurusha",
        nameEn = "Mahapurusha",
        nameHi = "महापुरुष योग",
        sanskrit = "Pancha Mahapurusha",
        represents = "Extraordinary personality, specialized greatness, mastery in a field.",
        planet = "Non-Luminaries",
        condition = "Planet in Own/Exalt Sign in Kendra",
        results = "Exceptional human qualities based on the ruling planet.",
        deepDive = "These five 'Great Person' yogas are formed by Mars (Ruchaka), Mercury (Bhadra), Jupiter (Hamsa), Venus (Malavya), and Saturn (Shasha). They create an individual who embodies the highest virtues of that planet.",
        classification = "Planetary Strength",
        color = Color(0xFF8B5CF6),
        icon = Icons.Default.TrendingUp
    ),
    Yoga(
        id = "viparita",
        nameEn = "Viparita Raja",
        nameHi = "विपरीत राज योग",
        sanskrit = "Viparita Raja Yoga",
        represents = "Success after crisis, power through reversal, benefit from loss of others.",
        planet = "6th, 8th, 12th Lords",
        condition = "Negative Planets in Negative Houses",
        results = "Resilience and breakthrough during times of crisis.",
        deepDive = "A highly strategic yoga where the lords of the 'difficult houses' are placed within other difficult houses. It signifies that the native will rise to power by overcoming immense obstacles.",
        classification = "Dusthana Alchemy",
        color = Color(0xFFF97316),
        icon = Icons.Default.FlashOn
    )
)

@Composable
fun YogaScreen(onBack: () -> Unit, onOpenDrawer: () -> Unit) {
    var selectedYoga by remember { mutableStateOf<Yoga?>(null) }
    val setTitle = LocalTopBarTitle.current

    LaunchedEffect(selectedYoga) {
        setTitle?.invoke(selectedYoga?.nameEn)
    }

    BackHandler(enabled = true) {
        if (selectedYoga != null) {
            selectedYoga = null
        } else {
            onBack()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.Transparent)) {
            if (selectedYoga == null) {
                YogaList(yogas) { selectedYoga = it }
            } else {
                YogaDetail(selectedYoga!!)
            }
        }
    }
}

@Composable
fun YogaList(yogas: List<Yoga>, onSelect: (Yoga) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.Transparent),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Vedic astrology identifies hundreds of Yogas — specific planetary alignments that create unique life results.", 
                 style = MaterialTheme.typography.bodyMedium, 
                 color = MaterialTheme.colorScheme.onBackground,
                 fontWeight = FontWeight.Bold,
                 modifier = Modifier.padding(bottom = 8.dp))
        }
        
        items(yogas) { yoga ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(yoga) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(56.dp).background(yoga.color, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(yoga.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(yoga.nameEn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Text(yoga.classification, style = MaterialTheme.typography.labelSmall, color = yoga.color, fontWeight = FontWeight.Black)
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun YogaDetail(yoga: Yoga) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(2.dp, yoga.color)
        ) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(yoga.icon, contentDescription = null, tint = yoga.color, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(yoga.nameEn, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text(yoga.nameHi, style = MaterialTheme.typography.titleMedium, color = yoga.color, fontWeight = FontWeight.ExtraBold)
            }
        }

        // Stats Row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            YogaStatCard("Classification", yoga.classification, yoga.color, Modifier.weight(1f))
            YogaStatCard("Key Planets", yoga.planet, yoga.color, Modifier.weight(1f))
        }

        // Logic Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("THE LOGIC", style = MaterialTheme.typography.labelSmall, color = yoga.color, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(yoga.condition, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }

        // Deep Dive
        Text("Detailed Analysis", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text(
            text = yoga.deepDive, 
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 28.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        // Results
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EXPECTED RESULTS", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(yoga.results, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun YogaStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, letterSpacing = 1.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}
