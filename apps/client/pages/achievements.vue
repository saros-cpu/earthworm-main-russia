<template>
  <div class="w-full py-6">
    <section
      class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <p class="text-sm font-bold text-amber-600 dark:text-amber-300">游戏成就</p>
      <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">成就徽章</h1>
      <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
        已解锁 {{ unlocked.length }}/{{ all.length }}（{{ progress }}%）
      </p>
      <div class="mt-3 h-2 w-full overflow-hidden rounded-full bg-slate-200 dark:bg-slate-700">
        <div
          class="h-full rounded-full bg-gradient-to-r from-amber-400 to-emerald-500 transition-all"
          :style="{ width: progress + '%' }"
        ></div>
      </div>
    </section>

    <section class="mb-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <div
        v-for="ach in unlocked"
        :key="ach.id"
        class="rounded-md border border-emerald-200 bg-white p-4 shadow-sm transition hover:shadow-md dark:border-emerald-800 dark:bg-slate-900"
      >
        <div class="flex items-center gap-3">
          <div
            class="flex h-12 w-12 items-center justify-center rounded-full bg-emerald-100 text-emerald-600 dark:bg-emerald-900 dark:text-emerald-300"
          >
            <UIcon
              :name="ach.icon"
              class="h-6 w-6"
            />
          </div>
          <div>
            <div class="font-bold text-slate-950 dark:text-white">{{ ach.title }}</div>
            <div class="text-xs text-slate-500">{{ ach.description }}</div>
          </div>
        </div>
      </div>
      <div
        v-for="ach in locked"
        :key="ach.id"
        class="rounded-md border border-slate-200 bg-white p-4 opacity-50 dark:border-slate-700 dark:bg-slate-900"
      >
        <div class="flex items-center gap-3">
          <div
            class="flex h-12 w-12 items-center justify-center rounded-full bg-slate-100 text-slate-300 dark:bg-slate-800"
          >
            <UIcon
              name="i-ph-lock"
              class="h-5 w-5"
            />
          </div>
          <div>
            <div class="font-bold text-slate-400">{{ ach.title }}</div>
            <div class="text-xs text-slate-400">{{ ach.description }}</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useAchievements } from "~/composables/main/achievements";

const { all, unlocked, locked, progress } = useAchievements();
</script>
