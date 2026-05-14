# Paywall System — Frontend Integration Guide

## Overview

The backend now has a **unified paywall system**. There is one reusable paywall component that works for all 6 feature gates. The frontend doesn't need to build separate upgrade UIs everywhere — just one `<PaywallCard>` component that receives the same data shape from any endpoint.

**There is no payment system yet.** The paywall only tells the frontend *whether* a feature is accessible and *what to show* if it's not. Actual purchase flows (Razorpay, Google Play, App Store) will come later.

---

## 6 Paywall Trigger Points

| Feature | What Happens for Free Users | Soft or Hard |
|---------|----------------------------|--------------|
| **AI Chat** | Blocked when credits = 0. Show upgrade prompt. | **Hard** — completely blocked |
| **Full Daily Horoscope** | Gets basic horoscope (score + tip). No area texts, no transit overlays, no alerts. Show upgrade overlay. | **Soft** — partial access, show upgrade card on top |
| **Tomorrow Horoscope** | Gets a teaser: "Unlock tomorrow's insights with Pro." No horoscope data. | **Soft** — teaser only |
| **Guided Consultation** | Blocked when credits < 5. Show upgrade prompt. | **Hard** — completely blocked |
| **Match Report** | Blocked when credits < 10. Show upgrade prompt. | **Hard** — completely blocked |
| **Premium Kundli Sections** | Gets basic kundli (planets, ascendant, houses). Premium sections (dasha, ashtakavarga, strengths, transits) return `{"locked": true}`. Show upgrade overlay. | **Soft** — partial access |

---

## How to Check Feature Access

### Option 1: Pre-check endpoint (recommended)

Call this **before** attempting a feature to know if you should show the paywall:

```
GET /api/entitlements/paywall?feature=chat_message
GET /api/entitlements/paywall?feature=full_daily_horoscope
GET /api/entitlements/paywall?feature=tomorrow_horoscope
GET /api/entitlements/paywall?feature=guided_consult
GET /api/entitlements/paywall?feature=match_report
GET /api/entitlements/paywall?feature=kundli_premium
```

**Requires auth** (JWT token in header).

#### Response when accessible:

```json
{
  "accessible": true,
  "feature_key": "chat_message",
  "current_tier": "pro",
  "available_credits": 300
}
```

→ Proceed normally, no paywall needed.

#### Response when blocked:

```json
{
  "accessible": false,
  "feature_key": "full_daily_horoscope",
  "reason": "insufficient_tier",
  "current_tier": "free",
  "min_tier": "pro",
  "required_credits": 0,
  "available_credits": 30,
  "paywall": {
    "featureKey": "full_daily_horoscope",
    "title": "Full Daily Horoscope",
    "titleHi": "पूर्ण दैनिक राशिफल",
    "description": "Get personalized area insights, transit overlays, and daily alerts based on your chart.",
    "descriptionHi": "अपनी कुंडली के अनुसार व्यक्तिगत क्षेत्र विवरण, गोचर, और दैनिक अलर्ट पाएं।",
    "icon": "☀️",
    "isSoft": true,
    "color": "#F59E0B",
    "badge": "Pro",
    "suggestedProducts": [
      {
        "productId": "pro_monthly",
        "productType": "subscription",
        "nameEn": "Pro Monthly",
        "nameHi": "प्रो मासिक",
        "credits": 300,
        "tier": "pro",
        "priceInr": 199.00,
        "priceUsd": 2.49,
        "currency": "INR",
        "icon": "pro",
        "color": "#7C3AED"
      }
    ]
  }
}
```

→ Render the `<PaywallCard>` component using the `paywall` object.

### Option 2: Batch check all features (for dashboard/app launch)

```
GET /api/entitlements/paywall/features
```

Returns an array of paywall checks for all 6 features at once:

```json
{
  "features": [
    { "accessible": true, "feature_key": "chat_message", ... },
    { "accessible": false, "feature_key": "full_daily_horoscope", "paywall": { ... } },
    { "accessible": false, "feature_key": "tomorrow_horoscope", "paywall": { ... } },
    ...
  ]
}
```

Use this on app launch or dashboard load to pre-determine which features need paywall overlays.

---

## How Existing Endpoints Now Return Paywall Data

### Daily Horoscope (`GET /api/daily-horoscope`)

**No change for Pro/Premium users** — they get the full personalized horoscope as before.

**Change for Free users** — the response now includes a `paywall` field and `system.is_personalized` is set to `false`:

```json
{
  "sign": "Leo",
  "score": { ... },       // Still present (basic)
  "tip": { ... },         // Still present (basic)
  "areas_text": { ... },  // Still present but NOT enriched (basic version)
  "system": { "is_personalized": false },
  "paywall": {
    "featureKey": "full_daily_horoscope",
    "title": "Full Daily Horoscope",
    "isSoft": true,
    "suggestedProducts": [...]
  }
}
```

→ Frontend renders the basic horoscope as normal, then overlays the `<PaywallCard>` on the premium sections (area insights, transit overlay, alerts).

### Tomorrow Horoscope (`GET /api/tomorrow-horoscope`) — NEW ENDPOINT

**Pro/Premium** — full personalized horoscope for tomorrow (same structure as daily).

**Free/Anonymous** — teaser response:

