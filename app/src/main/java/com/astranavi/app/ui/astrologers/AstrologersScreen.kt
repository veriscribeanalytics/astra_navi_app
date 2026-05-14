package com.astranavi.app.ui.astrologers

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.data.model.Astrologer
import com.astranavi.app.ui.components.shimmerEffect

@Composable
fun AstrologersScreen(viewModel: AstrologersViewModel, onOpenDrawer: () -> Unit = {}, onBack: () -> Unit = {}) {
    val astrologers = viewModel.astrologers.value
    val isLoading = viewModel.isLoading.value

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.fetchAstrologers()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        if (isLoading) {
            AstrologersSkeleton(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Select a verified astrologer for a deep-dive consultation into your personal chart.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                
                items(astrologers) { guru ->
                    AstrologerCard(guru)
                }
            }
        }
    }
}

@Composable
fun AstrologersSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
        }
        
        items(5) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(64.dp).clip(CircleShape).shimmerEffect())
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.width(120.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.width(80.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Box(modifier = Modifier.width(50.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.width(30.dp).height(10.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.width(180.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(16.dp)).shimmerEffect())
                }
            }
        }
    }
}

@Composable
fun AstrologerCard(guru: Astrologer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(guru.name.take(1), fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                    if (guru.online) {
                        Box(modifier = Modifier.size(12.dp).background(Color(0xFF22C55E), CircleShape).border(2.dp, Color.White, CircleShape).align(Alignment.BottomEnd))
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        guru.name, 
                        fontWeight = FontWeight.Black, 
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(14.dp))
                        Text(" ${guru.rating} ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("(${guru.reviews} sessions)", color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text("$${guru.price}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 20.sp)
                    Text("/min", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                guru.special.joinToString(" · "), 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.primary, 
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("${guru.exp} years of Vedic wisdom & practice.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = {}, 
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Text("Book Session", fontWeight = FontWeight.Bold)
            }
        }
    }
}
