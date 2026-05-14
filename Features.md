# AstraNavi – Complete Feature & Product Extraction Document

---

## 1. LOGIN SCREEN

**Purpose**
Allows existing users to authenticate via email and password. Also provides a toggle to switch to the Registration screen for new users.

**User Flow**
1. User opens the app → Login screen is shown (if not logged in).
2. User enters email and password.
3. Password field has a toggle icon to show/hide password.
4. User taps "Log In" → API call to `api/login` is made.
5. On success, session tokens are stored in DataStore via `SessionManager.saveSession()`, and the user is redirected to the Dashboard (or Profile completion screen if profile is incomplete).
6. If the user does not have an account, they tap "Don't have an account? Register" → switches to Registration screen.

**Inputs**
- Email (text)
- Password (secure text)
- "Log In" button tap

**Outputs**
- On success: Navigates to Dashboard or Profile completion screen.
- On failure: Displays error message below the password field.
- Loading indicator while authenticating.

**UI/UX Details**
- Email and password `OutlinedTextField`s with validation.
- Password field has a trailing icon to toggle visibility.
- "Log In" button is full-width, enabled only when both fields are non-empty.
- Loading state shows a `CircularProgressIndicator` and "Logging in..." text.
- Error message displayed in red below fields.
- "Don't have an account? Register" / "Already have an account? Log In" toggle link.

**AI Involvement**
None.

**Astrology Logic**
None.

**Retention/Engagement Purpose**
- Secure authentication ensures user data privacy.
- Quick login encourages repeat usage.
- Registration link enables new user acquisition.

**Technical Architecture**
- `LoginViewModel`: Manages email, password, loading state, error messages, and login action.
- `AuthRepository.login()`: Calls `ApiService.login()`.
- `ApiService.login()`: POST to `api/login` with `LoginRequest`.
- `SessionManager.saveSession()`: Stores user session data in DataStore.
- `RetrofitClient`: Handles authenticated HTTP requests with token refresh.

---

## 2. REGISTRATION SCREEN

**Purpose**
Allows new users to create an account by providing email and password, with confirmation password validation.

**User Flow**
1. User taps "Don't have an account? Register" from the Login screen.
2. User enters email, password, and confirm password.
3. User taps "Register" → API call to `api/register` is made.
4. On success, session tokens are stored via `SessionManager.saveSession()`, and user is redirected to the Profile screen for completion.
5. On failure, error message is displayed.

**Inputs**
- Email (text)
- Password (secure text)
- Confirm Password (secure text)
- "Register" button tap

**Outputs**
- On success: Navigates to Profile completion screen.
- On failure: Displays error message (e.g., "Passwords do not match", server validation errors).
- Loading indicator while registering.

**UI/UX Details**
- Three `OutlinedTextField`s: Email, Password, Confirm Password.
- Password fields have toggle visibility icons.
- "Register" button is full-width, enabled only when email and both passwords are non-empty.
- Confirm password must match password; otherwise, error is shown.
- Loading state shows a `CircularProgressIndicator` and "Registering..." text.
- Error message displayed in red below fields.
- "Already have an account? Log In" link to switch back to Login.

**AI Involvement**
None.

**Astrology Logic**
None.

**Retention/Engagement Purpose**
- Smooth onboarding flow encourages new user sign-up.
- Immediate profile completion prompt ensures user data is captured for personalized experience.

**Technical Architecture**
- `RegistrationViewModel`: Manages email, password, confirm password, loading state, error messages, and registration action.
- `AuthRepository.register()`: Calls `ApiService.register()`.
- `ApiService.register()`: POST to `api/register` with `RegisterRequest`.
- `SessionManager.saveSession()`: Stores user session data in DataStore.
- Password validation: Checks if password and confirm password match before making API call.
- Error parsing: Parses 422 validation errors from JSON response body.

---

## 3. PROFILE SCREEN

**Purpose**
Allows users to view and update their personal information, including name, date of birth, time of birth, place of birth, phone number, gender, marital status, occupation, and language. Also provides options to delete the account or log out.

**User Flow**
1. User navigates to Profile screen (from Dashboard hamburger menu or after registration).
2. User's current profile data is loaded from API.
3. User can edit fields in Personal Details and Birth Information sections.
4. User taps "Save Profile Changes" → API call to update profile.
5. On success, a snackbar confirms the update. If profile becomes complete (DOB, TOB, POB filled), user can navigate to Dashboard.
6. User can also delete their account permanently via a confirmation dialog.

**Inputs**
- Name, Date of Birth, Time of Birth, Place of Birth, Phone Number, Gender, Marital Status, Occupation, Language
- "Save Profile Changes" button tap
- "Delete Account Permanently" button tap
- "Go to Dashboard" button tap (if profile is complete)

**Outputs**
- Updated profile data saved via API.
- Snackbar confirmation message.
- Navigation to Dashboard if profile is complete.
- Account deletion confirmation dialog.

**UI/UX Details**
- User Header Card showing email and "Account Status: Verified".
- Two sections: Personal Details and Birth Information, each with an icon and title.
- `OutlinedTextField`s for text inputs.
- `ExposedDropdownMenuBox` for dropdown selections (Gender, Marital Status, Occupation, Language).
- `AstroDatePickerField` for date of birth with date picker dialog.
- `AstroTimePickerField` for time of birth with HH:MM format and auto-colon insertion.
- "Save Profile Changes" button shows a loading spinner when updating.
- "Go to Dashboard" button visible only when profile is complete.
- "Delete Account Permanently" button with confirmation `AlertDialog`.

**AI Involvement**
None.

**Astrology Logic**
None.

**Retention/Engagement Purpose**
- Personalization: User data enables personalized horoscopes, forecasts, and match analyses.
- Profile completion acts as a gate to the main app experience, increasing engagement.
- Account management options (delete, logout) provide user control and trust.

**Technical Architecture**
- `ProfileViewModel`: Manages UI state (Loading, Success, Error), user data, update actions, and deletion.
- `AuthRepository.getProfile()`: GET request to `api/user/profile`.
- `AuthRepository.updateProfile()`: PUT request to `api/user/profile` with `ProfileUpdateRequest`.
- `AuthRepository.deleteUser()`: DELETE request to `api/user`.
- `SessionManager`: Stores and updates user session data in DataStore.
- `ErrorSanitizer`: Sanitizes error messages from network exceptions.
- Profile completion check: Verifies that DOB, TOB, and POB are all non-blank.

---

## 4. DASHBOARD SCREEN

**Purpose**
The main landing screen after login. Provides a comprehensive overview of the user's daily horoscope, cosmic energy, and quick access to key features.

**User Flow**
1. User logs in or completes profile → Dashboard loads.
2. Dashboard fetches and displays daily horoscope, forecast, Kundli preview, and recent consultations.
3. User can interact with various sections: read horoscope, view forecast, explore Kundli, see consultations.

**Inputs**
- User session data (email, tokens)
- API responses for horoscope, forecast, Kundli preview, consultations

