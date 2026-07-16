<template>
  <Teleport to="body">
    <Transition name="popup-fade">
      <div
        v-if="open"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
        @click.self="open = false"
      >
        <div
          class="flex h-[560px] w-full max-w-lg flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-2xl dark:border-slate-700 dark:bg-slate-900"
        >
          <div
            class="flex items-center justify-between border-b border-slate-200 px-4 py-3 dark:border-slate-700"
          >
            <div class="flex items-center gap-2">
              <div
                class="flex h-7 w-7 items-center justify-center rounded-full bg-purple-500 text-xs font-bold text-white"
              >
                AI
              </div>
              <span class="text-sm font-bold text-slate-950 dark:text-white">{{
                scenario.title
              }}</span>
            </div>
            <button
              @click="open = false"
              class="text-slate-400 hover:text-slate-600"
            >
              <UIcon
                name="i-ph-x"
                class="h-5 w-5"
              />
            </button>
          </div>

          <div class="flex gap-2 border-b border-slate-100 px-4 py-2 dark:border-slate-800">
            <button
              v-for="s in scenarios"
              :key="s.id"
              class="rounded-full px-3 py-1 text-xs font-bold transition"
              :class="
                s.id === scenario.id
                  ? 'bg-purple-100 text-purple-700 dark:bg-purple-900 dark:text-purple-200'
                  : 'bg-slate-100 text-slate-500 hover:bg-slate-200 dark:bg-slate-800 dark:text-slate-300'
              "
              @click="selectScenario(s)"
            >
              {{ s.title }}
            </button>
          </div>

          <div
            ref="chatBox"
            class="flex-1 space-y-4 overflow-y-auto p-4"
          >
            <div
              v-if="messages.length === 0"
              class="py-6 text-center text-sm text-slate-400"
            >
              选择一个场景，开始俄语对话练习！
            </div>
            <div
              v-for="(msg, i) in messages"
              :key="i"
              class="flex"
              :class="msg.role === 'user' ? 'justify-end' : 'justify-start'"
            >
              <div
                class="max-w-[80%] rounded-2xl px-4 py-2.5 text-sm leading-relaxed shadow-sm"
                :class="
                  msg.role === 'user'
                    ? 'rounded-tr-md bg-purple-500 text-white'
                    : 'rounded-tl-md bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-200'
                "
              >
                <div
                  v-if="msg.role === 'assistant' && msg.russian"
                  class="mb-1 font-medium"
                >
                  {{ msg.russian }}
                </div>
                <div
                  v-if="msg.role === 'assistant' && msg.chinese"
                  class="text-xs text-slate-400 dark:text-slate-400"
                >
                  {{ msg.chinese }}
                </div>
                <div
                  v-if="msg.role === 'assistant' && msg.rating"
                  class="mt-1 text-[10px] font-bold text-emerald-600 dark:text-emerald-400"
                >
                  评分：{{ msg.rating }}/5
                  <span
                    v-if="msg.feedback"
                    class="ml-1 font-normal text-slate-400"
                    >· {{ msg.feedback }}</span
                  >
                </div>
                <div v-else-if="msg.role === 'user'">{{ msg.content }}</div>
              </div>
            </div>
            <div
              v-if="loading"
              class="flex justify-start"
            >
              <div
                class="rounded-2xl bg-slate-100 px-4 py-2.5 text-sm italic text-slate-400 dark:bg-slate-800"
              >
                {{ scenario.title === "自由对话" ? "你说：" : "对方正在说话..." }}
              </div>
            </div>
          </div>

          <div class="border-t border-slate-200 p-4 dark:border-slate-700">
            <div class="flex gap-2">
              <input
                v-model="userInput"
                type="text"
                :placeholder="loading ? '等待回复...' : '输入俄语回答...'"
                class="h-10 flex-1 rounded-lg border border-slate-200 bg-white px-4 text-sm outline-none transition focus:border-purple-400 dark:border-slate-700 dark:bg-slate-800"
                @keydown.enter="send"
                :disabled="loading"
              />
              <button
                @click="send"
                :disabled="loading || !userInput.trim()"
                class="inline-flex h-10 w-10 items-center justify-center rounded-lg bg-purple-500 text-white transition hover:bg-purple-600 disabled:opacity-50"
              >
                <UIcon
                  name="i-ph-paper-plane-right"
                  class="h-4 w-4"
                />
              </button>
            </div>
            <div class="mt-2 flex justify-between text-[10px] text-slate-400">
              <span>限额 {{ questionCount }}/{{ dailyLimit }}</span>
              <button
                v-if="messages.length > 0"
                @click="resetConversation"
                class="hover:text-purple-600"
              >
                重新开始
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { nextTick, ref } from "vue";

