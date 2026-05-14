package com.astranavi.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astranavi.app.data.model.LocationSearchResult
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onLocationSelected: (LocationSearchResult) -> Unit,
    searchLocations: suspend (String) -> List<LocationSearchResult>,
    label: String = "Place of Birth *",
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
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
        onExpandedChange = {},
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newText ->
                onValueChange(newText)
                showResults = true
            },
            label = { Text(label) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .bringIntoViewOnFocus()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    if (!focusState.isFocused) {
                        showResults = false
                    }
                },
            shape = RoundedCornerShape(16.dp),
            isError = isError,
            supportingText = supportingText,
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.secondary
            ),
            singleLine = true
        )

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
