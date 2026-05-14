Ask Navi on Career — Implementation Plan
Goal
When a user taps "Ask Navi" on a life-area orbit (Career, Love, Health, Finance), the chat opens with a clean, readable prefilled message in the input bar. The actual career/ratings/insight data is passed as hidden context to the AI — never dumped into the input box or shown in the chat bubble.

Current State (Broken)
onNavigateToChat(String?) in DashboardScreen passes a message string, but MainActivity completely ignores it — navigation always goes to bare "chat" route with no arguments.
The Chat NavHost composable has no nav arguments — no way to receive a prefilled message or context.
ChatRequest only has { text, language } — no context field for injecting career data.
ChatViewModel.sendMessage() sends only the visible text — everything the user types shows up as-is in the chat bubble with no hidden metadata.
Backend API does not accept or process a context field — it only maps text and language.
Architecture Flow
User taps "Ask Navi" on Career orbit
  → Dashboard extracts career data (score, insight, rating)
  → Navigate to Chat with:
      message = "Tell me about my career today using my career rating and today's career reading."  (clean, user-visible)
      context = { "area": "career", "score": 72, "insight": "...", "tone": "..." }  (hidden, API-only)
  → ChatScreen opens, input bar prefilled with clean message
  → User presses Send
  → ChatViewModel.sendMessage(text, context):
      - User bubble shows ONLY the clean text
      - API receives BOTH text + context
      - Backend injects context into AI system prompt invisibly
  → AI responds using career context, user never sees raw data
Phase 1: Backend API Changes (MUST GO FIRST)
1A. Add context field to chat message endpoint
File: Backend route handler for POST /api/chats/{chatId}/messages

Extend request body schema to accept:
{
  "text": "Tell me about my career today...",
  "language": "english",
  "context": {
    "area": "career",
    "score": 72,
    "insight": "Saturn transit through 10th house suggests...",
    "tone": "moderate",
    "rating_text": "Your career rating is 7/10 today"
  }
}
context should be optional and nullable — existing chats without context continue to work.
1B. Inject context into AI system prompt (not user message)
When context is present, the backend should inject it into the system/hidden prompt sent to the AI model.
Example system prompt augmentation:
The user is asking about their career. Here is their current career data:
- Career score today: 72/100
- Career insight: Saturn transit through 10th house suggests...
- Tone: moderate

Use this data to provide a personalized, grounded response. Do not repeat the raw data verbatim.
The context must NOT be stored as part of the user message content in the database. Store only the clean text.
1C. Optionally store context as message metadata
Consider storing context as a separate metadata or context field on the message record, so it can be referenced later (e.g., for follow-up messages in the same chat where context is still relevant).
Phase 2: Frontend — Fix Navigation (Can Start Independently)
2A. Add message nav argument to Chat route
File: MainActivity.kt

Change Screen.Chat route to accept optional query param:
object Chat : Screen("chat?message={message}", "AI Chat", Icons.Default.AutoAwesome)
Or better, keep the base route and add the argument separately:
composable(
    "${Screen.Chat.route}?message={message}",
    arguments = listOf(navArgument("message") {
        type = NavType.StringType
        nullable = true
        defaultValue = null
    })
) { backStackEntry ->
    val initialMessage = backStackEntry.arguments?.getString("message")
    val chatViewModel: ChatViewModel = viewModel(...)
    ChatScreen(viewModel = chatViewModel, initialMessage = initialMessage, ...)
}
2B. Actually pass the message through navigation
File: MainActivity.kt

Fix onNavigateToChat to pass the string:
onNavigateToChat = { message ->
    val route = if (message != null) "${Screen.Chat.route}?message=${Uri.encode(message)}" else Screen.Chat.route
    navController.navigate(route)
}
Important: URL-encode the message string since it may contain special characters.
2C. Add context nav argument (or use shared state)
Since context data is a JSON object and too large for URL params, use one of these approaches:

Option A: Shared state holder (Recommended)

Create a simple singleton or CompositionLocal that holds the pending chat context:
object PendingChatContext {
    var message: String? = null
    var context: String? = null  // JSON string of career/love/health data
}
Dashboard sets PendingChatContext.message and PendingChatContext.context before navigating.
ChatScreen/ChatViewModel reads from it on init and clears it after use.
Option B: Add context as nav argument

Less clean because career data JSON can be very long and URL encoding is fragile. Avoid this.
Phase 3: Frontend — Chat UX Changes
3A. Prefill the input bar with the clean message
File: ChatScreen.kt — ChatInputBar

Add initialMessage parameter:
@Composable
fun ChatInputBar(isSending: Boolean, initialMessage: String? = null, onSendMessage: (String) -> Unit) {
    var inputText by remember { mutableStateOf(initialMessage ?: "") }
    ...
}
Pass initialMessage from ChatScreen down to ChatInputBar.
3B. Store context in ChatViewModel
File: ChatViewModel.kt

Add a field to hold pending context:
private var pendingContext: String? = null