**Outputs**
- Visual dashboard with horoscope, cosmic score, forecast, Kundli preview, and consultation teaser.
- Navigation to other screens (Forecast, Kundli, Consult, Chat, etc.).

**UI/UX Details**
- Cosmic Header with "COSMIC NAVIGATION" and "AstraNavi" title.
- Particle background animation for cosmic feel.
- Streak bar showing consecutive app usage days.
- Horoscope section: Displays greeting, user name, rising sign, and "PERSONALIZED" badge.
- Identity Badges: Moon and Sun sign badges with blurred background images.
- Active Guidance Card: Shows current time trigger with advice.
- Lucky Stats Row: Displays lucky color and number.
- Tip Section: Cosmic tip with lightbulb icon.
- "ASK NAVI" button for quick AI chat.
- Orbit System: Animated orbital rings with life area scores (Love, Health, Career, Finance).
- Score Core: Central score display with mood and energy.
- Home Popup Overlay: Detailed view when tapping an orbit item.
- Weekly Forecast Section: Graph showing weekly scores.
- Kundli Peek Card: Mini birth chart with current Mahadasha.
- Consult Teaser Card: Last consultation with "ASK AGAIN" button.

**AI Involvement**
- "ASK NAVI" button triggers AI chat for cosmic guidance.
- Personalized horoscope and score based on user's birth data.

**Astrology Logic**
- Horoscope generation based on user's moon sign.
- Score calculation across life areas.
- Dasha period display (Mahadasha, Antardasha).
- Transit and aspect analysis.

**Retention/Engagement Purpose**
- Comprehensive overview keeps users engaged.
- Daily horoscope encourages daily visits.
- Streak bar incentivizes consistent usage.
- Quick access to AI chat and consultations.

**Technical Architecture**
- `DashboardViewModel`: Manages dashboard state (Loading, Success, Error).
- `DashboardRepository`: Fetches horoscope, forecast, Kundli preview, consultations.
- `AstrologyRepository`: Fetches Kundli analysis and consultations.
- `SessionManager`: Provides user session data.
- `RetrofitClient`: Handles API requests.
- State management: `mutableStateOf` for reactive UI updates.
- Coroutines: `viewModelScope.launch` for async operations.
- DataStore: Local storage for session tokens and user data.

---

## 5. FORECAST SCREEN

**Purpose**
Provides detailed daily, weekly, and area-specific forecasts based on Vedic astrology.

**User Flow**
1. User selects an area (General, Love, Career, Health, Finance) from the segmented control.
2. Forecast data is loaded for the selected area.
3. User views today's snapshot, weekly trend graph, and detailed day cards.
4. User can tap a day to see detailed forecast.

**Inputs**
- Selected area (General, Love, Career, Health, Finance)
- Date selection for detailed view

**Outputs**
- Forecast data displayed in cards and graphs.
- Detailed day view with score, mood, lucky stats, alerts, and transits.

**UI/UX Details**
- Cosmic Area Tabs: Segmented control with area-specific colors and emojis.
- Today Snapshot Card: Large score arc, mood, dominant planet meaning, and micro stats (Adjust, Lucky, Dasha).
- Weekly Forecast Graph: Line graph with smooth curves, today dot with pulse animation, day labels.
- Detailed Forecast Day Card: Expandable card with score, date, mood, lucky stats, alerts, and transits.
- Alert Row: Warning or positive alerts with color coding.
- Transit Row: Planet transits with sign and house info.

**AI Involvement**
None. Forecast is generated server-side based on Vedic algorithms.

**Astrology Logic**
- Forecast calculation based on planetary transits and user's birth chart.
- Score calculation: Daily score based on planetary positions.
- Mood and dominant planet determination.
- Lucky color and number generation.
- Transit interpretation: Planetary movements relative to natal chart.

**Retention/Engagement Purpose**
- Area-specific forecasts increase relevance.
- Weekly graph encourages regular check-ins.
- Detailed alerts and transits provide actionable insights.

**Technical Architecture**
- `ForecastViewModel`: Manages forecast state (Loading, Success, Error).
- `DashboardRepository.getForecast()`: GET request to `api/forecast/{area}`.
- `ForecastResponse`: Data model for forecast with daily scores, transits, alerts.
- State management: `mutableStateOf` for area selection and date.
- Coroutines: `viewModelScope.launch` for async operations.

---

## 6. KUNDLI SCREEN

**Purpose**
Provides a detailed Vedic birth chart analysis including ascendant, dasha periods, planet strengths, house analysis, transits, and key themes.

**User Flow**
1. User navigates to Kundli screen.
2. Kundli data is fetched via API.
3. User views hero chart with animated reveal, ascendant info, dasha timeline, planet gallery, strength ranking, house analysis, transits, and key themes.
4. User can tap on houses for detailed view with expansion animation.

**Inputs**
- User session data (email, tokens) for chart generation.

**Outputs**
- Birth chart SVG rendered via API.
- Detailed Kundli analysis with multiple sections.

**UI/UX Details**
- Zone1Hero: Birth chart SVG with 2-stage reveal (Cosmic Reveal, Orbital Assembly), parallax effect, identity pills (Lagna, Mahadasha, Antardasha).
- Zone2Ascendant: Ascendant sign, degree, nakshatra, and interpretation.
- Zone3Dasha: Current Dasha period with Mahadasha, Antardasha, Pratyantardasha timeline items.
- Zone4Planets: Horizontal pager with planet gallery cards showing dignity, retrograde, combustion, strength, nakshatra, aspects.
- Zone5Strengths: Planet strength ranking with animated progress bars.
- Zone6HouseAnalysis: 4x3 grid of houses with scores, strongest/weakest indicators, tap to expand.
- House Expansion Overlay: Animated expansion from circle to card showing house details (lord, occupants, aspects).
- Zone9Transits: Horizontal pager with transit planet cards showing current sign and house.
- Zone10Timeline: Dasha timeline with horizontal pager, previous/current/upcoming tags.
- Zone8KeyThemes: List of key insights from backend.
- Kundli Skeleton: Shimmer loading effect.

**AI Involvement**
None. Kundli analysis is generated server-side.

**Astrology Logic**
- Chart calculation: Ascendant, planetary positions, house cusps.
- Dasha system: Vimshottari Dasha with Mahadasha, Antardasha, Pratyantardasha.
- Shadbala: Planetary strength calculation.
- Ashtakavarga: House score analysis.
- Transit analysis: Current planetary transits relative to natal chart.
- Key themes: Backend-generated insights.

**Retention/Engagement Purpose**
- Comprehensive birth chart analysis encourages deep exploration.
- Animated reveal and interactive elements increase engagement.
- House expansion provides detailed insights.

**Technical Architecture**
- `KundliViewModel`: Manages Kundli state (Loading, Success, Error), house UI state.
- `AstrologyRepository.analyzeFull()`: POST request to `api/analyze-full`.
- `AnalyzeFullResponse`: Data model with identity, chart summary, ascendant, houses, planets, dasha, key themes, ashtakavarga, planet strength ranking, transits.
- `HouseData` and `CircleInfo` data classes for house analysis.
- Coroutines: `viewModelScope.launch` for async operations.
- DataStore: User session data.
- SVG rendering: Birth chart via `AsyncImage` from API.

