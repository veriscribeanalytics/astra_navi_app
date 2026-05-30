# CONFIRM.md — AstraNavi Production-Readiness Source of Truth

> **Read this before changing anything related to: secrets, signing, build config, Manifest, network config, DataStore, or release flow.**
>
> This file is the canonical checklist for AstraNavi. Both the human owner and any AI agent must consult and update it.
>
> Last verified against repo: **2026-05-28**. Stack snapshot: `compileSdk=35`, `minSdk=24`, `targetSdk=35`, `versionCode=1`, `versionName="1.0"`, Compose + kotlinx.serialization + Retrofit + DataStore. **No release shipped yet.**

---

## 0. How to use this file

- ✅ = verified present and correct in the repo as of the date above.
- ⚠️ = present but with a caveat — read the note.
- ❌ = missing. Must be fixed before the dates noted (most before first Play Store upload).
- Section 6 is the **active TODO list** in priority order. Update statuses there as items are completed; do not delete completed items — convert ❌ → ✅ with a short note and a date.

---

## 1. What AstraNavi already does well (verified)

| Area | Status | Evidence |
|---|---|---|
| Git initialized, remote set | ✅ | `origin → github.com/veriscribeanalytics/astra_navi_app.git` |
| No secrets ever committed to git history | ✅ | `git log --all --diff-filter=A` for `local.properties`, `*.jks`, `*.keystore`, `google-services.json`, `.env` → 0 hits |
| `local.properties` is gitignored | ✅ | `.gitignore` line 3 & 15 |
| `API_KEY` loaded into `BuildConfig` from `local.properties` | ✅ | `app/build.gradle.kts:16–31` |
| `usesCleartextTraffic="false"` in Manifest | ✅ | `app/src/main/AndroidManifest.xml:19` |
| `networkSecurityConfig` enforces HTTPS only | ✅ | `app/src/main/res/xml/network_security_config.xml` |
| `allowBackup="false"` + `fullBackupContent="false"` | ✅ | Manifest lines 8–9 |
| `data_extraction_rules.xml` excludes sharedpref / datastore / files / db from cloud backup AND device transfer | ✅ | Better than the checklist asked for |
| ProGuard/R8 enabled for release (`isMinifyEnabled=true`, `isShrinkResources=true`) with keep rules for kotlinx-serialization models, Retrofit, OkHttp, Coil, DataStore, Composables | ✅ | `app/build.gradle.kts:41–48`, `app/proguard-rules.pro` |
| Folder structure `ui/ data/{api,cache,model,repository} util/` | ✅ | Matches the recommended layout |
| Single Activity + Compose Navigation in deps | ✅ | `MainActivity.kt`, `androidx.navigation.compose` in `libs` |
| Image loading via Coil (`coil-compose`, `coil-svg`) | ✅ | `app/build.gradle.kts:97–99` |
| kotlinx.serialization end-to-end (not Gson) | ✅ | Project-wide rule from memory; confirmed in `JsonConfig.kt`, `RetrofitClient.kt` |
| HTTP layer hardening — token-refresh `Authenticator` with bounded retries, race-safe synchronized refresh, session clear on terminal 401 | ✅ ✨ | `RetrofitClient.kt:75–155` — this is **more robust than the original checklist suggested** |
| Per-app locale config (`locales_config.xml`, `AppLocalesMetadataHolderService`) supporting 11 languages | ✅ ✨ | Beyond checklist scope |
| Lint guards: `MissingTranslation` and `ExtraTranslation` are **errors**, not warnings | ✅ ✨ | `app/build.gradle.kts:36–38` |
| Java 11 + core library desugaring | ✅ | `app/build.gradle.kts:53–56` |
| Only `INTERNET` permission declared (no over-asking) | ✅ | Manifest line 5 |

**Bottom line:** the security & build hygiene is already at or above the bar the checklist set for "Phase 1–2 + Phase 4 + Phase 8.1/8.3." The gaps are concentrated in **secret storage at rest (8.2), release signing (Phase 9), crash reporting (6.1), and process-wide error handling (6.2).**

---

## 2. What's missing or wrong (verified gaps, in priority order)

### 🔴 P0 — fix before any external testers / Play Internal Testing

1. **❌ Auth tokens stored in plaintext DataStore.** `SessionManager.kt` writes `ACCESS_TOKEN` and `REFRESH_TOKEN` into a plain `preferencesDataStore`. On a rooted device or with adb backup re-enabled accidentally, these are recoverable. The original checklist's `EncryptedSharedPreferences` recommendation is one option; a cleaner Compose-era option is **DataStore + EncryptedFile (Tink)** or **DataStore + Android Keystore-derived `Serializer<Preferences>` encryption**. Pick one and migrate `ACCESS_TOKEN` + `REFRESH_TOKEN` (and only those) out of the plain store.
   *Do NOT encrypt the whole `user_session` DataStore* — birth data / signs / theme don't need it and key-rotation churn isn't worth it.

