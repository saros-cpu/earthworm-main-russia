<template>
  <div class="w-full py-6">
    <section class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-sm font-bold text-emerald-600 dark:text-emerald-300">{{ $t('pages.review') }}</p>
          <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">复习</h1>
          <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
            基于间隔重复算法，在最佳时间安排复习，巩固记忆。
          </p>
        </div>
        <div class="rounded-md bg-slate-50 px-4 py-3 text-sm dark:bg-slate-950">
          <span class="font-black text-slate-950 dark:text-white">{{ dueCount }}</span>
          <span class="ml-1 text-slate-500 dark:text-slate-400">条待复习</span>
        </div>
      </div>
      <div class="mt-4 flex gap-4">
        <div v-for="s in reviewStats" :key="s.label" class="flex-1 rounded-md bg-slate-50 p-3 text-center dark:bg-slate-800">
          <div class="text-lg font-black text-slate-950 dark:text-white">{{ s.value }}</div>
          <div class="text-[10px] font-bold text-slate-400 uppercase tracking-wide">{{ s.label }}</div>
        </div>
      </div>
    </section>

    <section v-if="currentReview" class="mb-5">
      <div class="rounded-md border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div class="mb-6 text-center">
          <div class="text-2xl font-black text-slate-950 dark:text-white md:text-3xl">
            {{ currentStatement?.chinese || '加载中...' }}
          </div>
          <div class="mt-2 text-sm text-slate-400">输入对应的俄语句子</div>
        </div>

        <div class="relative mx-auto max-w-3xl">
          <input ref="inputRef" v-model="userAnswer" lang="ru" type="text" placeholder="在此输入俄语..."
            class="h-12 w-full rounded-md border border-slate-200 bg-white px-4 text-lg outline-none transition focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-950"
            @keydown.enter="checkAnswer" :disabled="checked" />
        </div>

        <div v-if="checked" class="mt-4 text-center">
          <div :class="isCorrect ? 'text-emerald-600' : 'text-red-500'" class="mb-3 text-lg font-bold">
            {{ isCorrect ? '✓ 正确!' : '✗ 不正确' }}
          </div>
          <div v-if="!isCorrect" class="mb-3 text-slate-600 dark:text-slate-300">
            正确答案：<span class="font-bold">{{ currentStatement?.english }}</span>
          </div>
          <div class="flex justify-center gap-3">
            <button v-for="q in qualityOptions" :key="q.value"
              class="rounded-md px-4 py-2 text-sm font-bold transition"
              :class="selectedQuality === q.value ? q.activeClass : q.class"
              @click="submitReview(q.value)">
              {{ q.label }}
            </button>
          </div>
          <div class="mt-3 text-xs text-slate-400">
            你记得怎么样？选择对应分数，系统会自动安排下次复习时间。
          </div>
        </div>
      </div>
    </section>

    <section v-else class="rounded-md border border-dashed border-slate-300 bg-white p-10 text-center text-slate-500 dark:border-slate-700 dark:bg-slate-900">
      <div class="text-4xl mb-3">🎉</div>
      <div class="font-bold text-lg mb-1">暂无待复习内容</div>
      <div class="text-sm">继续学习新课程，系统会自动安排复习计划。</div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";

import { fetchDueReviews, fetchRecordReview, fetchScheduleReview, fetchTotalStats } from "~/api/learning";
import { fetchCourse } from "~/api/course";
import type { CourseApiResponse, StatementApiResponse } from "~/api/course";

interface ReviewItem {
  id: string;
  statementId: string;
  coursePackId: string;
  courseId: string;
  easiness: number;
  interval: number;
  repetitions: number;
  nextReviewAt: string;
}

const inputRef = ref<HTMLInputElement>();
const userAnswer = ref("");
const checked = ref(false);
const isCorrect = ref(false);
const selectedQuality = ref<number | null>(null);
const dueItems = ref<ReviewItem[]>([]);
const currentIndex = ref(0);
const dueCount = ref(0);
const currentStatement = ref<StatementApiResponse | null>(null);

const currentReview = ref<ReviewItem | null>(null);
const currentStreak = ref(0);
const totalReviewed = ref(0);

const reviewStats = computed(() => [
  { label: "今日待复习", value: dueCount.value },
  { label: "当前连续", value: currentStreak.value + "天" },
  { label: "累计复习", value: totalReviewed.value },
]);

const qualityOptions = [
  { value: 1, label: "完全不记得", class: "bg-red-100 text-red-700 hover:bg-red-200 dark:bg-red-900 dark:text-red-200", activeClass: "bg-red-200 text-red-800 ring-2 ring-red-400" },
  { value: 2, label: "有点印象", class: "bg-orange-100 text-orange-700 hover:bg-orange-200 dark:bg-orange-900 dark:text-orange-200", activeClass: "bg-orange-200 text-orange-800 ring-2 ring-orange-400" },
  { value: 3, label: "勉强答对", class: "bg-yellow-100 text-yellow-700 hover:bg-yellow-200 dark:bg-yellow-900 dark:text-yellow-200", activeClass: "bg-yellow-200 text-yellow-800 ring-2 ring-yellow-400" },
  { value: 4, label: "答对了，但犹豫", class: "bg-lime-100 text-lime-700 hover:bg-lime-200 dark:bg-lime-900 dark:text-lime-200", activeClass: "bg-lime-200 text-lime-800 ring-2 ring-lime-400" },
  { value: 5, label: "完全正确！", class: "bg-emerald-100 text-emerald-700 hover:bg-emerald-200 dark:bg-emerald-900 dark:text-emerald-200", activeClass: "bg-emerald-200 text-emerald-800 ring-2 ring-emerald-400" },
];

function checkAnswer() {
  const correct = (currentStatement.value?.english || "").trim().toLowerCase();
  const answer = userAnswer.value.trim().toLowerCase();
  isCorrect.value = sameSentence(correct, answer);
  checked.value = true;
}

function sameSentence(a: string, b: string): boolean {
  const norm = (s: string) => s.normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/\s+/g, " ").trim();
  return norm(a) === norm(b);
}

async function submitReview(quality: number) {
  selectedQuality.value = quality;
  if (currentReview.value) {
    try {
      await fetchRecordReview(currentReview.value.statementId, quality);
    } catch (e) { console.error(e); }
  }
  await nextReview();
}

async function nextReview() {
  currentIndex.value++;
  checked.value = false;
  userAnswer.value = "";
  selectedQuality.value = null;
  loadCurrentReview();
}

async function loadCurrentReview() {
  if (currentIndex.value < dueItems.value.length) {
    currentReview.value = dueItems.value[currentIndex.value];
    try {
      const course = await fetchCourse(currentReview.value.coursePackId, currentReview.value.courseId);
      const stmt = course.statements.find((s: any) => s.id === currentReview.value?.statementId);
      currentStatement.value = stmt || null;
    } catch (e) {
      currentStatement.value = null;
    }
  } else {
    currentReview.value = null;
    currentStatement.value = null;
  }
}

onMounted(async () => {
  try {
    const [due, stats] = await Promise.all([
      fetchDueReviews(),
      fetchTotalStats().catch(() => ({})),
    ]);
    dueItems.value = due;
    dueCount.value = due.length;
    currentStreak.value = (stats as any).currentStreak || 0;
    totalReviewed.value = (stats as any).totalExercises || 0;
    if (due.length > 0) {
      loadCurrentReview();
    }
  } catch (e) {
    console.error("Failed to load reviews", e);
  }
});
</script>
