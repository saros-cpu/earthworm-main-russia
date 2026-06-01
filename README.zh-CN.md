<div align="center">
  <img alt="中大俄语" width="120" height="120" src="./apps/client/public/logo-circle.png">
  <h1>中大俄语 · Джунда русский</h1>
  <p>A typing-based Russian learning platform built for Chinese-speaking learners</p>
  <p>
    <a href="./README.md">中文</a> · <strong>English</strong>
  </p>
</div>

---

> Hard fork of [cuixueshe/earthworm](https://github.com/cuixueshe/earthworm). The entire backend, database, and course data have been **rewritten for Russian**. Branding fully migrated to "中大俄语 / Джунда русский".

## Status (v2026.05)

| Metric | Value |
|---|---|
| Course packs | **25** |
| Lessons | **1 841** |
| Practice items | **19 103** |
| Coverage | Cyrillic → Beginner → TORFL A1–C2 → Russia Around Us 1–4 → Infant Care · Gas Station / Oil / Engineering |

## Stack

| Layer | Choice |
|---|---|
| Frontend | **Nuxt 3.21** + Vue 3.5 + TypeScript 6 + Pinia + Tailwind + DaisyUI + Nuxt UI v2 |
| Backend | **Spring Boot 3.4** + **Java 21** + JPA + Flyway |
| Database | **MySQL 8** |
| AI | OpenRouter (default `openai/gpt-4o-mini`) |
| Russian NLP | pymorphy3 |
| Parallel corpus | Tatoeba RU–ZH (11 301 native pairs) |
| Dictionary | BKRS large RU–CN dictionary (DSL) |
| Frequency list | Leeds Russian Frequency List |
| Testing | Vitest 1 + Cypress 13 + Spring Boot Test |
| CI/CD | GitHub Actions (lint + build + test + CodeQL) |

## Quick start

### Dev mode (hot-reload)

```powershell
# Prereqs: JDK 21, Node 20+, MySQL 8, pnpm 8+
pnpm install
mysql -e "CREATE DATABASE earthworm DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# One-click start both backend & frontend
.\start.bat
```

### Production mode (external-network friendly)

```powershell
# One-click build
.\prod-build.ps1

# One-click start
.\prod-start.ps1

# Check status
.\prod-status.ps1

# Stop services
.\prod-stop.ps1
```

`prod-build.ps1` runs:
1. `mvn -DskipTests package` — builds Spring Boot JAR
2. `pnpm --filter client build` — builds the Nuxt SSR server output and static assets

`prod-start.ps1` runs:
1. Stops only processes recorded by a prior production launch; aborts without killing if another program owns a port
2. Starts backend JAR (port 8080)
3. Starts Nuxt Nitro (`apps/client/.output/server/index.mjs`, port 3000) which proxies `/api/backend/*` to backend

### Access

| URL | Purpose |
|-----|---------|
| http://localhost:3000 | Frontend UI |
| http://IP:3000 | LAN/public access (e.g. `http://109.71.228.50:3000`) |
| http://localhost:8080 | Backend API |
| http://localhost:3000/admin | Admin panel (ADMIN only) |

No default admin password is provided. Create a dedicated account with a strong password, then grant the `ADMIN` role through a controlled operation.

Browser sessions use an `HttpOnly` cookie and CSRF protection. Keep `AUTH_SESSION_COOKIE_SECURE=true` in HTTPS deployments; set it to `false` only for local HTTP development.
AI requests reserve against a persistent daily output-token budget before reaching the provider. Set `AI_DAILY_MAX_RESERVED_OUTPUT_TOKENS` to control that cap.

## Role system

| Role | Permissions |
|------|-------------|
| **Guest** (not logged in) | View only "入门基础" course packs |
| **USER** (logged in) | Full access to all course packs |
| **ADMIN** | Extra "课程编辑器" menu item, backend admin access |

New users default to `USER`. Promote via DB: `UPDATE users SET role='ADMIN' WHERE username='...';`

## Project layout

```
apps/client/           Nuxt 3 frontend
  pages/
    course-pack/       Course pack list & detail
    media-course/      Media courses (video/audio + quiz + subtitles)
    dashboard.vue      Stats overview
    admin.vue          Admin console
  components/
    media/             Media components
      VideoPlayer.vue  Video/audio player
      VideoQuiz.vue    Video + quiz mode
      AudioLrcPlayer.vue  Audio + subtitle mode
    Navbar.vue         With mobile hamburger drawer
    Landing/           Landing pages (responsive)
    mode/              Practice modes
  composables/         Shared composables

backend/               Spring Boot
  controller/
    MediaController.java  Media stream/info (cache, transcoding)
  service/
    MediaService.java     Media resolution, ffmpeg transcoding
  src/main/resources/
    torfl/levels/      A1..C2 vocab + sentences JSON
    customs/           Professional-pack JSON seeds (with video paths)
    db/migration/      Flyway SQL

scripts/
  prod-frontend-server.mjs  Legacy-compatible static proxy server
  prod-build.ps1            One-click build
  prod-start.ps1            One-click start
  prod-stop.ps1             Stop services
  prod-status.ps1           Check service status
  prod-health.ps1           Full health check

runtime/
  transcode-cache/     ffmpeg transcoded MP4/MP3 cache

Logo/                  Brand assets
  中亚能源logo（双语）.png     Top-left navbar logo
apps/client/public/
  logo-circle.png           Circular logo (no text) for other uses
```

## Data pipelines

### 1. TORFL A1–C2 vocabulary (BKRS + Leeds + HSK)

```powershell
python scripts/dsl-import/parse_dsl.py
python scripts/dsl-import/build_levels.py
python scripts/dsl-import/quality_report.py
```

### 2. Native sentence retrieval (Tatoeba)

```powershell
python scripts/sentence-import/fetch_tatoeba.py
python scripts/sentence-import/add_sentences.py
```

### 3. AI sentence generation (OpenRouter)

```powershell
python scripts/sentence-import/build_term_lists.py
python scripts/sentence-import/ai_generate.py --input ... --output ... --batch 10 --resume
python scripts/sentence-import/inject_ai_sentences.py
```

### 4. Custom packs (xlsx / csv)

```powershell
python scripts/custom-import/build_baby_care.py
python scripts/custom-import/build_oil_engineering.py
```

## Admin REST API (selected)

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/admin/stats` | Aggregated stats |
| POST | `/admin/torfl-pack/reseed` | Disabled to protect learning progress |
| POST | `/admin/custom-pack/reseed` | Disabled to protect learning progress |
| GET/PUT | `/admin/course-packs/{id}` | View / edit pack |
| POST | `/admin/course-packs/{id}/generate-course` | Generate AI lesson |
| DELETE | `/admin/course-packs/{id}`, `/admin/courses/{id}`, `/admin/statements/{id}` | Archives content without deleting learning history; admins can restore it |
| POST | `/admin/statements/{id}/refine`, `/admin/courses/{id}/refine-all` | Store rule-based refinements; does not call paid AI |
| POST | `/admin/pdf-import-jobs/local-directory` | Disabled: PDF import is not available yet |
| POST | `/admin/vocabulary-course-pack` | Disabled: vocabulary pack tooling is not available yet |

## FAQ

**Flyway validation fails on boot?**
Keep a backup, restore previously applied migration files to their original contents, and add a new migration for required changes. Do not delete migration history or bypass validation.

**AI assistant says "AI not configured"?**
Set `OPENROUTER_API_KEY`. Check balance at https://openrouter.ai/keys.

**Can I reseed the current production database?**
No. Reseed endpoints are disabled to protect learning records; validate data changes in a test database and publish them through a controlled migration.

**Must I use OpenRouter?**
No, any OpenAI-compatible endpoint works. Override `openai.baseUrl` and `openai.model` in `application.yml`.

## Files you may want to change

| File | What |
|------|------|
| Environment variables | DB credentials and JWT secret |
| `apps/client/nuxt.config.ts` | API proxy, Vite/HMR config |
| `backend/src/main/java/.../config/JwtUtil.java` | JWT secret / expiration |
| `scripts/dsl-import/*.py` | Data pipeline scripts |

## License

MIT, continuing from upstream. See [LICENSE](./LICENSE).