---

## 7. MATCH SCREEN

**Purpose**
Performs Vedic compatibility analysis (36 Guna Melaap) between two individuals for marriage or relationship compatibility.

**User Flow**
1. User enters details for Person 1 (Name, DOB, TOB, POB, Gender) and Person 2 (same fields).
2. User taps "Calculate Compatibility" → API call to `api/match` is made.
3. Compatibility result is displayed with score, Guna breakdown, and Mangal Dosha analysis.

**Inputs**
- Person 1: Name, Date of Birth, Time of Birth, Place of Birth, Gender
- Person 2: Same fields
- "Calculate Compatibility" button tap

**Outputs**
- Compatibility score (out of 36).
- Guna breakdown (8 Kootas).
- Mangal Dosha status and details.
- AI narrative summary.

**UI/UX Details**
- MatchInputForm: Two sections (Person 1, Person 2) with input fields.
- `AstroDatePickerField` for DOB.
- `AstroTimePickerField` for TOB with HH:MM format.
- Gender selection via FilterChip (Male/Female).
- Sync icon between the two person sections.
- MatchResultView: Score ring with color coding (Green ≥25, Yellow ≥18, Red <18).
- Guna breakdown cards with progress bars.
- Mangal Dosha panel with status and description.
- "Analyze Another Match" button to reset.

**AI Involvement**
- AI narrative summary generated server-side.

**Astrology Logic**
- 36 Guna Melaap: Compatibility scoring based on 8 Kootas.
- Mangal Dosha: Mars influence analysis with cancellation detection.
- Score calculation: Based on planetary positions and house lords.

**Retention/Engagement Purpose**
- Match analysis is a key feature for marriage-related queries.
- Shareable results encourage social engagement.
- Quick analysis encourages repeated use.

**Technical Architecture**
- `MatchViewModel`: Manages match state (Idle, Loading, Success, Error).
- `AstrologyRepository.calculateMatch()`: POST request to `api/match` with `MatchRequest`.
- `MatchResponse`: Data model with Ashtakoot, Mangal Dosha, AI narrative.
- `PersonDetail` and `MatchRequest` data classes.
- Coroutines: `viewModelScope.launch` for async operations.

---

## 8. MATCH HISTORY SCREEN

**Purpose**
Displays a list of past compatibility analyses with the ability to view details and delete records.

**User Flow**
1. User navigates to Match History.
2. History is fetched and displayed as a list.
3. User can tap a record to expand and view details (score, Guna breakdown, Mangal Dosha).
4. User can delete a record.

**Inputs**
- User session data.

**Outputs**
- List of past match records.
- Detailed view on tap.
- Deletion confirmation.

**UI/UX Details**
- HistoryRecordCard: Compact view with score color, names, date, and score.
- Expansion: Detailed view with AI narrative, Guna breakdown, and Mangal Dosha.
- Delete button at the bottom of expanded card.
- Loading skeleton and empty state.

**AI Involvement**
None. Data is fetched from API.

**Astrology Logic**
- Display of past match results with Guna scores and Mangal Dosha.

**Retention/Engagement Purpose**
- Historical records encourage revisiting past analyses.
- Detailed breakdowns add value.

**Technical Architecture**
- `MatchHistoryViewModel`: Manages history state (Loading, Success, Error), expanded details, deletion.
- `AstrologyRepository.getMatchHistory()`: GET request to `api/match/history`.
- `AstrologyRepository.getMatchResult()`: GET request to `api/match/{matchId}`.
- `AstrologyRepository.deleteMatch()`: DELETE request to `api/match/{matchId}`.
- `MutableStateMap` for tracking expanded details and loading states.

---

## 9. CHAT SCREEN

**Purpose**
AI-powered chat interface for personalized astrological guidance.

**User Flow**
1. User navigates to Chat screen.
2. Welcome messages are displayed.
3. User can type questions or select from suggested options.
4. AI responds with streaming text.
5. User can rate responses (thumbs up/down) and copy messages.

**Inputs**
- Text input from user.
- Suggested question taps.
- Rating taps.

**Outputs**
- Streaming AI responses.
- Rating updates.
- Chat history.

**UI/UX Details**
- ChatInputBar: Text field with send button, suggestion chips.
- ChatConversationList: Bubbles for user and AI messages with timestamps.
- ThinkingIndicator: Animated dots while AI is responding.
- Rating buttons (thumbs up/down) on AI messages.
- Copy button for messages.
- Markdown rendering for AI responses (headers, bold, italic, bullet points).
- History view with load more and delete options.

**AI Involvement**
- Core feature: AI chatbot for astrological guidance.
- Streaming responses via SSE (Server-Sent Events).
- User ratings for feedback.

**Astrology Logic**
None. AI generates responses based on user input and context.

**Retention/Engagement Purpose**
- AI chat encourages longer sessions.
- Personalization and conversation history increase engagement.
- Ratings and feedback improve AI quality.

**Technical Architecture**
- `ChatViewModel`: Manages chat state (Idle, LoadingHistory, LoadingMessages, ActiveChat, Error).
- `AstrologyRepository.sendMessage()`: POST request to `api/chats/{chatId}/messages` with streaming response.
- `AstrologyRepository.createChat()`: POST request to `api/chats`.
- `AstrologyRepository.listChats()`: GET request to `api/chats`.
- `AstrologyRepository.getChatHistory()`: GET request to `api/chats/{chatId}`.
- `AstrologyRepository.deleteChat()`: DELETE request to `api/chats/{chatId}`.
- `AstrologyRepository.rateMessage()`: PUT request to `api/chats/{chatId}/messages/{msgId}/rate`.
- `ChatMessage`, `ChatSummary`, `ChatDetailResponse` data models.
- `renderMarkdown()` function for parsing AI responses.
- SSE streaming via `responseBody.byteStream()`.

---

## 10. CONSULT SCREEN

**Purpose**
Guided consultation flow with step-by-step interface for birth details, category selection, question selection, and AI-generated results.

**User Flow**
1. User taps "Consult" from navigation.
2. Step 1: Birth Details – User enters DOB, TOB, POB.
3. Step 2: Category Selection – User selects a life domain.
4. Step 3: Subcategory Selection – User selects a focus area.
5. Step 4: Question Selection – User picks a specific question or asks custom.
6. Step 5: Result – AI generates and displays the consultation result.

**Inputs**
- Birth details (DOB, TOB, POB).
- Category, subcategory, and question selections.
- Custom note (optional).
- Response tone selection (Warm, Direct, Spiritual).

**Outputs**
- Streaming AI-generated consultation result.
- Loading skeleton with animated text.

