package com.astranavi.app.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.R
import com.astranavi.app.data.model.AnalyzeFullResponse
import com.astranavi.app.data.model.LocationSearchResult
import com.astranavi.app.data.model.User
import com.astranavi.app.ui.components.PreviewMultiDevice
import com.astranavi.app.ui.components.ShimmerBlock
import com.astranavi.app.ui.components.bringIntoViewOnFocus
import com.astranavi.app.ui.components.responsiveMetrics
import com.astranavi.app.ui.components.shimmerEffect
import com.astranavi.app.ui.components.titleCase
import com.astranavi.app.ui.theme.AstraNaviTheme
import com.astranavi.app.util.LocaleFormatter
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val GenderOptions = listOf("Male", "Female", "Other", "Not Specified")
private val MaritalStatusOptions = listOf("Single", "Married", "Divorced", "Widowed", "Not Specified")
private val OccupationOptions = listOf("Student", "Business", "Employed", "Homemaker", "Retired", "Unemployed", "Not Specified")

private fun getGenderResId(key: String): Int = when (key) {
    "Male" -> R.string.profile_option_male
    "Female" -> R.string.profile_option_female
    "Other" -> R.string.profile_option_other
    else -> R.string.profile_option_not_specified
}

private fun getMaritalStatusResId(key: String): Int = when (key) {
    "Single" -> R.string.profile_option_single
    "Married" -> R.string.profile_option_married
    "Divorced" -> R.string.profile_option_divorced
    "Widowed" -> R.string.profile_option_widowed
    else -> R.string.profile_option_not_specified
}

private fun getOccupationResId(key: String): Int = when (key) {
    "Student" -> R.string.profile_option_student
    "Business" -> R.string.profile_option_business
    "Employed" -> R.string.profile_option_employed
    "Homemaker" -> R.string.profile_option_homemaker
    "Retired" -> R.string.profile_option_retired
    "Unemployed" -> R.string.profile_option_unemployed
    else -> R.string.profile_option_not_specified
}

private fun profileCompletionProgress(name: String, gender: String, dob: String, tob: String, pob: String): Pair<Int, Int> {
    val required = listOf(name, gender, dob, tob, pob)
    return Pair(required.count { it.isNotBlank() }, required.size)
}

// Resolves Lagna / Moon / Sun signs preferring the top-level user fields, falling back
// to the parsed astrologyData (ascendant + planets). On free tier the backend masks
// planet names as "?", so the fallback returns null for those users — chips are
// then hidden gracefully.
private fun resolveSigns(user: User): Triple<String?, String?, String?> {
    val lagna = user.lagnaSign?.takeIf { it.isNotBlank() }
        ?: user.astrologyData?.ascendant?.sign?.takeIf { it.isNotBlank() }
    val moon = user.moonSign?.takeIf { it.isNotBlank() }
        ?: user.astrologyData?.planets
            ?.firstOrNull { it.planet.equals("Moon", ignoreCase = true) }
            ?.sign?.takeIf { it.isNotBlank() }
    val sun = user.sunSign?.takeIf { it.isNotBlank() }
        ?: user.astrologyData?.planets
            ?.firstOrNull { it.planet.equals("Sun", ignoreCase = true) }
            ?.sign?.takeIf { it.isNotBlank() }
    return Triple(lagna, moon, sun)
}

