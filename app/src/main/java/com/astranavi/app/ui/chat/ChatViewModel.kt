package com.astranavi.app.ui.chat

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.model.ChatAvatar
import com.astranavi.app.data.model.ChatMessage
import com.astranavi.app.data.model.ChatSummary
import com.astranavi.app.data.model.PaywallCardData
import com.astranavi.app.data.repository.AstrologyRepository
import com.astranavi.app.data.repository.EntitlementRepository
import com.astranavi.app.util.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import org.json.JSONObject

sealed class ChatUiState {
    object Idle : ChatUiState()
    object LoadingHistory : ChatUiState()
    object LoadingMessages : ChatUiState()
    data class ActiveChat(val chatId: String, val messages: List<ChatMessage>) : ChatUiState()
    data class PaywallBlocked(val paywall: PaywallCardData) : ChatUiState()
    data class Error(val message: String, val retryAction: (() -> Unit)? = null) : ChatUiState()
}

class ChatViewModel(
    private val repository: AstrologyRepository,
    private val sessionManager: SessionManager,
    private val entitlementRepository: EntitlementRepository? = null
) : ViewModel() {

    private val TEMP_INITIAL_CHAT_ID = "temp_initial"

    private val _uiState = mutableStateOf<ChatUiState>(ChatUiState.Idle)
    val uiState: State<ChatUiState> = _uiState

    private val _chatHistory = mutableStateOf<List<ChatSummary>>(emptyList())
    val chatHistory: State<List<ChatSummary>> = _chatHistory

    private val _isSending = mutableStateOf(false)
    val isSending: State<Boolean> = _isSending

    private val _historyError = mutableStateOf<String?>(null)
    val historyError: State<String?> = _historyError

    private var nextCursor: String? = null
    private var isLoadingMoreHistory = false

    private val _canLoadMoreHistory = mutableStateOf(false)
    val canLoadMoreHistory: State<Boolean> = _canLoadMoreHistory

    private val _userRatings = mutableStateOf<Map<String, Int>>(emptyMap())
    val userRatings: State<Map<String, Int>> = _userRatings

    private val _showHistory = mutableStateOf(false)
    val showHistory: State<Boolean> = _showHistory

    private val _activeAvatar = mutableStateOf<ChatAvatar?>(null)
    val activeAvatar: State<ChatAvatar?> = _activeAvatar

    private val _suggestedQuestions = mutableStateOf<List<String>>(emptyList())
    val suggestedQuestions: State<List<String>> = _suggestedQuestions

    private val _userName = mutableStateOf<String>("")
    val userName: State<String> = _userName

    fun setActiveAvatar(avatar: ChatAvatar?) {
        val previousId = _activeAvatar.value?.avatarId
        _activeAvatar.value = avatar
        if (previousId != avatar?.avatarId) {
            initializeWelcomeChat()
        }
    }

    fun switchActiveAvatar(avatar: ChatAvatar) {
        _activeAvatar.value = avatar
    }

    init {
        initializeWelcomeChat()
        loadHistory()
        loadUserName()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            _userName.value = sessionManager.userName.first()?.trim().orEmpty()
        }
    }

    private fun initializeWelcomeChat() {
        // Empty active chat — the chat screen renders a hero empty state instead
        // of auto-injecting assistant bubbles, so the page doesn't feel sparse.
        _suggestedQuestions.value = emptyList()
        _uiState.value = ChatUiState.ActiveChat(TEMP_INITIAL_CHAT_ID, emptyList())
    }

    fun loadHistory() {
        viewModelScope.launch {
            _historyError.value = null
            if (_uiState.value !is ChatUiState.ActiveChat) {
                _uiState.value = ChatUiState.LoadingHistory
            }
            try {
                val response = repository.listChats()
                if (response.isSuccessful) {
                    val body = response.body()
                    _chatHistory.value = body?.chats ?: emptyList()
                    nextCursor = body?.nextCursor
                    _canLoadMoreHistory.value = !nextCursor.isNullOrEmpty()
                } else {
                    _historyError.value = "Failed to load history"
                }
            } catch (e: Exception) {
                _historyError.value = "Network error loading history"
            } finally {
                if (_uiState.value is ChatUiState.LoadingHistory) {
                    _uiState.value = ChatUiState.Idle
                }
            }
        }
    }

    fun loadMoreHistory() {
        if (isLoadingMoreHistory || nextCursor.isNullOrEmpty()) return
        viewModelScope.launch {
            isLoadingMoreHistory = true
            try {
                val response = repository.listChats(cursor = nextCursor)
                if (response.isSuccessful) {
                    val body = response.body()
                    val moreChats = body?.chats ?: emptyList()
                    _chatHistory.value = _chatHistory.value + moreChats
                    nextCursor = body?.nextCursor
                    _canLoadMoreHistory.value = !nextCursor.isNullOrEmpty()
                }
            } catch (e: Exception) {
            } finally {
                isLoadingMoreHistory = false
            }
        }
    }

    fun selectChat(chatId: String) {
        _showHistory.value = false
        _suggestedQuestions.value = emptyList()
        viewModelScope.launch {
            _uiState.value = ChatUiState.LoadingMessages
            try {
                val response = repository.getChatHistory(chatId)
                if (response.isSuccessful && response.body()?.chat != null) {
                    val chat = response.body()!!.chat!!
                    val lastAvatarId = chat.messages.lastOrNull { !it.avatarId.isNullOrEmpty() }?.avatarId
                    if (lastAvatarId != null) {
                        val foundAvatar = FallbackChatAvatarCatalog.avatars.find { it.avatarId == lastAvatarId }
                        if (foundAvatar != null) {
                            _activeAvatar.value = foundAvatar
                        }
                    }
                    _uiState.value = ChatUiState.ActiveChat(chat.id, chat.messages)
                } else {
                    _uiState.value = ChatUiState.Error("Failed to load chat") { selectChat(chatId) }
                }
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error("Network error loading chat") { selectChat(chatId) }
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            try {
                val response = repository.deleteChat(chatId)
                if (response.isSuccessful) {
                    _chatHistory.value = _chatHistory.value.filter { it.id != chatId }
                    val current = _uiState.value
                    if (current is ChatUiState.ActiveChat && current.chatId == chatId) {
                        initializeWelcomeChat()
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    fun startNewChat() {
        _showHistory.value = false
        initializeWelcomeChat()
    }

    private fun handleStartChatError(initialMessage: String?, tempChatId: String, errorMessage: String, paywall: PaywallCardData? = null) {
        if (paywall != null) {
            _isSending.value = false
            _uiState.value = ChatUiState.PaywallBlocked(paywall)
            return
        }
        if (initialMessage != null) {
            _isSending.value = false
            val errorMsg = ChatMessage(UUID.randomUUID().toString(), "assistant", errorMessage)
            val currentMessages = (_uiState.value as? ChatUiState.ActiveChat)?.messages ?: emptyList()
            _uiState.value = ChatUiState.ActiveChat(tempChatId, currentMessages + errorMsg)
        }
    }

    fun sendMessage(text: String) {
        val currentState = _uiState.value
        if (text.isBlank()) return

        if (currentState is ChatUiState.ActiveChat) {
            _suggestedQuestions.value = emptyList()
            val chatId = currentState.chatId
            val userMsg = ChatMessage(UUID.randomUUID().toString(), "user", text)
            val messagesWithUser = currentState.messages + userMsg
            _uiState.value = currentState.copy(messages = messagesWithUser)
            _isSending.value = true

            if (chatId == TEMP_INITIAL_CHAT_ID) {
                viewModelScope.launch {
                    try {
                        val title = text.take(30)
                        val response = repository.createChat(title)
                        if (response.isSuccessful && response.body()?.chat != null) {
                            val newChat = response.body()!!.chat!!
                            val initialMessages = messagesWithUser
                            _uiState.value = ChatUiState.ActiveChat(newChat.id, initialMessages)
                            streamAiResponse(newChat.id, text, initialMessages)
                            loadHistory()
                        } else if (response.code() == 402) {
                            val errorBody = response.errorBody()?.string()
                            val paywallData = entitlementRepository?.parsePaywallFrom402Body(errorBody)
                            handleStartChatError(text, TEMP_INITIAL_CHAT_ID, "Insufficient credits", paywallData)
                        } else {
                            handleStartChatError(text, TEMP_INITIAL_CHAT_ID, "Failed to start chat. Please try again.")
                        }
                    } catch (e: Exception) {
                        handleStartChatError(text, TEMP_INITIAL_CHAT_ID, "Network error. Please try again.")
                    }
                }
            } else {
                streamAiResponse(chatId, text, messagesWithUser)
            }
        } else {
            startNewChat()
        }
    }

    private fun streamAiResponse(chatId: String, text: String, messagesWithUser: List<ChatMessage>) {
        viewModelScope.launch {
            var fullResponse = ""
            val aiMsgId = UUID.randomUUID().toString()

            try {
                val responseBody = repository.sendMessage(chatId, text, avatarId = _activeAvatar.value?.avatarId)
                responseBody.use { body ->
                    val contentType = body.contentType()
                    if (contentType != null && contentType.subtype == "json") {
                        val errorBody = body.string()
                        val paywallData = entitlementRepository?.parsePaywallFrom402Body(errorBody)
                        if (paywallData != null) {
                            _uiState.value = ChatUiState.PaywallBlocked(paywallData)
                            _isSending.value = false
                            return@launch
                        } else {
                            val errorMsg = ChatMessage(UUID.randomUUID().toString(), "assistant", "I'm having trouble connecting. Please try again.")
                            _uiState.value = ChatUiState.ActiveChat(chatId, messagesWithUser + errorMsg)
                            _isSending.value = false
                            return@launch
                        }
                    }

                    body.byteStream().bufferedReader().use { reader ->
                        reader.forEachLine { line ->
                            if (line.startsWith("data: ")) {
                                val dataStr = line.substring(6).trim()
                                if (dataStr == "[DONE]") return@forEachLine

                                try {
                                    val json = JSONObject(dataStr)
                                    val token = json.optString("token")
                                    if (token.isNotEmpty()) {
                                        fullResponse += token
                                        val aiMsg = ChatMessage(aiMsgId, "assistant", fullResponse)
                                        _uiState.value = ChatUiState.ActiveChat(chatId, messagesWithUser + aiMsg)
                                    }
                                    val suggestedArray = json.optJSONArray("suggestedQuestions")
                                    if (suggestedArray != null) {
                                        val list = mutableListOf<String>()
                                        for (i in 0 until suggestedArray.length()) {
                                            list.add(suggestedArray.getString(i))
                                        }
                                        _suggestedQuestions.value = list
                                    }
                                } catch (_: Exception) {
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessage(UUID.randomUUID().toString(), "assistant", "I'm having trouble connecting to the stars right now. Please try again.")
                _uiState.value = ChatUiState.ActiveChat(chatId, messagesWithUser + errorMsg)
            } finally {
                _isSending.value = false
            }
        }
    }

    fun rateMessage(chatId: String, msgId: String, rating: Int) {
        _userRatings.value = _userRatings.value + (msgId to rating)

        viewModelScope.launch {
            try {
                repository.rateMessage(chatId, msgId, rating)
            } catch (e: Exception) {
                _userRatings.value = _userRatings.value - msgId
            }
        }
    }

    fun clearHistoryError() {
        _historyError.value = null
    }

    fun toggleHistory() {
        _showHistory.value = !_showHistory.value
    }

    fun setShowHistory(show: Boolean) {
        _showHistory.value = show
    }

    fun resetToIdle() {
        initializeWelcomeChat()
    }
}
