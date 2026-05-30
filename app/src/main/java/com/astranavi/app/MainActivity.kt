package com.astranavi.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.astranavi.app.ui.navigation.AppDestination
import com.astranavi.app.ui.navigation.TransitionStyle
import com.astranavi.app.ui.navigation.TopAppBarConfig
import com.astranavi.app.ui.navigation.RightAction
import com.astranavi.app.ui.navigation.LocalTopBarConfigOverride
import com.astranavi.app.ui.navigation.ScrollBehavior
import com.astranavi.app.data.api.RetrofitClient
import com.astranavi.app.data.cache.ApiResponseCache
import com.astranavi.app.data.repository.*
import com.astranavi.app.ui.astrologers.AstrologersScreen
import com.astranavi.app.ui.astrologers.AstrologersViewModel
import com.astranavi.app.ui.chat.ChatScreen
import com.astranavi.app.ui.chat.ChatViewModel
import com.astranavi.app.ui.chat.AvatarSelectionScreen
import com.astranavi.app.ui.chat.AvatarSelectionViewModel
import com.astranavi.app.ui.dashboard.DashboardScreen
import com.astranavi.app.ui.dashboard.DashboardViewModel
import com.astranavi.app.ui.forecast.ForecastScreen
import com.astranavi.app.ui.forecast.ForecastViewModel
import com.astranavi.app.ui.components.ParticleBackground
import com.astranavi.app.ui.components.CreditBadge
import com.astranavi.app.ui.components.LanguageChip
import com.astranavi.app.ui.components.PaywallCard
import com.astranavi.app.ui.components.PaywallFullBlock
import com.astranavi.app.ui.components.AnimatedAtmosphericGlow
import com.astranavi.app.ui.components.GlowColors
import com.astranavi.app.ui.components.NeutralGlowColors
import com.astranavi.app.ui.components.GlowRegistry
import com.astranavi.app.ui.components.LocalGlowRegistry
import com.astranavi.app.ui.chat.ChatAvatarImage
import com.astranavi.app.ui.chat.FallbackChatAvatarCatalog
import com.astranavi.app.data.model.ChatAvatar
import com.astranavi.app.ui.entitlement.EntitlementViewModel
import com.astranavi.app.ui.entitlement.PlansPage
import com.astranavi.app.ui.entitlement.PlansViewModel
import com.astranavi.app.data.model.PaywallCardData
import com.astranavi.app.ui.consult.ConsultScreen
import com.astranavi.app.ui.consult.ConsultViewModel
import com.astranavi.app.ui.knowledge.HouseScreen
import com.astranavi.app.ui.knowledge.KnowledgeHubScreen
import com.astranavi.app.ui.knowledge.NakshatraScreen
import com.astranavi.app.ui.knowledge.PlanetScreen
import com.astranavi.app.ui.knowledge.YogaScreen
import com.astranavi.app.ui.kundli.KundliScreen
import com.astranavi.app.ui.kundli.KundliViewModel
import com.astranavi.app.ui.login.LoginScreen
import com.astranavi.app.ui.login.LoginViewModel
import com.astranavi.app.ui.login.RegistrationViewModel
import com.astranavi.app.ui.match.MatchHistoryScreen
import com.astranavi.app.ui.match.MatchHistoryViewModel
import com.astranavi.app.ui.match.MatchScreen
import com.astranavi.app.ui.match.MatchViewModel
import com.astranavi.app.ui.profile.ProfileScreen
import com.astranavi.app.ui.profile.ProfileViewModel
import com.astranavi.app.ui.rashis.RashiScreen
import com.astranavi.app.ui.rashis.RashiViewModel
import com.astranavi.app.ui.test.TestScreen
import com.astranavi.app.ui.test.TestViewModel
import com.astranavi.app.ui.splash.IntroAnimationScreen
import com.astranavi.app.ui.splash.LogoSplashScreen
import com.astranavi.app.ui.theme.AstraNaviTheme
import com.astranavi.app.util.LocaleManager
import com.astranavi.app.util.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.astranavi.app.ui.ViewModelFactory
import com.astranavi.app.ui.consult.ConsultHistoryScreen
import com.astranavi.app.ui.consult.ConsultHistoryViewModel
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.astranavi.app.ui.entitlement.EntitlementUiState

sealed class Screen(val route: String, @StringRes val labelRes: Int, val icon: ImageVector) {
    object Dashboard : Screen("home", R.string.nav_home, Icons.Default.Home)
    object Forecast : Screen("forecast", R.string.nav_forecast, Icons.Default.TrendingUp)
    object Kundli : Screen("kundli", R.string.nav_kundli, Icons.Default.Star)
    object Match : Screen("match", R.string.nav_match, Icons.Default.Favorite)
    object Consult : Screen("consult", R.string.nav_consult, Icons.Default.Info)
    object Blogs : Screen("blogs", R.string.nav_blogs, Icons.AutoMirrored.Filled.MenuBook)
    object Rashis : Screen("rashis", R.string.nav_rashis, Icons.Default.PlayArrow)
    object Chat : Screen("chat", R.string.nav_chat, Icons.Default.AutoAwesome)
    object Profile : Screen("profile", R.string.nav_profile, Icons.Default.Person)
    object Astrologers : Screen("astrologers", R.string.nav_astrologers, Icons.Default.Face)
    object Plans : Screen("plans", R.string.nav_plans, Icons.Default.AutoAwesome)
}

val LocalTopBarTitle = compositionLocalOf<((String?) -> Unit)?> { null }
val LocalTopBarColor = compositionLocalOf<((Color?) -> Unit)?> { null }
val LocalEntitlementViewModel = compositionLocalOf<EntitlementViewModel?> { null }
val LocalBottomBarHeight = compositionLocalOf { 0.dp }

