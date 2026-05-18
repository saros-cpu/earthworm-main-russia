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

| Metric         | Value                                                                                                    |
| -------------- | -------------------------------------------------------------------------------------------------------- |
| Course packs   | **25**                                                                                                   |
| Lessons        | **1 841**                                                                                                |
| Practice items | **19 103**                                                                                               |
| Coverage       | Cyrillic → Beginner → TORFL A1–C2 → Russia Around Us 1–4 → Infant Care · Gas Station / Oil / Engineering |

## Stack

| Layer           | Choice                                                      |
| --------------- | ----------------------------------------------------------- |
| Frontend        | **Nuxt 3** + Vue 3 + TypeScript + Pinia + UnoCSS + Tailwind |
| Backend         | **Spring Boot 3** + **Java 21** + JPA + Flyway              |
| Database        | **MySQL 8**                                                 |
| AI              | OpenRouter (default `openai/gpt-4o-mini`)                   |
| Russian NLP     | pymorphy3                                                   |
| Parallel corpus | Tatoeba RU–ZH (11 301 native pairs)                         |
| Dictionary      | BKRS large RU–CN dictionary (DSL)                           |
| Frequency list  | Leeds Russian Frequency List                                |

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
cd apps\client
pnpm build          # Bundle all assets
pnpm preview        # Serve on port 3000
```

Production mode pre-compiles all Vue components into static chunks, avoiding Vite's per-file dynamic loading. Recommended for networks with strict firewalls.

### Access

| URL                         | Purpose                  |
| --------------------------- | ------------------------ |
| http://localhost:3000       | Frontend UI              |
| http://localhost:8080       | Backend API              |
| http://localhost:3000/admin | Admin panel (ADMIN only) |

Default admin account: `yangjie` / `123456`

## Role system

| Role                      | Permissions                                        |
| ------------------------- | -------------------------------------------------- |
| **Guest** (not logged in) | View only "入门基础" course packs                  |
| **USER** (logged in)      | Full access to all course packs                    |
| **ADMIN**                 | Extra "课程编辑器" menu item, backend admin access |

New users default to `USER`. Promote via DB: `UPDATE users SET role='ADMIN' WHERE username='...';`

## Project layout

```
apps/client/           Nuxt 3 frontend
  pages/dashboard.vue  Stats overview
  pages/admin.vue      Admin console
  components/          Vue components (responsive, mobile-friendly)
    Navbar.vue         With mobile hamburger drawer
    Landing/           Landing pages (responsive)
    mode/              Practice modes (dictation, speech assessment, etc.)

backend/               Spring Boot
  src/main/resources/
    torfl/levels/      A1..C2 vocab + sentences JSON
    customs/           Professional-pack JSON seeds
    db/migration/      Flyway SQL (V6 adds user role)

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

| Method  | Path                                       | Purpose                  |
| ------- | ------------------------------------------ | ------------------------ |
| GET     | `/admin/stats`                             | Aggregated stats         |
| POST    | `/admin/torfl-pack/reseed`                 | Re-seed TORFL packs      |
| POST    | `/admin/custom-pack/reseed`                | Re-seed custom packs     |
| GET/PUT | `/admin/course-packs/{id}`                 | View / edit pack         |
| POST    | `/admin/course-packs/{id}/generate-course` | Generate AI lesson       |
| POST    | `/admin/pdf-import-jobs/local-directory`   | Bulk-import PDFs         |
| POST    | `/admin/vocabulary-course-pack`            | Generate vocabulary pack |

## FAQ

**Flyway validation fails on boot?**
Drop `flyway_schema_history` or temporarily flip `ddl-auto: update`.

**AI assistant says "AI not configured"?**
Set `OPENROUTER_API_KEY`. Check balance at https://openrouter.ai/keys.

**Do user progress records survive a reseed?**
Yes. Pack and course ids are hash-stable.

**Must I use OpenRouter?**
No, any OpenAI-compatible endpoint works. Override `openai.baseUrl` and `openai.model` in `application.yml`.

## Files you may want to change

| File                                            | What                       |
| ----------------------------------------------- | -------------------------- |
| `backend/src/main/resources/application.yml`    | DB user/pass, JWT secret   |
| `apps/client/nuxt.config.ts`                    | API proxy, Vite/HMR config |
| `backend/src/main/java/.../config/JwtUtil.java` | JWT secret / expiration    |
| `scripts/dsl-import/*.py`                       | Data pipeline scripts      |

## License

MIT, continuing from upstream. See [LICENSE](./LICENSE).
