<template>
  <div class="flex w-full flex-col py-6">
    <div class="mb-6 flex flex-col gap-3 border-b border-slate-200 pb-5 dark:border-slate-800 md:flex-row md:items-end md:justify-between">
      <div>
        <p class="text-sm font-bold text-emerald-600 dark:text-emerald-300">{{ $t('pages.mediaCourses') }}</p>
        <h2 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">视频 · 音频学习</h2>
        <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
          从课程包中选择带媒体资源的课程，切换播放器、答题或字幕模式学习。
        </p>
      </div>
      <NuxtLink
        to="/course-pack"
        class="inline-flex h-10 items-center rounded-md border border-slate-300 px-4 text-sm font-bold text-slate-700 transition hover:border-emerald-500 hover:text-emerald-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-emerald-400 dark:hover:text-emerald-300"
      >
        <UIcon name="i-ph-arrow-left" class="mr-1 h-4 w-4" />
        课程包总览
      </NuxtLink>
    </div>

    <template v-if="isLoading">
      <Loading />
    </template>
    <template v-else-if="errorMessage">
      <div class="rounded-md border border-rose-300 bg-rose-50 p-6 text-rose-700 dark:border-rose-900 dark:bg-rose-950/40 dark:text-rose-200">
        <div class="font-bold">加载失败</div>
        <div class="mt-2 text-sm">{{ errorMessage }}</div>
        <button class="btn btn-sm mt-3" @click="setup">重试</button>
      </div>
    </template>
    <template v-else-if="!groupedSeries.length">
      <div class="rounded-md border border-dashed border-slate-300 p-10 text-center text-sm text-slate-500 dark:border-slate-700">
        暂无带媒体资源的课程包。
      </div>
    </template>

    <template v-else>
      <nav class="mb-6 flex flex-wrap gap-2">
        <button
          v-for="series in groupedSeries"
          :key="series.key"
          type="button"
          :class="[
            'inline-flex items-center gap-2 rounded-full border px-3 py-1 text-sm font-bold transition',
            activeSeries === series.key
              ? series.accent.active
              : series.accent.chip,
          ]"
          @click="selectSeries(series.key)"
        >
          <UIcon :name="series.icon" class="h-4 w-4" />
          {{ series.label }}
          <span :class="['ml-1 rounded px-1.5 py-0.5 text-xs', activeSeries === series.key ? 'bg-white/30' : 'bg-white/70 text-slate-700 dark:bg-slate-900/70 dark:text-slate-200']">
            {{ series.packs.length }}
          </span>
        </button>
      </nav>

      <section v-if="currentSeries" class="space-y-4">
        <header class="flex flex-wrap items-end justify-between gap-2 border-b border-slate-200 pb-3 dark:border-slate-800">
          <div class="flex items-center gap-3">
            <span :class="['inline-flex h-9 w-9 items-center justify-center rounded-md', currentSeries.accent.bubble]">
              <UIcon :name="currentSeries.icon" class="h-5 w-5" />
            </span>
            <div>
              <h3 class="text-xl font-black text-slate-950 dark:text-white">{{ currentSeries.label }}</h3>
              <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">{{ currentSeries.description }}</p>
            </div>
          </div>
        </header>

        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4">
          <div
            v-for="pack in currentSeries.packs"
            :key="pack.id"
            class="group relative flex cursor-pointer flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-md dark:border-slate-800 dark:bg-slate-900"
            @click="openPack(pack)"
          >
            <div class="relative aspect-video w-full overflow-hidden bg-gradient-to-br from-emerald-500 to-emerald-800">
              <NuxtImg
                :src="pack.cover"
                :alt="pack.title"
                width="400"
                height="225"
                class="h-full w-full object-cover max-h-52"
              />
              <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
              <div class="absolute bottom-2 left-2">
                <span
                  v-if="packHasVideo(pack.id)"
                  class="rounded-full bg-white/90 px-2 py-0.5 text-xs font-bold text-slate-800"
                >
                  <UIcon name="i-ph-play" class="mr-0.5 inline h-3 w-3" />
                  视频课程
                </span>
                <span
                  v-else
                  class="rounded-full bg-white/90 px-2 py-0.5 text-xs font-bold text-slate-800"
                >
                  <UIcon name="i-ph-file-text" class="mr-0.5 inline h-3 w-3" />
                  文字课程
                </span>
              </div>
            </div>
            <div class="flex flex-1 flex-col p-4">
              <h3 class="font-bold text-slate-950 dark:text-white">
                <UIcon v-if="packHasVideo(pack.id)" name="i-ph-video" class="mr-1 inline h-4 w-4 text-emerald-500" />
                {{ pack.title }}
              </h3>
              <p class="mt-1 line-clamp-2 flex-1 text-xs leading-5 text-slate-500 dark:text-slate-400">
                {{ pack.description }}
              </p>
              <div class="mt-3 flex items-center gap-2">
                <span
                  v-if="packHasVideo(pack.id)"
                  class="rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-bold text-emerald-700 dark:bg-emerald-950 dark:text-emerald-200"
                >
                  <UIcon name="i-ph-play" class="mr-0.5 inline h-3 w-3" />
                  视频学习
                </span>
                <span
                  v-else
                  class="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-bold text-slate-700 dark:bg-slate-800 dark:text-slate-200"
                >
                  <UIcon name="i-ph-file-text" class="mr-0.5 inline h-3 w-3" />
                  文字学习
                </span>
              </div>
            </div>
          </div>
          <div
            v-if="currentSeries.packs.length === 0"
            class="col-span-full rounded-md border border-dashed border-slate-300 p-10 text-center text-sm text-slate-500 dark:border-slate-700"
          >
            这个系列下还没有课程包。
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { navigateTo } from "#app";

