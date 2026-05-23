# AstraNavi Next Steps

## CHUNK 3 — Chat Transition Overlay

After tapping `Chat with <guide>`, show a 600–900 ms transition overlay:
- Avatar scales up slightly
- Text: "Connecting you with <guide>…"
- Subtext: "Reading your question context"
- Keep it short — anything longer feels laggy.
- Implement as a small overlay composable above the chat route, driven by a
  `connectingTo: ChatAvatar?` state that clears on first frame after navigation
  resolves (or after the timeout).

**Status:** ❌ — No `connectingTo` state or overlay composable exists. Navigation goes directly from `AvatarSelectionScreen` → `ChatScreen` with no intermediate animation.

## CHUNK 4 — Contextual Guide Preselection & Suggestion Card

### Preselect guide by context

When the user enters chat from a Home card like "Ask Navi About Love":
- Preselect the right guide (Love → Meera, Career → Arya, default → Navi).
- Either prefill the input with a contextual question, OR show a single
  suggestion card above the input:
  - "From today's Love reading"
  - "Emotional understanding supports bonds today."
  - Button: "Ask Meera"
- Plumb this through as optional `seedPrompt: String?` and
  `seedContext: String?` on the chat navigation route.
- Current `prompt` param sends the question immediately; `seedPrompt` should
  only prefill / suggest, not auto-send.

**Status:** ❌ — No `seedPrompt`/`seedContext` route params. Home cards go to `chat_select` (selector), not directly to `chat?avatarId=relationship_guide`.

### Recommended-guide highlighting by context

When entering from a contextual card (Love, Career), highlight the
contextually recommended guide instead of the global default — e.g. pass a
`recommendedAvatarId` override down to `AvatarSelectionScreen`.

**Status:** ❌ — `AvatarSelectionScreen` highlights based on `avatar.isDefault || avatar.avatarId == defaultAvatarId` (global default only). No `recommendedAvatarId` override param exists.

### Suggestion card above input

Show contextual card in `ChatEmptyState`:
- "From today's Love reading"
- "Emotional understanding supports bonds today."
- Button: "Ask Meera"

**Status:** ❌ — `ChatEmptyState` only shows 4 generic suggested questions, no contextual suggestion card.

## CHUNK 5 — Chat Header Small-Phone Overflow

On small phones, collapse the right side of the chat header to credits + overflow menu only (instead of showing credits + history icon + new-chat icon individually as three separate `IconButton`s).

**Status:** ❌ — All three actions are always rendered as separate `IconButton`s regardless of screen size.

## CHUNK 6 — Avatar Drawables Catalog Verification

`chatAvatarDrawable()` maps all 5 known IDs to existing drawables (`avatar_navi`, `avatar_arya`, `avatar_meera`, `avatar_anand`, `avatar_rishi`). Anything not mapped falls back to `avatar_navi`.

**Remaining work:** Before launch, confirm the full backend avatar catalog matches these 5 IDs. If the backend adds new IDs, add drawable mappings + assets or the fallback will silently show the wrong face.

**Status:** ✅ for known catalog, ❌ for backend catalog verification