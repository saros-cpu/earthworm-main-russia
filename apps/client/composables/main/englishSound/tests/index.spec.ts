import { createTestingPinia } from "@pinia/testing";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { PronunciationType, usePronunciation } from "~/composables/user/pronunciation";
import { useCourseStore } from "~/store/course";
import { playSource, speakRussian, updateSource } from "../audio";
import { useCurrentStatementEnglishSound } from "../index";

vi.mock("../audio.ts", () => {
  return {
    updateSource: vi.fn(),
    playSource: vi.fn(),
    speakRussian: vi.fn(() => false),
  };
});

describe("useCurrentStatementEnglishSound", () => {
  beforeEach(() => {
    vi.useFakeTimers();
    localStorage.clear();
    createTestingPinia({
      createSpy: vi.fn,
    });

    const courseStore = useCourseStore();
    courseStore.currentStatement = {
      id: "1",
      order: 1,
      english: "I",
      soundmark: "/I/",
      chinese: "我",
      isMastered: false,
    };

    vi.clearAllMocks();
  });

  it("plays sound with the backend TTS voice by default", async () => {
    const { playSound } = useCurrentStatementEnglishSound();

    playSound();

    expect(speakRussian).not.toHaveBeenCalled();
    expect(playSource).toHaveBeenCalled();
  });

  it("uses the system voice only when system default pronunciation is selected", async () => {
    const { togglePronunciation } = usePronunciation();
    togglePronunciation(PronunciationType.British);
    vi.mocked(speakRussian).mockReturnValueOnce(() => {});
    const { playSound } = useCurrentStatementEnglishSound();

    playSound();

    expect(speakRussian).toHaveBeenCalled();
    expect(playSource).not.toHaveBeenCalled();
  });

  it("should updates audio source", async () => {
    useCurrentStatementEnglishSound();

    // update english value
    const courseStore = useCourseStore();
    courseStore.currentStatement = {
      id: "2",
      order: 2,
      english: "like",
      soundmark: "/like/",
      chinese: "喜欢",
      isMastered: false,
    };
    await vi.advanceTimersToNextTimerAsync();

    expect(updateSource).toBeCalledTimes(1);
  });

  it("does not update audio source if the word is the same", async () => {
    useCurrentStatementEnglishSound();

    const courseStore = useCourseStore();
    courseStore.currentStatement = {
      id: "1",
      order: 1,
      english: "I",
      soundmark: "/I/",
      chinese: "我",
      isMastered: false,
    };

    expect(updateSource).toHaveBeenCalledTimes(1);
  });
});
