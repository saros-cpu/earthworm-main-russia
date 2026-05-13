/**
 * Logto 插件（已禁用）。
 * 认证由自建 JWT 系统处理（services/auth.ts）。
 */
import { defineNuxtPlugin } from "nuxt/app";

export default defineNuxtPlugin(() => {
  // Auth handled by JWT via services/auth.ts
});