@Composable
private fun greetingForNow(): String {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    return when (hour) {
        in 5..11 -> stringResource(R.string.profile_greeting_morning)
        in 12..16 -> stringResource(R.string.profile_greeting_afternoon)
        in 17..20 -> stringResource(R.string.profile_greeting_evening)
        else -> stringResource(R.string.profile_greeting_night)
    }
}

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onOpenDrawer: () -> Unit = {},
    onBack: () -> Unit = {},
    onProfileComplete: () -> Unit = {},
    onAccountDeleted: () -> Unit = {},
    onNavigateToKundli: () -> Unit = {},
    onNavigateToPlans: () -> Unit = {}
) {
    com.astranavi.app.util.SecureScreen()
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState = viewModel.uiState.value
    val snackbarHostState = remember { SnackbarHostState() }
    val updateMessage = viewModel.updateMessage.value
    val isProfileComplete by viewModel.profileComplete

    LaunchedEffect(updateMessage) {
        updateMessage?.let {
            val messageStr = it.asString(context)
            snackbarHostState.showSnackbar(messageStr)
            viewModel.clearUpdateMessage()
            if (messageStr.contains("Profile updated", ignoreCase = true) && isProfileComplete) {
                onProfileComplete()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                is ProfileState.Loading -> ProfileSkeleton()
                is ProfileState.Error -> Text(
                    uiState.message.asString(),
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
                is ProfileState.Success -> ProfileContent(
                    user = uiState.user,
                    viewModel = viewModel,
                    isProfileComplete = isProfileComplete,
                    onProfileComplete = onProfileComplete,
                    onAccountDeleted = onAccountDeleted,
                    onNavigateToKundli = onNavigateToKundli,
                    onNavigateToPlans = onNavigateToPlans
                )
            }
        }
    }
}

@Composable
fun ProfileSkeleton() {
    val metrics = responsiveMetrics()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = metrics.pagePadding)
                .padding(top = metrics.pagePadding, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap)
        ) {
            ShimmerBlock(height = 250.dp, cornerRadius = 32.dp)
            ShimmerBlock(height = 120.dp, cornerRadius = 24.dp)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.width(140.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                repeat(5) { ShimmerBlock(height = 64.dp, cornerRadius = 18.dp) }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.width(140.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                repeat(3) { ShimmerBlock(height = 64.dp, cornerRadius = 18.dp) }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.width(140.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                repeat(2) { ShimmerBlock(height = 64.dp, cornerRadius = 18.dp) }
            }
        }
        ShimmerBlock(height = 92.dp, modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(), cornerRadius = 0.dp)
    }
}

