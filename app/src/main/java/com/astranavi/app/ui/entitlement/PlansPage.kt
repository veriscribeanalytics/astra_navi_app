package com.astranavi.app.ui.entitlement

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.ui.components.responsiveMetrics
import com.astranavi.app.ui.components.titleCase
import com.astranavi.app.data.model.PaywallProduct
import com.astranavi.app.data.model.PackDetail
import com.astranavi.app.data.model.SubscriptionDetail
import com.astranavi.app.data.model.UsageLedgerEntry
import com.astranavi.app.data.model.getLocalizedName
import com.astranavi.app.data.model.getLocalizedDescription
import androidx.compose.ui.res.stringResource
import com.astranavi.app.R
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Composable
fun PlansPage(
    viewModel: PlansViewModel,
    onBack: () -> Unit
) {
    com.astranavi.app.util.SecureScreen()
    val uiState by viewModel.uiState
    val showHistory by viewModel.showUsageHistory
    var selectedTab by remember { mutableIntStateOf(0) } // 0 for Subscriptions, 1 for Packs
    var selectedPackForDetail by remember { mutableStateOf<PaywallProduct?>(null) }
    val metrics = responsiveMetrics()

    // Plans data is refreshed automatically in the ViewModel init block

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is PlansUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            }
            is PlansUiState.Success -> {
                val data = uiState as PlansUiState.Success
                
                if (metrics.useTabletTwoPane) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = metrics.pagePadding)
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(metrics.twoPaneGap),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left Pane: CreditBalanceCard
                        Column(
                            modifier = Modifier.weight(metrics.twoPaneLeftWeight),
                            verticalArrangement = Arrangement.spacedBy(metrics.twoPaneGap)
                        ) {
                            CreditBalanceCard(data.balance, data.subscription)
                        }
                        
                        // Right Pane: PlanTabSelector + cards
                        Column(
                            modifier = Modifier
                                .weight(metrics.twoPaneRightWeight)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(metrics.twoPaneGap)
                        ) {
                            if (showHistory) {
                                UsageHistorySection(data.usageHistory)
                            } else {
                                PlanTabSelector(
                                    selectedTab = selectedTab,
                                    onTabSelected = { selectedTab = it }
                                )
                                
                                if (selectedTab == 0) {
                                    SubscriptionComparisonSection(
                                        catalog = data.catalog.filter { it.productType == "subscription" },
                                        currentTier = data.balance.tier
                                    )
                                } else {
                                    PackCatalogSection(
                                        packs = data.catalog.filter { it.productType != "subscription" },
                                        onPackClick = { selectedPackForDetail = it }
                                    )
                                    ActivePacksSection(data.packs)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = metrics.pagePadding),
                        verticalArrangement = Arrangement.spacedBy(if (metrics.isCompactHeight) 16.dp else 20.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = metrics.listBottomPadding)
                    ) {
                        if (showHistory) {
                            item { UsageHistorySection(data.usageHistory) }
                        } else {
                            item { CreditBalanceCard(data.balance, data.subscription) }
                            
                            item {
                                PlanTabSelector(
                                    selectedTab = selectedTab,
                                    onTabSelected = { selectedTab = it }
                                )
                            }

                            if (selectedTab == 0) {
                                item {
                                    SubscriptionComparisonSection(
                                        catalog = data.catalog.filter { it.productType == "subscription" },
                                        currentTier = data.balance.tier
                                    )
                                }
                            } else {
                                item {
                                    PackCatalogSection(
                                        packs = data.catalog.filter { it.productType != "subscription" },
                                        onPackClick = { selectedPackForDetail = it }
                                    )
                                }
                                item { ActivePacksSection(data.packs) }
                            }
                        }
                    }
                }
            }
            is PlansUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text((uiState as PlansUiState.Error).message, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadPlansData() }) { Text(stringResource(R.string.plans_btn_retry)) }
                    }
                }
            }
        }

        // Pack Detail Popup
        selectedPackForDetail?.let { pack ->
            PackDetailDialog(
                pack = pack,
                onDismiss = { selectedPackForDetail = null }
            )
        }
    }
}

