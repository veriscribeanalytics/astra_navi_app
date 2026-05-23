# AstraNavi: Future Tech Debt, Permissions & Fallbacks

Last audited: 2026-05-21

This document tracks production-readiness work for Android permissions, privacy, security, weak networks, offline fallback, streaming, billing, device fragmentation, and Compose performance. It is a planning document. Items here should be implemented deliberately and tested on real devices before release.

## Completed (2026-05-21)

- Pull-to-refresh now bypasses the on-device cache on Dashboard and Forecast (Kundli already did). `forceRefresh` is threaded through repositories to `bypassRead` / `force_refresh`.
- `ForecastViewModel` no longer surfaces raw `response.message()` HTTP text. Errors route through `ErrorSanitizer`, with a new `sanitizeHttpCode()` code-to-message map.
- Forecast renders partial sections: a failed weekly/monthly/yearly period no longer fails the whole screen, and each tab has its own retry.
- `CancellationException` is rethrown (not swallowed) in the Dashboard, Forecast, and Kundli ViewModels.
- The OkHttp token authenticator is bounded to 2 attempts to prevent infinite 401 refresh loops.
- Network security config in place: `res/xml/network_security_config.xml` denies cleartext traffic in release; debug overrides allow user CA trust for local proxies. Manifest also sets `android:usesCleartextTraffic="false"`.
- `FLAG_SECURE` applied to sensitive screens (Profile, Chat, Consult, Kundli, Match, Match history, Consult history, Plans) via a shared `SecureScreen()` composable. Screenshots and Recents previews are now blocked on these surfaces.
- Cache metadata (`cachedAtMillis`, `expiresAtMillis`, `fromCache`) flows through `ApiResponseCache.getOrFetch` via an optional `onMeta` callback. Dashboard, Forecast, and Kundli combine the per-section meta and show a small "Updated X ago" / "Showing cached guidance" label via the new `CacheFreshnessLabel` composable.
- Stable list keys added where missing (`RashiScreen`, family-member row in `DashboardSections`); other lazy lists already had keys.
- Content descriptions: the one icon-only `IconButton` with `contentDescription = null` (consult-history expand toggle) now has an accessible label. Other `contentDescription = null` occurrences are decorative and intentional.

## 0. Current App Snapshot

Current manifest and dependency state:
- Declared permissions: `INTERNET` only.
- No runtime permissions are currently declared for notifications, microphone, camera, storage/media, contacts, location, SMS, phone, Bluetooth, calendar, exact alarms, foreground services, biometrics, or boot receivers.
- `android:allowBackup="false"` and `android:fullBackupContent="false"` are set.
- No `networkSecurityConfig` is declared.
- No Google Play Billing, Firebase/FCM, WorkManager, Paging, Room, Biometric, or AndroidX Security Crypto dependency is present.

Current local audit:
- `catch (e: Exception)`: 44 instances.
- Lazy layouts: 15 `LazyColumn`, 7 `LazyRow`, 7 `LazyVerticalGrid`, 3 `HorizontalPager`.
- `LaunchedEffect(Unit)`: 19 instances.
- Streaming `byteStream()`: 2 instances, in chat and consult streaming.
- API key is injected from `local.properties` into `BuildConfig`. `local.properties` is gitignored, but client API keys are not secrets once shipped.
- Profile place-of-birth search is server-backed manual entry; no Android location permission is used, which is the right privacy default.

## 1. Permission Strategy

Global rule:
- Ask for dangerous permissions only in context, when the user taps a feature that needs the permission.
- Never request all future permissions on app launch.
- Every permission flow must have a "Not now" path and a degraded fallback.
- If permission is denied, revoked, or temporarily unavailable, keep the app usable and hide or degrade only the dependent feature.
- Keep a single `PermissionGate`/`PermissionCoordinator` layer so screens do not duplicate permission logic.
- Do not persist permission-granted state in DataStore; always check the platform state at the time of use.

Current status:
- The app has no dangerous permissions today. Do not add permissions until the matching feature is actually implemented.

### Permission Matrix