@Composable
fun ProfileContent(
    user: User,
    viewModel: ProfileViewModel,
    isProfileComplete: Boolean,
    onProfileComplete: () -> Unit,
    onAccountDeleted: () -> Unit,
    onNavigateToKundli: () -> Unit,
    onNavigateToPlans: () -> Unit
) {
    var name by remember { mutableStateOf(user.name ?: "") }
    var dob by remember { mutableStateOf(user.dob ?: "") }
    var tob by remember { mutableStateOf(user.tob ?: "") }
    var pob by remember { mutableStateOf(user.pob ?: "") }
    var birthPlaceName by remember { mutableStateOf(user.birthPlaceName ?: "") }
    var birthLatitude by remember { mutableStateOf(user.birthLatitude) }
    var birthLongitude by remember { mutableStateOf(user.birthLongitude) }
    var birthTimezoneName by remember { mutableStateOf(user.birthTimezoneName ?: "") }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber ?: "") }
    var gender by remember { mutableStateOf(user.gender?.titleCase() ?: "") }
    var maritalStatus by remember { mutableStateOf(user.maritalStatus?.titleCase() ?: "") }
    var occupation by remember { mutableStateOf(user.occupation?.titleCase() ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var dobError by remember { mutableStateOf(false) }
    var tobError by remember { mutableStateOf(false) }
    var pobError by remember { mutableStateOf(false) }
    var genderError by remember { mutableStateOf(false) }

    val isUpdating by viewModel.isUpdating
    val metrics = responsiveMetrics()

    val hasChanges = remember(user, name, dob, tob, pob, birthPlaceName, birthLatitude, birthLongitude, birthTimezoneName, phoneNumber, gender, maritalStatus, occupation) {
        name != (user.name ?: "") ||
            dob != (user.dob ?: "") ||
            tob != (user.tob ?: "") ||
            pob != (user.pob ?: "") ||
            birthPlaceName != (user.birthPlaceName ?: "") ||
            birthLatitude != user.birthLatitude ||
            birthLongitude != user.birthLongitude ||
            birthTimezoneName != (user.birthTimezoneName ?: "") ||
            phoneNumber != (user.phoneNumber ?: "") ||
            gender != (user.gender?.titleCase() ?: "") ||
            maritalStatus != (user.maritalStatus?.titleCase() ?: "") ||
            occupation != (user.occupation?.titleCase() ?: "")
    }

    val (completedRequired, totalRequired) = profileCompletionProgress(name, gender, dob, tob, pob)
    val completionProgress = completedRequired / totalRequired.toFloat()

    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = metrics.pagePadding)
                .padding(top = metrics.pagePadding, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap)
        ) {
            ProfileHeroCard(
                user = user,
                name = name,
                isProfileComplete = isProfileComplete,
                onViewKundli = onNavigateToKundli
            )

            ChartAccuracyCard(
                completed = completedRequired,
                total = totalRequired,
                progress = completionProgress
            )

            SectionHeader(
                title = stringResource(R.string.profile_title_birth_details),
                icon = Icons.Default.AutoAwesome,
                helper = stringResource(R.string.profile_text_birth_helper)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassInlineTextField(
                    icon = Icons.Default.Person,
                    label = stringResource(R.string.profile_label_full_name),
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    error = nameError,
                    errorText = stringResource(R.string.profile_error_name_required),
                    trailingIcon = Icons.Default.Edit
                )

                GlassDropdownField(
                    icon = Icons.Default.Wc,
                    label = stringResource(R.string.profile_label_gender),
                    selected = gender,
                    options = GenderOptions,
                    onSelected = { gender = it; genderError = false },
                    optionLabel = { stringResource(getGenderResId(it)) },
                    error = genderError
                )

                GlassDateField(
                    icon = Icons.Default.CalendarToday,
                    label = stringResource(R.string.profile_label_dob),
                    value = dob,
                    onValueChange = { dob = it; dobError = false },
                    error = dobError,
                    errorText = stringResource(R.string.profile_error_dob_required)
                )

                GlassTimeField(
                    icon = Icons.Default.AccessTime,
                    label = stringResource(R.string.profile_label_tob),
                    value = tob,
                    onValueChange = { tob = it; tobError = false },
                    error = tobError,
                    errorText = stringResource(R.string.profile_error_tob_required)
                )

                GlassPlaceField(
                    icon = Icons.Default.LocationOn,
                    label = stringResource(R.string.profile_label_pob),
                    value = pob,
                    onValueChange = { pob = it; pobError = false },
                    onLocationSelected = { location ->
                        pob = location.name; pobError = false
                        birthPlaceName = location.name
                        birthLatitude = location.lat
                        birthLongitude = location.lon
                        birthTimezoneName = location.timezone
                    },
                    searchLocations = { viewModel.searchLocations(it) },
                    error = pobError,
                    errorText = stringResource(R.string.profile_error_pob_required)
                )

                HelperRow(text = stringResource(R.string.profile_text_pob_helper))
            }

            SectionHeader(
                title = stringResource(R.string.profile_title_personal_details),
                icon = Icons.Default.Person,
                helper = stringResource(R.string.profile_text_personal_helper)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassInlineTextField(
                    icon = Icons.Default.Phone,
                    label = stringResource(R.string.profile_label_phone_number),
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = stringResource(R.string.profile_label_add_phone),
                    trailingIcon = Icons.Default.Edit,
                    keyboardType = KeyboardType.Phone
                )

                GlassDropdownField(
                    icon = Icons.Default.FavoriteBorder,
                    label = stringResource(R.string.profile_label_marital_status),
                    selected = maritalStatus,
                    options = MaritalStatusOptions,
                    onSelected = { maritalStatus = it },
                    optionLabel = { stringResource(getMaritalStatusResId(it)) }
                )

                GlassDropdownField(
                    icon = Icons.Default.Work,
                    label = stringResource(R.string.profile_label_occupation),
                    selected = occupation,
                    options = OccupationOptions,
                    onSelected = { occupation = it },
                    optionLabel = { stringResource(getOccupationResId(it)) }
                )
            }

            SectionHeader(
                title = stringResource(R.string.profile_title_account_security),
                icon = Icons.Default.Shield
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassReadField(
                    icon = Icons.Default.Email,
                    label = stringResource(R.string.profile_label_email),
                    value = user.email,
                    trailingIcon = Icons.Default.Lock
                )

                GlassActionRow(
                    icon = Icons.Default.WorkspacePremium,
                    label = stringResource(R.string.profile_label_manage_sub),
                    onClick = onNavigateToPlans
                )

                LogoutButton(onClick = { viewModel.logout { onAccountDeleted() } })
            }

            SectionHeader(
                title = stringResource(R.string.profile_title_danger_zone),
                icon = Icons.Default.Warning,
                tint = MaterialTheme.colorScheme.error
            )
            GlassActionRow(
                icon = Icons.Default.DeleteForever,
                label = stringResource(R.string.profile_btn_delete_account),
                onClick = { showDeleteDialog = true },
                danger = true
            )

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(stringResource(R.string.profile_dialog_delete_title)) },
                    text = { Text(stringResource(R.string.profile_dialog_delete_text)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteDialog = false
                                viewModel.deleteAccount { onAccountDeleted() }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text(stringResource(R.string.profile_dialog_delete_confirm)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(R.string.profile_dialog_delete_dismiss))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        StickyProfileAction(
            isProfileComplete = isProfileComplete,
            hasChanges = hasChanges,
            isUpdating = isUpdating,
            onClick = {
                var hasError = false
                if (name.isBlank()) { nameError = true; hasError = true }
                if (gender.isBlank()) { genderError = true; hasError = true }
                if (dob.isBlank()) { dobError = true; hasError = true }
                if (tob.isBlank()) { tobError = true; hasError = true }
                if (pob.isBlank()) { pobError = true; hasError = true }
                if (hasError) return@StickyProfileAction

                viewModel.updateProfile(
                    user.copy(
                        name = name,
                        dob = dob,
                        tob = tob,
                        pob = pob,
                        birthPlaceName = birthPlaceName,
                        birthLatitude = birthLatitude,
                        birthLongitude = birthLongitude,
                        birthTimezoneName = birthTimezoneName,
                        phoneNumber = phoneNumber,
                        gender = gender,
                        maritalStatus = maritalStatus,
                        occupation = occupation
                    )
                )
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ===== Hero Card =====

@Composable
private fun ProfileHeroCard(
    user: User,
    name: String,
    isProfileComplete: Boolean,
    onViewKundli: () -> Unit
) {
    val metrics = responsiveMetrics()
    val gold = MaterialTheme.colorScheme.secondary
    val purple = MaterialTheme.colorScheme.primary
    val tier = (user.tier ?: "free").lowercase()
    val isPremium = tier == "pro" || tier == "premium"
    val (lagna, moon, sun) = resolveSigns(user)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, purple.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar with gold gradient ring + pencil overlay (decorative)
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(Brush.sweepGradient(listOf(gold, purple, gold))),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = purple,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    // Pencil overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(gold)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = greetingForNow(),
                            color = purple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = purple,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = name.ifBlank { stringResource(R.string.profile_label_astra_user) },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = user.email,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badge row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Badge(
                    icon = Icons.Default.WorkspacePremium,
                    text = stringResource(
                        R.string.profile_label_member_suffix,
                        (user.tier ?: "Free").titleCase()
                    ),
                    color = if (isPremium) gold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Badge(
                    icon = if (isProfileComplete) Icons.Default.Verified else Icons.Default.ErrorOutline,
                    text = if (isProfileComplete)
                        stringResource(R.string.profile_label_verified)
                    else
                        stringResource(R.string.profile_label_setup_needed),
                    color = if (isProfileComplete) purple else MaterialTheme.colorScheme.error
                )
            }

            // Sign chips (only show if we have at least one sign)
            if (lagna != null || moon != null || sun != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    lagna?.let { SignChip(label = stringResource(R.string.profile_label_lagna), value = it, icon = Icons.Default.Adjust, modifier = Modifier.weight(1f)) }
                    moon?.let { SignChip(label = stringResource(R.string.profile_label_moon), value = it, icon = Icons.Default.NightsStay, modifier = Modifier.weight(1f)) }
                    sun?.let { SignChip(label = stringResource(R.string.profile_label_sun), value = it, icon = Icons.Default.WbSunny, modifier = Modifier.weight(1f)) }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // View Kundli CTA
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onViewKundli),
                shape = RoundedCornerShape(14.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, gold.copy(alpha = 0.55f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.profile_btn_view_kundli),
                        color = gold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = gold
                    )
                }
            }
        }
    }
}

