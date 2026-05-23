# AstraNavi Current Feature And Product Specification

Last updated: 2026-05-19

This document reflects the current Android app implementation in this repository. It replaces the earlier extraction-style feature list with a source-backed view of what is now implemented, how the app is structured, and what still remains incomplete before a production pricing launch.

---

## 1. Product Snapshot

AstraNavi is a Kotlin/Jetpack Compose Android astrology app. The current app is no longer just a simple login, dashboard, and horoscope flow. It now includes:

- Authenticated onboarding with intro and logo splash transitions.
- Profile completion gating with birth details and location search.
- A personalized dashboard with daily horoscope, score map, forecast preview, Kundli preview, streaks, and recent consultation teaser.
- Deep Kundli analysis with chart SVG loading, dasha, houses, planets, transits, strengths, overlays, and AI prompt handoffs.
- Forecast browsing by area and time range.
- Streaming AI chat with persisted chat history and message rating.
- Guided AI consultations with category tree, streaming result, and consultation history.
- Match-making reports with match history and deletion.
- Static knowledge hub for rashis, planets, nakshatras, houses, and yogas.
- Entitlements, credits, paywalls, plan catalog, packs, and usage ledger UI.
- Responsive layout system for phones, folds, tablets, compact heights, and large font settings.
- DataStore-backed session, theme, profile fields, API response cache, version tracking, and migration flags.

The app is implemented as a single-activity Compose application in `MainActivity.kt`, with Navigation Compose routes defined in `AppDestination.kt` and screen-specific ViewModels injected through `ViewModelFactory`.

---

## 2. Current Tech Stack

- Language: Kotlin.
- UI: Jetpack Compose, Material 3, Navigation Compose.
- Architecture: Single `MainActivity`, Compose screens, ViewModels, repositories, Retrofit API layer.
- Persistence: Android DataStore preferences through `SessionManager`.
- Networking: Retrofit 2.11, OkHttp 4.12, kotlinx.serialization JSON converter.
- Streaming: OkHttp `ResponseBody` line parsing for chat and consultation server-sent token streams.
- Images: Coil Compose with SVG decoder for authenticated chart SVGs.
- Design assets: local WebP zodiac, nakshatra, and planet assets.
- Android config: `minSdk 24`, `targetSdk 35`, `compileSdk 35`.
- App id: `com.astranavi.app`.
- Runtime permissions: Internet only.

---

## 3. Navigation And Shell

The app uses route metadata in `AppDestination` to control title, auth requirements, top bar visibility, bottom bar visibility, and transition style.

Top-level authenticated destinations:

- Home: `home`
- Knowledge: `blogs`
- Kundli: `kundli`
- Consult: `consult`

Primary detail destinations:

- AI Chat: `chat?prompt={prompt}`
- Profile: `profile`
- Forecast: `forecast?area={area}`
- Match: `match`
- Match History: `match_history`
- Consult History: `consult_history`
- Plans: `plans`
- Experts: `astrologers`
- Zodiac: `rashis?rashiId={rashiId}`
- Planets: `planets`
- Nakshatras: `nakshatras`
- Houses: `houses`
- Yogas: `yogas`

Unauthenticated/startup destinations:

- Intro: `intro`
- Logo splash: `logo_splash?loadingText={text}`
- Login/Register: `login`

Shell behavior:

- Main pages show a transparent `CenterAlignedTopAppBar`.
- The top bar includes a credit badge that opens Plans.
- Top-level pages show a bottom navigation bar with Home, Kundli, Consult, Knowledge, and a center AI Chat FAB.
- Detail pages generally hide the bottom bar.
- The drawer/menu slides from the right and includes Home, Knowledge, AI Chat, Consult, Match, theme toggle, profile entry, and logout.
- Offline mode displays a global banner: "Offline Mode - Using cached guidance".
- Route transitions are crossfade for top-level tabs and slide-right for detail screens.

---

## 4. Startup, Session, And Migration Flow

