package com.astranavi.app.ui.consult

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.astranavi.app.data.model.*
import com.astranavi.app.ui.components.GlassCard
import com.astranavi.app.ui.components.bringIntoViewOnFocus
import com.astranavi.app.ui.components.shimmerEffect
import com.astranavi.app.ui.components.shimmerSweepEffect
import com.astranavi.app.ui.theme.AstroColors
import kotlinx.coroutines.delay
import androidx.activity.compose.BackHandler
import androidx.compose.ui.text.input.TransformedText
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConsultScreen(
    viewModel: ConsultViewModel,
    onOpenDrawer: () -> Unit,
    onBack: () -> Unit,
    onViewHistory: () -> Unit
) {
    val step = viewModel.step.value
    val isLoading = viewModel.isLoading.value
    val birthError = viewModel.birthDetailsError.value

    // Phase 1C: Handle internal back navigation from TopBar
    BackHandler(enabled = step != ConsultStep.BirthDetails) {
        viewModel.navigateBack()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        // Phase 1A: Cosmic Progress Tracker
        CosmicStepTracker(
            step = step, 
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            onStepClick = { index -> viewModel.jumpToStep(index) }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading && step != ConsultStep.Result) {
                ConsultSkeleton(modifier = Modifier.fillMaxSize())
            } else {
                // Phase 1B: Step Transitions with AnimatedContent
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        (slideInVertically(animationSpec = tween(400, easing = EaseOutQuart)) { it / 8 } + fadeIn(tween(400)))
                            .togetherWith(slideOutVertically(animationSpec = tween(300, easing = EaseInQuart)) { -it / 8 } + fadeOut(tween(300)))
                    },
                    label = "step_transition"
                ) { targetStep ->
                    Column(modifier = Modifier.fillMaxSize()) {
                        when (targetStep) {
                            is ConsultStep.BirthDetails -> BirthDetailsStep(viewModel, onViewHistory)
                            is ConsultStep.CategorySelection -> CategoryStep(viewModel)
                            is ConsultStep.SubCategorySelection -> SubCategoryStep(viewModel)
                            is ConsultStep.QuestionSelection -> QuestionStep(viewModel)
                            is ConsultStep.Result -> ResultStep(viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CosmicStepTracker(
    step: ConsultStep, 
    modifier: Modifier = Modifier,
    onStepClick: (Int) -> Unit = {}
) {
    val currentIndex = when (step) {
        is ConsultStep.BirthDetails -> 0
        is ConsultStep.CategorySelection -> 1
        is ConsultStep.SubCategorySelection -> 2
        is ConsultStep.QuestionSelection -> 3
        is ConsultStep.Result -> 4
    }

    val nodes = listOf("Birth", "Domain", "Focus", "Query", "Insight")
    val activeColor = MaterialTheme.colorScheme.secondary
    val inactiveColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        nodes.forEachIndexed { index, _ ->
            val isActive = index == currentIndex
            val isCompleted = index < currentIndex
            val isClickable = index <= currentIndex

            // Pulse animation for active node
            val infiniteTransition = rememberInfiniteTransition(label = "node_pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1.1f,
                targetValue = 1.4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            // Node Circle with Clickable Target
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = isClickable) { onStepClick(index) }
                    .padding(6.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(if (isActive) 12.dp else 10.dp)
                        .then(if (isActive) Modifier.scale(scale) else Modifier),
                    shape = CircleShape,
                    color = if (isActive || isCompleted) activeColor else Color.Transparent,
                    border = if (!isActive && !isCompleted) androidx.compose.foundation.BorderStroke(1.dp, inactiveColor) else null
                ) {
                    if (isActive) {
                        Box(modifier = Modifier.fillMaxSize().background(activeColor.copy(alpha = 0.3f)))
                    }
                }
            }

            // Connecting line (between nodes)
            if (index < nodes.size - 1) {
                val lineColor = if (index < currentIndex) activeColor else inactiveColor
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.5.dp)
                        .background(lineColor)
                )
            }
        }
    }
}

@Composable
fun ConsultSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(modifier = Modifier.width(180.dp).height(28.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        items(4) {
            // Phase 7B: Branded Skeleton Shimmer
            GlassCard(
                modifier = Modifier.fillMaxWidth().height(104.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect()
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.width(140.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.width(100.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    }
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).shimmerEffect())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthDetailsStep(viewModel: ConsultViewModel, onViewHistory: () -> Unit) {
    val birthError = viewModel.birthDetailsError.value
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    // Date Picker State
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Helper to format stored YYYY-MM-DD to DD-MM-YYYY for display
    val displayDob = remember(viewModel.dob.value) {
        if (viewModel.dob.value.isNotEmpty()) {
            try {
                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(viewModel.dob.value)
                parsed?.let { SimpleDateFormat("dd-MM-yyyy", Locale.US).format(it) } ?: ""
            } catch (e: Exception) { viewModel.dob.value }
        } else ""
    }

    // Phase 2D: Button Upgrade (moved to top)
    var isPressed by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        viewModel.dob.value = sdf.format(Date(millis))
                    }
                }) { Text("OK", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
            },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    headlineContentColor = MaterialTheme.colorScheme.secondary,
                    selectedDayContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedDayContentColor = MaterialTheme.colorScheme.onSecondary,
                    todayContentColor = MaterialTheme.colorScheme.secondary,
                    todayDateBorderColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Phase 2A: Header Rewrite
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            val infiniteTransition = rememberInfiniteTransition(label = "icon_rotation")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(8000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.graphicsLayer { rotationZ = rotation }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Cosmic Blueprint",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Button(
            onClick = { viewModel.fetchTree() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scale(buttonScale)
                .shimmerSweepEffect(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect { interaction ->
                        when (interaction) {
                            is androidx.compose.foundation.interaction.PressInteraction.Press -> isPressed = true
                            is androidx.compose.foundation.interaction.PressInteraction.Release -> isPressed = false
                            is androidx.compose.foundation.interaction.PressInteraction.Cancel -> isPressed = false
                        }
                    }
                }
            }
        ) {
            Text("Begin Session", fontWeight = FontWeight.ExtraBold)
        }

        if (birthError != null) {
            Text(
                text = birthError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Phase 2B & 2C: Staggered Input Fields
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500, delayMillis = 0)) + slideInVertically(tween(500, delayMillis = 0)) { it / 4 }
        ) {
            CosmicClickableField(
                value = displayDob,
                onClick = { showDatePicker = true },
                label = "Date of Birth (DD-MM-YYYY)"
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500, delayMillis = 120)) + slideInVertically(tween(500, delayMillis = 120)) { it / 4 }
        ) {
            CosmicInputField(
                value = viewModel.tob.value,
                onValueChange = { if (it.length <= 4) viewModel.tob.value = it },
                label = "Time of Birth (HHMM)",
                visualTransformation = com.astranavi.app.ui.components.TimeVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500, delayMillis = 240)) + slideInVertically(tween(500, delayMillis = 240)) { it / 4 }
        ) {
            CosmicInputField(
                value = viewModel.pob.value,
                onValueChange = { viewModel.pob.value = it },
                label = "Place of Birth"
            )
        }

        Text(
            "Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 8.dp)
        )

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500, delayMillis = 400)) +
                    slideInVertically(tween(500, delayMillis = 400)) { it / 4 }
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(16.dp)) {
                    var languageMenuExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = languageMenuExpanded,
                        onExpandedChange = { languageMenuExpanded = !languageMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = viewModel.selectedLanguage.value,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Preferred Language") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageMenuExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor().bringIntoViewOnFocus(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false }
                        ) {
                            listOf("English", "Hindi", "Marathi", "Gujarati", "Tamil").forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang) },
                                    onClick = {
                                        viewModel.selectedLanguage.value = lang
                                        languageMenuExpanded = false
                                    }
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
fun CosmicInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val shadowColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isFocused) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = shadowColor,
                        spotColor = shadowColor
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewOnFocus()
                .onFocusChanged { isFocused = it.isFocused },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.secondary
            )
        )
    }
}