@Composable
private fun Badge(icon: ImageVector, text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
private fun SignChip(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            Column {
                Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), fontWeight = FontWeight.Medium)
                Text(value, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ===== Chart Accuracy Card =====

@Composable
private fun ChartAccuracyCard(completed: Int, total: Int, progress: Float) {
    val metrics = responsiveMetrics()
    val gold = MaterialTheme.colorScheme.secondary
    val purple = MaterialTheme.colorScheme.primary
    val isComplete = completed >= total

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
        border = BorderStroke(0.8.dp, purple.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(metrics.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left icon: purple circle with gold ring, optional check badge
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(purple.copy(alpha = 0.18f))
                        .border(2.dp, gold.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = gold,
                        modifier = Modifier.size(28.dp)
                    )
                }
                if (isComplete) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(gold)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_card_chart_accuracy),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.profile_text_details_complete, completed, total),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = gold
                )
                Text(
                    text = if (isComplete)
                        stringResource(R.string.profile_text_predictions_personalized)
                    else
                        stringResource(R.string.profile_hint_completion_needed),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = gold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

// ===== Section header =====

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    helper: String? = null,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            color = tint,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold
        )
        if (helper != null) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = helper,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun HelperRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
    }
}

// ===== Glass field container =====

@Composable
private fun GlassFieldRow(
    icon: ImageVector,
    label: String,
    error: Boolean = false,
    errorText: String? = null,
    onRowClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
    valueContent: @Composable () -> Unit
) {
    val borderColor = if (error)
        MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
    else
        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    Column {
        val rowModifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f))
            .border(0.8.dp, borderColor, RoundedCornerShape(18.dp))
            .let { if (onRowClick != null) it.clickable(onClick = onRowClick) else it }
            .padding(horizontal = 14.dp, vertical = 10.dp)

        Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.weight(1.4f),
                contentAlignment = Alignment.CenterEnd
            ) { valueContent() }
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }

        if (error && errorText != null) {
            Text(
                text = errorText,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 14.dp, top = 4.dp)
            )
        }
    }
}