**UI/UX Details**
- CosmicStepTracker: 5-step progress indicator (Birth, Domain, Focus, Query, Insight).
- AnimatedContent for step transitions.
- BirthDetailsStep: Date picker, time field, place field, preferences dropdown.
- CategoryStep: Cards with floating animation for categories.
- SubCategoryStep: Accordion-style expanding cards.
- QuestionStep: Cards with gold stripe, tone chips, custom question button.
- ResultStep: Sacred document style card with streaming text, loading animation with orbiting dots.
- Consult Skeleton: Shimmer loading.

**AI Involvement**
- Core feature: AI generates consultation based on birth data and selected question.
- Streaming responses via SSE.
- Tone selection influences AI response style.

**Astrology Logic**
- Consultation tree is fetched from backend based on user's age, gender, marital status, occupation.
- Birth data is used for chart context.

**Retention/Engagement Purpose**
- Guided flow encourages completion.
- Personalized consultations increase perceived value.
- Step-by-step progress keeps users engaged.

**Technical Architecture**
- `ConsultViewModel`: Manages step state, birth details, selections, and consultation generation.
- `AstrologyRepository.getConsultTree()`: GET request to `api/consult/tree`.
- `AstrologyRepository.generateConsultation()`: POST request to `api/consult` with streaming response.
- `AstrologyRepository.getConsultHistory()`: GET request to `api/consult/history`.
- `ConsultTree`, `Category`, `SubCategory` data models.
- `ConsultStep` sealed class for step management.

---

## 11. CONSULT HISTORY SCREEN

**Purpose**
Lists past consultations with expandable details.

**User Flow**
1. User navigates to Consult History.
2. List of past consultations is fetched and displayed.
3. User can tap to expand and view details (category, question, insight).

**Inputs**
- User session data.

**Outputs**
- List of past consultations.
- Detailed view on tap.

**UI/UX Details**
- ConsultHistoryCard: Compact view with category icon, question, date.
- Expansion: Detailed view with insight text.
- Loading skeleton and empty state.

**AI Involvement**
None. Data is fetched from API.

**Astrology Logic**
None.

**Retention/Engagement Purpose**
- Historical records encourage revisiting past consultations.

**Technical Architecture**
- `ConsultHistoryViewModel`: Manages history state and list.
- `AstrologyRepository.getConsultHistory()`: GET request to `api/consult/history`.
- `ConsultRecord` data model.

---

## 12. ASTROLOGERS SCREEN

**Purpose**
Lists available astrologers with their profiles, ratings, and pricing for booking sessions.

**User Flow**
1. User navigates to Astrologers screen.
2. List of astrologers is displayed (hardcoded data).
3. User can view details and book a session (placeholder).

**Inputs**
- None (hardcoded data).

**Outputs**
- List of astrologer cards.

**UI/UX Details**
- AstrologerCard: Avatar, name, rating, reviews, specialties, experience, price, online status indicator.
- Book Session button (non-functional placeholder).
- Skeleton loading shimmer.

**AI Involvement**
None.

**Astrology Logic**
None.

**Retention/Engagement Purpose**
- Provides access to expert consultations.
- Online status encourages real-time engagement.

**Technical Architecture**
- `AstrologersViewModel`: Manages astrologer list and loading state.
- `Astrologer` data model.
- Hardcoded list of astrologers.

---

## 13. KNOWLEDGE HUB SCREEN

**Purpose**
Serves as an encyclopedia entry point for Vedic astrology topics: Rashis, Planets, Nakshatras, Houses, and Yogas.

**User Flow**
1. User navigates to Knowledge Hub.
2. User selects a topic to explore.

**Inputs**
- None.

**Outputs**
- Navigation to respective topic screens.

**UI/UX Details**
- KnowledgeCategoryCard: Icons and descriptions for each topic.

**AI Involvement**
None.

**Astrology Logic**
None.

**Retention/Engagement Purpose**
- Educational content increases user knowledge and engagement.

**Technical Architecture**
- Static UI with navigation intents.

---

## 14. PLANET SCREEN

**Purpose**
Encyclopedia of planets with detailed information on each planet's nature, indications, and karakatvas.

**User Flow**
1. User selects a planet from the list.
2. Detailed view with nature, identity grid, indications, and karakatvas is displayed.

**Inputs**
- None (static data).

**Outputs**
- Planet details.

**UI/UX Details**
- PlanetList: Cards with planet icons.
- PlanetDetail: Image, info chips, core nature, identity grid, indications, karakatvas.

**AI Involvement**
None.

**Astrology Logic**
Static data.

**Retention/Engagement Purpose**
- Educational.

**Technical Architecture**
- `PlanetData` class with static list.

---

## 15. NAKSHATRA SCREEN

**Purpose**
Encyclopedia of 27 Nakshatras with detailed information.

**User Flow**
1. User selects a Nakshatra from the list.
2. Detailed view with Pada breakdown is displayed.

**Inputs**
- None.

**Outputs**
- Nakshatra details.

**UI/UX Details**
- NakshatraList: Cards.
- NakshatraDetail: Top bar color changes based on ruler, HorizontalPager for Padas.

**AI Involvement**
None.

**Astrology Logic**
Static data.

**Retention/Engagement Purpose**
- Educational.

**Technical Architecture**
- `NakshatraData` class with static list.

---

## 16. HOUSE SCREEN

**Purpose**
Encyclopedia of 12 houses with detailed information.

**User Flow**
1. User selects a house from the grid.
2. Detailed view with significations is displayed.

**Inputs**
- None.

**Outputs**
- House details.

**UI/UX Details**
- HouseGrid: 2-column grid.
- HouseDetail: Core meaning, identity grid, significations.

**AI Involvement**
None.

**Astrology Logic**
Static data.

**Retention/Engagement Purpose**
- Educational.

**Technical Architecture**
- Static `houses` list in `HouseData.kt`.

---

## 17. YOGA SCREEN

**Purpose**
Encyclopedia of important Yogas with detailed explanations.

**User Flow**
1. User selects a Yoga from the list.
2. Detailed view with classification, logic, and results is displayed.

**Inputs**
- None.

**Outputs**
- Yoga details.

**UI/UX Details**
- YogaList: Cards with icons.
- YogaDetail: Hero card, stats, logic, deep dive, results.

**AI Involvement**
None.

**Astrology Logic**
Static data.

**Retention/Engagement Purpose**
- Educational.

**Technical Architecture**
- `Yoga` data class with static `yogas` list.

---

## 18. RASHI SCREEN

**Purpose**
Encyclopedia of Rashis with general horoscope for each sign.

**User Flow**
1. User selects a Rashi from the grid.
2. Detailed view with general horoscope is displayed.

**Inputs**
- None (static data).
- API call for general horoscope.

**Outputs**
- Rashi details and horoscope.

**UI/UX Details**
- RashiGrid: 2-column grid.
- RashiDetail: Horoscope fetched via API.

**AI Involvement**
None.

**Astrology Logic**
- General horoscope fetched from API.

**Retention/Engagement Purpose**
- Educational and personalized content.

**Technical Architecture**
- `RashiData` class with static list.
- `AstrologyRepository.getGeneralHoroscope()`: GET request to `api/horoscope/{sign}`.

---

