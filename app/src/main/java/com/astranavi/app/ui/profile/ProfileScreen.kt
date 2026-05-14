package com.astranavi.app.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.data.model.LocationSearchResult
import com.astranavi.app.data.model.User
import com.astranavi.app.ui.components.AstroDatePickerField
import com.astranavi.app.ui.components.AstroTimePickerField
import com.astranavi.app.ui.components.LocationSearchField
import com.astranavi.app.ui.components.bringIntoViewOnFocus
import com.astranavi.app.ui.components.shimmerEffect

data class ProfileOption(val label: String, val value: String)

private val GenderOptions = listOf("Male", "Female", "Other", "Not Specified")
private val MaritalStatusOptions = listOf("Single", "Married", "Divorced", "Widowed", "Not Specified")
private val OccupationOptions = listOf("Student", "Business", "Employed", "Homemaker", "Retired", "Unemployed", "Not Specified")
private val LanguageOptions = listOf(ProfileOption("English", "en"), ProfileOption("Hindi", "hi"))

private fun isRequiredProfileComplete(name: String, gender: String, dob: String, tob: String, pob: String): Boolean {
    return listOf(name, gender, dob, tob, pob).all { it.isNotBlank() }
}

private fun profileCompletionProgress(name: String, gender: String, dob: String, tob: String, pob: String): Pair<Int, Int> {
    val required = listOf(name, gender, dob, tob, pob)
    return Pair(required.count { it.isNotBlank() }, required.size)
}

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onOpenDrawer: () -> Unit = {}, onBack: () -> Unit = {}, onProfileComplete: () -> Unit = {}, onAccountDeleted: () -> Unit = {}) {
    val uiState = viewModel.uiState.value
    val snackbarHostState = remember { SnackbarHostState() }
    val updateMessage = viewModel.updateMessage.value
    val isProfileComplete by viewModel.profileComplete

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    LaunchedEffect(updateMessage) {
        updateMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUpdateMessage()
            if (it.contains("Profile updated", ignoreCase = true) && isProfileComplete) {
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
                is ProfileState.Error -> Text(uiState.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                is ProfileState.Success -> ProfileContent(uiState.user, viewModel, isProfileComplete, onProfileComplete, onAccountDeleted)
            }
        }
    }
}

