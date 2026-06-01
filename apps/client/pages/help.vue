<template>
  <div class="w-full py-6">
    <section class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <p class="text-sm font-bold text-emerald-600 dark:text-emerald-300">{{ $t('pages.help') }}</p>
      <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">帮助文档</h1>
      <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
        俄语学习平台使用说明：按从上到下的顺序读一遍，基本就能上手。
      </p>
    </section>

    <section class="mb-5 grid gap-4 md:grid-cols-2">
      <article v-for="block in BLOCKS" :key="block.title"
        class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div class="mb-3 flex items-center gap-2">
          <UIcon :name="block.icon" class="h-5 w-5 text-emerald-600 dark:text-emerald-300" />
          <h2 class="text-lg font-black text-slate-950 dark:text-white">{{ block.title }}</h2>
        </div>
        <ul class="space-y-2 text-sm leading-7 text-slate-600 dark:text-slate-300">
          <li v-for="(line, i) in block.lines" :key="i" class="flex gap-2">
            <span class="mt-2 inline-block h-1.5 w-1.5 shrink-0 rounded-full bg-emerald-500"></span>
            <span v-html="line"></span>
          </li>
        </ul>
      </article>
    </section>

    <section class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <h2 class="mb-3 text-lg font-black text-slate-950 dark:text-white">快捷键速查</h2>
      <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        <div v-for="kb in KEYBINDS" :key="kb.action"
          class="flex items-center justify-between rounded-md border border-slate-100 px-3 py-2 dark:border-slate-800">
          <span class="text-sm text-slate-700 dark:text-slate-300">{{ kb.action }}</span>
          <kbd class="rounded border border-slate-300 bg-slate-50 px-2 py-0.5 text-xs font-bold text-slate-700 dark:border-slate-700 dark:bg-slate-950 dark:text-slate-200">
            {{ kb.key }}
          </kbd>
        </div>
      </div>
    </section>

    <section class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <h2 class="mb-2 text-lg font-black text-slate-950 dark:text-white">遇到问题？</h2>
      <p class="text-sm leading-7 text-slate-600 dark:text-slate-300">
        点击侧边菜单的「建议反馈」直接在网页留言，开发者会定期查看。
      </p>
    </section>
  </div>
</template>

<script setup lang="ts">
const BLOCKS = [
  {
    icon: "i-ph-rocket-launch",
    title: "开始练习",
    lines: [
      "首页点「先玩一课」或导航栏「功能」可直接进入入门课程。",
      "登录后访问 <b>课程包</b> 浏览全部系列：入门基础、走遍俄罗斯、词汇专项、TORFL 等。",
      "选一节课点「开始闯关」就能进入闯关模式。",
    ],
  },
  {
    icon: "i-ph-book-open-text",
    title: "闯关界面",
    lines: [
      "看到紫色下划线亮起，直接用键盘输入俄语，西里尔输入法或字母键都行。",
      "回车提交。错词会标红，按 <kbd>Space</kbd> 跳到首个错词并清空。",
      "答对后会自动播放该句俄语朗读，方便听写记忆。",
    ],
  },
  {
    icon: "i-ph-graduation-cap",
    title: "复习与生词本",
    lines: [
      "练习中答错的句子自动进入 <b>错题本</b>，集中突破薄弱环节。",
      "<b>生词本</b> 记录练习中遇到的生词，方便回顾。",
      "<b>复习</b> 基于间隔重复算法（SRS），到期会在导航栏显示红色数字。",
    ],
  },
  {
    icon: "i-ph-gear",
    title: "个性化设置",
    lines: [
      "右上角头像 → 设置：可改快捷键、声音、答题样式。",
      "支持深色 / 浅色主题切换，菜单底部一键切换。",
      "TORFL 等级（A1–C2）按 ТРКИ 词频划分，可在「课程包」筛选。",
    ],
  },
];

const KEYBINDS = [
  { action: "开始 / 下一题", key: "Enter" },
  { action: "跳到下个单词 / 错词", key: "Space" },
  { action: "上一个错词", key: "Backspace" },
  { action: "播放朗读", key: "Ctrl + '" },
  { action: "显示答案", key: "Ctrl + ;" },
  { action: "重做当前题", key: "Ctrl + ;" },
];
</script>
