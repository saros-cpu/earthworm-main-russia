<template>
  <div class="w-full py-6">
    <section
      class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <p class="text-sm font-bold text-orange-600 dark:text-orange-400">{{ $t("pages.duel") }}</p>
      <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">PK 对战</h1>
      <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
        创建或加入房间，和好友实时PK俄语答题！
      </p>
    </section>

    <template v-if="!currentRoom || !currentRoom.roomId">
      <section class="grid gap-4 md:grid-cols-2">
        <div
          class="rounded-md border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900"
        >
          <h2 class="mb-2 text-lg font-black text-slate-950 dark:text-white">创建房间</h2>
          <p class="mb-4 text-sm text-slate-500 dark:text-slate-400">
            选择一个课程包，创建对战房间。
          </p>
          <select
            v-model="selectedPack"
            class="select select-bordered mb-3 w-full text-sm"
          >
            <option
              value=""
              disabled
            >
              请选择课程包
            </option>
            <option
              v-for="p in packs"
              :key="p.id"
              :value="p.id"
            >
              {{ p.name || p.title || p.id }}
            </option>
          </select>
          <button
            class="btn btn-primary w-full"
            :disabled="!selectedPack"
            @click="createRoom"
          >
            创建房间
          </button>
        </div>
        <div
          class="rounded-md border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900"
        >
          <h2 class="mb-2 text-lg font-black text-slate-950 dark:text-white">加入房间</h2>
          <p class="mb-4 text-sm text-slate-500 dark:text-slate-400">输入对方创建的房间ID。</p>
          <input
            v-model="joinRoomId"
            type="text"
            placeholder="输入房间ID"
            class="input input-bordered mb-3 w-full text-sm"
          />
          <button
            class="btn btn-secondary w-full"
            :disabled="!joinRoomId"
            @click="joinRoom"
          >
            加入房间
          </button>
        </div>
      </section>
    </template>

    <template v-else-if="currentRoom.status === 'waiting'">
      <section
        class="rounded-md border border-emerald-200 bg-emerald-50 p-6 text-center shadow-sm dark:border-emerald-800 dark:bg-emerald-950"
      >
        <div class="mb-3 text-4xl">⏳</div>
        <h2 class="mb-2 text-lg font-black text-slate-950 dark:text-white">等待对手加入...</h2>
        <p class="mb-4 text-sm text-slate-500 dark:text-slate-400">将房间ID分享给好友：</p>
        <div
          class="inline-flex items-center gap-2 rounded-md bg-white px-4 py-2 font-mono text-lg font-bold shadow-sm dark:bg-slate-800"
        >
          {{ currentRoom.roomId }}
          <button
            class="btn btn-ghost btn-xs"
            @click="copyRoomId"
          >
            复制
          </button>
        </div>
      </section>
    </template>

    <template v-else-if="currentRoom.status === 'playing'">
      <section
        class="rounded-md border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <div class="mb-6 flex items-center justify-between">
          <div class="flex-1 text-center">
            <div class="text-xs text-slate-400">你</div>
            <div class="text-2xl font-black text-emerald-600 dark:text-emerald-400">
              {{ myScore }}
            </div>
          </div>
          <div class="text-2xl font-black text-slate-300">VS</div>
          <div class="flex-1 text-center">
            <div class="text-xs text-slate-400">对手</div>
            <div class="text-2xl font-black text-orange-600 dark:text-orange-400">
              {{ opponentScore }}
            </div>
          </div>
        </div>
        <div class="mb-6 text-center">
          <div class="mb-2 text-xl font-black text-slate-950 dark:text-white">
            {{ currentStatement?.chinese || "加载中..." }}
          </div>
          <input
            v-model="battleAnswer"
            lang="ru"
            type="text"
            placeholder="输入俄语..."
            class="input input-bordered w-full max-w-md text-center text-lg"
            @keydown.enter="submitBattleAnswer"
            :disabled="answerSubmitted"
          />
        </div>
        <div class="flex justify-center gap-3">
          <button
            class="btn btn-primary"
            :disabled="!battleAnswer.trim() || answerSubmitted"
            @click="submitBattleAnswer"
          >
            {{ answerSubmitted ? "已提交" : "提交答案" }}
          </button>
        </div>
      </section>
    </template>

    <template v-else-if="currentRoom.status === 'finished'">
      <section
        class="rounded-md border p-6 text-center shadow-sm dark:border-slate-800 dark:bg-slate-900"
        :class="
          isWinner
            ? 'border-emerald-200 bg-emerald-50 dark:border-emerald-800 dark:bg-emerald-950'
            : 'border-slate-200 bg-white dark:border-slate-700 dark:bg-slate-900'
        "
      >
        <div class="mb-3 text-5xl">{{ isWinner ? "🏆" : isDraw ? "🤝" : "😢" }}</div>
        <h2
          class="mb-2 text-2xl font-black"
          :class="
            isWinner ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-950 dark:text-white'
          "
        >
          {{ isWinner ? "你赢了！" : isDraw ? "平局！" : "你输了" }}
        </h2>
        <div class="my-4 flex items-center justify-center gap-8">
          <div class="text-center">
            <div class="text-xs text-slate-400">你</div>
            <div class="text-3xl font-black text-emerald-600 dark:text-emerald-400">
              {{ currentRoom.creatorScore }}
            </div>
          </div>
          <div class="text-2xl text-slate-300">:</div>
          <div class="text-center">
            <div class="text-xs text-slate-400">对手</div>
            <div class="text-3xl font-black text-orange-600 dark:text-orange-400">
              {{ currentRoom.opponentScore }}
            </div>
          </div>
        </div>
        <button
          class="btn btn-primary mt-4"
          @click="resetBattle"
        >
          再来一局
        </button>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";

