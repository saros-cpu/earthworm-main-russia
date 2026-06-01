<template>
  <div class="flex h-screen overflow-hidden bg-slate-50 dark:bg-slate-950">
    <!-- 侧边栏遮罩（移动端） -->
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 z-40 bg-black/40 lg:hidden"
      @click="sidebarOpen = false"
    />

    <!-- 侧边栏 -->
    <aside
      :class="[
        'fixed inset-y-0 left-0 z-50 flex w-60 flex-col border-r border-slate-200 bg-white transition-transform duration-200 dark:border-slate-800 dark:bg-slate-900 lg:static lg:translate-x-0',
        sidebarOpen ? 'translate-x-0' : '-translate-x-full',
      ]"
    >
      <!-- Logo -->
      <div class="flex h-16 items-center gap-3 border-b border-slate-200 px-5 dark:border-slate-800">
        <img src="/logo-circle.png" alt="应用图标" class="h-8 w-8" />
        <span class="text-lg font-black text-slate-950 dark:text-white">俄语学习平台</span>
        <span class="rounded bg-purple-100 px-1.5 py-0.5 text-[10px] font-bold text-purple-700 dark:bg-purple-950 dark:text-purple-200">管理</span>
      </div>

      <!-- 导航菜单 -->
      <nav class="flex-1 space-y-1 overflow-y-auto px-3 py-4">
        <NuxtLink
          v-for="item in menuItems"
          :key="item.to"
          :to="item.to"
          class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition"
          :class="isActive(item.to)
            ? 'bg-purple-100 text-purple-800 dark:bg-purple-950/60 dark:text-purple-200'
            : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-slate-200'"
          @click="sidebarOpen = false"
        >
          <UIcon :name="item.icon" class="h-5 w-5" />
          {{ item.label }}
        </NuxtLink>
      </nav>

      <!-- 底部用户 -->
      <div class="border-t border-slate-200 p-4 dark:border-slate-800">
        <div class="flex items-center gap-3">
          <div class="flex h-8 w-8 items-center justify-center rounded-full bg-purple-100 text-sm font-bold text-purple-700 dark:bg-purple-950 dark:text-purple-200">
            {{ userInitial }}
          </div>
          <div class="flex-1 truncate">
            <div class="text-sm font-medium text-slate-900 dark:text-slate-100">{{ userName }}</div>
            <div class="text-xs text-slate-400">管理员</div>
          </div>
          <button class="rounded p-1 text-slate-400 hover:text-slate-600 dark:hover:text-slate-300" @click="handleLogout" title="退出登录">
            <UIcon name="i-ph-sign-out" class="h-4 w-4" />
          </button>
        </div>
      </div>
    </aside>

    <!-- 主区域 -->
    <div class="flex min-w-0 flex-1 flex-col">
      <!-- 顶栏 -->
      <header class="flex h-16 items-center gap-4 border-b border-slate-200 bg-white px-4 dark:border-slate-800 dark:bg-slate-900">
        <button class="rounded-lg p-2 text-slate-500 hover:bg-slate-100 lg:hidden dark:hover:bg-slate-800" @click="sidebarOpen = true">
          <UIcon name="i-ph-list" class="h-5 w-5" />
        </button>
        <div class="flex-1" />
        <NuxtLink to="/" class="flex items-center gap-2 text-sm text-slate-400 hover:text-slate-600 dark:hover:text-slate-300">
          <UIcon name="i-ph-arrow-left" class="h-4 w-4" />
          返回前台
        </NuxtLink>
      </header>

      <!-- 内容 -->
      <main class="flex-1 overflow-y-auto p-6">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useRoute } from "vue-router";
import { getStoredUser, signOut } from "~/services/auth";

const route = useRoute();
const sidebarOpen = ref(false);

const menuItems = [
  { to: "/admin", icon: "i-ph-chart-bar", label: "仪表盘" },
  { to: "/admin/courses", icon: "i-ph-book-open", label: "课程管理" },
  { to: "/admin/media", icon: "i-ph-play-circle", label: "媒体管理" },
  { to: "/admin/ai", icon: "i-ph-sparkle", label: "AI 工具" },
  { to: "/admin/users", icon: "i-ph-users-three", label: "用户管理" },
];

function isActive(to: string) {
  if (to === "/admin") return route.path === "/admin";
  return route.path.startsWith(to);
}

const storedUser = getStoredUser();
const userName = computed(() => storedUser?.nickname || storedUser?.username || "管理员");
const userInitial = computed(() => (userName.value.charAt(0) || "A").toUpperCase());

async function handleLogout() {
  await signOut();
}
</script>
