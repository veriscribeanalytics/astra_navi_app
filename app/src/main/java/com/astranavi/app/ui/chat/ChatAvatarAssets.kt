package com.astranavi.app.ui.chat

import com.astranavi.app.R
import com.astranavi.app.data.model.ChatAvatar
import com.astranavi.app.data.model.ChatAvatarCatalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage

fun chatAvatarDrawable(avatarId: String?): Int = when (avatarId) {
    "career_mentor" -> R.drawable.avatar_arya
    "relationship_guide" -> R.drawable.avatar_meera
    "spiritual_guide" -> R.drawable.avatar_anand
    "astro_sage" -> R.drawable.avatar_rishi
    else -> R.drawable.avatar_navi
}

val FallbackChatAvatarCatalog = ChatAvatarCatalog(
    defaultAvatarId = "navi",
    avatars = listOf(
        ChatAvatar(
            avatarId = "navi",
            name = "Navi",
            title = "General Vedic Guide",
            description = "Balanced Vedic astrology guidance across career, love, timing, remedies, and life direction.",
            expertise = listOf("general astrology", "birth chart reading", "timing", "practical guidance"),
            personality = "Warm, clear, balanced, and practical.",
            creditCost = 1,
            isDefault = true,
            imageUrl = "/static/avatars/NAVI_AVATAR.webp"
        ),
        ChatAvatar(
            avatarId = "career_mentor",
            name = "Arya",
            title = "Career Mentor",
            description = "Career-focused guidance for jobs, business, skills, promotion timing, and work direction.",
            expertise = listOf("career", "job change", "business", "skills", "workplace strategy", "timing"),
            personality = "Direct, pragmatic, strategic, and action-oriented.",
            creditCost = 2,
            imageUrl = "/static/avatars/ARYA_AVATAR.webp"
        ),
        ChatAvatar(
            avatarId = "relationship_guide",
            name = "Meera",
            title = "Relationship Guide",
            description = "Sensitive guidance for love, marriage, compatibility, communication, and emotional patterns.",
            expertise = listOf("love", "marriage", "compatibility", "relationship timing", "communication"),
            personality = "Emotionally careful, compassionate, honest, and non-fatalistic.",
            creditCost = 2,
            imageUrl = "/static/avatars/MEERA_AVATAR.webp"
        ),
        ChatAvatar(
            avatarId = "spiritual_guide",
            name = "Anand",
            title = "Health Guide",
            description = "Wellness and health-focused guidance — vitality, daily routines, emotional balance, lifestyle habits, and holistic well-being through Vedic wisdom.",
            expertise = listOf("wellness", "health habits", "vitality", "emotional balance", "lifestyle", "daily routines"),
            personality = "Calm, supportive, practical, and encouraging.",
            creditCost = 2,
            imageUrl = "/static/avatars/ANAND_AVATAR.webp"
        ),
        ChatAvatar(
            avatarId = "finance_mentor",
            name = "Vidya",
            title = "Finance Mentor",
            description = "Financial guidance for earnings, savings, business, investments, financial timing, and practical money planning through Vedic astrology.",
            expertise = listOf("money", "career earnings", "savings", "business finance", "investments", "financial timing", "practical planning"),
            personality = "Practical, strategic, grounded, and cautious.",
            creditCost = 2,
            imageUrl = "/static/avatars/VIDYA_AVATAR.webp"
        ),
        ChatAvatar(
            avatarId = "astro_sage",
            name = "Rishi",
            title = "Deep Chart Sage",
            description = "Advanced chart synthesis for users who want deeper house, dasha, transit, and theme analysis.",
            expertise = listOf("deep chart analysis", "dasha", "transits", "house synthesis", "life themes"),
            personality = "Analytical, precise, traditional, and thorough.",
            creditCost = 3,
            imageUrl = "/static/avatars/RISHI_AVATAR.webp"
        )
    )
)

@Composable
fun ChatAvatarImage(
    avatar: ChatAvatar?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val fallbackRes = chatAvatarDrawable(avatar?.avatarId)
    val model = if (avatar?.imageUrl != null) {
        val url = avatar.imageUrl
        if (url.startsWith("http")) url else "https://api.veriscribeanalytics.com" + url
    } else {
        fallbackRes
    }
    AsyncImage(
        model = model,
        placeholder = painterResource(id = fallbackRes),
        error = painterResource(id = fallbackRes),
        contentDescription = avatar?.name,
        contentScale = contentScale,
        modifier = modifier
    )
}

fun verifyAvatarCatalog(avatars: List<ChatAvatar>) {
    val knownIds = setOf("navi", "career_mentor", "relationship_guide", "spiritual_guide", "finance_mentor", "astro_sage")
    for (avatar in avatars) {
        if (!knownIds.contains(avatar.avatarId)) {
            android.util.Log.w("ChatAvatarAssets", "Warning: Dynamic catalog contains unmapped avatar ID '${avatar.avatarId}' (${avatar.name}). It will fallback to avatar_navi.")
        }
    }
}

