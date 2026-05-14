# AstraNavi: Future Tech Debt, Permissions & Fallbacks

Last audited: 2026-05-12

This document tracks production-readiness work for Android device fragmentation, permissions, weak networks, background limits, storage/media access, and UI performance. It is a planning document only; items here should be implemented deliberately and tested on real devices before release.

## 0. Current App Snapshot

Current manifest state:
- Declared permissions: `INTERNET` only.
- No runtime permissions are currently declared for notification, microphone, camera, storage/media, contacts, location, SMS, phone, Bluetooth, or calendar.
- `android:allowBackup="true"` is enabled.
- `backup_rules.xml` and `data_extraction_rules.xml` are still sample/TODO rules and do not exclude session tokens or API cache data.

Current local audit:
- `catch (e: Exception)`: 41 instances.
- `AsyncImage(...)`: 7 instances.
- Lazy layouts: 16 `LazyColumn`, 4 `LazyRow`, 3 `HorizontalPager`.
- `LaunchedEffect(Unit)`: 19 instances.
- Streaming `byteStream()`: 2 instances, in chat and consult streaming.
- `HttpLoggingInterceptor.Level.BODY` is enabled, which can log passwords, JWTs, refresh tokens, and birth/profile data.
- Clipboard copy exists in chat; copied AI responses may include sensitive personal or birth-chart data.

## 1. Permission Strategy

Global rule:
- Ask for dangerous permissions only in context, when the user taps a feature that needs the permission.
- Never request all future permissions on app launch.
- Every permission flow must have a "Not now" path and a degraded fallback.
- If permission is denied or revoked, keep the app usable and hide only the dependent feature.
- Keep a single `PermissionGate`/`PermissionCoordinator` layer so individual screens do not duplicate permission logic.

### Permission Matrix

