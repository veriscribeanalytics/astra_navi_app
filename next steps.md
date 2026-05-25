# AstraNavi — Immediate Translation Regression Fixes

## Context

`values-hi/strings.xml`, `values-ko/strings.xml`, `values-bn/strings.xml` etc. all have ~430 keys translated to good quality. `values/strings.xml` (default English) has 430 keys. Coverage is **not** the problem.

The problem is that many UI sites in the codebase use **hardcoded English string literals** instead of `stringResource(R.string.<key>)`. The matching keys already exist (and are already translated to all 11 languages). The UI just bypasses the resource lookup entirely, so language switching has no effect on those labels.

This audit covered **only `MainActivity.kt` and `DashboardScreen.kt`**. A full scan is required across every screen.

---

## A — `MainActivity.kt`: top app bar, bottom nav, cosmic drawer

### A1. Bottom nav labels — Screen sealed class

`MainActivity.kt:112-122` — all `Screen` objects hardcode their `label: String`:

```kotlin
object Dashboard : Screen("home", "Home", Icons.Default.Home)
object Forecast  : Screen("forecast", "Forecast", Icons.Default.TrendingUp)
object Kundli    : Screen("kundli", "Kundli", Icons.Default.Star)
object Match     : Screen("match", "Match", Icons.Default.Favorite)
object Consult   : Screen("consult", "Consult", Icons.Default.Info)
object Blogs     : Screen("blogs", "Knowledge", Icons.Default.List)
object Rashis    : Screen("rashis", "Zodiac", Icons.Default.PlayArrow)
object Chat      : Screen("chat", "AI Chat", Icons.Default.AutoAwesome)
object Profile   : Screen("profile", "Profile", Icons.Default.Person)
object Astrologers : Screen("astrologers", "Experts", Icons.Default.Face)
object Plans     : Screen("plans", "Plans", Icons.Default.AutoAwesome)
```

These labels are used:
- In `NavBarItem` → `Text(screen.label, ...)` (bottom nav)
- In `CosmicNavItem` inside the drawer → `label = screen.label`

Resource keys already exist: `nav_home`, `nav_forecast`, `nav_kundli`, `nav_match`, `nav_consult`, `nav_blogs`, `nav_rashis`, `nav_chat`, `nav_profile`, `nav_astrologers`, `nav_plans` — all translated in every `values-XX/`.

**Fix:** change `Screen` to take `@StringRes val labelRes: Int` instead of `val label: String`. Update `NavBarItem` and `CosmicNavItem` to call `stringResource(screen.labelRes)`.

### A2. Top app bar default title — `defaultTopBarTitle` when-block

`MainActivity.kt:460-476`:

```kotlin
when (currentDestination?.route?.substringBefore("?")) {
    Screen.Dashboard.route   -> "Home"
    Screen.Forecast.route    -> "Forecast"
    Screen.Blogs.route       -> "Knowledge"
    Screen.Consult.route     -> "Consult"
    Screen.Chat.route        -> "AI Chat"
    Screen.Astrologers.route -> "Experts"
    Screen.Profile.route     -> "Profile"
    Screen.Kundli.route      -> "Kundli"
    Screen.Match.route       -> "Match"
    Screen.Rashis.route      -> "Zodiac"
    "planets"        -> "Planets"
    "nakshatras"     -> "Nakshatras"
    "houses"         -> "Houses"
    "yogas"          -> "Yogas"
    "match_history"  -> "Match History"
    else -> "AstraNavi"
}
```

Every branch returns a hardcoded string. After fix A1, the `Screen.X.route` branches can be replaced with `stringResource(screen.labelRes)`. The string branches need new keys:

| Hardcoded | Needs key (some may exist) |
|---|---|
| `"Planets"` | `screen_planets` |
| `"Nakshatras"` | `screen_nakshatras` |
| `"Houses"` | `screen_houses` |
| `"Yogas"` | `screen_yogas` |
| `"Match History"` | `screen_match_history` |
| `"AstraNavi"` (fallback) | `app_name` |

