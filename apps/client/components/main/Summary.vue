<template>
  <UModal
    v-model="showModal"
    prevent-close
  >
    <UContainer
      :ui="{
        base: 'w-[90vw]',
        constrained: 'max-w-[780px]',
      }"
    >
      <div class="flex justify-between items-center mb-4">
        <div class="flex items-center gap-3">
          <h3 class="font-bold text-lg">{{ $t('summary.congratulations') }}</h3>
          <span v-if="gameStore.totalQuestions > 0"
            class="rounded-full px-4 py-1 text-sm font-black tracking-wider"
            :class="ratingBadgeClass"
          >
            {{ ratingText }}
          </span>
        </div>
        <button
          tabindex="0"
          class="btn btn-ghost btn-sm mx-1 h-7 w-7 rounded-md p-0"
          @click="soundSentence"
        >
          <UIcon
            name="i-ph-speaker-simple-high"
            class="h-full w-full"
          ></UIcon>
        </button>
      </div>

      <div class="flex flex-col">
        <div class="flex">
          <span class="text-3xl font-bold sm:text-4xl lg:text-6xl">"</span>
          <div class="flex-1 text-center text-sm leading-loose sm:text-base lg:text-xl">
            {{ ruSentence }}
          </div>
          <span class="invisible text-3xl font-bold sm:text-4xl lg:text-6xl">"</span>
        </div>

        <div class="flex">
          <span class="invisible text-3xl font-bold sm:text-4xl lg:text-6xl">"</span>
          <div class="flex-1 text-center text-sm leading-loose sm:text-base lg:text-xl">
            {{ zhSentence }}
          </div>
          <span class="text-3xl font-bold sm:text-4xl lg:text-6xl">"</span>
        </div>
        <p class="text-right text-xs text-gray-200 sm:text-sm">{{ $t('summary.dailyPhrase') }}</p>
        <p
          class="pl-2 text-xs leading-loose text-gray-600 sm:pl-4 sm:text-sm lg:pl-14 lg:text-base"
        >
          {{ $t('summary.completedTasks', { count: courseTimer.totalRecordNumber(), time: formatSecondsToTime(courseTimer.calculateTotalTime()) }) }}
        </p>
        <p
          v-if="isAuthenticated()"
          class="pl-2 text-xs leading-loose text-gray-400 sm:pl-4 sm:text-sm lg:pl-14 lg:text-base"
        >
          {{ $t('summary.todayStudied', { minutes: formattedMinutes }) }}
          <span v-if="totalMinutes >= 30">{{ $t('summary.greatJob') }}</span>
        </p>
      </div>
      <div class="mt-4 grid grid-cols-2 gap-2 rounded-md bg-slate-50 p-3 text-center text-sm dark:bg-slate-800 sm:grid-cols-4">
        <div>
          <div class="text-lg font-black" :class="ratingTextColor">{{ gameStore.getRating() }}</div>
          <div class="text-xs text-slate-400">{{ $t('summary.rating') }}</div>
        </div>
        <div>
          <div class="text-lg font-black text-emerald-600 dark:text-emerald-400">{{ maxComboText }}</div>
          <div class="text-xs text-slate-400">{{ $t('summary.maxCombo') }}</div>
        </div>
        <div>
          <div class="text-lg font-black text-slate-950 dark:text-white">{{ accuracyText }}%</div>
          <div class="text-xs text-slate-400">{{ $t('summary.accuracy') }}</div>
        </div>
        <div>
          <div class="text-lg font-black text-amber-600 dark:text-amber-400">{{ gameStore.totalScore }}</div>
          <div class="text-xs text-slate-400">{{ $t('summary.points') }}</div>
        </div>
      </div>
      <div className="modal-action flex flex-col sm:flex-row gap-2 justify-center sm:justify-end">
        <button
          class="btn btn-primary w-full sm:w-auto"
          @click="toShare"
        >
          {{ $t('summary.share') }}
        </button>
        <button
          class="btn w-full sm:w-auto"
          @click="handleDoAgain"
        >
          {{ $t('summary.again') }}
        </button>
        <button
          class="btn w-full sm:w-auto"
          @click="handleGoToCourseList"
        >
          {{ $t('summary.toList') }}
        </button>
        <button
          class="btn w-full sm:w-auto"
          @click="goToNextCourse"
        >
          {{ $t('summary.next') }}
          <UKbd> ↵ </UKbd>
        </button>
      </div>
    </UContainer>
  </UModal>

  <canvas
    ref="confettiCanvasRef"
    class="pointer-events-none absolute left-0 top-0 z-[1000] h-full w-full"
  ></canvas>
