<template>
  <div class="w-full py-6">
    <section
      class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <p class="text-sm font-bold text-orange-600 dark:text-orange-300">每日挑战</p>
      <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">
        {{ completed ? "挑战完成！" : "今日挑战" }}
      </h1>
      <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
        {{
          completed
            ? `答对 ${correctCount}/${totalCount} 题，得分 ${score}`
            : "每天 5 道随机俄语题，限时作答"
        }}
      </p>
    </section>

    <section
      v-if="!started"
      class="mb-5 rounded-md border border-slate-200 bg-white p-10 text-center shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <UIcon
        name="i-ph-sword"
        class="mx-auto mb-4 h-16 w-16 text-orange-400"
      />
      <p class="mb-6 text-lg text-slate-600 dark:text-slate-300">
        每天 5 道随机中译俄题，每题 30 秒，挑战你的反应速度！
      </p>
      <button
        class="btn btn-primary"
        @click="startChallenge"
      >
        开始挑战
      </button>
      <div
        v-if="bestScore > 0"
        class="mt-4 text-sm text-slate-400"
      >
        历史最佳：{{ bestScore }} 分
      </div>
    </section>

    <section
      v-else-if="!completed"
      class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <div class="mb-4 flex items-center justify-between">
        <span class="text-sm font-bold text-slate-600 dark:text-slate-300"
          >第 {{ currentIndex + 1 }}/{{ totalCount }} 题</span
        >
        <span class="text-sm text-slate-400">
          <UIcon
            name="i-ph-check-circle"
            class="mr-1 inline h-3.5 w-3.5 text-emerald-500"
          />
          {{ correctCount }} 正确
          <UIcon
            name="i-ph-x-circle"
            class="ml-2 mr-1 inline h-3.5 w-3.5 text-red-500"
          />
          {{ wrongCount }} 错误
        </span>
      </div>

      <div
        v-if="currentStmt"
        class="mb-6"
      >
        <p class="mb-4 text-center text-xl font-bold text-slate-950 dark:text-white">
          {{ currentStmt.chinese }}
        </p>
        <input
          v-model="userInput"
          type="text"
          placeholder="输入俄语翻译..."
          class="h-12 w-full rounded-lg border border-slate-200 bg-white px-4 text-center text-lg outline-none transition focus:border-orange-400 dark:border-slate-700 dark:bg-slate-800"
          @keydown.enter="submitAnswer"
          :disabled="answered"
          ref="inputRef"
        />
        <div
          v-if="answered"
          class="mt-4 rounded-lg bg-slate-50 p-4 text-center dark:bg-slate-800"
        >
          <p class="text-sm text-slate-500">正确答案：</p>
          <p
            class="mt-1 text-lg font-bold"
            :class="isCorrect ? 'text-emerald-600' : 'text-red-500'"
          >
            {{ currentStmt.russian }}
          </p>
        </div>
      </div>

      <div class="flex justify-center gap-3">
        <button
          v-if="!answered"
          class="btn btn-primary"
          @click="submitAnswer"
          :disabled="!userInput.trim()"
        >
          提交
        </button>
        <button
          v-else
          class="btn"
          @click="nextQuestion"
        >
          下一题
        </button>
      </div>
    </section>

    <section
      v-else
      class="rounded-md border border-emerald-200 bg-white p-8 text-center shadow-sm dark:border-emerald-800 dark:bg-slate-900"
    >
      <UIcon
        :name="score >= 80 ? 'i-ph-trophy' : score >= 40 ? 'i-ph-smiley' : 'i-ph-facebook-logo'"
        class="mx-auto mb-4 h-16 w-16 text-orange-400"
      />
      <div class="mb-4 text-5xl font-black text-slate-950 dark:text-white">
        {{ score }}<span class="text-2xl text-slate-400">/100</span>
      </div>
      <p class="mb-2 text-sm text-slate-500">正确 {{ correctCount }}/{{ totalCount }} 题</p>
      <p class="text-sm text-slate-500">{{ getRatingText }}</p>
      <button
        class="btn mt-6"
        @click="resetChallenge"
      >
        再来一次
      </button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from "vue";

import { getHttp } from "~/api/http";
import { fetchWords } from "~/api/learning";
import { playRussianText } from "~/composables/main/englishSound";

interface ChallengeItem {
  chinese: string;
  russian: string;
}

const totalCount = 5;
const started = ref(false);
const completed = ref(false);
const currentIndex = ref(0);
const userInput = ref("");
const answered = ref(false);
const isCorrect = ref(false);
const correctCount = ref(0);
const wrongCount = ref(0);
const score = ref(0);
const items = ref<ChallengeItem[]>([]);
const inputRef = ref<HTMLInputElement>();
const bestScore = ref(0);

const currentStmt = computed(() => items.value[currentIndex.value] || null);

const STORAGE_KEY = "daily_challenge_best";

onMounted(() => {
  bestScore.value = parseInt(localStorage.getItem(STORAGE_KEY) || "0");
});

async function startChallenge() {
  started.value = true;
  completed.value = false;
  currentIndex.value = 0;
  correctCount.value = 0;
  wrongCount.value = 0;
  score.value = 0;
  answered.value = false;
  userInput.value = "";
  try {
    const data = await fetchWords({ page: 0, size: 50 });
    const pool = data.content || [];
    const shuffled = pool.sort(() => Math.random() - 0.5).slice(0, totalCount);
    const http = getHttp();
    items.value = [];
    for (const w of shuffled) {
      const detail = await http<any>("/words/" + w.id, { method: "get" });
      items.value.push({
        chinese: detail.chinese || w.chinese || "",
        russian: (detail as any).word || w.word || "",
      });
    }
    await nextTick();
    inputRef.value?.focus();
  } catch {
    items.value = [
      { chinese: "你好", russian: "Здравствуйте" },
      { chinese: "谢谢", russian: "Спасибо" },
      { chinese: "是的", russian: "Да" },
      { chinese: "不是", russian: "Нет" },
      { chinese: "再见", russian: "До свидания" },
    ];
  }
}

function submitAnswer() {
  if (!userInput.value.trim() || answered.value) return;
  answered.value = true;
  const input = userInput.value.trim().toLowerCase();
  const correct = currentStmt.value.russian.toLowerCase();
  isCorrect.value = input === correct || correct.includes(input) || input.includes(correct);
  if (isCorrect.value) correctCount.value++;
  else wrongCount.value++;
  score.value = Math.round((correctCount.value / totalCount) * 100);
}

function nextQuestion() {
  if (currentIndex.value >= totalCount - 1) {
    completed.value = true;
    if (score.value > bestScore.value) {
      bestScore.value = score.value;
      localStorage.setItem(STORAGE_KEY, String(score.value));
    }
    return;
  }
  currentIndex.value++;
  answered.value = false;
  userInput.value = "";
  nextTick(() => inputRef.value?.focus());
}

function resetChallenge() {
  started.value = false;
  completed.value = false;
}

function playAudio(text: string) {
  playRussianText(text);
}

const getRatingText = computed(() => {
  if (score.value >= 100) return "完美！全部答对！⭐";
  if (score.value >= 80) return "优秀！继续保持！";
  if (score.value >= 60) return "不错，再接再厉！";
  return "今天状态不佳，明天再来吧！";
});
</script>
