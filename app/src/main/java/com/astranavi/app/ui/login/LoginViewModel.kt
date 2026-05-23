package com.astranavi.app.ui.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.R
import com.astranavi.app.data.repository.AuthRepository
import com.astranavi.app.util.ApiErrorParser
import com.astranavi.app.util.ErrorSanitizer
import com.astranavi.app.util.SessionManager
import com.astranavi.app.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    private val _errorMessage = mutableStateOf<UiText?>(null)
    val errorMessage: State<UiText?> = _errorMessage

    private val _isLoginSuccess = mutableStateOf(false)
    val isLoginSuccess: State<Boolean> = _isLoginSuccess

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        if (_errorMessage.value != null) _errorMessage.value = null
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        if (_errorMessage.value != null) _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun reset() {
        _errorMessage.value = null
        _isLoading.value = false
        _isLoginSuccess.value = false
    }

    fun login() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _errorMessage.value = UiText.StringResource(R.string.error_email_password_required)
            return
        }

        if (!ApiErrorParser.isValidEmail(_email.value)) {
            _errorMessage.value = UiText.StringResource(R.string.error_invalid_email_format)
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = repository.login(_email.value, _password.value)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    val user = loginResponse.user

                    if (loginResponse.accessToken.isNullOrBlank()) {
                        _errorMessage.value = UiText.StringResource(R.string.error_missing_access_token)
                        return@launch
                    }

                    withContext(Dispatchers.IO) {
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

                    try {
                        val localLanguage = sessionManager.userLanguage.first()
                        if (localLanguage != user.language) {
                            repository.updateLanguage(localLanguage)
                        }
                    } catch (_: Exception) {
                    }

                    _isLoginSuccess.value = true
                } else {
                    _errorMessage.value = ApiErrorParser.parse(response)
                    if (response.code() == 401) {
                        _password.value = ""
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = UiText.DynamicString(ErrorSanitizer.sanitize(e))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
