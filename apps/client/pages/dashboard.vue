<template>
  <div class="mx-auto max-w-screen-xl space-y-6 p-6">
    <header class="flex flex-wrap items-end justify-between gap-3">
      <div>
        <h1 class="text-3xl font-black text-slate-950 dark:text-white">课程库总览</h1>
        <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
          实时统计、按系列分布与单包详情。
        </p>
      </div>
      <div class="flex gap-2">
        <button
          class="inline-flex h-9 items-center rounded-md border border-slate-300 bg-white px-3 text-sm font-bold text-slate-700 hover:border-emerald-400 hover:text-emerald-700 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200"
          :disabled="loading"
          @click="reload"
        >
          {{ loading ? "刷新中…" : "刷新" }}
        </button>
        <span class="inline-flex h-9 items-center rounded-md bg-amber-50 px-3 text-xs font-bold text-amber-700 dark:bg-amber-950 dark:text-amber-300">
          重灌已停用：保护学习记录
        </span>
      </div>
    </header>

    <section v-if="stats" class="grid gap-3 sm:grid-cols-3">
      <article class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div class="text-xs font-bold uppercase tracking-wider text-emerald-700 dark:text-emerald-300">课程包</div>
        <div class="mt-2 text-4xl font-black text-slate-950 dark:text-white">{{ stats.totals.packs }}</div>
      </article>
      <article class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div class="text-xs font-bold uppercase tracking-wider text-purple-700 dark:text-purple-300">课时</div>
        <div class="mt-2 text-4xl font-black text-slate-950 dark:text-white">{{ stats.totals.courses.toLocaleString() }}</div>
      </article>
      <article class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div class="text-xs font-bold uppercase tracking-wider text-amber-700 dark:text-amber-300">练习项</div>
        <div class="mt-2 text-4xl font-black text-slate-950 dark:text-white">{{ stats.totals.statements.toLocaleString() }}</div>
      </article>
    </section>

    <section
      v-if="stats"
      class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <div class="mb-4 flex items-center justify-between">
        <h2 class="text-lg font-black text-slate-950 dark:text-white">按系列分布</h2>
        <span class="text-xs text-slate-500">共 {{ stats.series.length }} 个系列</span>
      </div>
      <div class="space-y-3">
        <div
          v-for="row in stats.series"
          :key="row.key"
          class="rounded-md border border-slate-100 p-3 dark:border-slate-800"
        >
          <div class="mb-2 flex items-center justify-between text-sm">
            <span class="font-bold text-slate-800 dark:text-slate-100">{{ row.label }}</span>
            <span class="text-slate-500 dark:text-slate-400">
              {{ row.packs }} 包 · {{ row.courses }} 课 · <span class="font-bold text-emerald-700 dark:text-emerald-300">{{ row.statements.toLocaleString() }}</span> 项
            </span>
          </div>
          <div class="h-2.5 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800">
            <div
              class="h-full rounded-full bg-gradient-to-r from-emerald-500 to-purple-500"
              :style="{ width: barWidth(row.statements) }"
            ></div>
          </div>
        </div>
      </div>
    </section>

    <section
      v-if="stats"
      class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <div class="mb-4 flex items-center justify-between">
        <h2 class="text-lg font-black text-slate-950 dark:text-white">单包详情（按句数降序）</h2>
        <span class="text-xs text-slate-500">共 {{ stats.packs.length }} 包</span>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="text-xs text-slate-500 dark:text-slate-400">
            <tr>
              <th class="px-2 py-2 text-left font-semibold">#</th>
              <th class="px-2 py-2 text-left font-semibold">课程包</th>
              <th class="px-2 py-2 text-right font-semibold">课时</th>
              <th class="px-2 py-2 text-right font-semibold">练习项</th>
              <th class="px-2 py-2 text-left font-semibold">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100 dark:divide-slate-800">
            <tr
              v-for="(p, i) in stats.packs"
              :key="p.id"
              class="hover:bg-slate-50 dark:hover:bg-slate-800/50"
            >
              <td class="px-2 py-2 text-slate-400">{{ i + 1 }}</td>
              <td class="px-2 py-2">
                <div class="font-semibold text-slate-800 dark:text-slate-100">{{ p.title }}</div>
                <div class="text-xs text-slate-400">{{ p.id }}</div>
              </td>
              <td class="px-2 py-2 text-right font-mono text-slate-700 dark:text-slate-200">{{ p.courses }}</td>
              <td class="px-2 py-2 text-right font-mono font-bold text-emerald-700 dark:text-emerald-300">{{ p.statements.toLocaleString() }}</td>
              <td class="px-2 py-2">
                <NuxtLink
                  :to="`/course-pack/${p.id}`"
                  class="text-xs font-bold text-emerald-700 hover:text-emerald-800 dark:text-emerald-300"
                >
                  打开 →
                </NuxtLink>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section
      v-if="lastMessage"
      class="rounded-md border border-emerald-300 bg-emerald-50 p-3 text-sm text-emerald-900 dark:border-emerald-800 dark:bg-emerald-950 dark:text-emerald-100"
    >
      {{ lastMessage }}
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";

import { getHttp } from "~/api/http";

type Stats = {
  totals: { packs: number; courses: number; statements: number };
  series: Array<{ key: string; label: string; packs: number; courses: number; statements: number }>;
  packs: Array<{ id: string; title: string; courses: number; statements: number }>;
};

const stats = ref<Stats | null>(null);
const loading = ref(false);
const lastMessage = ref("");

const maxStatements = computed(() =>
  stats.value ? Math.max(1, ...stats.value.series.map((s) => s.statements)) : 1,
);

function barWidth(value: number) {
  return `${Math.max(2, Math.round((value / maxStatements.value) * 100))}%`;
}

async function reload() {
  loading.value = true;
  try {
    stats.value = (await getHttp()<Stats>("/admin/stats")) as Stats;
  } catch (e: any) {
    lastMessage.value = `加载失败: ${e?.message || e}`;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  reload();
});

definePageMeta({ title: "课程库总览" });
</script>
