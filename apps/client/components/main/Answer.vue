<template>
  <div class="mx-auto max-w-5xl text-center">
    <div
      class="rounded-md border border-emerald-200 bg-white p-6 shadow-sm dark:border-emerald-800 dark:bg-slate-900"
    >
      <div
        class="mb-4 inline-flex items-center gap-2 rounded-md bg-emerald-50 px-3 py-2 text-sm font-bold text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300"
      >
        <UIcon
          name="i-ph-check-circle"
          class="h-4 w-4"
        />
        回答正确
      </div>

      <div
        class="inline-flex flex-wrap items-center justify-center gap-1 text-4xl font-black text-slate-950 dark:text-white md:text-5xl"
      >
        <span
          v-for="word in words"
          :key="word"
          class="cursor-pointer p-1 hover:text-emerald-600 dark:hover:text-emerald-300"
          @click="handlePlayWordSound(word)"
        >
          {{ word }}
        </span>
        <UIcon
          name="i-ph-speaker-simple-high"
          class="ml-1 inline-block h-7 w-7 cursor-pointer text-slate-500 hover:text-emerald-600 dark:hover:text-emerald-300"
          @click="handlePlayEnglishSound"
        />
      </div>
    </div>

    <div class="my-5 flex items-center justify-center gap-3">
      <span
        v-if="phoneticText"
        class="text-xl text-gray-500"
        >{{ phoneticText }}</span
      >
      <span
        v-if="difficultyLabel"
        class="rounded-full px-2.5 py-0.5 text-xs font-bold"
        :class="difficultyColor"
        >{{ difficultyLabel }}</span
      >
    </div>

    <div class="my-5 text-xl text-gray-500">
      {{ sourceText }}
    </div>

    <div
      v-if="hasLearningNotes"
      class="mx-auto mb-6 max-w-3xl rounded-md border border-slate-200 bg-white p-4 text-left text-sm text-slate-700 shadow-sm dark:border-slate-800 dark:bg-slate-900 dark:text-gray-200"
    >
      <div class="mb-3 flex items-center justify-between gap-3">
        <div class="flex items-center gap-2 font-semibold text-slate-900 dark:text-white">
          <UIcon
            name="i-ph-notebook"
            class="h-4 w-4 text-emerald-600"
          />
          学习提示
        </div>
        <span
          v-if="courseStore.currentStatement?.refinementMode"
          class="rounded bg-gray-100 px-2 py-0.5 text-xs text-gray-600 dark:bg-gray-800 dark:text-gray-300"
        >
          {{ courseStore.currentStatement.refinementMode === "ai" ? "AI 精炼" : "规则精炼" }}
        </span>
      </div>

      <div
        v-if="courseStore.currentStatement?.grammarNote"
        class="mb-4 rounded bg-slate-50 p-3 leading-6 dark:bg-slate-800"
      >
        {{ courseStore.currentStatement.grammarNote }}
      </div>

      <div
        v-if="courseStore.currentStatement?.vocabulary?.length"
        class="grid gap-3 md:grid-cols-2"
      >
        <article
          v-for="item in courseStore.currentStatement.vocabulary"
          :key="item.word"
          class="rounded-md border border-slate-200 p-3 dark:border-slate-800"
        >
          <div class="mb-2 flex items-center justify-between gap-2">
            <button
              class="font-bold text-slate-950 hover:text-emerald-700 dark:text-white dark:hover:text-emerald-300"
              @click="handlePlayWordSound(item.word)"
            >
              {{ item.word }}
            </button>
            <span
              v-if="item.partOfSpeech"
              class="rounded bg-emerald-50 px-2 py-0.5 text-xs text-emerald-700 dark:bg-emerald-950 dark:text-emerald-200"
            >
              {{ item.partOfSpeech }}
            </span>
          </div>
          <div class="text-slate-700 dark:text-gray-200">
            {{ item.meaning }}
          </div>
          <div
            v-if="item.example"
            class="mt-2 rounded bg-slate-50 p-2 text-xs leading-5 text-slate-600 dark:bg-slate-800 dark:text-gray-300"
          >
            <div>{{ item.example }}</div>
            <div v-if="item.exampleTranslation">{{ item.exampleTranslation }}</div>
          </div>
        </article>
      </div>
    </div>

    <div
      v-if="errorExplanation"
      class="mx-auto mb-6 max-w-3xl rounded-md border border-rose-200 bg-white p-4 text-left text-sm shadow-sm dark:border-rose-900 dark:bg-slate-900"
    >
      <div class="mb-2 flex items-center gap-2 font-semibold text-rose-600 dark:text-rose-300">
        <UIcon
          name="i-ph-warning-circle"
          class="h-4 w-4"
        />
        AI 错误分析
      </div>
      <div
        v-if="errorExplanation.loading"
        class="text-slate-400"
      >
        <span class="animate-pulse">分析中...</span>
      </div>
      <div
        v-else
        class="leading-6 text-slate-700 dark:text-slate-300"
      >
        {{ errorExplanation.aiExplanation }}
      </div>
      <div
        v-if="errorExplanation.userAnswer"
        class="mt-2 rounded bg-rose-50 p-2 text-xs text-rose-600 dark:bg-rose-950 dark:text-rose-300"
      >
        你的答案：{{ errorExplanation.userAnswer }}
      </div>
    </div>

    <MainStatementNotes :statement-id="courseStore.currentStatement?.id" />

    <div class="space-y-3">
      <div class="flex flex-wrap items-center justify-center gap-2">
        <button
          class="btn btn-outline btn-sm"
          @click="showQuestion"
        >
          再来一次
        </button>
        <button
          class="btn btn-sm border-none bg-slate-950 text-white hover:bg-slate-800 dark:bg-white dark:text-slate-950"
          @click="goToNextQuestion"
        >
          下一题
        </button>
        <button
          class="btn btn-outline btn-sm"
          @click="addWordsToVocabulary"
        >
          <UIcon
            name="i-ph-bookmark-simple"
            class="h-4 w-4"
          />
          收藏生词
        </button>
      </div>
      <div class="md:hidden">
        <MainMasteredBtn />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from "vue";