</template>

<script setup lang="ts">
import { useModal } from "#imports";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { toast } from "vue-sonner";

import Dialog from "~/components/common/Dialog.vue";
import { useActiveCourseMap } from "~/composables/courses/activeCourse";
import { courseTimer } from "~/composables/courses/courseTimer";
import { useConfetti } from "~/composables/main/confetti/useConfetti";
import { readOneSentencePerDayAloud } from "~/composables/main/englishSound";
import { useGameMode } from "~/composables/main/game";
import { useLearningTimeTracker } from "~/composables/main/learningTimeTracker";
import { useDailySentence, useSummary } from "~/composables/main/summary";
import { useNavigation } from "~/composables/useNavigation";
import { isAuthenticated, signIn } from "~/services/auth";
import { useCourseStore } from "~/store/course";
import { useCoursePackStore } from "~/store/coursePack";
import { useGameStore } from "~/store/game";
import { permitSaveStatement, preventSaveStatement } from "~/store/statement";
import { formatSecondsToTime } from "~/utils/date";
import { cancelShortcut, registerShortcut } from "~/utils/keyboardShortcuts";

const { t } = useI18n();

const courseStore = useCourseStore();
const coursePackStore = useCoursePackStore();
const { gotoCourseList, gotoGame } = useNavigation();
const { showQuestion } = useGameMode();
const { handleGoToCourseList, goToNextCourse, completeCourse } = useCourse();
const { handleDoAgain } = useDoAgain();
const { showModal, hideSummary } = useSummary();
const { zhSentence, ruSentence } = useDailySentence();
const { confettiCanvasRef, playConfetti } = useConfetti();
const { updateActiveCourseMap } = useActiveCourseMap();
const { totalMinutes, formattedMinutes } = useTotalLearningTime();

const gameStore = useGameStore();
const modal = useModal();

const maxComboText = computed(() => `${gameStore.maxCombo}x`);
const accuracyText = computed(() => {
  return gameStore.totalQuestions > 0
    ? Math.round((gameStore.totalCorrect / gameStore.totalQuestions) * 100)
    : 0;
});
const ratingText = computed(() => {
  const r = gameStore.getRating();
  const labels: Record<string, string> = { C: t('summary.ratingLabels.C'), B: t('summary.ratingLabels.B'), A: t('summary.ratingLabels.A'), S: t('summary.ratingLabels.S'), SS: t('summary.ratingLabels.SS'), SSS: t('summary.ratingLabels.SSS') };
  return labels[r] || r;
});
const ratingTextColor = computed(() => {
  const r = gameStore.getRating();
  const colors: Record<string, string> = { C: "text-slate-500", B: "text-violet-500", A: "text-blue-500", S: "text-emerald-500", SS: "text-orange-500", SSS: "text-yellow-500" };
  return colors[r] || "text-slate-500";
});
const ratingBadgeClass = computed(() => {
  const r = gameStore.getRating();
  const classes: Record<string, string> = {
    C: "bg-slate-100 text-slate-600 dark:bg-slate-700",
    B: "bg-violet-100 text-violet-700 dark:bg-violet-900 dark:text-violet-200",
    A: "bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-200",
    S: "bg-emerald-100 text-emerald-700 dark:bg-emerald-900 dark:text-emerald-200",
    SS: "bg-orange-100 text-orange-700 dark:bg-orange-900 dark:text-orange-200",
    SSS: "bg-yellow-100 text-yellow-700 dark:bg-yellow-900 dark:text-yellow-200",
  };
  return classes[r] || "bg-slate-100 text-slate-600";
});

