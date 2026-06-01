import { onMounted, ref } from "vue";

import { fetchDailySentence } from "~/api/tool";

const showModal = ref(false);
export function useSummary() {
  function showSummary() {
    showModal.value = true;
  }

  function hideSummary() {
    showModal.value = false;
  }

  return {
    showModal,
    showSummary,
    hideSummary,
  };
}

export const defaultRuSentence = "Повторение — мать учения.";
export const defaultZhSentence = "重复是学习之母。";

const ruSentence = ref(defaultRuSentence);
const zhSentence = ref(defaultZhSentence);
const hasLoadingDailySentence = ref(false);

export const resetSentenceLoading = () => (hasLoadingDailySentence.value = false);

export function useDailySentence() {
  const getDailySentence = async () => {
    if (!hasLoadingDailySentence.value) {
      hasLoadingDailySentence.value = true;
      const { ru, zh } = await fetchDailySentence().catch((err) => {
        hasLoadingDailySentence.value = false;
        return Promise.reject(err);
      });
      ruSentence.value = ru;
      zhSentence.value = zh;
    }
  };

  onMounted(() => {
    getDailySentence();
  });

  return {
    ruSentence,
    zhSentence,
    getDailySentence,
  };
}
