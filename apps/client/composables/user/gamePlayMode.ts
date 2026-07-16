import { ref } from "vue";

export enum GamePlayMode {
  Dictation = "DICTATION",
  ChineseToEnglish = "CHINESE_TO_ENGLISH",
  WordAssembly = "WORD_ASSEMBLY",
  SpeechAssessment = "SPEECH_ASSESSMENT",
  AudioCourse = "AUDIO_COURSE",
  Mixed = "MIXED",
}

export const gamePlayModeLabels: Record<GamePlayMode, string> = {
  [GamePlayMode.ChineseToEnglish]: "中译俄",
  [GamePlayMode.Dictation]: "俄语听写",
  [GamePlayMode.WordAssembly]: "连词成句",
  [GamePlayMode.SpeechAssessment]: "口语测评",
  [GamePlayMode.AudioCourse]: "听力课程",
  [GamePlayMode.Mixed]: "混合模式",
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
    if (mixedSubMode) mixedSubMode.value = undefined;
  }

  const subModes = [
    GamePlayMode.ChineseToEnglish,
    GamePlayMode.WordAssembly,
    GamePlayMode.Dictation,
  ];
  const mixedSubMode = ref<GamePlayMode>();

  function isDictationMode() {
    return currentGamePlayMode.value === GamePlayMode.Dictation;
  }
  function isChineseToEnglishMode() {
    return currentGamePlayMode.value === GamePlayMode.ChineseToEnglish;
  }
  function isWordAssemblyMode() {
    return currentGamePlayMode.value === GamePlayMode.WordAssembly;
  }
  function isSpeechAssessmentMode() {
    return currentGamePlayMode.value === GamePlayMode.SpeechAssessment;
  }
  function isAudioCourseMode() {
    return currentGamePlayMode.value === GamePlayMode.AudioCourse;
  }
  function isMixedMode() {
    return currentGamePlayMode.value === GamePlayMode.Mixed;
  }

  function currentOrMixedMode(): GamePlayMode {
    if (currentGamePlayMode.value !== GamePlayMode.Mixed) {
      return currentGamePlayMode.value;
    }
    if (!mixedSubMode.value || mixedSubMode.value === currentGamePlayMode.value) {
      const pick = subModes[Math.floor(Math.random() * subModes.length)];
      mixedSubMode.value = pick;
    }
    return mixedSubMode.value;
  }

  return {
    toggleGamePlayMode,
    getGamePlayModeOptions,
    currentGamePlayMode,
    mixedSubMode,
    subModes,
    currentOrMixedMode,
    isDictationMode,
    isChineseToEnglishMode,
    isWordAssemblyMode,
    isSpeechAssessmentMode,
    isAudioCourseMode,
    isMixedMode,
  };
}
