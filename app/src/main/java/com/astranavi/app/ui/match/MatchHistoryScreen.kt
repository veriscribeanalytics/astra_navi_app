package com.astranavi.app.ui.match

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.data.model.MatchRecord
import com.astranavi.app.data.model.MatchResponse
import com.astranavi.app.ui.components.GlassCard

@Composable
fun MatchHistoryScreen(
    viewModel: MatchHistoryViewModel,
    onOpenDrawer: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState = viewModel.uiState.value

    Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState) {
                is MatchHistoryState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
                is MatchHistoryState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.message, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
                is MatchHistoryState.Success -> {
                    if (uiState.history.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No compatibility records found.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.history) { record ->
                                HistoryRecordCard(
                                    record = record,
                                    details = viewModel.expandedDetails[record.id],
                                    isLoadingDetails = viewModel.loadingDetails[record.id] ?: false,
                                    onExpand = { viewModel.fetchMatchDetails(record.id) },
                                    onDelete = { viewModel.deleteMatch(record.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryRecordCard(
    record: MatchRecord,
    details: MatchResponse?,
    isLoadingDetails: Boolean,
    onExpand: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val displayScore = record.total_score ?: record.score ?: 0.0
    val scoreColor = getMatchScoreColor(displayScore)
    val displayName = when {
        !record.person1Name.isNullOrEmpty() -> "${record.person1Name} & ${record.person2Name}"
        !record.groom_name.isNullOrEmpty() -> "${record.groom_name} & ${record.bride_name}"
        else -> "${record.person1_name} & ${record.person2_name}"
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                isExpanded = !isExpanded 
                if (isExpanded) onExpand()
            },
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).background(scoreColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Favorite, 
                        contentDescription = null, 
                        tint = scoreColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        (record.created_at ?: "").take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${displayScore.toInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = scoreColor
                    )
                    Text("/ 36", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Black)
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isLoadingDetails) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.secondary)
                        }
                    } else {
                        val responseToUse = details ?: record.resultData
                        val narrative = responseToUse?.ai_narrative ?: record.aiNarrative ?: record.summary ?: "No summary available."
                        Text(
                            narrative,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp
                        )
                        
                        val ashtakoot = responseToUse?.ashtakoot
                        val koots = ashtakoot?.koots ?: responseToUse?.koot_details
                        if (koots != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Guna Milan Details", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            koots.forEach { koot ->
                                val kootObtained = koot.obtained ?: koot.score ?: 0.0
                                val kootMax = koot.max ?: koot.total ?: 0
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(koot.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text("${kootObtained.toInt()} / ${kootMax}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        val mangal = responseToUse?.mangal_dosha
                        if (mangal != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Mangal Dosha", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                            val doshaStatus = when {
                                mangal.compatible == true -> "Compatible (Safe)"
                                mangal.person2?.has_dosha == true -> "High Influence Detected"
                                mangal.is_mangal_dosha == true -> "Influence Detected"
                                else -> "No Major Influence"
                            }
                            Text(
                                doshaStatus,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Record", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
