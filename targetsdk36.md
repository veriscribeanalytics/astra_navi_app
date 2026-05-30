# AstraNavi — `targetSdk = 36` (Android 16) Migration Plan

**Deadline:** All new Play Store submissions and updates must target API 36 after **August 31, 2026**. Aim to finish this migration with at least two weeks of buffer.

**App profile:** single-activity Compose, network-only, no background work, no native code, no restricted permissions. Migration risk is narrow.

---

## 0. Snapshot of current state

| Setting | Current | Target |
|---|---|---|
| `compileSdk` | 35 | 36 |
| `targetSdk` | 35 | 36 |
| `minSdk` | 24 | 24 (unchanged) |
| Compose BOM | `2024.10.01` | latest stable (`2026.04.01` or newer) |
| AGP | 9.1.0 | check for newer at migration time |
| Kotlin | 2.2.10 | check for newer at migration time |
| AppCompat | 1.7.0 | 1.8.x+ |
| Retrofit | 2.11.0 | unchanged |
| OkHttp | 4.12.0 | unchanged |
| Coil | 2.7.0 | unchanged (Coil 3 is optional, separate effort) |

What's already compliant (no work needed):
- `enableEdgeToEdge()` called in `MainActivity.onCreate` — `MainActivity.kt:386`
- `WindowCompat.setDecorFitsSystemWindows(window, false)` in `Theme.kt:111`
- `android:enableOnBackInvokedCallback="true"` — `AndroidManifest.xml:11`
- `BackHandler` from Compose used in all back-intercepting screens
- Transparent status/nav in `values/themes.xml` and `values-night/themes.xml`
- Only `INTERNET` permission — none of the API-36-tightened permissions
- No foreground service / WorkManager / JobScheduler / AlarmManager / BroadcastReceiver / ContentProvider / WebView
- No native `.so` files — 16 KB page-size requirement is N/A
- No `screenOrientation` / `minAspectRatio` / `maxAspectRatio` lock in manifest

---

## 1. API 36 behavior changes — applied to AstraNavi

### 1.1 Zero risk (already handled by app architecture)
- Edge-to-edge enforcement
- Predictive back (`onBackPressed` no longer called; `KEYCODE_BACK` not dispatched)
- 16 KB page-size requirement for native libs
- JobScheduler quota tightening + `STOP_REASON_TIMEOUT_ABANDONED`
- Ordered-broadcast same-process restriction
- Intent-redirection hardening
- `setImportantWhileForeground` becomes no-op

### 1.2 Low risk — verify visually
- **Elegant text height forced on** for Tamil, Telugu, Kannada, Malayalam, Gujarati (plus Arabic, Lao, Myanmar, Odia, Thai). All five of those are shipped languages. Line heights will increase. Expected: cosmetic; possible: clipped cards or wrapped buttons in dense layouts.
- **AppCompat 1.7.0**: works on API 36, but 1.8.x has API 36 inset fixes. Bump as a hygiene step.

### 1.3 Medium risk — needs a tablet/foldable pass
- **Large-screen forced resizability (`sw ≥ 600dp`)**: `screenOrientation`, `setRequestedOrientation()`, `resizeableActivity="false"`, `minAspectRatio`, `maxAspectRatio` are ignored on tablets, foldable inner displays, ChromeOS. AstraNavi doesn't restrict orientation, so nothing is rejected — but every screen now renders at arbitrary aspect ratios and in multi-window. Screens to walk: `DashboardScreen`, `KundliScreen`, `ChatScreen`, `MatchScreen`, `AvatarSelectionScreen`, `ProfileScreen`, `LoginScreen`.
- **Temporary opt-out:** `PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY` exists in API 36 but is removed in API 37 (Aug 2027 deadline). Don't depend on it.

---

## 2. Migration steps (in order)

### Step 1 — Dependency bumps (do this first, on `targetSdk = 35`)
Goal: get on a Compose BOM and AppCompat that know about API 36, *without* changing target yet.

Files to edit:
- `gradle/libs.versions.toml`
  - `composeBom = "2024.10.01"` → latest stable (likely `2026.04.01` or newer at migration time)
  - `appcompat = "1.7.0"` → `1.8.0`+
  - Resolve the `foundation = "1.11.1"` + `foundationLayout = "1.11.1"` manual pins: either drop the explicit versions and let BOM manage them, or align both to a single non-BOM source. Don't mix.
  - Check AGP and Kotlin for newer stable releases at the time of migration.

