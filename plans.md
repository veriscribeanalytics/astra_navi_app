# AstraNavi Product Plan

Last updated: 2026-05-13

## Product Positioning

AstraNavi should launch as an AI-first, India-first Vedic astrology app. The core promise is instant personalized guidance from Navi using birth data, Dasha, transits, Kundli, compatibility, and daily timing.

Do not position the launch product as a human astrologer marketplace. The current Astrologers screen can remain as a future module, AI persona directory, or expert-style content surface, but launch monetization should be built around AI subscriptions and one-time AI packs.

## Current Product Surface

From `Features.md` and the app code, these modules already exist or are planned in the active product surface:

| Area | Status | Notes |
|---|---:|---|
| Login / Registration | Present | Email/password auth with session storage. |
| Profile | Present | Birth details, personal details, language, account controls. |
| Dashboard | Present | Daily horoscope, streaks, Kundli preview, forecast preview, consult teaser. |
| Daily Horoscope | Present | Strong retention hook; should become the Pro+ full daily product. |
| Forecast | Present | General/love/career/health/finance forecasts. |
| Kundli | Present | Full chart, Dasha, planets, houses, strengths, transits. |
| Match | Present | 36 Guna, Mangal Dosha, AI narrative. |
| Match History | Present | Stored compatibility results. |
| AI Chat | Present | Streaming AI responses, history, ratings, copy. |
| Guided AI Consult | Present | Step-based consultation tree and streaming insight. |
| Consult History | Present | Stored guided consults. |
| Knowledge Hub | Present | Planets, Nakshatras, Houses, Yogas, Rashis. |
| Astrologers | Placeholder | Treat as future/AI persona directory, not launch monetization. |

## Product Gaps To Close Before Pricing Launch

1. **Entitlements**: User plan, active subscription, pack balances, expiry, monthly quota reset.
2. **Usage ledger**: Record every AI message, guided consult, report generation, refund, and admin grant.
3. **Billing**: Google Play Billing plus server-side receipt validation.
4. **Paywalls**: Consistent upgrade prompts from chat, daily horoscope, consult, match, Kundli, and reports.
5. **Usage meter**: Show remaining monthly Navi Credits and pack balance before the user hits a hard stop.
6. **Config-driven pricing**: Backend controlled product IDs, allowances, prices, and sale flags.
7. **Pro+ daily horoscope**: Full personalized daily horoscope with areas, timing, remedies, and push notification.
8. **Regional language expansion**: Hindi should be first-class; add Marathi, Gujarati, Tamil, Telugu, Bengali after launch.
9. **Remedies**: Practical remedies, gemstone caution text, mantra/daan suggestions, and safety disclaimers.
10. **Share/export**: PDF/share outputs for Kundli, match, and monthly reports.
11. **Apple/Google SSO**: Reduce registration drop-off by adding social sign-in (Quick Win).
12. **Horoscope history**: Save past daily readings, let users favorite them (Quick Win).
13. **Yearly horoscope page**: Static content, high SEO/organic value (Quick Win).
14. **Tomorrow's horoscope teaser**: One line visible, rest blurred behind paywall (Quick Win).

## Priority Action List

### HIGH PRIORITY â€” Before or at launch

| # | Feature | Frontend Work | Backend Work | Status |
|---|---|---|---|---|
| 6 | Premium Subscription | Paywall UI, usage meter, plan selector, upgrade prompts | Entitlement tables, usage ledger, receipt validation, quota reset, config-driven pricing | In Progress |
| 10 | Offline Cache | Cache UI states, stale indicators, offline banner, retry buttons | Cache policy (TTLs per endpoint), invalidation rules, serve cached when offline | In Progress |
| 1 | Push Notifications | Notification permission flow, notification UI/settings, in-app fallback banners | FCM integration, user-chosen time scheduling, Dasha transition alerts, cosmic-event notification engine | Post-beta |
| 11 | Apple/Google SSO | Social sign-in buttons on Login/Register, token handling | OAuth server integration, account linking, session management | Quick Win |

### MEDIUM PRIORITY â€” Next development cycle