## 19. COMPLETE PRODUCT SUMMARY

**Application Name**: AstraNavi
**Package Name**: com.astranavi.app
**Platform**: Android (Kotlin, Jetpack Compose)
**Backend**: Veriscribe Analytics API (https://api.veriscribeanalytics.com/)

### Core Modules
1. **Authentication**: Login, Registration, Session Management
2. **Dashboard**: Daily Horoscope, Forecast, Kundli Preview, Consult Teaser
3. **Kundli**: Full Birth Chart Analysis
4. **Match**: Compatibility Analysis (36 Guna)
5. **Match History**: Past Analyses
6. **Chat**: AI-Powered Guidance
7. **Consult**: Guided AI Consultation
8. **Consult History**: Past Consultations
9. **Profile**: User Management
10. **Astrologers**: AI expert/persona directory or future marketplace placeholder
11. **Knowledge Hub**: Encyclopedia (Planets, Nakshatras, Houses, Yogas, Rashis)
12. **Forecast**: Area-Specific Predictions

### Technology Stack
- Language: Kotlin
- UI Framework: Jetpack Compose
- Architecture: MVVM with ViewModel, Repository Pattern
- Networking: Retrofit, OkHttp
- Local Storage: DataStore Preferences
- Image Loading: Coil
- Dependency Injection: Manual (ViewModelFactory)
- Background Processing: Kotlin Coroutines
- API Client: REST (JSON)

### Data Models (Partial)
- User, UserPreferences, ProfileUpdateRequest
- LoginRequest/Response, RegisterRequest/Response
- HoroscopeResponse, ForecastResponse, ForecastDay
- AnalyzeFullRequest/Response, ChartIdentity, ChartSummaryData
- AshtakavargaData, PlanetStrengthRank, TransitData, TransitPlanet
- AscendantData, Occupant, HouseData, PlanetData, DashaPeriod, CurrentDasha, DashaTimeline
- PersonDetail, MatchRequest, KootDetail, Ashtakoot, MangalDoshaPerson, MangalDoshaDetail, MatchResponse, MatchRecord
- ChatMessage, ChatSummary, ChatHistoryWrapper, ChatDetailResponse, ChatDetail
- ConsultRequest, ConsultRecord, ConsultTree, Category, SubCategory
- Astrologer

### Authentication System
- **Login**: Email + Password → JWT tokens (access + refresh)
- **Registration**: Email + Password → JWT tokens
- **Token Refresh**: Automatic via OkHttp Authenticator
- **Session Management**: DataStore Preferences
- **Logout**: Clears DataStore and navigates to Login

### AI Chat System
- **Provider**: Custom AI backend (via Veriscribe API)
- **Streaming**: Server-Sent Events (SSE)
- **Features**: Message ratings (thumbs up/down), chat history, delete chats, message copying
- **UI**: Markdown rendering, typing indicator, suggestion chips

### Astrology Engine
- **Chart Calculation**: Server-side (via API)
- **Dasha System**: Vimshottari Dasha (Mahadasha, Antardasha, Pratyantardasha)
- **Match Making**: 36 Guna Melaap with Mangal Dosha
- **Forecast**: Area-specific daily predictions
- **Kundli**: Full chart analysis (Ascendant, Planets, Houses, Transits, Yogas)
- **Knowledge Base**: Static data for Planets, Nakshatras, Houses, Yogas, Rashis

### Localization
- **Languages Supported**: English, Hindi (UI), with multi-language support for consultations (English, Hindi, Marathi, Gujarati, Tamil)
- **Date Formats**: DD-MM-YYYY (display), YYYY-MM-DD (API)
- **Time Format**: HH:MM

### Design System
- **Theme**: Dark/Light mode with custom semantic colors
- **Components**: Reusable Compose components (Cards, Chips, Pickers, etc.)
- **Animations**: Lottie, Compose Animations (loading, transitions)
- **Icons**: Material Design Icons

### Monetization (Not Fully Implemented)
- **Consultation Credits**: Purchase credits for AI consultations
- **Session Booking**: Book sessions with astrologers (UI present, backend not connected)
- **Premium Features**: Potential subscription for advanced analysis

### Analytics & Tracking
- **Mixpanel**: Event tracking for user behavior

### Error Handling & Validation
- **Email Validation**: Regex pattern
- **Password Validation**: Minimum 6 characters
- **API Errors**: Toast messages and logging
- **Network Errors**: Retry mechanisms and user feedback

---

## 20. CORE PRODUCT IDENTITY

**Product**: AstraNavi
**Tagline**: "Your Personal Astrology Guide"
**Category**: Lifestyle/Entertainment – Astrology
**Key Value Proposition**: AI-powered personalized astrological guidance based on ancient Vedic principles

### Target Audience
- Individuals interested in astrology, horoscopes, and spiritual guidance
- Users seeking daily cosmic insights and compatibility analysis
- People looking for AI-based personalized consultations
- Astrology enthusiasts wanting detailed birth chart analysis

### Core User Needs Addressed
1. **Daily Guidance**: Personalized horoscopes and cosmic energy readings
2. **Compatibility Analysis**: Marriage/relationship compatibility (36 Guna)
3. **Birth Chart Analysis**: Comprehensive Kundli interpretation
4. **AI Consultation**: Instant answers to astrological questions
5. **AI Expert Modes**: Instant access to specialized AI guidance for career, marriage, health, finance, remedies, and timing
6. **Educational Content**: Learning about Vedic astrology concepts

### Product Philosophy
AstraNavi positions itself as a modern, AI-enhanced astrology platform that combines ancient Vedic wisdom with cutting-edge technology. The product aims to provide personalized, accurate, and engaging astrological experiences while maintaining a premium, mystical aesthetic.

---

## 21. MAIN DIFFERENTIATORS

1. **AI-Powered Chatbot (Navi)**: Unlike static horoscope apps, AstraNavi offers an AI chatbot that provides real-time, personalized astrological guidance through natural conversations.

2. **Vedic Astrology Specialization**: Focuses specifically on Vedic (Indian) astrology, which is more detailed and personalized compared to Western astrology, with features like Dasha systems, Nakshatras, and Ashtakavarga.

3. **Comprehensive Birth Chart Analysis**: Provides detailed Kundli analysis including planetary positions, Dasha periods, transits, and predictive insights.

4. **36 Guna Compatibility System**: Offers in-depth marriage compatibility analysis beyond simple zodiac matching.

5. **AI-Generated Horoscopes**: Uses AI to create personalized daily horoscopes based on individual birth charts rather than generic sun-sign horoscopes.

6. **AI Expert Modes**: Provides specialized AI astrologer personas and guided consultation flows instead of relying on human astrologer availability.

7. **Multi-Language Support**: Supports Hindi and English for consultations, with potential for more Indian languages.

8. **Mystical UI/UX**: Combines modern design with cosmic/astrological aesthetics, creating an immersive experience.

---

## 22. USER EXPERIENCE PHILOSOPHY

1. **Personalization First**: Every feature is tailored to the individual user's birth data, creating a highly personalized experience.

2. **Guided Journeys**: Complex features like Kundli analysis and consultations are broken down into guided, step-by-step flows.

3. **Mystical Immersion**: The UI uses cosmic themes, particle effects, and smooth animations to create an immersive astrological experience.

4. **AI-First Interaction**: The AI chatbot serves as the primary interaction model, making complex astrological concepts accessible.

5. **Progressive Disclosure**: Information is revealed progressively, from high-level summaries to detailed analyses.

6. **Emotional Connection**: The app aims to create an emotional bond through personalized cosmic insights and daily guidance.

7. **Trust & Authenticity**: Features like verified astrologer profiles and detailed birth chart analysis build trust in the platform's authenticity.

8. **Engagement Loops**: Daily horoscopes, streak tracking, and AI interactions create habitual engagement patterns.

---

## 23. AI STRATEGY

### Primary AI Features
1. **AI Chatbot (Navi)**: 
   - Natural language processing for astrological queries
   - Personalized responses based on user's birth chart
   - Streaming responses for real-time feel
   - Context retention within conversations

2. **AI-Generated Horoscopes**:
   - Personalized daily horoscopes based on moon sign
   - Area-specific forecasts (love, career, health, finance)
   - Dynamic content generation based on planetary transits

3. **AI Consultation**:
   - Guided consultation flow with AI-generated insights
   - Multi-step analysis combining birth data with user's questions
   - Tone adaptation (warm, direct, spiritual)

### AI Implementation Details
- **Provider**: Custom AI backend (likely LLM-based)
- **Integration**: REST API with streaming support
- **Personalization**: User's birth data is sent with each query for context
- **Feedback Loop**: User ratings improve AI response quality

### AI Roadmap
- Expand AI capabilities for more complex astrological analysis
- Implement AI-powered compatibility analysis
- Add AI-generated remedies and suggestions
- Voice-based AI consultations

---

## 24. RETENTION SYSTEMS

### Engagement Loops
1. **Daily Horoscope**: Users return daily for personalized cosmic guidance
2. **Streak Tracking**: Visual progress indicator for consecutive app usage
3. **AI Chat**: Conversational AI encourages longer sessions
4. **Weekly Forecast**: Area-specific predictions for the week ahead

### Personalization Features
1. **Birth Chart-Based Content**: All horoscopes and forecasts are personalized
2. **User Profile**: Stores personal details for tailored experiences
3. **Consultation History**: Access to past analyses and insights
4. **Chat History**: Continuity in AI conversations

### Social Features
1. **AI Expert Modes**: Specialized AI consultation paths for different life areas
2. **Shareable Results**: Compatibility and horoscope results can be shared
3. **Community**: Potential for community features around astrology

### Monetization Hooks
1. **Free Tier**: Basic horoscopes and limited AI chat
2. **Premium Features**: Detailed analysis, full daily horoscope, advanced forecasts, family profiles, reports, and higher AI usage limits
3. **Consultation Credits**: Purchasable credits for AI chat, guided AI consultations, and one-time reports

### Data-Driven Retention
1. **User Ratings**: Improve AI and consultation quality
2. **Behavioral Analytics**: Track feature usage for personalization
3. **Feedback Mechanisms**: Continuous improvement based on user input

---

## 25. MONETIZATION SYSTEMS

### Current Monetization Decision
AstraNavi is positioned as an AI-first India product, not a human astrologer marketplace. Human astrologer booking should remain future/optional and should not be part of the launch pricing model.

### Recommended Monetization Features
1. **Three subscription tiers**: Free, Pro, Premium.
2. **Navi Credit-based AI allowance**: Monthly AI chat limits, because AI cost is variable at INR 1 per 5 credits/messages.
3. **One-time packs**: Chat message packs, guided AI consultation packs, Kundli/match/forecast reports.
4. **Pro+ daily horoscope**: Full personalized daily horoscope, daily timing, area guidance, and remedies should start at Pro.
5. **Premium expansion**: Higher chat allowance, monthly deep report, family profiles, deeper remedies, PDF/share/export, priority generation.

### Monetization Flow
1. Free users receive short daily horoscope, basic Kundli, limited chat trial, and low monthly message caps.
2. Pro and Premium users receive monthly Navi Credit allowances and Pro+ full daily horoscope.
3. One-time packs stack on top of subscriptions for users who do not want recurring billing.
4. Usage is deducted from subscription allowance first, then from paid packs.
5. No plan should promise truly unlimited AI chat until model cost is flat or much lower.

### Pricing Structure
- **Free**: INR 0, limited AI usage, short daily horoscope, basic chart access.
- **Pro**: Regular daily users; full daily horoscope, full Kundli, weekly forecast, moderate AI chat allowance.
- **Premium**: Power users and families; higher AI allowance, family profiles, monthly report, advanced remedies and exports.
- **Chat Packs**: Small one-time packs starting below competitor question pricing.
- **Guided AI Consultation Packs**: Domain-specific one-time consults for career, marriage, health, finance, timing, remedies.
- **Reports**: One-time Kundli, match, monthly forecast, and marriage decision reports.

### Technical Implementation
- Payment processing via Google Play Billing or compliant India alternative billing.
- Server-side purchase validation and receipt reconciliation.
- Entitlement service for plan, pack balances, expiry, and monthly quota resets.
- Usage ledger for every AI message, guided consult, report generation, refund, and admin grant.
- Pricing configuration served from backend so prices/allowances can change without app release.
- Transaction history and visible usage meter in the app.

---

## 26. MISSING SYSTEMS

### Major Gaps
1. **Payment Integration**: No visible payment processing UI or logic
2. **Push Notifications**: No notification system for daily horoscopes or updates
3. **Social Sharing**: No share functionality for horoscope results
4. **Remedies Section**: No specific remedies or suggestions for planetary issues
5. **Multi-User Support**: No family or multi-profile support
6. **Offline Mode**: No offline access to basic features
7. **Birth Time Rectification**: No feature for uncertain birth times
8. **Predictive Analytics**: No future prediction features beyond basic horoscopes
9. **Community Features**: No forums or community discussions
10. **Gamification**: No achievement system or rewards beyond streaks

### Minor Gaps
1. **No Settings Page**: Limited configuration options
2. **No Help Section**: No FAQ or support resources
3. **No Privacy Policy**: No privacy compliance information
4. **No Terms of Service**: No legal agreements
5. **No Accessibility Features**: Limited accessibility support
6. **No Export Options**: Cannot export birth chart or analysis
7. **No Backup**: No cloud backup of user data
8. **No Multi-Language UI**: Limited to English/Hindi only

---

## 27. WEAKNESSES

1. **Performance Issues**: Heavy animations and particle effects may cause lag on lower-end devices
2. **Loading Times**: Initial data loading can be slow due to multiple API calls
3. **Error Handling**: Limited error recovery and user feedback
4. **Incomplete Features**: Many features have placeholder or non-functional elements
5. **Limited Personalization**: AI responses may lack depth in astrological analysis
6. **UI Complexity**: Complex screen layouts may confuse new users
7. **No Offline Mode**: Requires constant internet connection
8. **Limited Compatibility**: Only available on Android
9. **Security Concerns**: Sensitive birth data transmitted to servers
10. **Scalability**: Current architecture may not handle large user bases

---

## 28. STRONGEST FEATURES

1. **AI Chatbot (Navi)**: Provides personalized, streaming astrological guidance
2. **Comprehensive Kundli Analysis**: Detailed birth chart with multiple analysis sections
3. **36 Guna Compatibility**: Thorough marriage compatibility analysis
4. **Daily Horoscope**: Personalized cosmic guidance based on birth chart
5. **Dasha System**: Accurate planetary period analysis
6. **Visual Design**: Immersive cosmic-themed UI with smooth animations
7. **Multi-Section Analysis**: Covers planets, houses, nakshatras, transits
8. **User Personalization**: Tailored content based on birth data

---

## 29. MOST INNOVATIVE FEATURES

1. **AI-Powered Astrological Chatbot**: Combines AI with Vedic astrology for personalized guidance
2. **Animated Birth Chart Reveal**: Cinematic chart presentation with particle effects
3. **Interactive House Analysis**: Touch-based expansion with smooth animations
4. **Streaming Consultation**: Real-time AI-generated consultation with typing indicators
5. **Cosmic Energy Visualization**: Orbit system representing life area energies
6. **Personalized Forecast**: Area-specific predictions based on birth chart
7. **Smart Time Picker**: Auto-formatting HH:MM input field
8. **Dynamic Theming**: Dark/Light mode with astrological color schemes

---

## 30. MOST UNDERRATED FEATURES

1. **Streak Tracking**: Simple but effective engagement motivator
2. **Knowledge Hub**: Comprehensive astrology education section
3. **Transit Analysis**: Real-time planetary movement tracking
4. **Planet Gallery**: Interactive planet exploration with detailed info
5. **Nakshatra Details**: In-depth lunar mansion analysis
6. **Yoga Encyclopedia**: Educational content on planetary combinations
7. **Consult History**: Access to past AI consultations
8. **Personalized Alerts**: Area-specific warnings and advice

---

## 31. FEATURES THAT FEEL PREMIUM

1. **AI Chatbot Interface**: Sleek, modern design with streaming responses
2. **Birth Chart Reveal**: Cinematic presentation with particle effects
3. **Interactive Orbit System**: Touch-responsive cosmic energy visualization
4. **Detailed Kundli Analysis**: Professional-grade chart interpretation
5. **Expert Astrologer Directory**: Verified profiles with ratings
6. **Personalized Forecast**: Tailored predictions with visual charts
7. **Glassmorphism UI**: Modern design language with transparency effects
8. **Smooth Animations**: High-quality transitions and interactions

---

## 32. FEATURES THAT FEEL INCOMPLETE

1. **Book Session Button**: Placeholder without actual booking functionality
2. **Payment System**: No visible payment processing
3. **Settings Page**: Minimal configuration options
4. **Help Section**: No FAQ or user support
5. **Privacy Policy**: Missing legal compliance information
6. **Multi-Language Support**: Limited language options
7. **Accessibility Features**: No screen reader support
8. **Export Options**: Cannot save or share analysis results

---

## 33. FEATURE DEPENDENCY MAP
Login/Registration │ ▼ Profile Setup │ ▼ Dashboard (requires: Horoscope, Forecast, Kundli, Consult History) │ ├──▶ Kundli (requires: AnalyzeFull API) │ ├──▶ Transits │ ├──▶ Dasha Timeline │ └──▶ House Analysis │ ├──▶ Forecast (requires: Forecast API) │ └──▶ Area-specific predictions │ ├──▶ Chat (requires: Chat API, Streaming) │ └──▶ AI Consultation │ ├──▶ Consult (requires: Consult Tree API, Streaming) │ └──▶ Consultation Result │ ├──▶ Match (requires: Match API) │ └──▶ Match History │ ├──▶ Knowledge Hub │ ├──▶ Planets │ ├──▶ Nakshatras │ ├──▶ Houses │ └──▶ Yogas │ └──▶ Astrologers (independent)


---

## 34. FULL NAVIGATION STRUCTURE
App Entry │ ├── Login/Registration │ ├── Login Screen │ └── Registration Screen │ ├── Main App (after login) │ │ │ ├── Dashboard (Home) │ │ ├── Horoscope Section │ │ ├── Forecast Section │ │ ├── Kundli Preview │ │ └── Consult Teaser │ │ │ ├── Kundli (Navigation) │ │ ├── Hero Chart │ │ ├── Ascendant Info │ │ ├── Dasha Timeline │ │ ├── Planet Gallery │ │ ├── Strength Ranking │ │ ├── House Analysis │ │ ├── Transits │ │ └── Key Themes │ │ │ ├── Forecast (Navigation) │ │ ├── Area Selection (General, Love, Career, Health, Finance) │ │ ├── Today Snapshot │ │ ├── Weekly Trend Graph │ │ └── Detailed Day View │ │ │ ├── Consult (Navigation) │ │ ├── Birth Details Step │ │ ├── Category Selection Step │ │ ├── Sub-Category Selection Step │ │ ├── Question Selection Step │ │ └── Result Step (AI Consultation) │ │ │ ├── Match (Navigation) │ │ ├── Input Form (2 Person Details) │ │ └── Result View (Score, Guna Breakdown) │ │ │ ├── Chat (Navigation) │ │ ├── Chat Interface │ │ ├── Suggested Questions │ │ └── Chat History │ │ │ ├── Knowledge Hub (Navigation) │ │ ├── Planets (Encyclopedia) │ │ ├── Nakshatras (Encyclopedia) │ │ ├── Houses (Encyclopedia) │ │ └── Yogas (Encyclopedia) │ │ │ ├── Astrologers (Navigation) │ │ └── Astrologer Profiles │ │ │ ├── Profile (Navigation) │ │ ├── Personal Information │ │ ├── Birth Information │ │ └── Account Management │ │ │ └── Settings (From Menu) │ ├── Theme Selection (Dark/Light/System) │ └── Logout


---

## 35. DATA FLOW OVERVIEW
User Interface (Compose) │ ▼ ViewModel (State Management) │ ▼ Repository (Business Logic) │ ▼ API Service (Network Calls) │ ▼ Backend Server (Veriscribe Analytics) │ ├──▶ Authentication Service │ └──▶ JWT Token Generation │ ├──▶ Astrology Engine │ ├──▶ Birth Chart Calculation │ ├──▶ Horoscope Generation │ ├──▶ Compatibility Analysis │ └──▶ Prediction Engine │ ├──▶ AI Service │ ├──▶ Natural Language Processing │ ├──▶ Context Management │ └──▶ Response Generation │ └──▶ Consultation Service ├──▶ Tree Structure └──▶ Streaming Response


### Local Data Flow
API Responses │ ▼ DataStore (Persistent Storage) │ ├──▶ Session Data (Tokens, Preferences) ├──▶ User Profile Data └──▶ Cached Analysis Data


---

## 36. DASHBOARD ECOSYSTEM BREAKDOWN

### Dashboard Components
1. **Daily Horoscope Widget**
   - Greeting message based on time of day
   - Personalized cosmic energy reading
   - Actionable advice for the day

2. **Cosmic Energy Orbiter**
   - Interactive orbit visualization
   - Life area scores (Love, Health, Career, Finance)
   - Tap-to-explore details

3. **Score Display**
   - Overall cosmic score (0-36)
   - Mood and energy indicators
   - Animated breathing effect

4. **Lucky Stats**
   - Lucky color
   - Lucky number
   - Quick-glance format

5. **Forecast Preview**
   - Weekly trend graph
   - Best/worst day indicators
   - Quick navigation to full forecast

6. **Kundli Quick Peek**
   - Mini birth chart
   - Current Dasha period
   - Link to full analysis

7. **Consult Teaser**
   - Last consultation summary
   - "Ask Again" quick action

8. **AI Chat Access**
   - Floating action button for quick access
   - "Ask Navi" prompt suggestions

### Dashboard Data Sources
1. **Horoscope API**: Daily personalized horoscope
2. **Forecast API**: Area-specific predictions
3. **Analyze API**: Full birth chart data
4. **Consult History API**: Past consultations
5. **User Profile API**: Personal data and preferences

### Dashboard Interaction Flow
User Opens App │ ▼ Dashboard Loads │ ├──▶ Fetch User Profile ├──▶ Fetch Daily Horoscope ├──▶ Fetch Forecast ├──▶ Fetch Kundli Preview └──▶ Fetch Recent Consultations │ ▼ Content Displayed │ ├──▶ User interacts with sections │ ├──▶ Tap Horoscope → Full View │ ├──▶ Tap Forecast → Full Forecast │ ├──▶ Tap Kundli → Full Analysis │ ├──▶ Tap Consult → Full History │ └──▶ Tap AI Chat → Conversation │ ▼ Navigation to Full Features


---

## 37. COMPLETE SCREEN INVENTORY

| # | Screen Name | Purpose | Key Features | Navigation |
|---|-------------|---------|--------------|------------|
| 1 | Login Screen | User authentication | Email/password, token management, session storage | Entry point |
| 2 | Registration Screen | New user signup | Email/password validation, confirmation, session setup | From Login |
| 3 | Profile Screen | User profile management | Personal details, birth info, account management | From Dashboard/Menu |
| 4 | Dashboard Screen | Main app overview | Horoscope, forecast preview, Kundli preview, AI chat access | Home screen |
| 5 | Forecast Screen | Area-specific predictions | Daily snapshot, weekly graph, detailed day view, alerts | From Dashboard |
| 6 | Kundli Screen | Full birth chart analysis | Chart reveal, ascendant, dasha, planets, houses, transits | From Dashboard |
| 7 | Match Screen | Compatibility analysis | Dual input form, 36 Guna system, Mangal Dosha | From Dashboard |
| 8 | Match History Screen | Past match records | Expandable list, detailed view, deletion | From Match screen |
| 9 | Chat Screen | AI-powered guidance | Streaming responses, ratings, history, suggestions | Floating button/Menu |
| 10 | Consult Screen | Guided AI consultation | Step-by-step flow, birth details, category/question selection | From Dashboard |
| 11 | Consult History Screen | Past consultation records | Expandable list, insight details | From Consult screen |
| 12 | Astrologers Screen | Expert directory | Profiles, ratings, pricing, booking (placeholder) | From Menu |
| 13 | Knowledge Hub Screen | Astrology encyclopedia | Entry point for Planets, Nakshatras, Houses, Yogas | From Menu |
| 14 | Planet Screen | Planet encyclopedia | List + detailed view with nature, indications | From Knowledge Hub |
| 15 | Nakshatra Screen | Nakshatra encyclopedia | List + detailed view with Pada breakdown | From Knowledge Hub |
| 16 | House Screen | House encyclopedia | Grid + detailed view with significations | From Knowledge Hub |
| 17 | Yoga Screen | Yoga encyclopedia | List + detailed view with logic and results | From Knowledge Hub |
| 18 | Rashi Screen | Rashi encyclopedia | Grid + detailed view with horoscope | From Menu/Knowledge Hub |

---

## 38. TECHNICAL SPECIFICATIONS

### Minimum Requirements
- **OS**: Android 8.0 (Oreo) or later
- **RAM**: 3GB minimum, 4GB recommended
- **Storage**: 100MB app size
- **Network**: Stable internet connection required

### Libraries Used
- **Jetpack Compose**: UI framework
- **Retrofit**: HTTP client
- **OkHttp**: Network layer
- **Gson**: JSON parsing
- **Coil**: Image loading
- **DataStore**: Local storage
- **Kotlin Coroutines**: Async operations
- **Material Design 3**: UI components
- **Lottie**: Animations (if used)

### API Endpoints
| Endpoint | Method | Purpose |
|----------|--------|---------|
| api/login | POST | User authentication |
| api/register | POST | New user registration |
| api/auth/refresh | POST | Token refresh |
| api/user/profile | GET/PUT/DELETE | Profile management |
| api/daily-horoscope | GET | Personalized horoscope |
| api/horoscope/{sign} | GET | General horoscope |
| api/forecast/{area} | GET | Area-specific forecast |
| api/analyze-full | POST | Full Kundli analysis |
| api/match | POST | Compatibility analysis |
| api/match/history | GET | Match history |
| api/match/{matchId} | GET/DELETE | Match details/deletion |
| api/chats | GET/POST | Chat history/creation |
| api/chats/{chatId} | GET/DELETE | Chat details/deletion |
| api/chats/{chatId}/messages | POST | Send message |
| api/chats/{chatId}/messages/{msgId}/rate | PUT | Rate message |
| api/consult/tree | GET | Consultation tree |
| api/consult | POST | Generate consultation |
| api/consult/history | GET | Consultation history |

### Backend Dependencies
- **Authentication Service**: Token generation and validation
- **Astrology Engine**: Birth chart calculation, horoscope generation
- **AI Service**: Natural language processing, response generation
- **Consultation Service**: Tree-structured guidance

---

## 39. CONCLUSION

AstraNavi is a comprehensive Vedic astrology platform that successfully combines traditional astrological principles with modern AI technology. The app provides a rich, personalized experience through its AI chatbot, detailed birth chart analysis, compatibility matching, and educational content.

**Key Strengths**:
- Innovative AI-powered astrological guidance
- Comprehensive Vedic astrology features
- Immersive, well-designed user interface
- Personalized content based on birth data

**Areas for Improvement**:
- Payment integration for consultations
- Push notifications for daily horoscopes
- Social sharing features
- Offline capabilities
- Additional accessibility support
- Privacy policy and legal compliance

**Growth Potential**:
- Expand AI capabilities for deeper analysis
- Add community features for user engagement
- Implement premium subscription model
- Add more languages and regional astrology variations
- Develop web and iOS versions for wider reach
