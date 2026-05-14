package com.astranavi.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.astranavi.app.ui.theme.LocalSemanticColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Global utility for applying a shimmering loading effect to any Compose modifier.
 * Creates an infinite left-to-right animated gradient over the component.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmer_anim"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
    background(brush)
}

/**
 * Premium CTA shimmer — a bright highlight sweep that moves left-to-right every 3s.
 * Unlike shimmerEffect (loading), this overlays a translucent white sweep on top of
 * the existing content, giving a "polish shine" feel.
 */
fun Modifier.shimmerSweepEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "sweep_transition")
    val sweepAnim by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "sweep_anim"
    )

    this.then(Modifier.drawWithContent {
        drawContent()
        val sweepWidth = size.width * 0.3f
        val sweepX = sweepAnim * size.width
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0f),
                    Color.White.copy(alpha = 0.18f),
                    Color.White.copy(alpha = 0f)
                ),
                startX = sweepX - sweepWidth,
                endX = sweepX + sweepWidth
            )
        )
    })
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bringIntoViewOnFocus(delayMillis: Long = 220L): Modifier = composed {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    this
        .bringIntoViewRequester(bringIntoViewRequester)
        .onFocusEvent { focusState ->
            if (focusState.isFocused) {
                scope.launch {
                    delay(delayMillis)
                    bringIntoViewRequester.bringIntoView()
                }
            }
        }
}

@Composable
fun AtmosphericGlowLayer(
    accentColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 260.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .drawBehind {
                val radius = maxOf(size.width, size.height) * 0.95f
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.46f),
                            accentColor.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, 0f),
                        radius = radius
                    )
                )
            }
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val semanticColors = LocalSemanticColors.current
    
    val cardColors = CardDefaults.cardColors(
        containerColor = semanticColors.glassSurface,
    )
    
    val finalBorder = border ?: BorderStroke(1.dp, semanticColors.glassBorder)
    
    if (onClick != null) {
        Card(
            modifier = modifier,
            shape = shape,
            colors = cardColors,
            border = finalBorder,
            onClick = onClick,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = cardColors,
            border = finalBorder,
            content = content
        )
    }
}

class TimeVisualTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0, 4) else text.text
        val hasColon = trimmed.length > 2
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 && hasColon) out += ":"
        }

        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset == 2) return if (hasColon) 3 else 2
                if (offset <= 4) return if (hasColon) offset + 1 else offset
                return if (hasColon) 5 else 4
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (!hasColon) {
                    if (offset >= trimmed.length) return trimmed.length
                    return offset
                }
                if (offset <= 2) return offset
                if (offset == 3) return 2
                if (offset <= 5) return offset - 1
                return 4
            }
        }
        return androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(out), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstroDatePickerField(
    value: String,              // YYYY-MM-DD format (API format)
    onValueChange: (String) -> Unit,  // returns YYYY-MM-DD
    label: String = "Date of Birth",
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    val displayDob = remember(value) {
        if (value.isNotEmpty()) {
            try {
                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(value)
                parsed?.let { SimpleDateFormat("dd-MM-yyyy", Locale.US).format(it) } ?: ""
            } catch (e: Exception) { value }
        } else ""
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        onValueChange(sdf.format(Date(millis)))
                    }
                }) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    OutlinedTextField(
        value = displayDob,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = modifier
            .bringIntoViewOnFocus()
            .pointerInput(Unit) {
                detectTapGestures {
                    showDatePicker = true
                }
            },
        shape = shape,
        isError = isError,
        supportingText = supportingText,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Select Date",
                modifier = Modifier.clickable { showDatePicker = true }
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.secondary,
            unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.secondary
        )
    )
}

@Composable
fun AstroTimePickerField(
    value: String,              // HH:MM or HHMM format
    onValueChange: (String) -> Unit,  // returns HH:MM
    label: String = "Time of Birth",
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    // Strip colon for internal editing if it exists
    val internalValue = value.replace(":", "")
    
    OutlinedTextField(
        value = internalValue,
        onValueChange = { 
            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                if (it.length == 4) {
                    val hh = it.substring(0, 2)
                    val mm = it.substring(2, 4)
                    onValueChange("$hh:$mm")
                } else {
                    onValueChange(it)
                }
            }
        },
        label = { Text(label) },
        modifier = modifier.bringIntoViewOnFocus(),
        shape = shape,
        isError = isError,
        supportingText = supportingText,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = TimeVisualTransformation(),
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Time"
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.secondary,
            unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.secondary
        )
    )
}
