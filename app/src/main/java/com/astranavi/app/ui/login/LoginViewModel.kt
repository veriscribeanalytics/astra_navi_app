package com.astranavi.app.ui.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.repository.AuthRepository
import com.astranavi.app.util.ErrorSanitizer
import com.astranavi.app.util.SessionManager
import kotlinx.coroutines.launch

import android.util.Log
import com.astranavi.app.data.model.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isLoginSuccess = mutableStateOf(false)
    val isLoginSuccess: State<Boolean> = _isLoginSuccess

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun login() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _errorMessage.value = "Email and Password are required"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                Log.d("LoginFlow", "Starting login for: ${_email.value}")
                val response = repository.login(_email.value, _password.value)
                
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    val user = loginResponse.user
                    
                    Log.d("LoginFlow", "Response received. Token present: ${!loginResponse.accessToken.isNullOrBlank()}")

                    if (loginResponse.accessToken.isNullOrBlank()) {
                        _errorMessage.value = "Server error: Missing access token"
                        return@launch
                    }

                    // Move heavy serialization and I/O to background thread
                    withContext(Dispatchers.IO) {
                        Log.d("LoginFlow", "Saving session to DataStore...")
                        sessionManager.saveSession(
                            id = user.id,
                            email = user.email,
                            name = user.name,
                            moonSign = user.moonSign,
                            sunSign = user.sunSign,
                            lagnaSign = user.lagnaSign,
                            dob = user.dob,
                            tob = user.tob,
                            pob = user.pob,
                            accessToken = loginResponse.accessToken,
                            refreshToken = loginResponse.refreshToken,
                            profileComplete = loginResponse.profileComplete ?: false
                        )
                    }
                    
                    Log.d("LoginFlow", "Session saved. Triggering navigation.")
                    _isLoginSuccess.value = true
                } else {
                    val errorCode = response.code()
                    Log.e("LoginFlow", "Login failed with code: $errorCode")
                    _errorMessage.value = if (errorCode == 401) "Invalid email or password" else "Login failed ($errorCode)"
                }
            } catch (e: Exception) {
                Log.e("LoginFlow", "Exception during login", e)
                _errorMessage.value = ErrorSanitizer.sanitize(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
