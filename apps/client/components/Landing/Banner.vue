<template>
  <section
    id="home"
    class="banner-hero relative -mx-5 min-h-[calc(100vh-4rem)] overflow-hidden text-slate-950 dark:text-white"
  >
    <!-- 俄语字母漂浮背景：纯 CSS/SVG，不再使用任何英文版截图 -->
    <div class="banner-bg-letters" aria-hidden="true">
      <span v-for="(item, i) in BG_LETTERS" :key="i" :style="item.style">{{ item.ch }}</span>
    </div>
    <div class="banner-bg-overlay" aria-hidden="true"></div>

    <div class="relative mx-auto flex min-h-[calc(100vh-4rem)] w-full max-w-screen-xl items-center px-5 pb-12 pt-20">
      <div class="grid w-full items-center gap-10 md:grid-cols-[minmax(0,1fr)_auto]">
        <div class="max-w-3xl">
          <div class="mb-5 inline-flex items-center gap-2 rounded-full border border-slate-300 bg-white/70 px-3 py-1 text-sm font-semibold text-slate-700 shadow-sm dark:border-slate-700 dark:bg-slate-900/70 dark:text-slate-200">
            <span class="h-2 w-2 rounded-full bg-emerald-500"></span>
            {{ $t('landing.bannerBadge') }}
          </div>

          <h1 class="text-5xl font-black leading-tight tracking-normal text-slate-950 dark:text-white md:text-6xl lg:text-7xl">
俄语学习平台
            <span class="ml-3 align-middle text-2xl font-bold text-emerald-700 dark:text-emerald-300 md:text-3xl">
              {{ $t('landing.brandName') }}
            </span>
          </h1>
          <p class="mt-5 max-w-2xl text-xl leading-9 text-slate-700 dark:text-slate-300 md:text-2xl">
            像玩游戏一样学俄语。先练 33 个西里尔字母和高频词，再进入教材句子，输入、听读、反馈一口气完成。
          </p>

          <div class="mt-8 flex flex-wrap gap-3">
            <button
              class="inline-flex h-12 items-center rounded-md bg-slate-950 px-6 font-bold text-white shadow-lg shadow-slate-300 transition hover:-translate-y-0.5 hover:bg-slate-800 dark:bg-white dark:text-slate-950 dark:shadow-none"
              type="button"
              @click="handleKeydown"
            >
              先玩一课
            </button>
            <NuxtLink
              to="/course-pack"
              class="inline-flex h-12 items-center rounded-md border border-slate-300 bg-white/80 px-6 font-bold text-slate-800 transition hover:border-emerald-500 hover:text-emerald-700 dark:border-slate-700 dark:bg-slate-900/80 dark:text-slate-100 dark:hover:border-emerald-400 dark:hover:text-emerald-300"
            >
              查看课程包
            </NuxtLink>
          </div>

          <dl class="mt-12 grid max-w-2xl grid-cols-2 gap-3 sm:grid-cols-4">
            <div
              v-for="metric in METRICS"
              :key="metric.label"
              class="border-l border-slate-300 pl-4 dark:border-slate-700"
            >
              <dt class="text-2xl font-black text-slate-950 dark:text-white">{{ metric.value }}</dt>
              <dd class="mt-1 text-sm text-slate-500 dark:text-slate-400">{{ metric.label }}</dd>
            </div>
          </dl>
        </div>

        <div class="hidden md:block">
          <div class="relative">
            <img
              src="/logo-circle.png"
              alt="俄语学习平台图标"
              width="280"
              height="280"
              class="h-56 w-56 lg:h-72 lg:w-72"
            />
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getHttp } from "~/api/http";

const emit = defineEmits(["start-lesson"]);

type StatsTotals = { packs: number; courses: number; statements: number };
type StatsResponse = { totals: StatsTotals };

const METRICS = ref([
  { value: "…", label: "课程包" },
  { value: "…", label: "节课程" },
  { value: "…", label: "练习句词" },
  { value: "A1-C2", label: "TORFL 等级" },
]);

onMounted(async () => {
  try {
    const json = await getHttp()<StatsResponse>("/admin/stats");
    const t = json.totals;
    if (t) {
      METRICS.value = [
        { value: String(t.packs), label: "课程包" },
        { value: fmtK(t.courses), label: "节课程" },
        { value: fmtK(t.statements), label: "练习句词" },
        { value: "A1-C2", label: "TORFL 等级" },
      ];
    }
  } catch { /* fallback, keep placeholder */ }
});

function fmtK(n: number): string {
  const k = n / 1000;
  return k >= 1 ? `${k.toFixed(1).replace(/\.0$/, "")}k+` : String(n);
}

// 俄语字母（含部分高频词）作为背景装饰
const CYRILLIC = [
  "А", "Б", "В", "Г", "Д", "Е", "Ж", "З", "И", "Й", "К", "Л",
  "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц", "Ч",
  "Ш", "Щ", "Ъ", "Ы", "Ь", "Э", "Ю", "Я",
  "Привет", "Спасибо", "Здравствуй", "Дом", "Книга", "Друг",
];

function rand(min: number, max: number) {
  return Math.random() * (max - min) + min;
}

const BG_LETTERS = Array.from({ length: 28 }, (_, i) => {
  const ch = CYRILLIC[i % CYRILLIC.length];
  const top = rand(2, 92).toFixed(2);
  const left = rand(1, 96).toFixed(2);
  const size = rand(2.4, 7.2).toFixed(2);
  const rot = rand(-22, 22).toFixed(1);
  const opacity = rand(0.06, 0.16).toFixed(2);
  return {
    ch,
    style: `top:${top}%;left:${left}%;font-size:${size}rem;transform:rotate(${rot}deg);opacity:${opacity};`,
  };
});

function handleKeydown() {
  emit("start-lesson");
}
</script>

<style scoped>
.banner-hero {
  background:
    radial-gradient(1200px 600px at 12% 10%, rgba(16, 185, 129, 0.16), transparent 60%),
    radial-gradient(900px 500px at 88% 90%, rgba(168, 85, 247, 0.18), transparent 60%),
    linear-gradient(180deg, #fbf7f0 0%, #f3efe7 100%);
}
:global(.dark) .banner-hero {
  background:
    radial-gradient(1200px 600px at 12% 10%, rgba(16, 185, 129, 0.20), transparent 60%),
    radial-gradient(900px 500px at 88% 90%, rgba(168, 85, 247, 0.22), transparent 60%),
    linear-gradient(180deg, #0f1218 0%, #14161d 100%);
}
.banner-bg-letters {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}
.banner-bg-letters span {
  position: absolute;
  font-family: "Times New Roman", "Noto Serif", serif;
  font-weight: 800;
  color: #0f172a;
  white-space: nowrap;
  letter-spacing: -0.02em;
  user-select: none;
}
:global(.dark) .banner-bg-letters span {
  color: #e2e8f0;
}
.banner-bg-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(251, 247, 240, 0.6) 0%, rgba(251, 247, 240, 0.15) 70%, transparent 100%);
  pointer-events: none;
}
:global(.dark) .banner-bg-overlay {
  background: linear-gradient(90deg, rgba(15, 18, 24, 0.65) 0%, rgba(15, 18, 24, 0.2) 70%, transparent 100%);
}
</style>
