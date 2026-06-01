<template>
  <div
    class="relative flex items-center justify-between border-b border-solid border-slate-200 bg-white px-4 py-3 text-base dark:border-slate-800 dark:bg-slate-900"
  >
    <!-- 左侧 -->
    <div class="flex items-center">
      <NuxtLink
        class="clickable-item flex h-9 w-9 items-center justify-center rounded-md border border-slate-200 bg-slate-50 dark:border-slate-700 dark:bg-slate-800"
        :to="`/course-pack/${courseStore.currentCourse?.coursePackId}`"
      >
        <UTooltip text="课程列表">
          <UIcon
            name="i-ph-list-bullets"
            class="h-5 w-5"
          />
        </UTooltip>
      </NuxtLink>
      <div
        class="clickable-item ml-3 max-w-[48vw] truncate rounded-md bg-slate-100 px-3 py-2 text-sm font-bold text-slate-700 dark:bg-slate-800 dark:text-slate-200"
        @click="openCourseContents"
      >
        <UTooltip text="课程题目列表">
          {{ currentCourseInfo }}
        </UTooltip>
      </div>
      <MainStudyVideoLink :video="courseStore.currentCourse?.video" />
    </div>

    <!-- 右侧 -->
    <div class="flex items-center gap-4">
      <USelectMenu
        v-model="currentMode"
        :options="modeOptions"
        size="xs"
        class="w-28"
        @change="switchMode"
      />
      <div
        @click="openGameSettingModal"
        v-if="isDictationMode()"
      >
        <UTooltip text="游戏设置">
          <UIcon
            name="i-ph-gear"
            class="clickable-icon"
          />
        </UTooltip>
      </div>

      <div
        v-if="isAuthenticated()"
        @click="pauseGame"
      >
        <UTooltip
          text="暂停游戏"
          :shortcuts="parseShortcut(shortcutKeys.pause)"
        >
          <UIcon
            name="i-ph-pause"
            class="clickable-icon"
          />
        </UTooltip>
      </div>

      <div @click="handleDoAgain">
        <UTooltip text="重置当前课程进度">
          <UIcon
            name="i-ph-arrow-counter-clockwise"
            class="clickable-icon"
          />
        </UTooltip>
      </div>
    </div>

    <MainCourseContents v-model:isOpen="isOpenCourseContents"></MainCourseContents>
  </div>

  <CommonProgressBar
    class="h-2 rounded-none p-0"
    :percentage="currentPercentage"
  />
</template>

<script setup lang="ts">
import { useModal } from "#imports";
import { computed, ref } from "vue";

import Dialog from "~/components/common/Dialog.vue";
import { useQuestionInput } from "~/components/main/QuestionInput/questionInputHelper";
import { courseTimer } from "~/composables/courses/courseTimer";
import { useGameMode } from "~/composables/main/game";
import { clearQuestionInput } from "~/composables/main/question";
import { useCourseContents } from "~/composables/main/useCourseContents";
import { useGamePause } from "~/composables/main/useGamePause";
import { useGameSetting } from "~/composables/main/useGameSetting";
import { useGamePlayMode } from "~/composables/user/gamePlayMode";
import { parseShortcut, useShortcutKeyMode } from "~/composables/user/shortcutKey";
import { isAuthenticated } from "~/services/auth";
import { useCourseStore } from "~/store/course";

const { shortcutKeys } = useShortcutKeyMode();
const { isDictationMode, isChineseToEnglishMode, currentGamePlayMode, toggleGamePlayMode, getGamePlayModeOptions } = useGamePlayMode();
const currentMode = ref(currentGamePlayMode.value);
const modeOptions = computed(() => getGamePlayModeOptions().map(o => ({ label: o.label, value: o.value })));
function switchMode(option: any) {
  if (option?.value) toggleGamePlayMode(option.value);
}
const courseStore = useCourseStore();
const { focusInput } = useQuestionInput();
const { openCourseContents } = useCourseContents();
const { handleDoAgain } = useDoAgain();
const { pauseGame } = useGamePause();
const { openGameSettingModal } = useGameSetting();
const modal = useModal();

const currentCourseInfo = computed(() => {
  return `${courseStore.currentCourse?.title}（${currentSchedule.value}/${courseStore.visibleStatementsCount}）`;
});

const currentSchedule = computed(() => {
  return courseStore.visibleStatementIndex + 1;
});

const currentPercentage = computed(() => {
  if (courseStore.isAllDone()) {
    return 100;
  }
  return ((courseStore.visibleStatementIndex / courseStore.visibleStatementsCount) * 100).toFixed(
    2,
  );
});

const isOpenCourseContents = ref(false);

function useDoAgain() {
  const { showQuestion } = useGameMode();

  function handleDoAgain() {
    modal.open(Dialog, {
      title: "重置进度",
      content: "是否确认重置当前课程进度？",
      showCancel: true,
      showConfirm: true,
      async onCancel() {
        setTimeout(() => {
          focusInput();
        }, 300);
      },
      async onConfirm() {
        handleTipConfirm();
      },
    });
  }

  function handleTipConfirm() {
    courseStore.doAgain();
    clearQuestionInput();
    showQuestion();
    courseTimer.reset();
    // dialog 关闭后 自动聚焦 因为关闭有个 200 毫秒的动画 所以需要延迟聚焦 input
    setTimeout(() => {
      focusInput();
    }, 300);
  }

  return {
    handleDoAgain,
    handleTipConfirm,
  };
}
</script>

<style scoped>
.clickable-item {
  @apply cursor-pointer select-none transition hover:text-emerald-600 dark:hover:text-emerald-300;
}

.clickable-icon {
  @apply h-9 w-9 cursor-pointer rounded-md border border-slate-200 bg-slate-50 p-2 text-slate-600 transition hover:border-emerald-300 hover:text-emerald-700 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-200 dark:hover:border-emerald-500 dark:hover:text-emerald-300;
}
</style>
