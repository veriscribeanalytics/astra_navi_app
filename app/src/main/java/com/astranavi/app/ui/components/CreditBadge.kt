package com.astranavi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.R
import com.astranavi.app.util.LocaleFormatter
import com.astranavi.app.util.currentAppLocale

@Composable
fun CreditBadge(
    credits: Int,
    tier: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tierColor = when (tier.lowercase()) {
        "pro" -> Color(0xFF7C3AED)
        "premium" -> Color(0xFFC8880A)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountBalanceWallet,
                contentDescription = stringResource(R.string.common_credits_badge_description),
                tint = tierColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                LocaleFormatter.number(credits, currentAppLocale()),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = tierColor
            )
        }
    }
}