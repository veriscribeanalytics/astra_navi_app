package com.astranavi.app.ui.splash

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.R
import com.astranavi.app.ui.theme.LocalSemanticColors

@Composable
fun BrandMark(
    brandColor: Color,
    shadowColor: Color,
    fontSize: Float = 44f,
    letterSpacing: Float = 3f,
    taglineSpacer: Float = 8f
) {
    val semanticColors = LocalSemanticColors.current
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(
            text = "AstraNavi",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = fontSize.sp,
                letterSpacing = letterSpacing.sp,
                shadow = Shadow(
                    color = shadowColor.copy(alpha = 0.6f),
                    offset = Offset(0f, 4f),
                    blurRadius = 8f
                )
            ),
            color = brandColor
        )
        Spacer(modifier = Modifier.height(taglineSpacer.dp))
        Text(
            text = stringResource(R.string.splash_tagline),
            style = MaterialTheme.typography.titleMedium.copy(
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Normal
            ),
            color = semanticColors.brandTextColor.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
    }
}