import { useCoursePackStore } from "~/store/coursePack";
import type { CoursePacksItem } from "~/types";

type SeriesAccent = {
  chip: string;
  active: string;
  bubble: string;
};

type MediaSeries = {
  key: string;
  label: string;
  description: string;
  icon: string;
  accent: SeriesAccent;
  packs: CoursePacksItem[];
};

const ACCENTS: Record<string, SeriesAccent> = {
  rose: {
    chip: "border-rose-200 bg-rose-50 text-rose-700 hover:bg-rose-100 dark:border-rose-900 dark:bg-rose-950/40 dark:text-rose-200",
    active: "border-rose-600 bg-rose-600 text-white shadow-sm",
    bubble: "bg-rose-100 text-rose-700 dark:bg-rose-950 dark:text-rose-300",
  },
  sky: {
    chip: "border-sky-200 bg-sky-50 text-sky-700 hover:bg-sky-100 dark:border-sky-900 dark:bg-sky-950/40 dark:text-sky-200",
    active: "border-sky-600 bg-sky-600 text-white shadow-sm",
    bubble: "bg-sky-100 text-sky-700 dark:bg-sky-950 dark:text-sky-300",
  },
  amber: {
    chip: "border-amber-200 bg-amber-50 text-amber-700 hover:bg-amber-100 dark:border-amber-900 dark:bg-amber-950/40 dark:text-amber-200",
    active: "border-amber-600 bg-amber-600 text-white shadow-sm",
    bubble: "bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-300",
  },
  emerald: {
    chip: "border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 dark:border-emerald-900 dark:bg-emerald-950/40 dark:text-emerald-200",
    active: "border-emerald-600 bg-emerald-600 text-white shadow-sm",
    bubble: "bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300",
  },
  indigo: {
    chip: "border-indigo-200 bg-indigo-50 text-indigo-700 hover:bg-indigo-100 dark:border-indigo-900 dark:bg-indigo-950/40 dark:text-indigo-200",
    active: "border-indigo-600 bg-indigo-600 text-white shadow-sm",
    bubble: "bg-indigo-100 text-indigo-700 dark:bg-indigo-950 dark:text-indigo-300",
  },
  slate: {
    chip: "border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100 dark:border-slate-700 dark:bg-slate-900/40 dark:text-slate-200",
    active: "border-slate-700 bg-slate-800 text-white shadow-sm",
    bubble: "bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-200",
  },
  violet: {
    chip: "border-violet-200 bg-violet-50 text-violet-700 hover:bg-violet-100 dark:border-violet-900 dark:bg-violet-950/40 dark:text-violet-200",
    active: "border-violet-600 bg-violet-600 text-white shadow-sm",
    bubble: "bg-violet-100 text-violet-700 dark:bg-violet-950 dark:text-violet-300",
  },
  cyan: {
    chip: "border-cyan-200 bg-cyan-50 text-cyan-700 hover:bg-cyan-100 dark:border-cyan-900 dark:bg-cyan-950/40 dark:text-cyan-200",
    active: "border-cyan-600 bg-cyan-600 text-white shadow-sm",
    bubble: "bg-cyan-100 text-cyan-700 dark:bg-cyan-950 dark:text-cyan-300",
  },
};