watch(showModal, (val) => {
  if (val) {
    // 阻止包含 statement 完成课程后会自动把用户的进度设置成下一课
    // 这里是为了防止先设置成下一课 后更新了 statement 的进度
    // 这就会造成获取用户最近的课程包进度出现错误  因为是基于时间来获取的
    preventSaveStatement();
    // 注册回车键进入下一课
    registerShortcut("enter", goToNextCourse);
    // 显示结算面板代表当前课程已经完成
    completeCourse();
    // 朗读每日一句
    soundSentence();
    // 延迟一小会放彩蛋
    // 停止计时
    gameStore.completeLevel();
    setTimeout(async () => {
      playConfetti();
    }, 300);
  } else {
    // 取消回车键进入下一课
    cancelShortcut("enter", goToNextCourse);
    permitSaveStatement();
  }
});

function useTotalLearningTime() {
  const { totalSeconds } = useLearningTimeTracker();
  const totalMinutes = computed(() => Math.ceil(totalSeconds.value / 60));

  const formattedMinutes = computed(() => {
    return Math.max(totalMinutes.value, 1).toString();
  });

  return {
    totalMinutes,
    formattedMinutes,
  };
}

function useDoAgain() {
  async function handleDoAgain() {
    // 看看是不是没有全部掌握了
    // 如果是全部掌握了 那么给个提示 然后挑战到课程列表
    if (courseStore.isAllMastered()) {
      toast.info(t("summary.allMastered"), {
        duration: 1500,
        onAutoClose: () => {
          handleGoToCourseList();
        },
      });
      return;
    }
    courseStore.doAgain();
    hideSummary();
    showQuestion();
    courseTimer.reset();
    gameStore.startGame();
  }

  return {
    handleDoAgain,
  };
}

// 朗读每日一句
function soundSentence() {
  readOneSentencePerDayAloud(ruSentence.value);
}

function useCourse() {
  let nextCourseId = ref("");

  const haveNextCourse = computed(() => {
    return nextCourseId.value;
  });

  async function goToNextCourse() {
    if (!isAuthenticated()) {
      // 去注册
      modal.open(Dialog, {
        title: t("summary.moreFeatures"),
        content: t("summary.registerPrompt"),
        showCancel: true,
        showConfirm: true,
        cancelText: t("summary.cancel"),
        confirmText: t("summary.register"),
        async onConfirm() {
          courseStore.resetStatementIndex();
          showQuestion();
          signIn();
        },
      });

      return;
    }

    hideSummary();

    if (!haveNextCourse.value) {
      toast.info(t("summary.lastLesson"), {
        duration: 1500,
        onAutoClose: () => {
          handleGoToCourseList();
        },
      });
      return;
    }

    if (courseStore.currentCourse) {
      gotoGame(courseStore.currentCourse.coursePackId, nextCourseId.value);
    }
  }

  function handleGoToCourseList() {
    hideSummary();
    if (courseStore.currentCourse) {
      gotoCourseList(courseStore.currentCourse.coursePackId);
    }
  }

  async function completeCourse() {
    if (isAuthenticated() && courseStore.currentCourse) {
      const { coursePackId } = courseStore.currentCourse;
      const { nextCourse } = await courseStore.completeCourse();
      coursePackStore.updateCoursesCompleteCount(coursePackId);

      if (nextCourse) {
        nextCourseId.value = nextCourse.id;
        updateActiveCourseMap(coursePackId, nextCourseId.value);
      } else {
        updateActiveCourseMap(coursePackId, "");
      }
    }
  }

  return {
    completeCourse,
    goToNextCourse,
    handleGoToCourseList,
  };
}

const toShare = async () => {
  const { useShareModal } = await import("~/composables/main/shareImage/share");
  const { showShareModal } = useShareModal();
  showShareModal();
};
</script>
