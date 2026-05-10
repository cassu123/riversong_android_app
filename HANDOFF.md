# River Song AI — Project Handoff

> Paste this file into Claude Code at the start of any new session to restore full context.
> To add screenshots: drag images into this file in VS Code, or paste image paths using the format `![label](./docs/screenshots/filename.jpg)`

---

## Project Overview

River Song AI is a personal home AI assistant with two surfaces:
- **Web app** — React/Vite frontend served from a Python FastAPI backend at `riversongai.com`
- **Android app** — Native Kotlin app (`riversong_android_app/`) that is a **thin client** to the same server

The Android app calls the exact same REST/WebSocket endpoints as the browser. One setting changed in the app reflects immediately on the web and vice versa. No local AI, no local processing — all intelligence lives on the server.

**Voice pipeline:** Mic → Whisper STT → Ollama LLM → Piper TTS → Speaker  
**Users:** Cheryl (admin), husband, sister's family (~5-6 people total)  
**Domain:** `riversongai.com` — Cloudflare proxy, home server AMD FX-8350 + GTX 1050 Ti

---

## Android App — Current State

**Architecture:** Single Activity (MainActivity), Jetpack Navigation, MVVM + LiveData, Koin DI, Retrofit + OkHttp WebSocket, Material3, ViewBinding

**Build target:** Min SDK 26 (Android 8), Target SDK 34

**Theme system:** 9 themes stored in SharedPreferences (`river_song_prefs`, key `app_theme`). Activity recreates on switch. SVG logo recolors via CSS variables in WebView.

### Themes

| Key | Name | Accent | Background |
|---|---|---|---|
| `default` | River Song | `#96CBFF` | `#0F1316` |
| `halo` | Halo Blue | `#35A7FF` | `#080C13` |
| `crimson` | Crimson Dark | `#C53A1F` | `#140C0B` |
| `combat` | Combat | `#3DCC79` | `#0A100A` |
| `violet` | Midnight Violet | `#9B6B9E` | `#1A1025` |
| `peach` | Peach Dream | `#D66C59` | `#FEE7D9` |
| `arctic` | Arctic | `#4A7AA8` | `#DCE6F0` |
| `cyberpunk` | Cyberpunk | `#E8FF00` | `#050505` |
| `dune` | Dune | `#DEB651` | `#0A0804` |

> Peach Dream and Arctic are **light themes** — wordmark uses dark text `#1A1A2E` on these two only.

---

## Screen-by-Screen Comparison

Instructions: For each screen below, paste a screenshot from the Android app on the left and the web browser on the right. Drag images directly into VS Code or add paths manually.

---

### Home / Dashboard

**Web endpoint:** `/` (DashboardPage.jsx)  
**Android:** `HomeFragment.kt`  
**API:** `GET /api/dashboard`, `GET /api/feeds/weather`, `GET /api/routines`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

**Web has:**
- COMMAND NODE label with date/time greeting
- SPEAK TO RIVER section with standing-by status
- Quick Actions: LISTEN · NEW ROUTINE · HOME SCENE · LOG EVENT
- Active Routines list
- System stats (ops/latency/facts/uptime) — admin only

**Android has:**
- Greeting card with date + River Song online status
- 4 navigation cards: Speak, Chat, Home, Memory
- Quick Actions row (LISTEN · NEW ROUTINE · HOME SCENE · LOG EVENT) — admin only
- Stats grid: Memory Facts, Summaries, Uptime, Latency
- Weather widget

**Known differences:**
- Android stats grid shows 0ms latency / 0°C temperature sometimes — check dashboard API response
- Web has ARRANGE mode for widget layout — Android does not

---

### Chat

**Web endpoint:** `/chat`  
**Android:** `ChatFragment.kt`  
**API:** WebSocket `/ws/conversation?token=&model=`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

**Status:** ✅ Aligned  
**Notes:** Model selector, connection status chip, history sidebar all implemented

---

### Speak (Voice)

**Web endpoint:** `/speak`  
**Android:** `SpeakFragment.kt`  
**API:** WebSocket `/ws/conversation?token=&model=` with audio chunks

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

**Status:** ✅ Core aligned  

---

### Feeds — News

**Web endpoint:** `/feeds` → News tab  
**Android:** `NewsFragment.kt` inside `FeedsFragment`  
**API:** `GET /api/feeds/news?category=`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

