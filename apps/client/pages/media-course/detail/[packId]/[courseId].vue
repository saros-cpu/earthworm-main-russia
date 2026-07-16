<template>
  <div class="flex w-full flex-col py-6">
    <template v-if="isLoading">
      <Loading />
    </template>
    <template v-else-if="errorMessage">
      <div
        class="rounded-md border border-rose-300 bg-rose-50 p-6 text-rose-700 dark:border-rose-900 dark:bg-rose-950/40 dark:text-rose-200"
      >
        <div class="font-bold">加载失败</div>
        <div class="mt-2 text-sm">{{ errorMessage }}</div>
        <button
          class="btn btn-sm mt-3"
          @click="setup"
        >
          重试
        </button>
      </div>
    </template>
    <template v-else-if="!course">
      <div
        class="rounded-md border border-dashed border-slate-300 p-10 text-center text-sm text-slate-500 dark:border-slate-700"
      >
        没有找到该课程。
      </div>
    </template>

    <template v-else>
      <div
        class="mb-4 flex items-center gap-3 border-b border-slate-200 pb-4 dark:border-slate-800"
      >
        <NuxtLink
          :to="`/media-course/${packId}`"
          class="inline-flex items-center gap-1 text-sm font-bold text-slate-500 transition hover:text-emerald-700 dark:text-slate-400 dark:hover:text-emerald-300"
        >
          <UIcon
            name="i-ph-arrow-left"
            class="h-4 w-4"
          />
          返回课程列表
        </NuxtLink>
        <span class="text-slate-300 dark:text-slate-600">|</span>
        <h2 class="text-lg font-black text-slate-950 dark:text-white">{{ course.title }}</h2>
      </div>

      <div
        v-if="streamUrl"
        class="flex-1"
      >
        <div class="mb-4 flex justify-end">
          <div
            class="flex gap-1 rounded-lg border border-slate-200 bg-slate-50 p-1 dark:border-slate-800 dark:bg-slate-900"
          >
            <button
              v-for="mode in modes"
              :key="mode.key"
              :class="[
                'inline-flex items-center gap-1.5 rounded-md px-3 py-1.5 text-xs font-bold transition',
                activeMode === mode.key
                  ? 'bg-emerald-600 text-white shadow-sm'
                  : 'text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-slate-200',
              ]"
              @click="activeMode = mode.key"
            >
              <UIcon
                :name="mode.icon"
                class="h-4 w-4"
              />
              {{ mode.label }}
            </button>
          </div>
        </div>

        <template v-if="activeMode === 'player'">
          <div
            v-if="isYouTubeUrl"
            class="w-full max-w-4xl overflow-hidden rounded-xl bg-black shadow-lg"
            style="max-height: 70vh"
          >
            <iframe
              :src="youtubeEmbedUrl"
              class="w-full"
              style="height: 70vh"
              frameborder="0"
              allow="
                accelerometer;
                autoplay;
                clipboard-write;
                encrypted-media;
                gyroscope;
                picture-in-picture;
              "
              allowfullscreen
            ></iframe>
          </div>
          <MediaVideoPlayer
            v-else
            :src="streamUrl"
            :type="mediaType"
            :title="course.title"
          />
        </template>

        <template v-if="activeMode === 'quiz'">
          <div
            v-if="isYouTubeUrl"
            class="w-full max-w-4xl overflow-hidden rounded-xl bg-black shadow-lg"
            style="max-height: 50vh"
          >
            <iframe
              :src="youtubeEmbedUrl"
              class="w-full"
              style="height: 50vh"
              frameborder="0"
              allow="
                accelerometer;
                autoplay;
                clipboard-write;
                encrypted-media;
                gyroscope;
                picture-in-picture;
              "
              allowfullscreen
            ></iframe>
          </div>
          <MediaVideoQuiz
            v-if="!isYouTubeUrl"
            :src="streamUrl"
            :type="mediaType"
            :course-pack-id="packId"
            :course="course"
          />
          <div
            v-if="isYouTubeUrl"
            class="mt-4"
          >
            <div class="mb-2 flex items-center justify-between">
              <h3 class="text-sm font-bold text-slate-950 dark:text-white">
                答题练习
                <span class="ml-2 text-xs font-normal text-slate-500"
                  >{{ currentQuizIndex + 1 }} / {{ lyricsArray.length }}</span
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
              v-if="lyricsArray.length"
              class="space-y-3"
            >
              <div
                v-for="(line, idx) in lyricsArray"
                :key="idx"
                :class="[
                  'cursor-pointer rounded-lg border p-3 text-sm transition',
                  currentQuizIndex === idx
                    ? 'border-emerald-400 bg-emerald-50 dark:border-emerald-600 dark:bg-emerald-950/40'
                    : 'border-slate-200 bg-white hover:border-slate-300 dark:border-slate-800 dark:bg-slate-900 dark:hover:border-slate-700',
                ]"
                @click="currentQuizIndex = Number(idx)"
              >
                <div class="font-bold text-slate-950 dark:text-white">{{ line.chinese }}</div>
                <div
                  v-if="showAnswer || currentQuizIndex === idx"
                  class="mt-1 text-emerald-700 dark:text-emerald-300"
                >
                  {{ line.russian }}
                </div>
              </div>
            </div>
            <div
              v-else
              class="flex items-center justify-center rounded-md border border-dashed p-6 text-sm text-slate-500"
            >
              暂无歌词。
            </div>
          </div>
        </template>

        <template v-if="activeMode === 'lrc'">
          <div
            v-if="isYouTubeUrl"
            class="w-full max-w-4xl overflow-hidden rounded-xl bg-black shadow-lg"
            style="max-height: 50vh"
          >
            <iframe
              :id="youtubeLrcIframeId"
              :src="youtubeLrcEmbedUrl"
              class="w-full"
              style="height: 50vh"
              frameborder="0"
              allow="
                accelerometer;
                autoplay;
                clipboard-write;
                encrypted-media;
                gyroscope;
                picture-in-picture;
              "
              allowfullscreen
            ></iframe>
          </div>
          <MediaAudioLrcPlayer
            v-if="!isYouTubeUrl"
            :src="streamUrl"
            :title="course.title"
            :lrc-lines="lrcLines"
          />
          <div
            v-if="isYouTubeUrl"
            class="mt-4"
          >
            <div class="w-full max-w-4xl">
              <div
                ref="youtubeLrcContainer"
                class="rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
                style="max-height: 40vh; overflow-y: auto"
              >
                <div
                  v-if="lrcLines.length"
                  class="space-y-3 text-center"
                >
                  <div
                    v-for="(line, idx) in lrcLines"
                    :key="idx"
                    :data-active="currentLrcIndex === idx ? 'true' : undefined"
                    :class="[
                      'rounded px-4 py-2 text-sm leading-6 transition',
                      currentLrcIndex === idx
                        ? 'scale-105 bg-emerald-100 font-bold text-emerald-800 dark:bg-emerald-950 dark:text-emerald-200'
                        : 'text-slate-700 dark:text-slate-300',
                    ]"
                  >
                    <span class="block font-medium">{{ line.russian }}</span>
                    <span class="mt-0.5 block text-xs text-slate-400 dark:text-slate-500">{{
                      line.translation
                    }}</span>
                  </div>
                </div>
                <div
                  v-else
                  class="flex h-48 items-center justify-center text-sm text-slate-500"
                >
                  暂无歌词字幕
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>

      <div
        v-else
        class="flex-1"
      >
        <div
          v-if="course.statements?.length"
          class="space-y-4"
        >
          <div
            v-for="(s, i) in course.statements"
            :key="i"
            class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900"
          >
            <div class="flex items-start justify-between gap-3">
              <div class="flex-1">
                <div class="text-lg font-bold text-slate-950 dark:text-white">{{ s.english }}</div>
                <div class="mt-2 text-sm text-amber-600 dark:text-amber-400">{{ s.chinese }}</div>
                <div
                  v-if="s.soundmark"
                  class="mt-1 text-xs italic text-slate-400"
                >
                  {{ s.soundmark }}
                </div>
              </div>
              <button
                class="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-emerald-100 text-emerald-600 transition hover:bg-emerald-200 dark:bg-emerald-950 dark:text-emerald-300 dark:hover:bg-emerald-900"
                title="播放发音"
                @click="playRussianText(s.english)"
              >
                <UIcon
                  name="i-ph-speaker-high"
                  class="h-5 w-5"
                />
              </button>
            </div>
          </div>
        </div>
        <div
          v-else
          class="flex flex-col items-center justify-center rounded-md border border-dashed border-slate-300 p-10 text-sm text-slate-500 dark:border-slate-700"
        >
          <UIcon
            name="i-ph-text-align-left"
            class="mb-2 h-8 w-8 text-slate-300 dark:text-slate-600"
          />
          暂无课程内容
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onUnmounted, ref, watch } from "vue";
import { useRoute } from "vue-router";

