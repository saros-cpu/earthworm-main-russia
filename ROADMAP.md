# Roadmap

## Short-term (Q2 2026)

- [x] Security hardening (SecurityConfig, JWT validation, credential cleanup)
- [x] Dependency upgrades (Spring Boot 3.4, VueUse 12, Vitest 1)
- [x] CI/CD improvements (MySQL in CI, CodeQL, Dependabot)
- [x] Media course: AudioLrcPlayer shows real course content (not mock data)

## Medium-term (Q3 2026)

- [ ] **Nuxt 4 upgrade**: Migrate from Nuxt 3.21 to Nuxt 4 (requires careful testing)
- [ ] **Backend test coverage**: Add integration tests for services, repositories, and controllers
- [ ] **Frontend test coverage**: Add component tests (Cypress) and increase unit test coverage
- [ ] **Media timeline sync**: Add `startTime`/`endTime` to seed JSON for auto-syncing quiz questions with video
- [ ] **Real LRC timestamps**: Migrate lyrics data to `[mm:ss.xx]` format for proper subtitle syncing
- [ ] **TypeScript strictness**: Replace `: any` types with proper interfaces across the frontend

## Long-term (Q4 2026+)

- [ ] **API DTOs**: Replace `Map<String, Object>` return types with Java records/classes
- [ ] **YouTube API integration**: Replace iframe embed with YouTube Player API for play/pause tracking
- [ ] **Offline support**: PWA / Service Worker for offline learning
- [ ] **Mobile app**: React Native or Flutter client
- [ ] **Real-time multiplayer**: WebSocket-based battle mode with live opponents
- [ ] **Spaced repetition**: Enhanced SRS algorithm for review scheduling
