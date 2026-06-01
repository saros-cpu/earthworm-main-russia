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
    <template v-else-if="!coursePack">
      <div class="rounded-md border border-dashed border-slate-300 p-10 text-center text-sm text-slate-500 dark:border-slate-700">
        没有找到这个课程包。
      </div>
    </template>

    <template v-else>
      <div class="mb-6 border-b border-slate-200 pb-5 dark:border-slate-800">
        <NuxtLink
          :to="`/media-course`"
          class="mb-3 inline-flex items-center gap-1 text-sm font-bold text-slate-500 transition hover:text-emerald-700 dark:text-slate-400 dark:hover:text-emerald-300"
        >
          <UIcon name="i-ph-arrow-left" class="h-4 w-4" />
          媒体课程列表
        </NuxtLink>
        <h2 class="text-3xl font-black text-slate-950 dark:text-white">{{ coursePack.title }}</h2>
        <p class="mt-2 max-w-3xl text-sm leading-6 text-slate-500 dark:text-slate-400">{{ coursePack.description }}</p>
      </div>

      <div v-if="displayCourses.length" class="space-y-3">
        <div
          v-for="course in displayCourses"
          :key="course.id"
          class="flex cursor-pointer items-center gap-4 rounded-lg border border-slate-200 bg-white p-4 transition hover:border-emerald-300 hover:shadow-sm dark:border-slate-800 dark:bg-slate-900 dark:hover:border-emerald-700"
          @click="openCourse(course)"
        >
          <div class="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-lg bg-emerald-100 text-emerald-600 dark:bg-emerald-950 dark:text-emerald-300">
            <UIcon v-if="mediaType(course) === 'video'" name="i-ph-video" class="h-6 w-6" />
            <UIcon v-else name="i-ph-headphones" class="h-6 w-6" />
          </div>
          <div class="flex-1">
            <div class="font-bold text-slate-950 dark:text-white">{{ course.title }}</div>
            <div class="mt-1 text-xs text-slate-500 dark:text-slate-400">{{ course.description }}</div>
          </div>
          <div class="flex items-center gap-2">
            <span class="rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-600 dark:bg-slate-800 dark:text-slate-300">
              {{ course.statementCount || 0 }} 句
            </span>
            <UIcon name="i-ph-caret-right" class="h-5 w-5 text-slate-400" />
          </div>
        </div>
      </div>
      <div v-else class="rounded-md border border-dashed border-slate-300 p-10 text-center text-sm text-slate-500 dark:border-slate-700">
        这个课程包还没有媒体课程。
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useRoute } from "vue-router";
import { navigateTo } from "#app";

import { useCoursePackStore } from "~/store/coursePack";
import type { Course } from "~/types";

const route = useRoute();
const coursePackStore = useCoursePackStore();
const coursePackId = route.params.coursePackId as string;
const isLoading = ref(false);
const errorMessage = ref("");

const coursePack = computed(() => coursePackStore.currentCoursePack);
const displayCourses = computed(() => coursePack.value?.courses || []);

function mediaType(course: Course): "video" | "audio" {
  const ext = (course.video || "").toLowerCase();
  if (ext.endsWith(".mp3") || ext.endsWith(".wma") || ext.endsWith(".wav") || ext.endsWith(".ogg") || ext.endsWith(".m4a")) return "audio";
  return "video";
}

setup();

async function setup() {
  errorMessage.value = "";
  isLoading.value = true;
  try {
    await coursePackStore.setupCoursePack(coursePackId);
  } catch (err: any) {
    errorMessage.value = err?.message || String(err);
  } finally {
    isLoading.value = false;
  }
}

function openCourse(course: Course) {
  navigateTo(`/media-course/detail/${coursePackId}/${course.id}`);
}
</script>
