<template>
  <div class="flex h-full flex-col items-center justify-center">
    <div class="mx-auto w-full max-w-3xl text-center">
      <div class="mb-6 inline-flex items-center gap-2 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-500 shadow-sm dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300">
        <UIcon name="i-ph-microphone" class="h-4 w-4 text-emerald-600 dark:text-emerald-300" />
        口语测评 - 跟读评分
      </div>

      <div class="mb-8 text-2xl font-black leading-snug text-slate-950 dark:text-gray-50 md:text-3xl">
        {{ courseStore.currentStatement?.targetText || courseStore.currentStatement?.english || "请选择课程" }}
      </div>

      <div class="mb-6 text-sm text-slate-400">
        {{ courseStore.currentStatement?.sourceText || courseStore.currentStatement?.chinese }}
      </div>

      <div class="mb-8 flex justify-center gap-4">
        <button @click="playReference" class="inline-flex h-12 w-12 items-center justify-center rounded-full bg-emerald-100 text-emerald-600 transition hover:bg-emerald-200 dark:bg-emerald-900 dark:text-emerald-300">
          <UIcon name="i-ph-speaker-high" class="h-6 w-6" />
        </button>
        <button @click="startRecording"
          class="inline-flex h-16 w-16 items-center justify-center rounded-full text-white shadow-lg transition active:scale-95"
          :class="isRecording ? 'bg-red-500 animate-pulse' : 'bg-emerald-500 hover:bg-emerald-600'">
          <UIcon :name="isRecording ? 'i-ph-stop-fill' : 'i-ph-microphone'" class="h-8 w-8" />
        </button>
      </div>

      <div v-if="transcript" class="mb-4 rounded-md border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div class="text-sm text-slate-500">你的发音:</div>
        <div class="text-lg font-bold text-slate-950 dark:text-white">{{ transcript }}</div>
      </div>

      <div v-if="score !== null" class="mb-4">
        <div class="inline-flex items-center gap-2 rounded-full px-6 py-2 text-lg font-black"
          :class="score >= 80 ? 'bg-emerald-100 text-emerald-700' : score >= 50 ? 'bg-amber-100 text-amber-700' : 'bg-red-100 text-red-700'">
          {{ score }} 分
        </div>
      </div>

      <div class="flex justify-center gap-3">
        <button v-if="score !== null" class="btn btn-outline btn-sm" @click="resetAssessment">再来一次</button>
        <button v-if="score !== null" class="btn btn-sm border-none bg-slate-950 text-white hover:bg-slate-800 dark:bg-white dark:text-slate-950"
          @click="goNext">下一句</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { playRussianText } from "~/composables/main/englishSound";
import { useGameStore } from "~/store/game";
import { useCourseStore } from "~/store/course";

const courseStore = useCourseStore();
const gameStore = useGameStore();
const isRecording = ref(false);
const transcript = ref("");
const score = ref<number | null>(null);
let recognition: any = null;

function playReference() {
  playRussianText(courseStore.currentStatement?.english);
}

function startRecording() {
  if (isRecording.value) {
    stopRecording();
    return;
  }

  const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
  if (!SpeechRecognition) {
    transcript.value = "浏览器不支持语音识别 (推荐 Chrome)";
    return;
  }

  recognition = new SpeechRecognition();
  recognition.lang = "ru-RU";
  recognition.continuous = false;
  recognition.interimResults = false;

  recognition.onresult = (event: any) => {
    const spoken = event.results[0][0].transcript;
    transcript.value = spoken;
    stopRecording();
    calculateScore(spoken);
  };

  recognition.onerror = () => {
    transcript.value = "语音识别失败，请检查麦克风权限";
    isRecording.value = false;
  };

  recognition.start();
  isRecording.value = true;
  transcript.value = "";
  score.value = null;
}

function stopRecording() {
  if (recognition) {
    try { recognition.stop(); } catch (_) {}
  }
  isRecording.value = false;
}

function calculateScore(spoken: string) {
  const norm = (s: string) => s.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
  const correct = norm(courseStore.currentStatement?.english || "");
  const userText = norm(spoken);

  if (!correct || !userText) { score.value = 0; return; }

  const correctWords = correct.split(/\s+/).filter(Boolean);
  const userWords = userText.split(/\s+/).filter(Boolean);

  if (userWords.length === 0) { score.value = 0; return; }

  let matches = 0;
  for (const uw of userWords) {
    if (correctWords.some((cw) => cw.includes(uw) || uw.includes(cw))) {
      matches++;
    }
  }

  score.value = Math.round((matches / Math.max(correctWords.length, userWords.length)) * 100);
  gameStore.recordAnswer(score.value >= 60);
}

function resetAssessment() {
  transcript.value = "";
  score.value = null;
}

function goNext() {
  resetAssessment();
  courseStore.toNextStatement();
}

onMounted(() => {
  playReference();
});

watch(() => courseStore.statementIndex, () => {
  resetAssessment();
  setTimeout(() => playReference(), 200);
});
</script>
