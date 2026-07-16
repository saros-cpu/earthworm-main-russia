<template>
  <div class="w-full py-6">
    <section
      class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <p class="text-sm font-bold text-rose-600 dark:text-rose-300">弱点分析</p>
      <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">学习弱点热力图</h1>
      <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
        基于练习中的错误自动分析语法薄弱环节，推荐针对性课程包
      </p>
    </section>

    <section
      v-if="weaknesses.length === 0"
      class="rounded-md border border-dashed border-slate-300 bg-white p-10 text-center text-slate-500 dark:border-slate-700 dark:bg-slate-900"
    >
      <UIcon
        name="i-ph-check-circle"
        class="mx-auto mb-3 h-12 w-12 text-emerald-400"
      />
      <p>暂无错误记录，继续练习吧！</p>
    </section>

    <section
      v-else
      class="space-y-4"
    >
      <section
        class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 class="mb-4 text-base font-bold text-slate-950 dark:text-white">语法弱点分布</h2>
        <div class="space-y-3">
          <div
            v-for="w in weaknesses"
            :key="w.id"
            class="flex items-center gap-4"
          >
            <UIcon
              :name="w.icon"
              class="h-5 w-5 shrink-0 text-slate-400"
            />
            <div class="min-w-0 flex-1">
              <div class="flex items-center justify-between">
                <span class="text-sm font-bold text-slate-800 dark:text-slate-200">{{
                  w.label
                }}</span>
                <span class="text-xs text-slate-400">{{ w.count }} 次错误</span>
              </div>
              <div
                class="mt-1 h-2 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800"
              >
                <div
                  class="h-full rounded-full transition-all"
                  :style="{ width: getPercent(w.count) + '%' }"
                  :class="getBarColor(w.count)"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section
        class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 class="mb-3 text-base font-bold text-slate-950 dark:text-white">推荐课程包</h2>
        <div class="grid gap-3 sm:grid-cols-2">
          <div
            v-for="w in weaknesses.slice(0, 4)"
            :key="w.id"
            class="rounded-md border border-purple-100 bg-purple-50 p-4 dark:border-purple-900 dark:bg-purple-950"
          >
            <div class="mb-1 flex items-center gap-2">
              <UIcon
                :name="w.icon"
                class="h-4 w-4 text-purple-500"
              />
              <span class="text-sm font-bold text-slate-900 dark:text-white">{{
                w.suggestedPackTitle || w.label
              }}</span>
            </div>
            <p class="text-xs text-slate-500">{{ w.description }}</p>
            <NuxtLink
              v-if="w.suggestedPackId"
              :to="'/course-pack/' + w.suggestedPackId"
              class="mt-2 inline-block text-xs font-bold text-purple-600 hover:text-purple-700 dark:text-purple-300"
            >
              查看课程包 →
            </NuxtLink>
          </div>
        </div>
      </section>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useWeaknessAnalysis } from "~/composables/main/weaknessAnalysis";

const { getWeaknesses, getTotalErrors } = useWeaknessAnalysis();
const weaknesses = getWeaknesses();
const total = getTotalErrors();

function getPercent(count: number | undefined) {
  if (total === 0 || !count) return 0;
  return Math.round((count / total) * 100);
}

function getBarColor(count: number | undefined) {
  const pct = getPercent(count ?? 0);
  if (pct >= 30) return "bg-red-500";
  if (pct >= 15) return "bg-amber-500";
  return "bg-emerald-500";
}
</script>
