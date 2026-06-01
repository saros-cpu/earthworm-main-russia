<template>
  <div class="flex w-full flex-col py-6">
    <div class="mb-6 flex flex-col gap-3 border-b border-slate-200 pb-5 dark:border-slate-800 md:flex-row md:items-end md:justify-between">
      <div>
        <p class="text-sm font-bold text-emerald-600 dark:text-emerald-300">{{ $t('pages.courseLibrary') }}</p>
        <h2 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">{{ $t('coursePack.title') }}</h2>
        <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
          {{ $t('coursePack.description') }}
        </p>
      </div>
      <NuxtLink
        to="/"
        class="inline-flex h-10 items-center rounded-md border border-slate-300 px-4 text-sm font-bold text-slate-700 transition hover:border-emerald-500 hover:text-emerald-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-emerald-400 dark:hover:text-emerald-300"
      >
        {{ $t('coursePack.backToHome') }}
      </NuxtLink>
    </div>

    <template v-if="isLoading">
      <Loading></Loading>
    </template>
    <template v-else-if="errorMessage">
        <div class="rounded-md border border-rose-300 bg-rose-50 p-6 text-rose-700 dark:border-rose-900 dark:bg-rose-950/40 dark:text-rose-200">
          <div class="font-bold">{{ $t('common.loadError') }}</div>
          <div class="mt-2 text-sm">{{ errorMessage }}</div>
          <button class="btn btn-sm mt-3" @click="setup">{{ $t('common.retry') }}</button>
      </div>
    </template>
    <template v-else-if="!coursePackStore.coursePacks.length">
      <div class="rounded-md border border-dashed border-slate-300 p-10 text-center text-sm text-slate-500 dark:border-slate-700">
        {{ $t('common.noPacksAtAll') }}
      </div>
    </template>

    <template v-else>
      <!-- Tab 切换 -->
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
            {{ series.kind === 'placeholder' ? TORFL_LEVELS.length : series.packs.length }}
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

        <!-- 普通系列 -->
        <div
          v-if="currentSeries.kind === 'packs'"
          class="grid grid-cols-1 gap-4 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4"
        >
          <CoursePackCard
            v-for="coursePack in currentSeries.packs"
            :key="coursePack.id"
            :coursePack="{
              id: coursePack.id,
              title: coursePack.title,
              description: coursePack.description,
              cover: coursePack.cover,
              isFree: coursePack.isFree,
            }"
            @cardClick="handleGoToCoursePack"
          />
          <div
            v-if="currentSeries.packs.length === 0"
            class="col-span-full rounded-md border border-dashed border-slate-300 p-10 text-center text-sm text-slate-500 dark:border-slate-700"
          >
            {{ $t('common.noPacks') }}
          </div>
        </div>

        <!-- TORFL 占位 -->
        <div
          v-else-if="currentSeries.kind === 'placeholder'"
          class="grid grid-cols-1 gap-4 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-3"
        >
          <article
            v-for="lvl in TORFL_LEVELS"
            :key="lvl.code"
            class="flex min-h-[200px] flex-col rounded-md border border-dashed border-slate-300 bg-slate-50/60 p-5 dark:border-slate-700 dark:bg-slate-900/40"
          >
            <div class="flex items-center justify-between">
              <span :class="['rounded px-2 py-1 text-sm font-black', lvl.chip]">{{ lvl.code }}</span>
              <span class="text-xs text-slate-400">{{ $t('coursePack.targetVocab', { target: lvl.target }) }}</span>
            </div>
            <div class="mt-3 text-lg font-black text-slate-950 dark:text-white">{{ lvl.label }}</div>
            <p class="mt-2 line-clamp-3 flex-1 text-sm leading-6 text-slate-500 dark:text-slate-400">
              {{ lvl.desc }}
            </p>
            <div class="mt-3 flex items-center gap-1 text-xs font-bold text-slate-500 dark:text-slate-400">
              <UIcon name="i-ph-hourglass-medium" class="h-4 w-4" />
              {{ $t('coursePack.comingSoon') }}
            </div>
          </article>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";

import type { CoursePack, CoursePacksItem } from "~/types";
import CoursePackCard from "~/components/courses/CoursePackCard.vue";
import { useNavigation } from "~/composables/useNavigation";
import { useCoursePackStore } from "~/store/coursePack";
import { isAuthenticated } from "~/services/auth";

const { t } = useI18n();

const coursePackStore = useCoursePackStore();
const { gotoCourseList } = useNavigation();
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
      console.error("[course-pack] fetch list failed", err);
      errorMessage.value = err?.message || String(err) || "未知错误";
    } finally {
      isLoading.value = false;
    }
  }
}

