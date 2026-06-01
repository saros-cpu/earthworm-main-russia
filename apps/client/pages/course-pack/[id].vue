<template>
  <div class="flex w-full flex-col py-6">
    <template v-if="isLoading">
      <Loading />
    </template>

    <template v-else-if="errorMessage">
      <div class="rounded-md border border-rose-300 bg-rose-50 p-6 text-rose-700 dark:border-rose-900 dark:bg-rose-950/40 dark:text-rose-200">
        <div class="font-bold">课程包详情加载失败</div>
        <div class="mt-2 text-sm">{{ errorMessage }}</div>
        <button class="btn btn-sm mt-3" @click="setup">重试</button>
      </div>
    </template>

    <template v-else-if="!coursePackStore.currentCoursePack">
      <div class="rounded-md border border-dashed border-slate-300 p-10 text-center text-sm text-slate-500 dark:border-slate-700">
        没有找到这个课程包（id: {{ coursePackId }}）。
      </div>
    </template>

    <template v-else>
      <div class="mb-6 border-b border-slate-200 pb-5 dark:border-slate-800">
        <NuxtLink
          to="/course-pack"
          class="mb-3 inline-flex items-center gap-1 text-sm font-bold text-slate-500 transition hover:text-emerald-700 dark:text-slate-400 dark:hover:text-emerald-300"
        >
          <UIcon
            name="i-ph-arrow-left"
            class="h-4 w-4"
          />
          全部课程包
        </NuxtLink>

        <div class="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h2 class="text-3xl font-black text-slate-950 dark:text-white">
              {{ coursePackStore.currentCoursePack?.title }}
            </h2>
            <p class="mt-2 max-w-3xl text-sm leading-6 text-slate-500 dark:text-slate-400">
              {{ coursePackStore.currentCoursePack?.description || "按顺序完成课程，逐步建立俄语输入和表达能力。" }}
            </p>
          </div>

          <button
            v-if="selectedCategory"
            class="btn btn-outline btn-sm"
            @click="selectedCategory = ''"
          >
            查看全部课程
          </button>
        </div>

        <div
          v-if="isVocabularyPack"
          class="mt-5 grid gap-3 md:grid-cols-3 xl:grid-cols-6"
        >
          <button
            v-for="item in vocabularyOverview"
            :key="item.key"
            :class="[
              'rounded-md border bg-white p-3 text-left transition hover:-translate-y-0.5 hover:shadow-md dark:bg-slate-900',
              selectedCategory === item.key
                ? 'border-emerald-400 ring-2 ring-emerald-100 dark:border-emerald-500 dark:ring-emerald-950'
                : 'border-slate-200 dark:border-slate-800',
            ]"
            @click="toggleCategory(item.key)"
          >
            <div class="mb-2 flex items-center gap-2 text-xs font-bold text-slate-500 dark:text-slate-400">
              <UIcon
                :name="item.icon"
                class="h-4 w-4"
              />
              {{ item.label }}
            </div>
            <div class="text-2xl font-black text-slate-950 dark:text-white">{{ item.wordCount }}</div>
            <div class="mt-1 text-xs text-slate-500">{{ item.courseCount }} 课</div>
          </button>
        </div>

        <div
          v-if="selectedCategoryMeta"
          class="mt-4 flex flex-wrap items-center justify-between gap-3 rounded-md border border-emerald-200 bg-emerald-50 p-3 dark:border-emerald-900 dark:bg-emerald-950/40"
        >
          <div class="text-sm text-emerald-900 dark:text-emerald-100">
            当前聚焦：<span class="font-bold">{{ selectedCategoryMeta.label }}</span>
            <span class="ml-2 text-emerald-700 dark:text-emerald-300">
              {{ filteredCourses.length }} 课，{{ selectedCategoryWordCount }} 词
            </span>
          </div>
          <button
            class="btn btn-sm border-none bg-emerald-700 text-white hover:bg-emerald-800"
            @click="startSelectedCategory"
          >
            开始练这一类
          </button>
        </div>

        <div
          v-else-if="isVocabularyPack && recommendedCourse"
          class="mt-4 flex flex-wrap items-center justify-between gap-3 rounded-md border border-slate-200 bg-white p-3 dark:border-slate-800 dark:bg-slate-900"
        >
          <div class="text-sm text-slate-600 dark:text-slate-300">
            建议学习路径：先从
            <span class="font-bold text-slate-950 dark:text-white">{{ recommendedCourse.title }}</span>
            开始，熟悉主语和称呼后再进入名词、动词。
          </div>
          <button
            class="btn btn-sm border-none bg-slate-950 text-white hover:bg-slate-800 dark:bg-white dark:text-slate-950"
            @click="handleChangeCourse(recommendedCourse.id)"
          >
            从推荐课开始
          </button>
        </div>
      </div>

      <div class="h-full scrollbar-hide">
        <div
          v-if="filteredCourses.length"
          class="grid h-[76vh] grid-cols-1 justify-start gap-4 overflow-y-auto overflow-x-hidden pb-40 pr-2 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
        >
          <CoursesCourseCard
            v-for="course in filteredCourses"
            :id="course.id"
            :key="course.id"
            :title="course.title"
            :description="course.description"
            :count="course.completionCount"
            :statement-count="course.statementCount"
            :coursePackId="course.coursePackId"
            @click="handleChangeCourse(course.id)"
          />
        </div>

        <div
          v-else
          class="rounded-md border border-dashed border-slate-300 p-10 text-center text-sm text-slate-500 dark:border-slate-700"
        >
          当前分类下还没有课程。
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { navigateTo } from "#app";
import { computed, ref } from "vue";
import { useRoute } from "vue-router";