| Capability | Permission | When to ask | Fallback if denied |
|---|---|---|---|
| Daily reminders / push notifications | `POST_NOTIFICATIONS` on Android 13+ | Only after user enables daily horoscope reminders or notification settings | Keep dashboard refresh, in-app banners, and manual open reminders |
| Voice input for Navi/chat/consult | `RECORD_AUDIO` | When user taps a microphone button | Text input remains primary; hide mic/waveform UI |
| Background voice capture, if ever approved | `RECORD_AUDIO`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MICROPHONE` | Avoid unless the feature is explicitly user-visible and active | Stop recording when app backgrounds; continue only through a visible foreground service after consent |
| Profile photo or media upload | Prefer Android Photo Picker, no broad storage permission | User taps upload/select image | Default avatar, retry picker, or document picker fallback |
| Custom gallery picker | Android 13+: `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO`; Android 14+: also handle `READ_MEDIA_VISUAL_USER_SELECTED`; older: `READ_EXTERNAL_STORAGE` with max SDK as needed | Only if Photo Picker cannot satisfy the UX | Photo Picker, document picker, default avatar |
| Export/share generated chart/report file | Usually no storage permission with share sheet, `MediaStore`, or app-private cache | User taps Share/Export | Copy text/share link; store only app-private/cache file |
| Camera capture for avatar/document scan | `CAMERA` | User taps camera capture | Photo Picker/manual upload/default avatar |
| Current city/location, if added | Prefer manual city search. If needed, `ACCESS_COARSE_LOCATION`; only use `ACCESS_FINE_LOCATION` if proven necessary | User taps "Use current location" | Manual city/place entry remains default |
| Biometrics for unlock, if added | `USE_BIOMETRIC` plus `BiometricManager` checks | User enables biometric unlock | Email/password login remains available |
| Exact daily notification time | Prefer no exact-alarm permission | Only if user explicitly chooses exact-minute reminders | WorkManager/flexible reminder window |
| Exact alarms, if truly required | `SCHEDULE_EXACT_ALARM` special app access or narrowly eligible `USE_EXACT_ALARM` | Dedicated settings explanation, not app launch | Flexible notification timing; dashboard refresh on open |
| Contacts/SMS/Phone/Calendar | Avoid for current product | Only after a specific approved feature | Share sheet/manual input |
| Bluetooth/Nearby devices | Avoid for current product | Only for approved wearable/device integration | Feature hidden or manually refreshed |

### Notification Permission

Future work:
- Add `POST_NOTIFICATIONS` only when notification features exist.
- Ask after the user opts into daily horoscope reminders, not during onboarding.
- Support granted, denied, and not-yet-asked states.
- If denied, show non-blocking in-app nudges and a settings deep link only after user interaction.
- Test fresh install, upgrade, deny, allow, swipe-away, backup/restore, and notification-channel states on Android 13+.

Fallback:
- Dashboard and Forecast always work from cache/API when opened, so notifications are convenience only.
- If notifications are denied, no core feature should fail.

### Storage, Media, and Profile Images

Future work:
- Prefer Android Photo Picker for avatars, attachment upload, and media selection. It lets users grant access to selected media instead of the whole library.
- Do not request broad storage access for avatar selection.
- If a custom gallery UI is added, implement Android 13 granular media permissions and Android 14 selected-media re-selection.
- Treat media URI access as temporary unless a long upload explicitly needs persisted URI access.
- Store temporary exports in cache/app-private files and clean them up.

Fallback:
- Use a default avatar/logo if the user cancels media selection or permission is denied.
- If Photo Picker is unavailable, use AndroidX Activity contracts so the library can fall back to `ACTION_OPEN_DOCUMENT` where supported.

### Microphone and Voice Input

Future work:
- Add `RECORD_AUDIO` only when voice input is implemented.
- Request permission only when the user taps the mic button.
- Show clear recording state, stop button, timeout, and error state.
- Stop recording on lifecycle pause unless the product explicitly supports background recording.
- Do not start microphone foreground services from the background or boot. Android 14+ enforces while-in-use permission restrictions for foreground services using microphone/camera/location.

Fallback:
- Text chat and typed consult questions remain the primary path.
- If denied, hide mic UI or show a one-tap "Enable microphone" affordance.

### Camera

Future work:
- Add `CAMERA` only for a real capture flow.
- Prefer Photo Picker for avatar selection unless in-app capture is a real requirement.
- Handle camera hardware missing, camera app unavailable, permission denied, and activity cancellation.

Fallback:
- Let the user pick an existing image or use the default avatar.

### Location

Current status:
- No Android location permission is used.
- Place of birth is typed/search-backed, which is better for privacy and usually better for astrology accuracy than current GPS.

Future work:
- Keep manual POB/city entry as the default.
- If "current city" is added for panchang/current transits, ask only after the user taps that action.
- Approximate location should be sufficient unless the feature proves it needs precise location.

Fallback:
- Manual city/place entry.

### Biometrics

Future work:
- Check `BiometricManager` before showing biometric unlock.
- Treat biometrics as convenience unlock, not account recovery.
- Fall back to email/password when hardware is missing, enrollment is absent, lockout occurs, or auth fails.

### Special App Access: Exact Alarms and Battery

Future work:
- Avoid `SCHEDULE_EXACT_ALARM` for normal daily horoscope reminders.
- Use WorkManager/flexible scheduling for non-critical reminders.
- If exact reminders are a paid/user-requested feature, explain why exact timing is needed and check `canScheduleExactAlarms()` before scheduling.
- If exact alarm access is revoked, cancel/reschedule gracefully.

Fallback:
- Fetch on dashboard open.
- Use a broad reminder window rather than exact-minute delivery.

## 2. Network & API Resilience

Current issues:
- 44 broad exception catches make it hard to distinguish timeout, offline, server error, parse error, auth failure, cancellation, and client bugs.
- Retrofit streaming endpoints return `ResponseBody` directly, so HTTP status, headers, timeout policy, and 402 paywall detection are harder to centralize.

Future work:
- Centralize API results into a sealed result model:
  - `Success`
  - `HttpError(code, safeMessage)`
  - `NetworkUnavailable`
  - `Timeout`
  - `AuthExpired`
  - `PaywallRequired(paywall)`
  - `ParseError`
  - `Cancelled`
  - `Unknown`
- Catch `CancellationException` separately and rethrow or map intentionally; do not swallow coroutine cancellation in broad `Exception` catches. (Done for Dashboard/Forecast/Kundli ViewModels; remaining ViewModels still pending.)
- Catch specific exceptions first: `UnknownHostException`, `SocketTimeoutException`, `ConnectException`, `SSLException`, `SerializationException`/JSON parse failures, `CancellationException`.
- Never show raw exception messages, API URLs, hostnames, stack traces, JWTs, request bodies, or backend payloads in UI.
- Standardize retry UI with exponential backoff for retryable reads.
- Keep histories network-fresh unless deliberate pagination/caching is added.

Fallback:
- Profile: short TTL cache.
- Daily horoscope: fixed hourly boundary cache.
- Forecast: once per local day, invalidated when birth data changes.
- Kundli: once per local day for now; eventual better policy is cache forever and invalidate on birth data/chart-context change.
- Chat, consult, match histories: network-first to avoid frontend/backend mismatch.

## 3. Streaming Chat and Consult Fallbacks

Current issues:
- Chat and consult use `ResponseBody.byteStream()` directly.
- Streaming failures can leave partial messages, stuck loading states, or duplicated user messages.
- SSE parse errors are swallowed.
- Chat retry uses a new local user message each time and has no idempotency/request ID.
- Consult stream updates UI through nested `launch(Dispatchers.Main)` calls inside an IO block, which can race with completion/error state.

Future work:
- Treat stream cancellation as a first-class state.
- Always close response bodies. Current `.use` on the buffered reader helps for stream reads, but JSON/error branches that call `string()` should also remain audited.
- Add request IDs or local message IDs so retry does not duplicate messages or double-charge credits.
- Persist partial AI output separately from confirmed server messages.
- Detect malformed SSE chunks and continue when possible, but record sanitized telemetry.
- Add stream timeout and idle timeout.
- Show "response interrupted, retry" when stream ends unexpectedly.
- Parse streaming endpoints through a shared SSE parser used by chat and consult.

Fallback:
- Keep the user's typed prompt in the input/retry state.
- If stream fails after partial content, allow copy/retry and label it as incomplete.
- If paywall is returned mid-flow, preserve the draft prompt and route to Plans without losing context.

## 4. Image Loading Fallbacks

Moved to `next steps.md` under `P0: Frontend Fallback Readiness`.

## 5. Lazy Loading, Large Lists, and Compose Performance

Current issues:
- 15 `LazyColumn`, 7 `LazyRow`, 7 `LazyVerticalGrid`, and 3 `HorizontalPager` usages exist.
- Some history/chat lists can grow without true pagination.
- Many animations are infinite or staged via `LaunchedEffect(Unit)`.

Future work:
- Use Paging 3 for chat history, consult history, and match history if lists become large.
- Use realistic placeholder heights; avoid zero-size placeholders that cause lazy layouts to over-compose.
- Move expensive sorting/filtering/mapping out of composables or wrap with `remember(...)`.
- Use `derivedStateOf` for scroll-derived UI state.
- Save scroll state where users expect continuity.
- Audit `HorizontalPager` pages for heavy child composition; avoid loading every heavy section eagerly.
- Review `LaunchedEffect(Unit)` fetches so navigation/recomposition does not accidentally refetch.
- Add reduced-motion or battery-saver behavior for particle effects, orbit animations, shimmer, and infinite transitions.

Fallback:
- If a list fetch is slow, show skeleton rows with fixed size.
- If pagination fails, keep already loaded pages visible and show retry at list bottom.

## 6. Background Work and OEM Battery Behavior

Current status:
- No WorkManager, AlarmManager, boot receiver, notification scheduler, or FCM dependency exists.

Future work:
- Use WorkManager for flexible reminders/sync.
- Do not depend on background work for correctness; dashboard open must always validate current cache/API state.
- Avoid wake locks unless a strict use case exists.
- If OEM battery optimizations break reminders, show a settings hint only after the user enables reminders and misses expected notifications.
- Do not ask users to disable battery optimization during onboarding.

Fallback:
- In-app reminders and dashboard refresh on open.
- Server-side push, if available, as a complement rather than the only trigger.

## 7. Google Play Services, Billing, and Non-GMS Devices

Current issues:
- Entitlement endpoints, paywall cards, credit balance, and Plans UI exist.
- No Billing Library dependency is present.
- No Play Services / Firebase dependency is present.

Future work:
- Integrate Google Play Billing Library for digital subscriptions and packs if distributing through Google Play.
- Add server-side purchase validation, entitlement reconciliation, refund/revoke handling, and Real-time Developer Notifications.
- Keep backend product catalog as the source of display metadata, but map it to Play product IDs and offers.
- Before adding FCM, Billing, Play Integrity, or Play Services Photo Picker backport behavior, check service availability.
- Hide or disable unavailable features on Huawei, Amazon Fire, custom ROMs, and devices without GMS.

Fallback:
- Manual refresh, purchase unavailable message, and non-blocking UI.
- Credit balance remains read-only if billing is unavailable.
- Existing free/entitled features should keep working even when Play services are missing.

## 8. Security, Privacy, and Sensitive Data

Current issues:
- DataStore stores access tokens, refresh tokens, profile fields, birth data, and API cache in plaintext app storage.
- Client API key in `BuildConfig` should be treated as an identifier, not a secret.
- No network security config explicitly documents cleartext policy or debug trust anchors.
- No `FLAG_SECURE`/screenshot policy exists for highly sensitive screens.

Future work:
- Move tokens to encrypted storage or no-backup storage if the threat model requires it.
- Avoid storing sensitive exports in external/shared storage.
- Add privacy policy, terms, medical/financial disclaimer language, and data deletion flow before release.

Fallback:
- On restore/transfer with stale or missing tokens, force re-login rather than silently using restored credentials.
- If cache decrypt/read fails, clear cache and refetch.
- If API key is missing, show offline/cache-safe messaging and avoid infinite loading spinners.

## 9. External Intents, Browser Links, Sharing, and Package Visibility

Current status:
- No external browser/share/payment intents are currently active in the app code.
- No `<queries>` or `QUERY_ALL_PACKAGES` is declared.

Future work:
- Wrap every external `startActivity(...)` in `try/catch ActivityNotFoundException`.
- Use explicit chooser/share intents for horoscope, match result, or chart exports.
- Add `<queries>` only for specific external interactions that require package visibility.
- Avoid `QUERY_ALL_PACKAGES`.
- For Privacy Policy, Terms, support email, payment pages, and maps/place links, validate URLs and offer "Copy link" fallback.

Fallback:
- Snackbar with copy-link action.
- Internal WebView only if security rules are defined; otherwise use browser intent.

## 10. Data Model and Parsing Robustness

Current status:
- `JsonConfig` uses tolerant kotlinx serialization settings: `ignoreUnknownKeys`, `isLenient`, `coerceInputValues`, and `explicitNulls=false`.
- `AnalyzeFullJsonAdapter` handles string-encoded JSON and locked premium section shapes.
- Several models already include alternate field names for backend compatibility.

Future work:
- Treat backend fields that can change shape as tolerant `JsonElement` or explicit versioned models.
- Add parse-error telemetry/logging without exposing raw payloads to UI.
- Add default values for optional fields used by UI.
- Do not assume arrays are non-empty.
- Keep API model tests using saved sample responses for login, profile, horoscope, forecast, kundli, chat, consult, match, entitlements, and paywalls.
- Add tests for locked premium responses and malformed/partial `analyze-full` responses.

Fallback:
- Show partial sections when one optional field fails or is absent. (Done for the Forecast screen; Dashboard horoscope is still a hard dependency.)
- Do not fail the whole dashboard because one optional card has missing data.

## 11. Offline and Low Connectivity UX

Current status:
- API response cache exists for profile, daily horoscope, general horoscope, forecast, and kundli.
- Cached responses are returned as normal `Response.success`, so UI currently cannot tell fresh network data from cached data.
- Cache is not a full offline mode yet; failed network after expired cache still becomes an error.

Future work:
- Add offline banner and per-section stale indicators.
- Keep cached dashboard/forecast/kundli visible when offline, even if stale, with clear labeling.
- Disable only write/stream actions that cannot work offline.
- Add retry buttons close to failed sections, not just full-screen error states. (Done for Forecast period tabs; other screens still use full-screen errors.)
- Add connectivity-aware upload/stream retry.

Fallback:
- "Showing last saved guidance" state for cached reads.
- "Connect to continue" state for chat/consult/match writes.

## 12. Accessibility and Input Robustness

Current issues:
- Many decorative icons use `contentDescription = null`, which is fine, but icon-only action buttons need a full pass.
- Dense horoscope/Kundli layouts and animated orbit UI need font-scale checks.
- Login/Profile/Match/Consult forms need IME, autofill, validation, and TalkBack traversal review.

Future work:
- Content descriptions for icon-only controls.
- Dynamic font scaling checks for dense dashboard/Kundli UI at 1.3x and 2.0x.
- TalkBack traversal for forms, chat messages, bottom/menu navigation, paywalls, and dialogs.
- Touch target audit for chips, cards, and icon buttons.
- Keyboard/IME behavior audit for login, profile, match, consult, chat, and location search.
- Reduced-motion support for particle/animated experiences.
- Use clear error text near fields and avoid color-only error states.

Fallback:
- Text-first controls remain available when animation/image content fails.

## 13. Product-Specific Fallbacks

### Entitlements and Paywalls

Current issues:
- Server entitlement APIs are wired, but client purchase flow is not.

Future work:
- Gate paid actions by server entitlement, not just UI state.
- Use idempotency keys for credit-consuming actions.
- Refresh balance after chat, consult, match, purchase, refund, and restore.

Fallback:
- If entitlement calls fail, do not incorrectly grant paid access. Show cached balance only as informational and block credit-consuming actions until the server confirms.

### Astrologers

Future work:
- If real booking is built, it needs backend availability, payment, cancellation, refunds, support, compliance, and quality controls.

Fallback:
- Route users to AI consult/chat if a future booking flow is unavailable.

### Health, Finance, Marriage, and Remedies Content

Future work:
- Add explicit safety disclaimers for medical, legal, financial, and gemstone/remedy guidance.
- Keep remedy language conservative; avoid claims that replace professional advice.
- Add crisis/safety fallback for self-harm or medical emergency language in AI chat/consult.

Fallback:
- Encourage professional help for high-stakes decisions and emergencies.

## 14. Testing Matrix

Required manual/device test matrix:
- API 24, 30, 31, 33, 34, 35.
- Android 13+ notification permission: fresh install allow/deny/swipe-away; upgrade path.
- Android 14 media partial access and re-selection if media permissions are ever added.
- No-GMS device/emulator.
- Slow network, offline, captive portal, timeout, server 500, invalid JSON.
- Token expired during SVG image load and during streaming.
- Background/foreground transitions during chat stream, consult stream, and profile update.
- Process death while logged in, during stream, and after cache write.
- Backup/restore or device transfer to a new device with stale/missing DataStore.
- Low RAM device with animations enabled.
- Font scale 1.3x and 2.0x.
- Dark/light/system theme switching.
- Entitlement server down, 402 paywall, empty catalog, purchase unavailable.

## 15. Prioritized Backlog

P0 - before wider testing:
- Centralize network error mapping and stop showing raw/generic HTTP details in UI. (Partially done: `ForecastViewModel` cleaned up and `ErrorSanitizer.sanitizeHttpCode()` added; the shared sealed result model and a full sweep of the other ViewModels are still pending.)

P1 - before production release:
- Permission coordinator with notification/media/microphone/camera/location flows as features are added.
- Offline/stale cache UI states.
- Streaming retry/cancellation/idempotency handling.
- Token storage/no-backup/encryption decision.
- Accessibility pass for major screens.
- Play Billing integration with backend receipt validation and entitlement reconciliation.
- Privacy policy, terms, disclaimers, and data deletion flow.
- Test matrix automation/checklist.

P2 - when adding future features:
- Notification opt-in and WorkManager reminders.
- Voice input with microphone fallback.
- Photo Picker/avatar flow.
- Billing/GMS/Play Integrity checks.
- Exact alarm special access only if product requires exact reminders.
- Share/export flows with safe intents and copy-link fallback.
- FCM push with in-app fallback.
- Family profiles, PDF exports, shareable cards, panchang, and cosmic calendar.

## Research Sources

- Android runtime permissions: https://developer.android.com/training/permissions/requesting
- Android notification permission: https://developer.android.com/develop/ui/compose/notifications/notification-permission
- Android Photo Picker: https://developer.android.com/training/data-storage/shared/photo-picker
- Android 14 selected photo/video access: https://developer.android.com/about/versions/14/changes/partial-photo-video-access
- Foreground service types: https://developer.android.com/develop/background-work/services/fgs/service-types
- Foreground service background-start restrictions: https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start
- Exact alarms: https://developer.android.com/develop/background-work/services/alarms
- Auto Backup and data extraction rules: https://developer.android.com/identity/data/autobackup
- Network Security Config: https://developer.android.com/privacy-and-security/security-config
- Package visibility: https://developer.android.com/training/package-visibility
- Clipboard sensitive content: https://developer.android.com/develop/ui/views/touch-and-input/copy-paste
- Google Play Billing system: https://developer.android.com/google/play/billing
- Compose performance best practices: https://developer.android.com/develop/ui/compose/performance/bestpractices
- Compose lazy lists, grids, and paging: https://developer.android.com/develop/ui/compose/lists
