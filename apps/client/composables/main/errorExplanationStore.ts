import { ref } from "vue";

interface ErrorExplanation {
  sentenceRussian: string;
  sentenceChinese: string;
  userAnswer: string;
  aiExplanation: string;
  loading: boolean;
}

const explanations = ref<Record<string, ErrorExplanation>>({});

export function useErrorExplanationStore() {
  function setLoading(
    statementId: string,
    info: { sentenceRussian: string; sentenceChinese: string; userAnswer: string },
  ) {
    explanations.value[statementId] = { ...info, aiExplanation: "", loading: true };
  }

  function setExplanation(statementId: string, text: string) {
    if (explanations.value[statementId]) {
      explanations.value[statementId].aiExplanation = text;
      explanations.value[statementId].loading = false;
    }
  }

  function hasExplanation(statementId: string) {
    return !!explanations.value[statementId];
  }

  function getExplanation(statementId: string) {
    return explanations.value[statementId] || null;
  }

  function clear(statementId: string) {
    delete explanations.value[statementId];
  }

  function clearAll() {
    explanations.value = {};
  }

  return {
    explanations,
    setLoading,
    setExplanation,
    hasExplanation,
    getExplanation,
    clear,
    clearAll,
  };
}
