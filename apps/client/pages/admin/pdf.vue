<template>
  <div class="space-y-6">
    <header>
      <h1 class="text-2xl font-black text-slate-950 dark:text-white">PDF 导入</h1>
      <p class="mt-1 text-sm text-slate-500">该功能尚未完成，目前不可用</p>
    </header>

    <section class="rounded-xl border border-amber-200 bg-amber-50 p-5 text-sm text-amber-900 dark:border-amber-800 dark:bg-amber-950 dark:text-amber-100">
      PDF 导入暂未启用，系统不会创建虚假的导入任务或课程包。
    </section>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: "admin", middleware: "admin" });
</script>
