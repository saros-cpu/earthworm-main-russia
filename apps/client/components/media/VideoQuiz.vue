<template>
  <div class="flex flex-col gap-6 lg:flex-row">
    <div class="flex-1">
      <div
        v-if="type === 'video'"
        class="overflow-hidden rounded-xl bg-black shadow-lg"
      >
        <video
          ref="mediaRef"
          controls
          class="w-full"
          style="max-height: 50vh"
          @error="onError"
        >
          <source
            :src="src"
            type="video/mp4"
          />
        </video>
      </div>
      <div
        v-else
        class="overflow-hidden rounded-xl bg-gradient-to-br from-emerald-500 to-emerald-800 p-6 text-center shadow-lg"
      >
        <UIcon
          name="i-ph-headphones"
          class="mb-2 h-10 w-10 text-white"
        />
        <audio
          ref="mediaRef"
          :src="src"
          controls
          class="w-full"
          @error="onError"
        ></audio>
      </div>

      <div
        v-if="error"
        :class="[
          'mt-3 rounded-md border p-3 text-xs',
          isTranscoding
            ? 'border-amber-300 bg-amber-50 text-amber-700 dark:border-amber-900 dark:bg-amber-950/40 dark:text-amber-200'
            : 'border-rose-300 bg-rose-50 text-rose-700 dark:border-rose-900 dark:bg-rose-950/40 dark:text-rose-200',
        ]"
      >
        {{ error }}
        <button
          v-if="!isTranscoding"
          class="btn btn-xs ml-2"
          @click="retry"
        >
          重试
        </button>
      </div>
    </div>

    <div class="flex w-full flex-col lg:w-96">
      <div class="mb-3 flex items-center justify-between">
        <h3 class="font-bold text-slate-950 dark:text-white">
          答题练习
          <span class="ml-2 text-xs font-normal text-slate-500"
            >{{ currentIndex + 1 }} / {{ statements.length }}</span
          >
        </h3>
        <button
          class="rounded p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-800 dark:hover:text-slate-300"
          @click="showAnswer = !showAnswer"
        >
          <UIcon
            :name="showAnswer ? 'i-ph-eye-slash' : 'i-ph-eye'"
            class="h-4 w-4"
          />
        </button>
      </div>

      <div
        v-if="statements.length"
        class="flex-1 space-y-3 overflow-y-auto"
        style="max-height: 50vh"
      >
        <div
          v-for="(stmt, idx) in statements"
          :key="stmt.id || idx"
          :class="[
            'cursor-pointer rounded-lg border p-3 text-sm transition',
            currentIndex === idx
              ? 'border-emerald-400 bg-emerald-50 dark:border-emerald-600 dark:bg-emerald-950/40'
              : 'border-slate-200 bg-white hover:border-slate-300 dark:border-slate-800 dark:bg-slate-900 dark:hover:border-slate-700',
          ]"
          @click="selectStatement(idx)"
        >
          <div class="font-bold text-slate-950 dark:text-white">{{ stmt.chinese }}</div>
          <div
            v-if="showAnswer || currentIndex === idx"
            class="mt-1 text-emerald-700 dark:text-emerald-300"
          >
            {{ stmt.english }}
          </div>
          <div
            v-if="(showAnswer || currentIndex === idx) && stmt.soundmark"
            class="mt-0.5 text-xs text-slate-400"
          >
            {{ stmt.soundmark }}
          </div>
        </div>
      </div>
      <div
        v-else
        class="flex flex-1 items-center justify-center text-sm text-slate-500"
      >
        该课程暂无语句。
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref } from "vue";

import type { Course } from "~/types";

const props = defineProps<{
  src: string;
  type: "video" | "audio";
  coursePackId: string;
  course: Course;
}>();

const mediaRef = ref<HTMLVideoElement | HTMLAudioElement | null>(null);
const error = ref("");
const isTranscoding = ref(false);
const currentIndex = ref(0);
const showAnswer = ref(false);
let retryTimer: ReturnType<typeof setTimeout> | null = null;

const statements = computed(() => props.course.statements || []);

function onError(e: Event) {
  checkAndRetry(e);
}

async function checkAndRetry(e: Event) {
  const el = mediaRef.value;
  if (!el) return;
  try {
    const res = await fetch(props.src, { method: "HEAD" });
    if (res.status === 503 && res.headers.get("X-Transcoding") === "in-progress") {
      isTranscoding.value = true;
      error.value = "视频正在转码中，系统将自动重试...";
      retryTimer = setTimeout(() => {
        error.value = "";
        el.load();
        el.play();
      }, 15000);
      return;
    }
  } catch (_) {}
  isTranscoding.value = false;
  const target = e?.target as HTMLMediaElement | null;
  error.value = target?.error?.message || "播放失败";
}

function retry() {
  if (retryTimer) {
    clearTimeout(retryTimer);
    retryTimer = null;
  }
  isTranscoding.value = false;
  error.value = "";
  const el = mediaRef.value;
  if (el) {
    el.load();
    el.play();
  }
}

onUnmounted(() => {
  if (retryTimer) clearTimeout(retryTimer);
});

function selectStatement(idx: number) {
  currentIndex.value = idx;
}
</script>
