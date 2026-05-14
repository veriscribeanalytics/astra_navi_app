# AstraNavi Pricing Plans

Last updated: 2026-05-12

This plan is for an India-first, fully AI-powered Vedic astrology app. All amounts are in INR unless noted.

## Executive Recommendation

Use **Navi Credits** instead of showing raw "messages" in pricing.

Core rule:

> 1 Navi Credit = 1 user-sent AI chat message. The AI reply is included.

Do not charge per minute for text chat. Credit-based pricing is better because typed AI cost depends on messages/tokens, not how long the user keeps the screen open. Use per-minute pricing only later for voice AI or live calls.

Launch with three subscription tiers plus one-time recharge packs:

| Tier | Price | Best For | Monthly Navi Credits |
|---|---:|---|---:|
| Free | 0 | Discovery and trust building | 30 |
| Pro | 199/month or 1,899/year | Daily users | 350 |
| Premium | 399/month or 3,799/year | Heavy and family users | 750 |

No launch plan should promise truly unlimited AI. Your known cost is INR 1 per 5 credits, so every plan needs a hard allowance and top-up packs.

## Credit System

### Credit Definition

| Action | Credit Cost | Notes |
|---|---:|---|
| Normal AI chat message | 1 credit | 1 user message; AI reply included. |
| Suggested question tap | 1 credit | Same as typing the message. |
| Guided AI consult result | 25 credits | Career, marriage, finance, health, timing, remedies. |
| Full Kundli deep AI report | 50 credits | If generated from credits instead of one-time purchase. |
| Match narrative report | 40 credits | Basic score can be free; deep AI report is paid/credits. |
| Monthly life forecast report | 60 credits | Premium can include this once/month. |
| Marriage decision pack | 80 credits | Match, timing, Mangal Dosha, remedies. |

### Deduction Rule

- Subscription credits are used first.
- Paid recharge credits are used after subscription credits.
- Paid recharge credits remain valid for 365 days.
- Monthly subscription credits reset every billing cycle and do not roll over.
- Static/free content should not consume credits.
- Cached daily horoscope should not consume credits for paid users.

## Text Chat vs Per-Minute

Text chat should be sold through credits, not minutes.

Worst active usage assumption:

| User Speed | Credits/Minute | AI Cost/Minute |
|---|---:|---:|
| Normal | 2 to 3 | INR 0.40 to INR 0.60 |
| Heavy | 4 | INR 0.80 |
| Worst case | 5 | INR 1.00 |

At worst case, Pro gives 350 credits = about 70 active chat minutes. Premium gives 750 credits = about 150 active chat minutes. This is enough for the user to understand value without introducing a stressful timer.

## Cost Assumptions

| Item | Assumption |
|---|---:|
| AI cost | INR 1 per 5 credits |
| Cost per credit | INR 0.20 |
| Worst active speed | 4 to 5 credits/minute |
| Worst AI cost/minute | INR 0.80 to INR 1.00 |
| Conservative net after platform fee and 18% tax reserve | About 72% of gross price |
| Hard stress net after higher fees/support/tax buffer | About 60% of gross price |

Google Play's official service-fee docs state that subscriptions are 15%, and that many developers qualify for 15% or less on the first USD 1M revenue. Use this as the launch model, but keep a tax/accounting review before production billing.

## Tier-By-Feature Plan

### Who Each Tier Is For

| Tier | User Type | What They Should Feel |
|---|---|---|
| Free | Curious user | "This app knows enough about me to trust it." |
| Pro | Daily astrology user | "This is my daily horoscope and guidance app." |
| Premium | Heavy/family user | "This covers my family, deeper reports, and serious decisions." |
| Recharge Packs | Non-subscription user | "I can pay only when I need answers." |

### Feature Matrix

