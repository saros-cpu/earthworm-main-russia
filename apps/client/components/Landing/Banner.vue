<template>
  <section
    id="home"
    class="relative -mx-5 min-h-[calc(100vh-4rem)] overflow-hidden bg-[#f7f4ee] text-slate-950 dark:bg-[#111318] dark:text-white"
  >
    <img
      src="~/assets/home-page-preview.png"
      alt="鹅语菌练习界面"
      class="absolute inset-x-0 bottom-0 h-full w-full object-cover object-center opacity-20 saturate-75 dark:opacity-16"
    />
    <div class="absolute inset-0 bg-[linear-gradient(90deg,rgba(247,244,238,0.98)_0%,rgba(247,244,238,0.9)_42%,rgba(247,244,238,0.36)_100%)] dark:bg-[linear-gradient(90deg,rgba(17,19,24,0.98)_0%,rgba(17,19,24,0.9)_44%,rgba(17,19,24,0.42)_100%)]"></div>

    <div class="relative mx-auto flex min-h-[calc(100vh-4rem)] w-full max-w-screen-xl flex-col justify-center px-5 pb-12 pt-20">
      <div class="max-w-3xl">
        <div class="mb-5 inline-flex items-center gap-2 rounded-full border border-slate-300 bg-white/70 px-3 py-1 text-sm font-semibold text-slate-700 shadow-sm dark:border-slate-700 dark:bg-slate-900/70 dark:text-slate-200">
          <span class="h-2 w-2 rounded-full bg-emerald-500"></span>
          从俄语字母到整句表达
        </div>

        <h1 class="text-5xl font-black leading-tight tracking-normal text-slate-950 dark:text-white md:text-6xl lg:text-7xl">
          鹅语菌
        </h1>
        <p class="mt-5 max-w-2xl text-xl leading-9 text-slate-700 dark:text-slate-300 md:text-2xl">
          像玩游戏一样学俄语。先练字母和高频词，再进入教材句子，输入、听读、反馈一口气完成。
        </p>

        <div class="mt-8 flex flex-wrap gap-3">
          <button
            class="inline-flex h-12 items-center rounded-md bg-slate-950 px-6 font-bold text-white shadow-lg shadow-slate-300 transition hover:-translate-y-0.5 hover:bg-slate-800 dark:bg-white dark:text-slate-950 dark:shadow-none"
            type="button"
            @click="handleKeydown"
          >
            先玩一课
          </button>
          <NuxtLink
            to="/course-pack"
            class="inline-flex h-12 items-center rounded-md border border-slate-300 bg-white/80 px-6 font-bold text-slate-800 transition hover:border-emerald-500 hover:text-emerald-700 dark:border-slate-700 dark:bg-slate-900/80 dark:text-slate-100 dark:hover:border-emerald-400 dark:hover:text-emerald-300"
          >
            查看课程包
          </NuxtLink>
        </div>

        <dl class="mt-12 grid max-w-2xl grid-cols-2 gap-3 sm:grid-cols-4">
          <div
            v-for="metric in METRICS"
            :key="metric.label"
            class="border-l border-slate-300 pl-4 dark:border-slate-700"
          >
            <dt class="text-2xl font-black text-slate-950 dark:text-white">{{ metric.value }}</dt>
            <dd class="mt-1 text-sm text-slate-500 dark:text-slate-400">{{ metric.label }}</dd>
          </div>
        </dl>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
const emit = defineEmits(["start-earthworm"]);

const METRICS = [
  { value: "33", label: "俄语字母" },
  { value: "13+", label: "教材 PDF" },
  { value: "1000+", label: "自动练习句" },
  { value: "AI", label: "课程精炼" },
];

function handleKeydown() {
  emit("start-earthworm");
}
</script>