Validate:
- `./gradlew assembleDebug` clean build, no new warnings.
- App launches, walk a few screens, no visible regressions.

### Step 2 — Compile against API 36 (still target 35)
Goal: surface deprecation warnings and removed-API errors before changing runtime behavior.

Files to edit:
- `app/build.gradle.kts`
  - `compileSdk = 35` → `compileSdk = 36`
  - Leave `targetSdk = 35`

Validate:
- Clean build. Read every new deprecation warning.
- App still runs identically on a phone (API 33–35 emulator).

### Step 3 — Tablet/landscape audit on the production target
Goal: catch layout breakage before flipping the runtime target.

Run on:
- Pixel Tablet emulator (sw ≥ 600dp), portrait + landscape
- Pixel Fold emulator, folded ↔ unfolded transition
- Phone in landscape (still affected, less severe)

Walk every screen in the app. For each layout issue logged, decide:
- Cap component width (`Modifier.widthIn(max = …)`) — preferred
- Switch to two-column at sw ≥ 600dp
- Add scrolling where it's missing
- Last resort: temporary `PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY` opt-out (with a TODO to remove before API 37)

Fix the issues. Do not proceed to step 4 with unresolved tablet layout bugs.

### Step 4 — Locale visual pass
Goal: catch elegant-text-height overflow.

Switch device language to Tamil, then Telugu, then Hindi, then Korean. Walk:
- Login screen
- Dashboard hero + cards
- Kundli main + dasha rows
- Chat empty state
- Profile

Look for clipped lines, wrapped buttons, expanded card heights pushing content off-screen.

### Step 5 — Flip the runtime target
Files to edit:
- `app/build.gradle.kts`
  - `targetSdk = 35` → `targetSdk = 36`

Validate the full test matrix in section 3.

### Step 6 — Regression sweep
Re-walk every screen on:
- Phone API 36 (Pixel 9 emulator or device)
- Phone API 33 (oldest realistic device for your audience)
- Tablet API 36
- Foldable API 36

---

## 3. Full test matrix (before merging to `main`)

| Scenario | Device | Pass criteria |
|---|---|---|
| Cold launch + login | Phone API 36 | Auth flow works, no crashes |
| Predictive back swipe | Phone API 36 | Smooth preview animation, no hijacked back |
| 3-button nav back long-press | Phone API 36 | Preview of previous screen appears |
| Walk all screens | Phone API 36 | No layout regression vs API 35 baseline |
| Walk all screens | Tablet API 36 portrait | No stretched / clipped layouts |
| Walk all screens | Tablet API 36 landscape | No clipped / off-screen content |
| Fold ↔ unfold | Foldable API 36 | State preserved, no crash, layout reflows |
| Multi-window split | Tablet API 36 | App renders correctly when resized |
| Hindi locale | Phone | All UI in Hindi (relies on translation work being merged) |
| Tamil + Telugu locale | Phone | No elegant-text-height clipping |
| Korean + Bengali locale | Phone | Renders correctly |
| Dark theme + light theme | Phone + tablet | Status/nav icon contrast legible |
| Network failure | Phone | Error states render, retry works |
| Long chat session | Phone API 36 | History loads, avatar switch works, no jank |

---

## 4. Rollback plan

If post-launch crash/ANR rate spikes or a layout regression escapes to production:

1. Revert `compileSdk` and `targetSdk` to 35 in `app/build.gradle.kts`.
2. Keep dependency bumps (BOM, AppCompat) — they are independently safe.
3. Ship hotfix.
4. Re-investigate the regression in a branch before re-attempting the bump.

---

## 5. Out of scope for this migration

Explicitly **not** part of this work, log as follow-ups if wanted:
- Coil 2 → Coil 3 migration
- WorkManager adoption (no current need)
- Material 3 Expressive adoption
- Adopting `androidx.window` for foldable hinge handling beyond default reflow
- Switching theme parent from `Theme.AppCompat.DayNight.NoActionBar` to a Material 3 base

---

## 6. Sign-off checklist

- [ ] Translation regression fixes from `next steps.md` are merged
- [ ] Compose BOM + AppCompat bumped, clean build
- [ ] `compileSdk = 36` compiles without new errors
- [ ] Tablet/landscape audit complete, all issues resolved
- [ ] Locale visual pass complete
- [ ] `targetSdk = 36` flipped, full test matrix passes
- [ ] Internal testing track release validated for ≥ 3 days
- [ ] Rollback branch tagged