| Capability | Permission | When to ask | Fallback if denied |
|---|---|---|---|
| Daily reminders / push notifications | `POST_NOTIFICATIONS` on Android 13+ | Only after user enables horoscope reminders or notifications in settings | Keep dashboard/in-app banners; no system notifications |
| Voice input for Navi/chat/consult | `RECORD_AUDIO` | When user taps a microphone button | Text input remains primary; hide voice waveform/record button |
| Background voice capture, if ever needed | `RECORD_AUDIO`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MICROPHONE` | Avoid if possible; only during explicit user-visible recording | Stop recording when app backgrounds; upload only after foreground confirmation |
| Profile photo or media upload | Prefer Android Photo Picker, no broad storage permission | User taps upload/select image | Manual/default avatar; retry picker |
| Custom gallery picker | Android 13+: `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO`; Android 14+: also handle `READ_MEDIA_VISUAL_USER_SELECTED`; older: `READ_EXTERNAL_STORAGE` | Only if Photo Picker cannot satisfy the UX | Photo Picker / document picker / default avatar |
| Export/share generated chart file | Usually no storage permission if using share sheet or app-private cache | User taps Share/Export | Copy text/share link; save to app-private cache only |
| Camera capture for avatar or document scan | `CAMERA` | User taps camera capture | Photo Picker/manual upload/default avatar |
| Location for current city, if added | `ACCESS_COARSE_LOCATION` or `ACCESS_FINE_LOCATION` | User taps "Use current location" | Manual place entry, which should remain the default |
| Biometrics for unlock, if added | `USE_BIOMETRIC` plus `BiometricManager` capability checks | User enables biometric unlock | Email/password login remains available |
| Exact daily notification time | Prefer no exact-alarm permission | Only if user explicitly requests exact alarm behavior | WorkManager/flexible reminder window |
| Exact alarms, if truly required | `SCHEDULE_EXACT_ALARM` special app access or narrowly eligible `USE_EXACT_ALARM` | Dedicated settings explanation, not app launch | Flexible notification timing; dashboard refresh on open |
| Contacts/SMS/Phone/Calendar | Avoid for current product | Should not be requested unless a specific feature is approved | Use share sheet/manual input |
| Bluetooth/Nearby devices | Avoid for current product | Should not be requested unless wearable/device integration is approved | Feature hidden |

### Notification Permission

Future work:
- Add `POST_NOTIFICATIONS` only when notification features exist.
- Ask after the user opts into daily horoscope reminders, not during onboarding.
- Support three states: granted, denied, and not-yet-asked.
- If denied, show non-blocking in-app nudges and a settings deep link only after user interaction.
- Test fresh install, upgrade, deny, allow, and backup/restore states on Android 13+.

Fallback:
- Dashboard always checks cache/API when opened, so notifications are convenience only.
- If notifications are denied, no feature should fail.

### Storage, Media, and Profile Images

Future work:
- Prefer Android Photo Picker for avatars, attachment upload, and media selection.
- Do not request broad storage access for avatar selection.
- If a custom gallery UI is ever added, implement Android 13 granular media permissions and Android 14 selected-media re-selection.
- For long-running uploads from selected media, persist URI access only when required.
- Store temporary exports in cache/app-private files and clean them up.

Fallback:
- Use a default avatar/logo if the user cancels media selection or permission is denied.
- If Photo Picker is unavailable, fall back to `ACTION_OPEN_DOCUMENT`.

### Microphone and Voice Input

Future work:
- Add `RECORD_AUDIO` only when voice input is implemented.
- Request when the user taps the mic button.
- Show a clear recording state, stop button, timeout, and error state.
- Stop recording on lifecycle pause unless the product explicitly supports background recording.
- Do not start microphone foreground services from background or boot.

Fallback:
- Text chat and typed consult questions remain the primary path.
- If denied, hide mic UI or show a one-tap "Enable microphone" affordance.

### Camera

Future work:
- Add `CAMERA` only for an actual capture flow.
- Prefer Photo Picker for avatar selection unless in-app capture is a real requirement.
- Handle camera hardware missing, camera app unavailable, permission denied, and activity cancellation.

Fallback:
- Let the user pick an existing image or use the default avatar.

### Location

Future work:
- Avoid location permission for birth place. Current manual POB entry is better for privacy and accuracy.
- If "current location" is added for transit/current city features, ask only after the user taps that action.
- Approximate location should be sufficient unless a feature proves it needs fine location.

Fallback:
- Manual city/place entry.

### Biometrics

Future work:
- Check `BiometricManager` before showing biometric unlock.
- Treat biometrics as a convenience unlock, not account recovery.
- Fall back to email/password if hardware is missing, enrollment is absent, lockout occurs, or auth fails.

### Special App Access: Exact Alarms and Battery

Future work:
- Avoid `SCHEDULE_EXACT_ALARM` for normal daily horoscope reminders.
- Use WorkManager/flexible scheduling for non-critical reminders.
- If exact reminders are a paid/user-requested feature, explain why exact timing is needed and check `canScheduleExactAlarms()` before scheduling.
- If the user revokes exact alarm access, cancel/reschedule gracefully.

Fallback:
- Fetch on dashboard open.
- Use a broad reminder window rather than exact minute delivery.

## 2. Network & API Resilience

Current issue:
- 41 broad exception catches make it hard to distinguish timeout, offline, server error, parse error, auth failure, cancellation, and client bugs.

Future work:
- Centralize API results into a sealed result model:
  - `Success`
  - `HttpError(code, safeMessage)`
  - `NetworkUnavailable`
  - `Timeout`
  - `AuthExpired`
  - `ParseError`
  - `Cancelled`
  - `Unknown`
- Catch specific exceptions first: `UnknownHostException`, `SocketTimeoutException`, `ConnectException`, `SSLException`, `JsonSyntaxException`, `CancellationException`.
- Never show raw exception messages, API URLs, hostnames, stack traces, JWTs, or request bodies in UI.
- Standardize retry UI with exponential backoff for retryable reads.
- Add "last updated" and "served from cache" labels for cached horoscope/forecast/kundli/profile data.
- Keep histories network-fresh unless a deliberate short memory cache is added later.

Fallback:
- Profile: short TTL cache.
- Daily horoscope: fixed hourly boundary cache.
- Forecast: once per local day.
- Kundli: once per local day and invalidate on birth data change.
- Chat, consult, match histories: keep network-first to avoid frontend/backend mismatch.

## 3. Streaming Chat and Consult Fallbacks

Current issue:
- Chat and consult use `ResponseBody.byteStream()` directly.
- Streaming failures can leave partial messages, stuck loading states, or duplicated user messages.

Future work:
- Treat stream cancellation as a first-class state.
- Always close response bodies.
- Add request IDs or local message IDs so retry does not duplicate messages.
- Persist partial AI output separately from confirmed server messages.
- Detect malformed SSE chunks and continue when possible.
- Add stream timeout and idle timeout.
- Show "response interrupted, retry" when stream ends unexpectedly.

Fallback:
- Keep the user's typed prompt in the input/retry state.
- If stream fails after partial content, allow copy/retry and label it as incomplete.

## 4. Image Loading Fallbacks

Current issue:
- 7 `AsyncImage(...)` usages currently rely on image load success.
- SVG chart loads from the API with auth headers. If token expires, network fails, or SVG decoding fails, chart sections can look broken.

Future work:
- Add explicit placeholder and error drawables for every `AsyncImage`.
- For local zodiac/planet images, fall back to initials/text glyphs.
- For API SVG birth chart, show a local chart placeholder, retry button, and friendly error.
- Add stable dimensions to all image containers so image loading does not shift layout.
- Consider a shared `AstraAsyncImage` wrapper for placeholder/error/crossfade/logging policy.

Fallback:
- Default logo or constellation image.
- Text initials for static encyclopedia items.
- "Chart unavailable. Retry" for authenticated SVG chart.

## 5. Lazy Loading, Large Lists, and Compose Performance

Current issue:
- 16 `LazyColumn`, 4 `LazyRow`, and 3 `HorizontalPager` usages exist.
- Several `items(...)` calls do not provide stable keys.
- Some history/chat lists can grow without true pagination.
- Many animations are infinite or staged via `LaunchedEffect(Unit)`.

Future work:
- Add stable `key = { ... }` to dynamic lazy items:
  - Chat messages: `message.id`
  - Consult history: `record.id ?: created_at`
  - Match history: `record.id`
  - Forecast days: `day.date`
  - Static encyclopedias: stable id/name
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

Future work:
- Use WorkManager for flexible reminders/sync.
- Do not depend on background work for correctness; dashboard open must always validate current cache/API state.
- Avoid wake locks unless a strict use case exists.
- If OEM battery optimizations break reminders, show a settings hint only after the user enables reminders and misses expected notifications.
- Do not ask users to disable battery optimization during onboarding.

Fallback:
- In-app reminders and dashboard refresh on open.
- Server-side push, if available, as a complement rather than the only trigger.

## 7. Google Play Services and Non-GMS Devices

Future work:
- Before adding FCM, Billing, Play Integrity, or Play Services Photo Picker backport behavior, check service availability.
- Hide or disable unavailable features on Huawei, Amazon Fire, custom ROMs, and devices without GMS.
- Billing must have a non-crashing unavailable state and server-side validation.
- Push notifications must degrade to in-app reminders.

Fallback:
- Manual refresh, in-app purchase disabled message, and non-blocking UI.

## 8. Security, Privacy, and Sensitive Data

Current issues:
- `HttpLoggingInterceptor.Level.BODY` can expose login passwords, access tokens, refresh tokens, birth data, and AI/consult content in Logcat.
- `android:allowBackup="true"` is enabled while backup rules are still sample/TODO.
- DataStore stores tokens and cached profile/astrology data.
- Chat copy-to-clipboard can expose sensitive text to other surfaces.

Future work:
- Disable BODY logging in release builds. Prefer `NONE` or `BASIC`, and redact `Authorization`, `X-API-Key`, refresh tokens, passwords, DOB/TOB/POB, and profile data.
- Update `backup_rules.xml` and `data_extraction_rules.xml` to exclude session tokens, refresh tokens, API cache, and any sensitive local profile data unless there is an explicit restore strategy.
- Consider app-private/no-backup storage for sensitive cache.
- Review whether tokens should move to encrypted storage.
- Add a network security config that explicitly disallows cleartext traffic and documents any debug-only overrides.
- Avoid storing sensitive exports in external/shared storage.
- Mark clipboard content as sensitive where supported and consider clearing app-owned sensitive copied text after a short timeout.
- Add privacy policy and data deletion language before release.

Fallback:
- On backup restore with stale tokens, force re-login rather than silently using restored credentials.
- If cache decrypt/read fails, clear cache and refetch.

## 9. External Intents, Browser Links, Sharing, and Package Visibility

Future work:
- Wrap every external `startActivity(...)` in `try/catch ActivityNotFoundException`.
- Use explicit chooser/share intents for sharing horoscope, match result, or chart exports.
- Add `<queries>` only for specific external interactions that require package visibility.
- Avoid `QUERY_ALL_PACKAGES`.
- For Privacy Policy, Terms, support email, payment pages, and maps/place links, validate URL and offer "Copy link" fallback.

Fallback:
- Snackbar with copy-link action.
- Internal WebView only if security rules are defined; otherwise use browser intent.

## 10. Data Model and Parsing Robustness

Future work:
- Treat backend fields that can change shape as tolerant JSON (`JsonElement`) or explicit versioned models.
- Add parse-error telemetry/logging without exposing raw payloads to UI.
- Add default values for optional fields used by UI.
- Do not assume arrays are non-empty.
- Keep API model tests using saved sample responses for login, profile, horoscope, forecast, kundli, chat, consult, and match.

Fallback:
- Show partial sections when one optional field fails or is absent.
- Do not fail the whole dashboard because one optional card has missing data.

## 11. Offline and Low Connectivity UX

Future work:
- Add offline banner and per-section stale indicators.
- Keep cached dashboard/forecast/kundli visible when offline.
- Disable only write/stream actions that cannot work offline.
- Add retry buttons close to failed sections, not just full-screen error states.
- Add connectivity-aware upload/stream retry.

Fallback:
- "Showing last saved guidance" state for cached reads.
- "Connect to continue" state for chat/consult/match writes.

## 12. Accessibility and Input Robustness

Future work:
- Content descriptions for icon-only controls.
- Dynamic font scaling checks for dense dashboard/Kundli UI.
- TalkBack traversal for forms, chat messages, and bottom/menu navigation.
- Touch target audit for chips, cards, and icon buttons.
- Keyboard/IME behavior audit for login, profile, match, consult, and chat.
- Reduced motion support for particle/animated experiences.

Fallback:
- Text-first controls remain available when animation/image content fails.

## 13. Testing Matrix

Required manual/device test matrix:
- API 24, 30, 31, 33, 34, 35.
- Android 13+ notification permission: fresh install allow/deny/swipe-away; upgrade path.
- Android 14 media partial access and re-selection.
- No GMS device/emulator.
- Slow network, offline, captive portal, timeout, server 500, invalid JSON.
- Token expired during SVG image load and during streaming.
- Background/foreground transitions during chat stream, consult stream, and profile update.
- Process death while logged in, during stream, and after cache write.
- Backup/restore to new device with stale DataStore.
- Low RAM device with animations enabled.
- Font scale 1.3x and 2.0x.
- Dark/light/system theme switching.

## 14. Prioritized Backlog

P0 - before wider testing:
- Disable or redact BODY network logs.
- Update backup/data extraction rules for session/cache data.
- Add explicit `AsyncImage` placeholders/errors.
- Centralize network error mapping and stop showing raw exception messages.
- Add stable lazy-list keys for chat, consult history, match history, forecast days, and static encyclopedia lists.

P1 - before production release:
- Permission coordinator with notification/media/microphone/camera flows.
- Offline/stale cache UI states.
- Streaming retry/cancellation handling.
- Network security config.
- Accessibility pass for major screens.
- Test matrix automation/checklist.

P2 - when adding future features:
- Notification opt-in and WorkManager reminders.
- Voice input with microphone fallback.
- Photo Picker/avatar flow.
- Billing/GMS checks.
- Exact alarm special access only if product requires exact reminders.
- Share/export flows with safe intents and copy-link fallback.

## Research Sources

- Android runtime permissions: https://developer.android.com/training/permissions/requesting
- Android notification permission: https://developer.android.com/develop/ui/compose/notifications/notification-permission
- Android Photo Picker: https://developer.android.com/training/data-storage/shared/photo-picker
- Android 14 selected photo/video access: https://developer.android.com/about/versions/14/changes/partial-photo-video-access
- Foreground service types: https://developer.android.com/develop/background-work/services/fgs/service-types
- Foreground service background-start restrictions: https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start
- Exact alarms: https://developer.android.com/develop/background-work/services/alarms
- Auto Backup: https://developer.android.com/identity/data/autobackup
- Network Security Config: https://developer.android.com/privacy-and-security/security-config
- Package visibility: https://developer.android.com/training/package-visibility
- Compose performance best practices: https://developer.android.com/develop/ui/compose/performance/bestpractices
- Compose lazy lists and paging: https://developer.android.com/develop/ui/compose/lists
