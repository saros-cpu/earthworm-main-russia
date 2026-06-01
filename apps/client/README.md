# Client — Nuxt 3 前端

"中大俄语"俄语学习平台前端。

## 技术栈

- Nuxt 3.21 (SPA 模式, ssr: false)
- Vue 3.5 + TypeScript 6
- Pinia 状态管理
- vue-i18n (默认语言 ru, fallback zh)
- Tailwind CSS + DaisyUI + Nuxt UI v2
- Vitest 1 + Cypress 13

## 目录

| 目录 | 说明 |
|------|------|
| `pages/` | 28 个页面路由 |
| `components/` | 65 个组件（media/main/mode/Home/Landing 等）|
| `composables/` | 可复用组合式函数（游戏逻辑、i18n、API 调用）|
| `store/` | Pinia stores（course、coursePack、game 等）|
| `locales/` | i18n 语言包（ru.json + zh.json）|
| `api/` | API 调用封装 |
| `plugins/` | Nuxt 插件（i18n、http、SSR localStorage 模拟）|
| `layouts/` | 布局（default + admin）|
| `types/` | TypeScript 类型定义 |

## 命令

```bash
pnpm dev          # 开发模式（热更新）
pnpm build        # 生产构建
pnpm test:unit    # 运行单元测试
pnpm test:e2e    # 运行 E2E 测试
```
