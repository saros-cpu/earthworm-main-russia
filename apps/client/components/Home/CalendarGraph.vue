<template>
  <div class="space-y-4">
    <div class="rounded-md bg-slate-50 p-3 dark:bg-slate-950">
      <div class="mb-3 flex items-center justify-between">
        <div>
          <div class="text-xs font-bold text-slate-500 dark:text-slate-400">最近 7 周</div>
          <div class="mt-1 text-lg font-black text-slate-950 dark:text-white">
            {{ totalLearningTime > 0 ? formatLearningTime(totalLearningTime) : "还未开始" }}
          </div>
        </div>
        <div class="rounded-md bg-white px-2 py-1 text-xs font-bold text-emerald-700 shadow-sm dark:bg-slate-900 dark:text-emerald-300">
          {{ activeDays }} 天
        </div>
      </div>

      <div class="grid grid-cols-[28px_1fr] gap-2">
        <div class="grid grid-rows-7 gap-1 text-[10px] leading-3 text-slate-400">
          <span
            v-for="day in weekLabels"
            :key="day"
            class="flex h-4 items-center justify-end"
          >
            {{ day }}
          </span>
        </div>
        <div class="grid grid-flow-col grid-rows-7 justify-start gap-1">
          <UTooltip
            v-for="(cell, index) in compactCells"
            :key="index"
            :text="cell?.tips"
          >
            <div
              class="cell"
              :class="[cell?.bg]"
            ></div>
          </UTooltip>
        </div>
      </div>
    </div>

    <div class="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400">
      <span>学习强度</span>
      <div class="flex items-center gap-1">
        <span>低</span>
        <div class="cell"></div>
        <div class="cell low"></div>
        <div class="cell moderate"></div>
        <div class="cell high"></div>
        <div class="cell higher"></div>
        <span>高</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, watchEffect } from "vue";

import type { CalendarDataItem, EmitsType } from "~/composables/user/calendarGraph";
import { useCalendarGraph } from "~/composables/user/calendarGraph";

enum ActivityLevel {
  Low = "low",
  Moderate = "moderate",
  High = "high",
  Higher = "higher",
}

const props = defineProps<{
  data: CalendarDataItem[];
  totalLearningTime: number;
}>();

const emits = defineEmits<EmitsType>();

const { initTable, renderBody, tbody } = useCalendarGraph(emits, {
  getActivityLevel(item) {
    if (!item) return "";

    const duration = secondToMinutes(item.duration);
    if (duration < 10) return ActivityLevel.Low;
    if (duration < 30) return ActivityLevel.Moderate;
    if (duration < 60) return ActivityLevel.High;
    return ActivityLevel.Higher;
  },
  tipFormatter(current) {
    if (current.duration === 0) return `${current?.date} 没有学习`;

    let tip = "";
    const minutes = secondToMinutes(current.duration);
    if (minutes < 1) {
      tip = "不足 1 分钟";
    } else {
      tip = ` ${secondToMinutes(current.duration)} 分钟`;
    }
    return `${current.date} 学习${tip}`;
  },
});

const weekLabels = ["一", "二", "三", "四", "五", "六", "日"];
const compactCells = computed(() => {
  if (!tbody.value.length) return [];
  const maxColumns = 7;
  const columnCount = Math.max(...tbody.value.map((row) => row.length));
  const startColumn = Math.max(0, columnCount - maxColumns);
  const cells = [];

  for (let column = startColumn; column < columnCount; column++) {
    for (let row = 1; row < 7; row++) {
      cells.push(tbody.value[row]?.[column] || null);
    }
    cells.push(tbody.value[0]?.[column] || null);
  }

  return cells;
});

const activeDays = computed(() => {
  return props.data.filter((item) => item.duration > 0).length;
});

function secondToMinutes(second: number) {
  return Math.floor(second / 60);
}

function formatLearningTime(totalSeconds: number) {
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);

  if (hours > 0) {
    return `${hours}小时${minutes}分钟`;
  } else {
    if (minutes === 0) {
      return `不足 1 分钟`;
    } else {
      return `${minutes}分钟`;
    }
  }
}

onMounted(() => {
  initTable();
});

watchEffect(() => {
  tbody.value = renderBody(props.data);
});
</script>

<style scoped>
.cell {
  @apply h-4 w-4 rounded border border-white bg-slate-200 transition hover:scale-110 hover:border-emerald-400 dark:border-slate-950 dark:bg-slate-800 dark:hover:border-emerald-300;
}

.low {
  @apply bg-emerald-100 dark:bg-emerald-950;
}

.moderate {
  @apply bg-emerald-300 dark:bg-emerald-800;
}

.high {
  @apply bg-emerald-500 dark:bg-emerald-600;
}

.higher {
  @apply bg-emerald-700 dark:bg-emerald-400;
}
</style>
