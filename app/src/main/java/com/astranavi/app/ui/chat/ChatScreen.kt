package com.astranavi.app.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.data.model.ChatMessage
import com.astranavi.app.data.model.ChatSummary
import com.astranavi.app.ui.components.bringIntoViewOnFocus
import kotlinx.coroutines.delay

@Composable
fun ChatScreen(viewModel: ChatViewModel, onBack: () -> Unit = {}, onOpenDrawer: () -> Unit) {
    val uiState = viewModel.uiState.value
    val chatHistory = viewModel.chatHistory.value
    val isSending = viewModel.isSending.value
    val historyError = viewModel.historyError.value
    val canLoadMore = viewModel.canLoadMoreHistory.value
    val userRatings = viewModel.userRatings.value
    val showHistory = viewModel.showHistory.value

    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // History error banner
            if (historyError != null && showHistory) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(historyError, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        TextButton(onClick = {
                            viewModel.clearHistoryError()
                            viewModel.loadHistory()
                        }) {
                            Text("Retry", fontSize = 12.sp)
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    showHistory -> ChatHistoryList(
                        history = chatHistory,
                        canLoadMore = canLoadMore,
                        onSelect = { chat -> viewModel.selectChat(chat.id) },
                        onDelete = { chatId -> showDeleteDialog = chatId },
                        onLoadMore = { viewModel.loadMoreHistory() }
                    )
                    uiState is ChatUiState.LoadingHistory || uiState is ChatUiState.LoadingMessages -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    uiState is ChatUiState.ActiveChat -> {
                        if (uiState.messages.isEmpty()) {
                            ChatEmptyState(
                                suggestedQuestions = listOf(
                                    "How will my career be in 2024?",
                                    "Tell me about my relationship compatibility.",
                                    "What are my health prospects?",
                                    "Which gemstones should I wear?"
                                ),
                                onQuestionClick = { viewModel.sendMessage(it) }
                            )
                        } else {
                            ChatConversationList(
                                messages = uiState.messages,
                                chatId = uiState.chatId,
                                isSending = isSending,
                                userRatings = userRatings,
                                onRate = { msgId, rating -> viewModel.rateMessage(uiState.chatId, msgId, rating) }
                            )
                        }
                    }
                    uiState is ChatUiState.Error -> {
                        ChatErrorState(
                            message = uiState.message,
                            onRetry = { uiState.retryAction?.invoke() }
                        )
                    }
                    else -> {
                        // Waiting for initialization
                    }
                }
            }

            if (!showHistory) {
                ChatInputBar(
                    isSending = isSending,
                    onSendMessage = { viewModel.sendMessage(it) }
                )
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { chatId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Chat") },
            text = { Text("Are you sure you want to delete this conversation? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteChat(chatId)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ChatErrorState(message: String, onRetry: (() -> Unit)?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry, shape = RoundedCornerShape(20.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

@Composable
fun ChatEmptyState(suggestedQuestions: List<String>, onQuestionClick: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Namaste ✦", fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "I am Navi, your cosmic guide. Ask me about your career, relationships, or health based on your Vedic chart.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))

        Text("HOW CAN I HELP YOU TODAY?", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items(suggestedQuestions) { question ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onQuestionClick(question) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(question, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatHistoryList(
    history: List<ChatSummary>,
    canLoadMore: Boolean,
    onSelect: (ChatSummary) -> Unit,
    onDelete: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No past conversations found.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(history, key = { it.id }) { chat ->
                ChatHistoryItem(
                    chat = chat,
                    onSelect = { onSelect(chat) },
                    onDelete = { onDelete(chat.id) }
                )
            }
            if (canLoadMore) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = onLoadMore) {
                            Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Load more chats")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatHistoryItem(chat: ChatSummary, onSelect: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ChatBubble, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(chat.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        chat.updatedAt?.take(10) ?: chat.createdAt?.take(10) ?: "Past conversation",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    if (chat.averageRating != null && chat.averageRating > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFFFC107))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            String.format("%.1f", chat.averageRating),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFC107),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
fun ChatConversationList(
    messages: List<ChatMessage>,
    chatId: String,
    isSending: Boolean,
    userRatings: Map<String, Int>,
    onRate: (msgId: String, rating: Int) -> Unit
) {
    val listState = rememberLazyListState()

    val reversedMessages = remember(messages) { messages.reversed() }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        reverseLayout = true
    ) {
        if (isSending) {
            item { ThinkingIndicator() }
        }
        items(reversedMessages) { message ->
            ChatBubble(
                message = message,
                chatId = chatId,
                userRating = userRatings[message.id],
                onRate = onRate
            )
        }
    }
}

@Composable
fun ChatInputBar(isSending: Boolean, onSendMessage: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }
    val suggestions = listOf(
        "How will my day go today?",
        "Ask about my career",
        "Ask about my education"
    )

    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).imePadding(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                contentPadding = PaddingValues(end = 24.dp)
            ) {
                items(suggestions) { suggestion ->
                    SuggestionChip(
                        onClick = { onSendMessage(suggestion) },
                        label = { Text(suggestion, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                            labelColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f).bringIntoViewOnFocus(),
                    placeholder = { Text("Ask questions...") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = {
                        onSendMessage(inputText)
                        inputText = ""
                    },
                    enabled = inputText.isNotBlank() && !isSending,
                    modifier = Modifier.background(
                        if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        CircleShape
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun ThinkingIndicator() {
    var dots by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            dots = when (dots) {
                "" -> "."
                "." -> ".."
                ".." -> "..."
                else -> ""
            }
            delay(500)
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
        Box(modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text("Navi is reflecting$dots", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    chatId: String,
    userRating: Int?,
    onRate: (msgId: String, rating: Int) -> Unit
) {
    val isUser = message.role == "user" || message.type == "user"
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            // AI label
            if (!isUser) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NAVI · AI ASTROLOGER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }

            // Message card
            Card(
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(message.content))
                    }
                ),
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = if (isUser) 24.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 24.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                ),
                border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                if (isUser) {
                    Text(
                        text = message.content,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                } else {
                    Text(
                        text = renderMarkdown(message.content),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            // Timestamp
            if (message.timestamp != null) {
                Text(
                    formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Rating buttons for AI messages
            if (!isUser && chatId != "temp_initial") {
                Row(
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbs up
                    IconButton(
                        onClick = {
                            val newRating = if (userRating == 1) 0 else 1
                            onRate(message.id, newRating)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            if (userRating == 1) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                            contentDescription = "Helpful",
                            modifier = Modifier.size(16.dp),
                            tint = if (userRating == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    // Thumbs down
                    IconButton(
                        onClick = {
                            val newRating = if (userRating == -1) 0 else -1
                            onRate(message.id, newRating)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            if (userRating == -1) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                            contentDescription = "Not helpful",
                            modifier = Modifier.size(16.dp),
                            tint = if (userRating == -1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    // Copy
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(message.content)) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Basic markdown → AnnotatedString renderer.
 * Handles: **bold**, *italic*, • bullet points, numbered lists, headers (##)
 */
private fun renderMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        for ((index, line) in lines.withIndex()) {
            if (index > 0) append("\n")

            val trimmed = line.trimStart()

            // Headers: ## or ###
            if (trimmed.startsWith("### ")) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)) {
                    append(trimmed.removePrefix("### ").trim())
                }
                continue
            }
            if (trimmed.startsWith("## ")) {
                withStyle(SpanStyle(fontWeight = FontWeight.Black, fontSize = 16.sp)) {
                    append(trimmed.removePrefix("## ").trim())
                }
                continue
            }
            if (trimmed.startsWith("# ")) {
                withStyle(SpanStyle(fontWeight = FontWeight.Black, fontSize = 18.sp)) {
                    append(trimmed.removePrefix("# ").trim())
                }
                continue
            }

            // Bullet points: - or • or *
            if (trimmed.startsWith("- ") || trimmed.startsWith("• ") || trimmed.matches(Regex("^[*]\\s.+"))) {
                append("• ")
                val content = trimmed.removePrefix("- ").removePrefix("• ").removePrefix("* ")
                appendMarkdownInline(content)
                continue
            }

            // Numbered lists: 1. 2. etc
            val numberedMatch = Regex("^(\\d+\\.\\s)(.+)").find(trimmed)
            if (numberedMatch != null) {
                append(numberedMatch.groupValues[1])
                appendMarkdownInline(numberedMatch.groupValues[2])
                continue
            }

            // Regular line
            appendMarkdownInline(trimmed)
        }
    }
}

private fun AnnotatedString.Builder.appendMarkdownInline(text: String) {
    var remaining = text
    // Process **bold** and *italic*
    val boldRegex = Regex("\\*\\*(.+?)\\*\\*")
    val italicRegex = Regex("\\*(.+?)\\*")

    while (remaining.isNotEmpty()) {
        val boldMatch = boldRegex.find(remaining)
        val italicMatch = italicRegex.find(remaining)

        val nextMatch = when {
            boldMatch != null && italicMatch != null -> if (boldMatch.range.first <= italicMatch.range.first) boldMatch else italicMatch
            boldMatch != null -> boldMatch
            italicMatch != null -> italicMatch
            else -> null
        }

        if (nextMatch == null) {
            append(remaining)
            break
        }

        // Append text before match
        if (nextMatch.range.first > 0) {
            append(remaining.substring(0, nextMatch.range.first))
        }

        if (nextMatch == boldMatch && nextMatch.groupValues.size > 1) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(nextMatch.groupValues[1])
            }
        } else if (nextMatch.groupValues.size > 1) {
            withStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                append(nextMatch.groupValues[1])
            }
        }

        remaining = remaining.substring(nextMatch.range.last + 1)
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        // Handle ISO format: 2024-01-15T10:30:00...
        val cleaned = timestamp.substringBefore(".").replace("T", " ")
        val datePart = cleaned.substringBefore(" ")
        val timePart = cleaned.substringAfter(" ").take(5) // HH:MM
        "$datePart $timePart"
    } catch (e: Exception) {
        timestamp.take(16)
    }
}
