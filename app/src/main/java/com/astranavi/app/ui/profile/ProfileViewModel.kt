package com.astranavi.app.ui.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.R
import com.astranavi.app.data.api.RetrofitClient
import com.astranavi.app.data.model.LocationSearchResult
import com.astranavi.app.data.model.ProfileResponse
import com.astranavi.app.data.model.User
import com.astranavi.app.data.repository.AuthRepository
import com.astranavi.app.util.ErrorSanitizer
import com.astranavi.app.util.SessionManager
import com.astranavi.app.util.UiText
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: UiText) : ProfileState()
}

class ProfileViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val apiService = RetrofitClient.instance

    private val _uiState = mutableStateOf<ProfileState>(ProfileState.Loading)
    val uiState: State<ProfileState> = _uiState

    private val _isUpdating = mutableStateOf(false)
    val isUpdating: State<Boolean> = _isUpdating

    private val _updateMessage = mutableStateOf<UiText?>(null)
    val updateMessage: State<UiText?> = _updateMessage

    private val _profileComplete = mutableStateOf(false)
    val profileComplete: State<Boolean> = _profileComplete

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        if (_uiState.value is ProfileState.Success) return
        viewModelScope.launch {
            _uiState.value = ProfileState.Loading
            try {
                val response = repository.getProfile()
                if (response.isSuccessful && response.body()?.user != null) {
                    val user = response.body()!!.user!!
                    _uiState.value = ProfileState.Success(user)
                    user.language?.takeIf { it.isNotBlank() }?.let {
                        sessionManager.setUserLanguage(it)
                    }
                    // Check if profile is complete (dob, tob, pob all filled)
                    val isComplete = !user.name.isNullOrBlank() &&
                            !user.gender.isNullOrBlank() &&
                            !user.dob.isNullOrBlank() &&
                            !user.tob.isNullOrBlank() &&
                            !user.pob.isNullOrBlank()
                    _profileComplete.value = isComplete
                } else {
                    _uiState.value = ProfileState.Error(UiText.StringResource(R.string.profile_error_fetch_failed))
                }
            } catch (e: Exception) {
                _uiState.value = ProfileState.Error(UiText.DynamicString(ErrorSanitizer.sanitize(e)))
            }
        }
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            _isUpdating.value = true
            _updateMessage.value = null
            try {
                val response = repository.updateProfile(user)
                if (response.isSuccessful) {
                    val body = response.body()
                    val updatedUser = body?.user
                    if (updatedUser != null) {
                        _uiState.value = ProfileState.Success(updatedUser)
                        sessionManager.updateProfileData(
                            updatedUser.dob, updatedUser.tob, updatedUser.pob,
                            updatedUser.birthPlaceName, updatedUser.birthLatitude,
                            updatedUser.birthLongitude, updatedUser.birthTimezoneName,
                            updatedUser.birthTimezoneOffsetAtBirth, updatedUser.birthTimeFold
                        )
                        updatedUser.language?.takeIf { it.isNotBlank() }?.let {
                            sessionManager.setUserLanguage(it)
                        }
                    }
                    _updateMessage.value = if (body?.message != null) {
                        UiText.DynamicString(body.message)
                    } else {
                        UiText.StringResource(R.string.profile_success_updated)
                    }

                    val isComplete = if (updatedUser != null) {
                        !updatedUser.name.isNullOrBlank() &&
                                !updatedUser.gender.isNullOrBlank() &&
                                !updatedUser.dob.isNullOrBlank() &&
                                !updatedUser.tob.isNullOrBlank() &&
                                !updatedUser.pob.isNullOrBlank()
                    } else {
                        !user.name.isNullOrBlank() &&
                                !user.gender.isNullOrBlank() &&
                                !user.dob.isNullOrBlank() &&
                                !user.tob.isNullOrBlank() &&
                                !user.pob.isNullOrBlank()
                    }
                    _profileComplete.value = isComplete
                    if (isComplete) {
                        sessionManager.updateProfileComplete(true)
                    }
                } else {
                    // Parse 422 validation errors
                    val errorBody = response.errorBody()?.string() ?: ""
                    val message = try {
                        val json = org.json.JSONObject(errorBody)
                        if (json.has("detail")) {
                            val detail = json.getJSONArray("detail")
                            if (detail.length() > 0) {
                                val first = detail.getJSONObject(0)
                                val msgStr = first.optString("msg", "")
                                if (msgStr.isNotBlank()) UiText.DynamicString(msgStr) else UiText.StringResource(R.string.profile_error_save_failed)
                            } else {
                                UiText.StringResource(R.string.profile_error_save_failed)
                            }
                        } else {
                            UiText.StringResource(R.string.profile_error_save_failed)
                        }
                    } catch (e: Exception) {
                        UiText.StringResource(R.string.profile_error_save_failed)
                    }
                    _updateMessage.value = message
                }
            } catch (e: Exception) {
                _updateMessage.value = UiText.DynamicString(ErrorSanitizer.sanitize(e))
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun clearUpdateMessage() {
        _updateMessage.value = null
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearSession()
            onLogout()
        }
    }

    fun deleteAccount(onAccountDeleted: () -> Unit) {
        viewModelScope.launch {
            _isUpdating.value = true
            try {
                val response = repository.deleteUser()
                if (response.isSuccessful) {
                    sessionManager.clearSession()
                    onAccountDeleted()
                } else {
                    _updateMessage.value = UiText.StringResource(R.string.profile_error_delete_failed)
                }
            } catch (e: Exception) {
                _updateMessage.value = UiText.DynamicString(ErrorSanitizer.sanitize(e))
            } finally {
                _isUpdating.value = false
            }
        }
    }

    suspend fun searchLocations(query: String): List<LocationSearchResult> {
        return try {
            val response = apiService.searchLocations(query)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
