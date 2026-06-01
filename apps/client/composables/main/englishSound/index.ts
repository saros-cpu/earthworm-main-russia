import { watchEffect } from "vue";

import type { PlayOptions } from "./audio";
import { useToolbar } from "~/composables/main/dictation";
import { useGamePlayMode } from "~/composables/user/gamePlayMode";
import { usePronunciation } from "~/composables/user/pronunciation";
import { useCourseStore } from "~/store/course";
import { playSource, speakRussian, updateSource } from "./audio";

const { getPronunciationUrl } = usePronunciation();

let lastPronunciationUrl = "";
export function useCurrentStatementEnglishSound() {
  const courseStore = useCourseStore();
  const { toolBarData } = useToolbar();
  const { isDictationMode } = useGamePlayMode();

  watchEffect(() => {
    const word = courseStore.currentStatement?.english;
    const pronunciationUrl = getPronunciationUrl(word);
    if (lastPronunciationUrl !== pronunciationUrl) {
      updateSource(pronunciationUrl);
    }
    lastPronunciationUrl = pronunciationUrl;
  });

  return {
    playSound: (options?: PlayOptions) => {
      const text = courseStore.currentStatement?.english;
      if (isDictationMode()) {
        const { times, rate, interval } = toolBarData;
        if (speakRussian(text, { times, rate, interval })) {
          return () => window.speechSynthesis?.cancel();
        }
        return playRussianText(text, { times, rate, interval });
      } else {
        return playRussianText(text, options);
      }
    },
  };
}

export function playRussianText(text: string | undefined, options?: PlayOptions) {
  if (!text) {
    return () => {};
  }
  if (speakRussian(text, options)) {
    return () => window.speechSynthesis?.cancel();
  }
  return playSource(getPronunciationUrl(text), options);
}

// 朗读每日一句
export function readOneSentencePerDayAloud(str: string) {
  playRussianText(str);
}

export function playEnglish(english: string) {
  playRussianText(english);
}
