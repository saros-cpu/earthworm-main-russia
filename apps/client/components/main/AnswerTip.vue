<template>
  <div
    class="absolute left-1/2 top-32 z-20 flex w-3/4 translate-x-[-50%] items-center justify-center text-xl dark:text-gray-50"
  >
    <div class="card rounded-md border border-amber-200 bg-amber-50 shadow-xl dark:border-amber-900 dark:bg-slate-900">
      <div class="card-body relative">
        <div class="absolute right-2 top-1 mt-0">
          <UButton
            color="gray"
            variant="ghost"
            icon="i-heroicons-x-mark-20-solid"
            @click="hiddenAnswerTip"
            tabindex="-1"
            :ui="{ color: { gray: { ghost: 'dark:hover:bg-gray-600' } } }"
          />
        </div>

        <div class="text-3xl font-black text-slate-950 dark:text-white">{{ courseStore.currentStatement?.english }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAnswerTip } from "~/composables/main/answerTip";
import { useCourseStore } from "~/store/course";

const courseStore = useCourseStore();
const { hiddenAnswerTip } = useAnswerTip();
</script>