@Composable
fun CosmicClickableField(
    value: String,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = false, // Handle clicks via parent clickable
            modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Transparent,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                disabledContainerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun CategoryStep(viewModel: ConsultViewModel) {
    val tree = viewModel.tree.value ?: return
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Select Domain",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Phase 3D: Orbit Badge
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                    val infiniteTransition = rememberInfiniteTransition(label = "orbit")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(8000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )
                    
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val strokeWidthPx = with(LocalDensity.current) { 2.dp.toPx() }

                    Canvas(modifier = Modifier.size(80.dp)) {
                        drawArc(
                            color = secondaryColor.copy(alpha = 0.3f),
                            startAngle = rotation,
                            sweepAngle = 90f,
                            useCenter = false,
                            style = Stroke(width = strokeWidthPx)
                        )
                    }
                    
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            tree.life_stage.replace("_", " ").uppercase(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        itemsIndexed(tree.primary) { index, category ->
            // Phase 3C: Staggered Card Entrance
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, delayMillis = index * 80)) +
                        slideInVertically(tween(600, delayMillis = index * 80)) { it / 3 }
            ) {
                val isHighlighted = viewModel.highlightedCategory.value == category
                CosmicCategoryCard(category, index, isHighlighted) { viewModel.selectCategory(category) }
            }
        }
    }
}

