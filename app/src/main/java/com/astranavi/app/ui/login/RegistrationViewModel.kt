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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<UiText?>(null)
    val errorMessage: State<UiText?> = _errorMessage

    private val _isRegistrationSuccess = mutableStateOf(false)
    val isRegistrationSuccess: State<Boolean> = _isRegistrationSuccess

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        if (_errorMessage.value != null) _errorMessage.value = null
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        if (_errorMessage.value != null) _errorMessage.value = null
    }

    fun onConfirmPasswordChange(newPassword: String) {
        _confirmPassword.value = newPassword
        if (_errorMessage.value != null) _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun reset() {
        _errorMessage.value = null
        _isLoading.value = false
        _isRegistrationSuccess.value = false
    }

    fun register() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _errorMessage.value = UiText.StringResource(R.string.error_email_password_required)
            return
        }

        if (!ApiErrorParser.isValidEmail(_email.value)) {
            _errorMessage.value = UiText.StringResource(R.string.error_invalid_email_format)
            return
        }

        if (_password.value != _confirmPassword.value) {
            _errorMessage.value = UiText.StringResource(R.string.error_passwords_do_not_match)
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val regResponse = repository.register(_email.value, _password.value)
                if (regResponse.isSuccessful && regResponse.body() != null) {
                    val data = regResponse.body()!!
                    val user = data.user
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
                        accessToken = data.accessToken,
                        refreshToken = data.refreshToken,
                        profileComplete = data.profileComplete
                    )

                    try {
                        val localLanguage = sessionManager.userLanguage.first()
                        if (localLanguage != user.language) {
                            repository.updateLanguage(localLanguage)
                        }
                    } catch (_: Exception) {
                    }

                    _isRegistrationSuccess.value = true
                } else {
                    _errorMessage.value = ApiErrorParser.parse(regResponse)
                }
            } catch (e: Exception) {
                _errorMessage.value = UiText.DynamicString(ErrorSanitizer.sanitize(e))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
