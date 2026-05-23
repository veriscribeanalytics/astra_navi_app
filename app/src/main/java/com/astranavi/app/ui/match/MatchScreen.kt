package com.astranavi.app.ui.match

import androidx.compose.animation.*
import com.astranavi.app.ui.components.PreviewMultiDevice
import com.astranavi.app.ui.theme.AstraNaviTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.R
import com.astranavi.app.data.model.*
import com.astranavi.app.ui.components.AstroDatePickerField
import com.astranavi.app.ui.components.AstroTimePickerField
import com.astranavi.app.ui.components.bringIntoViewOnFocus
import com.astranavi.app.ui.components.responsiveMetrics
import com.astranavi.app.ui.components.TwoPaneLayout
import androidx.compose.foundation.layout.FlowRow
//import androidx.compose.foundation.ExperimentalLayoutApi
import com.astranavi.app.ui.components.ShimmerBlock
import com.astranavi.app.ui.components.shimmerEffect
import com.astranavi.app.ui.components.titleCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun MatchScreen(
    viewModel: MatchViewModel, 
    onOpenDrawer: () -> Unit = {}, 
    onBack: () -> Unit = {}, 
    onViewHistory: () -> Unit = {}
) {
    com.astranavi.app.util.SecureScreen()
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchSkeleton() {
    val scrollState = rememberScrollState()
    val metrics = responsiveMetrics()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(metrics.matchCardHeight / 6)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(metrics.cardPadding * 1.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(metrics.orbitCoreSize * 1.2f).clip(CircleShape).shimmerEffect())
                Spacer(modifier = Modifier.height(metrics.matchCardHeight / 5))
                Box(modifier = Modifier.width(180.dp).height(28.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(metrics.matchCardHeight / 10))
                Box(modifier = Modifier.width(240.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(metrics.matchCardHeight / 10))
                Box(modifier = Modifier.fillMaxWidth(0.9f).height(80.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
            }
        }

        Box(modifier = Modifier.width(150.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) {
                Box(modifier = Modifier.weight(1f).widthIn(min = 280.dp).heightIn(min = metrics.matchCardHeight * 0.7f).clip(RoundedCornerShape(24.dp)).shimmerEffect())
            }
        }

        ShimmerBlock(height = 140.dp, cornerRadius = 24.dp)

        ShimmerBlock(height = 56.dp, cornerRadius = 16.dp)

        Spacer(modifier = Modifier.height(metrics.matchCardHeight / 4))
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
    val metrics = responsiveMetrics()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .navigationBarsPadding()
            .padding(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(metrics.matchCardHeight / 6)
    ) {
        Text(
            stringResource(R.string.match_desc_vedic_analysis),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        
        TwoPaneLayout(
            leftContent = {
                MatchPersonSection("PERSON 1 (YOU)", p1Name, onP1NameChange, p1Dob, onP1DobChange, p1Tob, onP1TobChange, p1Pob, onP1PobChange, p1Gender, onP1GenderChange)
            },
            rightContent = {
                MatchPersonSection("PERSON 2 (PARTNER)", p2Name, onP2NameChange, p2Dob, onP2DobChange, p2Tob, onP2TobChange, p2Pob, onP2PobChange, p2Gender, onP2GenderChange)
            }
        )

        Button(
            onClick = onCalculate,
            modifier = Modifier.fillMaxWidth().heightIn(min = metrics.buttonHeight),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = p1Name.isNotBlank() && p2Name.isNotBlank()
        ) {
            Text(stringResource(R.string.match_btn_calculate), fontWeight = FontWeight.ExtraBold)
        }
        
        Spacer(modifier = Modifier.height(metrics.matchCardHeight / 4))
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
    val metrics = responsiveMetrics()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding), verticalArrangement = Arrangement.spacedBy(metrics.matchCardHeight / 8)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, letterSpacing = if (metrics.isCompactWidth || metrics.isLargeFont) 1.sp else 2.sp, maxLines = 2)
            
            OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text(stringResource(R.string.match_field_name)) }, modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(), shape = RoundedCornerShape(12.dp))
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(metrics.matchCardHeight / 10),
                verticalArrangement = Arrangement.spacedBy(metrics.matchCardHeight / 10),
                modifier = Modifier.fillMaxWidth()
            ) {
                AstroDatePickerField(
                    value = dob, 
                    onValueChange = onDobChange, 
                    label = "DOB", 
                    modifier = Modifier.widthIn(min = metrics.matchCardHeight).weight(1f), 
                    shape = RoundedCornerShape(12.dp)
                )
                AstroTimePickerField(
                    value = tob, 
                    onValueChange = onTobChange, 
                    label = "TOB", 
                    modifier = Modifier.widthIn(min = metrics.matchCardHeight).weight(1f), 
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            OutlinedTextField(value = pob, onValueChange = onPobChange, label = { Text(stringResource(R.string.match_field_birth_place)) }, modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(), shape = RoundedCornerShape(12.dp))
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(metrics.matchCardHeight / 10),
                verticalArrangement = Arrangement.spacedBy(metrics.matchCardHeight / 15)
            ) {
                FilterChip(
                    selected = gender == "male",
                    onClick = { onGenderChange("male") },
                    label = { Text(stringResource(R.string.match_option_male), fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = gender == "female",
                    onClick = { onGenderChange("female") },
                    label = { Text(stringResource(R.string.match_option_female), fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchResultView(data: MatchResponse, onReset: () -> Unit) {
    val scrollState = rememberScrollState()
    val displayScore = data.ashtakoot?.total_score ?: data.score ?: 0.0
    val scoreColor = getMatchScoreColor(displayScore)
    val metrics = responsiveMetrics()

    val isSmallScreen = metrics.isCompactWidth || metrics.isLargeFont
    val ringSize = metrics.orbitCoreSize * 1.2f
    val textScale = if (isSmallScreen) 36.sp else 48.sp

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(metrics.matchCardHeight / 6)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, scoreColor)
        ) {
            Column(modifier = Modifier.padding(metrics.cardPadding * 1.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ringSize)) {
                    CircularProgressIndicator(
                        progress = { displayScore.toFloat() / 36f },
                        modifier = Modifier.fillMaxSize(),
                        color = scoreColor,
                        strokeWidth = metrics.matchIconSize / 3,
                        trackColor = scoreColor.copy(alpha = 0.1f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${displayScore.toInt()}", fontSize = textScale, fontWeight = FontWeight.Black, color = scoreColor)
                        Text(stringResource(R.string.match_label_out_of_36), style = MaterialTheme.typography.labelSmall, color = scoreColor, letterSpacing = 1.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Spacer(modifier = Modifier.height(metrics.matchCardHeight / 5))
                val scoreTextResourceId = when {
                    displayScore >= 25 -> R.string.match_score_excellent
                    displayScore >= 18 -> R.string.match_score_good
                    else -> R.string.match_score_low
                }
                Text(stringResource(scoreTextResourceId).titleCase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = scoreColor, textAlign = TextAlign.Center)
                val summaryText = data.ai_narrative ?: data.summary ?: "A detailed narrative analysis of your cosmic synchronization."
                Text(summaryText, textAlign = TextAlign.Center, modifier = Modifier.padding(top = metrics.matchCardHeight / 15), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }

        Text(stringResource(R.string.match_section_breakdown), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        val koots = data.ashtakoot?.koots ?: data.koot_details
        if (koots != null) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                koots.forEach { koot ->
                    Box(modifier = Modifier.weight(1f).widthIn(min = 280.dp)) {
                        KootCard(koot)
                    }
                }
            }
        }

        if (data.mangal_dosha != null) {
            MangalDoshaPanel(data.mangal_dosha!!)
        }

        Button(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth().heightIn(min = metrics.buttonHeight),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.match_btn_analyze_another), fontWeight = FontWeight.ExtraBold)
        }
        
        Spacer(modifier = Modifier.height(metrics.matchCardHeight / 4))
    }
}

@Composable
fun KootCard(koot: KootDetail) {
    val kootObtained = koot.obtained ?: koot.score ?: 0.0
    val kootMax = koot.max ?: koot.total ?: 1
    val metrics = responsiveMetrics()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(metrics.cardPadding), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    koot.name, 
                    fontWeight = FontWeight.ExtraBold, 
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val descriptionText = koot.detail ?: koot.description
                if (descriptionText != null) {
                    Text(descriptionText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(metrics.matchCardHeight / 10))
            Column(horizontalAlignment = Alignment.End) {
                Text("${kootObtained.toInt()}/$kootMax", fontWeight = FontWeight.Black, color = if (kootObtained >= kootMax/2.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                LinearProgressIndicator(
                    progress = { (kootObtained.toFloat() / kootMax.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.width(metrics.matchIconSize * 1.8f).height(metrics.matchCardHeight / 20).clip(CircleShape),
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
    val metrics = responsiveMetrics()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, color)
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding * 1.2f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isMangal) Icons.Default.Warning else Icons.Default.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.size(metrics.matchIconSize))
                Spacer(modifier = Modifier.width(metrics.matchCardHeight / 10))
                Text(
                    stringResource(R.string.match_label_mangal_status),
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(metrics.matchCardHeight / 8))
            Text(if (isMangal) stringResource(R.string.match_msg_mangal_detected) else stringResource(R.string.match_msg_no_mangal), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            
            val cancellation = dosha.person2?.cancellation ?: dosha.cancellation_reason
            if (cancellation != null) {
                Text(stringResource(R.string.match_label_cancellation) + " $cancellation", modifier = Modifier.padding(top = metrics.matchCardHeight / 15), style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
            }
            
            val descriptionText = dosha.note ?: dosha.description
            if (descriptionText != null) {
                Text(descriptionText, modifier = Modifier.padding(top = metrics.matchCardHeight / 10), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun getMatchScoreColor(score: Double) = when {
    score >= 25 -> Color(0xFF10B981)
    score >= 18 -> Color(0xFFF59E0B)
    else -> Color(0xFFEF4444)
}

@PreviewMultiDevice
@Composable
fun KootCardPreview() {
    AstraNaviTheme {
        Surface {
            KootCard(
                koot = KootDetail(
                    name = "Nadi",
                    score = 8.0,
                    total = 8,
                    description = "Pulse and physiological compatibility."
                )
            )
        }
    }
}

// Preview at bottom
fun getScoreLabel(score: Double) = when {
    score >= 25 -> "Excellent Match"
    score >= 18 -> "Good Match"
    else -> "Low Compatibility"
}
