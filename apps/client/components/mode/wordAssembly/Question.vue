<template>
  <div class="mx-auto flex w-full max-w-5xl flex-col items-center" @keydown="handleKeydown" tabindex="0" ref="containerRef">
    <div class="mb-4 inline-flex items-center gap-2 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-500 shadow-sm dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300">
      <UIcon name="i-ph-keyboard" class="h-4 w-4 text-emerald-600 dark:text-emerald-300" />
      点击或拖拽单词拼成句子 · ← → 移动 · Backspace 移除
    </div>

    <div class="mb-6 max-w-3xl text-center text-2xl font-black leading-snug text-slate-950 dark:text-gray-50 md:text-3xl">
      {{ promptText }}
    </div>

    <div class="mb-6 flex items-center gap-4 text-sm text-slate-400">
      <span>{{ selectedWords.length }} / {{ totalWords }} 词</span>
      <span v-if="timerRunning" class="font-mono text-emerald-600">{{ elapsed }}s</span>
    </div>

    <!-- Built sentence area with drag-drop -->
    <div class="mb-6 min-h-20 w-full max-w-3xl rounded-md border-2 border-dashed border-emerald-300 bg-emerald-50/50 p-4 dark:border-emerald-700 dark:bg-emerald-950/30"
      @dragover.prevent @drop="onDropToSelected">
      <div class="flex flex-wrap items-center gap-2">
        <div v-for="(word, i) in selectedWords" :key="'s-' + i"
          draggable="true"
          @dragstart="onDragStart(i, 'selected')"
          @dragover.prevent="dragOverIndex = i"
          @dragleave="dragOverIndex = -1"
          @drop="onDropReorder(i)"
          class="assembled-word"
          :class="[dragOverIndex === i ? 'drag-over' : '', focusIndex === i ? 'ring-2 ring-emerald-400' : '']"
          @click="removeWord(i)">
          {{ word }}
          <span class="ml-1 text-[10px] opacity-60">✕</span>
        </div>
        <div v-if="selectedWords.length === 0" class="w-full py-4 text-center text-sm text-slate-400">
          点击下方单词或拖拽到这里
        </div>
      </div>
    </div>

    <!-- Scrambled word bank with drag -->
    <div class="mb-6 flex max-w-3xl flex-wrap justify-center gap-2">
      <div v-for="(word, i) in availableWords" :key="'a-' + i"
        draggable="true"
        @dragstart="onDragStart(i, 'available')"
        class="word-chip"
        @click="selectWord(i)">
        {{ word }}
      </div>
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
        提交 (Enter)
      </button>
    </div>

    <div class="mt-4 flex gap-4 text-xs text-slate-400">
      <span>← → 选中移动</span>
      <span>Backspace 移除</span>
      <span>拖拽排序</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from "vue";

import { useCurrentStatementEnglishSound } from "~/composables/main/englishSound";
import { useGameMode } from "~/composables/main/game";
import { useCourseStore } from "~/store/course";
import { useGameStore } from "~/store/game";

const courseStore = useCourseStore();
const gameStore = useGameStore();
const { showAnswer } = useGameMode();
const { playSound } = useCurrentStatementEnglishSound();

const containerRef = ref<HTMLDivElement>();
const selectedWords = ref<string[]>([]);
const availableWords = ref<string[]>([]);
const wrongAttempt = ref(false);
const submitted = ref(false);
const focusIndex = ref(-1);
const dragOverIndex = ref(-1);
const dragSource = ref<{ type: string; index: number } | null>(null);
const elapsed = ref(0);
const timerRunning = ref(false);
let timerInterval: ReturnType<typeof setInterval> | null = null;

const promptText = computed(() =>
  courseStore.currentStatement?.sourceText || courseStore.currentStatement?.chinese || ""
);

const allWordsSelected = computed(() => availableWords.value.length === 0);
const totalWords = computed(() => selectedWords.value.length + availableWords.value.length);

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
  focusIndex.value = -1;
  elapsed.value = 0;
  startTimer();
}

