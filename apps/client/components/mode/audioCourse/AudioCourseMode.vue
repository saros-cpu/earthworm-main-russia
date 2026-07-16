<template>
  <div class="flex h-full flex-col items-center justify-center">
    <div class="mx-auto w-full max-w-3xl text-center">
      <div
        class="mb-6 inline-flex items-center gap-2 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-500 shadow-sm dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300"
      >
        <UIcon
          name="i-ph-headphones"
          class="h-4 w-4 text-emerald-600 dark:text-emerald-300"
        />
        {{ modeLabel }}
      </div>

      <div
        v-if="streamUrl"
        class="mb-6"
      >
        <video
          v-if="isVideoFile"
          controls
          class="w-full rounded-md shadow-sm"
          @ended="markCompleted"
        >
          <source
            :src="streamUrl"
            type="video/mp4"
          />
        </video>
        <audio
          v-else
          :src="streamUrl"
          controls
          class="w-full"
          @ended="markCompleted"
        ></audio>
      </div>

      <div
        class="mb-8 text-2xl font-black leading-snug text-slate-950 dark:text-gray-50 md:text-3xl"
      >
        {{
          courseStore.currentStatement?.sourceText ||
          courseStore.currentStatement?.chinese ||
          "请选择课程"
        }}
      </div>

      <div
        v-if="showTranscript"
        class="mb-6 rounded-md border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <div class="text-lg font-bold text-slate-950 dark:text-white">
          {{ courseStore.currentStatement?.targetText || courseStore.currentStatement?.english }}
        </div>
        <div class="mt-2 text-sm text-slate-400">
          {{ courseStore.currentStatement?.phonetic || courseStore.currentStatement?.soundmark }}
        </div>
      </div>

      <div class="flex justify-center gap-3">
        <button
          v-if="!showTranscript"
          class="btn btn-sm border-none bg-slate-950 text-white hover:bg-slate-800 dark:bg-white dark:text-slate-950"
          @click="showTranscript = true"
        >
          显示字幕
        </button>
        <button
          v-if="showTranscript"
          class="btn btn-outline btn-sm"
          @click="goNext"
        >
          下一句
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";

import { useCourseStore } from "~/store/course";
import { useGameStore } from "~/store/game";
import { getLocalMediaStreamUrl } from "~/utils/media";

const props = defineProps<{ mode?: string }>();
const courseStore = useCourseStore();
const gameStore = useGameStore();
const showTranscript = ref(false);

const ext = computed(() => {
  const v = courseStore.currentCourse?.video || "";
  const idx = v.lastIndexOf(".");
  return idx >= 0 ? v.substring(idx + 1).toLowerCase() : "";
});

const isVideoFile = computed(() => {
  const e = ext.value;
  return ["mp4", "avi", "mkv", "mov", "wmv", "flv", "webm"].includes(e);
});

const streamUrl = computed(() => {
  return getLocalMediaStreamUrl(courseStore.currentCourse?.video, "&v=mp4");
});

const modeLabel = computed(() => (isVideoFile.value ? "视频学习" : "听力训练"));

function markCompleted() {
  gameStore.recordAnswer(true);
  showTranscript.value = true;
}

function goNext() {
  showTranscript.value = false;
  courseStore.toNextStatement();
}

onMounted(() => {
  const vid = courseStore.currentCourse?.video;
  if (!vid) {
    showTranscript.value = true;
  }
});

watch(
  () => courseStore.statementIndex,
  () => {
    showTranscript.value = false;
  },
);
</script>
