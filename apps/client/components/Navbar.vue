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
                height="48"
                class="w-auto block"
                src="/logo.png"
                alt="俄语学习平台图标"
              />
            </div>
          </NuxtLink>

          <ClientOnly>
            <nav
              v-if="route.path === '/' && !isAuthenticated()"
              aria-label="Главное меню"
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
            <template #fallback>
              <div class="hidden md:block" aria-hidden="true"></div>
            </template>
          </ClientOnly>
        </div>

        <div class="flex items-center gap-1">
          <!-- 移动端菜单按钮 -->
          <button
            class="mr-1 flex h-9 w-9 items-center justify-center rounded-md text-slate-600 transition hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800 md:hidden"
            @click="mobileMenuOpen = !mobileMenuOpen"
            aria-label="菜单"
          >
            <UIcon v-if="!mobileMenuOpen" name="i-ph-list" class="h-6 w-6" />
            <UIcon v-else name="i-ph-x" class="h-6 w-6" />
          </button>

          <ClientOnly>
            <template v-if="isAuthenticated() && !isAdminPage && route.path !== '/'">
              <NuxtLink to="/media-course"
                class="hidden px-2 py-1 text-sm font-semibold text-slate-500 transition hover:text-emerald-600 dark:text-slate-400 dark:hover:text-emerald-300 md:inline-block">
                媒体课程
              </NuxtLink>
              <NuxtLink to="/stats"
                class="hidden px-2 py-1 text-sm font-semibold text-slate-500 transition hover:text-emerald-600 dark:text-slate-400 dark:hover:text-emerald-300 md:inline-block">
                统计
              </NuxtLink>
              <NuxtLink to="/review"
                class="hidden px-2 py-1 text-sm font-semibold text-slate-500 transition hover:text-emerald-600 dark:text-slate-400 dark:hover:text-emerald-300 md:inline-block">
                复习
                <span v-if="dueReviewCount > 0"
                  class="ml-1 rounded-full bg-red-500 px-1.5 py-0.5 text-[10px] font-bold text-white">{{ dueReviewCount }}</span>
              </NuxtLink>
              <NuxtLink to="/wrong-answers"
                class="hidden px-2 py-1 text-sm font-semibold text-slate-500 transition hover:text-red-600 dark:text-slate-400 dark:hover:text-red-300 md:inline-block">
                错题本
              </NuxtLink>
              <NuxtLink to="/vocabulary"
                class="hidden px-2 py-1 text-sm font-semibold text-slate-500 transition hover:text-emerald-600 dark:text-slate-400 dark:hover:text-emerald-300 md:inline-block">
                生词本
              </NuxtLink>
              <NuxtLink to="/mastered-elements"
                class="hidden px-2 py-1 text-sm font-semibold text-slate-500 transition hover:text-emerald-600 dark:text-slate-400 dark:hover:text-emerald-300 md:inline-block">
                复习本
              </NuxtLink>
              <NuxtLink to="/groups"
                class="hidden px-2 py-1 text-sm font-semibold text-slate-500 transition hover:text-emerald-600 dark:text-slate-400 dark:hover:text-emerald-300 md:inline-block">
                小组
              </NuxtLink>
            </template>
            <template #fallback>
              <div class="hidden md:flex items-center gap-1 min-w-[120px]"></div>
            </template>
          </ClientOnly>
          <!-- 显示用户信息 -->
          <ClientOnly>
            <div
              v-if="isAuthenticated() && route.path !== '/'"
              class="logged-in relative flex items-center"
            >
              <div
                class="h-8 w-8 cursor-pointer overflow-hidden rounded-full bg-gray-300 transition-all hover:scale-125 hover:opacity-90 dark:bg-gray-700"
                @click="isAdminPage ? (adminMenuOpen = !adminMenuOpen) : openUserMenu()"
              >
                <UAvatar
                  :src="userStore.user?.avatar"
                  alt="Аватар"
                />
              </div>
              <!-- 后台管理页面的用户下拉菜单 -->
              <Teleport to="body">
                <div v-if="isAdminPage && adminMenuOpen"
                  class="fixed right-4 top-14 z-50 w-48 rounded-md border bg-white py-1 shadow-lg dark:border-gray-700 dark:bg-slate-900"
                  @click.outside="adminMenuOpen = false"
                >
                  <div class="border-b px-3 py-2 text-sm dark:border-gray-700">
                    <div class="font-medium">{{ userStore.user?.nickname || userStore.user?.username || "用户" }}</div>
                    <div class="text-xs text-gray-500">@{{ userStore.user?.username }}</div>
                  </div>
                  <button class="flex w-full items-center px-3 py-2 text-sm text-red-600 hover:bg-gray-50 dark:hover:bg-gray-800" @click="handleAdminLogout">
                    <UIcon name="i-ph-sign-out" class="mr-2 h-4 w-4" /> 退出登录
                  </button>
                </div>
              </Teleport>
            </div>
            <template #fallback>
              <div class="min-w-[32px] min-h-[32px]"></div>
            </template>
          </ClientOnly>
          <!-- 登录/注册 -->
          <ClientOnly>
            <button
              v-if="!isAuthenticated() && route.path !== '/login'"
              aria-label="Войти"
              class="btn btn-sm mr-1 border-none bg-slate-950 text-white shadow-md hover:bg-slate-800 focus:outline-none dark:bg-white dark:text-slate-950"
              @click="signIn()"
            >
              {{ $t('nav.login') }}
            </button>
            <template #fallback>
              <button
                aria-label="Войти"
                class="btn btn-sm mr-1 border-none bg-slate-950 text-white shadow-md hover:bg-slate-800 focus:outline-none dark:bg-white dark:text-slate-950 invisible"
              >
                {{ $t('nav.login') }}
              </button>
            </template>
          </ClientOnly>
        </div>
      </div>
    </div>

    <!-- 移动端抽屉菜单 -->
    <Teleport to="body">
      <Transition name="drawer-fade">
        <div v-if="mobileMenuOpen" class="fixed inset-0 z-50 bg-black/40 md:hidden" @click="mobileMenuOpen = false"></div>
      </Transition>
      <Transition name="drawer-slide">
        <div v-if="mobileMenuOpen"
          class="fixed bottom-0 left-0 right-0 z-50 max-h-[70vh] overflow-y-auto rounded-t-2xl bg-white px-6 py-5 shadow-2xl dark:bg-slate-900 md:hidden">
          <div class="mb-4 flex items-center justify-between">
            <span class="text-lg font-bold text-slate-800 dark:text-white">菜单</span>
            <button @click="mobileMenuOpen = false" class="text-slate-400 hover:text-slate-600">
              <UIcon name="i-ph-x" class="h-5 w-5" />
            </button>
          </div>

          <!-- 未登录时显示的菜单项 -->
          <template v-if="!isAuthenticated()">
            <a v-for="(optItem, optIndex) in HEADER_OPTIONS" :key="optIndex"
              :href="optItem.href" :target="optItem.target ?? '_self'"
              class="block rounded-lg px-3 py-3 text-base font-semibold text-slate-700 transition hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-800"
              @click="mobileMenuOpen = false">
              {{ optItem.name }}
            </a>
          </template>

          <!-- 已登录时显示的菜单项 -->
          <template v-if="isAuthenticated()">
            <NuxtLink to="/media-course" @click="mobileMenuOpen = false"
              class="flex items-center rounded-lg px-3 py-3 text-base font-semibold text-slate-700 transition hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-800">
              <UIcon name="i-ph-play-circle" class="mr-3 h-5 w-5" /> 媒体课程
            </NuxtLink>
            <NuxtLink to="/stats" @click="mobileMenuOpen = false"
              class="flex items-center rounded-lg px-3 py-3 text-base font-semibold text-slate-700 transition hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-800">
              <UIcon name="i-ph-chart-bar" class="mr-3 h-5 w-5" /> 统计
            </NuxtLink>
            <NuxtLink to="/review" @click="mobileMenuOpen = false"
              class="flex items-center rounded-lg px-3 py-3 text-base font-semibold text-slate-700 transition hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-800">
              <UIcon name="i-ph-arrows-clockwise" class="mr-3 h-5 w-5" /> 复习
              <span v-if="dueReviewCount > 0" class="ml-2 rounded-full bg-red-500 px-1.5 py-0.5 text-[10px] font-bold text-white">{{ dueReviewCount }}</span>
            </NuxtLink>
            <NuxtLink to="/wrong-answers" @click="mobileMenuOpen = false"
              class="flex items-center rounded-lg px-3 py-3 text-base font-semibold text-slate-700 transition hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-800">
              <UIcon name="i-ph-x-circle" class="mr-3 h-5 w-5" /> 错题本
            </NuxtLink>
            <NuxtLink to="/vocabulary" @click="mobileMenuOpen = false"
              class="flex items-center rounded-lg px-3 py-3 text-base font-semibold text-slate-700 transition hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-800">
              <UIcon name="i-ph-book-open" class="mr-3 h-5 w-5" /> 生词本
            </NuxtLink>
            <NuxtLink to="/mastered-elements" @click="mobileMenuOpen = false"
              class="flex items-center rounded-lg px-3 py-3 text-base font-semibold text-slate-700 transition hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-800">
              <UIcon name="i-ph-checks" class="mr-3 h-5 w-5" /> 复习本
            </NuxtLink>
            <NuxtLink to="/groups" @click="mobileMenuOpen = false"
              class="flex items-center rounded-lg px-3 py-3 text-base font-semibold text-slate-700 transition hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-800">
              <UIcon name="i-ph-users-three" class="mr-3 h-5 w-5" /> 小组
            </NuxtLink>
          </template>
        </div>
      </Transition>
    </Teleport>
  </header>
</template>

<script setup lang="ts">
import { useWindowScroll } from "@vueuse/core";
import { useRuntimeConfig } from "nuxt/app";
import { computed } from "vue";
import { useRoute } from "vue-router";

import { onMounted, ref } from "vue";
import { useUserMenu } from "~/composables/user/useUserMenu";
import { isAuthenticated, signIn, signOut } from "~/services/auth";
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
  { name: "文档", href: "/help" },
  { name: "功能", href: "#features" },
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

const mobileMenuOpen = ref(false);
const adminMenuOpen = ref(false);

const isAdminPage = computed(() => route.path.startsWith("/admin"));

async function handleAdminLogout() {
  adminMenuOpen.value = false;
  await signOut();
}
</script>

<style scoped>
.drawer-fade-enter-active,
.drawer-fade-leave-active {
  transition: opacity 0.2s ease;
}
.drawer-fade-enter-from,
.drawer-fade-leave-to {
  opacity: 0;
}
.drawer-slide-enter-active,
.drawer-slide-leave-active {
  transition: transform 0.25s ease;
}
.drawer-slide-enter-from,
.drawer-slide-leave-to {
  transform: translateY(100%);
}
</style>
