# River Song Android — Design Prototype

This is the interactive UI design prototype built in Claude's design tool.
It is a **static HTML/JSX preview** — not production Android code.

## How to open it

Open `River Song Android.html` directly in a browser. No build step needed.
It loads React from CDN and runs Babel in-browser to compile the JSX files.

## What's here

| File | Purpose |
|---|---|
| `River Song Android.html` | Entry point — open this |
| `tokens.css` | M3 color/shape/motion tokens, 9 themes |
| `screens.jsx` | All screen components (Speak, Chat, Memory, etc.) |
| `app.jsx` | Phone shell, nav drawer, bottom nav, tweaks panel wiring |
| `holobust.jsx` | Animated holographic bust + audio ring (canvas) |
| `android-frame.jsx` | Generic Material 3 device frame components |
| `design-canvas.jsx` | Pan/zoom design canvas with artboard layout |
| `tweaks-panel.jsx` | Floating tweaks panel (theme, screen, voice state) |

## Screens covered

Speak · Chat · Memory · Inventory · Maintenance · Store · Analytics · Links · Feeds · Reading · Dashboard · Login

## Themes

River Song Blue · Halo · Crimson Dark · Combat · Midnight Violet · Peach Dream · Arctic · Cyberpunk · Dune

## Relationship to the Android app

The Kotlin Android app lives in `app/src/main/java/com/riversongai/`.
This prototype is the visual reference for implementing those screens.
