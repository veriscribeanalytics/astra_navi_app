package com.astranavi.app.ui.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.astranavi.app.LocalTopBarTitle
import com.astranavi.app.ui.components.ApplyRootGlow
import com.astranavi.app.ui.components.GlowColors
import com.astranavi.app.ui.theme.AstroColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    viewModel: TestViewModel,
    onBack: () -> Unit = {}
) {
    val setTitle = LocalTopBarTitle.current
    LaunchedEffect(Unit) {
        setTitle?.invoke("API Test Page")
    }

    val clipboardManager = LocalClipboardManager.current

    val sign by viewModel.sign
    val name by viewModel.name
    val lang by viewModel.lang
    val jsonResponse by viewModel.jsonResponse
    val isLoading by viewModel.isLoading
    val error by viewModel.error

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Box(modifier = Modifier.fillMaxSize()) {
        // Apply cosmic glowing backgrounds similar to other screens
        ApplyRootGlow(
            GlowColors(
                accent = AstroColors.Default.copy(alpha = 0.2f),
                deep = if (isDark) Color(0xFF0B071A) else Color(0xFFFAF7F2),
                radial = AstroColors.Jupiter.copy(alpha = 0.15f)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Daily Horoscope Timings JSON Tester",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Input Card
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, AstroColors.Default.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Query Parameters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AstroColors.Default
                    )

                    OutlinedTextField(
                        value = sign,
                        onValueChange = { viewModel.onSignChange(it) },
                        label = { Text("Zodiac Sign (Optional)") },
                        placeholder = { Text("Leave blank for personalized user chart") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AstroColors.Default,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("User Name (Optional)") },
                        placeholder = { Text("Leave blank for personalized name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AstroColors.Default,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    OutlinedTextField(
                        value = lang,
                        onValueChange = { viewModel.onLangChange(it) },
                        label = { Text("Language Code (Optional)") },
                        placeholder = { Text("e.g. en, hi, ko...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AstroColors.Default,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.fetchTimings() },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AstroColors.Default,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Fetching...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Fetch")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Test Request", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Results Section
            if (error.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Error Encountered",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (jsonResponse.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF130E26) else Color(0xFFF5F2EB)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "JSON Response Body",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFD1C7EC) else Color(0xFF4A3B6A)
                            )
                            Button(
                                onClick = { clipboardManager.setText(AnnotatedString(jsonResponse)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AstroColors.Default.copy(alpha = 0.15f),
                                    contentColor = AstroColors.Default
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy JSON",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        SelectionContainer {
                            Text(
                                text = jsonResponse,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = if (isDark) Color(0xFFE4DFEC) else Color(0xFF231942),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (isDark) Color(0xFF090614) else Color(0xFFEBE6D8),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

