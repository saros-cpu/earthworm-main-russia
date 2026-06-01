<template>
  <MainComboDisplay
    :combo-count="gameStore.comboCount"
    :combo-label="gameStore.getComboLabel(gameStore.comboCount)"
    :score="lastScore"
    :rating="gameStore.getRating()"
    :show-rating="showRating"
  />
  <template v-if="isDictationMode()">
    <ModeDictationMode />
  </template>
  <template v-else-if="isChineseToEnglishMode()">
    <ModeChineseToEnglishMode />
  </template>
  <template v-else-if="isWordAssemblyMode()">
    <ModeWordAssemblyMode />
  </template>
  <template v-else-if="isSpeechAssessmentMode()">
    <ModeSpeechAssessmentMode />
  </template>
  <template v-else-if="isAudioCourseMode()">
    <ModeAudioCourseMode />
  </template>

  <MainLearningTimer v-if="isAuthenticated()"></MainLearningTimer>
  <MainTips />
  <MainSummary />
  <LazyMainShare />
  <GamePauseModal v-if="isAuthenticated()"></GamePauseModal>
  <MainGameSettingModal />
  <MainAiAssistant />
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from "vue";

import GamePauseModal from "~/components/main/GamePauseModal.vue";
import { courseTimer } from "~/composables/courses/courseTimer";
import { useGamePlayMode } from "~/composables/user/gamePlayMode";
import { isAuthenticated } from "~/services/auth";
import { useCourseStore } from "~/store/course";
import { useGameStore } from "~/store/game";
import { fetchSaveExercise } from "~/api/learning";
import comboMilestoneSound from "~/assets/sounds/right.mp3";
import comboMaxSound from "~/assets/sounds/right.mp3";

const { isChineseToEnglishMode, isDictationMode, isWordAssemblyMode, isSpeechAssessmentMode, isAudioCourseMode } = useGamePlayMode();
const gameStore = useGameStore();
const courseStore = useCourseStore();
const lastScore = ref(0);
const showRating = ref(false);

const comboAudio = new Audio(comboMilestoneSound);
const maxComboAudio = new Audio(comboMaxSound);

watch(() => gameStore.comboCount, (combo) => {
  if (combo === 5) { comboAudio.currentTime = 0; comboAudio.play().catch(() => {}); }
  else if (combo === 10) { comboAudio.currentTime = 0; comboAudio.playbackRate = 1.2; comboAudio.play().catch(() => {}); }
  else if (combo === 15) { comboAudio.currentTime = 0; comboAudio.playbackRate = 1.5; comboAudio.play().catch(() => {}); }
  else if (combo === 20) { maxComboAudio.currentTime = 0; maxComboAudio.playbackRate = 2; maxComboAudio.play().catch(() => {}); }
});

function handleAnswerResult(correct: boolean, timeSpentMs: number) {
  const { score, combo } = gameStore.recordAnswer(correct);
  lastScore.value = score;
  if (gameStore.totalQuestions >= 3) {
    showRating.value = true;
  }

  if (isAuthenticated() && courseStore.currentCourse && courseStore.currentStatement) {
    fetchSaveExercise({
      coursePackId: courseStore.currentCourse.coursePackId,
      courseId: courseStore.currentCourse.id,
      statementId: courseStore.currentStatement.id,
      correct,
      attempts: 1,
      timeSpentMs,
      score,
      combo,
    }).catch(() => {});
  }
}

onMounted(() => {
  courseTimer.reset();
  gameStore.startGame();
  gameStore.resetCombo();
});

onUnmounted(() => {
  gameStore.exitGame();
});
</script>