import {
  fetchBattleResult,
  fetchBattleStatus,
  fetchCreateBattleRoom,
  fetchJoinBattleRoom,
  fetchSubmitBattleScore,
} from "~/api/battle";
import { fetchCoursePacks } from "~/api/course-pack";
import { useUserStore } from "~/store/user";

const userStore = useUserStore();
const packs = ref<any[]>([]);
const selectedPack = ref("");
const joinRoomId = ref("");
const currentRoom = ref<Record<string, any>>({});
const currentStatement = ref<any>(null);
const battleAnswer = ref("");
const answerSubmitted = ref(false);
const myScore = ref(0);
const opponentScore = ref(0);
const pollTimer = ref<ReturnType<typeof setInterval> | null>(null);

const isWinner = computed(() => {
  if (!currentRoom.value.winner || currentRoom.value.winner === "draw") return false;
  return currentRoom.value.winner === userStore.user?.id;
});

const isDraw = computed(() => currentRoom.value.winner === "draw");

function copyRoomId() {
  if (currentRoom.value.roomId) {
    navigator.clipboard.writeText(currentRoom.value.roomId).catch(() => {});
  }
}

async function createRoom() {
  if (!selectedPack.value) return;
  try {
    const room = await fetchCreateBattleRoom(selectedPack.value);
    currentRoom.value = room;
    startPolling();
  } catch (e: any) {
    console.error(e);
  }
}

async function joinRoom() {
  if (!joinRoomId.value.trim()) return;
  try {
    const room = await fetchJoinBattleRoom(joinRoomId.value.trim());
    currentRoom.value = room;
    startPolling();
  } catch (e: any) {
    console.error(e);
  }
}

async function submitBattleAnswer() {
  if (!battleAnswer.value.trim() || answerSubmitted.value) return;
  answerSubmitted.value = true;
  const correct = (currentStatement.value?.english || "").trim().toLowerCase();
  const answer = battleAnswer.value.trim().toLowerCase();
  const isCorrect = sameSentence(correct, answer);
  const score = isCorrect ? 100 : 0;
  try {
    await fetchSubmitBattleScore(currentRoom.value.roomId, score);
    myScore.value = score;
  } catch (e: any) {
    console.error(e);
  }
}

function sameSentence(a: string, b: string): boolean {
  const norm = (s: string) =>
    s
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/\s+/g, " ")
      .trim();
  return norm(a) === norm(b);
}

function startPolling() {
  pollTimer.value = setInterval(async () => {
    if (!currentRoom.value.roomId) return;
    try {
      const status = await fetchBattleStatus(currentRoom.value.roomId);
      currentRoom.value = { ...currentRoom.value, ...status };
      if (status.creatorScore != null) opponentScore.value = status.creatorScore;
      if (status.opponentScore != null) opponentScore.value = status.opponentScore;
      if (status.status === "finished") {
        stopPolling();
        const result = await fetchBattleResult(currentRoom.value.roomId);
        currentRoom.value = { ...currentRoom.value, ...result };
      }
    } catch (e: any) {
      console.error(e);
    }
  }, 2000);
}

function stopPolling() {
  if (pollTimer.value) {
    clearInterval(pollTimer.value);
    pollTimer.value = null;
  }
}

function resetBattle() {
  stopPolling();
  currentRoom.value = {};
  battleAnswer.value = "";
  answerSubmitted.value = false;
  myScore.value = 0;
  opponentScore.value = 0;
}

onMounted(async () => {
  try {
    packs.value = await fetchCoursePacks();
  } catch (e: any) {
    console.error(e);
  }
});
</script>