function startTimer() {
  stopTimer();
  timerRunning.value = true;
  timerInterval = setInterval(() => { elapsed.value++; }, 1000);
}

function stopTimer() {
  if (timerInterval) { clearInterval(timerInterval); timerInterval = null; }
  timerRunning.value = false;
}

function selectWord(index: number) {
  if (index < 0 || index >= availableWords.value.length) return;
  const word = availableWords.value[index];
  selectedWords.value.push(word);
  availableWords.value.splice(index, 1);
  focusIndex.value = selectedWords.value.length - 1;
  wrongAttempt.value = false;
}

function removeWord(index: number) {
  if (index < 0 || index >= selectedWords.value.length) return;
  const word = selectedWords.value[index];
  availableWords.value.push(word);
  selectedWords.value.splice(index, 1);
  if (focusIndex.value >= selectedWords.value.length) {
    focusIndex.value = selectedWords.value.length - 1;
  }
}

// Drag & drop
function onDragStart(index: number, type: string) {
  dragSource.value = { type, index };
}

function onDropToSelected(e: DragEvent) {
  if (!dragSource.value) return;
  if (dragSource.value.type === 'available') {
    selectWord(dragSource.value.index);
  }
  dragSource.value = null;
}

function onDropReorder(targetIndex: number) {
  dragOverIndex.value = -1;
  if (!dragSource.value || dragSource.value.type !== 'selected') return;
  const from = dragSource.value.index;
  if (from === targetIndex) return;
  const [word] = selectedWords.value.splice(from, 1);
  selectedWords.value.splice(targetIndex, 0, word);
  focusIndex.value = targetIndex;
  dragSource.value = null;
}

// Keyboard navigation
function handleKeydown(e: KeyboardEvent) {
  if (submitted.value) return;

  if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
    e.preventDefault();
    if (focusIndex.value < selectedWords.value.length - 1) {
      focusIndex.value++;
    } else if (availableWords.value.length > 0) {
      selectWord(0);
    }
  } else if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') {
    e.preventDefault();
    if (focusIndex.value > 0) {
      focusIndex.value--;
    }
  } else if (e.key === 'Backspace' && focusIndex.value >= 0) {
    e.preventDefault();
    removeWord(focusIndex.value);
  } else if (e.key === 'Enter' && allWordsSelected.value) {
    e.preventDefault();
    submitAssembly();
  } else if (e.key === 'r' || e.key === 'R') {
    if (selectedWords.value.length > 0) resetAssembly();
  }
}

function resetAssembly() {
  availableWords.value.push(...selectedWords.value);
  selectedWords.value = [];
  focusIndex.value = -1;
  wrongAttempt.value = false;
}

function submitAssembly() {
  stopTimer();
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
  containerRef.value?.focus();
});

onUnmounted(() => {
  stopTimer();
});

watch(() => courseStore.statementIndex, () => {
  initWords();
  setTimeout(() => playSound(), 100);
});
</script>

<style scoped>
.assembled-word {
  cursor: pointer;
  border-radius: 0.5rem;
  border: 1px solid #6ee7b7;
  background: white;
  padding: 0.375rem 0.75rem;
  font-size: 1.125rem;
  font-weight: 700;
  color: #065f46;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  transition: all 0.15s;
  user-select: none;
}
.assembled-word:hover {
  background: #fef2f2;
  color: #dc2626;
  border-color: #fca5a5;
}
.drag-over {
  border-color: #10b981;
  background: #d1fae5;
  transform: scale(1.05);
}
.word-chip {
  cursor: pointer;
  border-radius: 0.5rem;
  border: 1px solid #e2e8f0;
  background: white;
  padding: 0.5rem 1rem;
  font-size: 1.125rem;
  font-weight: 700;
  color: #020617;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  transition: all 0.15s;
  user-select: none;
}
.word-chip:hover {
  border-color: #6ee7b7;
  background: #ecfdf5;
  color: #047857;
  transform: scale(1.05);
}
.word-chip:active {
  transform: scale(0.95);
}
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
