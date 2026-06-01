<template>
  <div class="w-full py-6">
    <section class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-sm font-bold text-emerald-600 dark:text-emerald-300">{{ $t('pages.vocabulary') }}</p>
          <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">生词本</h1>
          <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
            记录学习过程中遇到的生词，集中回顾和复习。
          </p>
        </div>
        <div class="rounded-md bg-slate-50 px-4 py-3 text-sm dark:bg-slate-950">
          <span class="font-black text-slate-950 dark:text-white">{{ words.length }}</span>
          <span class="ml-1 text-slate-500 dark:text-slate-400">个生词</span>
        </div>
      </div>
      <div class="mt-5">
        <div class="relative flex-1">
          <UIcon name="i-ph-magnifying-glass"
            class="pointer-events-none absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
          <input v-model="searchQuery" type="text" placeholder="搜索生词"
            class="h-11 w-full rounded-md border border-slate-200 bg-white pl-10 pr-3 text-sm outline-none transition focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-950" />
        </div>
      </div>
    </section>

    <section v-if="filteredWords.length" class="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
      <article v-for="item in filteredWords" :key="item.id"
        class="rounded-md border border-slate-200 bg-white p-4 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md dark:border-slate-800 dark:bg-slate-900">
        <div class="mb-2 flex items-start justify-between gap-2">
          <div>
            <button class="text-left text-lg font-black text-slate-950 hover:text-emerald-700 dark:text-white dark:hover:text-emerald-300"
              @click="playWord(item.word)">{{ item.word }}</button>
            <div v-if="item.chinese" class="mt-1 text-sm text-slate-500">{{ item.chinese }}</div>
          </div>
          <button
            class="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-md border border-slate-200 text-slate-500 transition hover:border-red-200 hover:bg-red-50 hover:text-red-600 dark:border-slate-700 dark:hover:border-red-900 dark:hover:bg-red-950"
            @click="removeWord(item)">
            <UTooltip text="移出生词本">
              <UIcon name="i-ph-trash" class="h-4 w-4" />
            </UTooltip>
          </button>
        </div>
        <div v-if="item.notes" class="mt-2 text-xs leading-5 text-slate-400">{{ item.notes }}</div>
        <div class="mt-2 text-xs text-slate-300">添加于 {{ formatDate(item.createdAt) }}</div>
      </article>
    </section>

    <section v-else
      class="rounded-md border border-dashed border-slate-300 bg-white p-10 text-center text-slate-500 dark:border-slate-700 dark:bg-slate-900">
      暂无生词，在练习中可以随时添加生词到生词本。
    </section>
  </div>
</template>

<script setup lang="ts">
import Fuse from "fuse.js";
import { computed, onMounted, ref } from "vue";

import { fetchVocabulary, fetchRemoveVocabulary, type VocabularyItem } from "~/api/learning";
import { playRussianText } from "~/composables/main/englishSound";

const words = ref<VocabularyItem[]>([]);
const searchQuery = ref("");

const fuse = computed(() => new Fuse(words.value, { keys: ["word", "chinese"], threshold: 0.4 }));
const filteredWords = computed(() => {
  if (!searchQuery.value) return words.value;
  return fuse.value.search(searchQuery.value).map((r) => r.item);
});

function playWord(text: string) { playRussianText(text); }

function formatDate(dateStr: string) {
  if (!dateStr) return "";
  return new Date(dateStr).toISOString().split("T")[0];
}

async function removeWord(item: VocabularyItem) {
  try {
    await fetchRemoveVocabulary(item.word);
    words.value = words.value.filter((w) => w.id !== item.id);
  } catch (e) { console.error(e); }
}

onMounted(async () => {
  try { words.value = await fetchVocabulary(); } catch (e) { console.error(e); }
});
</script>
