<template>
  <section
    id="features"
    class="w-full py-20"
  >
    <div class="mb-10 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
      <div>
        <p class="text-sm font-bold uppercase tracking-normal text-emerald-600 dark:text-emerald-300">
          {{ $t('landing.learningPath') }}
        </p>
        <h2 class="mt-2 text-3xl font-black text-slate-950 dark:text-white md:text-4xl">
          把俄语教材变成可练习的关卡
        </h2>
      </div>
      <p class="max-w-xl text-base leading-7 text-slate-500 dark:text-slate-400">
         此版本更偏俄语零基础路径：先建立字母和词形记忆，再用教材训练真实表达。
      </p>
    </div>

    <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      <article
        v-for="feature in FEATURES"
        :key="feature.title"
        class="rounded-md border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-1 hover:border-emerald-300 hover:shadow-lg dark:border-slate-800 dark:bg-slate-900"
      >
        <div class="mb-4 inline-flex h-10 w-10 items-center justify-center rounded-md bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-white">
          <UIcon
            :name="feature.icon"
            class="h-5 w-5"
          />
        </div>
        <h3 class="text-lg font-black text-slate-950 dark:text-white">{{ feature.title }}</h3>
        <p class="mt-3 text-sm leading-6 text-slate-500 dark:text-slate-400">{{ feature.desc }}</p>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
const FEATURES = [
  {
    icon: "i-ph-keyboard",
    title: "输入式闯关",
    desc: "用键盘完成俄语字母、单词和整句输入，靠即时反馈建立拼写和语序肌肉记忆。",
  },
  {
    icon: "i-ph-book-open-text",
    title: "教材课程包",
    desc: "内置教材课程包已经整理为可练习内容；PDF 自动导入能力仍在建设中。",
  },
  {
    icon: "i-ph-speaker-high",
    title: "语音练习",
    desc: "课程支持俄语朗读和自动播放，方便把看懂、听懂、打出来连成一条练习路径。",
  },
  {
    icon: "i-ph-sparkle",
    title: "课程精炼",
    desc: "后台可以继续补充翻译、词汇点和语法提示，把粗生成课程逐步打磨成正式课程。",
  },
];
</script>
