package com.astranavi.app.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.astranavi.app.ui.components.PreviewMultiDevice
import com.astranavi.app.ui.theme.AstraNaviTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.R
import com.astranavi.app.data.model.LocationSearchResult
import com.astranavi.app.data.model.User
import com.astranavi.app.ui.components.AstroDatePickerField
import com.astranavi.app.ui.components.AstroTimePickerField
import com.astranavi.app.ui.components.LocationSearchField
import com.astranavi.app.ui.components.bringIntoViewOnFocus
import com.astranavi.app.ui.components.responsiveMetrics
import androidx.compose.foundation.shape.CircleShape
import com.astranavi.app.ui.components.ShimmerBlock
import com.astranavi.app.ui.components.shimmerEffect
import com.astranavi.app.ui.components.titleCase

private val GenderOptions = listOf("Male", "Female", "Other", "Not Specified")
private val MaritalStatusOptions = listOf("Single", "Married", "Divorced", "Widowed", "Not Specified")
private val OccupationOptions = listOf("Student", "Business", "Employed", "Homemaker", "Retired", "Unemployed", "Not Specified")

private fun getGenderResId(key: String): Int {
    return when (key) {
        "Male" -> R.string.profile_option_male
        "Female" -> R.string.profile_option_female
        "Other" -> R.string.profile_option_other
        else -> R.string.profile_option_not_specified
    }
}

private fun getMaritalStatusResId(key: String): Int {
    return when (key) {
        "Single" -> R.string.profile_option_single
        "Married" -> R.string.profile_option_married
        "Divorced" -> R.string.profile_option_divorced
        "Widowed" -> R.string.profile_option_widowed
        else -> R.string.profile_option_not_specified
    }
}

private fun getOccupationResId(key: String): Int {
    return when (key) {
        "Student" -> R.string.profile_option_student
        "Business" -> R.string.profile_option_business
        "Employed" -> R.string.profile_option_employed
        "Homemaker" -> R.string.profile_option_homemaker
        "Retired" -> R.string.profile_option_retired
        "Unemployed" -> R.string.profile_option_unemployed
        else -> R.string.profile_option_not_specified
    }
}

private fun isRequiredProfileComplete(name: String, gender: String, dob: String, tob: String, pob: String): Boolean {
    return listOf(name, gender, dob, tob, pob).all { it.isNotBlank() }
}

