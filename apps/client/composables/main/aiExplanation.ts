import { ref } from "vue";

import { fetchAiGrammarExplanation } from "~/api/ai";

const explanation = ref("");
const loading = ref(false);
const visible = ref(false);

export function useAiExplanation() {
  async function requestExplanation(
    sentenceRussian: string,
    sentenceChinese: string,
    userAnswer: string,
  ) {
    loading.value = true;
    visible.value = true;
    explanation.value = "";
    try {
      explanation.value = await fetchAiGrammarExplanation({
        sentenceRussian,
        sentenceChinese,
        userAnswer,
      });
    } catch {
      explanation.value = "AI 分析暂时不可用，稍后重试。";
    }
    loading.value = false;
  }

  function hideExplanation() {
    visible.value = false;
    explanation.value = "";
  }

  return {
    explanation,
    loading,
    visible,
    requestExplanation,
    hideExplanation,
  };
}
