package com.astranavi.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.astranavi.app.ui.components.LocalSetGlowColors
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import com.astranavi.app.ui.entitlement.EntitlementUiState

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("home", "Home", Icons.Default.Home)
    object Forecast : Screen("forecast", "Forecast", Icons.Default.TrendingUp)
    object Kundli : Screen("kundli", "Kundli", Icons.Default.Star)
    object Match : Screen("match", "Match", Icons.Default.Favorite)
    object Consult : Screen("consult", "Consult", Icons.Default.Info)
    object Blogs : Screen("blogs", "Knowledge", Icons.Default.List)
    object Rashis : Screen("rashis", "Zodiac", Icons.Default.PlayArrow)
    object Chat : Screen("chat", "AI Chat", Icons.Default.AutoAwesome)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Astrologers : Screen("astrologers", "Experts", Icons.Default.Face)
    object Plans : Screen("plans", "Plans", Icons.Default.AutoAwesome)
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
            contentDescription = screen.label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            screen.label,
            color = if (isSelected) activeColor else inactiveColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CosmicHeader(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "COSMIC NAVIGATION",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Light
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        Text(
            text = "AstraNavi",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Your celestial guide",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun CosmicProfile(
    userName: String,
    lagnaSign: String?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Good Evening ✦",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (lagnaSign != null) {
                    Text(
                        text = "Rising: $lagnaSign",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun CosmicNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    staggerDelay: Int = 0
) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(staggerDelay.toLong())
        alpha.animateTo(1f, animationSpec = tween(400))
    }

    val scale by animateFloatAsState(if (selected) 1.01f else 1.0f, label = "scale")
    val backgroundAlpha by animateFloatAsState(if (selected) 0.12f else 0f, label = "bgAlpha")
    val borderAlpha by animateFloatAsState(if (selected) 0.2f else 0f, label = "borderAlpha")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .graphicsLayer {
                this.alpha = alpha.value
                scaleX = scale
                scaleY = scale
            }
            .clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = backgroundAlpha) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha)
        ) else null
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.7f
                ),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.9f
                ),
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