| Product Area | Free | Pro | Premium | Recharge / One-Time |
|---|---|---|---|---|
| Daily horoscope | Short daily reading, lucky color/number teaser | Full Pro+ daily horoscope, life areas, timing, caution window, basic remedies | Full daily + 7-day ahead view, deeper remedies, priority refresh | Not needed; daily is subscription retention. |
| Kundli/chart | Free birth chart, Lagna/Moon/Sun, basic planet list | Full Kundli analysis: Dasha, houses, planet strengths, transits | Full Kundli + PDF/share/export + family profiles | Full Kundli Deep Report at INR 69 or 50 credits. |
| Match making | 1 basic match/month with score and Mangal Dosha status | 5 full match reports/month or credit equivalent | 20 full match reports/month or credit equivalent | Match + Mangal Report at INR 79 or 40 credits. |
| AI chat | 30 credits/month, 3/day cap | 350 credits/month | 750 credits/month | Recharge packs from INR 19. |
| Guided consult | Consult categories visible; no full result except signup/sample promo | Full guided consults using credits; 25 credits/result | Same, with more credits and priority generation | One Focus Consult INR 39 or 25 credits. |
| Forecast | Basic daily/weekly preview for general area | Weekly forecast for love, career, health, finance | Monthly life forecast and 7-day ahead view | Monthly Life Forecast INR 99 or 60 credits. |
| Remedies | Generic safe tips only | Basic personalized remedies | Advanced remedies, gemstone guidance with disclaimers | Remedy consult INR 39 or 25 credits. |
| Knowledge hub | Free | Free | Free | Free acquisition surface. |
| History | Limited recent history | Chat, consult, match history | Longer history and search/export | Included if user buys packs. |
| Family profiles | No | 1 profile | 3 profiles | Extra profile pack can be added later. |
| PDF/share/export | No | Limited share cards | Full PDF/share/export | Report purchases include export. |

## Subscription Worst-Case Profit

This assumes the user uses every monthly credit. This is the correct stress test because light users will be much more profitable.

### Conservative Net Case

Assumption: net revenue is 72% of price after platform fee and tax reserve.

| Plan | Gross Price | Credits | AI Cost If Fully Used | Net Revenue | Profit After AI | Margin After AI |
|---|---:|---:|---:|---:|---:|---:|
| Free | 0 | 30 | 6 | 0 | -6 | Loss |
| Pro | 199 | 350 | 70 | 143 | 73 | 51% |
| Premium | 399 | 750 | 150 | 287 | 137 | 48% |

If you reserve extra cost for daily horoscope, report generation, storage, support, and infra:

| Plan | Profit After AI | Extra Product Cost Reserve | Estimated Contribution | Contribution Margin |
|---|---:|---:|---:|---:|
| Free | -6 | 0 to 3 | -6 to -9 | Loss leader |
| Pro | 73 | 30 | 43 | 30% |
| Premium | 137 | 50 | 87 | 30% |

### Hard Stress Case

Assumption: net revenue is only 60% of price after higher platform/tax/support pressure.

| Plan | Gross Price | Credits | AI Cost If Fully Used | Stress Net | Product Cost Reserve | Estimated Contribution |
|---|---:|---:|---:|---:|---:|---:|
| Free | 0 | 30 | 6 | 0 | 0 to 3 | -6 to -9 |
| Pro | 199 | 350 | 70 | 119 | 30 | 19 |
| Premium | 399 | 750 | 150 | 239 | 50 | 39 |

Verdict: Pro and Premium survive worst-case full usage, but margins become thin under the hard stress case. That is why unlimited AI should not be offered.

## Recommended Recharge Packs

These safer pack sizes replace the earlier more generous large packs. They keep one-time users profitable even if they use every credit.

### Conservative Net Case

Assumption: net revenue is 72% of price.

| Pack | Price | Credits | AI Cost | Net Revenue | Profit After AI | Margin After AI |
|---|---:|---:|---:|---:|---:|---:|
| Quick Ask | 19 | 25 | 5 | 14 | 9 | 64% |
| Mini Chat | 49 | 90 | 18 | 35 | 17 | 49% |
| Deep Chat | 99 | 220 | 44 | 71 | 27 | 38% |
| Power Chat | 199 | 450 | 90 | 143 | 53 | 37% |
| Navi Max | 399 | 850 | 170 | 287 | 117 | 41% |