function handleGoToCoursePack(coursePack: CoursePack) {
  if (coursePack.isFree) {
    gotoCourseList(coursePack.id);
  } else {
  }
}

// ----- 系列定义 -----
type SeriesAccent = {
  chip: string;
  active: string;
  bubble: string;
};

type PackSeries = {
  key: string;
  label: string;
  description: string;
  icon: string;
  accent: SeriesAccent;
  kind: "packs";
  packs: CoursePacksItem[];
};

type PlaceholderSeries = {
  key: string;
  label: string;
  description: string;
  icon: string;
  accent: SeriesAccent;
  kind: "placeholder";
  packs: never[];
};

type Series = PackSeries | PlaceholderSeries;

const ACCENTS: Record<string, SeriesAccent> = {
  emerald: {
    chip: "border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 dark:border-emerald-900 dark:bg-emerald-950/40 dark:text-emerald-200",
    active: "border-emerald-600 bg-emerald-600 text-white shadow-sm",
    bubble: "bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300",
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
  fuchsia: {
    chip: "border-fuchsia-200 bg-fuchsia-50 text-fuchsia-700 hover:bg-fuchsia-100 dark:border-fuchsia-900 dark:bg-fuchsia-950/40 dark:text-fuchsia-200",
    active: "border-fuchsia-600 bg-fuchsia-600 text-white shadow-sm",
    bubble: "bg-fuchsia-100 text-fuchsia-700 dark:bg-fuchsia-950 dark:text-fuchsia-300",
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
};

const TORFL_LEVELS = computed(() => {
  const codes = ["A1", "A2", "B1", "B2", "C1", "C2"];
  const chips: Record<string, string> = {
    A1: "bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-200",
    A2: "bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-200",
    B1: "bg-sky-100 text-sky-700 dark:bg-sky-950 dark:text-sky-200",
    B2: "bg-sky-100 text-sky-700 dark:bg-sky-950 dark:text-sky-200",
    C1: "bg-fuchsia-100 text-fuchsia-700 dark:bg-fuchsia-950 dark:text-fuchsia-200",
    C2: "bg-fuchsia-100 text-fuchsia-700 dark:bg-fuchsia-950 dark:text-fuchsia-200",
  };
  return codes.map(code => ({
    code,
    chip: chips[code],
    label: t(`coursePack.levels.${code}.label`),
    target: t(`coursePack.levels.${code}.target`),
    desc: t(`coursePack.levels.${code}.desc`),
  }));
});

// ----- 智能分类（按难度从低到高）-----
function classify(pack: CoursePacksItem): string {
  const title = pack.title || "";
  const id = pack.id || "";

  // 考试备考：TORFL + CATTI + 专四专八/考研/高考
  if (id.startsWith("torfl-") || id.startsWith("catti-") || id.startsWith("exam-")) return "exam";
  // 行业俄语 · 专业应用
  if (id === "ru-baby-care" || id === "ru-oil-station" ||
      id === "ru-construction" || id === "ru-logistics" ||
      id === "ru-it-tech" || id === "ru-legal" ||
      id === "ru-medical" || id === "ru-trade" ||
      id === "ru-tourism" || id === "ru-education") return "fluent";
  // 零基础·入门
  if (id.startsWith("ru-basic-") || title.includes("入门")) return "basic";
  // 词汇语法 · 基础强化
  if (id.startsWith("vocab-pack-") || id.startsWith("ru-grammar-") || title.includes("单词") || title.includes("单词汇总") || title.includes("词汇") || title.includes("语法")) return "grammar";
  // 教材同步 · 课本精讲
  if (id.startsWith("east-uni-") || title.includes("大学俄语")) return "textbook";
  if (title.includes("走遍") || title.includes("自学辅导")) return "textbook";
  // 口语会话 · 情景实战
  if (id.startsWith("ru-spoken-") || title.includes("口语")) return "speaking";

  return "other";
}

// 从 torfl-X-... id 中解析等级，用于排序
function torflLevelIndex(id: string): number {
  const match = id.match(/^torfl-([a-c])([12])(?:-|$)/i);
  if (!match) return 999;
  const letter = match[1].toLowerCase();
  const num = parseInt(match[2], 10);
  return (letter.charCodeAt(0) - "a".charCodeAt(0)) * 2 + (num - 1); // a1=0, a2=1, b1=2, b2=3, c1=4, c2=5
}

// 从标题中尽量解析出排序用的数字（"1" / "第一册" / "第 2 课"）
const CN_NUM: Record<string, number> = { 一: 1, 二: 2, 三: 3, 四: 4, 五: 5, 六: 6, 七: 7, 八: 8, 九: 9, 十: 10 };
function extractIndex(title: string): number {
  const arabic = title.match(/(\d+)/);
  if (arabic) return parseInt(arabic[1], 10);
  const cn = title.match(/第\s*([一二三四五六七八九十])/);
  if (cn) return CN_NUM[cn[1]] ?? 999;
  return 999;
}

const groupedSeries = computed<Series[]>(() => {
  const buckets: Record<string, CoursePacksItem[]> = {
    basic: [],
    grammar: [],
    textbook: [],
    speaking: [],
    exam: [],
    fluent: [],
    other: [],
  };

  for (const pack of coursePackStore.coursePacks) {
    const key = classify(pack);
    (buckets[key] ?? buckets.other).push(pack);
  }

  for (const key of Object.keys(buckets)) {
    if (key === "exam") {
      buckets[key].sort((a, b) => torflLevelIndex(a.id) - torflLevelIndex(b.id));
    } else {
      buckets[key].sort((a, b) => extractIndex(a.title) - extractIndex(b.title));
    }
  }

  const series: Series[] = [
    {
      key: "basic",
      label: "零基础 · 入门",
      description: "从俄语字母到日常基础句型，零起点起步。",
      icon: "i-ph-leaf",
      accent: ACCENTS.emerald,
      kind: "packs",
      packs: buckets.basic,
    },
    {
      key: "grammar",
      label: "词汇语法 · 基础强化",
      description: "核心词汇与语法规则，打好俄语基本功。",
      icon: "i-ph-pencil",
      accent: ACCENTS.amber,
      kind: "packs",
      packs: buckets.grammar,
    },
    {
      key: "textbook",
      label: "教材同步 · 课本精讲",
      description: "走遍俄罗斯、大学俄语(东方)等经典教材的词汇与课文练习。",
      icon: "i-ph-book-open",
      accent: ACCENTS.indigo,
      kind: "packs",
      packs: buckets.textbook,
    },
    {
      key: "speaking",
      label: "口语会话 · 情景实战",
      description: "日常对话、情景口语，学会地道表达。",
      icon: "i-ph-chats",
      accent: ACCENTS.sky,
      kind: "packs",
      packs: buckets.speaking,
    },
    {
      key: "exam",
      label: "俄语考级 · TORFL 与 CATTI",
      description: "TORFL (A1-C2) 等级备考 + CATTI 翻译资格证，系统备考练习。",
      icon: "i-ph-certificate",
      accent: ACCENTS.fuchsia,
      kind: "packs",
      packs: buckets.exam,
    },
    {
      key: "fluent",
      label: "行业俄语 · 专业应用",
      description: "婴幼儿护理、加油站·石油·工程等专业场景实用俄语。",
      icon: "i-ph-globe",
      accent: ACCENTS.indigo,
      kind: "packs",
      packs: buckets.fluent,
    },
    {
      key: "other",
      label: "其他",
      description: "尚未归类的课程包。",
      icon: "i-ph-folder-simple",
      accent: ACCENTS.slate,
      kind: "packs",
      packs: buckets.other,
    },
  ];

  // 未登录用户只能看到零基础
  if (!isAuthenticated()) {
    return series.filter((s) => s.key === "basic" && s.packs.length > 0);
  }
  // 过滤掉空的真实系列
  return series.filter((s) => s.kind === "placeholder" || s.packs.length > 0);
});

const activeSeries = ref<string>("");
let activeSeriesUserSet = false;

// 首次加载或数据变更时确保 activeSeries 落到一个有数据的真实系列
watch(
  groupedSeries,
  (list) => {
    if (!list.length) return;
    const current = list.find((s) => s.key === activeSeries.value);
    if (current && (activeSeriesUserSet || current.kind === "packs")) {
      return; // 用户已主动选择，或当前已是有效真实系列
    }
    // 优先选第一个非占位系列；都没有就退回第一个
    const firstRealSeries = list.find((s) => s.kind === "packs" && s.packs.length > 0);
    activeSeries.value = (firstRealSeries ?? list[0]).key;
  },
  { immediate: true },
);

function selectSeries(key: string) {
  activeSeriesUserSet = true;
  activeSeries.value = key;
}

const currentSeries = computed(() => groupedSeries.value.find((s) => s.key === activeSeries.value));
</script>

<style></style>