The launch flow now does more than checking whether a token exists.

Current launch process:

1. `SessionManager` is created.
2. `RetrofitClient.init(sessionManager)` starts token collection for auth headers.
3. App/data version values are read from DataStore.
4. Legacy or stale API caches are cleared when version/data-version checks require it.
5. If a user id and access token exist, the app calls `GET api/auth/me`.
6. A 200 response keeps the user authenticated.
7. A 401 response clears session data and sends the user to login.
8. A network failure keeps the user in offline mode if saved data exists.
9. Intro and logo splash decide final navigation to login, profile completion, or dashboard.

Session data stored locally:

- User id, email, display name.
- Moon sign, sun sign, lagna/rising sign.
- DOB, TOB, POB.
- Birth location metadata fields.
- Access token and refresh token.
- Profile completion flag.
- Theme preference.
- Intro seen flag.
- Launch id and dashboard entry animation state.
- Last app version code and data version.
- API cache entries.

Token handling:

- Every API request receives `X-API-Key`.
- Authenticated requests receive `Authorization: Bearer <access token>`.
- OkHttp authenticator attempts refresh through `POST api/auth/refresh`.
- If refresh fails, local session is cleared.
- Debug HTTP logging is currently `BASIC`; release logging is disabled.

---

## 5. Authentication And Onboarding

Implemented files:

- `LoginScreen.kt`
- `LoginViewModel.kt`
- `RegistrationViewModel.kt`
- `IntroAnimationScreen.kt`
- `LogoSplashScreen.kt`
- `AuthRepository.kt`
- `SessionManager.kt`

Login:

- Email/password form.
- Password visibility toggle.
- Validation for blank email/password.
- Calls `POST api/login`.
- Saves user, tokens, signs, birth fields, and profile completion flag.
- Handles invalid credentials and sanitized network errors.
- Routes through logo splash after submit.

Registration:

- Email/password/confirm password form.
- Confirms password match before API call.
- Calls `POST api/register`.
- Saves returned session directly; no separate login required.
- Parses validation error responses where possible.
- Routes newly registered users toward profile completion.

Intro/splash:

- Intro animation appears before auth routing.
- Logo splash supports custom loading text for login/register transitions.
- Auth errors return to login instead of progressing.

---

## 6. Profile And Account Management

Implemented files:

- `ProfileScreen.kt`
- `ProfileViewModel.kt`
- `LocationSearchField.kt`
- `AuthRepository.kt`

Profile capabilities:

- Loads current profile from `GET api/user/profile`.
- Edits name, gender, DOB, TOB, place of birth, phone number, marital status, occupation, and language.
- Uses date picker and time picker helpers.
- Uses location search through `GET api/locations/search`.
- Tracks birth place metadata: display name, latitude, longitude, timezone name, timezone offset, and fold.
- Shows profile completion progress.
- Required completion fields are name, gender, DOB, TOB, and POB.
- Saves through `PUT api/user/profile`.
- Updates DataStore profile fields, signs, and profile completion state.
- Includes account deletion through `DELETE api/user`.
- Includes logout action.
- Uses skeleton loading and a sticky save/complete action.

Profile is a major gate: dashboard access after registration depends on the stored `profileComplete` value and locally updated completion state.

---

## 7. Dashboard

Implemented files:

- `DashboardScreen.kt`
- `DashboardViewModel.kt`
- `DashboardRepository.kt`

Purpose:

The dashboard is the main daily habit surface. It combines daily guidance, personal identity, energy scoring, fast navigation, and previews of deeper modules.

Data loaded:

- Profile from `GET api/user/profile`.
- Personalized daily horoscope from `GET api/daily-horoscope`.
- Forecast preview from `GET api/forecast/general?days_back=0&days_forward=7`.
- Kundli preview from `POST api/analyze-full`.
- Recent consultations from `GET api/consult/history?limit=5`.

Implemented dashboard sections:

