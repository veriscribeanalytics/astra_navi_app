package com.astranavi.app.ui.entitlement

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.data.model.PaywallProduct
import com.astranavi.app.data.model.PackDetail
import com.astranavi.app.data.model.SubscriptionDetail
import com.astranavi.app.data.model.UsageLedgerEntry
import com.astranavi.app.ui.components.ParticleBackground
import com.astranavi.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansPage(
    viewModel: PlansViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Plans & Credits", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            ParticleBackground()

            when (uiState) {
                is PlansUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                }
                is PlansUiState.Success -> {
                    val data = uiState as PlansUiState.Success
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        item { CreditBalanceCard(data.balance.credits, data.balance.tier, data.balance.monthlyCredits ?: 0, data.balance.creditsUsed ?: 0) }
                        item { CurrentPlanSection(data.subscription, data.balance.tier) }
                        item { ActivePacksSection(data.packs) }
                        item { SubscriptionCatalogSection(data.catalog.filter { it.productType == "subscription" }) }
                        item { PackCatalogSection(data.catalog.filter { it.productType != "subscription" }) }
                        item { UsageHistorySection(data.usageHistory) }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
                is PlansUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text((uiState as PlansUiState.Error).message, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadPlansData() }) { Text("Retry") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreditBalanceCard(credits: Int, tier: String, monthlyCredits: Int, creditsUsed: Int) {
    val tierColor = when (tier.lowercase()) {
        "pro" -> Color(0xFF7C3AED)
        "premium" -> Color(0xFFC8880A)
        else -> Color.Gray
    }
    val tierLabel = when (tier.lowercase()) {
        "pro" -> "PRO"
        "premium" -> "PREMIUM"
        else -> "FREE"
    }
    val remaining = monthlyCredits - creditsUsed

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).background(tierColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Navi Credits", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("$tierLabel Plan", style = MaterialTheme.typography.labelMedium, color = tierColor, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("$credits", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black), color = tierColor)
            }
            if (monthlyCredits > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Monthly allowance: $monthlyCredits", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Used: $creditsUsed | Remaining: $remaining", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (monthlyCredits > 0) creditsUsed.toFloat() / monthlyCredits.toFloat() else 0f },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                    color = tierColor,
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun CurrentPlanSection(subscription: SubscriptionDetail?, currentTier: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Current Plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            if (subscription != null) {
                val tierColor = when (subscription.tier?.lowercase()) {
                    "pro" -> Color(0xFF7C3AED)
                    "premium" -> Color(0xFFC8880A)
                    else -> Color.Gray
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = tierColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${subscription.tier?.uppercase() ?: currentTier.uppercase()}", fontWeight = FontWeight.Bold, color = tierColor)
                    Spacer(modifier = Modifier.weight(1f))
                    if (subscription.currentPeriodEnd != null) {
                        Text("Renews: ${subscription.currentPeriodEnd}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (subscription.creditsPerMonth != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${subscription.creditsPerMonth} credits/month", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Free tier — ${currentTier.uppercase()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Upgrade for more", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
private fun ActivePacksSection(packs: List<PackDetail>) {
    if (packs.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Active Packs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            packs.forEach { pack ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(pack.name ?: "Pack", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("${pack.creditsRemaining}/${pack.creditsTotal}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (pack.expiresAt != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exp: ${pack.expiresAt}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionCatalogSection(subscriptions: List<PaywallProduct>) {
    if (subscriptions.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Subscription Plans", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            subscriptions.forEach { product ->
                val productColor = try { Color(android.graphics.Color.parseColor(product.color)) } catch (_: Exception) { MaterialTheme.colorScheme.secondary }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).border(1.dp, productColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(productColor, CircleShape), contentAlignment = Alignment.Center) {
                            Text(product.tier?.uppercase()?.take(1) ?: "P", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(product.nameEn ?: "Plan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("${product.credits} credits/month", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                            val price = product.priceInr?.let { "₹${it.toInt()}" } ?: "—"
                            Text(price, fontWeight = FontWeight.Black, color = productColor, fontSize = 18.sp)
                            Text("/month", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = productColor.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(8.dp),
                            enabled = false
                        ) {
                            Text("Coming Soon", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PackCatalogSection(packs: List<PaywallProduct>) {
    if (packs.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("One-Time Packs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            packs.forEach { product ->
                val productColor = try { Color(android.graphics.Color.parseColor(product.color)) } catch (_: Exception) { MaterialTheme.colorScheme.secondary }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = productColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(product.nameEn ?: "Pack", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Text("${product.credits} credits", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        val price = product.priceInr?.let { "₹${it.toInt()}" } ?: "—"
                        Text(price, fontWeight = FontWeight.Bold, color = productColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = productColor.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            enabled = false
                        ) {
                            Text("Coming Soon", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageHistorySection(entries: List<UsageLedgerEntry>) {
    if (entries.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Usage History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            entries.take(20).forEach { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val delta = entry.creditsDelta ?: 0
                    val deltaColor = if (delta < 0) Color(0xFFEF4444) else Color(0xFF22C55E)
                    Text(entry.action ?: "—", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text("${delta}", fontWeight = FontWeight.Bold, color = deltaColor, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(entry.timestamp?.take(10) ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}