private fun buildChatRoute(prompt: String?, area: String?, avatarId: String? = null): String {
    val params = listOfNotNull(
        prompt?.takeIf { it.isNotBlank() }?.let { "prompt=${Uri.encode(it)}" },
        area?.takeIf { it.isNotBlank() }?.let { "area=${Uri.encode(it)}" },
        avatarId?.takeIf { it.isNotBlank() }?.let { "avatarId=${Uri.encode(it)}" }
    )
    return if (params.isEmpty()) Screen.Chat.route
    else "${Screen.Chat.route}?${params.joinToString("&")}"
}

private fun buildAvatarSelectionRoute(prompt: String?, area: String?): String {
    val params = listOfNotNull(
        prompt?.takeIf { it.isNotBlank() }?.let { "prompt=${Uri.encode(it)}" },
        area?.takeIf { it.isNotBlank() }?.let { "area=${Uri.encode(it)}" }
    )
    return if (params.isEmpty()) "avatar_selection"
    else "avatar_selection?${params.joinToString("&")}"
}

@Composable
private fun ProvideLocalizedContext(
    languageTag: String,
    content: @Composable () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val baseConfiguration = androidx.compose.ui.platform.LocalConfiguration.current
    val localizedConfiguration = remember(languageTag, baseConfiguration) {
        android.content.res.Configuration(baseConfiguration).apply {
            setLocale(java.util.Locale.forLanguageTag(languageTag))
        }
    }
    val localizedContext = remember(languageTag, context) {
        context.createConfigurationContext(localizedConfiguration)
    }
    CompositionLocalProvider(
        androidx.compose.ui.platform.LocalContext provides localizedContext,
        androidx.compose.ui.platform.LocalConfiguration provides localizedConfiguration
    ) {
        content()
    }
}

@Composable
fun RowScope.NavBarItem(
    screen: Screen,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            screen.icon,
            contentDescription = stringResource(screen.labelRes),
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            stringResource(screen.labelRes),
            color = if (isSelected) activeColor else inactiveColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(3.dp)
                    .background(activeColor, CircleShape)
            )
        }
    }
}

