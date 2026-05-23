package com.astranavi.app.ui.astrologers

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.model.ChatAvatar
import com.astranavi.app.data.repository.AstrologyRepository
import com.astranavi.app.ui.chat.FallbackChatAvatarCatalog
import com.astranavi.app.ui.chat.verifyAvatarCatalog
import kotlinx.coroutines.launch

class AstrologersViewModel(
    private val repository: AstrologyRepository
) : ViewModel() {
    private val _avatars = mutableStateOf<List<ChatAvatar>>(FallbackChatAvatarCatalog.avatars)
    val avatars: State<List<ChatAvatar>> = _avatars

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

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
                        verifyAvatarCatalog(body.avatars)
                    }
                }
            } catch (_: Exception) {
                // Keep fallback on network failure.
            } finally {
                _isLoading.value = false
            }
        }
    }
}
