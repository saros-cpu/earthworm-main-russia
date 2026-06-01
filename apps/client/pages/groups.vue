<template>
  <div class="w-full py-6">
    <section class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-sm font-bold text-emerald-600 dark:text-emerald-300">{{ $t('pages.groups') }}</p>
          <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">学习小组</h1>
          <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">和学习伙伴一起打卡、互相激励。</p>
        </div>
        <button class="inline-flex h-11 items-center rounded-md bg-slate-950 px-4 text-sm font-bold text-white transition hover:bg-slate-800 dark:bg-white dark:text-slate-950"
          @click="showCreate = true">
          创建小组
        </button>
      </div>
    </section>

    <section v-if="showCreate" class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <h2 class="mb-3 text-lg font-bold text-slate-950 dark:text-white">创建新小组</h2>
      <div class="space-y-3">
        <input v-model="newGroupName" placeholder="小组名称" class="h-11 w-full rounded-md border border-slate-200 bg-white px-3 text-sm outline-none focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-800" />
        <textarea v-model="newGroupDesc" placeholder="小组介绍（可选）" rows="2" class="w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-800"></textarea>
        <div class="flex gap-2">
          <button class="rounded-md bg-emerald-600 px-4 py-2 text-sm font-bold text-white hover:bg-emerald-700" @click="createGroup">创建</button>
          <button class="rounded-md border border-slate-200 px-4 py-2 text-sm text-slate-600 hover:bg-slate-50" @click="showCreate = false">取消</button>
        </div>
      </div>
    </section>

    <section v-if="myGroups.length > 0" class="mb-5">
      <h2 class="mb-3 text-lg font-bold text-slate-950 dark:text-white">我的小组</h2>
      <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
        <article v-for="g in myGroups" :key="g.id"
          class="rounded-md border border-slate-200 bg-white p-4 shadow-sm hover:shadow-md dark:border-slate-800 dark:bg-slate-900">
          <div class="text-lg font-black text-slate-950 dark:text-white">{{ g.name }}</div>
          <div v-if="g.description" class="mt-1 text-sm text-slate-500 line-clamp-2">{{ g.description }}</div>
          <div class="mt-3 flex items-center justify-between text-xs text-slate-400">
            <span>{{ g.memberCount }} 位成员</span>
            <span>邀请码: <span class="font-mono font-bold text-emerald-600">{{ g.inviteCode }}</span></span>
          </div>
        </article>
      </div>
    </section>

    <section v-if="allGroups.length > 0">
      <h2 class="mb-3 text-lg font-bold text-slate-950 dark:text-white">发现小组</h2>
      <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
        <article v-for="g in allGroups" :key="g.id"
          class="rounded-md border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
          <div class="text-lg font-black text-slate-950 dark:text-white">{{ g.name }}</div>
          <div v-if="g.description" class="mt-1 text-sm text-slate-500 line-clamp-2">{{ g.description }}</div>
          <div class="mt-3 text-xs text-slate-400">{{ g.memberCount }} 位成员</div>
        </article>
      </div>
    </section>

    <section v-if="allGroups.length === 0 && myGroups.length === 0 && !showCreate"
      class="rounded-md border border-dashed border-slate-300 bg-white p-10 text-center text-slate-500 dark:border-slate-700 dark:bg-slate-900">
      还没有小组，创建一个开始学习之旅吧！
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { getHttp } from "~/api/http";

const showCreate = ref(false);
const newGroupName = ref("");
const newGroupDesc = ref("");
const myGroups = ref<any[]>([]);
const allGroups = ref<any[]>([]);

async function createGroup() {
  if (!newGroupName.value.trim()) return;
  try {
    const http = getHttp();
    const group = await http<any>("/groups", { method: "post", body: { name: newGroupName.value, description: newGroupDesc.value } });
    myGroups.value.unshift(group);
    newGroupName.value = "";
    newGroupDesc.value = "";
    showCreate.value = false;
  } catch (_) {}
}

async function loadGroups() {
  try {
    const http = getHttp();
    allGroups.value = await http<any[]>("/groups", { method: "get" });
    myGroups.value = await http<any[]>("/groups/my", { method: "get" });
  } catch (_) {}
}

onMounted(loadGroups);
</script>
