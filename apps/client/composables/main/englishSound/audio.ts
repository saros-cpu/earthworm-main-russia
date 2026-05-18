import { usePronunciation } from "~/composables/user/pronunciation";

// 便于测试
// 后面不使用 audio 后也可以不破坏业务逻辑
const audio = new Audio();
export function updateSource(src: string) {
  audio.src = src;
  audio.load();
}

const { getPronunciationUrl, shouldPreferRussianVoice } = usePronunciation();
export function usePlayWordSound() {
  let lastWord = "";
  let isPlaying = false;

  function handlePlayWordSound(word: string) {
    if (isPlaying && lastWord === word) {
      // skip
      return;
    }
    lastWord = word;
    if (speakRussian(word)) {
      return;
    }
    isPlaying = true;
    playSource(getPronunciationUrl(word));
    window.setTimeout(() => {
      isPlaying = false;
    }, 2000);
  }

  return {
    handlePlayWordSound,
  };
}

export interface PlayOptions {
  times?: number;
  rate?: number;
  interval?: number;
}

const DefaultPlayOptions = {
  times: 1,
  rate: 1,
  interval: 500,
};

let cachedRussianVoice: SpeechSynthesisVoice | null | undefined;
let currentAudio: HTMLAudioElement | undefined;
let currentTimeoutId: number | undefined;

function getVoices() {
  if (typeof window === "undefined" || !("speechSynthesis" in window)) {
    return [];
  }

  return window.speechSynthesis.getVoices();
}

function findRussianVoice() {
  const voices = getVoices();
  if (!voices.length) {
    return undefined;
  }

  if (cachedRussianVoice && voices.some((voice) => voice.name === cachedRussianVoice?.name)) {
    return cachedRussianVoice;
  }

  const russianVoices = voices.filter((voice) => {
    const lang = voice.lang.toLowerCase();
    const name = voice.name.toLowerCase();
    return lang.startsWith("ru") || name.includes("russian") || name.includes("рус");
  });

  cachedRussianVoice =
    russianVoices.find((voice) => voice.localService) ||
    russianVoices.find((voice) => voice.lang.toLowerCase() === "ru-ru") ||
    russianVoices[0] ||
    null;

  return cachedRussianVoice;
}

function canUseRussianSpeechSynthesis() {
  return Boolean(findRussianVoice());
}

export function speakRussian(text: string | undefined, playOptions?: PlayOptions) {
  if (!text || typeof window === "undefined" || !("speechSynthesis" in window)) {
    return false;
  }
  // 确保语音列表已加载
  const voices = window.speechSynthesis.getVoices();
  if (!voices.length) {
    window.speechSynthesis.addEventListener("voiceschanged", () => {}, { once: true });
  }
  const voice = findRussianVoice();
  if (!voice) return false;

  const { times, rate, interval } = Object.assign({}, DefaultPlayOptions, playOptions);
  let index = 0;
  let stopped = false;

  const speakOnce = () => {
    if (stopped) return;
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.voice = voice;
    utterance.lang = "ru-RU";
    utterance.rate = Number(rate) || 1;
    utterance.onend = () => {
      index++;
      if (!stopped && index < Number(times || 1)) {
        currentTimeoutId = window.setTimeout(speakOnce, Number(interval || 500));
      }
    };
    utterance.onerror = () => {
      if (!stopped) stopped = true;
    };
    window.speechSynthesis.speak(utterance);
  };

  speakOnce();

  return () => {
    stopped = true;
    window.speechSynthesis.cancel();
    stopCurrentAudio();
  };
}

export function playSource(src: string, playOptions?: PlayOptions) {
  const { times, rate, interval } = Object.assign({}, DefaultPlayOptions, playOptions);
  let index = 0;
  let stopped = false;

  stopCurrentAudio();

  const playOnce = () => {
    if (stopped) return;
    const player = new Audio(src);
    currentAudio = player;
    player.preload = "auto";
    player.playbackRate = Number(rate) || 1;
    player.onended = () => {
      index++;
      if (!stopped && index < Number(times || 1)) {
        currentTimeoutId = window.setTimeout(playOnce, Number(interval || 500));
      }
    };
    player.onerror = () => {
      if (currentAudio !== player || stopped) return;
      console.warn("俄语发音音频加载失败", src);
    };
    player.play().catch((error) => {
      console.warn("俄语发音播放失败", error);
    });
  };

  playOnce();

  return () => {
    stopped = true;
    stopCurrentAudio();
  };
}

function stopCurrentAudio() {
  if (currentTimeoutId) {
    clearTimeout(currentTimeoutId);
    currentTimeoutId = undefined;
  }
  if (currentAudio) {
    currentAudio.pause();
    currentAudio.currentTime = 0;
    currentAudio.src = "";
    currentAudio = undefined;
  }
}

export function play(playOptions?: PlayOptions) {
  const { times, rate, interval } = Object.assign({}, DefaultPlayOptions, playOptions);

  audio.playbackRate = rate;
  audio.play().catch((error) => {
    console.warn("俄语发音播放失败，请检查网络或浏览器自动播放权限", error);
  });
  if (times > 1) {
    audio.addEventListener("ended", handleEnded, false);
  }

  let index = 1;
  let timeoutId: NodeJS.Timeout;
  function handleEnded() {
    timeoutId = setTimeout(() => {
      if (index < times) {
        audio.play().catch((error) => {
          console.warn("俄语发音重复播放失败", error);
        });
        index++;
      } else {
        index = 1;
        audio.removeEventListener("ended", handleEnded);
      }
    }, interval);
  }

  return () => {
    audio.pause();
    audio.currentTime = 0;
    audio.removeEventListener("ended", handleEnded);
    timeoutId && clearTimeout(timeoutId);
  };
}
