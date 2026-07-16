<template>
  <div class="w-full py-6">
    <section
      class="mb-6 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <h1 class="text-3xl font-black text-slate-950 dark:text-white">课程市场</h1>
      <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">发现并导入社区分享的俄语课程</p>
    </section>

    <div class="mb-6 flex gap-2 overflow-x-auto pb-2">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        @click="activeTab = tab.key"
        class="whitespace-nowrap rounded-full px-4 py-1.5 text-sm font-semibold transition"
        :class="
          activeTab === tab.key
            ? 'bg-slate-950 text-white dark:bg-white dark:text-slate-950'
            : 'bg-slate-100 text-slate-600 hover:bg-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700'
        "
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="grid gap-4 lg:grid-cols-3">
      <div class="lg:col-span-2">
        <div class="grid gap-4 sm:grid-cols-2">
          <div
            v-for="course in filteredCourses"
            :key="course.id"
            class="relative rounded-md border border-slate-200 bg-white p-4 shadow-sm transition hover:shadow-md dark:border-slate-700 dark:bg-slate-900"
          >
            <div class="mb-2 flex items-start justify-between">
              <div>
                <h3 class="font-bold text-slate-950 dark:text-white">{{ course.title }}</h3>
                <p class="mt-0.5 text-xs text-slate-400">{{ course.author }}</p>
              </div>
              <span
                class="rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-semibold uppercase text-slate-500 dark:bg-slate-800 dark:text-slate-400"
              >
                {{ course.category }}
              </span>
            </div>
            <p class="mb-3 text-sm text-slate-500 dark:text-slate-400">{{ course.description }}</p>
            <div class="mb-3 flex flex-wrap gap-1">
              <span
                v-for="tag in course.tags"
                :key="tag"
                class="rounded bg-emerald-50 px-1.5 py-0.5 text-[10px] font-medium text-emerald-700 dark:bg-emerald-900 dark:text-emerald-300"
              >
                {{ tag }}
              </span>
            </div>
            <div class="flex items-center justify-between">
              <div class="text-xs text-slate-400">
                <span class="mr-3">{{ course.statements.length }} 句</span>
                <span>{{ course.downloads }} 次导入</span>
              </div>
              <button
                @click="handleClone(course.id)"
                class="rounded-full px-3 py-1 text-xs font-bold transition"
                :class="
                  clonedIds.has(course.id)
                    ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900 dark:text-emerald-300'
                    : 'bg-slate-950 text-white hover:bg-slate-800 dark:bg-white dark:text-slate-950 dark:hover:bg-slate-200'
                "
              >
                {{ clonedIds.has(course.id) ? "已导入 ✓" : "导入课程" }}
              </button>
            </div>
          </div>
        </div>
      </div>
      <div>
        <div
          class="sticky top-20 rounded-md border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-900"
        >
          <h3 class="mb-3 text-sm font-bold text-slate-600 dark:text-slate-300">热门趋势</h3>
          <div
            v-for="course in trending"
            :key="course.id"
            class="mb-2 flex items-center gap-3 rounded-lg p-2 transition hover:bg-slate-50 dark:hover:bg-slate-800"
          >
            <div
              class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-amber-100 text-xs font-bold text-amber-700 dark:bg-amber-900 dark:text-amber-300"
            >
              {{ trending.indexOf(course) + 1 }}
            </div>
            <div class="min-w-0">
              <div class="truncate text-sm font-semibold text-slate-950 dark:text-white">
                {{ course.title }}
              </div>
              <div class="text-xs text-slate-400">{{ course.downloads }} 次导入</div>
            </div>
          </div>
          <NuxtLink
            to="/course-editor"
            class="mt-4 block rounded-lg bg-gradient-to-r from-purple-500 to-emerald-500 p-3 text-center text-sm font-bold text-white transition hover:opacity-90"
          >
            创建课程
          </NuxtLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";

import {
  cloneCourseToLocal,
  fetchMarketplaceCourses,
  fetchTrendingCourses,
  isCourseCloned,
} from "~/api/courseMarket";

const tabs = [
  { key: "all", label: "全部" },
  { key: "featured", label: "精选" },
  { key: "official", label: "官方" },
  { key: "community", label: "社区" },
];

const activeTab = ref("all");

const allCourses = ref(fetchMarketplaceCourses());
const trending = ref(fetchTrendingCourses());

const filteredCourses = computed(() => {
  if (activeTab.value === "all") return allCourses.value;
  return allCourses.value.filter((c) => c.category === activeTab.value);
});

const clonedIds = ref(new Set<string>());

function checkCloned() {
  allCourses.value.forEach((c) => {
    if (isCourseCloned(c.id)) clonedIds.value.add(c.id);
  });
}
checkCloned();

function handleClone(id: string) {
  if (clonedIds.value.has(id)) return;
  if (cloneCourseToLocal(id)) {
    clonedIds.value.add(id);
    trending.value = fetchTrendingCourses();
  }
}
</script>
