/**
 * 测试模式：跳过认证检查，所有路由直接放行
 */
import { defineNuxtRouteMiddleware } from "nuxt/app";

export default defineNuxtRouteMiddleware(() => {
  // 测试模式：始终放行
});