@Composable
fun CosmicHeader(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.drawer_celestial_guide),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.btn_close),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun CompactProfileCard(
    userName: String,
    lagnaSign: String?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(92.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.drawer_good_evening),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (lagnaSign != null) {
                        Text(
                            text = stringResource(R.string.drawer_rising_sign, lagnaSign),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = " · ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                    Text(
                        text = stringResource(R.string.drawer_view_profile),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AskAiCta(onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(72.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(accent.copy(alpha = 0.22f), accent.copy(alpha = 0.06f))
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(accent.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.drawer_cta_ask_ai_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.drawer_cta_ask_ai_sub),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DrawerSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
fun DrawerRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    contentColor: Color? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val tint = contentColor ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            color = tint,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun KnowledgeChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

val sessionManager = SessionManager(this)
        runBlocking { sessionManager.migrateLegacyTokensIfPresent() }
        LocaleManager.apply(runBlocking { sessionManager.userLanguage.first() })
        RetrofitClient.init(sessionManager)
        val apiService = RetrofitClient.instance
        val apiCache = ApiResponseCache(sessionManager)
        val authRepository = AuthRepository(apiService, apiCache)
        val dashboardRepository = DashboardRepository(apiService, apiCache)
        val astrologyRepository = AstrologyRepository(apiService, apiCache)
        val entitlementRepository = EntitlementRepository(apiService)

        setContent {
            val themePreference by sessionManager.themePreference.collectAsState(initial = "system")
            val userName by sessionManager.userName.collectAsState(initial = "Astra User")
            val lagnaSign by sessionManager.lagnaSign.collectAsState(initial = null)
            val appLanguage by sessionManager.userLanguage.collectAsState(initial = "en")

            ProvideLocalizedContext(languageTag = appLanguage) {
                AstraNaviTheme(themePreference = themePreference) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ParticleBackground()

                        val navController = rememberNavController()
                        var startDestination by remember { mutableStateOf<String?>(null) }
                        val scope = rememberCoroutineScope()
                        var showLogoutDialog by remember { mutableStateOf(false) }
                        var dynamicTitle by remember { mutableStateOf<String?>(null) }
                        var dynamicTopBarColor by remember { mutableStateOf<Color?>(null) }
                        val context = androidx.compose.ui.platform.LocalContext.current

                        val sharedViewModelFactory = remember {
                            ViewModelFactory(
                                authRepository = authRepository,
                                dashboardRepository = dashboardRepository,
                                astrologyRepository = astrologyRepository,
                                entitlementRepository = entitlementRepository,
                                sessionManager = sessionManager
                            )
                        }

                        val entitlementViewModel: EntitlementViewModel = viewModel(factory = sharedViewModelFactory)
                        val entitlementState by entitlementViewModel.uiState
                        val activePaywall by entitlementViewModel.activePaywall

                        LaunchedEffect(Unit) {
                            startDestination = "intro"
                        }

                        if (startDestination != null) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination

                            LaunchedEffect(currentDestination?.route) {
                                dynamicTitle = null
                                dynamicTopBarColor = null
                                val route = currentDestination?.route?.substringBefore("?")
                                if (route != null && route != "login" && route != "intro" && route != "logo_splash") {
                                    entitlementViewModel.refreshAll()
                                }
                            }

                            // Get destination config from single source of truth
                            val appDestination = remember(currentDestination?.route) {
                                AppDestination.fromRoute(currentDestination?.route)
                            }

                            // Allow screens to override config dynamically (e.g., ConsultScreen for different steps)
                            val topBarConfigOverride = LocalTopBarConfigOverride.current
                            val effectiveTopBarConfig = topBarConfigOverride ?: appDestination.topBarConfig

                            val showMainActivityTopBar = effectiveTopBarConfig.visible
                            val showBottomBar = appDestination.showBottomBar
                            val isTopLevel = appDestination.isTopLevel

                            val defaultTopBarTitle = remember(currentDestination, appLanguage) {
                                context.getString(appDestination.titleResId)
                            }

                            val topBarTitle = dynamicTitle ?: defaultTopBarTitle
                            val currentBaseRoute = currentDestination?.route?.substringBefore("?")
                            val drawsBehindTopBar = effectiveTopBarConfig.drawsBehind

                            if (showLogoutDialog) {
                                AlertDialog(
                                    onDismissRequest = { showLogoutDialog = false },
                                    title = { Text(stringResource(R.string.logout_title)) },
                                    text = { Text(stringResource(R.string.logout_message)) },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                showLogoutDialog = false
                                                entitlementViewModel.reset()
                                                scope.launch {
                                                    sessionManager.clearSession()
                                                    navController.navigate("login") {
                                                        popUpTo(0) { inclusive = true }
                                                    }
                                                }
                                            }
                                        ) {
                                            Text(stringResource(R.string.logout_confirm))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showLogoutDialog = false }) {
                                            Text(stringResource(R.string.logout_cancel))
                                        }
                                    }
                                )
                            }

                            var isMenuOpen by remember { mutableStateOf(false) }

                            LaunchedEffect(currentDestination?.route) {
                                isMenuOpen = false
                            }

                            BackHandler(enabled = isMenuOpen) {
                                isMenuOpen = false
                            }

                            val backDispatcher =
                                LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

                            val glowRegistry = remember { GlowRegistry() }

                            val currentLanguage by sessionManager.userLanguage.collectAsState(initial = "en")
                            var avatarMenuExpanded by remember { mutableStateOf(false) }

                            val density = LocalDensity.current
                            val barsVisible = remember { mutableStateOf(true) }
                            var topBarHeightPx by remember { mutableStateOf(0) }
                            var bottomBarHeightPx by remember { mutableStateOf(0) }
                            val barHideProgress by animateFloatAsState(
                                targetValue = if (barsVisible.value) 0f else 1f,
                                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                label = "barHideProgress"
                            )
                            val scrollHideEnabled = effectiveTopBarConfig.scrollBehavior == ScrollBehavior.HIDE_ON_SCROLL
                            LaunchedEffect(currentBaseRoute) {
                                barsVisible.value = true
                            }
                            val scrollHideConnection = remember {
                                object : NestedScrollConnection {
                                    override fun onPreScroll(
                                        available: Offset,
                                        source: NestedScrollSource
                                    ): Offset {
                                        val dy = available.y
                                        if (dy < -2f) barsVisible.value = false
                                        else if (dy > 2f) barsVisible.value = true
                                        return Offset.Zero
                                    }
                                }
                            }
                            val topBarHideDp = with(density) { (topBarHeightPx * barHideProgress).toDp() }
                            val bottomBarHideDp = with(density) { (bottomBarHeightPx * barHideProgress).toDp() }

                            Box(modifier = Modifier.fillMaxSize()) {
                                val glowDurationMillis = when (AppDestination.fromRoute(currentDestination?.route).transitionStyle) {
                                    TransitionStyle.PUSH -> 200
                                    else -> 160
                                }
                                AnimatedAtmosphericGlow(
                                    accentColor = glowRegistry.colors.accent,
                                    deepColor = glowRegistry.colors.deep,
                                    radialColor = glowRegistry.colors.radial,
                                    durationMillis = glowDurationMillis,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Scaffold(
                                    containerColor = Color.Transparent,
                                    contentWindowInsets = WindowInsets(0.dp),
                                    topBar = {
                                        if (showMainActivityTopBar) {
                                            val animatedBarContentColor by animateColorAsState(
                                                targetValue = dynamicTopBarColor ?: MaterialTheme.colorScheme.onSurface,
                                                animationSpec = tween(160),
                                                label = "topBarContentColor"
                                            )
                                            CenterAlignedTopAppBar(
                                                modifier = Modifier
                                                    .onSizeChanged { topBarHeightPx = it.height }
                                                    .graphicsLayer { translationY = -topBarHeightPx * barHideProgress },
                                                title = {
                                                    if (currentBaseRoute == Screen.Chat.route) {
                                                        val chatViewModel: ChatViewModel = viewModel(
                                                            viewModelStoreOwner = this@MainActivity,
                                                            factory = sharedViewModelFactory
                                                        )
                                                        val activeAvatar by chatViewModel.activeAvatar
                                                        Box {
                                                            Row(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(20.dp))
                                                                    .clickable { avatarMenuExpanded = true }
                                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                ChatAvatarImage(
                                                                    avatar = activeAvatar,
                                                                    modifier = Modifier
                                                                        .size(28.dp)
                                                                        .clip(CircleShape)
                                                                )
                                                                Spacer(Modifier.width(8.dp))
                                                                Text(
                                                                    activeAvatar?.name ?: stringResource(R.string.nav_chat),
                                                                    fontWeight = FontWeight.Black,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                                Icon(
                                                                    Icons.Default.ArrowDropDown,
                                                                    contentDescription = stringResource(R.string.cd_switch_avatar)
                                                                )
                                                            }
                                                            DropdownMenu(
                                                                expanded = avatarMenuExpanded,
                                                                onDismissRequest = { avatarMenuExpanded = false }
                                                            ) {
                                                                FallbackChatAvatarCatalog.avatars.forEach { avatar ->
                                                                    val isSelected = avatar.avatarId == activeAvatar?.avatarId
                                                                    DropdownMenuItem(
                                                                        text = {
                                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                                ChatAvatarImage(
                                                                                    avatar = avatar,
                                                                                    modifier = Modifier
                                                                                        .size(28.dp)
                                                                                        .clip(CircleShape)
                                                                                )
                                                                                Spacer(Modifier.width(10.dp))
                                                                                Column {
                                                                                    Text(
                                                                                        avatar.name,
                                                                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                                                                                    )
                                                                                    Text(
                                                                                        avatar.title,
                                                                                        fontSize = 11.sp,
                                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                                    )
                                                                                }
                                                                            }
                                                                        },
                                                                        onClick = {
                                                                            avatarMenuExpanded = false
                                                                            if (!isSelected) chatViewModel.switchActiveAvatar(avatar)
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                         Crossfade(
                                                             targetState = topBarTitle,
                                                             animationSpec = tween(130),
                                                             label = "topBarTitle"
                                                         ) { title ->
                                                             Text(
                                                                 title,
                                                                 fontWeight = FontWeight.Black
                                                             )
                                                         }
                                                    }
                                                },
                                                navigationIcon = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (!effectiveTopBarConfig.showBackButton) {
                                                            Spacer(Modifier.width(4.dp))
                                                        } else {
                                                            IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                                                                Icon(
                                                                    Icons.Default.ArrowBack,
                                                                    contentDescription = stringResource(R.string.cd_back)
                                                                )
                                                            }
                                                        }
                                                        if (effectiveTopBarConfig.showLanguageChip) {
                                                            LanguageChip(
                                                                currentLanguage = currentLanguage,
                                                                onLanguageSelected = { lang ->
                                                                    scope.launch {
                                                                        authRepository.updateLanguage(lang)
                                                                        sessionManager.setUserLanguage(lang)
                                                                    }
                                                                }
                                                            )
                                                        }
                                                    }
                                                },
                                                actions = {
                                                    // Always show CreditBadge first (if authenticated)
                                                    if (appDestination.requiresAuth) {
                                                        val credits = if (entitlementState is EntitlementUiState.Success) (entitlementState as EntitlementUiState.Success).balance.totalCreditsRemaining else 0
                                                        val tier = if (entitlementState is EntitlementUiState.Success) (entitlementState as EntitlementUiState.Success).balance.tier else "free"
                                                        CreditBadge(credits = credits, tier = tier, onClick = { navController.navigate(Screen.Plans.route) })
                                                    }

                                                    // Then show the right action based on config
                                                    when (effectiveTopBarConfig.rightAction) {
                                                        RightAction.MENU -> {
                                                            IconButton(onClick = { isMenuOpen = true }) {
                                                                Icon(
                                                                    Icons.Default.Menu,
                                                                    contentDescription = stringResource(R.string.cd_open_menu)
                                                                )
                                                            }
                                                        }
                                                        RightAction.HISTORY -> {
                                                            IconButton(onClick = {
                                                                when (currentBaseRoute) {
                                                                    Screen.Consult.route -> navController.navigate("consult_history")
                                                                    Screen.Match.route -> navController.navigate("match_history")
                                                                    else -> { /* no-op */ }
                                                                }
                                                            }) {
                                                                Icon(Icons.Default.History, contentDescription = stringResource(R.string.cd_history))
                                                            }
                                                        }
                                                        RightAction.CHAT_ACTIONS -> {
                                                            val chatViewModel: ChatViewModel = viewModel(
                                                                viewModelStoreOwner = this@MainActivity,
                                                                factory = sharedViewModelFactory
                                                            )
                                                            val chatShowHistory by chatViewModel.showHistory
                                                            if (chatShowHistory) {
                                                                IconButton(onClick = { chatViewModel.setShowHistory(false) }) {
                                                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close_history))
                                                                }
                                                            } else {
                                                                IconButton(onClick = { chatViewModel.toggleHistory() }) {
                                                                    Icon(Icons.Default.History, contentDescription = stringResource(R.string.cd_chat_history))
                                                                }
                                                                IconButton(onClick = {
                                                                    navController.navigate("avatar_selection")
                                                                }) {
                                                                    Icon(Icons.Default.AddComment, contentDescription = stringResource(R.string.cd_new_chat))
                                                                }
                                                            }
                                                        }
                                                        RightAction.NONE -> {
                                                            // No additional action
                                                        }
                                                    }
                                                },
                                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                                    containerColor = Color.Transparent,
                                                    titleContentColor = animatedBarContentColor,
                                                    navigationIconContentColor = animatedBarContentColor,
                                                    actionIconContentColor = animatedBarContentColor
                                                ),
                                                windowInsets = WindowInsets.statusBars
                                            )
                                        }
                                    },
                                    bottomBar = {
                                         if (showBottomBar) {
                                             val isCurrentlyDark = when (themePreference) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
                                             val activeGold = if (isCurrentlyDark) Color(0xFFFAF7F2) else Color(0xFF7C3AED)
                                             val inactiveColor = Color.Gray
                                             val navBarInsets = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

                                             Box(
                                                 modifier = Modifier
                                                     .fillMaxWidth()
                                                     .wrapContentHeight(unbounded = true)
                                                     .onSizeChanged { bottomBarHeightPx = it.height }
                                                     .graphicsLayer { translationY = bottomBarHeightPx * barHideProgress },
                                                 contentAlignment = Alignment.BottomCenter
                                             ) {
                                                 Surface(
                                                     modifier = Modifier.fillMaxWidth(),
                                                     shape = RectangleShape,
                                                     color = MaterialTheme.colorScheme.background,
                                                     tonalElevation = 0.dp
                                                 ) {
                                                     Column(modifier = Modifier.fillMaxWidth()) {
HorizontalDivider(
                                                              color = if (glowRegistry.count > 0) glowRegistry.colors.accent.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant,
                                                              thickness = 0.5.dp
                                                          )
                                                         Row(
                                                             modifier = Modifier.fillMaxWidth().padding(bottom = navBarInsets),
                                                             verticalAlignment = Alignment.CenterVertically,
                                                             horizontalArrangement = Arrangement.SpaceEvenly
                                                         ) {
                                                             val leftItems = listOf(Screen.Dashboard, Screen.Kundli)
                                                             val rightItems = listOf(Screen.Consult, Screen.Blogs)

                                                             leftItems.forEach { screen ->
                                                                 val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                                                 NavBarItem(
                                                                     screen = screen,
                                                                     isSelected = isSelected,
                                                                     activeColor = activeGold,
                                                                     inactiveColor = inactiveColor,
                                                                     onClick = {
                                                                         if (screen == Screen.Dashboard) {
                                                                             navController.popBackStack(Screen.Dashboard.route, false)
                                                                         } else {
                                                                             navController.navigate(screen.route) {
                                                                                 popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                                                 launchSingleTop = true
                                                                                 restoreState = true
                                                                             }
                                                                         }
                                                                     }
                                                                 )
                                                             }
                                                             Spacer(modifier = Modifier.width(72.dp))
                                                             rightItems.forEach { screen ->
                                                                 val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                                                 NavBarItem(
                                                                     screen = screen,
                                                                     isSelected = isSelected,
                                                                     activeColor = activeGold,
                                                                     inactiveColor = inactiveColor,
                                                                     onClick = {
                                                                         navController.navigate(screen.route) {
                                                                             popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                                             launchSingleTop = true
                                                                             restoreState = true
                                                                         }
                                                                     }
                                                                 )
                                                             }
                                                         }
                                                     }
                                                 }
                                                 Box(
                                                     modifier = Modifier.align(Alignment.TopCenter).size(64.dp).background(MaterialTheme.colorScheme.surface, CircleShape),
                                                     contentAlignment = Alignment.Center
                                                 ) {
val fabBg = MaterialTheme.colorScheme.secondary
                                                       val fabFg = MaterialTheme.colorScheme.onSecondary
                                                     FloatingActionButton(
                                                         onClick = {
                                                             navController.navigate("avatar_selection") {
                                                                 popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                                 launchSingleTop = true
                                                                 restoreState = true
                                                             }
                                                         },
                                                         shape = CircleShape,
                                                         containerColor = fabBg,
                                                         elevation = FloatingActionButtonDefaults.elevation(4.dp),
                                                         modifier = Modifier.size(56.dp)
                                                     ) {
                                                         Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                             Icon(Screen.Chat.icon, contentDescription = null, tint = fabFg, modifier = Modifier.size(30.dp))
                                                             Text(stringResource(R.string.nav_chat).uppercase(), color = fabFg, fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                                                         }
                                                     }
                                                 }
                                             }
                                         }
                                    }
) { innerPadding ->
                                    CompositionLocalProvider(
                                        LocalTopBarTitle provides { dynamicTitle = it },
                                        LocalTopBarColor provides { dynamicTopBarColor = it },
                                        LocalEntitlementViewModel provides entitlementViewModel,
                                        LocalGlowRegistry provides glowRegistry
                                    ) {
                                        NavHost(
                                            navController = navController,
                                            startDestination = startDestination!!,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .then(if (scrollHideEnabled) Modifier.nestedScroll(scrollHideConnection) else Modifier)
                                                .padding(
                                                     top = run {
                                                         val basePadding = if (drawsBehindTopBar) {
                                                             0.dp
                                                         } else {
                                                             val customPadding = effectiveTopBarConfig.customTopPadding
                                                             if (customPadding != null) {
                                                                 (innerPadding.calculateTopPadding() + customPadding).coerceAtLeast(0.dp)
                                                             } else {
                                                                 innerPadding.calculateTopPadding()
                                                             }
                                                         }
                                                         (basePadding - topBarHideDp).coerceAtLeast(0.dp)
                                                     },
                                                     bottom = (innerPadding.calculateBottomPadding() - bottomBarHideDp).coerceAtLeast(0.dp)
                                                 )
                                        ) {
                                            composable("intro") {
                                                var isFirstLaunch by remember { mutableStateOf(true) }
                                                var isLoggedIn by remember { mutableStateOf(false) }
                                                LaunchedEffect(Unit) {
                                                    isFirstLaunch = !(sessionManager.hasSeenIntro.first())
                                                    isLoggedIn = sessionManager.userId.first() != null
                                                }
                                                IntroAnimationScreen(
                                                    isLoggedIn = isLoggedIn,
                                                    isFirstLaunch = isFirstLaunch,
                                                    onIntroComplete = {
                                                        scope.launch {
                                                            sessionManager.setHasSeenIntro(true)
                                                            val userId = sessionManager.userId.first()
                                                            val next = if (userId != null) {
                                                                val isProfileComplete = sessionManager.profileComplete.first() ?: false
                                                                if (isProfileComplete) Screen.Dashboard.route else Screen.Profile.route
                                                            } else "login"
                                                            navController.navigate(next) {
                                                                popUpTo("intro") { inclusive = true }
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                            composable(
                                                "logo_splash?loadingText={loadingText}",
                                                arguments = listOf(
                                                    navArgument("loadingText") {
                                                        type = NavType.StringType
                                                        nullable = true
                                                        defaultValue = null
                                                    }
                                                )
                                            ) { backStackEntry ->
                                                val loadingText = backStackEntry.arguments?.getString("loadingText")
                                                    ?: "Preparing your first guidance…"
                                                LogoSplashScreen(
                                                    loadingText = loadingText,
                                                    onSplashComplete = {
                                                        scope.launch {
                                                            val isProfileComplete = sessionManager.profileComplete.first() ?: false
                                                            val next = if (isProfileComplete) Screen.Dashboard.route else Screen.Profile.route
                                                            navController.navigate(next) {
                                                                popUpTo("logo_splash") { inclusive = true }
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                            composable("login") {
                                                val loginViewModel: LoginViewModel = viewModel(factory = sharedViewModelFactory)
                                                val registrationViewModel: RegistrationViewModel = viewModel(factory = sharedViewModelFactory)
                                                val currentLanguage by sessionManager.userLanguage.collectAsState(initial = "en")
                                                var isRegisterMode by remember { mutableStateOf(false) }

                                                val isLoginSuccess by loginViewModel.isLoginSuccess
                                                val isRegistrationSuccess by registrationViewModel.isRegistrationSuccess

                                                LaunchedEffect(isLoginSuccess) {
                                                    if (isLoginSuccess) {
                                                        isMenuOpen = false
                                                        val text = Uri.encode("Aligning your chart…")
                                                        navController.navigate("logo_splash?loadingText=$text") {
                                                            popUpTo("login") { inclusive = true }
                                                        }
                                                        loginViewModel.reset()
                                                    }
                                                }

                                                LaunchedEffect(isRegistrationSuccess) {
                                                    if (isRegistrationSuccess) {
                                                        isMenuOpen = false
                                                        val text = Uri.encode("Setting up your chart…")
                                                        navController.navigate("logo_splash?loadingText=$text") {
                                                            popUpTo("login") { inclusive = true }
                                                        }
                                                        registrationViewModel.reset()
                                                    }
                                                }

                                                LoginScreen(
                                                    loginViewModel = loginViewModel,
                                                    registrationViewModel = registrationViewModel,
                                                    currentLanguage = currentLanguage,
                                                    isRegisterMode = isRegisterMode,
                                                    onModeChange = { isRegisterMode = it },
                                                    onLanguageSelected = { lang ->
                                                        scope.launch {
                                                            sessionManager.setUserLanguage(lang)
                                                        }
                                                    },
                                                    onTriggerAuthAction = { /* No-op, navigation is handled on success states */ }
                                                )
                                            }
                                            composable(Screen.Dashboard.route) {
                                                val dashboardViewModel: DashboardViewModel = viewModel(factory = sharedViewModelFactory)
                                                DashboardScreen(
                                                    viewModel = dashboardViewModel,
                                                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                                                    onNavigateToRashis = { rashiId ->
                                                        val route = if (rashiId != null) "${Screen.Rashis.route}?rashiId=$rashiId" else Screen.Rashis.route
                                                        navController.navigate(route)
                                                    },
                                                    onNavigateToExperts = { navController.navigate(Screen.Astrologers.route) },
                                                    onNavigateToChat = { prompt, area ->
                                                        navController.navigate(buildAvatarSelectionRoute(prompt, area))
                                                    },
                                                    onChatWithAstrologer = { avatarId ->
                                                        navController.navigate(buildChatRoute(null, null, avatarId))
                                                    },
                                                    onNavigateToKundli = { navController.navigate(Screen.Kundli.route) },
                                                    onNavigateToMatch = { navController.navigate(Screen.Match.route) },
                                                    onNavigateToMatchHistory = { navController.navigate("match_history") },
                                                    onNavigateToNakshatras = { navController.navigate("nakshatras") },
                                                    onNavigateToPlanets = { navController.navigate("planets") },
                                                    onNavigateToConsult = { navController.navigate(Screen.Consult.route) },
                                                    onNavigateToForecast = { area ->
                                                        val route = if (area != null) "${Screen.Forecast.route}?area=$area" else Screen.Forecast.route
                                                        navController.navigate(route)
                                                    }
                                                )
                                            }
                                            composable("${Screen.Forecast.route}?area={area}", arguments = listOf(navArgument("area") { type = NavType.StringType; nullable = true; defaultValue = null })) { backStackEntry ->
                                                val area = backStackEntry.arguments?.getString("area")
                                                val forecastViewModel: ForecastViewModel = viewModel(factory = sharedViewModelFactory)
                                                ForecastScreen(
                                                    viewModel = forecastViewModel,
                                                    initialArea = area,
                                                    onBack = { navController.popBackStack() },
                                                    onNavigateToChat = { prompt ->
                                                        navController.navigate(buildAvatarSelectionRoute(prompt, null))
                                                    }
                                                )
                                            }
                                            composable(Screen.Kundli.route) {
                                                val kundliViewModel: KundliViewModel = viewModel(factory = sharedViewModelFactory)
                                                KundliScreen(viewModel = kundliViewModel, onBack = { navController.popBackStack() })
                                            }
                                            composable(Screen.Match.route) {
                                                val matchViewModel: MatchViewModel = viewModel(factory = sharedViewModelFactory)
                                                MatchScreen(viewModel = matchViewModel, onBack = { navController.popBackStack() }, onViewHistory = { navController.navigate("match_history") })
                                            }
                                            composable("match_history") {
                                                val matchHistoryViewModel: MatchHistoryViewModel = viewModel(factory = sharedViewModelFactory)
                                                MatchHistoryScreen(viewModel = matchHistoryViewModel, onBack = { navController.popBackStack() })
                                            }
                                            composable(Screen.Consult.route) {
                                                val consultViewModel: ConsultViewModel = viewModel(factory = sharedViewModelFactory)
                                                ConsultScreen(viewModel = consultViewModel, onOpenDrawer = { isMenuOpen = true }, onBack = { navController.popBackStack() }, onViewHistory = { navController.navigate("consult_history") })
                                            }
                                            composable("consult_history") {
                                                val consultHistoryViewModel: ConsultHistoryViewModel = viewModel(factory = sharedViewModelFactory)
                                                ConsultHistoryScreen(viewModel = consultHistoryViewModel, onBack = { navController.popBackStack() })
                                            }
                                            composable(Screen.Blogs.route) {
                                                KnowledgeHubScreen(
                                                    onOpenDrawer = { isMenuOpen = true },
                                                    onNavigateToRashis = { navController.navigate(Screen.Rashis.route) },
                                                    onNavigateToPlanets = { navController.navigate("planets") },
                                                    onNavigateToNakshatras = { navController.navigate("nakshatras") },
                                                    onNavigateToHouses = { navController.navigate("houses") },
                                                    onNavigateToYogas = { navController.navigate("yogas") }
                                                )
                                            }
                                            composable("planets") { PlanetScreen(onBack = { navController.popBackStack() }) }
                                            composable("nakshatras") { NakshatraScreen(onBack = { navController.popBackStack() }) }
                                            composable("houses") { HouseScreen(onBack = { navController.popBackStack() }) }
                                            composable("yogas") { YogaScreen(onBack = { navController.popBackStack() }, onOpenDrawer = { isMenuOpen = true }) }
                                            composable("test_page") {
                                                val testViewModel: TestViewModel = viewModel(factory = sharedViewModelFactory)
                                                TestScreen(viewModel = testViewModel, onBack = { navController.popBackStack() })
                                            }
                                            composable(Screen.Astrologers.route) {
                                                val astrologersViewModel: AstrologersViewModel = viewModel(factory = sharedViewModelFactory)
                                                AstrologersScreen(
                                                    viewModel = astrologersViewModel,
                                                    onBack = { navController.popBackStack() },
                                                    onChatWithAvatar = { avatar ->
                                                        navController.navigate(buildChatRoute(null, null, avatar.avatarId))
                                                    }
                                                )
                                            }
                                            composable("${Screen.Rashis.route}?rashiId={rashiId}", arguments = listOf(navArgument("rashiId") { type = NavType.StringType; nullable = true; defaultValue = null })) { backStackEntry ->
                                                val rashiId = backStackEntry.arguments?.getString("rashiId")
                                                val rashiViewModel: RashiViewModel = viewModel(factory = sharedViewModelFactory)
                                                LaunchedEffect(rashiId) { rashiId?.let { rashiViewModel.selectRashiById(it) } }
                                                RashiScreen(viewModel = rashiViewModel, onBack = { navController.popBackStack() })
                                            }
                                            composable(
                                                "${Screen.Chat.route}?prompt={prompt}&area={area}&avatarId={avatarId}",
                                                arguments = listOf(
                                                    navArgument("prompt") { type = NavType.StringType; nullable = true; defaultValue = null },
                                                    navArgument("area") { type = NavType.StringType; nullable = true; defaultValue = null },
                                                    navArgument("avatarId") { type = NavType.StringType; nullable = true; defaultValue = null }
                                                )
                                            ) { backStackEntry ->
                                                val chatViewModel: ChatViewModel = viewModel(
                                                    viewModelStoreOwner = this@MainActivity,
                                                    factory = sharedViewModelFactory
                                                )
                                                val chatShowHistory by chatViewModel.showHistory
                                                LaunchedEffect(chatShowHistory) { dynamicTitle = if (chatShowHistory) context.getString(R.string.chat_history_title) else null }
                                                ChatScreen(
                                                    viewModel = chatViewModel,
                                                    seedPrompt = backStackEntry.arguments?.getString("prompt"),
                                                    seedContext = backStackEntry.arguments?.getString("area"),
                                                    seedAvatarId = backStackEntry.arguments?.getString("avatarId"),
                                                    onBack = { navController.popBackStack() },
                                                    onOpenDrawer = { isMenuOpen = true }
                                                )
                                            }
                                            composable(
                                                "avatar_selection?prompt={prompt}&area={area}",
                                                arguments = listOf(
                                                    navArgument("prompt") { type = NavType.StringType; nullable = true; defaultValue = null },
                                                    navArgument("area") { type = NavType.StringType; nullable = true; defaultValue = null }
                                                )
                                            ) { backStackEntry ->
                                                val avatarSelectionViewModel: AvatarSelectionViewModel = viewModel(factory = sharedViewModelFactory)
                                                val seedPrompt = backStackEntry.arguments?.getString("prompt")
                                                val seedArea = backStackEntry.arguments?.getString("area")
                                                AvatarSelectionScreen(
                                                    viewModel = avatarSelectionViewModel,
                                                    onAvatarSelected = { avatar ->
                                                        navController.navigate(buildChatRoute(seedPrompt, seedArea, avatar.avatarId)) {
                                                            popUpTo("avatar_selection") { inclusive = true }
                                                        }
                                                    }
                                                )
                                            }
                                            composable(Screen.Profile.route) {
                                                val profileViewModel: ProfileViewModel = viewModel(factory = sharedViewModelFactory)
                                                ProfileScreen(
                                                    viewModel = profileViewModel,
                                                    onBack = { navController.popBackStack() },
                                                    onProfileComplete = { navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Profile.route) { inclusive = true } } },
                                                    onAccountDeleted = { navController.navigate("login") { popUpTo(0) { inclusive = true } } },
                                                    onNavigateToKundli = { navController.navigate(Screen.Kundli.route) },
                                                    onNavigateToPlans = { navController.navigate(Screen.Plans.route) }
                                                )
                                            }
                                            composable(Screen.Plans.route) {
                                                val plansViewModel: PlansViewModel = viewModel(factory = sharedViewModelFactory)
                                                PlansPage(viewModel = plansViewModel, onBack = { navController.popBackStack() })
                                            }
                                        }
                                    }
                                }

                                AnimatedVisibility(visible = isMenuOpen, enter = fadeIn(tween(200)), exit = fadeOut(tween(200))) {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { isMenuOpen = false })
                                }

                                if (activePaywall != null) {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { entitlementViewModel.dismissPaywall() })
                                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                                        PaywallFullBlock(paywall = activePaywall!!)
                                    }
                                }

                                AnimatedVisibility(
                                    visible = isMenuOpen,
                                    enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)),
                                    exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250, easing = FastOutSlowInEasing)),
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(0.82f)
                                            .widthIn(max = 360.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                                        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 0.dp, bottomEnd = 0.dp),
                                        shadowElevation = 24.dp,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    ) {
                                        Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).navigationBarsPadding()) {
                                            CosmicHeader(onClose = { isMenuOpen = false })

                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f)
                                                        .verticalScroll(rememberScrollState())
                                                ) {
                                                    CompactProfileCard(
                                                        userName = userName ?: stringResource(R.string.drawer_user_default),
                                                        lagnaSign = lagnaSign,
                                                        onClick = { isMenuOpen = false; navController.navigate(Screen.Profile.route) }
                                                    )

                                                    DrawerSectionLabel(stringResource(R.string.drawer_section_quick_action))
                                                    AskAiCta(onClick = {
                                                        isMenuOpen = false
                                                        navController.navigate("avatar_selection") {
                                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    })

                                                    DrawerSectionLabel(stringResource(R.string.drawer_section_astrology_tools))
                                                    DrawerRow(
                                                        label = stringResource(R.string.drawer_item_match_compatibility),
                                                        icon = Icons.Default.Favorite,
                                                        onClick = { isMenuOpen = false; navController.navigate(Screen.Match.route) }
                                                    )
                                                    DrawerRow(
                                                        label = stringResource(R.string.nav_forecast),
                                                        icon = Icons.Default.TrendingUp,
                                                        onClick = { isMenuOpen = false; navController.navigate(Screen.Forecast.route) }
                                                    )
                                                    DrawerRow(
                                                        label = stringResource(R.string.title_match_history),
                                                        icon = Icons.Default.History,
                                                        onClick = { isMenuOpen = false; navController.navigate("match_history") }
                                                    )
                                                    DrawerRow(
                                                        label = stringResource(R.string.drawer_item_astrologers_directory),
                                                        icon = Icons.Default.Face,
                                                        onClick = { isMenuOpen = false; navController.navigate(Screen.Astrologers.route) }
                                                    )

                                                    DrawerSectionLabel(stringResource(R.string.drawer_section_knowledge_library))
                                                    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            KnowledgeChip(
                                                                label = stringResource(R.string.drawer_item_knowledge_hub),
                                                                icon = Icons.Default.List,
                                                                onClick = { isMenuOpen = false; navController.navigate(Screen.Blogs.route) },
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            KnowledgeChip(
                                                                label = stringResource(R.string.drawer_item_rashis),
                                                                icon = Icons.Default.Stars,
                                                                onClick = { isMenuOpen = false; navController.navigate("rashis") },
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }
                                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            KnowledgeChip(
                                                                label = stringResource(R.string.title_planets),
                                                                icon = Icons.Default.NightsStay,
                                                                onClick = { isMenuOpen = false; navController.navigate("planets") },
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            KnowledgeChip(
                                                                label = stringResource(R.string.title_nakshatras),
                                                                icon = Icons.Default.AutoAwesome,
                                                                onClick = { isMenuOpen = false; navController.navigate("nakshatras") },
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }
                                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            KnowledgeChip(
                                                                label = stringResource(R.string.title_houses),
                                                                icon = Icons.Default.Home,
                                                                onClick = { isMenuOpen = false; navController.navigate("houses") },
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            KnowledgeChip(
                                                                label = stringResource(R.string.title_yogas),
                                                                icon = Icons.Default.Eco,
                                                                onClick = { isMenuOpen = false; navController.navigate("yogas") },
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }
                                                    }

                                                    DrawerSectionLabel(stringResource(R.string.drawer_section_account))
                                                    DrawerRow(
                                                        label = stringResource(R.string.drawer_item_plans_credits),
                                                        icon = Icons.Default.AccountBalanceWallet,
                                                        onClick = { isMenuOpen = false; navController.navigate(Screen.Plans.route) }
                                                    )
                                                    DrawerRow(
                                                        label = "API Test Page",
                                                        icon = Icons.Default.BugReport,
                                                        onClick = { isMenuOpen = false; navController.navigate("test_page") }
                                                    )
                                                    val isDark = when (themePreference) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
                                                    DrawerRow(
                                                        label = if (isDark) stringResource(R.string.menu_light_mode) else stringResource(R.string.menu_dark_mode),
                                                        icon = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                        onClick = { scope.launch { sessionManager.setThemePreference(if (isDark) "light" else "dark") } },
                                                        trailing = {
                                                            Switch(
                                                                checked = isDark,
                                                                onCheckedChange = { scope.launch { sessionManager.setThemePreference(if (isDark) "light" else "dark") } },
                                                                modifier = Modifier.scale(0.8f)
                                                            )
                                                        }
                                                    )
                                                    DrawerRow(
                                                        label = stringResource(R.string.menu_logout),
                                                        icon = Icons.Default.Logout,
                                                        onClick = { isMenuOpen = false; showLogoutDialog = true },
                                                        contentColor = Color(0xFFEF4444).copy(alpha = 0.85f),
                                                        trailing = {}
                                                    )

                                                    Spacer(modifier = Modifier.height(24.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