import type { Course } from "~/types";
import { fetchCourse } from "~/api/course";
import { playRussianText } from "~/composables/main/englishSound";
import { getMediaPlaybackUrl, getYouTubeEmbedUrl } from "~/utils/media";

declare global {
  interface Window {
    YT?: any;
    onYouTubeIframeAPIReady?: () => void;
  }
}

const route = useRoute();
const packId = route.params.packId as string;
const courseId = route.params.courseId as string;
const isLoading = ref(false);
const errorMessage = ref("");
const activeMode = ref<string>("player");
const courseRef = ref<Course>();
const currentQuizIndex = ref(0);
const showAnswer = ref(false);
const youtubeLrcContainer = ref<HTMLDivElement | null>(null);
const lrcDuration = ref(0);
const youtubeLrcPlayer = ref<any>(null);
let youtubeApiReady: Promise<void> | null = null;

const modes = [
  { key: "player", label: "播放器", icon: "i-ph-play-circle" },
  { key: "quiz", label: "播放 + 答题", icon: "i-ph-question" },
  { key: "lrc", label: "音频 + 字幕", icon: "i-ph-subtitles" },
];

const course = computed(() => courseRef.value);

const mediaType = computed(() => {
  const v = course.value?.video || "";
  if (
    v.endsWith(".mp3") ||
    v.endsWith(".wma") ||
    v.endsWith(".wav") ||
    v.endsWith(".ogg") ||
    v.endsWith(".m4a")
  )
    return "audio";
  return "video";
});

