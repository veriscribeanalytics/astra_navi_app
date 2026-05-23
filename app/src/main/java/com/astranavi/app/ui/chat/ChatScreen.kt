package com.astranavi.app.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import com.astranavi.app.ui.components.PreviewMultiDevice
import com.astranavi.app.ui.theme.AstraNaviTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.astranavi.app.R
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.data.model.ChatAvatar
import com.astranavi.app.data.model.ChatMessage
import com.astranavi.app.data.model.ChatSummary
import com.astranavi.app.ui.components.ApplyRootGlow
import com.astranavi.app.ui.components.GlowColors
import com.astranavi.app.ui.components.bringIntoViewOnFocus
import com.astranavi.app.ui.components.responsiveMetrics
import com.astranavi.app.util.setSensitiveText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    seedPrompt: String? = null,
    seedContext: String? = null,
    onBack: () -> Unit = {},
    onOpenDrawer: () -> Unit,
    guideListPane: (@Composable (modifier: Modifier) -> Unit)? = null
) {
    com.astranavi.app.util.SecureScreen()
    val uiState = viewModel.uiState.value
    val chatHistory = viewModel.chatHistory.value
    val isSending = viewModel.isSending.value
    val historyError = viewModel.historyError.value
    val canLoadMore = viewModel.canLoadMoreHistory.value
    val userRatings = viewModel.userRatings.value
    val showHistory = viewModel.showHistory.value
    val activeAvatar = viewModel.activeAvatar.value
    val userName = viewModel.userName.value
    val suggestedQuestions = viewModel.suggestedQuestions.value
    val metrics = responsiveMetrics()

    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    val palette = chatAvatarPalette(activeAvatar?.avatarId)
    ApplyRootGlow(GlowColors(accent = palette.accent, deep = palette.deep, radial = palette.radial))

    // Wide-screen split: guide list left, conversation right.
    // Triggered at >= 600dp via responsive.isMediumWidth (foldables unfolded + tablets).
    val showSplit = guideListPane != null && metrics.isMediumWidth && !showHistory

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // History error banner (spans full width either way)
            if (historyError != null && showHistory) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = metrics.pagePadding, vertical = metrics.chatMessagePadding / 2),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(metrics.kundliSmallIconSize), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(metrics.chatMessagePadding / 2))
                        Text(historyError, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        TextButton(onClick = {
                            viewModel.clearHistoryError()
                            viewModel.loadHistory()
                        }) {
                            Text(stringResource(R.string.chat_btn_retry), fontSize = 12.sp)
                        }
                    }
                }
            }

            if (showSplit) {
                Row(modifier = Modifier.fillMaxSize()) {
                    guideListPane!!(Modifier.weight(0.38f).fillMaxHeight())
                    VerticalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.fillMaxHeight()
                    )
                    Column(modifier = Modifier.weight(0.62f).fillMaxHeight()) {
                        Box(modifier = Modifier.weight(1f)) {
                            ChatBodySelector(
                                viewModel = viewModel,
                                uiState = uiState,
                                isSending = isSending,
                                userRatings = userRatings,
                                activeAvatar = activeAvatar,
                                userName = userName,
                                palette = palette,
                                seedPrompt = seedPrompt,
                                seedContext = seedContext
                            )
                        }
                        ChatInputBar(
                            isSending = isSending,
                            suggestedQuestions = suggestedQuestions,
                            onSendMessage = { viewModel.sendMessage(it) }
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        showHistory -> ChatHistoryList(
                            history = chatHistory,
                            canLoadMore = canLoadMore,
                            onSelect = { chat -> viewModel.selectChat(chat.id) },
                            onDelete = { chatId -> showDeleteDialog = chatId },
                            onLoadMore = { viewModel.loadMoreHistory() }
                        )
                        else -> ChatBodySelector(
                            viewModel = viewModel,
                            uiState = uiState,
                            isSending = isSending,
                            userRatings = userRatings,
                            activeAvatar = activeAvatar,
                            userName = userName,
                            palette = palette,
                            seedPrompt = seedPrompt,
                            seedContext = seedContext
                        )
                    }
                }

                if (!showHistory) {
                    ChatInputBar(
                        isSending = isSending,
                        suggestedQuestions = suggestedQuestions,
                        onSendMessage = { viewModel.sendMessage(it) }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { chatId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.chat_delete_title)) },
            text = { Text(stringResource(R.string.chat_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteChat(chatId)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.chat_btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.chat_btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun ChatBodySelector(
    viewModel: ChatViewModel,
    uiState: ChatUiState,
    isSending: Boolean,
    userRatings: Map<String, Int>,
    activeAvatar: ChatAvatar?,
    userName: String,
    palette: ChatAvatarPalette,
    seedPrompt: String? = null,
    seedContext: String? = null
) {
    when {
        uiState is ChatUiState.LoadingHistory || uiState is ChatUiState.LoadingMessages -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
            }
        }
        uiState is ChatUiState.ActiveChat -> {
            if (uiState.messages.isEmpty()) {
                ChatEmptyState(
                    avatar = activeAvatar,
                    userName = userName,
                    suggestedQuestions = listOf(
                        stringResource(R.string.chat_suggested_question_1),
                        stringResource(R.string.chat_suggested_question_2),
                        stringResource(R.string.chat_suggested_question_3),
                        stringResource(R.string.chat_suggested_question_4)
                    ),
                    onQuestionClick = { viewModel.sendMessage(it) },
                    seedPrompt = seedPrompt,
                    seedContext = seedContext
                )
            } else {
                ChatConversationList(
                    messages = uiState.messages,
                    chatId = uiState.chatId,
                    isSending = isSending,
                    userRatings = userRatings,
                    userBubbleColor = palette.bubble,
                    onUserBubbleColor = palette.onBubble,
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

@Composable
fun ChatErrorState(message: String, onRetry: (() -> Unit)?) {
    val metrics = responsiveMetrics()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(metrics.pagePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(metrics.orbitCoreSize * 0.4f),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(metrics.chatMessagePadding))
        Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(metrics.chatMessagePadding))
            Button(onClick = onRetry, shape = RoundedCornerShape(20.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(metrics.kundliSmallIconSize))
                Spacer(modifier = Modifier.width(metrics.chatMessagePadding / 2))
                Text(stringResource(R.string.chat_btn_retry))
            }
        }
    }
}

@Composable
fun ChatEmptyState(
    avatar: ChatAvatar?,
    userName: String,
    suggestedQuestions: List<String>,
    onQuestionClick: (String) -> Unit,
    seedPrompt: String? = null,
    seedContext: String? = null
) {
    val metrics = responsiveMetrics()
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val displayName = avatar?.name ?: "Navi"
    val displayTitle = avatar?.title ?: stringResource(R.string.chat_default_guide_title)
    val heroSize = if (metrics.isVeryCompactWidth || metrics.isLargeFont) 104.dp
                   else if (metrics.isCompactWidth) 132.dp else 156.dp

    val greeting = if (userName.isNotBlank()) {
        stringResource(R.string.chat_greeting_with_name, userName)
    } else {
        stringResource(R.string.chat_greeting_default)
    }
    val intro = stringResource(R.string.chat_intro_format, displayName, displayTitle)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = metrics.pagePadding, vertical = metrics.chatMessagePadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(metrics.chatMessagePadding))

        // Hero avatar with soft glow
        Box(
            modifier = Modifier.size(heroSize + 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                primary.copy(alpha = 0.30f),
                                primary.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            ChatAvatarImage(
                avatar = avatar,
                modifier = Modifier
                    .size(heroSize)
                    .clip(CircleShape)
                    .border(2.dp, primary.copy(alpha = 0.45f), CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(metrics.chatMessagePadding * 1.25f))

        Text(
            text = greeting,
            fontSize = if (metrics.isVeryCompactWidth || metrics.isLargeFont) 22.sp
                      else if (metrics.isCompactWidth) 24.sp else 26.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = intro,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = if (metrics.isCompactWidth) 0.dp else metrics.pagePadding)
        )

        Spacer(modifier = Modifier.height(metrics.chatMessagePadding * 1.5f))

        // Trust chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            TrustChip(
                icon = Icons.Default.AutoAwesome,
                tint = primary,
                title = stringResource(R.string.chat_trust_vedic_title),
                subtitle = stringResource(R.string.chat_trust_vedic_subtitle)
            )
            TrustChip(
                icon = Icons.Default.Person,
                tint = primary,
                title = stringResource(R.string.chat_trust_personalized_title),
                subtitle = stringResource(R.string.chat_trust_personalized_subtitle)
            )
            TrustChip(
                icon = Icons.Default.Shield,
                tint = primary,
                title = stringResource(R.string.chat_trust_private_title),
                subtitle = stringResource(R.string.chat_trust_private_subtitle)
            )
        }

        Spacer(modifier = Modifier.height(metrics.chatMessagePadding * 1.5f))

        if (!seedPrompt.isNullOrBlank()) {
            val contextTitle = if (!seedContext.isNullOrBlank()) seedContext else "From your daily energy reading"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = metrics.chatMessagePadding),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = primary.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(primary.copy(alpha = 0.4f), secondary.copy(alpha = 0.4f))))
            ) {
                Column(
                    modifier = Modifier.padding(metrics.cardPadding),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = contextTitle,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = seedPrompt,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { onQuestionClick(seedPrompt) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Ask $displayName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Popular Questions card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = secondary.copy(alpha = 0.06f)
            ),
            border = BorderStroke(1.dp, secondary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(metrics.cardPadding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = secondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.chat_popular_questions_title),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(metrics.chatMessagePadding * 0.75f))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestedQuestions.forEach { question ->
                        PopularQuestionRow(question = question, onClick = { onQuestionClick(question) })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(metrics.chatMessagePadding))
    }
}

@Composable
private fun TrustChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    title: String,
    subtitle: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 88.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = tint.copy(alpha = 0.10f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = tint
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            subtitle,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PopularQuestionRow(question: String, onClick: () -> Unit) {
    val metrics = responsiveMetrics()
    val hPad = if (metrics.isVeryCompactWidth) 12.dp else 14.dp
    val vPad = if (metrics.isVeryCompactWidth) 10.dp else 12.dp
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = hPad, vertical = vPad),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                question,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
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
    val metrics = responsiveMetrics()
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(metrics.orbitCoreSize * 0.4f), tint = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(metrics.chatMessagePadding))
                Text(stringResource(R.string.chat_no_past_conversations), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(metrics.pagePadding), verticalArrangement = Arrangement.spacedBy(metrics.chatMessagePadding / 2)) {
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
                        modifier = Modifier.fillMaxWidth().padding(metrics.pagePadding),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = onLoadMore) {
                            Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(metrics.kundliSmallIconSize))
                            Spacer(modifier = Modifier.width(metrics.chatMessagePadding / 4))
                            Text(stringResource(R.string.chat_load_more_button))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatHistoryItem(chat: ChatSummary, onSelect: () -> Unit, onDelete: () -> Unit) {
    val metrics = responsiveMetrics()
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = metrics.cardPadding, vertical = metrics.cardPadding * 0.75f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size(metrics.snapshotImageSize), shape = CircleShape, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ChatBubble, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(metrics.bottomNavIconSize))
                }
            }
            Spacer(modifier = Modifier.width(metrics.chatMessagePadding))
            Column(modifier = Modifier.weight(1f)) {
                Text(chat.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        chat.updatedAt?.take(10) ?: chat.createdAt?.take(10) ?: stringResource(R.string.chat_history_default_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    if (chat.averageRating != null && chat.averageRating > 0) {
                        Spacer(modifier = Modifier.width(metrics.chatMessagePadding / 2))
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(metrics.kundliSmallIconSize * 0.75f), tint = Color(0xFFFFC107))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            String.format(Locale.US, "%.1f", chat.averageRating),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFC107),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(metrics.snapshotImageSize * 0.8f)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.chat_btn_delete), modifier = Modifier.size(metrics.kundliSmallIconSize), tint = MaterialTheme.colorScheme.outlineVariant)
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
    userBubbleColor: Color,
    onUserBubbleColor: Color,
    onRate: (msgId: String, rating: Int) -> Unit
) {
    val listState = rememberLazyListState()
    val metrics = responsiveMetrics()

    val reversedMessages = remember(messages) { messages.reversed() }

    LaunchedEffect(messages.size, isSending) {
        if (messages.isNotEmpty()) {
            val lastMessage = messages.last()
            val isUserMessage = lastMessage.role == "user" || lastMessage.type == "user"
            val isAtBottom = listState.firstVisibleItemIndex <= 1
            if (isAtBottom || isUserMessage) {
                listState.animateScrollToItem(0)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(metrics.chatMessagePadding),
        reverseLayout = true
    ) {
        if (isSending) {
            item { ThinkingIndicator() }
        }
        items(reversedMessages, key = { it.id }) { message ->
            ChatBubble(
                message = message,
                chatId = chatId,
                userRating = userRatings[message.id],
                userBubbleColor = userBubbleColor,
                onUserBubbleColor = onUserBubbleColor,
                onRate = onRate
            )
        }
    }
}

@Composable
fun ChatInputBar(
    isSending: Boolean,
    suggestedQuestions: List<String>,
    onSendMessage: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val metrics = responsiveMetrics()
    val verticalPad = if (metrics.isVeryCompactWidth) 6.dp
                      else if (metrics.isCompactWidth) 8.dp
                      else metrics.chatMessagePadding
    val sendButtonSize = if (metrics.isCompactWidth) 40.dp else 44.dp
    val fieldMaxLines = if (metrics.isVeryCompactWidth) 3 else 4

    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).imePadding().navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(horizontal = metrics.pagePadding, vertical = verticalPad)) {
            if (suggestedQuestions.isNotEmpty() && !isSending) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(suggestedQuestions) { question ->
                        Surface(
                            modifier = Modifier
                                .clickable { onSendMessage(question) },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = question,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
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
                    placeholder = { Text(stringResource(R.string.chat_input_placeholder)) },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = fieldMaxLines
                )
                Spacer(modifier = Modifier.width(metrics.chatMessagePadding))
                IconButton(
                    onClick = {
                        onSendMessage(inputText)
                        inputText = ""
                    },
                    enabled = inputText.isNotBlank() && !isSending,
                    modifier = Modifier
                        .size(sendButtonSize)
                        .background(
                            if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            CircleShape
                        )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.chat_desc_send), tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun ThinkingIndicator() {
    var dots by remember { mutableStateOf("") }
    val metrics = responsiveMetrics()
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
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(metrics.chatMessagePadding / 2)) {
        Box(modifier = Modifier.size(metrics.bottomNavIconSize * 1.2f).background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(metrics.kundliSmallIconSize * 0.75f), tint = MaterialTheme.colorScheme.secondary)
        }
        Spacer(modifier = Modifier.width(metrics.chatMessagePadding))
        Text(stringResource(R.string.chat_thinking_message_format, dots), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    chatId: String,
    userRating: Int?,
    userBubbleColor: Color,
    onUserBubbleColor: Color,
    onRate: (msgId: String, rating: Int) -> Unit
) {
    val isUser = message.role == "user" || message.type == "user"
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val metrics = responsiveMetrics()
    fun copyMessage() {
        scope.launch {
            val text = message.content.ifEmpty { message.text ?: "" }
            clipboard.setSensitiveText("message", text)
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            // AI label
            if (!isUser) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(metrics.kundliSmallIconSize * 0.6f), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(metrics.chatMessagePadding / 3))
                    Text(stringResource(R.string.chat_ai_astrologer_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }

            // Message card
            Card(
                modifier = Modifier.widthIn(max = metrics.chatBubbleMaxWidth).combinedClickable(
                    onClick = {},
                    onLongClick = { copyMessage() }
                ),
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = if (isUser) 24.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 24.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) userBubbleColor else MaterialTheme.colorScheme.surface
                ),
                border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                if (isUser) {
                    Text(
                        text = message.content.ifEmpty { message.text ?: "" },
                        modifier = Modifier.padding(horizontal = metrics.cardPadding, vertical = metrics.chatMessagePadding),
                        color = onUserBubbleColor,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                } else {
                    val parsedContent = remember(message.content, message.text) {
                        renderMarkdown(message.content.ifEmpty { message.text ?: "" })
                    }
                    Text(
                        text = parsedContent,
                        modifier = Modifier.padding(horizontal = metrics.cardPadding, vertical = metrics.chatMessagePadding),
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
                        modifier = Modifier.size(metrics.bottomNavIconSize * 1.4f)
                    ) {
                        Icon(
                            if (userRating == 1) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                            contentDescription = stringResource(R.string.chat_desc_helpful),
                            modifier = Modifier.size(metrics.kundliSmallIconSize),
                            tint = if (userRating == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    // Thumbs down
                    IconButton(
                        onClick = {
                            val newRating = if (userRating == -1) 0 else -1
                            onRate(message.id, newRating)
                        },
                        modifier = Modifier.size(metrics.bottomNavIconSize * 1.4f)
                    ) {
                        Icon(
                            if (userRating == -1) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                            contentDescription = stringResource(R.string.chat_desc_not_helpful),
                            modifier = Modifier.size(metrics.kundliSmallIconSize),
                            tint = if (userRating == -1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    // Copy
                    IconButton(
                        onClick = { copyMessage() },
                        modifier = Modifier.size(metrics.bottomNavIconSize * 1.4f)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.chat_desc_copy),
                            modifier = Modifier.size(metrics.kundliSmallIconSize * 0.85f),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

private val bulletRegex = Regex("^[*]\\s.+")
private val numberedRegex = Regex("^(\\d+\\.\\s)(.+)")
private val boldRegex = Regex("\\*\\*(.+?)\\*\\*")
private val italicRegex = Regex("\\*(.+?)\\*")

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
            if (trimmed.startsWith("- ") || trimmed.startsWith("• ") || trimmed.matches(bulletRegex)) {
                append("• ")
                val content = trimmed.removePrefix("- ").removePrefix("• ").removePrefix("* ")
                appendMarkdownInline(content)
                continue
            }

            // Numbered lists: 1. 2. etc
            val numberedMatch = numberedRegex.find(trimmed)
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

@PreviewMultiDevice
@Composable
fun ChatEmptyStatePreview() {
    AstraNaviTheme {
        Surface {
            ChatEmptyState(
                avatar = FallbackChatAvatarCatalog.avatars.first(),
                userName = "Ankit Prasad",
                suggestedQuestions = listOf(
                    "How will my day go today?",
                    "Ask about my career",
                    "Is this a good time for a new job?",
                    "What should I focus on right now?"
                ),
                onQuestionClick = {}
            )
        }
    }
}
