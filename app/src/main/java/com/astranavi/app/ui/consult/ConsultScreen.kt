package com.astranavi.app.ui.consult

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import com.astranavi.app.ui.components.PreviewMultiDevice
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
import androidx.compose.ui.res.stringResource
import com.astranavi.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.astranavi.app.data.model.*
import com.astranavi.app.ui.components.GlassCard
import com.astranavi.app.ui.components.bringIntoViewOnFocus
import com.astranavi.app.ui.components.responsiveMetrics
import com.astranavi.app.ui.components.ShimmerBlock
import com.astranavi.app.ui.components.shimmerEffect
import com.astranavi.app.ui.components.shimmerSweepEffect
import com.astranavi.app.ui.components.SupportedLanguages
import com.astranavi.app.ui.components.titleCase
import com.astranavi.app.ui.theme.AstroColors
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.platform.LocalClipboard
import com.astranavi.app.util.setSensitiveText
import androidx.compose.ui.graphics.luminance
import com.astranavi.app.ui.components.GlowColors
import com.astranavi.app.ui.components.ApplyRootGlow
import com.astranavi.app.ui.components.ScoreColors
import com.astranavi.app.ui.navigation.LocalTopBarConfigOverride
import com.astranavi.app.ui.navigation.TopAppBarConfig
import com.astranavi.app.ui.navigation.RightAction

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.PressInteraction
import com.astranavi.app.ui.components.TimeVisualTransformation
import kotlinx.coroutines.launch
import com.astranavi.app.util.LocaleFormatter
import com.astranavi.app.util.SecureScreen
import com.astranavi.app.util.currentAppLocale
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConsultScreen(
    viewModel: ConsultViewModel,
    onOpenDrawer: () -> Unit,
    onBack: () -> Unit,
    onViewHistory: () -> Unit
) {
    SecureScreen()
    val step = viewModel.step.value
    val isLoading = viewModel.isLoading.value
    val metrics = responsiveMetrics()

    val selectedCategory = viewModel.selectedCategory.value
    val showGlow = step != ConsultStep.BirthDetails && step != ConsultStep.CategorySelection

    // Override top bar config to hide language chip on CategorySelection step
    val topBarConfigOverride = remember(step) {
        if (step == ConsultStep.CategorySelection) {
            TopAppBarConfig(
                showBackButton = false,
                showLanguageChip = false,
                rightAction = RightAction.HISTORY
            )
        } else null
    }

    CompositionLocalProvider(LocalTopBarConfigOverride provides topBarConfigOverride) {
        if (showGlow && selectedCategory != null) {
            val isDarkTheme = MaterialTheme.colorScheme.background.luminance() <= 0.5f
            val glowColors = remember(selectedCategory, isDarkTheme) {
                val areaKey = when {
                    selectedCategory.key.contains("career", ignoreCase = true) -> "career"
                    selectedCategory.key.contains("love", ignoreCase = true) -> "love"
                    selectedCategory.key.contains("health", ignoreCase = true) -> "health"
                    selectedCategory.key.contains("wealth", ignoreCase = true) || selectedCategory.key.contains("finance", ignoreCase = true) -> "finance"
                    else -> "general"
                }
                val palette = ScoreColors.paletteFor(areaKey, 80, isDarkTheme)
                GlowColors(
                    accent = palette.glow,
                    deep = palette.main,
                    radial = palette.glow
                )
            }
            ApplyRootGlow(glowColors)
        }

        BackHandler(enabled = step != ConsultStep.BirthDetails) {
            viewModel.navigateBack()
        }

        Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
            if (step != ConsultStep.BirthDetails) {
                CosmicStepTracker(
                    step = step,
                    modifier = Modifier.padding(top = metrics.consultSectionGap, bottom = 4.dp),
                    onStepClick = { index -> viewModel.jumpToStep(index) }
                )
            }

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading && step != ConsultStep.Result) {
                ConsultSkeleton(modifier = Modifier.fillMaxSize())
            } else {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        (slideInVertically(animationSpec = tween(400)) { it / 8 } + fadeIn(tween(400)))
                            .togetherWith(slideOutVertically(animationSpec = tween(300)) { -it / 8 } + fadeOut(tween(300)))
                    },
                    label = "step_transition"
                ) { targetStep ->
                    Column(modifier = Modifier.fillMaxSize()) {
                        when (targetStep) {
                            ConsultStep.BirthDetails -> BirthDetailsStep(viewModel, onViewHistory)
                            ConsultStep.CategorySelection -> CategoryStep(viewModel)
                            ConsultStep.SubCategorySelection -> SubCategoryStep(viewModel)
                            ConsultStep.QuestionSelection -> QuestionStep(viewModel)
                            ConsultStep.Result -> ResultStep(viewModel)
                        }
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
    val metrics = responsiveMetrics()
    val currentIndex = when (step) {
        ConsultStep.BirthDetails -> 0
        ConsultStep.CategorySelection -> 0
        ConsultStep.SubCategorySelection -> 1
        ConsultStep.QuestionSelection -> 2
        ConsultStep.Result -> 3
    }

    val nodes = listOf(
        "Domain" to "1",
        "Focus" to "2",
        "Query" to "3",
        "Insight" to "4"
    )
    val activeColor = Color(0xFFC8880A) // Golden color
    val inactiveColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = metrics.pagePadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .padding(horizontal = 24.dp)
        ) {
            val width = size.width
            val cy = size.height / 2f
            
            drawLine(
                color = inactiveColor,
                start = Offset(0f, cy),
                end = Offset(width, cy),
                strokeWidth = 2.dp.toPx()
            )
            
            if (currentIndex > 0) {
                val fraction = currentIndex.toFloat() / (nodes.size - 1)
                drawLine(
                    color = activeColor,
                    start = Offset(0f, cy),
                    end = Offset(width * fraction, cy),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            nodes.forEachIndexed { index, (label, num) ->
                val isActive = index == currentIndex
                val isCompleted = index < currentIndex
                val isClickable = index <= currentIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = isClickable) { onStepClick(index + 1) }
                ) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = if (isActive || isCompleted) activeColor else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = if (isActive || isCompleted) activeColor else inactiveColor
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = num,
                                color = if (isActive || isCompleted) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ConsultSkeleton(modifier: Modifier = Modifier) {
    val metrics = responsiveMetrics()

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(metrics.consultSectionGap)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(CircleShape).shimmerEffect())
            Spacer(modifier = Modifier.width(metrics.cardPadding))
            Column {
                Box(modifier = Modifier.width(200.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.width(140.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }
        }

        Spacer(modifier = Modifier.height(metrics.consultSectionGap * 2))

        GlassCard(shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(metrics.cardPadding)) {
                val widths = listOf(0.9f, 0.75f, 0.85f, 0.6f, 0.5f)
                widths.forEach { frac ->
                    Box(modifier = Modifier.fillMaxWidth(frac).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(metrics.heroBottomPadding))

        ShimmerBlock(height = 56.dp, cornerRadius = 16.dp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthDetailsStep(viewModel: ConsultViewModel, onViewHistory: () -> Unit) {
    val birthError = viewModel.birthDetailsError.value
    val metrics = responsiveMetrics()
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val displayDob = remember(viewModel.dob.value) {
        if (viewModel.dob.value.isNotEmpty()) {
            try {
                LocaleFormatter.displayDate(viewModel.dob.value, Locale.US, "dd-MM-yyyy")
            } catch (e: Exception) {
                viewModel.dob.value
            }
        } else ""
    }

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
            .padding(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(metrics.consultSectionGap)
    ) {
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
            Spacer(modifier = Modifier.width(metrics.consultSectionGap))
            Text(
                stringResource(R.string.consult_title_cosmic_blueprint),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val interactionSource = remember { MutableInteractionSource() }
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> isPressed = true
                    is PressInteraction.Release -> isPressed = false
                    is PressInteraction.Cancel -> isPressed = false
                }
            }
        }

        Button(
            onClick = { viewModel.fetchTree() },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = metrics.buttonHeight)
                .scale(buttonScale)
                .shimmerSweepEffect(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            interactionSource = interactionSource
        ) {
            Text(stringResource(R.string.consult_btn_begin_session), fontWeight = FontWeight.ExtraBold)
        }

        /*
        ========================================================================
        FINAL CONSULT PLAN MAPPED ONLY FROM REAL API TREE
        ========================================================================
        API Input Data:
        {
            "age": 23,
            "age_group": {
                "key": "21-29",
                "label": "21–29",
                "life_stage": "Young Adult"
            },
            "tree": {
                "life_stage": "Young Adult",
                "primary": [
                    {
                        "key": "career_study",
                        "label": "Job Search / Job Change",
                        "icon": "💼",
                        "subs": [ ... ]
                    },
                    ...
                ],
                "hidden": [ ... ]
            }
        }

        Consult Flow Steps:
        Step 1: Select Domain (Use tree.primary normally, tree.hidden under "More Topics")
        Step 2: Select Focus Area (Show selected category, then subcategories)
        Step 3: Pick Query / Custom Question (Show path breadcrumbs, pick query, Response Tone)
        Step 4: AI Response Page / Cosmic Insight (Parse result text into 4 distinct cards: 
                Navi's Insight, Chart Reasoning, Timing, Guidance)
        ========================================================================
        */

        if (birthError != null) {
            Text(
                text = birthError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = metrics.consultSectionGap / 2)
            )
        }

        Spacer(modifier = Modifier.height(metrics.consultSectionGap / 2))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500, delayMillis = 0)) + slideInVertically(tween(500, delayMillis = 0)) { it / 4 }
        ) {
            CosmicClickableField(
                value = displayDob,
                onClick = { showDatePicker = true },
                label = stringResource(R.string.consult_label_dob)
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500, delayMillis = 120)) + slideInVertically(tween(500, delayMillis = 120)) { it / 4 }
        ) {
            val internalTob = viewModel.tob.value.replace(":", "")
            CosmicInputField(
                value = internalTob,
                onValueChange = { newValue ->
                    if (newValue.length <= 4 && newValue.all { char -> char.isDigit() }) {
                        if (newValue.length == 4) {
                            val hh = newValue.substring(0, 2)
                            val mm = newValue.substring(2, 4)
                            viewModel.tob.value = "$hh:$mm"
                        } else {
                            viewModel.tob.value = newValue
                        }
                    }
                },
                label = stringResource(R.string.consult_label_tob),
                visualTransformation = TimeVisualTransformation(),
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
                label = stringResource(R.string.consult_label_pob)
            )
        }

        Text(
            stringResource(R.string.consult_section_preferences),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = metrics.consultSectionGap / 2)
        )

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500, delayMillis = 400)) +
                    slideInVertically(tween(500, delayMillis = 400)) { it / 4 }
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(metrics.cardPadding)) {
                    var languageMenuExpanded by remember { mutableStateOf(false) }
                    val currentLanguageLabel = remember(viewModel.selectedLanguage.value) {
                        SupportedLanguages.firstOrNull { it.code == viewModel.selectedLanguage.value }?.let {
                            "${it.flag} ${it.label}"
                        } ?: "🇬🇧 English"
                    }
                    ExposedDropdownMenuBox(
                        expanded = languageMenuExpanded,
                        onExpandedChange = { languageMenuExpanded = !languageMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentLanguageLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.consult_label_preferred_language)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .bringIntoViewOnFocus(),
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
                            SupportedLanguages.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(option.flag, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(option.label)
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectedLanguage.value = option.code
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
            enabled = false,
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
    val metrics = responsiveMetrics()
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(metrics.consultSectionGap)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.consult_question_guidance_prompt),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.consult_subtitle_life_stage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                val age = viewModel.serverAge.value ?: viewModel.dob.value.let { dobString ->
                    if (dobString.isNotEmpty()) {
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val birthDate = sdf.parse(dobString) ?: Date()
                            val today = Calendar.getInstance()
                            val birth = Calendar.getInstance()
                            birth.time = birthDate
                            var calculatedAge = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
                            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                                calculatedAge--
                            }
                            calculatedAge
                        } catch (e: Exception) {
                            23
                        }
                    } else 23
                }

                val lifeStageFormatted = tree.life_stage.replace("_", " ").titleCase()

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.consult_life_stage_age_format, lifeStageFormatted, age),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(metrics.consultSectionGap / 2))
        }
        
        items(tree.primary, key = { it.key }) { category ->
            val index = tree.primary.indexOf(category)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, delayMillis = index * 80)) +
                        slideInVertically(tween(600, delayMillis = index * 80)) { it / 3 }
            ) {
                val isHighlighted = viewModel.highlightedCategory.value == category
                CosmicCategoryCard(category, index, isHighlighted) { viewModel.selectCategory(category) }
            }
        }

        if (tree.hidden.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(
                        text = stringResource(R.string.consult_section_more_topics),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.consult_subtitle_more_topics),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        tree.hidden.forEach { category ->
                            SecondaryCategoryCard(
                                category = category,
                                isHighlighted = viewModel.highlightedCategory.value == category,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.selectCategory(category) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CosmicCategoryCard(category: Category, index: Int, isHighlighted: Boolean, onClick: () -> Unit) {
    val metrics = responsiveMetrics()

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

    val guidanceText = category.description?.takeIf { it.isNotBlank() } ?: when (category.key) {
        "career_study" -> "Find work, switch roles, career growth and promotions."
        "money_wealth" -> "Income, savings, loans, investments and wealth growth."
        "love_marriage" -> "Partner, relationship, marriage and compatibility."
        "movement_change" -> "New city, abroad, travel and independence."
        "spirituality_luck" -> "Purpose, life path, luck cycles and opportunities."
        else -> "Personalized guidance based on your birth chart."
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
        border = if (isHighlighted) BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null
    ) {
        Row(modifier = Modifier.padding(metrics.cardPadding), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(metrics.consultAvatarSize)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(planetColor, planetColor.copy(alpha = 0.6f))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(category.icon, fontSize = if (metrics.isCompactWidth || metrics.isLargeFont) 24.sp else 28.sp)
            }
            Spacer(modifier = Modifier.width(metrics.cardPadding))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    category.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = guidanceText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SecondaryCategoryCard(
    category: Category,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val planetColor = when {
        category.key.contains("career", ignoreCase = true) -> AstroColors.Saturn
        category.key.contains("love", ignoreCase = true) -> AstroColors.Venus
        category.key.contains("health", ignoreCase = true) -> AstroColors.Mars
        category.key.contains("wealth", ignoreCase = true) || category.key.contains("finance", ignoreCase = true) -> AstroColors.Jupiter
        else -> AstroColors.Default
    }

    GlassCard(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = if (isHighlighted) BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(planetColor.copy(alpha = 0.8f), planetColor.copy(alpha = 0.4f))
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(category.icon, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SubCategoryStep(viewModel: ConsultViewModel) {
    val category = viewModel.selectedCategory.value ?: return
    val metrics = responsiveMetrics()
    
    val planetColor = when {
        category.key.contains("career", ignoreCase = true) -> AstroColors.Saturn
        category.key.contains("love", ignoreCase = true) -> AstroColors.Venus
        category.key.contains("health", ignoreCase = true) -> AstroColors.Mars
        category.key.contains("wealth", ignoreCase = true) || category.key.contains("finance", ignoreCase = true) -> AstroColors.Jupiter
        else -> AstroColors.Default
    }
    
    val parentSubtitle = when (category.key) {
        "career_study" -> "Career growth and opportunities"
        "money_wealth" -> "Wealth accumulation and financial habits"
        "love_marriage" -> "Relationships, compatibility and marriage"
        "movement_change" -> "Relocation, travel and independence"
        "spirituality_luck" -> "Life path, purpose and luck cycles"
        "health_mind" -> "Wellness, mental health and vitality"
        "family_relationships" -> "Family dynamics and responsibilities"
        else -> "Personalized guidance"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding().navigationBarsPadding(),
        contentPadding = PaddingValues(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(metrics.consultSectionGap)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = planetColor.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, planetColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(planetColor, shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(category.icon, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = parentSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(metrics.consultSectionGap / 2))
            Text(
                text = "Choose your focus area",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Help us understand your situation better.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(metrics.consultSectionGap / 2))
        }

        items(category.subs, key = { it.key }) { sub ->
            val subIcon = when (sub.key) {
                "job_search" -> Icons.Default.Search
                "first_job" -> Icons.Default.Person
                "job_change" -> Icons.Default.Refresh
                "promotion" -> Icons.Default.TrendingUp
                "business_start" -> Icons.Default.Star
                
                "income_growth" -> Icons.Default.TrendingUp
                "investments" -> Icons.Default.TrendingUp
                "debt_loans" -> Icons.Default.AccountBalanceWallet
                "wealth_timing" -> Icons.Default.DateRange
                "financial_habits" -> Icons.Default.Settings
                
                "finding_partner" -> Icons.Default.Favorite
                "current_relationship" -> Icons.Default.Favorite
                "marriage_timing" -> Icons.Default.DateRange
                "breakup_recovery" -> Icons.Default.Refresh
                "marriage_compatibility" -> Icons.Default.Star
                
                "moving_out" -> Icons.Default.Home
                "abroad_settlement" -> Icons.Default.Star
                "buying_renting" -> Icons.Default.Home
                "travel" -> Icons.Default.Send
                "life_changes" -> Icons.Default.Refresh
                
                "life_purpose" -> Icons.Default.Info
                "next_5_years" -> Icons.Default.DateRange
                "luck_cycles" -> Icons.Default.Star
                "spiritual_growth" -> Icons.Default.Person
                "remedies_general" -> Icons.Default.Build
                
                "health_general_21" -> Icons.Default.Info
                "family_21" -> Icons.Default.Person
                else -> Icons.Default.Star
            }

            val subSubtitle = when (sub.key) {
                "job_search" -> "Finding your first proper job."
                "first_job" -> "Settling in, understanding role and workplace."
                "job_change" -> "Switching to a better opportunity."
                "promotion" -> "Moving up, recognition and career advancement."
                "business_start" -> "Entrepreneurship, startup and self-employment."
                
                "income_growth" -> "Salary, income increase and earning potential."
                "investments" -> "Stocks, savings, real estate and future planning."
                "debt_loans" -> "Loan repayment, borrowing and financial stress."
                "wealth_timing" -> "Peak earning period, Dhana Yoga and prosperity timing."
                "financial_habits" -> "Spending, saving and financial discipline."
                
                "finding_partner" -> "Meeting life partner and relationship timing."
                "current_relationship" -> "Compatibility, issues and relationship future."
                "marriage_timing" -> "Best marriage year, delays and family support."
                "breakup_recovery" -> "Healing, ex-partner and moving forward."
                "marriage_compatibility" -> "Match strength, doshas and long-term bond."
                
                "moving_out" -> "New city, independence and living setup."
                "abroad_settlement" -> "Visa, foreign settlement and country suitability."
                "buying_renting" -> "House, car, home loan and property decisions."
                "travel" -> "Travel, abroad trips and life-changing journeys."
                "life_changes" -> "Stability, transformation and transit periods."
                
                "life_purpose" -> "Karma, Rahu, purpose and life alignment."
                "next_5_years" -> "Dasha, future phase and long-term priorities."
                "luck_cycles" -> "Jupiter, Sade Sati, breakthrough years and rise."
                "spiritual_growth" -> "Meditation, Ketu, mantra and inner growth."
                "remedies_general" -> "Gemstone, puja, remedies and lucky days."
                
                "health_general_21" -> "General health, fitness, and vitality."
                "family_21" -> "Family dynamics, providorship, and balancing independent life."
                else -> "Focus area guidance."
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectSubCategory(sub) },
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(metrics.cardPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(planetColor.copy(alpha = 0.8f), planetColor.copy(alpha = 0.4f))
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = subIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(metrics.cardPadding))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sub.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = planetColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionStep(viewModel: ConsultViewModel) {
    val sub = viewModel.selectedSubCategory.value ?: return
    val metrics = responsiveMetrics()
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(metrics.consultSectionGap)
    ) {
        item {
            val category = viewModel.selectedCategory.value
            if (category != null) {
                val planetColor = when {
                    category.key.contains("career", ignoreCase = true) -> AstroColors.Saturn
                    category.key.contains("love", ignoreCase = true) -> AstroColors.Venus
                    category.key.contains("health", ignoreCase = true) -> AstroColors.Mars
                    category.key.contains("wealth", ignoreCase = true) || category.key.contains("finance", ignoreCase = true) -> AstroColors.Jupiter
                    else -> AstroColors.Default
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = planetColor.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, planetColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )

                    val subIcon = when (sub.key) {
                        "job_search" -> Icons.Default.Search
                        "first_job" -> Icons.Default.Person
                        "job_change" -> Icons.Default.Refresh
                        "promotion" -> Icons.Default.TrendingUp
                        "business_start" -> Icons.Default.Star
                        
                        "income_growth" -> Icons.Default.TrendingUp
                        "investments" -> Icons.Default.TrendingUp
                        "debt_loans" -> Icons.Default.AccountBalanceWallet
                        "wealth_timing" -> Icons.Default.DateRange
                        "financial_habits" -> Icons.Default.Settings
                        
                        "finding_partner" -> Icons.Default.Favorite
                        "current_relationship" -> Icons.Default.Favorite
                        "marriage_timing" -> Icons.Default.DateRange
                        "breakup_recovery" -> Icons.Default.Refresh
                        "marriage_compatibility" -> Icons.Default.Star
                        
                        "moving_out" -> Icons.Default.Home
                        "abroad_settlement" -> Icons.Default.Star
                        "buying_renting" -> Icons.Default.Home
                        "travel" -> Icons.Default.Send
                        "life_changes" -> Icons.Default.Refresh
                        
                        "life_purpose" -> Icons.Default.Info
                        "next_5_years" -> Icons.Default.DateRange
                        "luck_cycles" -> Icons.Default.Star
                        "spiritual_growth" -> Icons.Default.Person
                        "remedies_general" -> Icons.Default.Build
                        
                        "health_general_21" -> Icons.Default.Info
                        "family_21" -> Icons.Default.Person
                        else -> Icons.Default.Star
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = planetColor.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, planetColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = subIcon,
                                contentDescription = null,
                                tint = planetColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sub.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "Choose your question",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "You can pick one below or write your own.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(metrics.consultSectionGap / 2))
        }
        
        itemsIndexed(sub.questions, key = { _, question -> question }) { index, question ->
            val isSelected = viewModel.selectedQuestion.value == question
            val planetColor = viewModel.selectedCategory.value?.let { category ->
                when {
                    category.key.contains("career", ignoreCase = true) -> AstroColors.Saturn
                    category.key.contains("love", ignoreCase = true) -> AstroColors.Venus
                    category.key.contains("health", ignoreCase = true) -> AstroColors.Mars
                    category.key.contains("wealth", ignoreCase = true) || category.key.contains("finance", ignoreCase = true) -> AstroColors.Jupiter
                    else -> AstroColors.Default
                }
            } ?: MaterialTheme.colorScheme.secondary

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, delayMillis = index * 80)) + slideInVertically(tween(600, delayMillis = index * 80)) { it / 4 }
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectedQuestion.value = question },
                    shape = RoundedCornerShape(16.dp),
                    border = if (isSelected) BorderStroke(1.5.dp, planetColor) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Surface(
                            modifier = Modifier.size(20.dp),
                            shape = CircleShape,
                            color = if (isSelected) planetColor else Color.Transparent,
                            border = if (isSelected) null else BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        ) {
                            if (isSelected) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Text(
                    text = "Ask your own question (optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        OutlinedTextField(
                            value = viewModel.customNote.value,
                            onValueChange = { if (it.length <= 300) viewModel.customNote.value = it },
                            placeholder = { Text("Tell Navi your situation...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        Text(
                            text = "${viewModel.customNote.value.length}/300",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.align(Alignment.BottomEnd)
                        )
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Text(
                    text = "Response Tone",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tones = listOf(
                        Triple("warm", "Warm", Icons.Default.WbSunny),
                        Triple("direct", "Direct", Icons.Default.Explore),
                        Triple("spiritual", "Spiritual", Icons.Default.AutoAwesome)
                    )
                    
                    tones.forEach { (toneKey, toneLabel, icon) ->
                        val isSelected = viewModel.selectedTone.value == toneKey
                        val activeColor = viewModel.selectedCategory.value?.let { category ->
                            when {
                                category.key.contains("career", ignoreCase = true) -> AstroColors.Saturn
                                category.key.contains("love", ignoreCase = true) -> AstroColors.Venus
                                category.key.contains("health", ignoreCase = true) -> AstroColors.Mars
                                category.key.contains("wealth", ignoreCase = true) || category.key.contains("finance", ignoreCase = true) -> AstroColors.Jupiter
                                else -> AstroColors.Default
                            }
                        } ?: MaterialTheme.colorScheme.secondary

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.selectedTone.value = toneKey },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) activeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = toneLabel,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            val activeColor = viewModel.selectedCategory.value?.let { cat ->
                when {
                    cat.key.contains("career", ignoreCase = true) -> AstroColors.Saturn
                    cat.key.contains("love", ignoreCase = true) -> AstroColors.Venus
                    cat.key.contains("health", ignoreCase = true) -> AstroColors.Mars
                    cat.key.contains("wealth", ignoreCase = true) || cat.key.contains("finance", ignoreCase = true) -> AstroColors.Jupiter
                    else -> AstroColors.Default
                }
            } ?: MaterialTheme.colorScheme.primary

            val isSelectedQuestion = viewModel.selectedQuestion.value.isNotEmpty()
            val hasCustomNote = viewModel.customNote.value.isNotBlank()
            val canSubmit = isSelectedQuestion || hasCustomNote

            Button(
                onClick = {
                    if (canSubmit) {
                        val q = viewModel.selectedQuestion.value
                        viewModel.selectQuestion(q.ifEmpty { "Other" })
                    }
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .then(if (canSubmit) Modifier.shimmerSweepEffect() else Modifier),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = activeColor,
                    contentColor = Color.White
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ask Navi", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ToneChip(tone: String, isSelected: Boolean, onClick: () -> Unit) {
    val metrics = responsiveMetrics()
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

    val toneDisplayText = when(tone) {
        "warm" -> stringResource(R.string.consult_tone_warm)
        "direct" -> stringResource(R.string.consult_tone_direct)
        "spiritual" -> stringResource(R.string.consult_tone_spiritual)
        else -> tone.replaceFirstChar { it.uppercase() }
    }

    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = shape,
        color = if (isSelected) color else Color.Transparent,
        border = if (!isSelected) BorderStroke(1.dp, color.copy(alpha = 0.5f)) else null,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else color
    ) {
        Text(
            toneDisplayText,
            modifier = Modifier.padding(horizontal = metrics.cardPadding, vertical = metrics.consultSectionGap / 2),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

data class InsightSection(
    val key: String,
    val uiLabel: String,
    val body: String,
    val index: Int
)

private val SECTION_LABEL_MAP = mapOf(
    "direct_response" to "Direct Answer",
    "direct_answer" to "Direct Answer",
    "chart_analysis" to "Chart Analysis",
    "chart_reasoning" to "Chart Analysis",
    "timing_n_probability" to "Timing & Probability",
    "timing_and_probability" to "Timing & Probability",
    "timing" to "Timing & Probability",
    "practical_next_steps" to "Practical Next Steps",
    "next_steps" to "Practical Next Steps",
    "guidance" to "Practical Next Steps"
)

fun parseInsightSections(resultText: String): List<InsightSection> {
    // New API format: lines like "#1 direct_response:" followed by body text.
    val headerRegex = Regex("""(?m)^\s*#?\s*(\d+)\s+([A-Za-z_]+)\s*:\s*$""")
    val matches = headerRegex.findAll(resultText).toList()

    if (matches.isNotEmpty()) {
        return matches.mapIndexed { i, match ->
            val num = match.groupValues[1].toIntOrNull() ?: (i + 1)
            val key = match.groupValues[2].lowercase()
            val bodyStart = match.range.last + 1
            val bodyEnd = if (i + 1 < matches.size) matches[i + 1].range.first else resultText.length
            val body = resultText.substring(bodyStart, bodyEnd).trim()
            val label = SECTION_LABEL_MAP[key]
                ?: key.replace("_", " ").split(" ").joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
            InsightSection(key = key, uiLabel = label, body = body, index = num)
        }.filter { it.body.isNotEmpty() }
    }

    // Fallback: split on blank lines and assign the 4 fixed sections in order.
    val paragraphs = resultText.split(Regex("\n\n+")).filter { it.trim().isNotEmpty() }
    val fallbackKeys = listOf("direct_response", "chart_analysis", "timing_n_probability", "practical_next_steps")
    return paragraphs.mapIndexed { i, p ->
        val key = fallbackKeys.getOrElse(i) { "practical_next_steps" }
        InsightSection(key = key, uiLabel = SECTION_LABEL_MAP[key] ?: key, body = p.trim(), index = i + 1)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InsightSectionCard(
    section: InsightSection,
    activeColor: Color,
    onLongPress: () -> Unit
) {
    val isPrimary = section.key in listOf("direct_response", "direct_answer")
    val isBullets = section.key in listOf("practical_next_steps", "next_steps", "guidance")

    val sectionIcon = when (section.key) {
        "direct_response", "direct_answer" -> Icons.Default.AutoAwesome
        "chart_analysis", "chart_reasoning" -> Icons.Default.Info
        "timing_n_probability", "timing_and_probability", "timing" -> Icons.Default.AccessTime
        "practical_next_steps", "next_steps", "guidance" -> Icons.Default.Star
        else -> Icons.Default.AutoAwesome
    }

    val containerColor = if (isPrimary) activeColor.copy(alpha = 0.12f) else null
    val borderColor = if (isPrimary) activeColor.copy(alpha = 0.5f) else activeColor.copy(alpha = 0.25f)

    val cardModifier = Modifier
        .fillMaxWidth()
        .combinedClickable(onClick = {}, onLongClick = onLongPress)

    val cardInner: @Composable () -> Unit = {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(if (isPrimary) 36.dp else 32.dp)
                        .background(activeColor.copy(alpha = if (isPrimary) 0.22f else 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = sectionIcon,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(if (isPrimary) 20.dp else 16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = section.uiLabel,
                    style = if (isPrimary) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                    fontWeight = if (isPrimary) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isPrimary) activeColor else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (isBullets) {
                val items = section.body
                    .lines()
                    .map { it.trim().removePrefix("-").removePrefix("•").removePrefix("*").trim() }
                    .filter { it.isNotEmpty() }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEach { item ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 7.dp)
                                    .size(5.dp)
                                    .background(activeColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = section.body,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    fontWeight = if (isPrimary) FontWeight.Medium else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isPrimary) 0.95f else 0.8f)
                )
            }
        }
    }

    if (containerColor != null) {
        Surface(
            modifier = cardModifier,
            shape = RoundedCornerShape(20.dp),
            color = containerColor,
            border = BorderStroke(1.5.dp, borderColor)
        ) {
            cardInner()
        }
    } else {
        GlassCard(
            modifier = cardModifier,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, borderColor)
        ) {
            cardInner()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResultStep(viewModel: ConsultViewModel) {
    val result = viewModel.consultResult.value
    val metrics = responsiveMetrics()
    var revealStarted by remember { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(result != null) {
        if (result != null) {
            revealStarted = true
        } else {
            revealStarted = false
        }
    }
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(metrics.pagePadding)) {
        val category = viewModel.selectedCategory.value
        val activeColor = category?.let { cat ->
            when {
                cat.key.contains("career", ignoreCase = true) -> AstroColors.Saturn
                cat.key.contains("love", ignoreCase = true) -> AstroColors.Venus
                cat.key.contains("health", ignoreCase = true) -> AstroColors.Mars
                cat.key.contains("wealth", ignoreCase = true) || cat.key.contains("finance", ignoreCase = true) -> AstroColors.Jupiter
                else -> AstroColors.Default
            }
        } ?: MaterialTheme.colorScheme.primary

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(800)) + scaleIn(initialScale = 0.95f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(activeColor.copy(alpha = 0.2f), CircleShape)
                        .border(1.5.dp, activeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cosmic Insight",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (viewModel.selectedQuestion.value == "Other") viewModel.customNote.value else viewModel.selectedQuestion.value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = activeColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        val ageGroup = viewModel.ageGroup.value
        val serverAge = viewModel.serverAge.value
        if (ageGroup != null || serverAge != null) {
            Spacer(modifier = Modifier.height(10.dp))
            val lifeStage = ageGroup?.life_stage?.replace("_", " ")?.titleCase()
            val chipText = when {
                lifeStage != null && serverAge != null -> "$lifeStage  •  Age $serverAge"
                lifeStage != null -> lifeStage
                serverAge != null -> "Age $serverAge"
                else -> ""
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = chipText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(metrics.consultSectionGap * 1.5f))

        if (result != null) {
            AnimatedVisibility(
                visible = revealStarted,
                enter = fadeIn(tween(1000)) + slideInVertically { it / 10 }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val sections = remember(result) { parseInsightSections(result) }
                    sections.forEach { section ->
                        InsightSectionCard(
                            section = section,
                            activeColor = activeColor,
                            onLongPress = {
                                scope.launch {
                                    clipboard.setSensitiveText("insight_section", section.body)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "This is an AI generated insight based on your birth details. For reference only.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Ask follow-up */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, activeColor.copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = activeColor)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ask Follow-up", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Button(
                            onClick = { viewModel.navigateBack() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = activeColor,
                                contentColor = Color.White
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Explore Another", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        } else {
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val strokeWidthPx = with(LocalDensity.current) { 2.dp.toPx() }
            val dotRadiusPx = with(LocalDensity.current) { 6.dp.toPx() }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = metrics.consultSectionGap * 2)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(metrics.orbitCoreSize)) {
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
                        modifier = Modifier.size(metrics.snapshotImageSize).scale(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(metrics.consultSectionGap))
                
                val loadingTexts = listOf(
                    stringResource(R.string.consult_loading_planetary),
                    stringResource(R.string.consult_loading_cosmic),
                    stringResource(R.string.consult_loading_karmic),
                    stringResource(R.string.consult_loading_celestial)
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
    }
}

@PreviewMultiDevice
@Composable
fun CosmicStepTrackerPreview() {
    Surface {
        CosmicStepTracker(
            step = ConsultStep.CategorySelection,
            onStepClick = {}
        )
    }
}
