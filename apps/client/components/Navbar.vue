<template>
  <header
    class="w-full px-5 font-customFont transition-all duration-300 ease-linear"
    :class="{
      'sticky top-0 z-10': isStickyNavBar,
      'border-b border-slate-200 bg-white/88 shadow-sm backdrop-blur dark:border-slate-800 dark:bg-slate-950/88':
        isStickyNavBar && isScrolled,
    }"
  >
    <div class="mx-auto max-w-screen-xl">
      <div class="flex h-16 items-center justify-between">
        <div class="flex flex-1 items-center justify-between">
          <NuxtLink to="/">
            <div class="logo flex items-center">
              <img
                width="48"
                height="48"
                class="mr-6 hidden overflow-hidden rounded-md md:block"
                src="/logo.png"
                alt="earth-worm-logo"
              />
              <h1 class="text-wrap text-2xl font-extrabold leading-normal dark:text-white">
                鹅语菌
              </h1>
            </div>
          </NuxtLink>

          <nav
            v-if="route.path === '/' && !isAuthenticated()"
            aria-label="Global"
            class="hidden md:block"
          >
            <ul class="flex items-center text-base">
              <li
                class="px-4"
                v-for="(optItem, optIndex) in HEADER_OPTIONS"
                :key="optIndex"
              >
                <a
                  class="text-nowrap font-semibold text-slate-600 hover:text-emerald-700 dark:text-slate-200 dark:hover:text-emerald-300"
                  :href="optItem.href"
                  :target="optItem.target ?? '_self'"
                >
                  {{ optItem.name }}
                </a>
              </li>
            </ul>
          </nav>
        </div>

        <div class="flex items-center gap-1">
          <NuxtLink v-if="isAuthenticated()" to="/stats"
            class="hidden px-2 py-1 text-sm font-semibold text-slate-500 transition hover:text-emerald-600 dark:text-slate-400 dark:hover:text-emerald-300 md:inline-block">
            统计
          </NuxtLink>
          <NuxtLink v-if="isAuthenticated()" to="/review"
            class="hidden px-2 py-1 text-sm font-semibold text-slate-500 transition hover:text-emerald-600 dark:text-slate-400 dark:hover:text-emerald-300 md:inline-block">
            复习
            <span v-if="dueReviewCount > 0"
              class="ml-1 rounded-full bg-red-500 px-1.5 py-0.5 text-[10px] font-bold text-white">{{ dueReviewCount }}</span>
          </NuxtLink>
          <NuxtLink v-if="isAuthenticated()" to="/vocabulary"
            class="hidden px-2 py-1 text-sm font-semibold text-slate-500 transition hover:text-emerald-600 dark:text-slate-400 dark:hover:text-emerald-300 md:inline-block">
            生词本
          </NuxtLink>
          <NuxtLink v-if="isAuthenticated()" to="/mastered-elements"
            class="hidden px-2 py-1 text-sm font-semibold text-slate-500 transition hover:text-emerald-600 dark:text-slate-400 dark:hover:text-emerald-300 md:inline-block">
            复习本
          </NuxtLink>
          <NuxtLink v-if="isAuthenticated()" to="/groups"
            class="hidden px-2 py-1 text-sm font-semibold text-slate-500 transition hover:text-emerald-600 dark:text-slate-400 dark:hover:text-emerald-300 md:inline-block">
            小组
          </NuxtLink>
          <!-- 显示用户信息 -->
          <div
            v-if="isAuthenticated()"
            class="logged-in flex items-center"
          >
            <div
              class="h-8 w-8 cursor-pointer overflow-hidden rounded-full bg-gray-300 transition-all hover:scale-125 hover:opacity-90 dark:bg-gray-700"
              @click="openUserMenu"
            >
              <UAvatar
                :src="userStore.user?.avatar"
                alt="Avatar"
              />
            </div>
          </div>
          <!-- 登录/注册 -->
          <button
            v-else
            aria-label="Login"
            class="btn btn-sm mr-1 border-none bg-slate-950 text-white shadow-md hover:bg-slate-800 focus:outline-none dark:bg-white dark:text-slate-950"
            @click="signIn()"
          >
            登录
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useWindowScroll } from "@vueuse/core";
import { useRuntimeConfig } from "nuxt/app";
import { computed } from "vue";
import { useRoute } from "vue-router";

import { onMounted, ref } from "vue";
import { useUserMenu } from "~/composables/user/useUserMenu";
import { isAuthenticated, signIn } from "~/services/auth";
import { useUserStore } from "~/store/user";
import { fetchDueReviewCount } from "~/api/learning";

const runtimeConfig = useRuntimeConfig();
const { openUserMenu } = useUserMenu();

const route = useRoute();
const userStore = useUserStore();
const { y } = useWindowScroll();

const SCROLL_THRESHOLD = 8;
// https://developer.mozilla.org/zh-CN/docs/Web/HTML/Element/a#%E5%B1%9E%E6%80%A7
interface AnchorAttributes extends Record<string, any> {
  href: string;
  target?: string;
  download?: string;
}
const HEADER_OPTIONS: AnchorAttributes[] = [
  { name: "文档", href: runtimeConfig.public.helpDocsURL as string, target: "_blank" },
  { name: "功能", href: "#features" },
  { name: "答疑", href: "#faq" },
  { name: "联系我们", href: "#contact" },
];

// TODO: 设置需要固定导航栏的页面
const dueReviewCount = ref(0);
async function loadDueReviews() {
  try {
    const res = await fetchDueReviewCount();
    dueReviewCount.value = res.count;
  } catch (_) { dueReviewCount.value = 0; }
}
onMounted(() => { if (isAuthenticated()) loadDueReviews(); });

const isStickyNavBar = computed(() =>
  ["index", "User-Setting", "mastered-elements"].includes(route.name as string),
);
const isScrolled = computed(() => y.value >= SCROLL_THRESHOLD);
</script>