import { useActiveCourseMap } from "~/composables/courses/activeCourse";
import { useCoursePackStore } from "~/store/coursePack";
import type { Course } from "~/types";

type VocabularyCategory = {
  key: string;
  label: string;
  icon: string;
  matches: string[];
};

const categories: VocabularyCategory[] = [
  { key: "pronoun", label: "代词", icon: "i-ph-user-focus", matches: ["代词"] },
  { key: "noun", label: "名词", icon: "i-ph-book-open-text", matches: ["名词"] },
  { key: "verb", label: "动词", icon: "i-ph-lightning", matches: ["动词"] },
  { key: "modifier", label: "修饰词", icon: "i-ph-palette", matches: ["形容词", "副词"] },
  { key: "function", label: "功能词", icon: "i-ph-link", matches: ["介词", "连接词"] },
  { key: "pending", label: "待精炼", icon: "i-ph-hourglass-medium", matches: ["待补"] },
];

const isLoading = ref(false);
const errorMessage = ref("");
const selectedCategory = ref("");
const route = useRoute();
const coursePackStore = useCoursePackStore();
const coursePackId = route.params.id as string;
const { updateActiveCourseMap } = useActiveCourseMap();

const courses = computed(() => coursePackStore.currentCoursePack?.courses || []);

const isVocabularyPack = computed(() => {
  const pack = coursePackStore.currentCoursePack;
  if (!pack) return false;
  return pack.id.startsWith("vocab-pack-") || pack.title.includes("单词") || pack.title.includes("词汇");
});

const vocabularyOverview = computed(() =>
  categories
    .map((category) => {
      const matchedCourses = courses.value.filter((course) => categoryMatches(course, category));
      return {
        ...category,
        courseCount: matchedCourses.length,
        wordCount: matchedCourses.reduce((sum, course) => sum + (course.statementCount || 0), 0),
      };
    })
    .filter((item) => item.courseCount > 0),
);

const filteredCourses = computed(() => {
  if (!selectedCategory.value) {
    return courses.value;
  }
  const category = categories.find((item) => item.key === selectedCategory.value);
  if (!category) {
    return courses.value;
  }
  return courses.value.filter((course) => categoryMatches(course, category));
});

const selectedCategoryMeta = computed(() =>
  selectedCategory.value ? categories.find((item) => item.key === selectedCategory.value) : undefined,
);

const selectedCategoryWordCount = computed(() =>
  filteredCourses.value.reduce((sum, course) => sum + (course.statementCount || 0), 0),
);

const recommendedCourse = computed(() => {
  if (!isVocabularyPack.value) {
    return undefined;
  }
  return courses.value.find((course) => course.title.includes("代词")) || courses.value[0];
});

setup();

async function setup() {
  errorMessage.value = "";
  isLoading.value = true;
  try {
    await coursePackStore.setupCoursePack(coursePackId);
  } catch (err: any) {
    console.error("[course-pack/id] fetch failed", err);
    errorMessage.value = err?.message || String(err) || "未知错误";
  } finally {
    isLoading.value = false;
  }
}

function categoryMatches(course: Course, category: VocabularyCategory) {
  return category.matches.some((keyword) => course.title.includes(keyword));
}

function toggleCategory(categoryKey: string) {
  selectedCategory.value = selectedCategory.value === categoryKey ? "" : categoryKey;
}

function startSelectedCategory() {
  const firstCourse = filteredCourses.value[0];
  if (firstCourse) {
    handleChangeCourse(firstCourse.id);
  }
}

function handleChangeCourse(courseId: string) {
  updateActiveCourseMap(coursePackId, courseId);
  navigateTo(`/game/${coursePackId}/${courseId}`);
}
</script>
