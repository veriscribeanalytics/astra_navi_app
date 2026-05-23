package com.astranavi.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.data.model.PaywallCardData
import com.astranavi.app.data.model.PaywallProduct
import com.astranavi.app.data.model.getLocalizedTitle
import com.astranavi.app.data.model.getLocalizedDescription
import com.astranavi.app.data.model.getLocalizedName
import androidx.compose.ui.res.stringResource
import com.astranavi.app.R

@Composable
fun PaywallCard(
    paywall: PaywallCardData,
    isVisible: Boolean,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
    ) {
        if (paywall.isSoft) {
            PaywallOverlay(paywall = paywall, onDismiss = onDismiss, modifier = modifier)
        } else {
            PaywallFullBlock(paywall = paywall, modifier = modifier)
        }
    }
}

@Composable
fun PaywallOverlay(
    paywall: PaywallCardData,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val accentColor = try { Color(android.graphics.Color.parseColor(paywall.color)) } catch (_: Exception) { MaterialTheme.colorScheme.secondary }
    val metrics = responsiveMetrics()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (paywall.icon != null) {
                    Text(paywall.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(paywall.getLocalizedTitle().ifEmpty { stringResource(R.string.paywall_premium_feature) }, fontWeight = FontWeight.Bold, color = accentColor, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                    if (paywall.badge != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = accentColor.copy(alpha = 0.2f),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(paywall.badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                if (onDismiss != null) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.paywall_dismiss), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(paywall.getLocalizedDescription(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (paywall.suggestedProducts?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(12.dp))
                SuggestedProductsRow(products = paywall.suggestedProducts!!, accentColor = accentColor)
            }
        }
    }
}

@Composable
fun PaywallFullBlock(
    paywall: PaywallCardData,
    modifier: Modifier = Modifier
) {
    val accentColor = try { Color(android.graphics.Color.parseColor(paywall.color)) } catch (_: Exception) { MaterialTheme.colorScheme.secondary }
    val metrics = responsiveMetrics()

    Box(
        modifier = modifier.fillMaxWidth().heightIn(max = if (metrics.isCompactHeight || metrics.isLargeFont) 560.dp else 680.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = if (metrics.isCompactWidth) 0.dp else 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, accentColor.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(if (metrics.isCompactWidth || metrics.isLargeFont) 20.dp else 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(if (metrics.isCompactHeight || metrics.isLargeFont) 52.dp else 64.dp).background(accentColor.copy(alpha = 0.15f), CircleShape).border(2.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = accentColor, modifier = Modifier.size(if (metrics.isCompactHeight || metrics.isLargeFont) 28.dp else 32.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (paywall.icon != null) {
                    Text(paywall.icon, fontSize = if (metrics.isCompactHeight || metrics.isLargeFont) 26.sp else 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(paywall.getLocalizedTitle().ifEmpty { stringResource(R.string.paywall_premium_feature) }, fontWeight = FontWeight.Black, color = accentColor, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, maxLines = 3)
                if (paywall.badge != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.2f)
                    ) {
                        Text(paywall.badge, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accentColor, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(paywall.getLocalizedDescription(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                if (paywall.suggestedProducts?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(20.dp))
                    SuggestedProductsColumn(products = paywall.suggestedProducts!!, accentColor = accentColor)
                }
            }
        }
    }
}

@Composable
private fun SuggestedProductsRow(products: List<PaywallProduct>, accentColor: Color) {
    val metrics = responsiveMetrics()

    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        products.take(2).forEach { product ->
            val productColor = try { Color(android.graphics.Color.parseColor(product.color)) } catch (_: Exception) { accentColor }
            Card(
                modifier = Modifier.fillMaxWidth(if (metrics.isCompactWidth || metrics.isLargeFont) 1f else 0.48f),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = productColor.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, productColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(product.getLocalizedName(), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, textAlign = TextAlign.Center)
                    Text(stringResource(R.string.paywall_credits_format, product.credits ?: 0), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val price = product.priceInr?.let { "₹${it.toInt()}" } ?: "—"
                    Text(price, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = productColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = productColor.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        enabled = false
                    ) {
                        Text(stringResource(R.string.paywall_coming_soon), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestedProductsColumn(products: List<PaywallProduct>, accentColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        products.take(3).forEach { product ->
            val productColor = try { Color(android.graphics.Color.parseColor(product.color)) } catch (_: Exception) { accentColor }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = productColor.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, productColor.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.getLocalizedName().ifEmpty { stringResource(R.string.plans_default_pack_name) }, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(stringResource(R.string.paywall_credits_format, product.credits ?: 0), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (product.tier != null) {
                            Surface(shape = RoundedCornerShape(4.dp), color = productColor.copy(alpha = 0.2f)) {
                                Text(product.tier.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = productColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                    val price = product.priceInr?.let { "₹${it.toInt()}" } ?: "—"
                    Text(price, fontWeight = FontWeight.Black, fontSize = 18.sp, color = productColor)
                }
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(bottom = 10.dp), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = productColor),
                        shape = RoundedCornerShape(8.dp),
                        enabled = false
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.paywall_coming_soon), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
