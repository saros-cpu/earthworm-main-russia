import { defineNuxtRouteMiddleware, navigateTo } from "nuxt/app";

import { clearAuth, setStoredUser } from "~/api/auth";
import { fetchAuthenticatedIdentity } from "~/api/user";

export default defineNuxtRouteMiddleware(async () => {
  try {
    const user = await fetchAuthenticatedIdentity();
    setStoredUser({
      userId: user.userId,
      username: user.username,
      nickname: user.nickname,
      avatar: user.avatar,
      role: user.role || "USER",
    });
    if (user.role === "ADMIN") return;
  } catch {
    clearAuth();
  }
  return navigateTo("/");
});
