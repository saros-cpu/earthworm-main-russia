<template>
  <div class="flex flex-col items-center gap-6">
    <div class="w-full max-w-xl">
      <div class="rounded-xl bg-gradient-to-br from-indigo-500 to-indigo-800 p-6 text-center text-white shadow-lg">
        <UIcon name="i-ph-music-notes" class="mb-3 h-12 w-12" />
        <div class="mb-4 text-sm opacity-80">Audio · LRC 字幕</div>
        <audio ref="audioRef" :src="src" controls class="w-full" @error="onError" @timeupdate="onTimeUpdate">
          您的浏览器不支持音频播放。
        </audio>
      </div>
    </div>

    <div v-if="error" class="w-full max-w-xl rounded-md border border-rose-300 bg-rose-50 p-3 text-xs text-rose-700 dark:border-rose-900 dark:bg-rose-950/40 dark:text-rose-200">
      {{ error }}
      <button class="btn btn-xs ml-2" @click="retry">重试</button>
    </div>

    <div class="w-full max-w-xl">
      <h3 class="mb-3 text-sm font-bold text-slate-950 dark:text-white">{{ title }}</h3>

      <div
        ref="lrcContainer"
        class="scrollbar-thin relative h-80 overflow-y-auto rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
        @click="handleLrcClick"
      >
        <div v-if="parsedLines.length" class="space-y-2 text-center">
          <div
            v-for="(line, idx) in parsedLines"
            :key="idx"
            :data-time="line.time"
            :class="[
              'cursor-pointer rounded px-3 py-2 text-sm leading-6 transition-all duration-300',
              currentLrcIndex === idx
                ? 'scale-105 bg-emerald-100 font-bold text-emerald-800 dark:bg-emerald-950 dark:text-emerald-200'
                : 'text-slate-500 hover:bg-slate-50 dark:text-slate-400 dark:hover:bg-slate-800',
            ]"
          >
            <span v-if="line.translation" class="block text-xs text-slate-400 dark:text-slate-500">{{ line.russian }}</span>
            <span>{{ line.translation || line.russian }}</span>
          </div>
        </div>
        <div v-else class="flex h-full items-center justify-center text-sm text-slate-500">
          <div class="text-center">
            <UIcon name="i-ph-file-text" class="mx-auto mb-2 h-8 w-8 text-slate-300 dark:text-slate-600" />
            <p>暂无 LRC 字幕</p>
            <p class="mt-1 text-xs">字幕加载后将在此同步高亮显示</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";

const props = defineProps<{
  src: string;
  title: string;
  lrcLines?: { time: number; russian: string; translation: string }[];
}>();

const audioRef = ref<HTMLAudioElement | null>(null);
const lrcContainer = ref<HTMLDivElement | null>(null);
const error = ref("");
const currentTime = ref(0);

interface LrcLine {
  time: number;
  russian: string;
  translation: string;
}

const parsedLines = computed<LrcLine[]>(() => {
  if (props.lrcLines?.length) {
    return props.lrcLines.map((line) => ({
      ...line,
      translation: line.translation || "",
    }));
  }
  return [];
});

const currentLrcIndex = computed(() => {
  const t = currentTime.value;
  let idx = parsedLines.value.length - 1;
  for (let i = 0; i < parsedLines.value.length; i++) {
    if (t < parsedLines.value[i].time) {
      idx = i - 1;
      break;
    }
  }
  return Math.max(0, idx);
});

function onError(e: Event) {
  const target = e.target as HTMLMediaElement;
  error.value = target.error?.message || "播放失败";
}

function retry() {
  error.value = "";
  if (audioRef.value) {
    audioRef.value.load();
    audioRef.value.play();
  }
}

function onTimeUpdate() {
  if (audioRef.value) {
    currentTime.value = audioRef.value.currentTime;
  }
}

function handleLrcClick(e: MouseEvent) {
  const target = e.target as HTMLElement;
  const lineEl = target.closest("[data-time]") as HTMLElement | null;
  if (lineEl && audioRef.value) {
    const time = parseFloat(lineEl.dataset.time || "0");
    audioRef.value.currentTime = time;
    audioRef.value.play();
  }
}

function scrollToCurrentLine() {
  if (!lrcContainer.value) return;
  const active = lrcContainer.value.querySelector(".scale-105");
  if (active) {
    active.scrollIntoView({ block: "center", behavior: "smooth" });
  }
}

watch(currentLrcIndex, () => {
  scrollToCurrentLine();
});
</script>
