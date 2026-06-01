<template>
  <div class="w-full py-6">
    <section class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <p class="text-sm font-bold text-emerald-600 dark:text-emerald-300">统计 / Статистика</p>
      <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">你的学习统计 / Ваша статистика</h1>
      <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">查看你的成绩、学习进度和每日活动记录 / Ваши результаты, прогресс и ежедневная активность</p>
    </section>

    <section class="mb-5 grid gap-4 md:grid-cols-4">
      <div class="rounded-md border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div class="text-xs font-semibold text-slate-400">{{ $t('stats.total') }}</div>
        <div class="mt-1 text-2xl font-black text-slate-950 dark:text-white">{{ totalStats.totalExercises || 0 }}</div>
      </div>
      <div class="rounded-md border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div class="text-xs font-semibold text-slate-400">{{ $t('stats.accuracy') }}</div>
        <div class="mt-1 text-2xl font-black text-emerald-600 dark:text-emerald-400">{{ accuracy }}%</div>
      </div>
      <div class="rounded-md border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div class="text-xs font-semibold text-slate-400">{{ $t('stats.streak') }}</div>
        <div class="mt-1 text-2xl font-black text-amber-600 dark:text-amber-400">{{ totalStats.currentStreak || 0 }} {{ $t('stats.days') }}</div>
      </div>
      <div class="rounded-md border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div class="text-xs font-semibold text-slate-400">{{ $t('stats.points') }}</div>
        <div class="mt-1 text-2xl font-black text-purple-600 dark:text-purple-400">{{ totalStats.totalScore || 0 }}</div>
      </div>
    </section>

    <section class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <h2 class="mb-4 text-lg font-bold text-slate-950 dark:text-white">{{ $t('stats.heatmap') }}</h2>
      <div class="flex flex-wrap gap-1">
        <div
          v-for="(day, i) in heatmapData"
          :key="i"
          class="heatmap-cell"
          :class="getHeatmapColor(day.count)"
          :title="$t('stats.exercises', { count: day.count })"
        >
        </div>
      </div>
      <div class="mt-3 flex items-center gap-2 text-xs text-slate-400">
        <span>{{ $t('stats.little') }}</span>
        <div class="h-3 w-3 rounded-sm bg-slate-100 dark:bg-slate-800"></div>
        <div class="h-3 w-3 rounded-sm bg-emerald-200 dark:bg-emerald-900"></div>
        <div class="h-3 w-3 rounded-sm bg-emerald-400 dark:bg-emerald-700"></div>
        <div class="h-3 w-3 rounded-sm bg-emerald-600 dark:bg-emerald-500"></div>
        <div class="h-3 w-3 rounded-sm bg-emerald-800 dark:bg-emerald-300"></div>
        <span>{{ $t('stats.much') }}</span>
      </div>
    </section>

    <section class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <h2 class="mb-4 text-lg font-bold text-slate-950 dark:text-white">{{ $t('stats.details') }}</h2>
      <div v-if="dailyStats.length === 0" class="py-8 text-center text-sm text-slate-400">
        {{ $t('stats.noData') }}
      </div>
      <div v-else class="space-y-3">
        <div v-for="stat in dailyStats" :key="stat.date"
          class="flex items-center justify-between rounded-md border border-slate-100 p-3 dark:border-slate-800"
        >
          <div>
            <div class="text-sm font-bold text-slate-950 dark:text-white">{{ stat.date }}</div>
            <div class="text-xs text-slate-400">
              {{ stat.totalExercises }} {{ $t('stats.ex') }} · {{ stat.correctExercises }} {{ $t('stats.correct') }} · {{ formatTime(stat.totalTimeSeconds) }}
            </div>
          </div>
          <div class="text-right">
            <div class="text-sm font-bold text-emerald-600 dark:text-emerald-400">{{ stat.totalScore }} {{ $t('stats.pt') }}</div>
            <div class="text-xs text-slate-400">{{ $t('stats.maxStreak') }} {{ stat.maxCombo }}x</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { fetchDailyStats, fetchTotalStats, type DailyStat, type UserTotalStats } from "~/api/learning";

const dailyStats = ref<DailyStat[]>([]);
const totalStats = ref<UserTotalStats>({} as UserTotalStats);

const accuracy = computed(() => {
  const s = totalStats.value;
  if (!s || !s.totalExercises) return 0;
  return Math.round((s.totalCorrect / s.totalExercises) * 100);
});

const heatmapData = computed(() => {
  const data: { date: string; count: number }[] = [];
  const end = new Date();
  const start = new Date();
  start.setDate(start.getDate() - 83);

  const map = new Map(dailyStats.value.map((s) => [s.date, s.totalExercises]));

  for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
    const dateStr = d.toISOString().split("T")[0];
    data.push({ date: dateStr, count: map.get(dateStr) || 0 });
  }
  return data;
});

function getHeatmapColor(count: number): string {
  if (count === 0) return "bg-slate-100 dark:bg-slate-800";
  if (count <= 5) return "bg-emerald-200 dark:bg-emerald-900";
  if (count <= 15) return "bg-emerald-400 dark:bg-emerald-700";
  if (count <= 30) return "bg-emerald-600 dark:bg-emerald-500";
  return "bg-emerald-800 dark:bg-emerald-300";
}

function formatTime(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  if (m === 0) return `${s}с`;
  return `${m}м ${s}с`;
}

onMounted(async () => {
  try {
    dailyStats.value = await fetchDailyStats();
    totalStats.value = await fetchTotalStats();
  } catch (e) {
    console.error("Failed to load stats", e);
  }
});
</script>

<style scoped>
.heatmap-cell {
  width: 14px;
  height: 14px;
  border-radius: 3px;
}
</style>