const youtubeEmbedUrl = computed(() => {
  return getYouTubeEmbedUrl(course.value?.video) || "";
});

const youtubeLrcIframeId = computed(() => {
  return `youtube-lrc-${String(courseId).replace(/[^A-Za-z0-9_-]/g, "-")}`;
});

const youtubeLrcEmbedUrl = computed(() => {
  const origin = typeof window === "undefined" ? undefined : window.location.origin;
  return (
    getYouTubeEmbedUrl(course.value?.video, {
      enableJsApi: true,
      origin,
    }) || ""
  );
});

const isYouTubeUrl = computed(() => Boolean(youtubeEmbedUrl.value));

const streamUrl = computed(() => {
  return getMediaPlaybackUrl(course.value?.video);
});

const lyricsArray = computed(() => {
  const raw = course.value?.lyrics;
  if (raw) {
    try {
      return JSON.parse(raw);
    } catch {
      /* fall through */
    }
  }
  // fallback: generate lyrics from course statements
  const stmts = course.value?.statements;
  if (stmts?.length) {
    return stmts.map((s: any) => ({ russian: s.english, chinese: s.chinese }));
  }
  return [];
});

function estimatedSongDuration(lineCount: number): number {
  return Math.max(120, lineCount * 8);
}

const lrcLines = computed(() => {
  const lines = lyricsArray.value;
  if (!lines.length) return [];
  const hasExplicitTime = lines.some(
    (line: any) => typeof line.time === "number" && Number.isFinite(line.time),
  );
  const duration = lrcDuration.value > 0 ? lrcDuration.value : estimatedSongDuration(lines.length);
  const interval = duration / lines.length;
  return lines.map((s: any, i: number) => ({
    time: hasExplicitTime && typeof s.time === "number" ? s.time : i * interval,
    russian: s.russian,
    translation: s.chinese,
  }));
});

const lrcTime = ref(0);
let lrcTimer: ReturnType<typeof setInterval> | null = null;
let quizTimer: ReturnType<typeof setInterval> | null = null;

function loadYouTubeApi() {
  if (typeof window === "undefined") return Promise.resolve();
  if (window.YT?.Player) return Promise.resolve();
  if (youtubeApiReady) return youtubeApiReady;

  youtubeApiReady = new Promise((resolve) => {
    const previousReady = window.onYouTubeIframeAPIReady;
    window.onYouTubeIframeAPIReady = () => {
      previousReady?.();
      resolve();
    };
    if (!document.querySelector('script[src="https://www.youtube.com/iframe_api"]')) {
      const script = document.createElement("script");
      script.src = "https://www.youtube.com/iframe_api";
      script.async = true;
      document.head.appendChild(script);
    }
  });
  return youtubeApiReady;
}

