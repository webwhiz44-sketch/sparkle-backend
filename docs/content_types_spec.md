# Content Types Spec — Gul

## Overview

Gul has three distinct content formats. Each serves a different emotional purpose and is differentiated by format constraints, UI copy, and card design — so users instinctively know which one to use.

---

## 1. Posts

**What it is:** Your everyday social presence. Quick thoughts, achievements, questions, community discussions, polls, photos. Attributed to you by name.

**Tone:** Social, conversational, public

**Format:**
- No title
- Content: 1000 character limit
- Optional image
- Optional poll
- Optional community tag
- Topic tags

**UI copy:**
- Input placeholder: *"What's on your mind?"*
- Submit button: *"Post"*

**Card design:** Compact social card with avatar, name, content, like/comment/save actions.

---

## 2. Spill the Tea

**What it is:** Short, raw, in-the-moment sharing. Venting, hot takes, confessions, real talk. The content is vulnerable — anonymity is optional, not the default.

**Tone:** Unfiltered, reactive, emotional

**Format:**
- No title
- Content: **500 character limit** (forces brevity — if it doesn't fit, it belongs in Stories)
- Optional image
- Topic tags (up to 3)
- Anonymous toggle (optional — off by default)

**UI copy:**
- Input placeholder: *"What's on your mind right now?"*
- Submit button: *"Spill it ✦"*
- Character counter counts down from 500

**Card design:** Compact card, no title, shows anon badge only when anonymous.

---

## 3. Radiant Stories

**What it is:** Long-form personal narratives. You've been through something and want to tell the story properly — with a beginning, middle, and end. Meant to inspire or help other women going through similar things. Always attributed to you by name.

**Tone:** Reflective, narrative, inspiring

**Format:**
- Title: **150 character limit** (required)
- Body: **10,000 character limit** (~1,500–2,000 words)
- Optional cover image
- Topic tags

**UI copy:**
- Title placeholder: *"Give your story a title"*
- Body placeholder: *"Tell your story..."*
- Submit button: *"Publish Story ✦"*
- Body shows word count going up (not countdown)

**Card design:** Blog-style card with cover image, bold title, body preview, author, and read time badge.

---

## How the Difference is Communicated to Users

| Signal | Spill the Tea | Radiant Stories |
|---|---|---|
| Has a title field? | No | Yes (required) |
| Character counter | Counts down from 500 | Counts up toward 10,000 |
| Submit CTA | "Spill it ✦" | "Publish Story ✦" |
| Card has cover image? | No | Yes |
| Card shows read time? | No | Yes |
| Time to write | ~30 seconds | Minutes |

**Rule of thumb for users:** If it fits in 500 characters, Spill it. If it needs a title, write a Story.

---

## Character Limits — Frontend & Backend

| Field | Limit | Enforced in |
|---|---|---|
| Post content | 1,000 chars | Flutter `maxLength` + backend `@Size` |
| Spill content | 500 chars | Flutter `maxLength` + backend `@Size` |
| Story title | 150 chars | Flutter `maxLength` + backend `@Size` |
| Story body | 10,000 chars | Flutter `maxLength` + backend `@Size` |

---

## Status

- [ ] Update Spill the Tea: reduce `maxLength` from 2500 → 500, update placeholder + CTA copy
- [ ] Update Radiant Stories: add `maxLength` 10,000 on body, 150 on title, update placeholder + CTA copy
- [ ] Update Posts: increase `maxLength` from 500 → 1000
- [ ] Add backend `@Size` validation to match all limits
