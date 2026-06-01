<template>
  <div class="flex min-h-[260px]">
    <!-- Loading -->
    <div
      v-if="isLoading"
      class="flex flex-1 items-center justify-center"
    >
      <span class="loading loading-dots loading-md"></span>
    </div>
    <div
      v-else
      class="w-full"
    >
      <div
        v-if="coursePacks.length"
        class="grid w-full grid-cols-1 gap-4 min-[680px]:grid-cols-2 2xl:grid-cols-3"
      >
        <template v-for="coursePack in coursePacks">
          <CoursePackCard
            :coursePack="{
              id: coursePack.coursePackId,
              title: coursePack.title,
              description: coursePack.description,
              cover: coursePack.cover,
              isFree: coursePack.isFree,
            }"
          >
            <template #actions>
              <div class="mt-3 flex gap-2">
                <button
                  class="btn btn-outline btn-sm flex-1"
                  @click.stop="gotoCourseList(coursePack.coursePackId)"
                >
                  课程列表
                </button>
                <button
                  class="btn btn-sm flex-1 border-none bg-emerald-600 text-white hover:bg-emerald-700"
                  @click.stop="gotoGame(coursePack.coursePackId, coursePack.courseId)"
                >
                  继续游戏
                </button>
              </div>
            </template>
          </CoursePackCard>
        </template>
      </div>
      <template v-else>
        <div class="flex h-full w-full flex-1 items-center justify-center text-slate-500">
          暂无记录，          <NuxtLink
            to="/course-pack"
            class="link text-blue-500 no-underline hover:opacity-75"
            >先学习一课， </NuxtLink
          >再来看看吧~
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { navigateTo } from "#app";
import { ref } from "vue";

import CoursePackCard from "~/components/courses/CoursePackCard.vue";
import { useNavigation } from "~/composables/useNavigation";
import { useRecentCoursePack } from "./helper";

const { gotoCourseList, gotoGame } = useNavigation();
const { coursePacks, fetchCoursePacks } = useRecentCoursePack();
const isLoading = ref(false);

setup();

async function setup() {
  if (coursePacks.value.length === 0) {
    isLoading.value = true;
    await fetchCoursePacks();
    isLoading.value = false;
  } else {
    await fetchCoursePacks();
  }
}
</script>

<style scoped></style>
