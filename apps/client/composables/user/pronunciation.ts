import { ref } from "vue";
import { useRuntimeConfig } from "#app";

/**
 * 俄语朗读优先使用浏览器内置语音；浏览器不支持时，使用远程 TTS 作为兜底。
 */

export enum PronunciationType {
  American = "American",
  British = "British",
}

export const pronunciationLabels: { [key in PronunciationType]: string } = {
  [PronunciationType.American]: "俄语优先",
  [PronunciationType.British]: "系统默认",
};

const PRONUNCIATION_TYPE = "pronunciationType";
const pronunciation = ref<PronunciationType>(PronunciationType.American);
export function usePronunciation() {
  loadCache();

  function loadCache() {
    const type = getStore() || pronunciation.value;
    setStore(type);
  }

  function setStore(value: PronunciationType) {
    pronunciation.value = value;
    localStorage.setItem(PRONUNCIATION_TYPE, value);
  }

  function getStore(): PronunciationType {
    return localStorage.getItem(PRONUNCIATION_TYPE) as PronunciationType;
  }

  function shouldPreferRussianVoice() {
    return pronunciation.value === PronunciationType.American;
  }

  function getPronunciationOptions() {
    return Object.entries(pronunciationLabels).map(([key, value]) => {
      return {
        label: value,
        value: key,
      };
    });
  }

  function getPronunciationUrl(text: string | undefined): string {
    const config = useRuntimeConfig();
    return `${config.public.apiBase}/tts/ru?text=${encodeURIComponent(text || "")}`;
  }

  // 切换发音
  function togglePronunciation(type: PronunciationType) {
    if (type !== pronunciation.value) setStore(type);
  }

  return {
    pronunciation,
    getPronunciationOptions,
    getPronunciationUrl,
    shouldPreferRussianVoice,
    togglePronunciation,
  };
}