2. **❌ No release `signingConfig`.** A `./gradlew assembleRelease` today would either fail or sign with the debug key. Required before any AAB upload. Defer the actual `.jks` creation, but the **gradle scaffolding + `local.properties` keys for it** should land soon, so you can't accidentally upload an unsigned/debug-signed build.

3. **❌ `.gitignore` does not list keystore / Firebase / env file patterns.** No such files exist yet, so history is still clean — but the moment you create a keystore on this machine it could be auto-staged. Add **now**, before you generate any keys:
   ```
   *.jks
   *.keystore
   keystore.properties
   google-services.json
   GoogleService-Info.plist
   .env
   .env.*
   app/release/
   /captures
   ```

### 🟠 P1 — fix before public launch

4. **❌ No Firebase Crashlytics / no crash reporting at all.** When a user's phone crashes, you have zero signal. The checklist Phase 6.1 is correct — add Crashlytics (free, Firebase). Alternative: Sentry. Either way, pick one before launch.

5. **❌ No `Application` subclass.** You need one to: (a) initialize Crashlytics, (b) install a `Thread.setDefaultUncaughtExceptionHandler`, (c) call `RetrofitClient.init(sessionManager)` from a single owned place instead of `MainActivity`. Register it as `android:name=".AstraNaviApp"` in the Manifest.

6. **❌ Debug vs release build split is missing.** Single `API_BASE_URL` (`https://api.veriscribeanalytics.com/`) hard-coded in `RetrofitClient.kt:30` and single `API_KEY` in `local.properties`. Per checklist Phase 2.3, add:
   - `DEBUG_API_BASE_URL` / `RELEASE_API_BASE_URL` in `local.properties`
   - `DEBUG_API_KEY` / `RELEASE_API_KEY` in `local.properties`
   - `buildConfigField` for each, scoped to `buildTypes.debug { }` and `buildTypes.release { }`
   - Remove the hard-coded URL in `RetrofitClient.kt`; read `BuildConfig.API_BASE_URL` instead.

7. **❌ No branch discipline.** You have `main` only, ~30 modified files dirty, and you commit directly to `main`. Create `develop`, do daily work there, fast-forward `main` only when stable, tag releases (`v0.x.0` while pre-launch, `v1.0.0` at launch). This is your only insurance against "I broke something three days ago and don't know what."

### 🟡 P2 — nice to have, not blockers

8. **⚠️ HTTP logging interceptor is always installed at `Level.NONE`** (`RetrofitClient.kt:58–60`). That's safe but dead weight. Either delete it or gate it: `if (BuildConfig.DEBUG) Level.BODY else Level.NONE`. The conditional version helps you debug staging without ever leaking bodies in release.

9. **⚠️ No Hilt / DI framework.** You have a hand-rolled `ViewModelFactory.kt`. This is fine for the current size; it becomes painful around ~25 ViewModels. Decide before then. *Not required by Play Store.*

10. **⚠️ `targetSdk = 35` but `targetsdk36.md` exists.** You're already tracking the SDK 36 bump. Don't ship to Play Store on SDK 35 if Google's deadline has passed by your launch date — check Play Console requirements at launch time.

11. **⚠️ Empty `di/` directory** — either populate when Hilt lands or delete to avoid confusion.

12. **⚠️ `API_KEY = "vedicastra-api-key-2026-secure"` in `local.properties`** — this looks like a long-lived shared secret. **Anything in `BuildConfig` ships inside the APK and can be extracted by anyone with `apktool`.** A static `X-API-Key` header does not authenticate users; it only gates abuse weakly. Treat it as: "rate-limit gate, not a secret." Real auth = the JWT access token (which is per-user and short-lived — that part is correct). If your backend treats `X-API-Key` as a real secret, that's a backend bug.

---

## 3. Secret-handling rules (specific to AstraNavi)

The single rule that covers everything:

> **If it ships inside the APK, it is public. If it can charge money, send messages, or read another user's data, it lives only on your backend.**

Concretely for this app:

