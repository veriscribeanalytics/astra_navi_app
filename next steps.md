# Next Steps — All Items Completed ✅

All items from the Life Areas Scoring Review and Glow Fade Bug Fix have been resolved (2026-05-28).

## Completed fixes

1. **OVERALL ENERGY → GENERAL OUTLOOK** — Hero card + all 11 locales renamed.
2. **Score bands aligned to 35/50/65/80** — `getStatusText`, `ScoreColors.phaseFor`, and `scoreToneColor` all use spec boundaries.
3. **`dashboard_general` → "General Outlook"** — All 11 locales updated.
4. **Spiritual added to `displayOrder`** — Between health and general.
5. **Per-area low-band status strings** — Area-specific caution language at low scores.
6. **Hardcoded fallback insights removed** — Empty string when backend omits insight; no English leak to non-English users.
7. **Hero subtitle / Ask Navi prompts** — No "overall life" phrasing; uses generic "Day" / "energy" wording.
8. **Glow Fade / Cross-fade Bug Fix** — GlowRegistry counter-based approach; glow transition 200ms.

## Backend ask (still open)

If product wants a true overall life score, request backend exposes:
```
overall_life_score = average(career, love, finance, health, spiritual)
```
Until that field lands, the UI must not present any number as "Overall Life Score".