- Header with personalized date, panchanga, planetary context, and identity badges.
- Moon, Sun, and Lagna/rising sign display using profile or Kundli fallback data.
- Daily snapshot card with score, mood, lucky color/number, tip, and alert content.
- "Ask Navi" entry point into chat.
- Active guidance card based on time triggers.
- Energy map/orbit system for life areas such as general, love, career, health, and finance.
- Popup overlays for expanded area score details and rashi details.
- Weekly forecast preview with graph and route handoff into Forecast.
- Kundli peek card with authenticated SVG chart preview, strongest planet insight, and current dasha summary.
- Recent consultation teaser with "ask again" behavior.
- Streak card using horoscope engagement data.
- Pull-to-refresh tied to `fetchDashboardData(forceRefresh = true)`.
- One-time dashboard orbit entry animation per launch.

Dashboard state:

- `Loading`
- `Success`
- `Error`

Dashboard paywall behavior:

- Dashboard can surface paywall data returned by horoscope or Kundli preview responses.
- Full-screen paywall overlay is handled globally by `EntitlementViewModel`.

---

## 8. Forecast

Implemented files:

- `ForecastScreen.kt`
- `ForecastViewModel.kt`
- `ScoreColors.kt`

Purpose:

Forecast shows area-specific past/future astrology scoring and daily detail.

Areas:

- General
- Love
- Career
- Health
- Finance

Ranges:

- Past month: `days_back=30`, `days_forward=0`
- Next 7 days: `days_back=0`, `days_forward=7`
- Next 30 days: `days_back=0`, `days_forward=30`

UI features:

- Area tabs.
- Range selector.
- Dynamic top bar title such as "Career Forecast".
- Pull-to-refresh.
- Today snapshot with score arc, mood, dominant planet meaning, lucky color, and personal adjustment.
- Forecast trend graph reused from dashboard graph logic.
- Clickable day selection.
- Detailed day card with interpretation, lucky values, alerts, and transit chips.
- Two-pane layout on wider screens.
- Skeleton and error states.

---

## 9. Kundli

Implemented files:

- `KundliScreen.kt`
- `KundliViewModel.kt`
- `DashaComponents.kt`
- `AnalyzeFullJsonAdapter.kt`
- `AstrologyRepository.kt`

Purpose:

The Kundli screen is now a full chart-analysis experience, not only a static chart preview.

Data source:

- `POST api/analyze-full`
- Request: `AnalyzeFullRequest(force_refresh = Boolean, chart_context = optional)`
- Response wrapper: `AnalyzeFullWrapper`

Implemented Kundli zones:

- Quick insights bar with high-level chart prompts.
- Ascendant/rising identity.
- Current dasha summary.
- Planet strength ranking.
- Planet gallery with dignity, retrograde, combust, shadbala, nakshatra, house, aspects, ownership, and interpretations.
- House energy bar and house analysis using Ashtakavarga and house data.
- Transit section.
- Dasha timeline.
- Key themes.
- Navi AI prompt section.
- Authenticated North Indian chart SVG using Coil SVG decoder and API/auth headers.
- Pull-to-refresh with `force_refresh=true`.
- Tablet/two-pane and phone layouts.

Interactive overlays:

- House expansion overlay.
- Planet expansion overlay.
- Back handling closes detail overlays before leaving the screen.
- Overlay content includes prompt handoffs into chat.

Paywall handling:

- Kundli response supports `lockedSections` and `paywall`.
- The ViewModel exposes locked sections and paywall data.
- UI components exist for locked/paywall cards.

---

## 10. AI Chat

Implemented files:

- `ChatScreen.kt`
- `ChatViewModel.kt`
- `AstrologyRepository.kt`

Purpose:

Navi is the app's conversational astrology guide. Chat can start from the Chat tab/FAB or from contextual prompts passed by Dashboard and Kundli.

Chat capabilities:

- Welcome chat is created locally before a server chat exists.
- First real user message creates a chat through `POST api/chats`.
- Existing chats load through `GET api/chats/{chatId}`.
- Chat list loads through `GET api/chats`.
- Pagination uses `cursor`.
- Chats can be deleted through `DELETE api/chats/{chatId}`.
- Messages stream from `POST api/chats/{chatId}/messages`.
- Stream parser reads `data: {...}` lines and appends `token` values.
- Messages can be rated through `PUT api/chats/{chatId}/messages/{msgId}/rate`.
- Chat history panel can be toggled.
- Prompt route parameter can immediately send a contextual message.

Chat states:

- `Idle`
- `LoadingHistory`
- `LoadingMessages`
- `ActiveChat`
- `PaywallBlocked`
- `Error`

Paywall handling:

- Chat creation or message streaming can return JSON paywall data.
- 402-style paywall data is parsed by `EntitlementRepository`.
- Paywall can block the active chat surface.

---

## 11. Guided AI Consultation

Implemented files:

- `ConsultScreen.kt`
- `ConsultViewModel.kt`
- `ConsultHistoryScreen.kt`
- `ConsultHistoryViewModel.kt`

Purpose:

Consult is a guided, structured version of AI astrology. It asks for birth details, loads a category tree, guides the user to a final question, and streams an answer.

Consult steps:

1. Birth details.
2. Category selection.
3. Sub-category selection.
4. Question selection.
5. Result.

Implemented capabilities:

- Prefills DOB, TOB, and POB from DataStore.
- Validates DOB, HH:MM time format, and POB.
- Converts `HHMM` to `HH:MM`.
- Calculates age from DOB for category-tree request.
- Loads consult tree through `GET api/consult/tree`.
- Supports selected tone, language, and custom note.
- Streams consultation through `POST api/consult`.
- Parses streaming `data:` token lines.
- Handles JSON paywall responses.
- Supports backward step navigation and direct step jumps when state allows.
- Shows consult history through `GET api/consult/history`.

Consult history:

- Displays saved consultation records from the backend.
- Supports loading, success, and error states.

---

## 12. Match Making

Implemented files:

- `MatchScreen.kt`
- `MatchViewModel.kt`
- `MatchHistoryScreen.kt`
- `MatchHistoryViewModel.kt`

Purpose:

Match calculates relationship compatibility using two birth profiles and stores/retrieves prior match reports.

Implemented capabilities:

- Person 1 and Person 2 detail entry through `PersonDetail`.
- Calls `POST api/match?narrative=true`.
- Displays match result from `MatchResponse`.
- Handles paywall/insufficient credit responses.
- Loads match history through `GET api/match/history`.
- Supports retrieving a single match through `GET api/match/{matchId}` in repository.
- Supports deleting saved matches through `DELETE api/match/{matchId}`.

Match state:

- `Idle`
- `Loading`
- `Success`
- `PaywallBlocked`
- `Error`

---

## 13. Knowledge Hub

Implemented files:

- `KnowledgeHubScreen.kt`
- `RashiScreen.kt`
- `RashiViewModel.kt`
- `PlanetScreen.kt`
- `NakshatraScreen.kt`
- `HouseScreen.kt`
- `YogaScreen.kt`
- Static repositories: `RashiData.kt`, `PlanetData.kt`, `NakshatraData.kt`, `HouseData.kt`, `YogaData.kt`

Purpose:

The Knowledge Hub is the static learning/encyclopedia area. It does not require API calls for most content.

Implemented knowledge categories:

- Rashis: 12 zodiac signs with descriptions, traits, career themes, elements, modalities, lords, and imagery.
- Planets: 9 grahas including Sanskrit names, relationships, dignities, karakatvas, and interpretations.
- Nakshatras: 27 nakshatras with pada data, keywords, traits, careers, and mythology-style metadata.
- Houses: 12 houses with meanings and significations.
- Yogas: static yoga explanations and traits.

Navigation:

- Knowledge hub routes to each encyclopedia section.
- Dashboard can deep-link into rashi details and knowledge sections.

---