### Hard Stress Case

Assumption: net revenue is 60% of price.

| Pack | Price | Credits | AI Cost | Stress Net | Profit After AI |
|---|---:|---:|---:|---:|---:|
| Quick Ask | 19 | 25 | 5 | 11 | 6 |
| Mini Chat | 49 | 90 | 18 | 29 | 11 |
| Deep Chat | 99 | 220 | 44 | 59 | 15 |
| Power Chat | 199 | 450 | 90 | 119 | 29 |
| Navi Max | 399 | 850 | 170 | 239 | 69 |

Verdict: Recharge packs are safer than unlimited subscriptions because the maximum cost is fixed upfront.

## Guided AI Consultation Packs

Guided consults should be available through credits and also as simple one-time purchases.

| Pack | Price | Includes | Equivalent Credits | Cost Reserve | Notes |
|---|---:|---|---:|---:|---|
| One Focus Consult | 39 | 1 guided AI consult | 25 | 5 | Good impulse purchase. |
| Three Consults | 99 | 3 guided AI consults | 75 | 15 | Good for non-subscription users. |
| Ten Consults | 249 | 10 guided AI consults | 250 | 50 | Good for heavy but non-recurring users. |

Consult categories:

- Career and job timing.
- Marriage and relationship.
- Money and property.
- Health and wellbeing, with medical disclaimer.
- Education and exams.
- Remedies and spiritual practice.
- Muhurat/timing guidance.

## One-Time Reports

Reports are better as either one-time purchases or Premium benefits. Free users should see previews, not full generated reports.

| Report | Price | Credit Equivalent | Free | Pro | Premium |
|---|---:|---:|---|---|---|
| Full Kundli Deep Report | 69 | 50 | Preview only | Buy or use credits | 1 included/quarter or use credits |
| Match + Mangal Report | 79 | 40 | 1 basic score/month | 5 full/month or credits | 20 full/month or credits |
| Monthly Life Forecast | 99 | 60 | Preview only | Buy or use credits | 1 included/month |
| Marriage Decision Pack | 149 | 80 | Preview only | Buy or use credits | Buy or use credits |

## Free Tier Details

Free should be useful, but it must not become a heavy-cost plan.

Give for free:

- Birth chart visual.
- Basic Kundli identity: Lagna, Moon sign, Sun sign, Nakshatra.
- Short daily horoscope.
- Lucky color and lucky number.
- Basic weekly forecast preview.
- 1 basic match score/month.
- Knowledge hub.
- 30 Navi Credits/month with 3/day cap.

Do not give free:

- Full daily horoscope.
- Deep Kundli interpretation.
- Unlimited match reports.
- Full guided AI consults.
- Advanced remedies.
- PDF export.
- Family profiles.

## Pro Tier Details

Pro is the main paid plan. It should feel like the daily-use version of AstraNavi.

Give in Pro:

- Full Pro+ daily horoscope.
- Daily timing and caution windows.
- Love, career, health, finance area guidance.
- Full Kundli analysis.
- Weekly forecast for all areas.
- 350 Navi Credits/month.
- Guided consults through credits.
- 5 full match reports/month.
- Basic personalized remedies.
- Full chat and consult history.
- 1 profile.

Do not include:

- Unlimited AI.
- Family profiles.
- Full PDF/export suite.
- Monthly deep report unless bought separately.

## Premium Tier Details

Premium is for heavy users, couples, and families.

Give in Premium:

- Everything in Pro.
- 750 Navi Credits/month.
- 3 family profiles.
- Full daily horoscope plus 7-day ahead view.
- 1 monthly life forecast report included.
- More match reports.
- Advanced remedies and gemstone guidance with disclaimers.
- PDF/share/export for Kundli, match, and reports.
- Priority generation during load spikes.
- Early access to new AI modes.

Do not include:

- Unlimited AI.
- Voice AI minutes until real voice cost is known.
- Human astrologer sessions in the base subscription.

