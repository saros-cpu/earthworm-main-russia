import { defineStore } from "pinia";
import { computed, ref } from "vue";

import { useLearningTimeTracker } from "~/composables/main/learningTimeTracker";
import { isAuthenticated } from "~/services/auth";

export type ComboRating = "C" | "B" | "A" | "S" | "SS" | "SSS";

enum GameStatus {
  NOT_PLAYED = "not_played",
  STARTED = "started",
  PAUSED = "paused",
  LEVEL_COMPLETED = "level_completed",
}

export const useGameStore = defineStore("game", () => {
  const { startTracking, stopTracking } = useLearningTimeTracker();
  const gameStatus = ref<GameStatus>(GameStatus.NOT_PLAYED);
  const comboCount = ref(0);
  const maxCombo = ref(0);
  const totalScore = ref(0);
  const totalCorrect = ref(0);
  const totalQuestions = ref(0);

  function startGame() {
    gameStatus.value = GameStatus.STARTED;
    if (isAuthenticated()) {
      startTracking();
    }
  }

  function exitGame() {
    gameStatus.value = GameStatus.NOT_PLAYED;
    if (isAuthenticated()) {
      stopTracking();
    }
  }

  function pauseGame() {
    if (gameStatus.value === GameStatus.STARTED) {
      gameStatus.value = GameStatus.PAUSED;
      if (isAuthenticated()) {
        stopTracking();
      }
      return true;
    } else {
      return false;
    }
  }

  function resumeGame() {
    if (gameStatus.value === GameStatus.PAUSED) {
      gameStatus.value = GameStatus.STARTED;
      if (isAuthenticated()) {
        startTracking();
      }
    } else {
    }
  }

  function completeLevel() {
    if (gameStatus.value === GameStatus.STARTED) {
      gameStatus.value = GameStatus.LEVEL_COMPLETED;
      if (isAuthenticated()) {
        stopTracking();
      }
    } else {
    }
  }

  function getComboMultiplier(combo: number): number {
    if (combo >= 20) return 2.0;
    if (combo >= 15) return 1.7;
    if (combo >= 10) return 1.5;
    if (combo >= 7) return 1.3;
    if (combo >= 5) return 1.2;
    if (combo >= 3) return 1.1;
    return 1.0;
  }

  function getRating(): ComboRating {
    const accuracy = totalQuestions.value > 0 ? totalCorrect.value / totalQuestions.value : 0;
    if (accuracy >= 0.98 && maxCombo.value >= 10) return "SSS";
    if (accuracy >= 0.95 && maxCombo.value >= 7) return "SS";
    if (accuracy >= 0.90 && maxCombo.value >= 5) return "S";
    if (accuracy >= 0.80) return "A";
    if (accuracy >= 0.60) return "B";
    return "C";
  }

  function recordAnswer(correct: boolean): { rating: ComboRating; score: number; combo: number } {
    totalQuestions.value++;
    if (correct) {
      totalCorrect.value++;
      comboCount.value++;
      if (comboCount.value > maxCombo.value) {
        maxCombo.value = comboCount.value;
      }
    } else {
      comboCount.value = 0;
    }
    const combo = comboCount.value;
    const baseScore = correct ? 100 : 0;
    const comboMultiplier = getComboMultiplier(combo);
    const score = correct ? Math.round(baseScore * comboMultiplier) : 0;
    totalScore.value += score;
    return { rating: getRating(), score, combo };
  }

  function getComboLabel(combo: number): string {
    if (combo >= 20) return "LEGENDARY";
    if (combo >= 15) return "AMAZING";
    if (combo >= 10) return "GREAT";
    if (combo >= 7) return "GOOD";
    if (combo >= 5) return "NICE";
    if (combo >= 3) return "COOL";
    return "";
  }

  function resetCombo() {
    comboCount.value = 0;
    maxCombo.value = 0;
    totalScore.value = 0;
    totalCorrect.value = 0;
    totalQuestions.value = 0;
  }

  function isGameNotPlayed() {
    return gameStatus.value === GameStatus.NOT_PLAYED;
  }

  function isGameStarted() {
    return gameStatus.value === GameStatus.STARTED;
  }

  function isGamePaused() {
    return gameStatus.value === GameStatus.PAUSED;
  }

  function isLevelCompleted() {
    return gameStatus.value === GameStatus.LEVEL_COMPLETED;
  }

  return {
    gameStatus,
    comboCount,
    maxCombo,
    totalScore,
    totalCorrect,
    totalQuestions,
    startGame,
    pauseGame,
    resumeGame,
    exitGame,
    completeLevel,
    recordAnswer,
    getComboLabel,
    getRating,
    resetCombo,
    isGameNotPlayed,
    isGameStarted,
    isGamePaused,
    isLevelCompleted,
  };
});
