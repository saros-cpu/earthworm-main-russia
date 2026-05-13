// https://nuxt.com/docs/api/configuration/nuxt-config

export default defineNuxtConfig({
  ssr: false,
  imports: {
    autoImport: true,
  },
  devtools: {
    enabled: true,
  },
  app: {
    head: {
      title: "鹅语菌",
      link: [{ rel: "icon", href: "/favicon.ico" }],
    },
  },
  css: ["~/assets/css/globals.css"],
  modules: [
    "@nuxt/ui",
    "@vueuse/nuxt",
    "@nuxt/test-utils/module",
    "@hypernym/nuxt-anime",
    "@nuxt/image",
  ],
  plugins: ["~/plugins/logto.ts", "~/plugins/http.ts"],
  runtimeConfig: {
    public: {
      apiBase: process.env.API_BASE || "http://localhost:8080",
      // 以下字段保留以兼容可能读取它们的组件，但测试模式下无实际作用
      endpoint: "",
      appId: "",
      backendEndpoint: process.env.BACKEND_ENDPOINT || "http://localhost:8080/",
      signInRedirectURI: "",
      signOutRedirectURI: "",
      helpDocsURL: process.env.HELP_DOCS_URL || "",
    },
  },
  build: {
    transpile: ["vue-sonner"],
  },
});