### A3. Logout dialog — all four texts hardcoded

`MainActivity.kt:497-516`:

| Line | Hardcoded | Existing key |
|---|---|---|
| 497 | `Text("Logout")` (title) | `R.string.logout_title` |
| 498 | `Text("Are you sure you want to logout from AstraNavi?")` | `R.string.logout_message` |
| 511 | `Text("Logout")` (confirm button) | `R.string.logout_confirm` |
| 516 | `Text("Cancel")` (dismiss button) | `R.string.logout_cancel` |

All four keys exist with full translations. Trivial fix.

### A4. Chat avatar dropdown fallback

`MainActivity.kt:576`:
```kotlin
activeAvatar?.name ?: "AI Chat"
```
Hardcoded fallback. → `stringResource(R.string.nav_chat)`.

### A5. Dynamic title for chat history

`MainActivity.kt:1009`:
```kotlin
LaunchedEffect(chatShowHistory) { dynamicTitle = if (chatShowHistory) "Chat History" else null }
```
Need new key like `chat_history_title`. The `dynamicTitle` is a plain `String?` so this needs the value passed in instead of an ID — either resolve via context at assignment, or convert `dynamicTitle` to `@StringRes Int?`.

### A6. FAB label

`MainActivity.kt:793`:
```kotlin
Text("AI CHAT", color = MaterialTheme.colorScheme.onPrimary, fontSize = 7.sp, ...)
```
Center chat FAB caption. Could reuse `R.string.nav_chat` (uppercased visually) or add `nav_chat_short`.

### A7. Top app bar — content descriptions (accessibility)

All hardcoded in `MainActivity.kt`:

| Line | Hardcoded `contentDescription` |
|---|---|
| 233 | `"Close"` (CosmicHeader close button) |
| 583 | `"Switch avatar"` |
| 636 | `"Back"` |
| 660 | `"Open Menu"` |
| 671 | `"Consult History"` |
| 676 | `"Match History"` |
| 686 | `"Close History"` |
| 690 | `"Chat History"` |
| 695 | `"New Chat"` |

Less critical (only screen readers consume them) but should still go through `stringResource`. May need new keys: `cd_close`, `cd_switch_avatar`, `cd_back`, `cd_open_menu`, etc.

### A8. Cosmic drawer

| Line | Hardcoded | Existing key (or new) |
|---|---|---|
| 223 | `text = "COSMIC NAVIGATION"` | `R.string.drawer_cosmic_navigation` ✓ exists |
| 239 | `text = "AstraNavi"` | need `R.string.app_name` |
| 247 | `text = "Your celestial guide"` | `R.string.drawer_celestial_guide` ✓ exists |
| 1067 | `userName ?: "Astra User"` | need `R.string.user_default_name` |
| 1073 | `text = "PREFERENCES"` | need `R.string.drawer_preferences` |
| 1076 | `CosmicNavItem(label = "Logout", ...)` | `R.string.menu_logout` ✓ exists |

Three keys already exist and aren't being used. Three need to be created (translations follow once keys are added).

---

## B — `DashboardScreen.kt`

### B1. Visible labels (9 sites, all have existing translated keys)

| Line | Hardcoded literal | Existing key |
|---|---|---|
| 1481 | `"COSMIC STREAK"` | `dashboard_label_cosmic_streak` |
| 1514 | `"YOUR COSMIC STREAK"` | `dashboard_label_your_cosmic_streak` |
| 1584 | `"Retry Connection"` | `dashboard_btn_retry_connection` |
| 1717 | `"Mood: ..."` + `"Balanced"` fallback | `dashboard_label_mood_title` + `dashboard_status_balanced` |
| 2127 | `label = "Mahadasha"` | `dashboard_label_mahadasha` |
| 2137 | `label = "Antardasha"` | `dashboard_label_antardasha` |
| 2157 | `"EXPLORE FULL ANALYSIS"` | `dashboard_label_explore_full_analysis` |
| 2290 | `"LAST CONSULTATION"` | `dashboard_label_last_consultation` |
| 2304 | `"ASK AGAIN"` | `dashboard_label_ask_again` |

