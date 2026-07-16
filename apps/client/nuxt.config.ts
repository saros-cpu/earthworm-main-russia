// https://nuxt.com/docs/api/configuration/nuxt-config

const devOrigin = process.env.DEV_ORIGIN?.trim();
const devHost = process.env.DEV_HOST?.trim();
const devPort = Number(process.env.NUXT_DEV_PORT || 3000);

export default defineNuxtConfig({
  buildDir: process.env.NUXT_BUILD_DIR || ".nuxt",
  ssr: false,
  imports: {
    autoImport: true,
  },
  devtools: {
    enabled: false,
  },
  experimental: {
    viteEnvironmentApi: true,
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
    "@vite-pwa/nuxt",
  ],
  pwa: {
    registerType: "autoUpdate",
    includeAssets: ["logo-circle.png", "logo.png"],
    manifest: {
      name: "俄语学习平台",
      short_name: "Earthworm",
      description: "俄语学习平台 — 通过句子学习俄语",
      theme_color: "#0f172a",
      background_color: "#ffffff",
      display: "standalone",
      orientation: "portrait",
      start_url: "/",
      icons: [
        { src: "logo-circle.png", sizes: "192x192", type: "image/png" },
        { src: "logo-circle.png", sizes: "512x512", type: "image/png" },
        { src: "logo-circle.png", sizes: "512x512", type: "image/png", purpose: "maskable" },
      ],
    },
    workbox: {
      globPatterns: ["**/*.{js,css,html,ico,png,svg,woff,woff2,ttf}"],
      globIgnores: ["**/fonts/*.otf"],
      navigateFallback: "/",
      maximumFileSizeToCacheInBytes: 4 * 1024 * 1024,
      runtimeCaching: [
        {
          urlPattern: /^\/api\/backend\/.*/i,
          handler: "NetworkFirst",
          method: "GET",
          options: {
            cacheName: "api-cache",
            expiration: { maxEntries: 100, maxAgeSeconds: 60 * 60 * 24 },
          },
        },
        {
          urlPattern: /\.(?:otf|ttf|woff2?)$/i,
          handler: "CacheFirst",
          method: "GET",
          options: {
            cacheName: "font-cache",
            expiration: { maxEntries: 20, maxAgeSeconds: 60 * 60 * 24 * 30 },
          },
        },
      ],
    },
    client: {
      installPrompt: true,
      periodicSyncForUpdates: 3600,
    },
    devOptions: {
      enabled: true,
      type: "module",
    },
  },
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
    "/api/backend/**": {
      proxy: process.env.BACKEND_PROXY_TARGET || "http://localhost:8000/api/v1/**",
    },
  },
  vite: {
    server: {
      ...(devOrigin ? { origin: devOrigin } : {}),
      hmr: {
        ...(devHost ? { host: devHost } : {}),
        protocol: "ws",
      },
      fs: {
        allow: ["..", "../.."],
      },
    },
  },
  devServer: {
    host: "0.0.0.0",
    port: devPort,
  },
  build: {},
});
