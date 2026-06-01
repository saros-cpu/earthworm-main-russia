<template>
  <div class="w-full py-6">
    <section class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <p class="text-sm font-bold text-emerald-600 dark:text-emerald-300">{{ $t('pages.feedback') }}</p>
      <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">建议反馈</h1>
      <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
        在网页留言，记录你的使用感受、想要的功能或遇到的 bug。留言会保存在当前浏览器本地。
      </p>
    </section>

    <section class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <label class="mb-2 block text-sm font-bold text-slate-700 dark:text-slate-200">写下你的想法</label>
      <textarea
        v-model="draft"
        rows="5"
        maxlength="800"
        placeholder="例如：希望增加 XX 教材课程包 / TORFL B2 想要更多例句…"
        class="w-full rounded-md border border-slate-200 bg-white p-3 text-sm leading-6 outline-none transition focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-950"
      ></textarea>
      <div class="mt-3 flex items-center justify-between gap-3">
        <span class="text-xs text-slate-400">{{ draft.length }} / 800</span>
        <button
          class="inline-flex h-10 items-center rounded-md bg-slate-950 px-5 text-sm font-bold text-white transition hover:bg-slate-800 disabled:opacity-50 dark:bg-white dark:text-slate-950"
          :disabled="!draft.trim()"
          @click="submit"
        >
          提交留言
        </button>
      </div>
    </section>

    <section class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <div class="mb-3 flex items-center justify-between">
        <h2 class="text-lg font-black text-slate-950 dark:text-white">我的留言</h2>
        <span class="text-xs text-slate-400">{{ messages.length }} 条</span>
      </div>
      <div v-if="messages.length === 0" class="rounded-md border border-dashed border-slate-200 p-6 text-center text-sm text-slate-400 dark:border-slate-700">
        还没有留言，写一条吧。
      </div>
      <ul v-else class="space-y-3">
        <li v-for="msg in messages" :key="msg.id"
          class="rounded-md border border-slate-100 bg-slate-50/60 p-4 dark:border-slate-800 dark:bg-slate-950/40">
          <div class="mb-2 flex items-center justify-between text-xs text-slate-400">
            <span>{{ formatTime(msg.createdAt) }}</span>
            <button class="text-slate-400 hover:text-red-500" @click="remove(msg.id)">删除</button>
          </div>
          <p class="whitespace-pre-wrap text-sm leading-7 text-slate-700 dark:text-slate-200">{{ msg.content }}</p>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { toast } from "vue-sonner";

interface FeedbackMessage {
  id: string;
  content: string;
  createdAt: number;
}

const STORAGE_KEY = "gusi-feedback-messages-v1";

const draft = ref("");
const messages = ref<FeedbackMessage[]>([]);

function load() {
  if (typeof window === "undefined") return;
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    messages.value = raw ? JSON.parse(raw) : [];
  } catch {
    messages.value = [];
  }
}

function persist() {
  if (typeof window === "undefined") return;
  localStorage.setItem(STORAGE_KEY, JSON.stringify(messages.value));
}

function submit() {
  const content = draft.value.trim();
  if (!content) return;
  messages.value.unshift({
    id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    content,
    createdAt: Date.now(),
  });
  persist();
  draft.value = "";
  toast.success("留言已保存到本机");
}

function remove(id: string) {
  messages.value = messages.value.filter((m) => m.id !== id);
  persist();
}

function formatTime(ts: number) {
  const d = new Date(ts);
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

onMounted(load);
</script>
