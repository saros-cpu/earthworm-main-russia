import { defineNuxtPlugin } from "nuxt/app";

import { initializeCsrf, setupHttp } from "../api/http";

export default defineNuxtPlugin(async () => {
  setupHttp();
  if (import.meta.client) {
    await initializeCsrf().catch(() => undefined);
  }
});
