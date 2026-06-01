<template>
  <div class="mx-auto flex w-full max-w-5xl flex-col items-center text-center">
    <div class="mb-8 inline-flex items-center gap-2 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-500 shadow-sm dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300">
      <UIcon
        name="i-ph-translate"
        class="h-4 w-4 text-emerald-600 dark:text-emerald-300"
      />
      根据中文输入俄语
    </div>
    <div class="mb-8 max-w-3xl text-3xl font-black leading-snug text-slate-950 dark:text-gray-50 md:text-4xl">
      {{ courseStore.currentStatement?.sourceText || courseStore.currentStatement?.chinese || "请输入对应的俄语句子" }}
    </div>
    <MainQuestionInput />
  </div>
</template>

<script setup lang="ts">
import { onMounted, watch } from "vue";

import { useCurrentStatementEnglishSound } from "~/composables/main/englishSound";
import { useAutoPlayEnglish } from "~/composables/user/sound";
import { useCourseStore } from "~/store/course";

const courseStore = useCourseStore();
const { playSound } = useCurrentStatementEnglishSound();
const { isAutoPlayEnglish } = useAutoPlayEnglish();

onMounted(() => {
  handleAutoPlayEnglish();
});

watch(
  () => courseStore.currentStatement,
  () => {
    handleAutoPlayEnglish();
  },
);

function handleAutoPlayEnglish() {
  if (isAutoPlayEnglish()) {
    playSound();
  }
}
</script>
