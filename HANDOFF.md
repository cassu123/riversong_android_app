# River Song AI — Setup Handoff

This file is for continuing work with Claude Code on a new machine.
Paste this into the chat when you start a new session.

---

## Who I Am
- Working on River Song AI — a personal AI operating system (FastAPI backend + React/Vite frontend)
- Voice loop: mic → Whisper STT → Ollama LLM → Piper TTS → speaker
- Development machine: Chromebook running Linux, VS Code terminal
- I am not an advanced developer — explain things clearly

---

## Production Server (Not Yet Set Up)
- **Hardware:** AMD FX-8350 8-core 4GHz, 32GB DDR RAM, GTX 1050 Ti 4GB, ~2TB storage
- **OS to install:** Ubuntu Desktop 24.04 LTS (chose Desktop over Server for the GUI safety net)
- **Domain:** riversongai.com — already in Cloudflare, full proxy enabled (orange cloud on)
- **Users:** Me, my husband, my sister and her family (~5-6 people), login screen required

---

## What's Already Done
- Core River Song AI app is built and working on the Chromebook
- Cloudflare: riversongai.com added, full DNS setup, AI crawler blocking on
- `/docs`, `/redoc`, `/openapi.json` hidden in production (env-controlled)
- TrustedHostMiddleware and CloudflareIPMiddleware added to main.py
- `ENVIRONMENT`, `ALLOWED_HOSTS`, `CORS_ORIGINS` settings added to config/settings.py
- `.gitignore` covers all secrets and credential files
- Pre-commit hook updated to allow deleting credential files (blocks adds only)
- Dev `.env` stays in localhost/dev mode on the Chromebook

---

## Deployment Checklist (Do This on the Ubuntu Machine)
1. Install Ubuntu Desktop 24.04 LTS
2. Install NVIDIA proprietary drivers: `sudo ubuntu-drivers autoinstall`
3. Clone the River Song AI repo
4. Copy `.env` from Chromebook and update these values:
   - `ENVIRONMENT=production`
   - `CORS_ORIGINS=["https://riversongai.com","https://www.riversongai.com"]`
   - `ALLOWED_HOSTS=["riversongai.com","www.riversongai.com"]`
5. Add DNS A record in Cloudflare: type A, name @, value = production machine's home IP
6. UFW firewall — only allow Cloudflare IPs on port 8000 (or nginx on 443 → localhost:8000)
7. Optional: Cloudflare Access — whitelist family email addresses for a second login gate

---

## Key Decisions Made
- No PWA yet (works fine in mobile browser for now)
- Ubuntu Desktop not Server (GUI available as safety net)
- Cloudflare proxy hides home IP — family accesses via riversongai.com
- GTX 1050 Ti will run Whisper base/small on GPU; Ollama runs on CPU/RAM (32GB is plenty)

---

## To Continue Claude's Memory on the New Machine
Copy this directory from the Chromebook to the same path on Ubuntu:
~/.claude/projects/-home-river-song-RiverSongAI/memory/