@Composable
fun ProfileSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.width(150.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }
        }

        repeat(2) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.width(140.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                repeat(3) {
                    Box(modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).shimmerEffect())
                }
            }
        }
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
    var gender by remember { mutableStateOf(user.gender ?: "") }
    var maritalStatus by remember { mutableStateOf(user.maritalStatus ?: "") }
    var occupation by remember { mutableStateOf(user.occupation ?: "") }
    var language by remember { mutableStateOf(user.language ?: "en") }

    var nameError by remember { mutableStateOf(false) }
    var dobError by remember { mutableStateOf(false) }
    var tobError by remember { mutableStateOf(false) }
    var pobError by remember { mutableStateOf(false) }
    var genderError by remember { mutableStateOf(false) }

    val isUpdating by viewModel.isUpdating

    val hasChanges = remember(user, name, dob, tob, pob, birthPlaceName, birthLatitude, birthLongitude, birthTimezoneName, phoneNumber, gender, maritalStatus, occupation, language) {
        name != (user.name ?: "") ||
        dob != (user.dob ?: "") ||
        tob != (user.tob ?: "") ||
        pob != (user.pob ?: "") ||
        birthPlaceName != (user.birthPlaceName ?: "") ||
        birthLatitude != user.birthLatitude ||
        birthLongitude != user.birthLongitude ||
        birthTimezoneName != (user.birthTimezoneName ?: "") ||
        phoneNumber != (user.phoneNumber ?: "") ||
        gender != (user.gender ?: "") ||
        maritalStatus != (user.maritalStatus ?: "") ||
        occupation != (user.occupation ?: "") ||
        language != (user.language ?: "en")
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
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProfileSummaryCard(
                name = name,
                email = user.email,
                isProfileComplete = isProfileComplete,
                lagnaSign = user.lagnaSign,
                moonSign = user.moonSign,
                sunSign = user.sunSign
            )

            ProfileCompletionCard(
                completedRequired = completedRequired,
                totalRequired = totalRequired,
                completionProgress = completionProgress
            )

            ProfileSection("BIRTH PROFILE", Icons.Default.AutoAwesome) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(),
                    shape = RoundedCornerShape(16.dp),
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Name is required") } } else null
                )

                ProfileDropdown(
                    label = "Gender *",
                    options = GenderOptions,
                    selectedOption = gender,
                    onOptionSelected = { gender = it; genderError = false },
                    modifier = Modifier.fillMaxWidth(),
                    isError = genderError
                )

                AstroDatePickerField(
                    value = dob,
                    onValueChange = { dob = it; dobError = false },
                    label = "Date of Birth *",
                    modifier = Modifier.fillMaxWidth(),
                    isError = dobError,
                    supportingText = if (dobError) { { Text("Date of Birth is required") } } else null
                )

                AstroTimePickerField(
                    value = tob,
                    onValueChange = { tob = it; tobError = false },
                    label = "Time of Birth *",
                    modifier = Modifier.fillMaxWidth(),
                    isError = tobError,
                    supportingText = if (tobError) { { Text("Time of Birth is required") } } else null
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
                    label = "Place of Birth *",
                    modifier = Modifier.fillMaxWidth(),
                    isError = pobError,
                    supportingText = if (pobError) { { Text("Place of Birth is required") } } else null
                )

                Text(
                    text = "Exact birth time and place improve chart accuracy.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            ProfileSection("PERSONAL DETAILS", Icons.Default.Person) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(),
                    shape = RoundedCornerShape(16.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileDropdown(
                        label = "Marital Status",
                        options = MaritalStatusOptions,
                        selectedOption = maritalStatus,
                        onOptionSelected = { maritalStatus = it },
                        modifier = Modifier.weight(1f)
                    )
                    ProfileDropdown(
                        label = "Occupation",
                        options = OccupationOptions,
                        selectedOption = occupation,
                        onOptionSelected = { occupation = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                LabeledProfileDropdown(
                    label = "Language",
                    options = LanguageOptions,
                    selectedValue = language,
                    onValueSelected = { language = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AccountManagementSection(
                email = user.email,
                onDeleteClick = { showDeleteDialog = true },
                onLogoutClick = { viewModel.logout { onAccountDeleted() } }
            )

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Account?") },
                    text = { Text("This action is permanent and cannot be undone. All your history and profile data will be lost forever.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteDialog = false
                                viewModel.deleteAccount { onAccountDeleted() }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("DELETE")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("CANCEL")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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
                    occupation = occupation,
                    language = language
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
    lagnaSign: String?,
    moonSign: String?,
    sunSign: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = name.ifBlank { "Astra User" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isProfileComplete) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            ) {
                Text(
                    text = if (isProfileComplete) "Profile Complete" else "Setup Needed",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isProfileComplete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            if (lagnaSign != null || moonSign != null || sunSign != null) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (lagnaSign != null) ProfileInfoChip("Lagna", lagnaSign)
                    if (moonSign != null) ProfileInfoChip("Moon", moonSign)
                    if (sunSign != null) ProfileInfoChip("Sun", sunSign)
                }
            }
        }
    }
}

@Composable
fun ProfileInfoChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            LinearProgressIndicator(
                progress = { completionProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$completedRequired of $totalRequired required details complete",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            if (completedRequired < totalRequired) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Complete these details for accurate horoscope, kundli, forecast, and match results.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AccountManagementSection(email: String, onDeleteClick: () -> Unit, onLogoutClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Text("ACCOUNT MANAGEMENT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = email,
            onValueChange = {},
            readOnly = true,
            label = { Text("Email") },
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
            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete Account", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontWeight = FontWeight.Bold)
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.imePadding().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Button(
                onClick = onClick,
                enabled = hasChanges && !isUpdating,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving...", fontWeight = FontWeight.Bold)
                } else if (!isProfileComplete) {
                    Text("Complete Profile", fontWeight = FontWeight.Bold)
                } else {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
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
            value = if (selectedOption.isEmpty()) "Select $label" else selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth().bringIntoViewOnFocus(),
            shape = RoundedCornerShape(16.dp),
            isError = isError
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text("Select $label", color = Color.Gray, fontWeight = FontWeight.Medium) },
                onClick = {
                    onOptionSelected("")
                    expanded = false
                }
            )

            options.forEach { option ->
                val isSelected = option == selectedOption
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledProfileDropdown(
    label: String,
    options: List<ProfileOption>,
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.value == selectedValue }?.label ?: selectedValue

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = if (selectedValue.isEmpty()) "Select $label" else selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth().bringIntoViewOnFocus(),
            shape = RoundedCornerShape(16.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text("Select $label", color = Color.Gray, fontWeight = FontWeight.Medium) },
                onClick = {
                    onValueSelected("")
                    expanded = false
                }
            )

            options.forEach { option ->
                val isSelected = option.value == selectedValue
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onValueSelected(option.value)
                        expanded = false
                    },
                    modifier = if (isSelected) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) else Modifier
                )
            }
        }
    }
}
