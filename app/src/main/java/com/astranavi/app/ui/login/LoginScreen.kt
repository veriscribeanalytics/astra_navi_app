package com.astranavi.app.ui.login

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.astranavi.app.R
import com.astranavi.app.ui.components.LanguageChip
import com.astranavi.app.ui.components.bringIntoViewOnFocus
import com.astranavi.app.ui.components.responsiveMetrics

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    registrationViewModel: RegistrationViewModel,
    currentLanguage: String,
    isRegisterMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onTriggerAuthAction: (Boolean) -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isLoginLoading by loginViewModel.isLoading
    val isRegLoading by registrationViewModel.isLoading
    val isSubmitting = if (isRegisterMode) isRegLoading else isLoginLoading

    val metrics = responsiveMetrics()

    val emailValue = if (isRegisterMode) registrationViewModel.email.value else loginViewModel.email.value
    val passwordValue = if (isRegisterMode) registrationViewModel.password.value else loginViewModel.password.value
    val confirmPasswordValue = registrationViewModel.confirmPassword.value

    val loginError = loginViewModel.errorMessage.value
    val regError = registrationViewModel.errorMessage.value
    val errorMessage = if (isRegisterMode) regError else loginError

    val onSubmit = {
        if (!isSubmitting) {
            if (isRegisterMode) {
                registrationViewModel.register()
                if (registrationViewModel.errorMessage.value == null && registrationViewModel.isLoading.value) {
                    onTriggerAuthAction(true)
                }
            } else {
                loginViewModel.login()
                if (loginViewModel.errorMessage.value == null && loginViewModel.isLoading.value) {
                    onTriggerAuthAction(false)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(metrics.pagePadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isRegisterMode) stringResource(R.string.login_register_title) else stringResource(R.string.login_login_title),
                style = if (metrics.isCompactHeight || metrics.isLargeFont) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(if (metrics.isCompactHeight || metrics.isLargeFont) 20.dp else 32.dp))

            OutlinedTextField(
                value = emailValue,
                onValueChange = {
                    if (isRegisterMode) registrationViewModel.onEmailChange(it)
                    else loginViewModel.onEmailChange(it)
                },
                label = { Text(stringResource(R.string.login_label_email)) },
                modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(),
                enabled = !isSubmitting
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = passwordValue,
                onValueChange = {
                    if (isRegisterMode) registrationViewModel.onPasswordChange(it)
                    else loginViewModel.onPasswordChange(it)
                },
                label = { Text(stringResource(R.string.login_label_password)) },
                modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = stringResource(R.string.login_desc_toggle_password)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSubmit() }
                ),
                enabled = !isSubmitting
            )

            if (isRegisterMode) {
                Spacer(modifier = Modifier.height(if (metrics.isCompactHeight) 12.dp else 16.dp))
                OutlinedTextField(
                    value = confirmPasswordValue,
                    onValueChange = { registrationViewModel.onConfirmPasswordChange(it) },
                    label = { Text(stringResource(R.string.login_label_confirm_password)) },
                    modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSubmit() }
                    ),
                    enabled = !isSubmitting
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(if (metrics.isCompactHeight) 12.dp else 16.dp))
                Text(
                    text = errorMessage.asString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(if (metrics.isCompactHeight || metrics.isLargeFont) 16.dp else 24.dp))

            Button(
                onClick = { onSubmit() },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                enabled = !isSubmitting
            ) {
                Text(if (isRegisterMode) stringResource(R.string.login_register_title) else stringResource(R.string.login_login_title))
            }

            TextButton(
                onClick = {
                    loginViewModel.clearError()
                    registrationViewModel.clearError()
                    onModeChange(!isRegisterMode)
                },
                enabled = !isSubmitting
            ) {
                Text(if (isRegisterMode) stringResource(R.string.login_switch_to_login) else stringResource(R.string.login_switch_to_register))
            }
        }

        LanguageChip(
            currentLanguage = currentLanguage,
            onLanguageSelected = onLanguageSelected,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 12.dp, start = 12.dp)
        )
    }
}
