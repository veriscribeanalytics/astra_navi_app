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

import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch
import com.astranavi.app.util.LocaleFormatter
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
    com.astranavi.app.util.SecureScreen()
    val step = viewModel.step.value
    val isLoading = viewModel.isLoading.value
    val metrics = responsiveMetrics()

    BackHandler(enabled = step != ConsultStep.BirthDetails) {
        viewModel.navigateBack()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        CosmicStepTracker(
            step = step, 
            modifier = Modifier.padding(top = metrics.consultSectionGap, bottom = 4.dp),
            onStepClick = { index -> viewModel.jumpToStep(index) }
        )

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

@Composable
fun CosmicStepTracker(
    step: ConsultStep, 
    modifier: Modifier = Modifier,
    onStepClick: (Int) -> Unit = {}
) {
    val metrics = responsiveMetrics()
    val currentIndex = when (step) {
        ConsultStep.BirthDetails -> 0
        ConsultStep.CategorySelection -> 1
        ConsultStep.SubCategorySelection -> 2
        ConsultStep.QuestionSelection -> 3
        ConsultStep.Result -> 4
    }

    val nodes = listOf("Birth", "Domain", "Focus", "Query", "Insight")
    val activeColor = MaterialTheme.colorScheme.secondary
    val inactiveColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = metrics.pagePadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        nodes.forEachIndexed { index, _ ->
            val isActive = index == currentIndex
            val isCompleted = index < currentIndex
            val isClickable = index <= currentIndex

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

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = isClickable) { onStepClick(index) }
                    .padding(metrics.consultSectionGap / 2)
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
                    is androidx.compose.foundation.interaction.PressInteraction.Press -> isPressed = true
                    is androidx.compose.foundation.interaction.PressInteraction.Release -> isPressed = false
                    is androidx.compose.foundation.interaction.PressInteraction.Cancel -> isPressed = false
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
            CosmicInputField(
                value = viewModel.tob.value,
                onValueChange = { if (it.length <= 4) viewModel.tob.value = it },
                label = stringResource(R.string.consult_label_tob),
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
                    ExposedDropdownMenuBox(
                        expanded = languageMenuExpanded,
                        onExpandedChange = { languageMenuExpanded = !languageMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = viewModel.selectedLanguage.value,
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
                            listOf(stringResource(R.string.consult_lang_english), stringResource(R.string.consult_lang_hindi), stringResource(R.string.consult_lang_marathi), stringResource(R.string.consult_lang_gujarati), stringResource(R.string.consult_lang_tamil)).forEach { lang ->
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.consult_title_select_domain),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(metrics.consultSectionGap / 2)) {
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

                    Canvas(modifier = Modifier.size(metrics.consultAvatarSize * 1.25f)) {
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
                            tree.life_stage.replace("_", " ").titleCase(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                    stringResource(R.string.consult_label_personalized_guidance),
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
fun SubCategoryStep(viewModel: ConsultViewModel) {
    val category = viewModel.selectedCategory.value ?: return
    val metrics = responsiveMetrics()
    var expandedSubKey by remember { mutableStateOf<String?>(null) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding().navigationBarsPadding(),
        contentPadding = PaddingValues(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(metrics.consultSectionGap)
    ) {
        item {
            Text(
                stringResource(R.string.consult_title_focus_area_format, category.label),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(metrics.consultSectionGap))
        }
        
        items(category.subs, key = { it.key }) { sub ->
            val isExpanded = expandedSubKey == sub.key
            val rotation by animateFloatAsState(if (isExpanded) 90f else 0f, label = "arrow_rotation")

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .clickable { expandedSubKey = if (isExpanded) null else sub.key },
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(metrics.cardPadding)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            sub.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
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
                        Spacer(modifier = Modifier.height(metrics.consultSectionGap))
                        Text(
                            stringResource(R.string.consult_desc_focus_analysis),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(metrics.consultSectionGap))
                        Button(
                            onClick = { viewModel.selectSubCategory(sub) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(stringResource(R.string.consult_btn_select_focus), fontWeight = FontWeight.Bold)
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
    val metrics = responsiveMetrics()
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(metrics.consultSectionGap)
    ) {
        item {
            Text(stringResource(R.string.consult_title_pick_query), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.consult_desc_query_analysis), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(metrics.consultSectionGap))
        }
        
        itemsIndexed(sub.questions, key = { _, question -> question }) { index, question ->
            val isSelected = viewModel.selectedQuestion.value == question
            
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
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(MaterialTheme.colorScheme.secondary))
                        
                        Box(modifier = Modifier.padding(metrics.cardPadding)) {
                            Text(question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.consult_section_settings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = metrics.consultSectionGap))
            Spacer(modifier = Modifier.height(metrics.consultSectionGap))

            CosmicInputField(
                value = viewModel.customNote.value,
                onValueChange = { viewModel.customNote.value = it },
                label = stringResource(R.string.consult_label_custom_context)
            )

            Spacer(modifier = Modifier.height(metrics.consultSectionGap))
            Text(stringResource(R.string.consult_label_response_tone), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(metrics.consultSectionGap), 
                verticalArrangement = Arrangement.spacedBy(metrics.consultSectionGap / 2),
                modifier = Modifier.padding(vertical = metrics.consultSectionGap)
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
            val hasContext = viewModel.customNote.value.isNotBlank()
            Button(
                onClick = { viewModel.selectQuestion("Other") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = metrics.buttonHeight)
                    .then(if (hasContext) Modifier.shimmerSweepEffect() else Modifier),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasContext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (hasContext) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(stringResource(R.string.consult_btn_ask_custom), fontWeight = FontWeight.ExtraBold)
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
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)) else null,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResultStep(viewModel: ConsultViewModel) {
    val result = viewModel.consultResult.value
    val metrics = responsiveMetrics()
    var revealStarted by remember { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(result) {
        if (result != null && !revealStarted) {
            delay(600) 
            revealStarted = true
        }
    }
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(metrics.pagePadding)) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(800)) + scaleIn(initialScale = 0.95f)
        ) {
            Column {
                Box(
                    modifier = Modifier.size(metrics.consultAvatarSize).background(MaterialTheme.colorScheme.secondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
                }
                Spacer(modifier = Modifier.height(metrics.consultSectionGap))
                Text(stringResource(R.string.consult_title_cosmic_insight), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text(
                    if (viewModel.selectedQuestion.value == "Other") viewModel.customNote.value else viewModel.selectedQuestion.value,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(metrics.consultSectionGap * 2))
        
        if (result != null) {
            AnimatedVisibility(
                visible = revealStarted,
                enter = fadeIn(tween(1000)) + slideInVertically { it / 10 }
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                if (result != null) {
                                    scope.launch {
                                        clipboard.setSensitiveText("insight", result)
                                    }
                                }
                            }
                        ),
                    shape = RoundedCornerShape(28.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = result,
                        modifier = Modifier.padding(metrics.cardPadding),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = if (metrics.isLargeFont) 26.sp else 28.sp,
                        fontWeight = FontWeight.Bold
                    )
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
        
        Spacer(modifier = Modifier.height(metrics.heroBottomPadding))
        
        Button(
            onClick = { viewModel.navigateBack() },
            modifier = Modifier.fillMaxWidth().heightIn(min = metrics.buttonHeight).shimmerSweepEffect(),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.consult_btn_explore_again), fontWeight = FontWeight.ExtraBold)
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