## 14. Astrologers / Experts

Implemented files:

- `AstrologersScreen.kt`
- `AstrologersViewModel.kt`
- `AstrologerModel.kt`

Current status:

- Displays a static/mock list of astrologers.
- Shows name, years of experience, specialties, rating, review count, price, and online status.
- Includes loading skeleton.
- Includes "Book Session" UI.

Important limitation:

- Expert booking is not wired to a backend or payment flow yet.
- Astrologer data is hardcoded in the ViewModel.
- This should be treated as a placeholder surface until real booking, availability, pricing, and compliance rules are implemented.

---

## 15. Plans, Credits, Entitlements, And Paywalls

Implemented files:

- `EntitlementModels.kt`
- `EntitlementRepository.kt`
- `EntitlementViewModel.kt`
- `PlansViewModel.kt`
- `PlansPage.kt`
- `CreditBadge.kt`
- `PaywallCard.kt`
- `LockedSectionCard.kt`
- `PaywallUtils.kt`

Backend endpoints:

- `GET api/entitlements/balance`
- `GET api/entitlements/subscription`
- `GET api/entitlements/packs`
- `GET api/entitlements/history`
- `POST api/entitlements/consume`
- `GET api/entitlements/catalog`
- `GET api/entitlements/paywall`
- `GET api/entitlements/paywall/features`

Global entitlement state:

- Balance.
- Tier.
- Total/current remaining credits.
- Subscription detail.
- Active packs.
- Feature paywall states.
- Active global paywall modal.

Credit badge:

- Displayed in top bar for authenticated pages.
- Opens the Plans page.

Plans page:

- Shows current credit balance.
- Shows current subscription.
- Has tabs for subscriptions and add-on packs.
- Loads catalog products.
- Shows active packs.
- Shows usage history through top bar history toggle.
- Supports tablet two-pane layout.
- Shows retry state when loading fails.

Paywall behavior:

- Soft paywalls render as inline/overlay cards.
- Hard paywalls render as full block cards.
- 402 responses can be parsed into `PaywallCardData`.
- Global active paywall overlays the app with scrim.

Important limitation:

- Purchase buttons in Plans and paywall suggested products are UI placeholders.
- There is no completed Google Play Billing / payment purchase flow in this repo yet.

---

## 16. API Contract Inventory

Auth:

- `POST api/register`
- `POST api/login`
- `GET api/auth/me`
- `POST api/auth/refresh` used directly by OkHttp authenticator

Profile:

- `GET api/user/profile`
- `PUT api/user/profile`
- `DELETE api/user`
- `GET api/locations/search`

Daily guidance:

- `GET api/daily-horoscope`
- `GET api/horoscope/{sign}`
- `GET api/forecast/{area}`

Kundli:

- `POST api/analyze-full`
- `GET api/profile/svg?style=north&theme={dark|light}` loaded by Coil using API/auth headers

Match:

- `POST api/match`
- `GET api/match/history`
- `GET api/match/{matchId}`
- `DELETE api/match/{matchId}`

Chat:

- `GET api/chats`
- `POST api/chats`
- `GET api/chats/{chatId}`
- `DELETE api/chats/{chatId}`
- `POST api/chats/{chatId}/messages`
- `PUT api/chats/{chatId}/messages/{msgId}/rate`

Consult:

- `GET api/consult/tree`
- `POST api/consult`
- `GET api/consult/history`

Entitlements:

- `GET api/entitlements/balance`
- `GET api/entitlements/subscription`
- `GET api/entitlements/packs`
- `GET api/entitlements/history`
- `POST api/entitlements/consume`
- `GET api/entitlements/catalog`
- `GET api/entitlements/paywall`
- `GET api/entitlements/paywall/features`

---

## 17. Data And Caching

Implemented cache layer:

- `ApiResponseCache.kt`
- `ApiCachePolicy.ForDuration`
- `ApiCachePolicy.UntilNextLocalHour`
- `ApiCachePolicy.UntilNextLocalMidnight`