private fun profileCompletionProgress(name: String, gender: String, dob: String, tob: String, pob: String): Pair<Int, Int> {
    val required = listOf(name, gender, dob, tob, pob)
    return Pair(required.count { it.isNotBlank() }, required.size)
}

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onOpenDrawer: () -> Unit = {}, onBack: () -> Unit = {}, onProfileComplete: () -> Unit = {}, onAccountDeleted: () -> Unit = {}) {
    com.astranavi.app.util.SecureScreen()
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState = viewModel.uiState.value
    val snackbarHostState = remember { SnackbarHostState() }
    val updateMessage = viewModel.updateMessage.value
    val isProfileComplete by viewModel.profileComplete

    // Profile data is fetched automatically in the ViewModel init block

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
                is ProfileState.Error -> Text(uiState.message.asString(), modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                is ProfileState.Success -> ProfileContent(uiState.user, viewModel, isProfileComplete, onProfileComplete, onAccountDeleted)
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
                .padding(top = metrics.pagePadding, bottom = metrics.heroBottomPadding * 2.5f),
            verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().height(260.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(metrics.cardPadding).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(metrics.profileAvatarSize).clip(CircleShape).shimmerEffect())
                    Spacer(modifier = Modifier.height(metrics.profileSectionGap / 2))
                    Box(modifier = Modifier.width(metrics.profileFieldWidth * 0.5f).height(metrics.profileSectionGap / 2).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(modifier = Modifier.height(metrics.profileSectionGap / 3))
                    Box(modifier = Modifier.width(metrics.profileFieldWidth * 0.3f).height(metrics.profileSectionGap / 3).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(modifier = Modifier.height(metrics.profileSectionGap / 2))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(2) { Box(modifier = Modifier.height(24.dp).weight(1f).clip(RoundedCornerShape(4.dp)).shimmerEffect()) }
                    }
                    Spacer(modifier = Modifier.height(metrics.profileSectionGap / 2))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) { Box(modifier = Modifier.height(28.dp).weight(1f).clip(RoundedCornerShape(12.dp)).shimmerEffect()) }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().height(110.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(metrics.cardPadding)) {
                    Box(modifier = Modifier.fillMaxWidth().height(metrics.profileSectionGap / 3).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(modifier = Modifier.height(metrics.profileSectionGap / 2))
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 2)) {
                Box(modifier = Modifier.width(metrics.profileFieldWidth * 0.4f).height(metrics.profileSectionGap / 2).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                repeat(5) { ShimmerBlock(height = 64.dp, cornerRadius = 16.dp) }
            }

            Column(verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 2)) {
                Box(modifier = Modifier.width(metrics.profileFieldWidth * 0.4f).height(metrics.profileSectionGap / 2).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                ShimmerBlock(height = 64.dp, cornerRadius = 16.dp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 2)) {
                    repeat(2) { ShimmerBlock(height = 64.dp, modifier = Modifier.weight(1f), cornerRadius = 16.dp) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 2)) {
                Box(modifier = Modifier.width(metrics.profileFieldWidth * 0.4f).height(metrics.profileSectionGap / 2).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                ShimmerBlock(height = 64.dp, cornerRadius = 16.dp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 2)) {
                    repeat(2) { ShimmerBlock(height = 56.dp, modifier = Modifier.weight(1f), cornerRadius = 16.dp) }
                }
            }

            Spacer(modifier = Modifier.height(metrics.heroBottomPadding))
        }

        ShimmerBlock(height = 72.dp, modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(), cornerRadius = 16.dp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileContent(user: User, viewModel: ProfileViewModel, isProfileComplete: Boolean, onProfileComplete: () -> Unit, onAccountDeleted: () -> Unit) {
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
                .padding(top = metrics.pagePadding, bottom = metrics.heroBottomPadding * 2.5f),
            verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap)
        ) {
            if (metrics.isTabletWidth) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(metrics.profileSectionGap)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ProfileSummaryCard(
                            name = name,
                            email = user.email,
                            isProfileComplete = isProfileComplete,
                            tier = user.tier,
                            lagnaSign = user.lagnaSign,
                            moonSign = user.moonSign,
                            sunSign = user.sunSign
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ProfileCompletionCard(
                            completedRequired = completedRequired,
                            totalRequired = totalRequired,
                            completionProgress = completionProgress
                        )
                    }
                }
            } else {
                ProfileSummaryCard(
                    name = name,
                    email = user.email,
                    isProfileComplete = isProfileComplete,
                    tier = user.tier,
                    lagnaSign = user.lagnaSign,
                    moonSign = user.moonSign,
                    sunSign = user.sunSign
                )

                ProfileCompletionCard(
                    completedRequired = completedRequired,
                    totalRequired = totalRequired,
                    completionProgress = completionProgress
                )
            }

            if (metrics.useTabletTwoPane) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(metrics.profileSectionGap),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap)
                    ) {
                        ProfileSection(stringResource(R.string.profile_title_birth_profile), Icons.Default.AutoAwesome) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it; nameError = false },
                                label = { Text(stringResource(R.string.profile_label_full_name)) },
                                modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(),
                                shape = RoundedCornerShape(16.dp),
                                isError = nameError,
                                supportingText = if (nameError) { { Text(stringResource(R.string.profile_error_name_required)) } } else null
                            )

                            ProfileDropdown(
                                label = stringResource(R.string.profile_label_gender),
                                options = GenderOptions,
                                selectedOption = gender,
                                onOptionSelected = { gender = it; genderError = false },
                                optionToResId = { getGenderResId(it) },
                                modifier = Modifier.fillMaxWidth(),
                                isError = genderError
                            )

                            AstroDatePickerField(
                                value = dob,
                                onValueChange = { dob = it; dobError = false },
                                label = stringResource(R.string.profile_label_dob),
                                modifier = Modifier.fillMaxWidth(),
                                isError = dobError,
                                supportingText = if (dobError) { { Text(stringResource(R.string.profile_error_dob_required)) } } else null
                            )

                            AstroTimePickerField(
                                value = tob,
                                onValueChange = { tob = it; tobError = false },
                                label = stringResource(R.string.profile_label_tob),
                                modifier = Modifier.fillMaxWidth(),
                                isError = tobError,
                                supportingText = if (tobError) { { Text(stringResource(R.string.profile_error_tob_required)) } } else null
                            )

                            LocationSearchField(
                                value = pob,
                                onValueChange = { pob = it; pobError = false },
                                onLocationSelected = { location: LocationSearchResult ->
                                    pob = location.name; pobError = false
                                    birthPlaceName = location.name
                                    birthLatitude = location.lat
                                    birthLongitude = location.lon
                                    birthTimezoneName = location.timezone
                                },
                                searchLocations = { query -> viewModel.searchLocations(query) },
                                label = stringResource(R.string.profile_label_pob),
                                modifier = Modifier.fillMaxWidth(),
                                isError = pobError,
                                supportingText = if (pobError) { { Text(stringResource(R.string.profile_error_pob_required)) } } else null
                            )

                            Text(
                                text = stringResource(R.string.profile_hint_chart_accuracy),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = metrics.cardPadding)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap)
                    ) {
                        ProfileSection(stringResource(R.string.profile_title_personal_details), Icons.Default.Person) {
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text(stringResource(R.string.profile_label_phone_number)) },
                                modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(),
                                shape = RoundedCornerShape(16.dp)
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 2),
                                verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 2),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ProfileDropdown(
                                    label = stringResource(R.string.profile_label_marital_status),
                                    options = MaritalStatusOptions,
                                    selectedOption = maritalStatus,
                                    onOptionSelected = { maritalStatus = it },
                                    optionToResId = { getMaritalStatusResId(it) },
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                                )
                                ProfileDropdown(
                                    label = stringResource(R.string.profile_label_occupation),
                                    options = OccupationOptions,
                                    selectedOption = occupation,
                                    onOptionSelected = { occupation = it },
                                    optionToResId = { getOccupationResId(it) },
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                                )
                            }
                        }
                    }
                }
            } else {
                ProfileSection(stringResource(R.string.profile_title_birth_profile), Icons.Default.AutoAwesome) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; nameError = false },
                        label = { Text(stringResource(R.string.profile_label_full_name)) },
                        modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(),
                        shape = RoundedCornerShape(16.dp),
                        isError = nameError,
                        supportingText = if (nameError) { { Text(stringResource(R.string.profile_error_name_required)) } } else null
                    )

                    ProfileDropdown(
                        label = stringResource(R.string.profile_label_gender),
                        options = GenderOptions,
                        selectedOption = gender,
                        onOptionSelected = { gender = it; genderError = false },
                        optionToResId = { getGenderResId(it) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = genderError
                    )

                    AstroDatePickerField(
                        value = dob,
                        onValueChange = { dob = it; dobError = false },
                        label = stringResource(R.string.profile_label_dob),
                        modifier = Modifier.fillMaxWidth(),
                        isError = dobError,
                        supportingText = if (dobError) { { Text(stringResource(R.string.profile_error_dob_required)) } } else null
                    )

                    AstroTimePickerField(
                        value = tob,
                        onValueChange = { tob = it; tobError = false },
                        label = stringResource(R.string.profile_label_tob),
                        modifier = Modifier.fillMaxWidth(),
                        isError = tobError,
                        supportingText = if (tobError) { { Text(stringResource(R.string.profile_error_tob_required)) } } else null
                    )

                    LocationSearchField(
                        value = pob,
                        onValueChange = { pob = it; pobError = false },
                        onLocationSelected = { location: LocationSearchResult ->
                            pob = location.name; pobError = false
                            birthPlaceName = location.name
                            birthLatitude = location.lat
                            birthLongitude = location.lon
                            birthTimezoneName = location.timezone
                        },
                        searchLocations = { query -> viewModel.searchLocations(query) },
                        label = stringResource(R.string.profile_label_pob),
                        modifier = Modifier.fillMaxWidth(),
                        isError = pobError,
                        supportingText = if (pobError) { { Text(stringResource(R.string.profile_error_pob_required)) } } else null
                    )

                    Text(
                        text = stringResource(R.string.profile_hint_chart_accuracy),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = metrics.cardPadding)
                    )
                }

                ProfileSection(stringResource(R.string.profile_title_personal_details), Icons.Default.Person) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text(stringResource(R.string.profile_label_phone_number)) },
                        modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 2),
                        verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileDropdown(
                            label = stringResource(R.string.profile_label_marital_status),
                            options = MaritalStatusOptions,
                            selectedOption = maritalStatus,
                            onOptionSelected = { maritalStatus = it },
                            optionToResId = { getMaritalStatusResId(it) },
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                        ProfileDropdown(
                            label = stringResource(R.string.profile_label_occupation),
                            options = OccupationOptions,
                            selectedOption = occupation,
                            onOptionSelected = { occupation = it },
                            optionToResId = { getOccupationResId(it) },
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                    }
                }
            }

            AccountManagementSection(
                email = user.email,
                onDeleteClick = { showDeleteDialog = true },
                onLogoutClick = { viewModel.logout { onAccountDeleted() } }
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
                        ) {
                            Text(stringResource(R.string.profile_dialog_delete_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(R.string.profile_dialog_delete_dismiss))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(metrics.profileSectionGap / 2))
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

                viewModel.updateProfile(user.copy(
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
                ))
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSummaryCard(
    name: String,
    email: String,
    isProfileComplete: Boolean,
    tier: String?,
    lagnaSign: String?,
    moonSign: String?,
    sunSign: String?
) {
    val metrics = responsiveMetrics()
    val tierColor = when (tier?.lowercase()) {
        "pro" -> Color(0xFF7C3AED)
        "premium" -> Color(0xFFC8880A)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.size(metrics.profileAvatarSize), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(metrics.profileAvatarSize / 5), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(metrics.profileSectionGap / 2))
            Text(
                text = name.ifBlank { stringResource(R.string.profile_label_astra_user) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(metrics.profileSectionGap / 2))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 3),
                verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 3)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tierColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = stringResource(R.string.profile_label_member_suffix, (tier ?: "Free").titleCase()),
                        modifier = Modifier.padding(horizontal = metrics.cardPadding / 2, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tierColor
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isProfileComplete) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (isProfileComplete) stringResource(R.string.profile_label_verified) else stringResource(R.string.profile_label_setup_needed),
                        modifier = Modifier.padding(horizontal = metrics.cardPadding / 2, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfileComplete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
            if (lagnaSign != null || moonSign != null || sunSign != null) {
                Spacer(modifier = Modifier.height(metrics.profileSectionGap / 2))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 3),
                    verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 3)
                ) {
                    if (lagnaSign != null) ProfileInfoChip(stringResource(R.string.profile_label_lagna), lagnaSign)
                    if (moonSign != null) ProfileInfoChip(stringResource(R.string.profile_label_moon), moonSign)
                    if (sunSign != null) ProfileInfoChip(stringResource(R.string.profile_label_sun), sunSign)
                }
            }
        }
    }
}

