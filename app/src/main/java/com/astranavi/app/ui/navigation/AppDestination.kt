package com.astranavi.app.ui.navigation

/**
 * Chrome contract — DO NOT VIOLATE:
 *   1. Every signed-in destination MUST set showTopBar = true. No page renders its
 *      own status-bar-padded header.
 *   2. Pages MUST NOT call statusBarsPadding() at the page level. The global
 *      CenterAlignedTopAppBar in MainActivity owns the status-bar inset.
 *   3. Page body MUST NOT duplicate the AppDestination.title string at the top
 *      (e.g. no in-page "Vedic Wisdom" headline on Blogs — the top bar already
 *      labels the page).
 *   4. The right-side action icon in the global top bar is Icons.Default.Menu
 *      and opens the Cosmic drawer. Profile is reachable from inside that drawer.
 */

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.astranavi.app.R

enum class TransitionStyle {
    NONE,
    TAB,
    PUSH
}

sealed class AppDestination(
    val route: String,
    val baseRoute: String,
    val titleResId: Int,
    val icon: ImageVector?,
    val requiresAuth: Boolean,
    val isTopLevel: Boolean,
    val showTopBar: Boolean,
    val showBottomBar: Boolean,
    val transitionStyle: TransitionStyle
) {
    // Auth Group
    object Intro : AppDestination("intro", "intro", R.string.title_intro, null, false, false, false, false, TransitionStyle.NONE)
    object LogoSplash : AppDestination("logo_splash", "logo_splash", R.string.title_welcome, null, false, false, false, false, TransitionStyle.NONE)
    object Login : AppDestination("login", "login", R.string.title_login, null, false, false, false, false, TransitionStyle.NONE)

    // Main Group (Top Level Tabs)
    object Dashboard : AppDestination("home", "home", R.string.nav_home, Icons.Default.Home, true, true, true, true, TransitionStyle.TAB)
    object Blogs : AppDestination("blogs", "blogs", R.string.nav_blogs, Icons.AutoMirrored.Filled.List, true, true, true, true, TransitionStyle.TAB)
    object Kundli : AppDestination("kundli", "kundli", R.string.nav_kundli, Icons.Default.Star, true, true, true, true, TransitionStyle.TAB)
    object Consult : AppDestination("consult", "consult", R.string.nav_consult, Icons.Default.Info, true, true, true, true, TransitionStyle.TAB)

    // Detail / Non-tab Main Group
    object Chat : AppDestination("chat", "chat", R.string.nav_chat, Icons.Default.AutoAwesome, true, false, true, false, TransitionStyle.PUSH)
    object ChatSelect : AppDestination("chat_select?prompt={prompt}", "chat_select", R.string.title_chat_select, Icons.Default.AutoAwesome, true, false, true, false, TransitionStyle.PUSH)
    object Profile : AppDestination("profile", "profile", R.string.nav_profile, Icons.Default.Person, true, false, true, false, TransitionStyle.PUSH)

    // Details
    object Forecast : AppDestination("forecast?area={area}", "forecast", R.string.nav_forecast, Icons.AutoMirrored.Filled.TrendingUp, true, false, true, false, TransitionStyle.PUSH)
    object Match : AppDestination("match", "match", R.string.nav_match, Icons.Default.Favorite, true, false, true, false, TransitionStyle.PUSH)
    object MatchHistory : AppDestination("match_history", "match_history", R.string.title_match_history, Icons.Default.History, true, false, true, false, TransitionStyle.PUSH)
    object ConsultHistory : AppDestination("consult_history", "consult_history", R.string.title_consult_history, Icons.Default.History, true, false, true, false, TransitionStyle.PUSH)
    object Plans : AppDestination("plans", "plans", R.string.nav_plans, Icons.Default.AutoAwesome, true, false, true, false, TransitionStyle.PUSH)
    object Astrologers : AppDestination("astrologers", "astrologers", R.string.nav_astrologers, Icons.Default.Face, true, false, true, false, TransitionStyle.PUSH)
    object Rashis : AppDestination("rashis?rashiId={rashiId}", "rashis", R.string.nav_rashis, Icons.Default.PlayArrow, true, false, true, false, TransitionStyle.PUSH)
    object Planets : AppDestination("planets", "planets", R.string.title_planets, null, true, false, true, false, TransitionStyle.PUSH)
    object Nakshatras : AppDestination("nakshatras", "nakshatras", R.string.title_nakshatras, null, true, false, true, false, TransitionStyle.PUSH)
    object Houses : AppDestination("houses", "houses", R.string.title_houses, null, true, false, true, false, TransitionStyle.PUSH)
    object Yogas : AppDestination("yogas", "yogas", R.string.title_yogas, null, true, false, true, false, TransitionStyle.PUSH)

    companion object {
        fun fromRoute(route: String?): AppDestination {
            if (route == null) return Intro
            val base = route.substringBefore("?").substringBefore("/")
            return values().firstOrNull { it.baseRoute == base } ?: Dashboard
        }

        fun values(): Array<AppDestination> = arrayOf(
            Intro, LogoSplash, Login,
            Dashboard, Blogs, Kundli, Consult,
            Chat, ChatSelect, Profile, Forecast, Match, MatchHistory, ConsultHistory,
            Plans, Astrologers, Rashis, Planets, Nakshatras, Houses, Yogas
        )
    }
}