| Thing | Where it lives | Who can see it | OK? |
|---|---|---|---|
| `API_KEY` (the `X-API-Key` header) | `local.properties` → `BuildConfig.API_KEY` → shipped in APK | Anyone with apktool | Acceptable **only if** backend treats it as a rate-limit gate, not auth |
| JWT access token | `EncryptedSharedPreferences` (`secure_tokens`), refreshed via `/auth/refresh` | The logged-in user only | ✅ encrypted at rest as of 2026-05-28 |
| Refresh token | Same as above | Same | ✅ encrypted at rest as of 2026-05-28 |
| User birth data (DOB/TOB/POB, signs) | DataStore | User only | Plaintext is acceptable; not a credential |
| Backend URL | Hard-coded in `RetrofitClient.kt` today | Anyone with apktool | Acceptable; URLs are not secrets |
| Future: Razorpay / payment keys | Backend only — **never** in app, even live keys you "think" are public | — | Server-side flow only |
| Future: Maps / 3rd-party API keys | `local.properties` → `BuildConfig` is fine **if** the key is restricted by application ID + SHA-1 in the provider console | — | Always restrict on the provider side |
| **Release keystore (`.jks`)** | Local disk + **encrypted Google Drive + USB backup** | You only | **Losing this = you cannot update the app on Play Store, ever.** Back up day-zero. |
| Keystore password + key alias password | Password manager (Bitwarden free is enough) | You only | Never in any file, never in any chat, never in git |
| `google-services.json` (when Firebase lands) | Per-environment, gitignored | — | Add to `.gitignore` **before** you download it |

### Concrete precautions to take NOW so you don't forget later

- **Before you generate the release keystore**, add the `.gitignore` entries from §2 item 3. Verify with `git check-ignore -v your-key.jks` after creating it.
- **Generate the keystore once, then never lose it.** Steps:
  1. `keytool -genkeypair -v -keystore astranavi-release.jks -alias astranavi -keyalg RSA -keysize 2048 -validity 10000`
  2. Pick a strong password; store **both** the keystore password and the key alias password in your password manager under one entry titled `AstraNavi — Release Keystore`.
  3. Copy `astranavi-release.jks` to: (a) encrypted Google Drive folder, (b) a physical USB stick stored separately, (c) a second password manager attachment if it supports files.
  4. Add a `keystore.properties` (also gitignored) in the project root with `storeFile`, `storePassword`, `keyAlias`, `keyPassword`, and reference it from `app/build.gradle.kts`.
- **Enroll in Google Play App Signing** when uploading the first AAB — Google holds the production signing key, you only need to keep your upload key safe. This is the modern default and the safety net if you ever lose the upload key.
- **Never paste `local.properties`, `keystore.properties`, or any token into chat with an AI assistant, into a screenshot, or into a public issue tracker.** If you must share for debugging, redact: replace the value with `<REDACTED>`.
- **Rotate the JWT signing secret on the backend** if a refresh token ever leaks. The app side just needs `clearSession()` (already implemented in `SessionManager.kt:199`).

---

## 4. Pre-release checklist (run every time you build an AAB for upload)

Tick **all** of these before clicking upload. None of these are optional.

