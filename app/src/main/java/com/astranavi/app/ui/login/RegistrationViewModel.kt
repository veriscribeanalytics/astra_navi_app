package com.astranavi.app.ui.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.repository.AuthRepository
import com.astranavi.app.util.ErrorSanitizer
import com.astranavi.app.util.SessionManager
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

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isRegistrationSuccess = mutableStateOf(false)
    val isRegistrationSuccess: State<Boolean> = _isRegistrationSuccess

    fun onEmailChange(newEmail: String) { _email.value = newEmail }
    fun onPasswordChange(newPassword: String) { _password.value = newPassword }
    fun onConfirmPasswordChange(newPassword: String) { _confirmPassword.value = newPassword }

    fun register() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _errorMessage.value = "Email and Password are required"
            return
        }

        if (_password.value != _confirmPassword.value) {
            _errorMessage.value = "Passwords do not match"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // Register — API returns tokens directly, no separate login needed
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
                    _isRegistrationSuccess.value = true
                } else {
                    val errorBody = regResponse.errorBody()?.string() ?: ""
                    val message = try {
                        val json = org.json.JSONObject(errorBody)
                        // Try "detail" array first (422 validation errors)
                        if (json.has("detail")) {
                            val detail = json.getJSONArray("detail")
                            if (detail.length() > 0) {
                                val first = detail.getJSONObject(0)
                                first.optString("msg", "Registration failed")
                            } else {
                                "Registration failed"
                            }
                        } else {
                            json.optString("error", "Registration failed")
                        }
                    } catch (e: Exception) {
                        "Registration failed"
                    }
                    _errorMessage.value = message
                }
            } catch (e: Exception) {
                _errorMessage.value = ErrorSanitizer.sanitize(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
