// https://nuxt.com/docs/api/configuration/nuxt-config

export default defineNuxtConfig({
  buildDir: process.env.NUXT_BUILD_DIR || ".nuxt",
  ssr: false,
  imports: {
    autoImport: true,
  },
  devtools: {
    enabled: false,
  },
  app: {
    head: {
      title: "俄语学习平台",
      link: [{ rel: "icon", type: "image/png", href: "/logo-circle.png" }],
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
  image: {
    domains: ["earthworm-prod-1312884695.cos.ap-beijing.myqcloud.com"],
    presets: {
      cover: {
        modifiers: {
          width: 400,
          height: 300,
          fit: "cover",
          format: "webp",
        },
      },
    },
  },
  plugins: ["~/plugins/http.ts"],
  runtimeConfig: {
    public: {
      // 默认走 Nuxt 反向代理 /api/backend，开发时可设 API_BASE=http://localhost:8080 直连
      apiBase: process.env.API_BASE || "/api/backend",
      // 以下字段保留以兼容可能读取它们的组件，但测试模式下无实际作用
      endpoint: "",
      appId: "",
      backendEndpoint: process.env.BACKEND_ENDPOINT || "/api/backend/",
      signInRedirectURI: "",
      signOutRedirectURI: "",
      helpDocsURL: process.env.HELP_DOCS_URL || "",
    },
  },
  // 把 /api/backend/* 反代到本地 Spring Boot，让 ngrok 单 tunnel 也能跑通
  routeRules: {
    "/api/backend/**": { proxy: "http://localhost:8080/**" },
  },
  vite: {
    server: {
      origin: process.env.DEV_ORIGIN || "http://localhost:3000",
      hmr: {
        host: process.env.DEV_HOST || "localhost",
        protocol: "ws",
      },
      fs: {
        allow: ["..", "../.."],
      },
    },
  },
  devServer: {
    url: "http://localhost:3000",
    host: "0.0.0.0",
  },
  build: {},
});