| # | Feature | Frontend Work | Backend Work | Description |
|---|---|---|---|---|
| 4 | Panchang / Daily Timing | "Best Hours Today" screen/widget, Rahu Kaal display, Hora/Choghadiya cards, auspicious hours per activity | Panchang calculation engine (Rahu Kaal, Hora, Choghadiya) based on user location + birth chart, location-aware sunrise/sunset | Daily astrological timing tool. Auspicious hours for career/love/travel/finance, inauspicious Rahu Kaal window, ruling Hora, Choghadiya slots. This becomes a daily-open habit feature. |
| 7 | Cosmic Calendar | Month-view calendar UI showing moon phases, retrograde periods, eclipses, transit events; tab inside Forecast screen | Planetary event computation engine: moon phases, retrograde dates, eclipse dates, major transit entries; per-user relevance scoring | Not a religious calendar â€” a planning tool. Think Google Calendar with planetary events. Can be a tab inside Forecast to keep scope small. |
| 9 | Navamsa (D9) + Dashamansha (D10) | Two extra chart tabs inside Kundli screen, SVG rendering for D9/D10, one-line explanation labels | D9 (marriage/soul purpose), D10 (career) chart generation from existing Kundli engine; D7 (children) optional later | The hard calculation work is already done. D9 and D10 are mostly a backend chart rendering addition. Label them clearly with one-line explanations. |
| 2 | AI Voice Narration | Play/pause/stop controls on daily horoscope, audio player UI | TTS API integration (Google/Azure TTS), audio file generation and caching | Low engineering effort, high perceived value. Users listen while getting ready in the morning. |
| 3 | Shareable Horoscope Cards | Card template design, image generation (user's sign + reading snippet + branding), share intent to WhatsApp/Instagram | Minimal â€” optional server-side card image generation if client-side is insufficient | Free marketing. One screen, one button. |
| 14 | Monthly & Annual Forecast | Forecast display UI for monthly/annual views, premium gating | Monthly/annual forecast generation engine, annual SEO-optimized content, premium entitlement checks | Annual forecast spikes in search traffic every January. Strong premium upsell. |
| 15 | Multi-turn Consult ("Dig Deeper") | "Dig Deeper" / "Ask a Follow-up" button on consult result, carry context into chat session | Context-passing API: consult result â†’ chat session with pre-loaded birth chart + consult context | Consult becomes a conversation starter, not a dead end. |
| 17 | Kundli PDF Export | Download/share button, share sheet, PDF viewer preview | PDF generation service (Kundli layout with chart SVG, planets, houses, Dasha timeline), file delivery | Very commonly requested for sharing with family elders or human astrologers. One-time build, high satisfaction. |

### LOWER PRIORITY â€” Plan for v2

| # | Feature | Frontend Work | Backend Work | Description |
|---|---|---|---|---|
| 11 | Chat Memory (Navi) | Minimal â€” existing chat UI; possible "past topics" sidebar | Conversation context persistence across sessions, topic extraction, recall engine | Navi remembers past conversations and references them naturally. |
| 13 not needed | Knowledge Hub Personalization | Dynamic content linking: show user's relevant planet/sign/nakshatra highlights when reading an article | Per-user chart-to-content mapping, personalized article recommendations | When they read about Mars, show how Mars functions in their own chart. |
| 8  no | Real Astrologer Booking | Replace placeholder with "Coming Soon" state or remove; if built: booking UI, calendar, payment flow | Booking system with payment, availability, quality control, compliance, customer support â€” OR remove entirely | Either build it properly, or remove the placeholder screen. A broken feature hurts trust more than having no feature. |
| 12 | Compatibility Timing | Dual-chart timing display for marriage windows | Combined Dasha overlap calculation for two charts, auspicious window scoring | Show "best marriage" window based on both charts' Dasha. Common user question. |

### Quick Wins â€” Do anytime

| Item | Frontend Work | Backend Work |
|---|---|---|
| Apple/Google SSO | Social sign-in buttons, token handling | OAuth integration, account linking |
| Horoscope history | History list UI, favorite toggle | Store past daily readings, favorite API |
| Yearly horoscope page | Static content page, SEO-friendly layout | Static/semi-static annual content per sign |
| Tomorrow's horoscope teaser | Paywall blur overlay on tomorrow's reading | Tomorrow's horoscope generation, premium gating |

## Data Sourcing: Frontend vs Backend

This section clarifies where each feature's DATA comes from. "Frontend" means static hardcoded data (Kotlin data classes, never changes). "Backend" means computed, generated, or served from the API. Almost every new feature needs backend data — only labels and visual assets can stay frontend-static.

### Data Source Categories

| Category | Definition | Examples |
|---|---|---|
| **Frontend Static** | Hardcoded Kotlin data classes, never changes, no API call needed | Planet encyclopedia, Nakshatra list, House meanings, Yoga definitions, Rashi list |
| **Frontend Assets** | Static visual resources stored in the app bundle | Moon phase icon assets (New Moon, Full Moon glyphs), card template layouts, branding images |
| **Frontend Static Labels** | Short explanation strings embedded in the app | D9 label "Navamsa reveals your soul purpose", D10 label "Dashamansha maps your career path" |
| **Backend Semi-Static** | Computed once from birth data, then cached forever (only invalidate if birth data changes) | Kundli D1 chart + SVG, D9 Navamsa chart + SVG, D10 Dashamansha chart + SVG, Dasha timeline sequence |
| **Backend Semi-Static (Yearly)** | Computed once per year, cached, same for all users globally | Moon phase dates for the year, retrograde period dates, eclipse dates, major transit entry dates, yearly horoscope per sign |
| **Backend Dynamic** | Computed per-user per-day, changes frequently (daily/location-dependent) | Daily horoscope, Panchang timing, weekly forecast scores, tomorrow's horoscope, push notification content, Panchang birth-chart overlay, Cosmic Calendar personal relevance |
| **Backend AI-Generated** | LLM produces text on demand, per-user, per-request | Chat messages, consult results, monthly/annual forecast narrative, voice narration audio, multi-turn consult follow-up |

### Feature-by-Feature Data Breakdown

#### Frontend Static Data Only (no backend needed for this data)

| Data | Where Stored | Changes? |
|---|---|---|
| Planet names, nature, karakatvas, indications | `PlanetData.kt` | Never |
| 27 Nakshatra names, Padas, rulers, deity | `NakshatraData.kt` | Never |
| 12 house meanings, significations | `HouseData.kt` | Never |
| Yoga names, classification, logic, results | `Yoga` data class | Never |
| 12 Rashi names, rulers, elements | `RashiData.kt` | Never |
| D9/D10 one-line explanation labels | Static string resources | Never |
| Moon phase icon assets (emojis/glyphs) | App drawable resources | Never |
| Shareable card template layout + branding | Compose layout + app assets | Never |
| Knowledge Hub article text content | Static Kotlin data | Never |

#### Backend Semi-Static Data (computed once, cached)

| Feature | Data | Computed From | Refresh Frequency | Endpoint |
|---|---|---|---|---|
| Kundli (D1) chart | Ascendant, planet positions, house cusps, SVG | Birth data (DOB, TOB, POB) | Once — cache forever, invalidate only if birth data edited | `api/analyze-full` (existing) |
| Navamsa D9 chart (#9) | D9 planet positions, Navamsa SVG | Same birth data — Kundli engine already computes | Once — cache forever | Extend `api/analyze-full` to return D9, OR new `api/kundli/navamsa` |
| Dashamansha D10 chart (#9) | D10 planet positions, D10 SVG | Same birth data — Kundli engine already computes | Once — cache forever | Extend `api/analyze-full` to return D10, OR new `api/kundli/dashamansha` |
| Saptamsha D7 chart (#9, future) | D7 planet positions, D7 SVG | Same birth data | Once — cache forever | New `api/kundli/saptamsha` (optional later) |
| Dasha timeline | Mahadasha/Antardasha/Pratyantardasha sequence | Birth data | Once — sequence is fixed, but "current" pointer moves daily | `api/analyze-full` (existing) |
| Yearly moon phase dates (#7) | New Moon, Full Moon, quarter phases for each day of the year | Ephemeris computation — same for everyone globally | Once per year (generate all 365 days in January, cache) | New `api/cosmic-calendar/moon-phases?year=YYYY` |
| Yearly retrograde/eclipse/transit dates (#7) | Mercury retrograde Aug 5-29, Jupiter enters Gemini May 14, eclipse dates | Ephemeris — same for everyone globally | Once per year | New `api/cosmic-calendar/events?year=YYYY` |
| Yearly horoscope per sign (Quick Win) | General per-sign yearly overview | AI-generated once per year per sign, then cached as static content | Once per year (January refresh) | New `api/horoscope/yearly?sign=Aries` |

#### Backend Dynamic Data (computed per-user per-day, changes daily)

| Feature | Data | Why Dynamic | Changes How Often | Endpoint |
|---|---|---|---|---|
| Daily horoscope | Personalized reading, score, mood, lucky color/number | AI-generated + planetary transit overlay on birth chart | Every day | `api/horoscope/{sign}` (existing) |
| Tomorrow's horoscope (Quick Win) | Tomorrow's personalized reading | Same engine as daily, shifted one day forward | Every day | Extend `api/horoscope` to accept `date=tomorrow` |
| **Panchang / Daily Timing (#4)** | Rahu Kaal window, Hora sequence, Choghadiya slots, auspicious hours per activity | **Rahu Kaal depends on sunrise/sunset for user's LOCATION — different for Delhi vs Chennai vs Jaipur. Hora depends on sunrise + weekday. Choghadiya depends on sunrise/sunset. Auspicious hours overlay needs birth chart + current transits. Cannot be static.** | Every day, varies by location | New `api/panchang?date=YYYY-MM-DD&location=city` |
| Weekly forecast scores | 7-day scores per life area, transits, alerts | Transit positions relative to birth chart | Every week | `api/forecast/{area}` (existing) |
| **Cosmic Calendar personal relevance (#7)** | "Jupiter entering Gemini hits YOUR 7th house" | Must map global transit events against user's birth chart — different for every user | Per-user, changes with each transit | New `api/cosmic-calendar/relevance?month=YYYY-MM` (returns per-user house mapping) |
| Push notification content (#1) | "Your Dasha shifts to Saturn-Moon today", "Mercury retrograde starts — affects your 3rd house" | Must compute from birth chart + current transits + Dasha timeline | Triggered by events, per-user | Backend notification scheduler service |
| Monthly forecast scores (#14) | 30-day trend, major transit events | Transit computation per birth chart | Every month | New `api/forecast/monthly` |

#### Backend AI-Generated Data (LLM produces text on demand)

| Feature | Data | When Generated | Endpoint |
|---|---|---|---|
| AI Chat messages | Streaming AI response | On every user message | `api/chats/{chatId}/messages` (existing) |
| Guided AI Consult result | Streaming consult insight | On each consult request | `api/consult` (existing) |
| Multi-turn Consult follow-up (#15) | Context-enriched follow-up chat | When user taps "Dig Deeper" — consult result loaded as system prompt for chat | Extend chat API to accept `context_from=consult_id` |
| Monthly forecast narrative (#14) | AI-written monthly story | Once per month per user | New `api/forecast/monthly-narrative` |
| Annual forecast narrative (#14) | AI-written annual overview | Once per year per user | New `api/forecast/annual-narrative` |
| Match AI narrative | Compatibility story | On each match calculation | `api/match` (existing) |
| AI Voice Narration audio (#2) | Audio file from TTS | On demand (from daily horoscope text) | New `api/horoscope/audio` — returns audio file URL |
| Kundli PDF content (#17) | PDF document with chart + analysis | On demand (from Kundli data) | New `api/kundli/pdf` — returns PDF download URL |

#### Frontend-Rendered from Backend Data (frontend composes/displays what backend sends)

| Feature | Data Flow | Frontend Role | Backend Role |
|---|---|---|---|
| Shareable Horoscope Cards (#3) | Existing horoscope API text + user's sign from session -> frontend renders into styled bitmap locally | Card template, fonts, branding — all frontend. Compose Canvas to bitmap to share intent | No new endpoint. Frontend uses existing `api/horoscope` data. Optional: backend card image generation if client-side rendering insufficient. |
| Kundli PDF Export (#17) | Backend generates PDF from Kundli data + chart SVG -> frontend downloads and shares via share sheet | Download button, share sheet, PDF preview viewer | PDF generation service: Kundli layout with chart SVG, planets, houses, Dasha timeline. Alternative: frontend-side Android PDF library, but backend is more reliable for consistent formatting. |
| Knowledge Hub Personalization (#13) | Backend sends `personalized_highlights` (which planets strong, which houses active) -> frontend injects highlight cards into existing static articles | Articles stay static frontend. Backend just adds highlight metadata per section | Extend Kundli API response to include `personalized_highlights` array mapping planets/houses to article sections |
| Horoscope history (Quick Win) | Backend stores past daily horoscope responses -> frontend displays list with favorite toggle | History list UI, favorite toggle button | New `api/horoscope/history` returns past readings. New `api/horoscope/favorite` toggles favorite. |
| Tomorrow's horoscope teaser (Quick Win) | Backend computes tomorrow's horoscope -> frontend shows one line + blur overlay | Paywall blur overlay, "Upgrade to see full" prompt | Same horoscope engine, date shifted forward. Premium entitlement check. |
| Yearly horoscope page (Quick Win) | Backend serves cached yearly content per sign -> frontend displays static-style page | Static-style content page, SEO-friendly layout | AI-generated once per year per sign, cached, served as semi-static content |

### Why Panchang (#4) Cannot Be Static

This is the most common misconception. Panchang data is NOT like the Knowledge Hub encyclopedia. Here is why it needs backend computation:

| Panchang Element | Depends On | Example Showing It Changes |
|---|---|---|
| **Rahu Kaal** | Sunrise time + weekday + city latitude | Monday Rahu Kaal in Delhi (sunrise 5:28 AM): 7:30-9:00 AM. Same Monday in Chennai (sunrise 6:05 AM): 7:42-9:09 AM. Completely different times. |
| **Hora** | Sunrise time + day of week | Each planetary hour starts from sunrise. If sunrise is at 5:30 AM, Sun hora rules 5:30-6:30. If sunrise is 6:00 AM, Sun hora rules 6:00-7:00. Different every day, every city. |
| **Choghadiya** | Sunrise + sunset duration + day/night split | Day Choghadiya slots shift when sunset time changes (shorter winter days vs longer summer days). Different seasons, different cities. |
| **Auspicious hours overlay** | Panchang + user's birth chart + current transits | "This Rahu Kaal falls in your 10th house — avoid career decisions today" is specific to the user's ascendant and current transit positions. Cannot be generic. |
| **Tithi, Nakshatra, Yoga, Karana** | Moon position relative to Sun | Changes every day. A Panchang for May 13 is different from May 14. |

Implementation approach: Backend needs an ephemeris library (Swiss Ephemeris / SWE is standard) to compute sunrise/sunset for any city, then derive Rahu Kaal, Hora, Choghadiya. The city can come from user's POB or a "current location" field. The birth chart overlay comes from the existing Kundli engine.

### Why Cosmic Calendar (#7) Has Two Data Layers

The Cosmic Calendar splits into global astronomical events (same for everyone) and personal relevance (different per user):

| Layer | Data | Same For Everyone? | Source | Refresh |
|---|---|---|---|---|
| **Global events** | Moon phases, retrograde periods, eclipse dates, transit entry dates | Yes — Mercury retrograde dates are the same in Delhi and Mumbai | Backend ephemeris, computed once per year, cached globally | Once per year |
| **Personal relevance** | "Jupiter entering Gemini hits YOUR 7th house", "This eclipse falls in your career axis" | No — depends on user's ascendant and house layout | Backend maps global events against user's birth chart | Per-user, when birth data changes or transit shifts |

Implementation approach: Backend computes global events once per year and stores them (or uses a pre-computed astronomical almanac). When a user requests the calendar, backend overlays their birth chart to score relevance. Frontend shows global events on the calendar grid, with per-user relevance as colored highlights or tooltips.

### New Backend Endpoints Summary

All endpoints that need to be added to the API for new features:

| Endpoint | Purpose | Method | Data Returned |
|---|---|---|---|
| `api/panchang?date=YYYY-MM-DD&location=city` | Panchang daily timing | GET | Rahu Kaal window, Hora sequence with planet rulers, Choghadiya slots (good/bad labels), auspicious hours per activity type, Tithi/Nakshatra/Yoga/Karana for the day, birth-chart overlay relevance per time slot |
| `api/cosmic-calendar/moon-phases?year=YYYY` | Yearly moon phase dates (global) | GET | Array of date + phase_name + phase_icon_key for all 365 days. Same for all users. Cache once per year. |
| `api/cosmic-calendar/events?year=YYYY` | Yearly retrograde/eclipse/transit events (global) | GET | Array of event_type + planet + start_date + end_date + description for retrogrades, eclipses, major transits. Same for all users. Cache once per year. |
| `api/cosmic-calendar/relevance?month=YYYY-MM` | Per-user relevance overlay for calendar events | GET | Array of event_id + affected_house + relevance_score + one_line_note mapping global events to user's birth chart. Different per user. |
| `api/kundli/navamsa` OR extend `api/analyze-full` | D9 Navamsa chart | GET/POST | D9 planet positions, D9 chart SVG, D9 house layout, D9 key themes |
| `api/kundli/dashamansha` OR extend `api/analyze-full` | D10 Dashamansha chart | GET/POST | D10 planet positions, D10 chart SVG, D10 house layout, D10 career themes |
| `api/kundli/saptamsha` (future) | D7 Saptamsha chart | GET/POST | D7 planet positions, D7 chart SVG, D7 house layout, D7 children themes |
| `api/forecast/monthly` | Monthly forecast scores + transit summary | GET | 30-day trend scores per area, major transit events for the month |
| `api/forecast/monthly-narrative` | AI-generated monthly forecast story | GET | AI-written monthly narrative, personalized per birth chart |
| `api/forecast/annual-narrative` | AI-generated annual forecast story | GET | AI-written annual overview, personalized per birth chart |
| `api/horoscope/audio` | Voice narration audio file | GET | Audio file URL (TTS-generated from daily horoscope text) |
| `api/horoscope/history` | Past daily horoscope readings | GET | Array of past horoscope responses with dates |
| `api/horoscope/favorite` | Toggle favorite on past readings | PUT | Favorite status confirmation |
| `api/horoscope/yearly?sign=Aries` | Yearly horoscope per sign | GET | AI-generated yearly overview, cached per year per sign |
| `api/kundli/pdf` | Kundli PDF download | GET | PDF file URL (server-generated from Kundli data + chart SVG) |
| Extend `api/chats` with `context_from=consult_id` | Multi-turn consult context passing | POST | Creates chat session with consult result pre-loaded as system context |
| Extend `api/horoscope` with `date=tomorrow` | Tomorrow's horoscope | GET | Same horoscope engine, date parameter shifted one day forward |
| Backend notification scheduler service | Push notification engine | Internal | Computes Dasha transitions + transit events + daily horoscope summary per user, pushes via FCM at user-chosen time |

### Data That Can Stay Frontend-Static (for new features)

Only these pieces of new feature data can be hardcoded in frontend:

| Feature | Frontend-Static Data | Purpose |
|---|---|---|
| Panchang (#4) | Time-slot type labels ("Auspicious", "Inauspicious", "Neutral"), activity category labels ("Career", "Love", "Travel", "Finance") | UI labels only. Actual time values are backend-dynamic. |
| Panchang (#4) | Hora planet order sequence reference (Sun to Moon to Mars to Mercury to Jupiter to Venus to Saturn for weekday Hora order) | Educational reference only. Actual Hora start/end times are backend-dynamic based on sunrise. |
| Cosmic Calendar (#7) | Moon phase icon assets (New Moon emoji, Full Moon emoji, Waxing/Waning Crescent glyphs) | Visual rendering only. Which phase falls on which date is backend data. |
| Cosmic Calendar (#7) | Event type labels ("Retrograde", "Eclipse", "Transit Entry") | UI labels only. Actual event dates and planets are backend data. |
| Navamsa D9 (#9) | One-line label: "Navamsa (D9) — reveals your soul purpose, dharma, and marriage potential" | Explanation tooltip. Chart data is backend. |
| Dashamansha D10 (#9) | One-line label: "Dashamansha (D10) — maps your career path, professional strength, and public image" | Explanation tooltip. Chart data is backend. |
| Saptamsha D7 (#9, future) | One-line label: "Saptamsha (D7) — shows children, progeny, and creative legacy" | Explanation tooltip. Chart data is backend. |
| Shareable Cards (#3) | Card layout template, font choices, color scheme, AstraNavi logo/branding asset | Visual composition only. Text content comes from existing horoscope API. |
| Knowledge Hub Personalization (#13) | The existing static article content stays unchanged | Articles stay frontend-static. Backend just adds a personalized_highlights array that frontend injects as highlight cards into those static articles. |

## Recommended Roadmap

### Phase 0: Monetization Foundation

- Build backend entitlement tables: `subscriptions`, `packs`, `usage_ledger`, `product_catalog`.
- Add server endpoints for current plan, remaining usage, pack purchase validation, and quota reset.
- Add a client-side usage meter component reused across chat, consult, and reports.
- Add rate limits: 3 user messages per minute, monthly hard caps, and abuse controls.
- Keep all pricing and allowances configurable from backend.
- Add Apple/Google SSO to registration (reduces drop-off significantly).
- Finish offline cache: daily horoscope and birth chart SVG must work offline.

### Phase 1: India Launch Pricing

- Launch Free, Pro, Premium.
- Add one-time chat packs for non-subscription users.
- Add guided AI consult packs for career, marriage, finance, health, timing, and remedies.
- Add report purchases: Kundli, match, monthly forecast, marriage decision pack.
- Make Pro+ full daily horoscope the main upgrade reason.
- Add tomorrow's horoscope as premium teaser (one line visible, rest blurred).
- Add yearly horoscope page (static content, high SEO/organic value).

### Phase 2: Retention & Daily Habit Features

- Daily Pro+ horoscope notification at user-chosen time, Dasha transition alert, one "cosmic event today" notification (retrograde, eclipse, etc.). Tie to user's actual chart, not generic blasts.
- Panchang / Daily Timing: "Best Hours Today" screen showing Rahu Kaal, auspicious hours for key activities (career, love, travel, finance), ruling Hora, Choghadiya. Daily-open habit feature.
- Cosmic Calendar: Monthly calendar view showing moon phases, retrogrades, eclipses, and major transits. Planning tool, not a religious calendar. Tab inside Forecast screen.
- Navamsa (D9) + Dashamansha (D10) charts: Two extra chart tabs inside Kundli screen. D9 for marriage/soul purpose, D10 for career. Backend engine already has the data.
- Streak rewards that give tiny non-cash usage boosts, not unlimited AI.
- Weekly life-area forecast summary.
- Monthly "Navi Report" for Premium.
- Saved insights and searchable history.
- Horoscope history: Save past daily readings, let users favorite them.

### Phase 3: Expansion & Premium Depth

- Multi-turn Consult: "Dig Deeper" / "Ask a Follow-up" button that carries consult context into chat with Navi.
- Kundli PDF Export: Download or share Kundli as PDF. High satisfaction, commonly requested.
- Shareable Horoscope Cards: Styled image card (user's sign + reading snippet + AstraNavi branding) for WhatsApp/Instagram. Free marketing.
- AI Voice Narration: TTS for daily horoscope. Low engineering effort, high perceived value. Only after real voice cost is measured.
- Monthly & Annual Forecast: Especially annual â€” spikes in search traffic every January, strong premium upsell.
- Knowledge Hub Personalization: Link encyclopedia articles to user's own chart.
- Chat Memory (Navi): Remember past conversations, reference them naturally.
- Family profiles for Premium.
- Compatibility Timing: Dual-chart timing for marriage windows based on both charts' Dasha.
- Human astrologer marketplace only if it has separate P&L, compliance, quality control, and customer support â€” OR remove placeholder entirely and show "Coming Soon" instead of dummy cards.

## Tier Philosophy

| Tier | User Type | Product Promise |
|---|---|---|
| Free | Curious user | Trust-building preview with low AI cost exposure. Basic Kundli, short daily horoscope, 3 AI messages/day. |
| Pro | Daily astrology user | Full daily horoscope, Panchang timing, enough AI chat for regular use, full chart access (D1). |
| Premium | Heavy/family user | More AI depth, family profiles, Cosmic Calendar, Navamsa/D10 charts, reports, exports, priority generation. |
| Packs | Non-subscription user | One-time access for specific needs without recurring billing. |

## Success Metrics

- Free-to-paid conversion: 3% to 7% within 30 days.
- Paid monthly churn: below 8% once daily horoscope notifications are live.
- Pro average AI cost: below INR 100/month/user.
- Premium average AI cost: below INR 200/month/user.
- Pack gross margin: keep above 25% after AI cost and platform fee.
- Free user AI cost cap: hard cap at 30 free Navi Credits/month until conversion data exists.

## Key Decisions

- Use **Navi Credit allowances**, not unlimited AI chat.
- Make **Pro+ full daily horoscope** the clearest recurring value.
- Keep packs simple and visible for Indian users who avoid subscriptions.
- Price below most AI astrology question packs, but avoid racing free products that are funded by ads, commerce, or B2B.
- Do not include voice minutes in subscriptions until real cost is measured.