function updateLrcFromYouTube() {
  const player = youtubeLrcPlayer.value;
  if (!player) return;
  try {
    const currentTime = player.getCurrentTime?.();
    if (typeof currentTime === "number" && Number.isFinite(currentTime)) {
      lrcTime.value = currentTime;
    }
    const duration = player.getDuration?.();
    if (typeof duration === "number" && Number.isFinite(duration) && duration > 0) {
      lrcDuration.value = duration;
    }
  } catch {
    // The iframe can be destroyed while switching modes; the next init will recreate it.
  }
}

function updateQuizFromYouTube() {
  const player = youtubeLrcPlayer.value;
  if (!player) return;
  try {
    const currentTime = player.getCurrentTime?.();
    if (typeof currentTime === "number" && Number.isFinite(currentTime)) {
      const totalDuration = Math.max(lrcDuration.value, 1);
      const avgLen = totalDuration / Math.max(lyricsArray.value.length, 1);
      const idx = Math.min(Math.floor(currentTime / avgLen), lyricsArray.value.length - 1);
      if (idx >= 0) currentQuizIndex.value = idx;
    }
  } catch {}
}

function startLrcTimer() {
  stopLrcTimer();
  updateLrcFromYouTube();
  lrcTimer = setInterval(updateLrcFromYouTube, 500);
}
function stopLrcTimer() {
  if (lrcTimer !== null) {
    clearInterval(lrcTimer);
    lrcTimer = null;
  }
}

function startQuizTimer() {
  stopQuizTimer();
  updateQuizFromYouTube();
  quizTimer = setInterval(updateQuizFromYouTube, 500);
}
function stopQuizTimer() {
  if (quizTimer !== null) {
    clearInterval(quizTimer);
    quizTimer = null;
  }
}

function destroyYoutubeLrcPlayer() {
  stopLrcTimer();
  stopQuizTimer();
  try {
    youtubeLrcPlayer.value?.destroy?.();
  } catch {
    // Ignore iframe teardown races while switching modes.
  }
  youtubeLrcPlayer.value = null;
}

async function initYoutubeLrcPlayer() {
  if (!isYouTubeUrl.value || activeMode.value !== "lrc") return;
  await nextTick();
  await loadYouTubeApi();
  if (!window.YT?.Player || youtubeLrcPlayer.value) return;

  youtubeLrcPlayer.value = new window.YT.Player(youtubeLrcIframeId.value, {
    events: {
      onReady(event: any) {
        const duration = event.target.getDuration?.();
        if (typeof duration === "number" && duration > 0) {
          lrcDuration.value = duration;
        }
        startLrcTimer();
      },
      onStateChange(event: any) {
        updateLrcFromYouTube();
        if (event.data === window.YT?.PlayerState?.PLAYING) {
          startLrcTimer();
        } else if (
          event.data === window.YT?.PlayerState?.PAUSED ||
          event.data === window.YT?.PlayerState?.ENDED
        ) {
          stopLrcTimer();
        }
      },
    },
  });
}

const currentLrcIndex = computed(() => {
  const t = lrcTime.value;
  const lines = lrcLines.value;
  if (!lines.length) return 0;
  let idx = lines.length - 1;
  for (let i = 0; i < lines.length; i++) {
    if (t < lines[i].time) {
      idx = i - 1;
      break;
    }
  }
  return Math.max(0, idx);
});

watch(activeMode, (val) => {
  if (val === "lrc" && isYouTubeUrl.value) {
    initYoutubeLrcPlayer();
  } else if (isYouTubeUrl.value) {
    destroyYoutubeLrcPlayer();
  } else if (val === "lrc") {
    lrcTime.value = 0;
  } else {
    stopLrcTimer();
  }
});

watch(currentLrcIndex, () => {
  nextTick(() => {
    const active = youtubeLrcContainer.value?.querySelector('[data-active="true"]');
    active?.scrollIntoView({ block: "center", behavior: "smooth" });
  });
});

watch(
  () => course.value?.id,
  () => {
    lrcTime.value = 0;
    lrcDuration.value = 0;
    destroyYoutubeLrcPlayer();
    if (activeMode.value === "lrc") {
      initYoutubeLrcPlayer();
    }
  },
);

onUnmounted(() => {
  destroyYoutubeLrcPlayer();
});

setup();

async function setup() {
  errorMessage.value = "";
  isLoading.value = true;
  try {
    courseRef.value = await fetchCourse(packId, courseId);
  } catch (err: any) {
    errorMessage.value = err?.message || String(err);
  } finally {
    isLoading.value = false;
  }
}
</script>