import { toast } from "vue-sonner";

import { fetchAddVocabulary } from "~/api/learning";
import { playRussianText } from "~/composables/main/englishSound";
import { usePlayWordSound } from "~/composables/main/englishSound/audio";
import { useErrorExplanationStore } from "~/composables/main/errorExplanationStore";
import { useGameMode } from "~/composables/main/game";
import { useAutoPronunciation } from "~/composables/user/sound";
import { useCourseStore } from "~/store/course";
import { cancelShortcut, registerShortcut } from "~/utils/keyboardShortcuts";
import { useAnswer } from "./QuestionInput/useAnswer";

const courseStore = useCourseStore();
const { handlePlayWordSound } = usePlayWordSound();
const { showQuestion } = useGameMode();
const { isAutoPlaySound } = useAutoPronunciation();
const { goToNextQuestion } = useAnswer();
const { getExplanation } = useErrorExplanationStore();

const words = computed(() => courseStore.currentStatement?.english.split(" ") || []);
const phoneticText = computed(
  () => courseStore.currentStatement?.phonetic || courseStore.currentStatement?.soundmark || "",
);
const sourceText = computed(
  () => courseStore.currentStatement?.sourceText || courseStore.currentStatement?.chinese || "",
);
const hasLearningNotes = computed(
  () =>
    !!courseStore.currentStatement?.grammarNote ||
    !!courseStore.currentStatement?.vocabulary?.length,
);
const difficultyLabel = computed(() => {
  const d = courseStore.currentStatement?.difficulty;
  if (!d) return null;
  const map: Record<string, string> = {
    beginner: "入门",
    elementary: "初级",
    intermediate: "中级",
  };
  return map[d] || d;
});
const difficultyColor = computed(() => {
  const d = courseStore.currentStatement?.difficulty;
  if (d === "beginner") return "bg-green-100 text-green-700 dark:bg-green-950 dark:text-green-300";
  if (d === "elementary") return "bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-300";
  if (d === "intermediate")
    return "bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-300";
  return "bg-slate-100 text-slate-500";
});
const errorExplanation = computed(() => {
  const stmtId = courseStore.currentStatement?.id;
  if (!stmtId) return null;
  return getExplanation(stmtId);
});

registerShortcutKeyForNextQuestion();

onMounted(() => {
  if (isAutoPlaySound()) {
    playRussianText(courseStore.currentStatement?.english);
  }
});

async function addWordsToVocabulary() {
  const text = courseStore.currentStatement?.english;
  if (!text) return;
  const words = text.split(" ").filter((w) => /[а-яё]/i.test(w));
  if (words.length === 0) {
    toast.info("没有可收藏的俄语单词");
    return;
  }
  let added = 0;
  for (const word of words) {
    try {
      await fetchAddVocabulary({ word, sourceStatementId: courseStore.currentStatement?.id });
      added++;
    } catch (_) {}
  }
  toast.success(`已添加 ${added} 个单词到生词本`);
}

function handlePlayEnglishSound() {
  playRussianText(courseStore.currentStatement?.english);
}

function registerShortcutKeyForNextQuestion() {
  function handleKeydown(e: KeyboardEvent) {
    e.preventDefault();
    goToNextQuestion();
  }
  onMounted(() => {
    registerShortcut(" ", handleKeydown);
    registerShortcut("enter", handleKeydown);
  });

  onUnmounted(() => {
    cancelShortcut(" ", handleKeydown);
    cancelShortcut("enter", handleKeydown);
  });
}
</script>