**Status:** ✅ Aligned  
**Android has:** Article count ("56 ARTICLES"), refresh countdown timer, Refresh Now button, category chips (All/World/US/Tech/Business/Science/Health/Sports)

---

### Feeds — Weather

**Web endpoint:** `/feeds` → Weather tab  
**Android:** `WeatherFragment.kt`  
**API:** `GET /api/feeds/weather`, `PUT /api/feeds/preferences`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

**Status:** ✅ Aligned  
**Android has:** GPS location, current conditions (temp/feels like/humidity/wind/gusts/UV/visibility), hourly scroll, 7-day forecast with sunrise/sunset, air quality card (AQI/PM2.5/PM10/O₃/NO₂/CO), weather alerts, °F/°C toggle

**Known issue:** Verify weather_unit preference is set to `fahrenheit` for US users — server defaults to celsius

---

### Feeds — Sports

**Web endpoint:** `/feeds` → Sports tab  
**Android:** `SportsFragment.kt`  
**API:** `GET /api/sports/following`, `GET /api/sports/results`, `GET /api/sports/fixtures`, `GET /api/sports/standings`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

**Status:** ✅ Aligned  
**Android has:** Team chips (All + followed teams), Results tab, Fixtures tab, Standings tab with dynamic columns

---

### Feeds — Stocks

**Web endpoint:** `/feeds` → Stocks tab  
**Android:** `StocksFragment.kt`  
**API:** `GET /api/feeds/stocks`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

**Status:** ✅ Aligned  
**Android has:** Top Gainer banner, sort by Move%/A-Z/Price, cards showing Open/High/Low/Prev Close, ▲/▼ color indicators

---

### Memory

**Web endpoint:** `/memory`  
**Android:** `MemoryFragment.kt` + sub-fragments  
**API:** `GET /api/memory/facts`, `/preferences`, `/summaries`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

**Status:** ✅ Aligned  
**Android has:** 3 tabs (Facts/Preferences/Summaries), fact table with KEY/VALUE/SOURCE/DATE columns, source chips (inferred=teal, manual=grey), search, add fact dialog, delete with confirmation

---

### Settings

**Web endpoint:** `/settings`  
**Android:** `SettingsFragment.kt`  
**API:** `GET/POST /api/settings/llm`, `/api/settings/voices`, `/api/settings/memory-ttl`, `/api/settings/orchestration`, `/api/admin/model-visibility`, `/api/admin/feature-visibility`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

**Status:** ✅ Aligned  
**Android has:** AI model selector (friendly names), cloud fallback toggle, n8n orchestration section (admin), voice list with ▶ Preview, memory TTL radio group, admin feature visibility toggles, admin model visibility toggles

---

### Profile / User Dashboard

**Web endpoint:** `/profile`  
**Android:** `UserDashboardScreen.kt`  
**API:** `GET/PATCH /api/user/profile`, `POST /api/user/change-password`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

**Status:** ✅ Aligned  
**Android has:** Avatar + name + email + role chip, fact/routine counts, 9-theme grid with color preview swatches, identity form (first/last/callsign), password change, smart home status, sign out

---

### Store / Commerce

**Web endpoint:** `/tools/inventory` (commerce)  
**Android:** `StoreFragment.kt`  
**API:** `GET /api/commerce/workspaces`, `/products`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

**Status:** ⚠️ Partial  
**Known gap:** Android only supports one workspace (firstOrNull). Web supports multiple workspaces with switching.

---

### Inventory (Home Items)

**Web endpoint:** `/home/inventory`  
**Android:** `InventoryFragment.kt`  
**API:** `GET /api/inventory/homes`, `/items`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

---

### Maintenance Pulse

**Web endpoint:** `/tools/maintenance`  
**Android:** `MaintenanceFragment.kt`  
**API:** `GET /api/vehicles/`, `/logs`, `/specs/checkpoints`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

---

### Culinary

**Web endpoint:** `/culinary`  
**Android:** `CulinaryFragment.kt`  
**API:** `GET /api/culinary/recipes`, `/household`, `/household/banned`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

**Status:** ✅ 3 tabs: Recipes (search + filter), Banned Items (swipe to delete), Equipment (toggle switches)

---

### Reading Shelf

**Web endpoint:** `/reading`  
**Android:** `ReadingFragment.kt`  
**API:** `GET /api/reading/shelf`, `/stats`, `/libby/loans`, `/libby/holds`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

**Status:** ✅ Book shelf + status filter + stats + Libby loans/holds section (if Libby connected)

---

### Analytics