## Entitlement Rules

- Use the word **credits** in pricing UI.
- In tooltips, explain: "1 credit = 1 message you send to Navi. Navi's reply is included."
- Show remaining credits before the user starts a chat or guided consult.
- Warn when the user has less than 25 credits before starting a guided consult.
- Use a hard cap, not overage billing.
- Rate limit text chat at 5 user messages/minute maximum.
- Add daily abuse caps: Free 3/day, Pro 60/day, Premium 150/day, recharge-only users 150/day.
- Allow backend-configured prices, credit amounts, and sale offers.

## Competitor Pricing Snapshot

| Competitor | Model | Current Public Pricing | Implication For AstraNavi |
|---|---|---|---|
| Vedic AstroGPT | AI question credits | 1 question: INR 49; 7: INR 200; 15: INR 399; 40: INR 999 | Question packs usually cost INR 25 to INR 49/question. AstraNavi can be cheaper with credits. |
| KundliGPT | AI subscription | Free: 3 questions; Pro yearly: INR 2,499/year; advertises unlimited chat | Strong yearly anchor. AstraNavi Pro at INR 1,899/year is lower, with clearer usage limits. |
| YastroTalk | AI credits subscription | Basic INR 99/month, Starter INR 249/month, Silver INR 499/month, Gold INR 1,499/month, Platinum INR 2,999/month | AstraNavi Pro at INR 199 fits between Basic and Starter; Premium at INR 399 undercuts Silver. |
| AstroViah | AI subscription | Premium INR 299/month; Yearly INR 1,999/year | AstraNavi Pro should be below monthly Premium and close to yearly value, with stronger Vedic depth. |
| SanatanAI | Request subscription | INR 100/month for 10 requests; INR 800/month for 50; INR 1,200/month for 100 | Requests are expensive. AstraNavi should sell more flexible credit usage. |
| TheAIAstrology | One-time question packs | INR 399 for 5 questions; INR 999 for 11; INR 2,999 for 50 | One-time packs can be much cheaper while still profitable. |
| GrahaGuru | AI question packs/reports | Free tier plus 5 questions INR 199, 10 INR 299, 25 INR 499; reports INR 499 | AstraNavi packs should beat question-pack pricing, not compete with "free forever" marketing. |
| Astroyogi | Human astrologer chat/call | Public listings show rates such as INR 12/min promos and up to INR 116/min | Human astrology is a high-price anchor. AstraNavi AI can be far cheaper per active minute. |
| AstroTalk | Human astrologer wallet/per-minute | App listings describe wallet recharge and per-minute chat/call; App Store wallet IAPs range from USD 5 to USD 1,000 | Use as market anchor, not direct AI competitor. |

## Why This Pricing Works

- Credits are easier to understand than tokens.
- Credits feel fairer than per-minute billing for text chat.
- Free users get real value without creating uncontrolled AI cost.
- Pro has enough credits for daily use while staying profitable.
- Premium works for families and heavy users without promising unlimited AI.
- Recharge packs support Indian users who avoid subscriptions.
- Full Kundli, match, consult, and reports can be monetized without hiding the basic chart.

## Sources

- Vedic AstroGPT pricing: https://vedicastrogpt.com/
- KundliGPT pricing: https://kundligpt.com/en/pricing/
- YastroTalk pricing: https://www.yastrotalk.com/pricing
- AstroViah pricing: https://www.astroviah.com/pricing
- SanatanAI pricing: https://www.sanatanai.com/pricing
- TheAIAstrology premium plans: https://theaiastrology.com/premium-plans
- GrahaGuru pricing snapshot: https://grahaguru.in/
- Astroyogi chat/call listings: https://www.astroyogi.com/astrologer/chat
- AstroTalk App Store / Google Play listings: https://apps.apple.com/in/app/astrotalk-talk-to-astrologer/id1208433822 and https://play.google.com/store/apps/details?id=com.astrotalk
- Google Play service fees: https://support.google.com/googleplay/android-developer/answer/112622
