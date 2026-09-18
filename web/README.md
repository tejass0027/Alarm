# Stake Alarm — web UI concept

A buildless mockup of the Stake Alarm interface, split into two static files:

- [index.html](index.html) — the landing page a visitor sees first: the pitch, a "how it
  works" walkthrough, and a static preview of the app's own cards. Plain HTML + Tailwind,
  no React needed since nothing on it is interactive.
- [app.html](app.html) — the actual dashboard (what "Open the app" links to). A
  single-file React + Tailwind mockup that loads React, Tailwind, and Babel from a CDN
  and compiles its JSX in-page, so no `npm install` or build step is needed.

This is a design concept, not a wired-up app: no backend, no persistence.

## Design direction: playful and rounded

Modeled after gamified consumer apps like Duolingo and Headspace rather than a typical
SaaS dashboard: a warm cream background, a vibrant blue gradient hero card, big
fully-rounded pill buttons and badges, and bouncy `active:scale-95` press feedback
throughout. Color carries meaning — blue for primary actions, red for anything
money-at-risk, green for success/win-rate — via the `azure`/`red`/`green` tokens (plus
their `*Soft` pastel-tint variants) in the `tailwind.config` block at the top of the
script. (The token is named `azure` rather than `blue` to avoid silently overriding
Tailwind's own built-in `blue-*` scale.)

Typefaces: **Fredoka** (rounded, friendly, a bit bouncy) for headlines and big numbers,
paired with **Nunito** (warm, rounded terminals, still readable at small sizes) for body
text and labels.

This replaced two earlier full-palette attempts that didn't land: first a dark navy/violet
SaaS dashboard, then a split-flap departure-board concept (tried in both a dark amber and
a light blue palette). The playful/rounded direction then went through a coral accent
before landing on this blue one. Each rebuild kept the same underlying React state and
interaction logic (toggle, add/edit, delete, deposit, the ringing/challenge flow) — only
the visual layer changed.

Icons are hand-inlined (in both files) to match the `lucide-react` API (`size`,
`strokeWidth`, `className` props) so `app.html` runs standalone. Dropping it into a real
React project just means deleting the icon block at the top of the script and importing
from `lucide-react` instead.

## Streak-gated withdrawals

Solving an alarm's challenge increments a streak counter; giving up resets it to zero.
The hero card's "Withdraw funds" button stays locked (shown with a lock icon) until the
streak reaches `STREAK_GOAL` (21), with a progress bar tracking how close you are. This
mirrors the real product idea: staying disciplined for 21 days in a row is what earns
access to cash out, not just having a balance.