@Composable
fun ProfileInfoChip(label: String, value: String) {
    val metrics = responsiveMetrics()
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = metrics.cardPadding / 2, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ProfileCompletionCard(completedRequired: Int, totalRequired: Int, completionProgress: Float) {
    val metrics = responsiveMetrics()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding)) {
            LinearProgressIndicator(
                progress = { completionProgress },
                modifier = Modifier.fillMaxWidth().height(metrics.profileSectionGap / 3).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(modifier = Modifier.height(metrics.profileSectionGap / 3))
            Text(
                text = stringResource(R.string.profile_text_completion_status, completedRequired, totalRequired),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            if (completedRequired < totalRequired) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.profile_hint_completion_needed),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AccountManagementSection(email: String, onDeleteClick: () -> Unit, onLogoutClick: () -> Unit) {
    val metrics = responsiveMetrics()
    Column(verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 2)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 3)) {
            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(metrics.kundliSmallIconSize), tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.profile_title_account_management), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = email,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.profile_label_email)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            trailingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) }
        )

        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(metrics.kundliSmallIconSize))
            Spacer(modifier = Modifier.width(metrics.profileSectionGap / 3))
            Text(stringResource(R.string.profile_btn_delete_account), fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(metrics.kundliSmallIconSize))
            Spacer(modifier = Modifier.width(metrics.profileSectionGap / 3))
            Text(stringResource(R.string.profile_btn_logout), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StickyProfileAction(
    isProfileComplete: Boolean,
    hasChanges: Boolean,
    isUpdating: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = responsiveMetrics()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.imePadding().navigationBarsPadding().padding(horizontal = metrics.pagePadding, vertical = metrics.profileSectionGap / 2)) {
            Button(
                onClick = onClick,
                enabled = hasChanges && !isUpdating,
                modifier = Modifier.fillMaxWidth().heightIn(min = metrics.buttonHeight),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(metrics.bottomNavIconSize * 1.2f), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(metrics.profileSectionGap / 3))
                    Text(stringResource(R.string.profile_state_saving), fontWeight = FontWeight.Bold)
                } else if (!isProfileComplete) {
                    Text(stringResource(R.string.profile_btn_complete_profile), fontWeight = FontWeight.Bold)
                } else {
                    Text(stringResource(R.string.profile_btn_save_changes), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    val metrics = responsiveMetrics()
    Column(verticalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 2)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(metrics.profileSectionGap / 3)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(metrics.kundliSmallIconSize), tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    optionToResId: (String) -> Int,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = if (selectedOption.isEmpty()) {
                stringResource(R.string.profile_select_field, label)
            } else {
                stringResource(optionToResId(selectedOption))
            },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
                .bringIntoViewOnFocus(),
            shape = RoundedCornerShape(16.dp),
            isError = isError
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.profile_select_field, label), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontWeight = FontWeight.Medium) },
                onClick = {
                    onOptionSelected("")
                    expanded = false
                }
            )

            options.forEach { option ->
                val isSelected = option == selectedOption
                val optionResId = optionToResId(option)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(optionResId),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    modifier = if (isSelected) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) else Modifier
                )
            }
        }
    }
}

@PreviewMultiDevice
@Composable
fun ProfileSummaryCardPreview() {
    AstraNaviTheme {
        Surface {
            ProfileSummaryCard(
                name = "Astra Seeker",
                email = "seeker@cosmos.com",
                isProfileComplete = true,
                tier = "Premium",
                lagnaSign = "Aries",
                moonSign = "Taurus",
                sunSign = "Leo"
            )
        }
    }
}
