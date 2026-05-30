package com.astranavi.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LanguageOption(
    val code: String,
    val label: String,
    val flag: String
)

val SupportedLanguages = listOf(
    LanguageOption("en", "English", "🇬🇧"),
    LanguageOption("hi", "हिंदी", "🇮🇳"),
    LanguageOption("bn", "বাংলা", "🇮🇳"),
    LanguageOption("gu", "ગુજરાતી", "🇮🇳"),
    LanguageOption("kn", "ಕನ್ನಡ", "🇮🇳"),
    LanguageOption("ml", "മലയാളം", "🇮🇳"),
    LanguageOption("mr", "मराठी", "🇮🇳"),
    LanguageOption("pa", "ਪੰਜਾਬੀ", "🇮🇳"),
    LanguageOption("ta", "தமிழ்", "🇮🇳"),
    LanguageOption("te", "తెలుగు", "🇮🇳"),
    LanguageOption("ko", "한국어", "🇰🇷")
)

private fun resolveLanguage(code: String?): LanguageOption =
    SupportedLanguages.firstOrNull { it.code.equals(code, ignoreCase = true) }
        ?: SupportedLanguages.first()

@Composable
fun LanguageChip(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val current = resolveLanguage(currentLanguage)
    val accent = MaterialTheme.colorScheme.onSurface

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.clickable { expanded = true },
            shape = RoundedCornerShape(20.dp),
            color = accent.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    current.flag,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    current.code.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = accent
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Change language",
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SupportedLanguages.forEach { option ->
                val isSelected = option.code.equals(currentLanguage, ignoreCase = true)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(option.flag, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                option.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        if (!isSelected) onLanguageSelected(option.code)
                    }
                )
            }
        }
    }
}
