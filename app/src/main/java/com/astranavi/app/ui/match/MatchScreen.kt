package com.astranavi.app.ui.match

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.data.model.*
import com.astranavi.app.ui.components.AstroDatePickerField
import com.astranavi.app.ui.components.AstroTimePickerField
import com.astranavi.app.ui.components.bringIntoViewOnFocus
import com.astranavi.app.ui.components.shimmerEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun MatchScreen(
    viewModel: MatchViewModel, 
    onOpenDrawer: () -> Unit = {}, 
    onBack: () -> Unit = {}, 
    onViewHistory: () -> Unit = {}
) {
    val uiState = viewModel.uiState.value
    
    var p1Name by remember { mutableStateOf("") }
    var p1Dob by remember { mutableStateOf("") }
    var p1Tob by remember { mutableStateOf("") }
    var p1Pob by remember { mutableStateOf("") }
    var p1Gender by remember { mutableStateOf("male") }

    var p2Name by remember { mutableStateOf("") }
    var p2Dob by remember { mutableStateOf("") }
    var p2Tob by remember { mutableStateOf("") }
    var p2Pob by remember { mutableStateOf("") }
    var p2Gender by remember { mutableStateOf("female") }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        when (uiState) {
            is MatchState.Loading -> MatchSkeleton()
            is MatchState.Success -> MatchResultView(uiState.data) { viewModel.resetToIdle() }
            else -> {
                MatchInputForm(
                    p1Name = p1Name, onP1NameChange = { p1Name = it },
                    p1Dob = p1Dob, onP1DobChange = { p1Dob = it },
                    p1Tob = p1Tob, onP1TobChange = { p1Tob = it },
                    p1Pob = p1Pob, onP1PobChange = { p1Pob = it },
                    p1Gender = p1Gender, onP1GenderChange = { p1Gender = it },
                    p2Name = p2Name, onP2NameChange = { p2Name = it },
                    p2Dob = p2Dob, onP2DobChange = { p2Dob = it },
                    p2Tob = p2Tob, onP2TobChange = { p2Tob = it },
                    p2Pob = p2Pob, onP2PobChange = { p2Pob = it },
                    p2Gender = p2Gender, onP2GenderChange = { p2Gender = it },
                    onCalculate = {
                        viewModel.calculateMatch(
                            PersonDetail(p1Name, p1Dob, p1Tob, p1Pob, p1Gender),
                            PersonDetail(p2Name, p2Dob, p2Tob, p2Pob, p2Gender)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun MatchSkeleton() {
// ... rest of MatchSkeleton ...
    val scrollState = rememberScrollState()
// ... rest of MatchSkeleton ...
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
// ... rest of MatchSkeleton ...
        // Shimmering Score Ring Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(
                    modifier = Modifier.size(160.dp).clip(CircleShape).shimmerEffect()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.width(180.dp).height(28.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.width(240.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }
        }

        // Shimmering title
        Box(modifier = Modifier.width(150.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())

        // Ghosted Breakdown Cards
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.width(100.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.width(140.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    }
                    Box(modifier = Modifier.width(60.dp).height(6.dp).clip(CircleShape).shimmerEffect())
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MatchInputForm(
    p1Name: String, onP1NameChange: (String) -> Unit,
    p1Dob: String, onP1DobChange: (String) -> Unit,
    p1Tob: String, onP1TobChange: (String) -> Unit,
    p1Pob: String, onP1PobChange: (String) -> Unit,
    p1Gender: String, onP1GenderChange: (String) -> Unit,
    p2Name: String, onP2NameChange: (String) -> Unit,
    p2Dob: String, onP2DobChange: (String) -> Unit,
    p2Tob: String, onP2TobChange: (String) -> Unit,
    p2Pob: String, onP2PobChange: (String) -> Unit,
    p2Gender: String, onP2GenderChange: (String) -> Unit,
    onCalculate: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "Traditional 36 Guna Vedic match analysis for harmony and longevity.", 
            style = MaterialTheme.typography.bodyMedium, 
            color = MaterialTheme.colorScheme.onBackground, 
            fontWeight = FontWeight.Bold
        )
        
        MatchPersonSection("PERSON 1 (YOU)", p1Name, onP1NameChange, p1Dob, onP1DobChange, p1Tob, onP1TobChange, p1Pob, onP1PobChange, p1Gender, onP1GenderChange)
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(modifier = Modifier.size(56.dp), shape = CircleShape, color = MaterialTheme.colorScheme.secondary) {
                Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.padding(14.dp), tint = Color.White)
            }
        }

        MatchPersonSection("PERSON 2 (PARTNER)", p2Name, onP2NameChange, p2Dob, onP2DobChange, p2Tob, onP2TobChange, p2Pob, onP2PobChange, p2Gender, onP2GenderChange)

        Button(
            onClick = onCalculate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = p1Name.isNotBlank() && p2Name.isNotBlank()
        ) {
            Text("Calculate Compatibility", fontWeight = FontWeight.ExtraBold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchPersonSection(
    label: String,
    name: String, onNameChange: (String) -> Unit,
    dob: String, onDobChange: (String) -> Unit,
    tob: String, onTobChange: (String) -> Unit,
    pob: String, onPobChange: (String) -> Unit,
    gender: String, onGenderChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            
            OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Name") }, modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(), shape = RoundedCornerShape(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AstroDatePickerField(
                    value = dob, 
                    onValueChange = onDobChange, 
                    label = "DOB", 
                    modifier = Modifier.weight(1f), 
                    shape = RoundedCornerShape(12.dp)
                )
                AstroTimePickerField(
                    value = tob, 
                    onValueChange = onTobChange, 
                    label = "TOB", 
                    modifier = Modifier.weight(1f), 
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            OutlinedTextField(value = pob, onValueChange = onPobChange, label = { Text("Birth Place") }, modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(), shape = RoundedCornerShape(12.dp))
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = gender == "male", 
                    onClick = { onGenderChange("male") }, 
                    label = { Text("Male", fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = gender == "female", 
                    onClick = { onGenderChange("female") }, 
                    label = { Text("Female", fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
fun MatchResultView(data: MatchResponse, onReset: () -> Unit) {
    val scrollState = rememberScrollState()
    val displayScore = data.ashtakoot?.total_score ?: data.score ?: 0.0
    val scoreColor = getMatchScoreColor(displayScore)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isSmallScreen = maxWidth < 360.dp
        val ringSize = if (isSmallScreen) 120.dp else 160.dp
        val textScale = if (isSmallScreen) 36.sp else 48.sp

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(if (isSmallScreen) 16.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Score Ring
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(2.dp, scoreColor)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ringSize)) {
                        CircularProgressIndicator(
                            progress = { displayScore.toFloat() / 36f },
                            modifier = Modifier.fillMaxSize(),
                            color = scoreColor,
                            strokeWidth = 12.dp,
                            trackColor = scoreColor.copy(alpha = 0.1f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${displayScore.toInt()}", fontSize = textScale, fontWeight = FontWeight.Black, color = scoreColor)
                            Text("OUT OF 36", style = MaterialTheme.typography.labelSmall, color = scoreColor, letterSpacing = 1.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(getScoreLabel(displayScore).uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = scoreColor, textAlign = TextAlign.Center)
                    val summaryText = data.ai_narrative ?: data.summary ?: "A detailed analysis of your cosmic synchronization."
                    Text(summaryText, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }

            // Guna Breakdown
            Text("Ashtakoot Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            val koots = data.ashtakoot?.koots ?: data.koot_details
            koots?.forEach { koot ->
                KootCard(koot)
            }

            // Mangal Dosha
            if (data.mangal_dosha != null) {
                MangalDoshaPanel(data.mangal_dosha!!)
            }

            Button(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Analyze Another Match", fontWeight = FontWeight.ExtraBold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun KootCard(koot: KootDetail) {
    val kootObtained = koot.obtained ?: koot.score ?: 0.0
    val kootMax = koot.max ?: koot.total ?: 1
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    koot.name, 
                    fontWeight = FontWeight.ExtraBold, 
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val descriptionText = koot.detail ?: koot.description
                if (descriptionText != null) {
                    Text(descriptionText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("${kootObtained.toInt()}/$kootMax", fontWeight = FontWeight.Black, color = if (kootObtained >= kootMax/2.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                LinearProgressIndicator(
                    progress = { (kootObtained.toFloat() / kootMax.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.width(60.dp).height(6.dp).clip(CircleShape),
                    color = if (kootObtained >= kootMax/2.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
fun MangalDoshaPanel(dosha: MangalDoshaDetail) {
    val isMangal = dosha.person2?.has_dosha ?: dosha.is_mangal_dosha ?: false
    val color = if (isMangal) Color(0xFFEF4444) else Color(0xFF10B981)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, color)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isMangal) Icons.Default.Warning else Icons.Default.CheckCircle, contentDescription = null, tint = color)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "MANGAL DOSHA STATUS", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = color, 
                    fontWeight = FontWeight.ExtraBold, 
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(if (isMangal) "Mars influence detected in the charts." else "No Mangal Dosha present.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            
            val cancellation = dosha.person2?.cancellation ?: dosha.cancellation_reason
            if (cancellation != null) {
                Text("Cancellation: $cancellation", modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
            }
            
            val descriptionText = dosha.note ?: dosha.description
            if (descriptionText != null) {
                Text(descriptionText, modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun getMatchScoreColor(score: Double) = when {
    score >= 25 -> Color(0xFF10B981)
    score >= 18 -> Color(0xFFF59E0B)
    else -> Color(0xFFEF4444)
}

fun getScoreLabel(score: Double) = when {
    score >= 25 -> "Excellent Match"
    score >= 18 -> "Good Match"
    else -> "Low Compatibility"
}