// ===== Glass field variants =====

@Composable
private fun GlassReadField(
    icon: ImageVector,
    label: String,
    value: String,
    trailingIcon: ImageVector? = null
) {
    GlassFieldRow(
        icon = icon,
        label = label,
        trailing = {
            if (trailingIcon != null) {
                Icon(
                    trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    ) {
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GlassInlineTextField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    trailingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: Boolean = false,
    errorText: String? = null
) {
    GlassFieldRow(
        icon = icon,
        label = label,
        error = error,
        errorText = errorText,
        trailing = {
            if (trailingIcon != null) {
                Icon(
                    trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End
            ),
            decorationBox = { inner ->
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                inner()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassDropdownField(
    icon: ImageVector,
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    optionLabel: @Composable (String) -> String,
    error: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        GlassFieldRow(
            icon = icon,
            label = label,
            error = error,
            onRowClick = { expanded = true },
            trailing = {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        ) {
            Text(
                text = if (selected.isEmpty())
                    stringResource(R.string.profile_select_field, label)
                else
                    optionLabel(selected),
                fontSize = 14.sp,
                color = if (selected.isEmpty())
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                else
                    MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                DropdownMenuItem(
                    text = {
                        Text(
                            text = optionLabel(option),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    modifier = if (isSelected)
                        Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    else Modifier
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassDateField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: Boolean = false,
    errorText: String? = null
) {
    var showPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val display = remember(value) {
        if (value.isNotEmpty()) try {
            LocaleFormatter.displayDate(value, Locale.US, "dd-MM-yyyy")
        } catch (_: Exception) { value }
        else ""
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showPicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        onValueChange(sdf.format(Date(millis)))
                    }
                }) { Text(stringResource(R.string.common_btn_ok), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.common_btn_cancel))
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    GlassFieldRow(
        icon = icon,
        label = label,
        error = error,
        errorText = errorText,
        onRowClick = { showPicker = true },
        trailing = {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    ) {
        Text(
            text = display,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}

@Composable
private fun GlassTimeField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: Boolean = false,
    errorText: String? = null
) {
    // Internal editing is digit-only (HHMM). Display is HH:MM.
    val internal = value.replace(":", "")
    GlassFieldRow(
        icon = icon,
        label = label,
        error = error,
        errorText = errorText,
        trailing = {
            Icon(
                Icons.Default.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    ) {
        BasicTextField(
            value = internal,
            onValueChange = { input ->
                if (input.length <= 4 && input.all { c -> c.isDigit() }) {
                    if (input.length == 4) {
                        onValueChange("${input.substring(0, 2)}:${input.substring(2, 4)}")
                    } else {
                        onValueChange(input)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End
            ),
            decorationBox = { inner ->
                if (internal.isEmpty()) {
                    Text(
                        text = "--:--",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // Inline display with colon: rebuild from internal because BasicTextField
                // shows raw text. We hide the raw input via opacity and render display Text.
                val display = when (internal.length) {
                    in 0..2 -> internal
                    3 -> "${internal.substring(0, 2)}:${internal.substring(2)}"
                    4 -> "${internal.substring(0, 2)}:${internal.substring(2)}"
                    else -> internal
                }
                if (internal.isNotEmpty()) {
                    Text(
                        text = display,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // We still need the inner() to capture focus/cursor — but visually it
                // duplicates the text. Wrap in zero-height box.
                Box(modifier = Modifier.size(0.dp)) { inner() }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassPlaceField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onLocationSelected: (LocationSearchResult) -> Unit,
    searchLocations: suspend (String) -> List<LocationSearchResult>,
    error: Boolean = false,
    errorText: String? = null
) {
    var searchResults by remember { mutableStateOf<List<LocationSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (value.length >= 2) {
            delay(300)
            isSearching = true
            try {
                searchResults = searchLocations(value)
                showResults = true
            } catch (_: Exception) {
                searchResults = emptyList()
            } finally {
                isSearching = false
            }
        } else {
            searchResults = emptyList()
            showResults = false
        }
    }

    ExposedDropdownMenuBox(
        expanded = showResults && isFocused && searchResults.isNotEmpty(),
        onExpandedChange = {}
    ) {
        GlassFieldRow(
            icon = icon,
            label = label,
            error = error,
            errorText = errorText,
            trailing = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        ) {
            BasicTextField(
                value = value,
                onValueChange = {
                    onValueChange(it)
                    showResults = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .bringIntoViewOnFocus()
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        if (!focusState.isFocused) showResults = false
                    },
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End
                ),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.profile_select_field, label),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    inner()
                }
            )
        }
        ExposedDropdownMenu(
            expanded = showResults && isFocused && searchResults.isNotEmpty(),
            onDismissRequest = { showResults = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            searchResults.forEach { location ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(location.name, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${location.lat}, ${location.lon} | ${location.timezone}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = {
                        onLocationSelected(location)
                        onValueChange(location.name)
                        showResults = false
                        isFocused = false
                    }
                )
            }
        }
    }
}

// ===== Account rows =====

@Composable
private fun GlassActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    val tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val borderColor = if (danger)
        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
    else
        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val bg = if (danger)
        MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(0.8.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = tint,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = tint.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
    ) {
        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.profile_btn_logout), fontWeight = FontWeight.SemiBold)
    }
}

// ===== Sticky bottom action =====

@Composable
fun StickyProfileAction(
    isProfileComplete: Boolean,
    hasChanges: Boolean,
    isUpdating: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = responsiveMetrics()
    val gold = MaterialTheme.colorScheme.secondary

    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = metrics.pagePadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = gold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasChanges)
                        stringResource(R.string.profile_text_unsaved_changes)
                    else
                        stringResource(R.string.profile_text_no_changes),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (hasChanges)
                        stringResource(R.string.profile_text_tap_save)
                    else
                        stringResource(R.string.profile_text_up_to_date),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClick,
                enabled = hasChanges && !isUpdating,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.heightIn(min = 44.dp)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_state_saving), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                } else if (!isProfileComplete) {
                    Text(stringResource(R.string.profile_btn_complete_profile), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                } else {
                    Text(stringResource(R.string.profile_btn_save_changes), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@PreviewMultiDevice
@Composable
fun ProfileHeroCardPreview() {
    AstraNaviTheme {
        Surface {
            // Preview-only stub user
            val u = User(
                id = "preview",
                email = "seeker@cosmos.com",
                name = "Astra Seeker",
                tier = "Premium",
                lagnaSign = "Cancer",
                moonSign = "Gemini",
                sunSign = "Libra"
            )
            ProfileHeroCard(user = u, name = u.name ?: "", isProfileComplete = true, onViewKundli = {})
        }
    }
}