fun setPendingContext(context: String?) {
    pendingContext = context
}
On init, read from PendingChatContext:
init {
    initializeWelcomeChat()
    loadHistory()
    // Read pending context if set before navigation
    pendingContext = PendingChatContext.context
    PendingChatContext.context = null  // Clear after reading
}
3C. Send context with the first message
File: ChatViewModel.kt — sendMessage()

Attach pendingContext to the API call, then clear it after the first send:
fun sendMessage(text: String) {
    ...
    streamAiResponse(newChat.id, text, initialMessages, pendingContext)
    pendingContext = null  // Context only sent with first message
}

private fun streamAiResponse(chatId: String, text: String, messagesWithUser: List<ChatMessage>, context: String? = null) {
    ...
    val responseBody = repository.sendMessage(chatId, text, language = "english", context = context)
    ...
}
Phase 4: Frontend — API Model Changes
4A. Add context to ChatRequest
File: ApiModels.kt

data class ChatRequest(
    val text: String,
    val language: String = "english",
    val context: String? = null  // JSON string of hidden career/love/health data
)
4B. Thread context through Repository
File: AstrologyRepository.kt

suspend fun sendMessage(chatId: String, text: String, language: String = "english", context: String? = null): okhttp3.ResponseBody {
    return apiService.sendMessage(chatId, ChatRequest(text, language, context))
}
Phase 5: Frontend — Dashboard Context Injection
5A. Build context JSON from orbit data
File: DashboardScreen.kt — OrbitSystem action handler

When user taps "Ask Navi" on a career/love/health/finance orbit:

onActionClick = { action, data ->
    when (action) {
        "chat" -> {
            val area = (data as? OrbitItem)?.id  // "career", "love", "health", "finance"
            val areaInsight = horoscope.areas_text?.let { 
                when(area) {
                    "career" -> it.career
                    "love" -> it.love
                    "health" -> it.health
                    "finance" -> it.finance
                    else -> null
                }
            }
            val areaScore = horoscope.score.areas?.get(area)?.value
            
            val contextJson = JSONObject().apply {
                put("area", area ?: "general")
                areaScore?.let { put("score", it) }
                areaInsight?.let { 
                    put("insight", it.insight)
                    put("tone", it.tone)
                }
            }.toString()
            
            val message = when(area) {
                "career" -> "Tell me about my career today using my career rating and today's career reading."
                "love" -> "Tell me about my love life today based on my ratings and readings."
                "health" -> "Give me health guidance today based on my health score and reading."
                "finance" -> "Advise me on my finances today based on my financial outlook."
                else -> "Give me cosmic guidance for my day."
            }
            
            PendingChatContext.message = message
            PendingChatContext.context = contextJson
            onNavigateToChat(message)
        }
    }
}
5B. Update TipNaviRow "Ask Navi" button
The existing generic "Ask Navi" button can keep sending a general message with no context:

onClick = { onChat("Give me cosmic guidance for my day.") }
This works fine — context will be null, and the AI responds based on the user's chart data that the backend already has access to.

Phase 6: Testing Checklist
 Tap "Ask Navi" on Career orbit → chat opens with prefilled "Tell me about my career today..." in input bar
 Tap "Ask Navi" on Love orbit → prefilled love-specific message
 Tap "Ask Navi" on Health orbit → prefilled health-specific message
 Tap "Ask Navi" on Finance orbit → prefilled finance-specific message
 Tap generic "ASK NAVI" button → prefilled general message, no context
 User can edit the prefilled message before sending
 Chat bubble shows ONLY the clean message — no raw JSON, no scores, no API data
 AI response references the career/love data correctly (verify backend is using context)
 Follow-up messages in the same chat work normally (context only sent on first message)
 Opening chat via bottom bar FAB (no prefilled message) works as before
 Existing chats without context continue to work — no regression
Dependency Order
Phase 1 (Backend) → Phase 4 (ChatRequest model) → Phase 3C (ViewModel sends context) → Phase 5 (Dashboard builds context)
Phase 2 (Navigation fix) — independent, can start now
Phase 3A (Prefill input bar) — depends on Phase 2
Start with Phase 2 — it's a pure frontend fix that improves UX immediately (prefilled messages finally reach the chat screen). Then Phase 1 backend, then chain the rest.

Files Modified Summary
File	Phase	Change
Backend: chat message handler	1A, 1B, 1C	Accept context, inject into AI system prompt, store as metadata
MainActivity.kt	2A, 2B	Fix navigation to pass message + read context from shared state
ChatScreen.kt	3A	ChatInputBar accepts initialMessage param
ChatViewModel.kt	3B, 3C	pendingContext field, send context on first message
ApiModels.kt	4A	ChatRequest gains context field
AstrologyRepository.kt	4B	sendMessage() gains context param
DashboardScreen.kt	5A, 5B	Build context JSON from orbit data, set PendingChatContext
New: PendingChatContext.kt	2C	Shared state singleton for cross-navigation context passing