**Code**
- [ ] No `TODO` / `FIXME` in critical paths (auth, payment, chart calculation).
- [ ] No hardcoded user-facing strings in Kotlin — all in `strings.xml` (lint already catches missing translations ✅).
- [ ] No hardcoded URLs or keys outside `BuildConfig`.
- [ ] No test accounts / dummy data accessible from the UI.
- [ ] HTTP logging interceptor is `Level.NONE` in release (today: hardcoded NONE ✅; after fix #8: `BuildConfig.DEBUG`-gated).
- [ ] No `Log.d` / `Log.i` / `println` in auth, token-refresh, or chart-calculation code paths.

**Build**
- [ ] `versionCode` incremented vs the previous Play Store upload.
- [ ] `versionName` updated (`1.0.0` → `1.0.1` etc.).
- [ ] `./gradlew assembleRelease` succeeds **with the real keystore**, not the debug key.
- [ ] Release APK/AAB installed on a **physical device** and the golden paths (login, dashboard, chat, kundli, paywall) all work.
- [ ] R8 didn't strip anything needed — check every screen, especially serialization models and Composables.

**Secrets / Manifest**
- [ ] `usesCleartextTraffic="false"` still in Manifest ✅.
- [ ] `allowBackup="false"` still in Manifest ✅.
- [ ] Access + refresh tokens encrypted at rest (after P0 #1 lands).
- [ ] `RELEASE_API_KEY` (not the debug one) is what got compiled in.
- [ ] No `*.jks`, `keystore.properties`, `local.properties`, `google-services.json` in `git ls-files`.

**Crash / monitoring (after P1 #4 lands)**
- [ ] Crashlytics initialized and force-test crash from a debug build appears in the Firebase console.
- [ ] `setUserId` and at least one `setCustomKey` (current screen) are set on app start.

**Play Store**
- [ ] Signed with the **release upload key**, not debug.
- [ ] Release notes written (per-locale where possible).
- [ ] Privacy policy URL is current; data-safety form matches what the app actually collects (location at birth, DOB, email).

---

## 5. Architecture invariants (do not violate)

Any AI agent or contributor working in this repo: **these are non-negotiable for AstraNavi.**

1. **Serialization = kotlinx.serialization only.** Never suggest Gson, Moshi, or Jackson. (Project memory rule.)
2. **No API calls inside `@Composable` functions.** Composables read state from a `ViewModel`. ViewModels call repositories. Repositories call `RetrofitClient.instance`.
3. **No blocking calls on `Dispatchers.Main`.** All I/O goes through `withContext(Dispatchers.IO)` or `viewModelScope.launch`.
4. **All API responses must be modeled as a state envelope** (Loading / Success / Error / Empty). Never show a blank screen while data is loading.
5. **All `LazyColumn` / `LazyRow` `items()` calls must supply a stable `key`** to avoid recomposition storms.
6. **All images load via Coil's `AsyncImage`** with `placeholder` and `error` painters. No `BitmapFactory.decodeStream`. No manual image caches.
7. **`RetrofitClient` is initialized exactly once**, from the `Application` class (after P1 #5 lands), not from `MainActivity`.
8. **Tokens are written and read only via `SessionManager`.** No other class touches the `ACCESS_TOKEN` / `REFRESH_TOKEN` DataStore keys directly.
9. **Permissions are requested at the moment of use**, not at app launch. Currently only `INTERNET` is declared — add a new `<uses-permission>` only when a feature actually needs it, and add the runtime request alongside.
10. **`networkSecurityConfig` stays HTTPS-only.** Do not add a `<domain cleartextTrafficPermitted="true">` rule for any reason. If a third-party SDK demands cleartext, find a different SDK.

---

## 6. Active TODO list (work top-down)

Update this section as you go. Format: `[ ]` → `[x] (YYYY-MM-DD: short note)`.

### P0 — before any external tester sees the app
- [x] (2026-05-28) Encrypt `ACCESS_TOKEN` + `REFRESH_TOKEN` at rest. Implemented via `androidx.security:security-crypto` (`EncryptedSharedPreferences`, file `secure_tokens`, AES256_SIV keys + AES256_GCM values, MasterKey in Android Keystore). `SessionManager.migrateLegacyTokensIfPresent()` moves any pre-existing plaintext tokens from DataStore on first launch; called from `MainActivity.onCreate` before `RetrofitClient.init`. Public `accessToken`/`refreshToken` Flow API unchanged so `RetrofitClient` and all callers are untouched.
- [ ] Add release `signingConfig` scaffolding in `app/build.gradle.kts` reading from `keystore.properties` (file itself created later when you generate the keystore).
- [x] (2026-05-28) Added `*.jks`, `*.keystore`, `keystore.properties`, `google-services.json`, `GoogleService-Info.plist`, `.env`, `.env.*`, `app/release/` to `.gitignore`.
- [ ] Create `develop` branch; switch daily work there; tag the current `main` as `v0.1.0-pre`.

### P1 — before public launch
- [ ] Add Firebase Crashlytics (or Sentry) with `setUserId` + `screen` custom key.
- [ ] Create `AstraNaviApp : Application`, move `RetrofitClient.init(sessionManager)` into it, register a `Thread.setDefaultUncaughtExceptionHandler` that records to Crashlytics.
- [ ] Split `API_BASE_URL` and `API_KEY` into `DEBUG_*` / `RELEASE_*` via `buildTypes` `buildConfigField`. Remove the hard-coded URL from `RetrofitClient.kt:30`.
- [ ] Generate the release keystore, back it up (encrypted Drive + USB + password-manager attachment), enroll in Play App Signing.
- [ ] Write release-build smoke-test script: install AAB → walk golden paths → confirm Crashlytics test crash appears.

### P2 — opportunistic
- [ ] Gate HTTP logging on `BuildConfig.DEBUG` (or delete the dead `NONE` interceptor).
- [ ] Decide on Hilt vs hand-rolled DI; if Hilt, migrate `ViewModelFactory` and populate `di/`.
- [ ] Bump `targetSdk` to 36 per `targetsdk36.md` before Google's deadline.
- [ ] Empty `di/` directory: populate or delete.

---

## 7. How an AI agent should use this file

When asked to make changes to AstraNavi:

1. **Read this file first.** It overrides generic best-practice advice from training data when the two conflict.
2. Specifically, **never suggest Gson** — see invariant #1.
3. **Never add a permission** the app doesn't already use without confirming with the human owner first — Play Store rejection risk (checklist Phase 4).
4. **Never weaken** `usesCleartextTraffic`, `allowBackup`, `networkSecurityConfig`, or the `data_extraction_rules` exclusions.
5. **Never write tokens or other credentials** to plain `SharedPreferences` or plain DataStore. Once P0 #1 lands, route them through the encrypted path only.
6. **Never commit** files matching the patterns in §3's "precautions" list. If `git status` shows one, stop and ask.
7. When making non-trivial changes, **append a dated note to §6** updating the relevant TODO, or open a new one if you discovered a new gap.
