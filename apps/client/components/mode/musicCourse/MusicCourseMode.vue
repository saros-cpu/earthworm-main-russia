<template>
  <div class="flex h-full flex-col items-center justify-center">
    <div class="mx-auto w-full max-w-3xl text-center">
      <div class="mb-6 inline-flex items-center gap-2 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm font-bold shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <UIcon name="i-ph-music-notes" class="h-4 w-4 text-pink-500" />
        <span class="text-slate-500 dark:text-slate-300">音乐课程 - 跟唱学俄语</span>
      </div>

      <div v-if="currentLine" class="mb-8">
        <div class="mb-4 text-2xl font-black text-slate-950 dark:text-gray-50 md:text-3xl">
          {{ currentLine.russian }}
        </div>
        <div class="text-sm text-slate-400">
          {{ currentLine.translation }}
        </div>
      </div>

      <div v-if="currentLine" class="mb-8">
        <button @click="togglePlay" class="inline-flex h-14 w-14 items-center justify-center rounded-full bg-pink-500 text-white shadow-lg transition hover:bg-pink-600 active:scale-95">
          <UIcon :name="isPlaying ? 'i-ph-pause-fill' : 'i-ph-play-fill'" class="h-7 w-7" />
        </button>
      </div>

      <div v-if="currentLine" class="mb-6">
        <div class="mb-2 text-xs font-bold text-slate-400">歌词进度</div>
        <div class="flex items-center gap-2">
          <button class="rounded-md border border-slate-200 px-3 py-1 text-xs font-bold text-slate-500 transition hover:bg-slate-100" @click="prevLine">
            <UIcon name="i-ph-caret-left" class="h-4 w-4" />
          </button>
          <div class="flex-1 text-center text-sm text-slate-500">{{ currentIndex + 1 }} / {{ lyrics.length }}</div>
          <button class="rounded-md border border-slate-200 px-3 py-1 text-xs font-bold text-slate-500 transition hover:bg-slate-100" @click="nextLine">
            <UIcon name="i-ph-caret-right" class="h-4 w-4" />
          </button>
        </div>
      </div>

      <div v-if="!currentLine" class="mb-8 rounded-md border border-dashed border-slate-300 bg-white p-8 text-slate-400 dark:border-slate-700 dark:bg-slate-900">
        当前课程没有歌词内容。<br/>在课程描述中添加歌词（每行用 | 分隔俄语和翻译）。
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { playRussianText } from "~/composables/main/englishSound";
import { useGameStore } from "~/store/game";
import { useCourseStore } from "~/store/course";

const courseStore = useCourseStore();
const gameStore = useGameStore();
const currentIndex = ref(0);
const isPlaying = ref(false);

interface LyricLine {
  russian: string;
  translation: string;
}

const lyrics = computed<LyricLine[]>(() => {
  const desc = courseStore.currentCourse?.description || "";
  if (!desc) return [];
  return desc.split("\n").filter(Boolean).map((line) => {
    const parts = line.split("|");
    return { russian: parts[0]?.trim() || "", translation: parts[1]?.trim() || "" };
  });
});

const currentLine = computed(() => lyrics.value[currentIndex.value] || null);

function togglePlay() {
  if (isPlaying.value) {
    isPlaying.value = false;
  } else {
    isPlaying.value = true;
    playRussianText(currentLine.value?.russian);
  }
}

function nextLine() {
  if (currentIndex.value < lyrics.value.length - 1) {
    currentIndex.value++;
    isPlaying.value = false;
  } else {
    gameStore.recordAnswer(true);
  }
}

function prevLine() {
  if (currentIndex.value > 0) currentIndex.value--;
}

onMounted(() => { currentIndex.value = 0; });

watch(() => courseStore.statementIndex, () => { currentIndex.value = 0; });
</script>
