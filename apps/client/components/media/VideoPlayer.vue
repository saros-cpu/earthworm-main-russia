<template>
  <div class="flex flex-col items-center gap-6">
    <div
      v-if="type === 'video'"
      class="w-full max-w-4xl overflow-hidden rounded-xl bg-black shadow-lg"
    >
      <video
        ref="videoRef"
        controls
        class="w-full"
        style="max-height: 70vh"
        @error="onError"
      >
        <source
          v-if="mediaSrc"
          :src="mediaSrc"
          type="video/mp4"
        />
        您的浏览器不支持视频播放。
      </video>
    </div>
    <div
      v-else
      class="w-full max-w-xl"
    >
      <div
        class="rounded-xl bg-gradient-to-br from-emerald-500 to-emerald-800 p-8 text-center text-white shadow-lg"
      >
        <UIcon
          name="i-ph-headphones"
          class="mb-4 h-16 w-16"
        />
        <audio
          ref="audioRef"
          :src="mediaSrc"
          controls
          class="w-full"
          @error="onError"
        ></audio>
      </div>
    </div>

    <div
      v-if="error"
      :class="[
        'w-full max-w-4xl rounded-md border p-4 text-sm',
        isTranscoding
          ? 'border-amber-300 bg-amber-50 text-amber-700 dark:border-amber-900 dark:bg-amber-950/40 dark:text-amber-200'
          : 'border-rose-300 bg-rose-50 text-rose-700 dark:border-rose-900 dark:bg-rose-950/40 dark:text-rose-200',
      ]"
    >
      <span
        v-if="isTranscoding"
        class="font-bold"
        >正在准备：</span
      >
      <span
        v-else
        class="font-bold"
        >播放失败：</span
      >
      {{ error }}
      <button
        v-if="!isTranscoding"
        class="btn btn-sm ml-3"
        @click="retry"
      >
        重试
      </button>
    </div>

    <div
      class="w-full max-w-4xl rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
    >
      <h3 class="font-bold text-slate-950 dark:text-white">{{ title }}</h3>
      <p class="mt-2 text-xs leading-6 text-slate-500 dark:text-slate-400">
        使用 A 模式纯播放器，支持倍速播放和全屏。视频支持 HTTP Range
        请求拖动进度条，音频支持后台播放。
      </p>
      <div class="mt-3 flex flex-wrap gap-2">
        <button
          v-for="rate in [0.5, 0.75, 1, 1.25, 1.5, 2]"
          :key="rate"
          :class="[
            'rounded px-2 py-1 text-xs font-bold transition',
            playbackRate === rate
              ? 'bg-emerald-600 text-white'
              : 'bg-slate-100 text-slate-600 hover:bg-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700',
          ]"
          @click="setPlaybackRate(rate)"
        >
          {{ rate }}x
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from "vue";

const props = defineProps<{
  src: string;
  type: "video" | "audio";
  title: string;
}>();

const videoRef = ref<HTMLVideoElement | null>(null);
const audioRef = ref<HTMLAudioElement | null>(null);
const error = ref("");
const isTranscoding = ref(false);
const mediaSrc = ref("");
const playbackRate = ref(1);
let retryTimer: ReturnType<typeof setTimeout> | null = null;
let probeId = 0;

function mediaElement() {
  return videoRef.value || audioRef.value;
}

function clearRetryTimer() {
  if (retryTimer) {
    clearTimeout(retryTimer);
    retryTimer = null;
  }
}

function retryAfterMs(res: Response): number {
  const retryAfter = Number(res.headers.get("Retry-After"));
  return Number.isFinite(retryAfter) && retryAfter > 0 ? retryAfter * 1000 : 15000;
}

async function prepareMedia(autoplay = false) {
  const currentProbe = ++probeId;
  clearRetryTimer();
  mediaSrc.value = "";
  error.value = "";
  isTranscoding.value = false;

  try {
    const res = await fetch(props.src, {
      method: "HEAD",
      credentials: "include",
    });
    if (currentProbe !== probeId) return;

    if (res.ok) {
      mediaSrc.value = props.src;
      await nextTick();
      const el = mediaElement();
      if (el) {
        el.load();
        el.playbackRate = playbackRate.value;
        if (autoplay) {
          await el.play().catch(() => {});
        }
      }
      return;
    }

    if (res.status === 503 && res.headers.get("X-Transcoding")) {
      isTranscoding.value = true;
      const state = res.headers.get("X-Transcoding");
      error.value =
        state === "queue-full"
          ? "转码任务较多，系统会稍后自动重试。"
          : "视频首次打开需要转码，系统会自动重试。";
      retryTimer = setTimeout(() => prepareMedia(autoplay), retryAfterMs(res));
      return;
    }

    isTranscoding.value = false;
    error.value =
      res.status === 404
        ? "找不到视频文件，请检查课程包里的视频路径和本地媒体目录。"
        : `媒体服务返回 ${res.status}，请稍后重试。`;
  } catch (_) {
    if (currentProbe !== probeId) return;
    isTranscoding.value = false;
    error.value = "无法连接媒体服务，请确认后端服务仍在运行。";
  }
}

function onError(e: Event) {
  checkAndRetry(e);
}

async function checkAndRetry(e: Event) {
  try {
    const res = await fetch(props.src, {
      method: "HEAD",
      credentials: "include",
    });
    if (res.status === 503 && res.headers.get("X-Transcoding")) {
      isTranscoding.value = true;
      error.value = "视频首次打开需要转码，系统会自动重试。";
      retryTimer = setTimeout(() => prepareMedia(true), retryAfterMs(res));
      return;
    }
  } catch (_) {}
  isTranscoding.value = false;
  const target = e?.target as HTMLMediaElement | null;
  error.value = target?.error?.message || "未知错误，可能是格式不支持或路径不存在。";
}

function retry() {
  prepareMedia(true);
}

onMounted(() => prepareMedia(false));

watch(
  () => props.src,
  () => prepareMedia(false),
);

onUnmounted(() => {
  clearRetryTimer();
});

function setPlaybackRate(rate: number) {
  playbackRate.value = rate;
  const el = mediaElement();
  if (el) el.playbackRate = rate;
}
</script>