```json
{
  "sign": "Leo",
  "date": "2026-05-14",
  "lang": "en",
  "teaser": true,
  "message": "Unlock tomorrow's insights with a Pro plan. Plan your day with planetary guidance a day ahead.",
  "paywall": {
    "featureKey": "tomorrow_horoscope",
    "isSoft": true,
    ...
  }
}
```

→ Show a teaser card with the paywall overlay.

### Kundli / Profile (`POST /api/analyze-full`)

**Pro/Premium** — full response, all sections available.

**Free** — premium sections replaced with locked placeholders:

```json
{
  "success": true,
  "astrologyData": {
    "identity": { ... },       // Available
    "ascendant": { ... },      // Available
    "planets": [ ... ],        // Available
    "houses": { ... },         // Available
    "chart_summary": { ... },  // Available
    "dasha": { "locked": true, "message": "Unlock dasha with Pro plan" },
    "ashtakavarga": { "locked": true, "message": "Unlock ashtakavarga with Pro plan" },
    "planet_strength_ranking": { "locked": true, "message": "Unlock planet_strength_ranking with Pro plan" },
    "transits": { "locked": true, "message": "Unlock transits with Pro plan" },
    "key_themes": { "locked": true, "message": "Unlock key_themes with Pro plan" }
  },
  "paywall": {
    "featureKey": "kundli_premium",
    "title": "Premium Kundli Sections",
    "isSoft": true,
    ...
  }
}
```

→ Render the basic kundli sections normally. For each `locked` section, show a locked card/placeholder with the upgrade prompt.

### Chat, Consult, Match (HTTP 402 errors)

When credits are insufficient, these endpoints now return HTTP **402** with a unified `paywall` detail:

```json
{
  "error": "paywall_blocked",
  "featureKey": "chat_message",
  "reason": "insufficient_credits",
  "requiredCredits": 1,
  "availableCredits": 0,
  "minTier": "free",
  "isSoft": false,
  "message": "You need 1 Navi Credits for AI Chat Credits. You have 0 remaining.",
  "paywall": {
    "featureKey": "chat_message",
    "title": "AI Chat Credits",
    "isSoft": false,
    "suggestedProducts": [...]
  }
}
```

→ Catch 402 responses in your API client. Extract `detail.paywall` and render `<PaywallCard>`.

---

## The One `<PaywallCard>` Component

You need **one component** that handles all 6 paywall points. The data shape is always the same:

```
type PaywallCard = {
  featureKey: string;         // Which feature is blocked
  title: string;              // English title
  titleHi: string | null;     // Hindi title
  description: string;        // English description
  descriptionHi: string | null; // Hindi description
  icon: string | null;        // Emoji/icon for the card
  isSoft: boolean;            // true = show overlay on partial content; false = full block
  color: string | null;       // Theme color for the card
  badge: string | null;       // "Pro" or "5 Credits" etc.
  suggestedProducts: [{
    productId: string;
    productType: string;      // "subscription" or "one_time_pack"
    nameEn: string;
    nameHi: string | null;
    credits: number;
    tier: string | null;      // "pro" or "premium" for subscriptions
    priceInr: number | null;
    priceUsd: number | null;
    currency: string;
    icon: string | null;
    color: string | null;
  }];
}
```

### Rendering Logic

- **`isSoft = true`**: Show the partial content (basic horoscope, basic kundli) and overlay the paywall card on top of the premium sections. Don't block the entire screen.
- **`isSoft = false`**: Block the feature entirely. Show the paywall card as the primary UI (no content behind it).

### Suggested Products

These are the products the user can purchase to unlock the feature. Display them as upgrade buttons/cards. **No purchase flow exists yet** — just show the product info (name, price, credits). The actual "Buy" button will be wired to Razorpay/Google Play/App Store later.

---

## What Has NOT Changed

- `/api/entitlements/balance` — same as before, returns credit balance
- `/api/entitlements/catalog` — same as before, returns product catalog
- `/api/entitlements/history` — same as before, returns usage ledger
- `/api/daily-horoscope` for anonymous users — same as before (basic horoscope, no paywall field)
- `/api/planets` — same as before (not paywall-gated)
- `/api/forecast/{area}` — same as before (not paywall-gated)
- Chat streaming, consult streaming, match computation — same as before, just the 402 error format changed

---

## Migration Required

Run this migration on the database before deploying:

```bash
python run_migrations.py
```

This adds the `paywall_features` table (migration 020) with the 6 seed entries.

---

## Quick Start Checklist for Frontend

1. **Create `<PaywallCard>` component** — accepts a `paywall` prop of the shape above
2. **Add API client interceptor** for 402 responses — extract `detail.paywall` from error
3. **Call `/api/entitlements/paywall?feature=...` before showing** premium features
4. **Or call `/api/entitlements/paywall/features` on dashboard load** to batch-check
5. **For daily horoscope** — if response has `paywall` field, render overlay on premium sections
6. **For tomorrow horoscope** — new endpoint; if `teaser: true`, show teaser + paywall
7. **For kundli profile** — if section has `locked: true`, show locked placeholder + paywall
8. **For chat/consult/match** — catch 402, show `<PaywallCard>` from `detail.paywall`
9. **No purchase flow yet** — suggested products are display-only until payment integration