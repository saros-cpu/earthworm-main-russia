<template>
  <div class="grid w-full gap-5 py-5 lg:grid-cols-[220px_minmax(0,1fr)_280px]">
    <aside class="hidden lg:block">
      <div class="sticky top-20 space-y-3">
        <div class="rounded-md border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
          <div class="flex items-center gap-3">
            <img
              class="h-12 w-12 rounded-md object-cover"
              :src="userStore.user?.avatar || '/logo.png'"
              alt=""
            />
            <div class="min-w-0">
              <div class="truncate font-black text-slate-950 dark:text-white">
                {{ userStore.user?.username || "俄语学习者" }}
              </div>
              <div class="truncate text-xs text-slate-500 dark:text-slate-400">
                {{ userStore.user?.name || "继续今天的练习" }}
              </div>
            </div>
          </div>
        </div>

        <nav class="rounded-md border border-slate-200 bg-white p-2 text-sm shadow-sm dark:border-slate-800 dark:bg-slate-900">
          <NuxtLink
            v-for="item in SIDE_NAV"
            :key="item.label"
            :to="item.href"
            class="flex items-center gap-3 rounded-md px-3 py-2 font-semibold text-slate-600 transition hover:bg-slate-100 hover:text-emerald-700 dark:text-slate-300 dark:hover:bg-slate-800 dark:hover:text-emerald-300"
          >
            <UIcon
              :name="item.icon"
              class="h-5 w-5"
            />
            {{ item.label }}
          </NuxtLink>
        </nav>
      </div>
    </aside>

    <main class="min-w-0 space-y-5">
      <section class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div class="mb-5 flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-black text-slate-950 dark:text-white">我的主页</h1>
            <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">今天先完成一组输入练习，让俄语保持在线。</p>
          </div>
          <NuxtLink
            to="/course-pack"
            class="hidden h-10 items-center rounded-md bg-slate-950 px-4 text-sm font-bold text-white transition hover:bg-slate-800 dark:bg-white dark:text-slate-950 md:inline-flex"
          >
            选择课程
          </NuxtLink>
        </div>

        <div class="grid gap-3 md:grid-cols-3">
          <article
            v-for="task in DAILY_TASKS"
            :key="task.title"
            class="rounded-md border border-slate-200 bg-slate-50 p-4 dark:border-slate-800 dark:bg-slate-950"
          >
            <div class="mb-4 flex items-center gap-3">
              <span class="flex h-10 w-10 items-center justify-center rounded-md bg-white text-emerald-700 shadow-sm dark:bg-slate-900 dark:text-emerald-300">
                <UIcon
                  :name="task.icon"
                  class="h-5 w-5"
                />
              </span>
              <div>
                <div class="font-black text-slate-950 dark:text-white">{{ task.title }}</div>
                <div class="text-xs text-slate-500 dark:text-slate-400">{{ task.reward }}</div>
              </div>
            </div>
            <NuxtLink
              :to="task.href"
              class="inline-flex h-9 items-center rounded-md border border-slate-300 bg-white px-3 text-sm font-bold text-slate-700 transition hover:border-emerald-400 hover:text-emerald-700 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200 dark:hover:border-emerald-400 dark:hover:text-emerald-300"
            >
              去完成
            </NuxtLink>
          </article>
        </div>
      </section>

      <section class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div class="mb-4 flex items-center justify-between">
          <h2 class="text-xl font-black text-slate-950 dark:text-white">最近学习</h2>
          <NuxtLink
            href="/course-pack"
            class="text-sm font-bold text-emerald-700 hover:text-emerald-800 dark:text-emerald-300"
          >
            更多课程
          </NuxtLink>
        </div>
        <HomeRecentCoursePack />
      </section>
    </main>

    <aside class="space-y-5">
      <MainDailyTasks />
      <section class="rounded-md border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div class="mb-3 flex items-center justify-between">
          <h2 class="font-black text-slate-950 dark:text-white">学习热力图</h2>
          <span class="text-xs text-slate-500 dark:text-slate-400">2026</span>
        </div>
        <HomeCalendarGraph
          :data="learningDailyTimeList"
          :totalLearningTime="learningDailyTotalTime"
          @toggleYear="toggleYear"
        />
      </section>

      <section class="rounded-md bg-emerald-600 p-4 text-white shadow-sm">
        <div class="mb-2 flex items-center gap-2 font-black">
          <UIcon
            name="i-ph-sparkle"
            class="h-5 w-5"
          />
          教材已就绪
        </div>
        <p class="text-sm leading-6 text-emerald-50">
          《走遍俄罗斯》PDF 已转成课程包，可以先从第一册或单词汇总开始练。
        </p>
        <NuxtLink
          to="/course-pack"
          class="mt-4 inline-flex h-9 w-full items-center justify-center rounded-md bg-white text-sm font-black text-emerald-700"
        >
          查看课程包
        </NuxtLink>
      </section>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { useAsyncData } from "#imports";
import { ref } from "vue";

import { fetchTodayLearningTime } from "~/api/user-learning-activity";
import { useLearningDailyTime } from "~/composables/learningDailyTime";
import { type CalendarDataItem } from "~/composables/user/calendarGraph";
import { useUserStore } from "~/store/user";
import { useLearningTimeTracker } from "../../composables/main/learningTimeTracker";

const SIDE_NAV = [
  { label: "主页", href: "/", icon: "i-ph-house" },
  { label: "我的课程包", href: "/course-pack", icon: "i-ph-folder-open" },
  { label: "学习统计", href: "/stats", icon: "i-ph-chart-bar" },
  { label: "复习", href: "/review", icon: "i-ph-arrows-clockwise" },
  { label: "错题本", href: "/wrong-answers", icon: "i-ph-x-circle" },
  { label: "生词本", href: "/vocabulary", icon: "i-ph-bookmark-simple" },
  { label: "学习小组", href: "/groups", icon: "i-ph-users-three" },
  { label: "掌握列表", href: "/mastered-elements", icon: "i-ph-check-circle" },
  { label: "设置", href: "/User/Setting", icon: "i-ph-gear" },
];

const userStore = useUserStore();
const { learningDailyTimeList, learningDailyTotalTime, setupLearningDailyTime } =
  useLearningDailyTime();
const { toggleYear } = useCalendarGraph();

useAsyncData(async () => {
  const { setupLearningTime } = useLearningTimeTracker();
  setupLearningTime(await fetchTodayLearningTime());
});

function useCalendarGraph() {
  const data = ref<CalendarDataItem[]>([]);
  const totalLearningTime = ref<number>(0);

  async function toggleYear(year?: number) {
    setupLearningDailyTime();
  }

  return {
    data,
    totalLearningTime,
    toggleYear,
  };
}
</script>
