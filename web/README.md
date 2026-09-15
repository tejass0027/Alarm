# Stake Alarm — web UI concept

A single-file, buildless React + Tailwind CSS mockup of the Stake Alarm interface. Open
[index.html](index.html) directly in a browser — it loads React, Tailwind, and Babel from
a CDN and compiles the JSX in-page, so no `npm install` or build step is needed.

This is a design concept, not a wired-up app: no backend, no persistence.

## Design direction: split-flap departure board

The interface is styled after mechanical airport/train departure boards: every time and
dollar amount renders as individual flip-tile characters (see the `FlapChar`/`FlapRow`
components), on a dark board casing with a warm amber display glow. Alarms are a
manifest-style list rather than a card grid, and a disabled alarm shows dimmed flap tiles —
like a cancelled flight grayed out on a real board.

Typefaces: **Big Shoulders Display** (bold, condensed, industrial signage character) for
headlines and flap numerals, paired with **IBM Plex Mono** (technical, manifest/ticket-printout
feel) for labels and data.

Built in five phases — theme/shell, alarm list, add/edit modal, ringing/challenge screen,
then a polish pass (entrance animation, keyboard focus rings, `prefers-reduced-motion`
support) — followed by this full visual redesign once the original purple SaaS look didn't
land.

Icons are hand-inlined to match the `lucide-react` API (`size`, `strokeWidth`, `className`
props) so the file runs standalone. Dropping this into a real React project just means
deleting the icon block at the top of the script and importing from `lucide-react` instead.
