<template>
  <div class="w-full py-6">
    <section
      class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-sm font-bold text-orange-600 dark:text-orange-400">Поединок</p>
          <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">PK 对战</h1>
          <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
            创建或加入房间，比拼答题得分！
          </p>
        </div>
      </div>
    </section>

    <div class="grid gap-5 md:grid-cols-2">
      <section
        class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 class="mb-4 text-lg font-bold text-slate-950 dark:text-white">创建房间</h2>
        <div class="space-y-3">
          <select
            v-model="selectedPack"
            class="h-11 w-full rounded-md border border-slate-200 bg-white px-3 text-sm outline-none focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-800"
          >
            <option value="">选择课程包</option>
            <option
              v-for="p in coursePacks"
              :key="p.id"
              :value="p.id"
            >
              {{ p.title }}
            </option>
          </select>
          <button
            @click="createRoom"
            :disabled="!selectedPack"
            class="h-11 w-full rounded-md bg-orange-500 text-sm font-bold text-white transition hover:bg-orange-600 disabled:opacity-50"
          >
            创建房间
          </button>
          <div
            v-if="createdRoom"
            class="rounded-md bg-slate-50 p-3 text-center dark:bg-slate-800"
          >
            <div class="text-xs text-slate-400">房间号</div>
            <div class="text-2xl font-black text-orange-600 dark:text-orange-400">
              {{ createdRoom }}
            </div>
            <div class="mt-2 text-xs text-slate-400">分享房间号给好友加入</div>
          </div>
        </div>
      </section>

      <section
        class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 class="mb-4 text-lg font-bold text-slate-950 dark:text-white">加入房间</h2>
        <div class="space-y-3">
          <input
            v-model="roomId"
            placeholder="输入房间号"
            class="h-11 w-full rounded-md border border-slate-200 bg-white px-3 text-sm outline-none focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-800"
          />
          <button
            @click="joinRoom"
            :disabled="!roomId.trim()"
            class="h-11 w-full rounded-md bg-emerald-600 text-sm font-bold text-white transition hover:bg-emerald-700 disabled:opacity-50"
          >
            加入
          </button>
        </div>
        <div
          v-if="joinedRoom"
          class="mt-3 rounded-md bg-emerald-50 p-3 text-center text-sm font-bold text-emerald-700 dark:bg-emerald-900 dark:text-emerald-300"
        >
          已加入房间 {{ joinedRoom }}！完成练习后提交分数查看结果。
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";

import type { AdminCoursePack } from "~/api/admin";
import { fetchAdminCoursePacks } from "~/api/admin";
import { getHttp } from "~/api/http";

const coursePacks = ref<AdminCoursePack[]>([]);
const selectedPack = ref("");
const createdRoom = ref("");
const roomId = ref("");
const joinedRoom = ref("");

async function createRoom() {
  if (!selectedPack.value) return;
  try {
    const http = getHttp();
    const res = await http<any>("/battle/create", {
      method: "post",
      body: { coursePackId: selectedPack.value },
    });
    createdRoom.value = res.roomId;
  } catch (_) {}
}

async function joinRoom() {
  if (!roomId.value.trim()) return;
  try {
    const http = getHttp();
    const res = await http<any>("/battle/join", {
      method: "post",
      body: { roomId: roomId.value.trim() },
    });
    if (res.error) {
      alert(res.error);
      return;
    }
    joinedRoom.value = roomId.value.trim();
  } catch (_) {
    alert("加入失败");
  }
}

onMounted(async () => {
  try {
    coursePacks.value = await fetchAdminCoursePacks();
  } catch (_) {}
});
</script>