@Composable
fun CosmicCategoryCard(category: Category, index: Int, isHighlighted: Boolean, onClick: () -> Unit) {
    // Phase 3B: Idle Floating Animation
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000 + index * 800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    val planetColor = when {
        category.key.contains("career", ignoreCase = true) -> AstroColors.Saturn
        category.key.contains("love", ignoreCase = true) -> AstroColors.Venus
        category.key.contains("health", ignoreCase = true) -> AstroColors.Mars
        category.key.contains("wealth", ignoreCase = true) || category.key.contains("finance", ignoreCase = true) -> AstroColors.Jupiter
        else -> AstroColors.Default
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = floatOffset.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        border = if (isHighlighted) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(planetColor, planetColor.copy(alpha = 0.6f))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(category.icon, fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    category.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Personalized guidance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SubCategoryStep(viewModel: ConsultViewModel) {
    val category = viewModel.selectedCategory.value ?: return
    var expandedSubKey by remember { mutableStateOf<String?>(null) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding().navigationBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Focus area for ${category.label}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        items(category.subs) { sub ->
            val isExpanded = expandedSubKey == sub.key
            val rotation by animateFloatAsState(if (isExpanded) 90f else 0f, label = "arrow_rotation")

            // Phase 4A: Accordion-Style Expanding Cards
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .clickable { expandedSubKey = if (isExpanded) null else sub.key },
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            sub.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.graphicsLayer { rotationZ = rotation }
                        )
                    }
                    
                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Navi will analyze your chart to provide deep insights on this specific focus.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.selectSubCategory(sub) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Select this Focus", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionStep(viewModel: ConsultViewModel) {
    val sub = viewModel.selectedSubCategory.value ?: return
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Pick a Query", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Text("Navi will analyze your chart to answer this specific question.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        itemsIndexed(sub.questions) { index, question ->
            val isSelected = viewModel.selectedQuestion.value == question
            
            // Phase 5A: Destiny Prompt Cards
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, delayMillis = index * 100)) + slideInVertically(tween(600, delayMillis = index * 100)) { it / 4 }
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (viewModel.selectedQuestion.value.isNotEmpty() && !isSelected) 0.5f else 1f)
                        .clickable { viewModel.selectQuestion(question) },
                    shape = RoundedCornerShape(24.dp),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Gold left stripe
                        Box(modifier = Modifier.width(4.dp).height(80.dp).background(MaterialTheme.colorScheme.secondary))
                        
                        Box(modifier = Modifier.padding(24.dp)) {
                            // Subtle constellation line background could be added with Canvas here
                            Text(question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        item {
            Text("Consultation Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Phase 5C: Context Box Upgrade
            CosmicInputField(
                value = viewModel.customNote.value,
                onValueChange = { viewModel.customNote.value = it },
                label = "Tell Navi your situation..."
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Response Tone", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
            
            // Phase 5D: Emotionally Distinct Tone Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp), 
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                listOf("warm", "direct", "spiritual").forEach { tone ->
                    ToneChip(
                        tone = tone,
                        isSelected = viewModel.selectedTone.value == tone,
                        onClick = { viewModel.selectedTone.value = tone }
                    )
                }
            }
        }
        
        item {
            // Phase 5E: Dynamic Custom Question Button
            val hasContext = viewModel.customNote.value.isNotBlank()
            Button(
                onClick = { viewModel.selectQuestion("Other") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp)
                    .then(if (hasContext) Modifier.shimmerSweepEffect() else Modifier),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasContext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (hasContext) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Ask Custom Question", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun ToneChip(tone: String, isSelected: Boolean, onClick: () -> Unit) {
    val shape = when(tone) {
        "warm" -> RoundedCornerShape(16.dp)
        "direct" -> RoundedCornerShape(4.dp)
        "spiritual" -> CircleShape
        else -> RoundedCornerShape(12.dp)
    }
    
    val color = when(tone) {
        "warm" -> MaterialTheme.colorScheme.secondary
        "direct" -> MaterialTheme.colorScheme.outlineVariant
        "spiritual" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }

    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = shape,
        color = if (isSelected) color else Color.Transparent,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)) else null,
        contentColor = if (isSelected) Color.White else color
    ) {
        Text(
            tone.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ResultStep(viewModel: ConsultViewModel) {
    val result = viewModel.consultResult.value
    var revealStarted by remember { mutableStateOf(false) }
    
    LaunchedEffect(result) {
        if (result != null && !revealStarted) {
            delay(600) // Phase 6B: Ceremonial Reveal pacing
            revealStarted = true
        }
    }
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(800)) + scaleIn(initialScale = 0.95f)
        ) {
            Column {
                Box(
                    modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.secondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Cosmic Insight", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text(
                    if (viewModel.selectedQuestion.value == "Other") viewModel.customNote.value else viewModel.selectedQuestion.value, 
                    color = MaterialTheme.colorScheme.secondary, 
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (result != null) {
            // Phase 6C: Sacred Document Style Result Card
            AnimatedVisibility(
                visible = revealStarted,
                enter = fadeIn(tween(1000)) + slideInVertically { it / 10 }
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = result,
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Phase 6A: Constellation Loader
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val strokeWidthPx = with(LocalDensity.current) { 2.dp.toPx() }
            val dotRadiusPx = with(LocalDensity.current) { 6.dp.toPx() }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 48.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                    val infiniteTransition = rememberInfiniteTransition(label = "loader")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
                        label = "rotation"
                    )
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = secondaryColor.copy(alpha = 0.1f),
                            style = Stroke(width = strokeWidthPx)
                        )
                        // Orbiting dots
                        val radius = size.width / 2
                        val angleRad = Math.toRadians(rotation.toDouble())
                        val x = (center.x + radius * Math.cos(angleRad)).toFloat()
                        val y = (center.y + radius * Math.sin(angleRad)).toFloat()
                        drawCircle(color = secondaryColor, radius = dotRadiusPx, center = Offset(x, y))
                    }
                    
                    Icon(
                        Icons.Default.AutoAwesome, 
                        contentDescription = null, 
                        tint = secondaryColor,
                        modifier = Modifier.size(40.dp).scale(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                val loadingTexts = listOf(
                    "Consulting planetary alignments...",
                    "Reading cosmic influences...",
                    "Interpreting your karmic patterns...",
                    "Channeling your celestial guidance..."
                )
                var textIndex by remember { mutableStateOf(0) }
                LaunchedEffect(Unit) {
                    while(true) {
                        delay(3000)
                        textIndex = (textIndex + 1) % loadingTexts.size
                    }
                }
                
                AnimatedContent(targetState = loadingTexts[textIndex], label = "loading_text") { text ->
                    Text(
                        text, 
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f), 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        // Phase 6E: Explore Another Insight Button
        Button(
            onClick = { viewModel.navigateBack() },
            modifier = Modifier.fillMaxWidth().height(56.dp).shimmerSweepEffect(),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Explore Another Insight", fontWeight = FontWeight.ExtraBold)
        }
    }
}
