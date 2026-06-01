import { ref, computed } from "vue";

export type ComboRating = "C" | "B" | "A" | "S" | "SS" | "SSS";

const comboCount = ref(0);
const maxCombo = ref(0);
const totalScore = ref(0);
const totalCorrect = ref(0);
const totalQuestions = ref(0);
const lastAnswerCorrect = ref<boolean | null>(null);

export function useCombo() {
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
    lastAnswerCorrect.value = correct;

    const combo = comboCount.value;
    const baseScore = correct ? 100 : 0;
    const comboMultiplier = getComboMultiplier(combo);
    const score = correct ? Math.round(baseScore * comboMultiplier) : 0;
    totalScore.value += score;

    const rating = getRating();
    return { rating, score, combo };
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
    lastAnswerCorrect.value = null;
  }

  function getSessionStats() {
    return {
      combo: comboCount.value,
      maxCombo: maxCombo.value,
      totalScore: totalScore.value,
      totalCorrect: totalCorrect.value,
      totalQuestions: totalQuestions.value,
      accuracy: totalQuestions.value > 0
        ? Math.round((totalCorrect.value / totalQuestions.value) * 100)
        : 0,
      rating: getRating(),
    };
  }

  return {
    comboCount: computed(() => comboCount.value),
    maxCombo: computed(() => maxCombo.value),
    totalScore: computed(() => totalScore.value),
    totalCorrect: computed(() => totalCorrect.value),
    totalQuestions: computed(() => totalQuestions.value),
    lastAnswerCorrect,
    recordAnswer,
    resetCombo,
    getSessionStats,
    getComboLabel,
    getRating,
  };
}
