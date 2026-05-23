package com.astranavi.app.ui.chat

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.model.ChatAvatar
import com.astranavi.app.data.repository.AstrologyRepository
import kotlinx.coroutines.launch

class AvatarSelectionViewModel(
    private val repository: AstrologyRepository
) : ViewModel() {

    private val _avatars = mutableStateOf<List<ChatAvatar>>(FallbackChatAvatarCatalog.avatars)
    val avatars: State<List<ChatAvatar>> = _avatars

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _defaultAvatarId = mutableStateOf(FallbackChatAvatarCatalog.defaultAvatarId)
    val defaultAvatarId: State<String> = _defaultAvatarId

    init {
        fetchAvatars()
    }

    fun fetchAvatars() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getChatAvatars()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.avatars.isNotEmpty()) {
                        _avatars.value = body.avatars
                        _defaultAvatarId.value = body.defaultAvatarId
                        verifyAvatarCatalog(body.avatars)
                    }
                }
            } catch (_: Exception) {
                // Keep fallback catalog on network failure.
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun findAvatar(avatarId: String?): ChatAvatar? {
        if (avatarId == null) return _avatars.value.firstOrNull { it.avatarId == _defaultAvatarId.value }
        return _avatars.value.firstOrNull { it.avatarId == avatarId }
    }
}
