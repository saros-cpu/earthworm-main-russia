<template>
  <div class="mx-auto flex w-full max-w-5xl flex-col items-center">
    <div class="mb-8 inline-flex items-center gap-2 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-500 shadow-sm dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300">
      <UIcon name="i-ph-cursor-click" class="h-4 w-4 text-emerald-600 dark:text-emerald-300" />
      点击单词，按正确顺序拼成句子
    </div>

    <div class="mb-6 max-w-3xl text-center text-2xl font-black leading-snug text-slate-950 dark:text-gray-50 md:text-3xl">
      {{ promptText }}
    </div>

    <!-- Built sentence area -->
    <div class="mb-8 min-h-20 w-full max-w-3xl rounded-md border-2 border-dashed border-emerald-300 bg-emerald-50/50 p-4 dark:border-emerald-700 dark:bg-emerald-950/30">
      <div class="flex flex-wrap items-center gap-2">
        <div v-for="(word, i) in selectedWords" :key="'s-' + i"
          class="cursor-pointer rounded-md border border-emerald-300 bg-white px-3 py-1.5 text-lg font-bold text-emerald-800 shadow-sm transition hover:bg-red-50 hover:text-red-600 dark:border-emerald-700 dark:bg-slate-800 dark:text-emerald-200 dark:hover:bg-red-950 dark:hover:text-red-300"
          @click="removeWord(i)">
          {{ word }}
          <span class="ml-1 text-xs text-slate-400">✕</span>
        </div>
        <div v-if="selectedWords.length === 0" class="text-sm text-slate-400">
          点击下方单词拼成句子...
        </div>
      </div>
    </div>

    <!-- Scrambled word bank -->
    <div class="mb-8 flex max-w-3xl flex-wrap justify-center gap-2">
      <button v-for="(word, i) in availableWords" :key="'a-' + i"
        class="rounded-md border border-slate-200 bg-white px-4 py-2 text-lg font-bold text-slate-950 shadow-sm transition hover:border-emerald-300 hover:bg-emerald-50 hover:text-emerald-700 active:scale-95 dark:border-slate-700 dark:bg-slate-800 dark:text-white dark:hover:border-emerald-600 dark:hover:bg-emerald-950 dark:hover:text-emerald-300"
        @click="selectWord(i)">
        {{ word }}
      </button>
    </div>

    <div v-if="wrongAttempt" class="mb-4 text-sm font-bold text-red-500 animate-shake">
      顺序不对，再试一次
    </div>

    <div class="flex gap-3">
      <button class="btn btn-outline btn-sm" @click="resetAssembly" :disabled="selectedWords.length === 0">
        重置
      </button>
      <button class="btn btn-sm border-none bg-slate-950 text-white hover:bg-slate-800 disabled:opacity-50 dark:bg-white dark:text-slate-950"
        :disabled="selectedWords.length === 0 || !allWordsSelected" @click="submitAssembly">
        提交
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";

import { useCurrentStatementEnglishSound } from "~/composables/main/englishSound";
import { useGameMode } from "~/composables/main/game";
import { useCourseStore } from "~/store/course";
import { useGameStore } from "~/store/game";

const courseStore = useCourseStore();
const gameStore = useGameStore();
const { showAnswer } = useGameMode();
const { playSound } = useCurrentStatementEnglishSound();

const selectedWords = ref<string[]>([]);
const availableWords = ref<string[]>([]);
const wrongAttempt = ref(false);
const submitted = ref(false);

const promptText = computed(() =>
  courseStore.currentStatement?.sourceText || courseStore.currentStatement?.chinese || ""
);

const allWordsSelected = computed(() => availableWords.value.length === 0);

function shuffle<T>(arr: T[]): T[] {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

function initWords() {
  const text = courseStore.currentStatement?.english || "";
  const raw = text.split(/\s+/).filter(Boolean);
  selectedWords.value = [];
  availableWords.value = shuffle(raw);
  wrongAttempt.value = false;
  submitted.value = false;
}

function selectWord(index: number) {
  const word = availableWords.value[index];
  selectedWords.value.push(word);
  availableWords.value.splice(index, 1);
  wrongAttempt.value = false;
}

function removeWord(index: number) {
  const word = selectedWords.value[index];
  availableWords.value.push(word);
  selectedWords.value.splice(index, 1);
}

function resetAssembly() {
  availableWords.value.push(...selectedWords.value);
  selectedWords.value = [];
  wrongAttempt.value = false;
}

function submitAssembly() {
  const correct = (courseStore.currentStatement?.english || "").split(/\s+/).filter(Boolean);
  const answer = selectedWords.value;
  const isCorrect = correct.length === answer.length && correct.every((w, i) => w === answer[i]);

  if (isCorrect) {
    gameStore.recordAnswer(true);
    submitted.value = true;
    showAnswer();
  } else {
    gameStore.recordAnswer(false);
    wrongAttempt.value = true;
    setTimeout(() => { wrongAttempt.value = false; }, 1500);
  }
}

onMounted(() => {
  initWords();
  playSound();
});

watch(() => courseStore.statementIndex, () => {
  initWords();
  setTimeout(() => playSound(), 100);
});
</script>

<style scoped>
.animate-shake {
  animation: shake 0.4s ease-in-out;
}
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-8px); }
  50% { transform: translateX(8px); }
  75% { transform: translateX(-4px); }
}
</style>
