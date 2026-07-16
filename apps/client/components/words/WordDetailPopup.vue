<template>
  <Teleport to="body">
    <Transition name="popup-fade">
      <div
        v-if="show"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
        @click.self="close"
      >
        <Transition
          name="popup-scale"
          appear
        >
          <div
            v-if="show"
            class="relative w-full max-w-lg rounded-xl border border-slate-200 bg-white p-6 shadow-xl dark:border-slate-700 dark:bg-slate-900"
          >
            <button
              class="absolute right-4 top-4 text-slate-400 hover:text-slate-600"
              @click="close"
            >
              <UIcon
                name="i-ph-x"
                class="h-5 w-5"
              />
            </button>
            <div
              v-if="word"
              class="space-y-4"
            >
              <div class="flex items-start justify-between gap-3">
                <div>
                  <h2 class="text-2xl font-black text-slate-950 dark:text-white">
                    {{ word.word }}
                  </h2>
                  <div class="mt-1 flex flex-wrap items-center gap-2 text-sm text-slate-500">
                    <span
                      v-if="word.partOfSpeech"
                      class="rounded-md bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300"
                      >{{ word.partOfSpeech }}</span
                    >
                    <span
                      v-if="word.phonetic"
                      class="font-mono text-slate-400"
                      >/{{ word.phonetic }}/</span
                    >
                  </div>
                </div>
                <button
                  class="shrink-0 rounded-md border border-slate-200 p-2 text-slate-400 transition hover:border-emerald-200 hover:bg-emerald-50 hover:text-emerald-600"
                  @click="playAudio"
                >
                  <UIcon
                    name="i-ph-speaker-high"
                    class="h-5 w-5"
                  />
                </button>
              </div>
              <p
                v-if="word.chinese"
                class="text-lg text-slate-700 dark:text-slate-200"
              >
                {{ word.chinese }}
              </p>
              <div
                v-if="word.exampleSentence"
                class="rounded-lg bg-slate-50 p-4 dark:bg-slate-800"
              >
                <p class="text-sm leading-relaxed text-slate-700 dark:text-slate-200">
                  {{ word.exampleSentence }}
                </p>
                <p
                  v-if="word.exampleTranslation"
                  class="mt-1 text-xs text-slate-400"
                >
                  {{ word.exampleTranslation }}
                </p>
              </div>
              <div
                v-if="word.notes"
                class="text-xs text-slate-400"
              >
                {{ word.notes }}
              </div>
              <div
                v-if="aiExamples.length > 0"
                class="rounded-lg border border-purple-100 bg-purple-50 p-4 dark:border-purple-900 dark:bg-purple-950"
              >
                <div
                  class="mb-2 flex items-center gap-1.5 text-xs font-semibold text-purple-600 dark:text-purple-300"
                >
                  <UIcon
                    name="i-ph-sparkle"
                    class="h-3.5 w-3.5"
                  />
                  AI 例句
                </div>
                <div
                  v-for="(ex, i) in aiExamples"
                  :key="i"
                  class="mb-2 last:mb-0"
                >
                  <p class="text-sm leading-relaxed text-slate-700 dark:text-slate-200">{{ ex }}</p>
                </div>
              </div>
              <div
                v-else-if="ai.loading"
                class="flex items-center gap-2 text-xs text-slate-400"
              >
                <UIcon
                  name="i-ph-spinner"
                  class="h-3.5 w-3.5 animate-spin"
                />
                正在生成例句...
              </div>
              <div
                class="flex items-center gap-2 border-t border-slate-100 pt-4 dark:border-slate-700"
              >
                <span class="text-xs font-medium text-slate-500">学习程度</span>
                <button
                  v-for="level in 5"
                  :key="level"
                  class="h-7 w-7 rounded-full text-xs font-bold transition"
                  :class="
                    level <= (word.studyLevel || 0)
                      ? 'bg-emerald-500 text-white'
                      : 'bg-slate-100 text-slate-400 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700'
                  "
                  @click="setStudyLevel(level)"
                >
                  {{ level }}
                </button>
              </div>
            </div>
            <div
              v-else
              class="flex items-center justify-center py-8"
            >
              <UIcon
                name="i-ph-spinner"
                class="h-6 w-6 animate-spin text-slate-400"
              />
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";

import type { WordItem } from "~/api/learning";
import { fetchAiExampleSentences } from "~/api/ai";
import { fetchUpdateWord, fetchWordById } from "~/api/learning";
import { playRussianText } from "~/composables/main/englishSound";

const props = defineProps<{ wordId: string | null }>();
const emit = defineEmits<{ close: [] }>();

const show = ref(false);
const word = ref<WordItem | null>(null);
const aiExamples = ref<string[]>([]);
const ai = ref({ loading: false, done: false });

watch(
  () => props.wordId,
  async (id) => {
    if (id) {
      show.value = true;
      word.value = null;
      aiExamples.value = [];
      ai.value = { loading: false, done: false };
      try {
        word.value = await fetchWordById(id);
        if (word.value) {
          ai.value.loading = true;
          fetchAiExampleSentences(word.value.word)
            .then((lines) => {
              aiExamples.value = lines.filter((l) => l.trim());
              ai.value.loading = false;
              ai.value.done = true;
            })
            .catch(() => {
              ai.value.loading = false;
            });
        }
      } catch (e) {
        console.error(e);
      }
    } else {
      show.value = false;
      word.value = null;
    }
  },
  { immediate: true },
);

function playAudio() {
  if (word.value) playRussianText(word.value.word);
}

async function setStudyLevel(level: number) {
  if (!word.value) return;
  try {
    word.value = await fetchUpdateWord(word.value.id, { studyLevel: level });
  } catch (e) {
    console.error(e);
  }
}

function close() {
  show.value = false;
  emit("close");
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
.popup-scale-enter-active {
  transition:
    transform 0.2s ease,
    opacity 0.2s ease;
}
.popup-scale-enter-from {
  transform: scale(0.9);
  opacity: 0;
}
</style>
