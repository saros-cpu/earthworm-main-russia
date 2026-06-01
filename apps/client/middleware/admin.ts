import { defineNuxtRouteMiddleware, navigateTo } from "nuxt/app";
import { fetchAuthenticatedIdentity } from "~/api/user";
import { clearAuth, setStoredUser } from "~/services/auth";

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
