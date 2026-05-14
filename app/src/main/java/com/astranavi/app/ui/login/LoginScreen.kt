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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.ui.components.bringIntoViewOnFocus

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    registrationViewModel: RegistrationViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    val isLoginLoading = loginViewModel.isLoading.value
    val isRegLoading = registrationViewModel.isLoading.value
    val isLoginSuccess = loginViewModel.isLoginSuccess.value
    val isRegistrationSuccess = registrationViewModel.isRegistrationSuccess.value

    LaunchedEffect(isLoginSuccess) {
        if (isLoginSuccess) onLoginSuccess()
    }
    
    LaunchedEffect(isRegistrationSuccess) {
        if (isRegistrationSuccess) onNavigateToProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoginLoading || isRegLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(if (isRegLoading) "Registering..." else "Logging in...")
        } else {
            Text(
                text = if (isRegisterMode) "Register" else "Log In",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = if (isRegisterMode) registrationViewModel.email.value else loginViewModel.email.value,
                onValueChange = { 
                    if (isRegisterMode) registrationViewModel.onEmailChange(it) 
                    else loginViewModel.onEmailChange(it) 
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = if (isRegisterMode) registrationViewModel.password.value else loginViewModel.password.value,
                onValueChange = { 
                    if (isRegisterMode) registrationViewModel.onPasswordChange(it) 
                    else loginViewModel.onPasswordChange(it) 
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (isRegisterMode) registrationViewModel.register() 
                        else loginViewModel.login()
                    }
                )
            )

            if (isRegisterMode) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = registrationViewModel.confirmPassword.value,
                    onValueChange = { registrationViewModel.onConfirmPasswordChange(it) },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth().bringIntoViewOnFocus(),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { registrationViewModel.register() }
                    )
                )
            }
            
            val errorMessage = if (isRegisterMode) registrationViewModel.errorMessage.value else loginViewModel.errorMessage.value
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { 
                    if (isRegisterMode) registrationViewModel.register() 
                    else loginViewModel.login() 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(if (isRegisterMode) "Register" else "Log In")
            }

            TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                Text(if (isRegisterMode) "Already have an account? Log In" else "Don't have an account? Register")
            }
        }
    }
}
