<template>
  <div class="flex w-full flex-col py-6">
    <div class="mb-6 flex flex-col gap-3 border-b border-slate-200 pb-5 dark:border-slate-800 md:flex-row md:items-end md:justify-between">
      <div>
        <p class="text-sm font-bold text-emerald-600 dark:text-emerald-300">Course Library</p>
        <h2 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">俄语课程包</h2>
        <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
          从入门课程到教材 PDF 自动生成内容，选择一套课程开始闯关。
        </p>
      </div>
      <NuxtLink
        to="/"
        class="inline-flex h-10 items-center rounded-md border border-slate-300 px-4 text-sm font-bold text-slate-700 transition hover:border-emerald-500 hover:text-emerald-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-emerald-400 dark:hover:text-emerald-300"
      >
        返回首页
      </NuxtLink>
    </div>
    <template v-if="isLoading">
      <Loading></Loading>
    </template>
    <template v-else>
      <div class="h-[76vh] overflow-y-auto overflow-x-hidden pr-1 scrollbar-hide">
        <div
          class="grid auto-rows-fr grid-cols-1 gap-4 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4"
        >
          <template v-for="coursePack in coursePackStore.coursePacks">
            <CoursePackCard
              :coursePack="{
                id: coursePack.id,
                title: coursePack.title,
                description: coursePack.description,
                cover: coursePack.cover,
                isFree: coursePack.isFree,
              }"
              @cardClick="handleGoToCoursePack"
            ></CoursePackCard>
          </template>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";

import type { CoursePack } from "~/types";
import CoursePackCard from "~/components/courses/CoursePackCard.vue";
import { useNavigation } from "~/composables/useNavigation";
import { useCoursePackStore } from "~/store/coursePack";

const coursePackStore = useCoursePackStore();
const { gotoCourseList } = useNavigation();
const isLoading = ref(false);

setup();

async function setup() {
  // 课程包不会更新 所以初始化的时候只拉取一次数据就好了
  if (coursePackStore.coursePacks.length === 0) {
    isLoading.value = true;
    await coursePackStore.setupCoursePacks();
    isLoading.value = false;
  }
}

function handleGoToCoursePack(coursePack: CoursePack) {
  if (coursePack.isFree) {
    gotoCourseList(coursePack.id);
  } else {
    // 看看是不是会员 不是的话 直接弹出消息告知 需要是会员
    // TODO 还没有检测是不是会员的功能函数
    console.log("需要是会员");
  }
}

</script>

<style></style>