Cached response types:

- Profile.
- Daily horoscope.
- General horoscope.
- Forecast.
- Kundli/analyze-full wrapper.

Cache behavior:

- Cache keys are scoped by user id or email.
- Cache version prefix is currently `v2:`.
- Forecast and Kundli keys include a birth-data fingerprint.
- Invalid cache payloads are removed.
- Expired cache entries are removed.
- Profile has a 5 minute TTL.
- Daily/time-sensitive data uses local hour or local midnight policies depending on repository call.
- Data version changes clear API cache.

Offline behavior:

- Startup network failure during token validation enables offline mode rather than forcing logout.
- Cached guidance can continue to render where cache exists.

---

## 18. Design System And Responsiveness

Implemented files:

- `Theme.kt`
- `Color.kt`
- `Type.kt`
- `Responsive.kt`
- `UIUtils.kt`
- `KnowledgeUIUtils.kt`
- `ScoreColors.kt`
- `ParticleBackground.kt`

Design direction:

- Cosmic/dark-first visual identity with Material 3 surfaces.
- Particle background used behind the shell and drawer.
- Planet and zodiac imagery from local WebP assets.
- Score colors vary by area and score phase rather than a single static palette.
- Cards use translucent surfaces and subtle borders.
- Dashboard has animated orbit/energy-map behavior.
- Kundli uses layered parallax, reveal, and expansion overlays.

Responsive behavior:

- Screen classes: compact phone, phone, fold, tablet.
- Adjusts padding, card sizes, bottom nav, FAB, grids, charts, and text sizes.
- Uses two-pane layouts on wider screens where practical.
- Has large-font checks and compact-height checks.
- Measures bottom bar height and exposes it through composition local for screen padding.

Theme behavior:

- Theme preference supports system, light, and dark.
- Drawer exposes light/dark toggle.
- Logout preserves theme preference.

---

## 19. Implemented Retention Loops

Daily habit:

- Daily horoscope.
- Today score/mood/lucky values.
- Weekly forecast preview.
- Streak display from engagement data.
- Pull-to-refresh on major daily data screens.

Personalization:

- Birth details drive profile completion.
- Signs and dasha are cached into DataStore.
- Dashboard derives missing signs from full Kundli analysis when profile data is incomplete.
- Chat prompts are contextual from dashboard, Kundli, and overlays.

History:

- Chat history.
- Consultation history.
- Match history.
- Entitlement usage history.

Monetization hooks:

- Global credit badge.
- Paywalls on chat/match/consult/Kundli/dashboard-backed data.
- Plans page with subscriptions, packs, catalog, and ledger.

---

## 20. Known Incomplete Or Launch-Risk Areas

These are not future feature ideas; they are current gaps or risks visible in the implementation.

- Payments are not complete. Plans and suggested paywall products show purchase UI, but real billing is not wired.
- Astrologer booking is placeholder. Experts are hardcoded and "Book Session" has no real flow.
- Some app source strings still show encoding/mojibake artifacts in the local file display. UI text should be audited on-device.
- Some dynamic lazy lists still need stable keys across all screens.
- Authenticated SVG chart loading now has loading/error placeholders in some places, but should still be tested under expired token, offline, and bad SVG responses.
- Sensitive copy/share behavior for chat and consult text should be reviewed.
- `data_extraction_rules.xml` and backup/privacy behavior should stay aligned with sensitive birth/profile data handling.
- Purchase/paywall buttons should be disabled, hidden, or clearly marked until billing launches.
- More automated tests are needed for startup auth validation, token refresh, streaming parsers, paywall parsing, profile completion routing, and cache invalidation.

---

## 21. Main Differentiators Now Implemented