All Hindi/Korean/Bengali/etc. translations already present in `values-XX/strings.xml`.

### B2. Content descriptions

| Line | Hardcoded | Existing key |
|---|---|---|
| 1312 | `contentDescription = "View all"` | `dashboard_label_view_all` |
| 2045 | `contentDescription = "Vedic Birth Chart"` | `dashboard_label_vedic_birth_chart` |
| 2062 | `contentDescription = "Chart loading failed"` | `dashboard_label_chart_failed` |
| 2075 | `contentDescription = "Sign in to view chart"` | `dashboard_label_sign_in_chart` |

### B3. Fragile backend-data string matching

`DashboardScreen.kt:855-856`:
```kotlin
?.replace("Mahadasha", "MD", ignoreCase = true)
?.replace("Antardasha", "AD", ignoreCase = true)
```

This abbreviates backend-returned text by string-matching the **English word**. Once backend returns "महादशा" (Hindi), the `.replace()` silently does nothing — full Hindi word displays instead of "MD". Same problem for every non-English language.

**Fix:** don't string-match backend text. Detect dasha type from a structured field (the API surely has one) and assemble the abbreviation client-side from existing `R.string.dashboard_label_mahadasha` / `dashboard_label_antardasha` with a short-form key (`dashboard_label_mahadasha_short`, etc.).

---

## C — Full app scan required

This audit only covered `MainActivity.kt` and `DashboardScreen.kt`. The same hardcoded-string-bypass pattern almost certainly exists in:

- `ForecastScreen.kt`
- `KundliScreen.kt`
- `MatchScreen.kt`, `MatchHistoryScreen.kt`
- `ConsultScreen.kt`, `ConsultHistoryScreen.kt`
- `ChatScreen.kt`, `AvatarSelectionScreen.kt`, `AstrologersScreen.kt`
- `KnowledgeHubScreen.kt`, `PlanetScreen.kt`, `NakshatraScreen.kt`, `HouseScreen.kt`, `YogaScreen.kt`, `RashiScreen.kt`
- `ProfileScreen.kt`, `LoginScreen.kt`
- `PlansPage.kt`
- All composables under `ui/components/` that render text (`PaywallCard`, `LockedSectionCard`, `KnowledgeUIUtils`, `LocationSearchField`, etc.)

**Scan strategy** (one Grep pass per file is enough):

```
grep -E 'Text\(\s*"[A-Z]|contentDescription\s*=\s*"[A-Z]' ui/<screen>.kt
```

Any hit returning a non-`stringResource` literal is a regression site. Cross-reference each against `values/strings.xml` — if a matching key exists, just swap the literal for `stringResource(R.string.<key>)`. If no key exists, add the key + ensure all 11 `values-XX/` files get a translation.

---

## D — Order of operations recommended

1. Fix the Screen sealed class (A1) — refactor `label: String` → `@StringRes labelRes: Int`. This single change resolves bottom nav, cosmic drawer nav items, and most of the top app bar default title in one stroke.
2. Apply A3 logout dialog, A4 dropdown fallback, A8 drawer literals where keys already exist (no new keys needed).
3. Apply B1 dashboard visible labels (9 swaps, all keys exist).
4. Add keys for items in A2/A5/A6/A7/A8/B2 that don't have one yet — create in `values/strings.xml` first, then `values-hi/`, then propagate to the other 10 language files in one batch.
5. B3 dasha abbreviation refactor.
6. Full app scan (Section C) to find the remaining hidden regressions.

## E — Verification

After every batch of edits, manually verify the fix by:

1. Open the app, switch language to Hindi.
2. Walk through every screen affected by the batch.
3. Confirm every label is in Hindi (no English mixed in).
4. Repeat for Bengali, Korean (good representative samples for non-Devanagari + non-Latin).

If a label stays English after the batch, that label is hardcoded somewhere we missed — re-grep.
