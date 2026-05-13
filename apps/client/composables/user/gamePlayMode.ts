import { ref } from "vue";

export enum GamePlayMode {
  Dictation = "DICTATION",
  ChineseToEnglish = "CHINESE_TO_ENGLISH",
  WordAssembly = "WORD_ASSEMBLY",
  SpeechAssessment = "SPEECH_ASSESSMENT",
  AudioCourse = "AUDIO_COURSE",
}

export const gamePlayModeLabels: Record<GamePlayMode, string> = {
  [GamePlayMode.ChineseToEnglish]: "中译俄",
  [GamePlayMode.Dictation]: "俄语听写",
  [GamePlayMode.WordAssembly]: "连词成句",
  [GamePlayMode.SpeechAssessment]: "口语测评",
  [GamePlayMode.AudioCourse]: "听力课程",
};

const GamePlayModeKey = "gamePlayMode";
const currentGamePlayMode = ref<GamePlayMode>(GamePlayMode.ChineseToEnglish);

function loadCache() {
  const mode = getStore() || currentGamePlayMode.value;
  currentGamePlayMode.value = mode;
}

function getStore() {
  return localStorage.getItem(GamePlayModeKey) as GamePlayMode;
}

function setStore(value: GamePlayMode) {
  localStorage.setItem(GamePlayModeKey, value);
}

loadCache();

export function useGamePlayMode() {
  function getGamePlayModeOptions() {
    return Object.entries(gamePlayModeLabels).map(([key, value]) => ({
      label: value,
      value: key,
    }));
  }

  function toggleGamePlayMode(mode: GamePlayMode) {
    currentGamePlayMode.value = mode;
    setStore(mode);
  }

  function isDictationMode() { return currentGamePlayMode.value === GamePlayMode.Dictation; }
  function isChineseToEnglishMode() { return currentGamePlayMode.value === GamePlayMode.ChineseToEnglish; }
  function isWordAssemblyMode() { return currentGamePlayMode.value === GamePlayMode.WordAssembly; }
  function isSpeechAssessmentMode() { return currentGamePlayMode.value === GamePlayMode.SpeechAssessment; }
  function isAudioCourseMode() { return currentGamePlayMode.value === GamePlayMode.AudioCourse; }

  return {
    toggleGamePlayMode,
    getGamePlayModeOptions,
    currentGamePlayMode,
    isDictationMode,
    isChineseToEnglishMode,
    isWordAssemblyMode,
    isSpeechAssessmentMode,
    isAudioCourseMode,
  };
}