const coursePackStore = useCoursePackStore();
const isLoading = ref(false);
const errorMessage = ref("");

setup();

async function setup() {
  errorMessage.value = "";
  if (coursePackStore.coursePacks.length === 0) {
    isLoading.value = true;
    try {
      await coursePackStore.setupCoursePacks();
    } catch (err: any) {
      errorMessage.value = err?.message || String(err);
    } finally {
      isLoading.value = false;
    }
  }
}

function openPack(pack: { id: string }) {
  navigateTo(`/media-course/${pack.id}`);
}

const CN_NUM: Record<string, number> = { 一: 1, 二: 2, 三: 3, 四: 4, 五: 5, 六: 6, 七: 7, 八: 8, 九: 9, 十: 10 };
function extractIndex(title: string): number {
  const arabic = title.match(/(\d+)/);
  if (arabic) return parseInt(arabic[1], 10);
  const cn = title.match(/第\s*([一二三四五六七八九十])/);
  if (cn) return CN_NUM[cn[1]] ?? 999;
  return 999;
}

function packHasVideo(id: string): boolean {
  if (id === "ru-basic-pack") return false;
  if (id.startsWith("ru-basic-")) return true;
  if (id.startsWith("ru-grammar-") && id !== "ru-grammar-pack") return true;
  if (id === "ru-pronunciation-course") return true;
  if (id === "catti-prep-pack") return true;
  if (id.startsWith("catti-level")) return true;
  if (id.startsWith("east-uni-lecture-")) return true;
  if (id.startsWith("ru-audio-")) return true;
  return false;
}

function classifyMedia(pack: CoursePacksItem): string {
  const id = pack.id || "";

  // 基础入门（含视频素材：A1 目录 AVI）
  if (id.startsWith("ru-basic-") || id === "ru-zero-basic" || id === "ru-basic-pack" || id === "ru-pronunciation-course") return "foundation";
  // 语法学习（含视频素材：A1/A2 目录 AVI，含成语谚语）
  if (id.startsWith("ru-grammar-") || id === "ru-idioms" || id === "ru-proverbs") return "grammar";
  // 经典教材（东方大学俄语 1-8 册 + 精讲视频）
  if (id.startsWith("east-uni-")) return "textbook";
  // 生活口语（含音频口语课）
  if (id.startsWith("ru-spoken-") || id.startsWith("ru-audio-")) return "speaking";
  // 考试备考（含 TORFL 考级包、新闻用语、CATTI 视频课程）
  if (id.startsWith("exam-") || id === "catti-prep-pack" || id === "ru-news" || id.startsWith("torfl-") || id.startsWith("catti-level")) return "exam";
  // 行业专业
  if (["ru-it-tech","ru-legal","ru-logistics","ru-medical",
       "ru-tourism","ru-trade","ru-construction",
       "ru-oil-station","ru-baby-care"].includes(id)) return "professional";
  // 人文知识（结构化知识类课程）
  if (["ru-culture","ru-history","ru-geography",
       "ru-literature","ru-education",
       "ru-festivals"].includes(id)) return "humanities";
  // 影音娱乐
  if (id === "ru-movies" || id === "ru-songs") return "media-fun";
  // 用户导入（PDF 上传和自定义内容）
  if (id.startsWith("pdf-") || id.startsWith("vocab-pack-")) return "user-import";
  // 其他（兜底）
  return "other";
}

