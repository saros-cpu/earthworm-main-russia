import { defineNuxtRouteMiddleware, navigateTo } from "nuxt/app";

export default defineNuxtRouteMiddleware(() => {
  try {
    const raw = localStorage.getItem("ew_user");
    if (raw) {
      const user = JSON.parse(raw);
      if (user.role === "ADMIN") return;
    }
  } catch {}
  return navigateTo("/");
});