@Composable
private fun PlanTabSelector(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            TabItem(
                label = stringResource(R.string.plans_tab_subscriptions),
                isSelected = selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(0) }
            )
            TabItem(
                label = stringResource(R.string.plans_tab_add_on_packs),
                isSelected = selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(1) }
            )
        }
    }
}

@Composable
private fun TabItem(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SubscriptionComparisonSection(catalog: List<PaywallProduct>, currentTier: String) {
    val metrics = responsiveMetrics()
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.plans_choose_plan_header),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        val tiers = listOf("free", "pro", "premium")
        val sortedCatalog = tiers.mapNotNull { tier ->
            catalog.find { it.tier?.lowercase() == tier }
        }

        if (metrics.isTabletWidth) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(metrics.twoPaneGap)
            ) {
                sortedCatalog.forEach { product ->
                    Box(modifier = Modifier.weight(1f)) {
                        SubscriptionCard(product, isCurrent = product.tier?.lowercase() == currentTier.lowercase())
                    }
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(metrics.twoPaneGap),
                contentPadding = PaddingValues(end = metrics.pagePadding),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sortedCatalog, key = { it.productId ?: it.hashCode() }) { product ->
                    SubscriptionCard(product, isCurrent = product.tier?.lowercase() == currentTier.lowercase())
                }
            }
        }
    }
}

