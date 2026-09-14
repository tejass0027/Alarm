# Stake Alarm — web UI concept

A single-file, buildless React + Tailwind CSS mockup of the Stake Alarm interface. Open
[index.html](index.html) directly in a browser — it loads React, Tailwind, and Babel from
a CDN and compiles the JSX in-page, so no `npm install` or build step is needed.

This is a design concept, not a wired-up app: no backend, no persistence. It's being built
in phases:

1. Theme, top bar, and dashboard shell (balance card + stats) — done
2. Alarm cards (time, stake badge, repeat days, toggle, edit/delete)
3. Add/edit alarm modal
4. Ringing/challenge screen
5. Polish pass

Icons are hand-inlined to match the `lucide-react` API (`size`, `strokeWidth`, `className`
props) so the file runs standalone. Dropping this into a real React project just means
deleting the icon block at the top of the script and importing from `lucide-react` instead.
