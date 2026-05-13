<template>
  <div class="mx-auto flex w-full max-w-5xl flex-col items-center">
    <div class="mb-8 inline-flex items-center gap-2 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-500 shadow-sm dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300">
      <UIcon
        name="i-ph-speaker-high"
        class="h-4 w-4 text-emerald-600 dark:text-emerald-300"
      />
      听音输入俄语
    </div>
    <MainQuestionInput />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, watch } from "vue";

import { useCurrentStatementEnglishSound } from "~/composables/main/englishSound";
import { useCourseStore } from "~/store/course";

usePlayEnglishSound();
const { playSound } = useCurrentStatementEnglishSound();

function usePlayEnglishSound() {
  onMounted(() => {
    const pauseSound = playSound();
    const courseStore = useCourseStore();

    watch(
      () => courseStore.statementIndex,
      () => {
        pauseSound();
        playSound();
      },
    );

    onUnmounted(() => {
      pauseSound();
    });
  });
}
</script>
