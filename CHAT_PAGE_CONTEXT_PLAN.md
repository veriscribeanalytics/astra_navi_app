# Universal Navi Page Context Implementation Plan

## Goal

When the user clicks **Ask Navi** from a specific product page, the frontend sends one tiny hint, and the backend expands it into a small trusted LLM context block.

Example: user is on the Career page and clicks Ask Navi.

Frontend sends:

```json
{
  "context": {
    "source": "career"
  }
}
```

Backend uses the user's existing stored chart/profile/forecast data, extracts career-relevant details, caps the size, and sends that compact block to the LLM.

## V1 Contract

Endpoint stays the same:

```http
POST /api/chats/{chatId}/messages
```

V1 request body:

```json
{
  "text": "Should I change my job soon?",
  "language": "en",
  "mode": "normal",
  "context": {
    "source": "career"
  }
}
```

The `context` object is optional. Normal chat continues working without it.

V1 only needs `source`.

Do not require `section` or `itemId` in the first implementation. Those can be added later when a page has multiple Ask Navi buttons that need more precision.

## Source Values

Start with a small allowlist:

```text
career
kundli
horoscope
transit
match
profile
```

If the frontend sends no context, backend behaves exactly like today.

If the frontend sends an unsupported source, backend should not fail the whole chat. It should ignore page context and continue normal chat, while optionally returning metadata that context was ignored.

## Backend Responsibility

Frontend must not send full Kundli/report/forecast data.

Backend should:

1. Receive the tiny `source` hint.
2. Use existing trusted backend data already available for the user.
3. Select only the relevant data for that source.
4. Convert it into a short text block.
5. Enforce a strict size cap before adding it to the LLM prompt.

Backend should not:

- Recompute a full chart just because page context was sent.
- Generate a new full report inside chat.
- Trust frontend-supplied astrology text as source of truth.
- Add huge JSON directly into the prompt.

## Prompt Budget

Use a fixed character budget for the extra page-context block:

| Chat mode | Extra page context cap |
| --- | --- |
| `quick` | 600 chars |
| `normal` | 1200 chars |
| `deep` | 2000 chars |

If source data is larger, select the most important fields and truncate safely.

This keeps LLM cost predictable and avoids weakening answer quality with too much context.

## Implementation Steps

### 1. Update request models

File: `src/models.py`

Add:

```python
class ChatPageContext(BaseModel):
    model_config = ConfigDict(extra="forbid")

    source: str = Field(..., min_length=1, max_length=40)
```

Then update `SendMessageRequest`:

```python
context: Optional[ChatPageContext] = None
```

Keep v1 source-only. Add `section` and `itemId` later only if needed.

### 2. Add backend context helper

New file:

```text
src/chat/page_context.py
```

Suggested public function:

```python
def build_page_context_prompt(user: dict, source: str, mode: str) -> tuple[str, dict]:
    ...
```

Return:

- prompt block string
- metadata dict, for example:

```json
{
  "contextUsed": true,
  "contextSource": "career",
  "contextChars": 842
}
```

### 3. Source mapping logic

The helper should be simple and cheap.

V1 mapping:

| `source` | Backend data to use |
| --- | --- |
| `career` | Existing `astrology_data`, `normalized_chart`, `chart_context`; extract career themes, work strengths, dasha/timing if already stored. |
| `kundli` | Existing `astrology_data` / `normalized_chart`; include compact chart summary only. |
| `horoscope` | Existing stored/current horoscope data if already available; otherwise just page-origin line. |
| `transit` | Existing transit context if cheap/available; no expensive recomputation in v1. |
| `match` | Match data only if already available to the user/chat flow; otherwise page-origin line. |
| `profile` | Basic profile/life context already on user record. |

For unknown or unavailable data, return a tiny block:

```text
PAGE CONTEXT:
The user opened Navi from the Career page.
No additional backend page data was available.
```

### 4. Keep extraction deterministic

Do not call the LLM to summarize page context before the main LLM call.

Use deterministic field selection:

- Pick top career themes from stored profile data.
- Pick current dasha/timing if already present.
- Pick 2-4 strongest relevant chart signals if already present.
- Convert to concise plain text.
- Apply final character cap.

This avoids extra API cost and keeps backend load low.

### 5. Wire into chat route

File: `routes/chat.py`

In `send_chat_message`, after `prepare_llm_context(...)` builds the normal user/chart context:

1. If `req.context` exists, call `build_page_context_prompt(user, req.context.source, mode)`.
2. Append returned text to `user_msg_base`.
3. Add returned metadata to the final SSE metadata event.

Do not change the existing chart-context flow.

### 6. Regeneration behavior

For v1, do not add context to `RegenerateMessageRequest`.

Reason: regeneration currently reuses the previous question and history. Adding page context there needs either:

- storing the original context with the message, or
- frontend resending it.

Keep regeneration unchanged in v1 to avoid scope creep.

### 7. Frontend changes

For page Ask Navi buttons, send only:

```ts
context: {
  source: "career"
}
```

Examples:

```ts
// Career page
context: { source: "career" }

// Kundli page
context: { source: "kundli" }

// Horoscope page
context: { source: "horoscope" }
```

For normal/global chat buttons, omit `context`.

Frontend should not send large data.

### 8. Tests

Add/adjust tests for:

1. `SendMessageRequest` accepts `context: {"source": "career"}`.
2. `SendMessageRequest` rejects large/unknown extra fields inside `context`.
3. Chat works exactly as before when `context` is omitted.
4. Known source produces a capped page context block.
5. Unknown source does not break chat.
6. SSE metadata includes `contextUsed`, `contextSource`, and `contextChars` when context is used.
7. Context cap differs by mode: quick/normal/deep.

## Load And Cost Decision

Keep the work on the backend.

Reason:

- Backend already has trusted user/chart data.
- Frontend sending big data increases API payload size and can be stale.
- Backend deterministic extraction is cheap compared to one LLM call.
- Character caps keep LLM cost predictable.

Expected backend load is low if v1 does not recompute charts or reports. It should only read existing fields and format a small string.

## Future V2

Add optional narrowing fields only after v1 works:

```json
{
  "context": {
    "source": "career",
    "section": "job_change",
    "itemId": "career-job-change-card"
  }
}
```

Use these only when one page has multiple Ask Navi buttons and backend needs to know which exact card/section the user clicked.

