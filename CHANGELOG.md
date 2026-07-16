# Changelog

## [1.4.21] - 2026-05

### Security
- **SecurityConfig**: Added proper Spring Security path-based authorization (`.anyRequest().authenticated()`) instead of blanket `.permitAll()`
- **JWT**: Removed insecure default fallback secret; startup now fails if `JWT_SECRET` is missing
- **Credentials**: Removed plaintext database passwords from `.env`, `.env.example`, `DEPLOYMENT.md`, and scripts
- **JwtAuthFilter**: Now sets Spring Security `SecurityContext` for proper integration with SecurityConfig

### Dependencies
- **Spring Boot**: 3.3.0 → 3.4.4
- **mysql-connector-j**: 8.0.33 → 9.2.0
- **springdoc-openapi**: 2.6.0 → 2.8.6
- **logstash-logback-encoder**: 7.4 → 8.0
- **@vueuse/core**: 10.x → 12.x
- **@vueuse/nuxt**: 10.x → 12.x
- **vitest**: 1.x → 无升级（`@nuxt/test-utils` 与 Vitest 2+/3+ 不兼容）

### Code Quality
- Removed 6 `console.log` statements from production code (`course-pack/index.vue`, `store/game.ts`, `shareImage/helper.ts`, `useGamePause.ts`)

### Media Courses
- **AudioLrcPlayer**: Now accepts `lrcLines` prop to display actual course content instead of hardcoded mock data
- **Detail page**: Passes real `lrcLines` (from course lyrics/statements) to `AudioLrcPlayer`
- **YouTube LRC**: Fixed `currentLrcIndex` hardcoded to 0 — now tracks elapsed playback time
- **Icon**: Fixed `ph:seedling` → `ph:leaf` (icon didn't exist in Phosphor set)

### Bug Fixes
- **Game page**: Added missing `const isLoading = ref(true)` — fixed `ReferenceError: isLoading is not defined`

### CI/CD
- Updated CI workflow to use MySQL (was PostgreSQL from old Prisma era)
- Added CodeQL security analysis workflow
- Added Dependabot configuration for npm, Maven, and GitHub Actions

## [1.0.0] - 2024-03-13

### Bug Fixes

- fix course 18 the ninety-first field is incorrect
- fix course 18 the ninety-first field is incorrect
- fix course issue, closes [#118]
- fix course-18
- fix course-34 statement is incorrect
- fix-course-18
- Keep word width consistent
- Prevents adding Spaces after the last word
- remove the horrible emoji lol
- **setting**: update cmd key display in the shortcut settings
- tests problems
- the env variable cannot be read, causing the db:init command to fail.
- update course 12-74
- update course 15.5-28

### Features

- add e2e test by cypress
- add mobile tips
- add scheduled task module and weekly reset ranking function
- added interaction to resolve wrong words
- long sentence modification error
- perfect use introduction
- read one sentence per day aloud
- shortcut key settings for submit operations
- submit with space
- support to delete back to the previous incorrect word
- the word is suggested by the width of the input box
