package com.astranavi.app.ui.navigation

/**
 * Chrome contract — DO NOT VIOLATE:
 *   1. Every signed-in destination MUST set topBarConfig.visible = true. No page renders its
 *      own status-bar-padded header.
 *   2. Pages MUST NOT call statusBarsPadding() at the page level. The global
 *      CenterAlignedTopAppBar in MainActivity owns the status-bar inset.
 *   3. Page body MUST NOT duplicate the AppDestination.title string at the top
 *      (e.g. no in-page "Vedic Wisdom" headline on Blogs — the top bar already
 *      labels the page).
 *   4. Right-side actions follow the pattern: [CreditBadge] [Menu OR History]
 *      - Default: Menu icon opens the Cosmic drawer
 *      - History pages: History icon replaces Menu
 */

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.astranavi.app.R

enum class TransitionStyle {
    NONE,
    TAB,
    PUSH
}

/**
 * Single source of truth for all app destinations and their chrome configuration.
 */
sealed class AppDestination(
    val route: String,
    val baseRoute: String,
    val titleResId: Int,
    val icon: ImageVector?,
    val requiresAuth: Boolean,
    val isTopLevel: Boolean,
    val showBottomBar: Boolean,
    val transitionStyle: TransitionStyle,
    val topBarConfig: TopAppBarConfig
) {
    // Auth Group
    object Intro : AppDestination(
        route = "intro",
        baseRoute = "intro",
        titleResId = R.string.title_intro,
        icon = null,
        requiresAuth = false,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.NONE,
        topBarConfig = TopAppBarConfig(visible = false, scrollBehavior = ScrollBehavior.NONE)
    )

    object LogoSplash : AppDestination(
        route = "logo_splash",
        baseRoute = "logo_splash",
        titleResId = R.string.title_welcome,
        icon = null,
        requiresAuth = false,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.NONE,
        topBarConfig = TopAppBarConfig(visible = false, scrollBehavior = ScrollBehavior.NONE)
    )

    object Login : AppDestination(
        route = "login",
        baseRoute = "login",
        titleResId = R.string.title_login,
        icon = null,
        requiresAuth = false,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.NONE,
        topBarConfig = TopAppBarConfig(visible = false, scrollBehavior = ScrollBehavior.NONE)
    )

    // Main Group (Top Level Tabs)
    object Dashboard : AppDestination(
        route = "home",
        baseRoute = "home",
        titleResId = R.string.nav_home,
        icon = Icons.Default.Home,
        requiresAuth = true,
        isTopLevel = true,
        showBottomBar = true,
        transitionStyle = TransitionStyle.TAB,
        topBarConfig = TopAppBarConfig(
            showBackButton = false,
            rightAction = RightAction.MENU,
            customTopPadding = (-16).dp
        )
    )

    object Blogs : AppDestination(
        route = "blogs",
        baseRoute = "blogs",
        titleResId = R.string.nav_blogs,
        icon = Icons.AutoMirrored.Filled.MenuBook,
        requiresAuth = true,
        isTopLevel = true,
        showBottomBar = true,
        transitionStyle = TransitionStyle.TAB,
        topBarConfig = TopAppBarConfig(
            showBackButton = false,
            rightAction = RightAction.MENU,
            customTopPadding = (-16).dp
        )
    )

    object Kundli : AppDestination(
        route = "kundli",
        baseRoute = "kundli",
        titleResId = R.string.nav_kundli,
        icon = Icons.Default.Star,
        requiresAuth = true,
        isTopLevel = true,
        showBottomBar = false,
        transitionStyle = TransitionStyle.TAB,
        topBarConfig = TopAppBarConfig(
            showBackButton = true,
            rightAction = RightAction.MENU,
            customTopPadding = (-16).dp
        )
    )

    object Consult : AppDestination(
        route = "consult",
        baseRoute = "consult",
        titleResId = R.string.nav_consult,
        icon = Icons.Default.Info,
        requiresAuth = true,
        isTopLevel = true,
        showBottomBar = false,
        transitionStyle = TransitionStyle.TAB,
        topBarConfig = TopAppBarConfig(
            showBackButton = true,
            rightAction = RightAction.HISTORY,
            customTopPadding = (-16).dp
        )
    )

    // Detail / Non-tab Main Group
    object Chat : AppDestination(
        route = "chat",
        baseRoute = "chat",
        titleResId = R.string.nav_chat,
        icon = Icons.Default.AutoAwesome,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            titleMode = TitleMode.ChatAvatar,
            rightAction = RightAction.CHAT_ACTIONS,
            scrollBehavior = ScrollBehavior.PIN,
            customTopPadding = (-16).dp
        )
    )

    object AvatarSelection : AppDestination(
        route = "avatar_selection",
        baseRoute = "avatar_selection",
        titleResId = R.string.title_chat_select,
        icon = Icons.Default.AutoAwesome,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            rightAction = RightAction.NONE,
            customTopPadding = (-16).dp
        )
    )

    object Profile : AppDestination(
        route = "profile",
        baseRoute = "profile",
        titleResId = R.string.nav_profile,
        icon = Icons.Default.Person,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            rightAction = RightAction.NONE,
            customTopPadding = (-16).dp
        )
    )

    // Details
    object Forecast : AppDestination(
        route = "forecast",
        baseRoute = "forecast",
        titleResId = R.string.nav_forecast,
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            rightAction = RightAction.NONE,
            customTopPadding = (-16).dp
        )
    )

    object Match : AppDestination(
        route = "match",
        baseRoute = "match",
        titleResId = R.string.nav_match,
        icon = Icons.Default.Favorite,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            rightAction = RightAction.HISTORY,
            customTopPadding = (-16).dp
        )
    )

    object MatchHistory : AppDestination(
        route = "match_history",
        baseRoute = "match_history",
        titleResId = R.string.title_match_history,
        icon = Icons.Default.History,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            rightAction = RightAction.NONE,
            customTopPadding = (-16).dp
        )
    )

    object ConsultHistory : AppDestination(
        route = "consult_history",
        baseRoute = "consult_history",
        titleResId = R.string.title_consult_history,
        icon = Icons.Default.History,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            rightAction = RightAction.NONE,
            customTopPadding = (-16).dp
        )
    )

    object Plans : AppDestination(
        route = "plans",
        baseRoute = "plans",
        titleResId = R.string.nav_plans,
        icon = Icons.Default.AutoAwesome,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            rightAction = RightAction.NONE,
            customTopPadding = (-16).dp
        )
    )

    object Astrologers : AppDestination(
        route = "astrologers",
        baseRoute = "astrologers",
        titleResId = R.string.nav_astrologers,
        icon = Icons.Default.Face,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            rightAction = RightAction.NONE,
            customTopPadding = (-16).dp
        )
    )

    object Rashis : AppDestination(
        route = "rashis",
        baseRoute = "rashis",
        titleResId = R.string.nav_rashis,
        icon = Icons.Default.PlayArrow,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            drawsBehind = true,
            rightAction = RightAction.NONE,
            customTopPadding = (-16).dp
        )
    )

    object Planets : AppDestination(
        route = "planets",
        baseRoute = "planets",
        titleResId = R.string.title_planets,
        icon = null,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            drawsBehind = true,
            rightAction = RightAction.NONE,
            customTopPadding = (-16).dp
        )
    )

    object Nakshatras : AppDestination(
        route = "nakshatras",
        baseRoute = "nakshatras",
        titleResId = R.string.title_nakshatras,
        icon = null,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            drawsBehind = true,
            rightAction = RightAction.NONE,
            customTopPadding = (-16).dp
        )
    )

    object Houses : AppDestination(
        route = "houses",
        baseRoute = "houses",
        titleResId = R.string.title_houses,
        icon = null,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            rightAction = RightAction.NONE,
            customTopPadding = (-16).dp
        )
    )

    object Yogas : AppDestination(
        route = "yogas",
        baseRoute = "yogas",
        titleResId = R.string.title_yogas,
        icon = null,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            rightAction = RightAction.NONE,
            customTopPadding = (-16).dp
        )
    )

    object TestPage : AppDestination(
        route = "test_page",
        baseRoute = "test_page",
        titleResId = R.string.app_name,
        icon = Icons.Default.BugReport,
        requiresAuth = true,
        isTopLevel = false,
        showBottomBar = false,
        transitionStyle = TransitionStyle.PUSH,
        topBarConfig = TopAppBarConfig(
            rightAction = RightAction.NONE,
            customTopPadding = (-16).dp
        )
    )

    companion object {
        fun fromRoute(route: String?): AppDestination {
            if (route == null) return Dashboard
            val base = route.substringBefore("?").substringBefore("/")
            return allDestinations.firstOrNull { it.baseRoute == base }
                ?: Dashboard // Fallback to Dashboard for unknown routes
        }

        val allDestinations: List<AppDestination> = listOf(
            Intro, LogoSplash, Login,
            Dashboard, Blogs, Kundli, Consult,
            Chat, AvatarSelection, Profile,
            Forecast, Match, MatchHistory, ConsultHistory,
            Plans, Astrologers, Rashis, Planets, Nakshatras, Houses, Yogas,
            TestPage
        )
    }
}