**Web endpoint:** `/analytics`  
**Android:** `AnalyticsFragment.kt`  
**API:** `GET /api/analytics/snapshots`, `/platforms`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

**Status:** ✅ Platform tiles, expand/collapse, AI insights per platform, snapshot adding, 7D/30D/90D filter

---

### Routines

**Web endpoint:** `/routines`  
**Android:** `RoutinesFragment.kt` (admin only)  
**API:** `GET /api/routines`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

---

### Home Node

**Web endpoint:** `/home/node`  
**Android:** `HomeNodeFragment.kt` (admin only)  
**API:** `GET /api/home/devices`, `POST /api/home/action`

| Web | Android App |
|---|---|
| *(paste web screenshot here)* | *(paste app screenshot here)* |

---

## Branding Assets

| Asset | File | Used In |
|---|---|---|
| Horizontal logo PNG | `app/src/main/res/drawable/river_song_logo.png` | Splash screen, profile page |
| Splash background | `app/src/main/res/drawable/rs_splash_bg.jpg` | SplashActivity |
| Animated SVG logo | Generated in `utils/LogoWebView.kt` | MainActivity toolbar |

**Logo source files in Google Drive → River Photos folder:**
- `Copilot_20260510_144716.png` — transparent horizontal logo (use for in-app PNG)
- `ChatGPT Image May 10 2026.png` — rounded square R mark (use for app icon)
- `507bbfd6...jpg` — constellation art (use for splash background)

---

## Known Issues / Open Items

| # | Screen | Issue | Priority |
|---|---|---|---|
| 1 | Weather | Verify °F default is set in server preferences for US users | High |
| 2 | Store | Only supports one workspace — multi-workspace switching missing | Medium |
| 3 | Home | Latency/uptime showing 0 sometimes — check dashboard API | Medium |
| 4 | Logo | SVG WebView logo only recolors on theme switch (requires recreate) | Low |
| 5 | All | Full build test not yet completed — compile errors may exist | Critical |

---

## Server Setup (AMD FX-8350 Machine)

**Hardware:** AMD FX-8350 8-core 4GHz, 32GB DDR RAM, GTX 1050 Ti 4GB, ~2TB storage  
**OS:** Ubuntu Desktop 24.04 LTS  
**Domain:** `riversongai.com` — Cloudflare proxy (orange cloud), home IP hidden

### Deployment Checklist
1. Install Ubuntu Desktop 24.04 LTS
2. Install NVIDIA drivers: `sudo ubuntu-drivers autoinstall`
3. Clone the River Song AI repo
4. Copy `.env` from dev machine, update:
   - `ENVIRONMENT=production`
   - `CORS_ORIGINS=["https://riversongai.com","https://www.riversongai.com"]`
   - `ALLOWED_HOSTS=["riversongai.com","www.riversongai.com"]`
5. Cloudflare DNS: A record → production machine's home IP
6. UFW: only allow Cloudflare IPs on port 8000

---

## Android App File Map

```
app/src/main/java/com/riversongai/
├── data/
│   ├── model/          — FeedModels, LlmModels, MemoryModels, Fact, etc.
│   ├── remote/         — RiverSongApiService (all endpoints), AuthInterceptor
│   └── repository/     — One repo per domain (Feeds, Settings, Memory, etc.)
├── di/
│   └── AppModule.kt    — Koin DI: all singletons + viewModels registered here
├── ui/
│   ├── MainActivity.kt — Single activity, nav graph, bottom nav, drawer
│   ├── SplashActivity.kt — 1.8s branded splash before MainActivity
│   ├── ThemeData.kt    — APP_THEMES list with all 9 theme hex values
│   ├── viewmodel/      — One ViewModel per screen
│   └── adapter/        — RecyclerView adapters
└── utils/
    ├── ThemeManager.kt — Read/write theme key, apply to activity
    ├── UIStyleManager.kt — resolveCardColor(depth) for layered surfaces
    ├── LogoWebView.kt  — Builds themed HTML/SVG logo for WebView
    └── SessionManager.kt — Auth token, user role, isAdmin()
```

---

## How to Continue in a New Session

1. Open `riversong_android_app/` in Claude Code
2. Paste this HANDOFF.md into the chat
3. Share any new screenshots to the **River Photos** folder in Google Drive — Claude can access it directly
4. Reference specific screens using the comparison tables above

**Claude can access Google Drive** — just say "check the Drive" and it will pull the latest screenshots from the River Photos folder automatically.