const groupedSeries = computed<MediaSeries[]>(() => {
  const buckets: Record<string, CoursePacksItem[]> = {
    foundation: [],
    grammar: [],
    textbook: [],
    speaking: [],
    exam: [],
    professional: [],
    humanities: [],
    "media-fun": [],
    "user-import": [],
    other: [],
  };

  for (const pack of coursePackStore.coursePacks) {
    const key = classifyMedia(pack);
    if (buckets[key]) buckets[key].push(pack);
  }

  for (const key of Object.keys(buckets)) {
    buckets[key].sort((a, b) => extractIndex(a.title) - extractIndex(b.title));
  }

  const series: MediaSeries[] = [
    {
      key: "foundation",
      label: "基础入门",
      description: "字母、发音、数字、颜色、家庭、问候等零起点课程（含 A1 视频素材）。",
      icon: "i-ph-leaf",
      accent: ACCENTS.emerald,
      packs: buckets.foundation,
    },
    {
      key: "grammar",
      label: "语法学习",
      description: "名词变格、动词变位、时态、前置词、成语谚语等语法与表达课程（含 A1/A2 视频素材）。",
      icon: "i-ph-pencil",
      accent: ACCENTS.indigo,
      packs: buckets.grammar,
    },
    {
      key: "textbook",
      label: "经典教材",
      description: "东方大学俄语（新版）学生用书 1-8 册配套课程。",
      icon: "i-ph-book-open",
      accent: ACCENTS.sky,
      packs: buckets.textbook,
    },
    {
      key: "speaking",
      label: "生活口语",
      description: "日常起居、餐厅、购物、交通、酒店、职场等情景对话。",
      icon: "i-ph-chats",
      accent: ACCENTS.amber,
      packs: buckets.speaking,
    },
    {
      key: "exam",
      label: "考试备考",
      description: "大学俄语四六级、考研、高考、CATTI 翻译资格证、新闻用语等备考素材。",
      icon: "i-ph-certificate",
      accent: ACCENTS.violet,
      packs: buckets.exam,
    },
    {
      key: "professional",
      label: "行业专业",
      description: "IT、法律、物流、医学、旅游、经贸、建筑、石油、婴幼儿护理等行业俄语。",
      icon: "i-ph-briefcase",
      accent: ACCENTS.slate,
      packs: buckets.professional,
    },
    {
      key: "humanities",
      label: "人文知识",
      description: "俄罗斯文化、历史、地理、文学、教育、节日传统等专题知识课程。",
      icon: "i-ph-globe",
      accent: ACCENTS.rose,
      packs: buckets.humanities,
    },
    {
      key: "media-fun",
      label: "影音娱乐",
      description: "经典影视台词与俄语歌曲，通过影音片段学习地道表达。",
      icon: "i-ph-music-notes",
      accent: ACCENTS.cyan,
      packs: buckets["media-fun"],
    },
    {
      key: "user-import",
      label: "用户导入",
      description: "通过 PDF 上传或其他方式导入的自定义课程。",
      icon: "i-ph-upload",
      accent: ACCENTS.slate,
      packs: buckets["user-import"],
    },
    {
      key: "other",
      label: "其他课程",
      description: "未分类的其他课程包。",
      icon: "i-ph-dots-three",
      accent: ACCENTS.amber,
      packs: buckets.other,
    },
  ];

  return series.filter((s) => s.packs.length > 0);
});

const activeSeries = ref<string>("");
let activeSeriesUserSet = false;

watch(
  groupedSeries,
  (list) => {
    if (!list.length) return;
    const current = list.find((s) => s.key === activeSeries.value);
    if (current && activeSeriesUserSet) return;
    const firstReal = list.find((s) => s.packs.length > 0);
    activeSeries.value = (firstReal ?? list[0]).key;
  },
  { immediate: true },
);

function selectSeries(key: string) {
  activeSeriesUserSet = true;
  activeSeries.value = key;
}

const currentSeries = computed(() => groupedSeries.value.find((s) => s.key === activeSeries.value));
</script>