val sessionManager = SessionManager(this)
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

                            val isLogin = currentDestination?.route == "login"
                            val isIntroOrSplash = currentDestination?.route == "intro" ||
                                currentDestination?.route?.substringBefore("?") == "logo_splash"
                            val showMainActivityTopBar = !isLogin && !isIntroOrSplash
                            val showBottomBar = currentDestination?.route in listOf(
                                Screen.Dashboard.route,
                                Screen.Blogs.route
                            )

                            val defaultTopBarTitle = remember(currentDestination) {
                                when (currentDestination?.route?.substringBefore("?")) {
                                    Screen.Dashboard.route -> "Home"
                                    Screen.Forecast.route -> "Forecast"
                                    Screen.Blogs.route -> "Knowledge"
                                    Screen.Consult.route -> "Consult"
                                    Screen.Chat.route -> "AI Chat"
                                    Screen.Astrologers.route -> "Experts"
                                    Screen.Profile.route -> "Profile"
                                    Screen.Kundli.route -> "Kundli"
                                    Screen.Match.route -> "Match"
                                    Screen.Rashis.route -> "Zodiac"
                                    "planets" -> "Planets"
                                    "nakshatras" -> "Nakshatras"
                                    "houses" -> "Houses"
                                    "yogas" -> "Yogas"
                                    "match_history" -> "Match History"
                                    else -> "AstraNavi"
                                }
                            }

                            val topBarTitle = dynamicTitle ?: defaultTopBarTitle
                            val currentBaseRoute = currentDestination?.route?.substringBefore("?")
                            val drawsBehindTopBar = currentBaseRoute in listOf(
                                Screen.Rashis.route,
                                "planets"
                            )

                            val isTopLevel = remember(currentDestination) {
                                currentDestination?.route in listOf(
                                    Screen.Dashboard.route,
                                    Screen.Blogs.route
                                )
                            }

                            if (showLogoutDialog) {
                                AlertDialog(
                                    onDismissRequest = { showLogoutDialog = false },
                                    title = { Text("Logout") },
                                    text = { Text("Are you sure you want to logout from AstraNavi?") },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                showLogoutDialog = false
                                                scope.launch {
                                                    sessionManager.clearSession()
                                                    navController.navigate("login") {
                                                        popUpTo(0) { inclusive = true }
                                                    }
                                                }
                                            }
                                        ) {
                                            Text("Logout")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showLogoutDialog = false }) {
                                            Text("Cancel")
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

                            var glowColors by remember { mutableStateOf<GlowColors?>(null) }
                            val setGlowColors = remember { { c: GlowColors? -> glowColors = c } }

                            val currentLanguage by sessionManager.userLanguage.collectAsState(initial = "en")
                            var avatarMenuExpanded by remember { mutableStateOf(false) }

                            Box(modifier = Modifier.fillMaxSize()) {
                                glowColors?.let { gc ->
                                    AnimatedAtmosphericGlow(
                                        accentColor = gc.accent,
                                        deepColor = gc.deep,
                                        radialColor = gc.radial,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Scaffold(
                                    containerColor = Color.Transparent,
                                    contentWindowInsets = WindowInsets(0.dp),
                                    topBar = {
                                        if (showMainActivityTopBar) {
                                            CenterAlignedTopAppBar(
                                                title = {
                                                    if (currentBaseRoute == Screen.Chat.route) {
                                                        val chatViewModel: ChatViewModel = viewModel(factory = sharedViewModelFactory)
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
                                                                    activeAvatar?.name ?: "AI Chat",
                                                                    fontWeight = FontWeight.Black,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                                Icon(
                                                                    Icons.Default.ArrowDropDown,
                                                                    contentDescription = "Switch avatar"
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
                                                        Text(
                                                            topBarTitle,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                    }
                                                },
                                                navigationIcon = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (!isTopLevel) {
                                                            IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                                                                Icon(
                                                                    Icons.Default.ArrowBack,
                                                                    contentDescription = "Back"
                                                                )
                                                            }
                                                        } else {
                                                            Spacer(Modifier.width(4.dp))
                                                        }
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
                                                },
                                                actions = {
                                                    if (showBottomBar) {
                                                        IconButton(onClick = {
                                                            isMenuOpen = true
                                                        }) {
                                                            Icon(
                                                                Icons.Default.Person,
                                                                contentDescription = "Open Menu"
                                                            )
                                                        }
                                                    }
                                                    if (!isLogin) {
                                                        val credits = if (entitlementState is EntitlementUiState.Success) (entitlementState as EntitlementUiState.Success).balance.credits else 0
                                                        val tier = if (entitlementState is EntitlementUiState.Success) (entitlementState as EntitlementUiState.Success).balance.tier else "free"
                                                        CreditBadge(credits = credits, tier = tier, onClick = { navController.navigate(Screen.Plans.route) })
                                                    }
                                                    if (currentDestination?.route == Screen.Consult.route) {
                                                        IconButton(onClick = { navController.navigate("consult_history") }) {
                                                            Icon(Icons.Default.History, contentDescription = "Consult History")
                                                        }
                                                    }
                                                    if (currentDestination?.route == Screen.Match.route) {
                                                        IconButton(onClick = { navController.navigate("match_history") }) {
                                                            Icon(Icons.Default.History, contentDescription = "Match History")
                                                        }
                                                    }
                                                    if (currentBaseRoute == Screen.Chat.route) {
                                                        val chatViewModel: ChatViewModel = viewModel(
                                                            factory = sharedViewModelFactory
                                                        )
                                                        val chatShowHistory by chatViewModel.showHistory
                                                        if (chatShowHistory) {
                                                            IconButton(onClick = { chatViewModel.setShowHistory(false) }) {
                                                                Icon(Icons.Default.Close, contentDescription = "Close History")
                                                            }
                                                        } else {
                                                            IconButton(onClick = { chatViewModel.toggleHistory() }) {
                                                                Icon(Icons.Default.History, contentDescription = "Chat History")
                                                            }
                                                            IconButton(onClick = {
                                                                navController.navigate("avatar_selection")
                                                            }) {
                                                                Icon(Icons.Default.AddComment, contentDescription = "New Chat")
                                                            }
                                                        }
                                                    }
                                                },
                                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                                    containerColor = Color.Transparent,
                                                    titleContentColor = dynamicTopBarColor ?: MaterialTheme.colorScheme.onSurface,
                                                    navigationIconContentColor = dynamicTopBarColor ?: MaterialTheme.colorScheme.onSurface,
                                                    actionIconContentColor = dynamicTopBarColor ?: MaterialTheme.colorScheme.onSurface
                                                ),
                                                windowInsets = WindowInsets.statusBars
                                            )
                                        }
                                    },
                                    bottomBar = {
                                        if (showBottomBar) {
                                            val activeGold = MaterialTheme.colorScheme.secondary
                                            val inactiveColor = Color.Gray
                                            val navBarInsets = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

                                            Box(
                                                modifier = Modifier.fillMaxWidth().wrapContentHeight(unbounded = true),
                                                contentAlignment = Alignment.BottomCenter
                                            ) {
                                                Surface(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RectangleShape,
                                                    color = MaterialTheme.colorScheme.background,
                                                    tonalElevation = 0.dp
                                                ) {
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

                                                Box(
                                                    modifier = Modifier.align(Alignment.TopCenter).size(64.dp).background(MaterialTheme.colorScheme.surface, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    FloatingActionButton(
                                                        onClick = {
                                                            navController.navigate("avatar_selection") {
                                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                                launchSingleTop = true
                                                                restoreState = true
                                                            }
                                                        },
                                                        shape = CircleShape,
                                                        containerColor = MaterialTheme.colorScheme.secondary,
                                                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                                                        modifier = Modifier.size(56.dp)
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                            Icon(Screen.Chat.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(30.dp))
                                                            Text("AI CHAT", color = MaterialTheme.colorScheme.onPrimary, fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
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
                                        LocalSetGlowColors provides setGlowColors
                                    ) {
                                        NavHost(
                                            navController = navController,
                                            startDestination = startDestination!!,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(
                                                    top = if (drawsBehindTopBar) 0.dp else innerPadding.calculateTopPadding(),
                                                    bottom = innerPadding.calculateBottomPadding()
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
                                                val chatViewModel: ChatViewModel = viewModel(factory = sharedViewModelFactory)
                                                val chatShowHistory by chatViewModel.showHistory
                                                LaunchedEffect(chatShowHistory) { dynamicTitle = if (chatShowHistory) "Chat History" else null }
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
                                                ProfileScreen(viewModel = profileViewModel, onBack = { navController.popBackStack() }, onProfileComplete = { navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Profile.route) { inclusive = true } } }, onAccountDeleted = { navController.navigate("login") { popUpTo(0) { inclusive = true } } })
                                            }
                                            composable(Screen.Plans.route) {
                                                val plansViewModel: PlansViewModel = viewModel(factory = sharedViewModelFactory)
                                                PlansPage(viewModel = plansViewModel, onBack = { navController.popBackStack() })
                                            }
                                        }
                                    }
                                }

                                AnimatedVisibility(visible = isMenuOpen, enter = fadeIn(tween(200)), exit = fadeOut(tween(200))) {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { isMenuOpen = false })
                                }

                                if (activePaywall != null) {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { entitlementViewModel.dismissPaywall() })
                                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                                        PaywallFullBlock(paywall = activePaywall!!)
                                    }
                                }

                                AnimatedVisibility(visible = isMenuOpen, enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)), exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250, easing = FastOutSlowInEasing)), modifier = Modifier.align(Alignment.CenterEnd)) {
                                    Surface(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.85f), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), shadowElevation = 24.dp, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            ParticleBackground()
                                            Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).navigationBarsPadding()) {
                                                CosmicHeader(onClose = { isMenuOpen = false })
                                                CosmicProfile(userName = userName ?: "Astra User", lagnaSign = lagnaSign, onClick = { isMenuOpen = false; navController.navigate(Screen.Profile.route) })
                                                Spacer(modifier = Modifier.height(16.dp))
                                                val menuItems = listOf(Screen.Dashboard, Screen.Blogs, Screen.Chat, Screen.Consult, Screen.Match)
                                                menuItems.forEachIndexed { index, screen -> CosmicNavItem(label = screen.label, icon = screen.icon, selected = currentDestination?.route == screen.route, onClick = { isMenuOpen = false; navController.navigate(screen.route) }, staggerDelay = 100 + (index * 40)) }
                                                Spacer(modifier = Modifier.weight(1f))
                                                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                                    Text(text = "PREFERENCES", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                                                    val isDark = when (themePreference) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
                                                    CosmicNavItem(label = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode", icon = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode, selected = false, onClick = { scope.launch { sessionManager.setThemePreference(if (isDark) "light" else "dark") } }, staggerDelay = 400)
                                                    CosmicNavItem(label = "Logout", icon = Icons.Default.Logout, selected = false, onClick = { isMenuOpen = false; showLogoutDialog = true }, staggerDelay = 450)
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
}