- The dashboard is a unified daily command center rather than a simple horoscope page.
- Kundli is interactive and visually rich, with house/planet expansion overlays and AI handoff prompts.
- Chat and Consult both support streaming AI responses.
- Entitlements and paywalls are integrated into core feature flows rather than only documented.
- Forecast supports area and time-range exploration with graphs and day-level detail.
- Knowledge content is broad enough to support app depth without requiring backend calls.
- Startup auth validation and offline mode are stronger than a token-presence-only app.
- Responsive metrics are centralized and used broadly across the app.

---

## 22. Practical Roadmap From Current State

Highest priority before production monetization:

- Implement or hide real purchase flows.
- Replace placeholder astrologer booking with real backend-backed experts or remove from launch navigation.
- Add automated tests for auth/session migration and streaming flows.
- Audit sensitive-data handling, copy behavior, backup rules, and logs.
- Validate paywall behavior for all 402 response shapes.
- Verify chart SVG loading with expired tokens and offline state.

Next product polish:

- Strengthen empty/error states for every history and catalog screen.
- Add stable list keys everywhere.
- Make entitlement refreshes visible when credit-changing actions complete.
- Add better offline/stale indicators for cached horoscope, forecast, and Kundli data.
- Finish localization beyond profile language selection.

Next growth/features:

- Real panchang and calendar surfaces.
- Notifications and daily reminder controls.
- Report export/download.
- Real astrologer scheduling, payments, and session history.
- Deeper remedies, muhurat, and personalized rituals if backend support exists.

---

## 23. File Map

Core app shell:

- `app/src/main/java/com/astranavi/app/MainActivity.kt`
- `app/src/main/java/com/astranavi/app/ui/navigation/AppDestination.kt`
- `app/src/main/java/com/astranavi/app/ui/navigation/AppNavigationActions.kt`
- `app/src/main/java/com/astranavi/app/ui/ViewModelFactory.kt`

Network/data:

- `app/src/main/java/com/astranavi/app/data/api/ApiService.kt`
- `app/src/main/java/com/astranavi/app/data/api/RetrofitClient.kt`
- `app/src/main/java/com/astranavi/app/data/api/JsonConfig.kt`
- `app/src/main/java/com/astranavi/app/data/api/AnalyzeFullJsonAdapter.kt`
- `app/src/main/java/com/astranavi/app/data/cache/ApiResponseCache.kt`
- `app/src/main/java/com/astranavi/app/data/repository/AuthRepository.kt`
- `app/src/main/java/com/astranavi/app/data/repository/DashboardRepository.kt`
- `app/src/main/java/com/astranavi/app/data/repository/AstrologyRepository.kt`
- `app/src/main/java/com/astranavi/app/data/repository/EntitlementRepository.kt`

Major screens:

- `ui/login`
- `ui/profile`
- `ui/dashboard`
- `ui/forecast`
- `ui/kundli`
- `ui/chat`
- `ui/consult`
- `ui/match`
- `ui/knowledge`
- `ui/rashis`
- `ui/astrologers`
- `ui/entitlement`
- `ui/splash`

Shared UI/components:

- `ui/components/Responsive.kt`
- `ui/components/UIUtils.kt`
- `ui/components/ScoreColors.kt`
- `ui/components/PaywallCard.kt`
- `ui/components/LockedSectionCard.kt`
- `ui/components/CreditBadge.kt`
- `ui/components/DashaComponents.kt`
- `ui/components/LocationSearchField.kt`
- `ui/components/ParticleBackground.kt`

Local/session utilities:

- `util/SessionManager.kt`
- `util/ErrorSanitizer.kt`
- `util/ZodiacMapper.kt`
- `util/PersonalizedQuestions.kt`
- `util/PaywallUtils.kt`

---

## 24. Conclusion

AstraNavi has moved from a documented concept/app shell into a much fuller product implementation. The current app has authenticated startup safety, personalized dashboard content, full Kundli analysis, streaming chat, guided consultations, match reports, knowledge modules, responsive UI, and a backend-facing entitlement system.

The most important remaining product gap is not feature breadth; it is launch completeness around payments, expert booking, privacy hardening, automated test coverage, and final error/offline polish.