@Composable
private fun SubscriptionCard(product: PaywallProduct, isCurrent: Boolean) {
    val colorHex = (product.metadata?.jsonObject?.get("color")?.jsonPrimitive?.contentOrNull) ?: product.color ?: "#7C3AED"
    val productColor = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) { MaterialTheme.colorScheme.primary }
    val metrics = responsiveMetrics()

    Card(
        modifier = Modifier
            .then(if (metrics.isTabletWidth) Modifier.fillMaxWidth() else Modifier.width(if (metrics.isCompactWidth || metrics.isLargeFont) 256.dp else 280.dp))
            .heightIn(min = 420.dp)
            .border(
                width = if (isCurrent) 2.dp else 1.dp,
                color = if (isCurrent) productColor else productColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) productColor.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(if (metrics.isCompactWidth || metrics.isLargeFont) 20.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(56.dp).background(productColor.copy(alpha = 0.1f), CircleShape).border(1.dp, productColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(product.tier?.lowercase()) {
                        "free" -> Icons.Default.Eco
                        "pro" -> Icons.Default.Bolt
                        "premium" -> Icons.Default.AutoAwesome
                        else -> Icons.Default.Star
                    },
                    contentDescription = null,
                    tint = productColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                (product.getLocalizedName().ifEmpty { stringResource(R.string.plans_default_pack_name) }).titleCase(),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                letterSpacing = if (metrics.isCompactWidth || metrics.isLargeFont) 0.5.sp else 1.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                val price = product.priceInr?.let { "₹${it.toInt()}" } ?: stringResource(R.string.plans_free)
                Text(
                    price,
                    fontWeight = FontWeight.Black,
                    fontSize = if (metrics.isCompactWidth || metrics.isLargeFont) 28.sp else 32.sp,
                    color = productColor
                )
                if (product.priceInr != null) {
                    Text(
                        stringResource(R.string.plans_price_per_month),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.plans_credits_per_month_format, product.credits ?: 0),
                style = MaterialTheme.typography.labelSmall,
                color = productColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.alpha(0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val description = product.getLocalizedDescription()
                val features = if (description.isNotEmpty()) description.split(",") else emptyList()
                features.forEach { feature ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = productColor, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(feature.trim(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.surfaceVariant else productColor.copy(alpha = 0.5f),
                    contentColor = if (isCurrent) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                    disabledContainerColor = if (isCurrent) MaterialTheme.colorScheme.surfaceVariant else productColor.copy(alpha = 0.5f),
                    disabledContentColor = if (isCurrent) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = false
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isCurrent) stringResource(R.string.plans_btn_current_plan) else stringResource(R.string.paywall_coming_soon),
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PackCatalogSection(packs: List<PaywallProduct>, onPackClick: (PaywallProduct) -> Unit) {
    if (packs.isEmpty()) return
    val metrics = responsiveMetrics()
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.plans_quick_top_ups_header),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        packs.forEach { product ->
            val colorHex = (product.metadata?.jsonObject?.get("color")?.jsonPrimitive?.contentOrNull) ?: product.color ?: "#10B981"
            val productColor = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) { MaterialTheme.colorScheme.secondary }
            
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onPackClick(product) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(metrics.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).background(productColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = productColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.getLocalizedName().ifEmpty { stringResource(R.string.plans_default_pack_name) }, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(stringResource(R.string.paywall_credits_format, product.credits ?: 0), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val price = product.priceInr?.let { "₹${it.toInt()}" } ?: "—"
                    Text(price, fontWeight = FontWeight.Black, color = productColor, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun PackDetailDialog(pack: PaywallProduct, onDismiss: () -> Unit) {
    val colorHex = (pack.metadata?.jsonObject?.get("color")?.jsonPrimitive?.contentOrNull) ?: pack.color ?: "#10B981"
    val productColor = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) { MaterialTheme.colorScheme.secondary }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = productColor.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                    disabledContainerColor = productColor.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = false
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.paywall_coming_soon), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.plans_btn_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(64.dp).background(productColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = productColor, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(pack.getLocalizedName().ifEmpty { stringResource(R.string.plans_default_top_up_name) }, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.plans_navi_credits_format, pack.credits ?: 0),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = productColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    pack.getLocalizedDescription().ifEmpty { stringResource(R.string.plans_default_pack_desc) },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (pack.category == "chat") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.plans_no_expiry_msg),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

@Composable
private fun CreditBalanceCard(balance: com.astranavi.app.data.model.BalanceResponse, subscription: SubscriptionDetail?) {
    val metrics = responsiveMetrics()
    val tierColor = when (balance.tier.lowercase()) {
        "pro" -> Color(0xFF7C3AED)
        "premium" -> Color(0xFFC8880A)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = tierColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = if (balance.tier.lowercase() == "free") stringResource(R.string.plans_free) else balance.tier.titleCase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = tierColor
                            )
                        }
                        if (subscription?.currentPeriodEnd != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.plans_renews_format, subscription.currentPeriodEnd.take(10)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.plans_total_balance), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Text("${balance.totalCreditsRemaining}", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
                }
                Box(
                    modifier = Modifier.size(48.dp).background(tierColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = tierColor, modifier = Modifier.size(24.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.alpha(0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.plans_from_plan), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${balance.subscriptionCreditsRemaining}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.plans_from_packs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${balance.packCreditsRemaining}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ActivePacksSection(packs: List<PackDetail>) {
    if (packs.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.plans_active_top_ups_header), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
        packs.forEach { pack ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(pack.getLocalizedName().ifEmpty { stringResource(R.string.plans_default_pack_name) }, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.plans_credits_left_format, pack.creditsRemaining ?: 0), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun UsageHistorySection(entries: List<UsageLedgerEntry>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.plans_usage_history_header),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.plans_no_recent_usage), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    entries.take(30).forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val delta = entry.creditsDelta ?: 0
                            val deltaColor = if (delta < 0) Color(0xFFEF4444) else Color(0xFF22C55E)
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.action?.replace("_", " ")?.uppercase() ?: "—", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                val date = (entry.timestamp ?: entry.createdAt ?: entry.createdAtAlt ?: "").take(16).replace("T", " ")
                                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            Text(
                                if (delta > 0) "+$delta" else "$delta",
                                fontWeight = FontWeight.Black,
                                color = deltaColor,
                                fontSize = 16.sp
                            )
                        }
                        if (entry != entries.last()) {
                            HorizontalDivider(modifier = Modifier.alpha(0.05f))
                        }
                    }
                }
            }
        }
    }
}

