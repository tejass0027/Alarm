# Stake Alarm — web UI concept

A single-file, buildless React + Tailwind CSS mockup of the Stake Alarm interface. Open
[index.html](index.html) directly in a browser — it loads React, Tailwind, and Babel from
a CDN and compiles the JSX in-page, so no `npm install` or build step is needed.

This is a design concept, not a wired-up app: no backend, no persistence.

## Design direction: split-flap departure board

The interface is styled after mechanical airport/train departure boards: every time and
dollar amount renders as individual flip-tile characters (see the `FlapChar`/`FlapRow`
components). Alarms are a manifest-style list rather than a card grid, and a disabled
alarm shows dimmed flap tiles — like a cancelled flight grayed out on a real board.

Currently a light theme: a soft sky-blue board, white flap tiles, and deep blue ink as
the primary display color (all three color tokens — `board`, `tile`, `accent` — live in
the `tailwind.config` block at the top of the script, so the palette is a quick swap).
Red and green are reserved as semantic colors for risk/danger and success, not part of
the main palette swap.

Typefaces: **Big Shoulders Display** (bold, condensed, industrial signage character) for
headlines and flap numerals, paired with **IBM Plex Mono** (technical, manifest/ticket-printout
feel) for labels and data.

Built in five phases — theme/shell, alarm list, add/edit modal, ringing/challenge screen,
then a polish pass (entrance animation, keyboard focus rings, `prefers-reduced-motion`
support) — followed by two full palette changes: first a dark board with amber glow,
then this light blue version.

Icons are hand-inlined to match the `lucide-react` API (`size`, `strokeWidth`, `className`
props) so the file runs standalone. Dropping this into a real React project just means
deleting the icon block at the top of the script and importing from `lucide-react` instead.