import { getHttp } from "~/api/http";

interface Scenario {
  id: string;
  title: string;
  prompt: string;
}

interface Message {
  role: "user" | "assistant";
  content?: string;
  russian?: string;
  chinese?: string;
  rating?: number;
  feedback?: string;
}

const props = withDefaults(defineProps<{ modelValue?: boolean }>(), { modelValue: false });
const emit = defineEmits<{ "update:modelValue": [boolean] }>();

const scenarios: Scenario[] = [
  {
    id: "restaurant",
    title: "餐厅点餐",
    prompt:
      "你是一家俄罗斯餐厅的服务员。你用俄语问候客人并询问要点什么。客人用俄语回答。每次只说1-2句话，简单自然。对话结束后请用中文总结。",
  },
  {
    id: "shopping",
    title: "购物",
    prompt: "你是俄罗斯商店的售货员。你用俄语打招呼并询问需要什么帮助。每次只说1-2句话。",
  },
  {
    id: "hotel",
    title: "酒店入住",
    prompt: "你是俄罗斯酒店的前台。你用俄语欢迎客人并办理入住手续。每次只说1-2句话。",
  },
  {
    id: "transport",
    title: "问路交通",
    prompt: "你是莫斯科街头的路人。有人用俄语向你问路。你用简单俄语回答。每次只说1-2句话。",
  },
  {
    id: "free",
    title: "自由对话",
    prompt:
      "你现在是俄语母语者，与我进行自由对话。我说一句中文你就翻译成俄语并回复。如果我的俄语有错误请纠正。每次只说1-2句话。",
  },
];

const open = ref(props.modelValue);
const userInput = ref("");
const loading = ref(false);
const questionCount = ref(0);
const dailyLimit = ref(20);
const chatBox = ref<HTMLDivElement>();
const messages = ref<Message[]>([]);
const scenario = ref<Scenario>(scenarios[0]);
const conversationHistory = ref<string[]>([]);

watch(
  () => props.modelValue,
  (v) => {
    open.value = v;
  },
);
watch(open, (v) => {
  emit("update:modelValue", v);
});

function selectScenario(s: Scenario) {
  scenario.value = s;
  resetConversation();
}

function resetConversation() {
  messages.value = [];
  conversationHistory.value = [];
  userInput.value = "";
}

async function send() {
  const text = userInput.value.trim();
  if (!text || loading.value) return;
  if (questionCount.value >= dailyLimit.value) return;

  userInput.value = "";
  messages.value.push({ role: "user", content: text });
  loading.value = true;
  scrollToBottom();

  try {
    const http = getHttp();
    const historyStr = conversationHistory.value.map((h) => `- ${h}`).join("\n");
    const isFirst = messages.value.length <= 1;
    const question = isFirst
      ? `${scenario.value.prompt}\n\n请开始对话，用俄语说出第一句，附中文翻译。`
      : `${scenario.value.prompt}\n\n当前对话历史：\n${historyStr}\n\n对方回复：${text}\n\n请继续用俄语对话，附中文翻译。如果对方俄语有语法错误，给出纠正建议并评分（1-5分）。`;

    const res = await http<{ answer: string }>("/ai/ask", { method: "post", body: { question } });
    const answer = res.answer;
    conversationHistory.value.push(`用户: ${text}`);
    conversationHistory.value.push(`AI: ${answer}`);

    const msg: Message = { role: "assistant" };
    const lines = answer.split("\n").filter(Boolean);
    msg.russian = lines[0] || answer;
    if (lines.length > 1)
      msg.chinese = lines
        .slice(1)
        .filter((l) => !/^\d/.test(l) && !/评分|纠正/i.test(l))
        .join(" ");
    const ratingMatch = answer.match(/评分[：:]\s*(\d+)/);
    if (ratingMatch) msg.rating = parseInt(ratingMatch[1]);
    const feedbackMatch = answer.match(/纠正[：:]\s*(.+?)(?:\n|$)/);
    if (feedbackMatch) msg.feedback = feedbackMatch[1];

    messages.value.push(msg);
    questionCount.value++;
  } catch {
    messages.value.push({
      role: "assistant",
      russian: "(AI 服务暂时不可用)",
      chinese: "请稍后重试",
    });
  }
  loading.value = false;
  scrollToBottom();
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight;
  });
}
</script>

<style scoped>
.popup-fade-enter-active,
.popup-fade-leave-active {
  transition: opacity 0.2s;
}
.popup-fade-enter-from,
.popup-fade-leave-to {
  opacity: 0;
}
</style>
