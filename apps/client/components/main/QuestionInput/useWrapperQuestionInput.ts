import { fetchAiGrammarExplanation } from "~/api/ai";
import { courseTimer } from "~/composables/courses/courseTimer";
import { useErrorExplanationStore } from "~/composables/main/errorExplanationStore";
import { useGameMode } from "~/composables/main/game";
import { useInput } from "~/composables/main/question";
import { useSummary } from "~/composables/main/summary";
import { useWeaknessAnalysis } from "~/composables/main/weaknessAnalysis";
import { useAutoNextQuestion } from "~/composables/user/autoNext";
import { useKeyboardSound } from "~/composables/user/sound";
import { useSpaceSubmitAnswer } from "~/composables/user/submitKey";
import { useCourseStore } from "~/store/course";
import { useGameStore } from "~/store/game";
import { useQuestionInput } from "./questionInputHelper";
import { useAnswerError } from "./useAnswerError";
import { usePlayTipSound, useTypingSound } from "./useTypingSound";

export function useWrapperQuestionInput() {
  const courseStore = useCourseStore();
  const gameStore = useGameStore();
  const { showAnswer } = useGameMode();
  const { showSummary } = useSummary();
  const { setInputCursorPosition, getInputCursorPosition, blurInput, focusInput } =
    useQuestionInput();
  const { isKeyboardSoundEnabled } = useKeyboardSound();
  const { checkPlayTypingSound, playTypingSound } = useTypingSound();
  const { handleAnswerError } = useAnswerError();
  const { playRightSound } = usePlayTipSound();
  const { isAutoNextQuestion } = useAutoNextQuestion();
  const { isUseSpaceSubmitAnswer } = useSpaceSubmitAnswer();

  const {
    initialize: initializeQuestionInput,
    findWordById,
    inputValue,
    submitAnswer,
    setInputValue,
    handleKeyboardInput,
    isFixMode,
    isFixInputMode,
  } = useInput({
    source: () =>
      courseStore.currentStatement?.targetText || courseStore.currentStatement?.english || "",
    setInputCursorPosition,
    getInputCursorPosition,
    inputChangedCallback,
  });

  function inputChangedCallback(e: KeyboardEvent) {
    if (isKeyboardSoundEnabled() && checkPlayTypingSound(e)) {
      playTypingSound();
    }
  }

  function handleAnswerRight() {
    courseTimer.timeEnd(String(courseStore.statementIndex));
    gameStore.recordAnswer(true);
    playRightSound();

    if (isAutoNextQuestion()) {
      if (courseStore.isAllDone()) {
        blurInput();
        showSummary();
      }
      courseStore.toNextStatement();
    } else {
      showAnswer();
    }
  }

  function handleAnswerWrong() {
    const stmt = courseStore.currentStatement;
    if (stmt) {
      useWeaknessAnalysis().analyzeError(stmt.english || stmt.targetText || "");
      gameStore.recordWrongAnswer(
        stmt.english || stmt.targetText || "",
        stmt.chinese || "",
        inputValue.value,
      );
      const stmtId = stmt.id || String(courseStore.statementIndex);
      const { setLoading, setExplanation } = useErrorExplanationStore();
      setLoading(stmtId, {
        sentenceRussian: stmt.english || stmt.targetText || "",
        sentenceChinese: stmt.chinese || "",
        userAnswer: inputValue.value,
      });
      fetchAiGrammarExplanation({
        sentenceRussian: stmt.english || stmt.targetText || "",
        sentenceChinese: stmt.chinese || "",
        userAnswer: inputValue.value,
      })
        .then((text) => setExplanation(stmtId, text))
        .catch(() => {});
    }
    gameStore.recordAnswer(false);
    handleAnswerError();
  }

  return {
    initializeQuestionInput,
    isFixMode,
    isFixInputMode,
    findWordById,
    inputValue,
    setInputValue,
    submitAnswer() {
      submitAnswer(handleAnswerRight, handleAnswerWrong);
      focusInput();
    },
    handleKeyboardInput(e: KeyboardEvent) {
      handleKeyboardInput(e, {
        useSpaceSubmitAnswer: {
          enable: isUseSpaceSubmitAnswer(),
          rightCallback: handleAnswerRight,
          errorCallback: handleAnswerWrong,
        },
      });
    },
  };
}
