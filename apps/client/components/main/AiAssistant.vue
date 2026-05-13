<template>
  <div class="fixed bottom-6 right-6 z-50">
    <Transition name="chat-slide">
      <div v-if="open"
        class="mb-4 flex h-[420px] w-[340px] flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-2xl dark:border-slate-700 dark:bg-slate-900">
        <div class="flex items-center justify-between border-b border-slate-200 bg-emerald-50 px-4 py-3 dark:border-slate-700 dark:bg-emerald-950">
          <div class="flex items-center gap-2">
            <div class="flex h-7 w-7 items-center justify-center rounded-full bg-emerald-500 text-xs font-bold text-white">AI</div>
            <span class="text-sm font-bold text-slate-950 dark:text-white">俄语助手</span>
          </div>
          <button @click="open = false" class="text-slate-400 hover:text-slate-600">
            <UIcon name="i-ph-x" class="h-5 w-5" />
          </button>
        </div>

        <div ref="chatBox" class="flex-1 overflow-y-auto p-3 space-y-3">
          <div v-if="messages.length === 0" class="py-8 text-center text-xs text-slate-400">
            你好！我是俄语语法助手。<br/>可以问我关于变格、变位、词义等问题。
          </div>
          <div v-for="(msg, i) in messages" :key="i"
            class="flex" :class="msg.role === 'user' ? 'justify-end' : 'justify-start'">
            <div
              class="max-w-[80%] rounded-xl px-3 py-2 text-sm leading-relaxed"
              :class="msg.role === 'user'
                ? 'bg-emerald-500 text-white rounded-br-md'
                : 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-200 rounded-bl-md'">
              {{ msg.content }}
            </div>
          </div>
          <div v-if="loading" class="flex justify-start">
            <div class="rounded-xl bg-slate-100 px-3 py-2 text-sm text-slate-400 dark:bg-slate-800">
              <span class="animate-pulse">思考中...</span>
            </div>
          </div>
        </div>

        <div class="border-t border-slate-200 p-3 dark:border-slate-700">
          <div class="flex gap-2">
            <input v-model="question" type="text" placeholder="输入俄语语法问题..."
              class="h-9 flex-1 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-800"
              @keydown.enter="send" :disabled="loading" />
            <button @click="send" :disabled="loading || !question.trim()"
              class="inline-flex h-9 w-9 items-center justify-center rounded-md bg-emerald-500 text-white transition hover:bg-emerald-600 disabled:opacity-50">
              <UIcon name="i-ph-paper-plane-right" class="h-4 w-4" />
            </button>
          </div>
          <div v-if="questionCount > 0" class="mt-1 text-right text-[10px] text-slate-400">
            今日已用 {{ questionCount }} 次
          </div>
        </div>
      </div>
    </Transition>

    <button @click="open = !open"
      class="flex h-12 w-12 items-center justify-center rounded-full bg-emerald-500 text-white shadow-lg transition hover:bg-emerald-600 hover:shadow-xl active:scale-95">
      <UIcon v-if="!open" name="i-ph-chat-circle" class="h-6 w-6" />
      <UIcon v-else name="i-ph-x" class="h-6 w-6" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from "vue";
import { getHttp } from "~/api/http";

const open = ref(false);
const question = ref("");
const loading = ref(false);
const questionCount = ref(0);
const chatBox = ref<HTMLDivElement>();
const messages = ref<{ role: string; content: string }[]>([]);

async function send() {
  const q = question.value.trim();
  if (!q || loading.value) return;
  question.value = "";
  messages.value.push({ role: "user", content: q });
  loading.value = true;
  scrollToBottom();

  try {
    const http = getHttp();
    const res = await http<{ answer: string }>("/ai/ask", {
      method: "post",
      body: { question: q, statementId: null },
    });
    messages.value.push({ role: "assistant", content: res.answer });
    questionCount.value++;
  } catch (e: any) {
    messages.value.push({ role: "assistant", content: "AI 服务暂时不可用，请稍后重试。" });
  }
  loading.value = false;
  scrollToBottom();
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight;
  });
}

onMounted(() => {
  messages.value.push({ role: "assistant", content: "Привет! 👋 我是俄语语法助手，有什么问题尽管问我！" });
});
</script>

<style scoped>
.chat-slide-enter-active,
.chat-slide-leave-active {
  transition: all 0.3s ease;
}
.chat-slide-enter-from,
.chat-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}
</style>
