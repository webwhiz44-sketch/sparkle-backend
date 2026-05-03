# Typography Spec — Gul

## Fonts

| Role | Font | Used for |
|---|---|---|
| **Display / Headings** | DM Serif Display | App name, page titles, section headings, story titles |
| **Body / UI** | Inter | Body text, post content, buttons, labels, captions, form fields |

---

## Type Scale

| Level | Font | Size | Weight | Used for |
|---|---|---|---|---|
| **Title** | DM Serif Display | 24–28 | 700 | AppBar titles, page headings (e.g., "Posts", "Gul", "Radiant Stories") |
| **Section Heading** | DM Serif Display | 20–22 | 700 | Section labels within a screen (e.g., "Trending", "My Communities") |
| **Card Heading** | DM Serif Display | 18 | 700 | Story card titles, community names as headings |
| **Subheading** | Inter | 18 | 600 | Screen sub-labels, form section titles |
| **Body** | Inter | 16 | 400 | Post content, story body, descriptions |
| **Secondary** | Inter | 14 | 400/500 | Author names, community names inline, list items |
| **Label** | Inter | 13 | 600 | Tags/chips, badge text, tab labels |
| **Meta** | Inter | 12 | 400 | Timestamps, like counts, comment counts, read time |
| **Button** | Inter | 14–16 | 700 | All CTA buttons |

---

## Rules

1. **No italics** — ever. No `FontStyle.italic` anywhere in the app.
2. **No inline font overrides** — always use `AppTextStyles.*` constants, never hardcode font names or sizes.
3. **DM Serif Display is for display only** — titles and headings. Never use it for body copy, captions, or buttons.
4. **Inter handles everything else** — body, UI chrome, buttons, labels, meta.

---

## Flutter Constants (`lib/theme/app_text_styles.dart`)

```dart
// Display / Headings — DM Serif Display
AppTextStyles.pageTitle       // 28, w700 — AppBar titles, hero headings
AppTextStyles.sectionHeading  // 22, w700 — Section labels
AppTextStyles.cardHeading     // 18, w700 — Story titles, card headings

// Body / UI — Inter
AppTextStyles.subheading      // 18, w600 — Sub-labels, form sections
AppTextStyles.body            // 16, w400 — Post content, story body
AppTextStyles.bodyMedium      // 16, w500 — Slightly emphasised body
AppTextStyles.secondary       // 14, w400 — Author names, secondary info
AppTextStyles.secondaryMedium // 14, w600 — Inline labels, usernames
AppTextStyles.label           // 13, w600 — Tags, chips, badges
AppTextStyles.meta            // 12, w400 — Timestamps, counts
AppTextStyles.button          // 15, w700 — Button labels
```

---

## Examples

| Element | Style |
|---|---|
| "Gul" (app name) | `pageTitle` — DM Serif Display 28 |
| "Posts" (AppBar) | `pageTitle` — DM Serif Display 24 |
| "Radiant Stories" (section) | `sectionHeading` — DM Serif Display 22 |
| Story card title | `cardHeading` — DM Serif Display 18 |
| Post body text | `body` — Inter 16 |
| "suhani sharma" (author) | `secondary` — Inter 14 |
| "#glowup" (tag chip) | `label` — Inter 13 |
| "2h ago · 3 likes" | `meta` — Inter 12 |
| "Publish Story ✦" (button) | `button` — Inter 15 w700 |
