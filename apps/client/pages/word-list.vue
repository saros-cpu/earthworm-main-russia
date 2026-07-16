<template>
  <div class="w-full py-6">
    <section
      class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-sm font-bold text-emerald-600 dark:text-emerald-300">单词库</p>
          <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">单词列表</h1>
          <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
            浏览已收藏的单词，点击查看详情。
          </p>
        </div>
      </div>
      <div class="mt-5">
        <div class="relative flex-1">
          <UIcon
            name="i-ph-magnifying-glass"
            class="pointer-events-none absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400"
          />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索单词或中文释义"
            class="h-11 w-full rounded-md border border-slate-200 bg-white pl-10 pr-3 text-sm outline-none transition focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-950"
            @input="onSearch"
          />
        </div>
      </div>
    </section>

    <template v-if="isLoading">
      <div class="flex items-center justify-center py-20">
        <UIcon
          name="i-ph-spinner"
          class="h-8 w-8 animate-spin text-slate-400"
        />
      </div>
    </template>

    <template v-else-if="words.length === 0">
      <section
        class="rounded-md border border-dashed border-slate-300 bg-white p-10 text-center text-slate-500 dark:border-slate-700 dark:bg-slate-900"
      >
        {{
          searchQuery ? "没有找到匹配的单词" : "暂无收藏的单词，在练习中可以随时添加单词到生词本。"
        }}
      </section>
    </template>

    <template v-else>
      <div
        class="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <table class="w-full text-sm">
          <thead class="border-b border-slate-200 text-xs text-slate-500 dark:border-slate-700">
            <tr>
              <th class="px-5 py-3 text-left font-semibold">单词</th>
              <th class="px-5 py-3 text-left font-semibold">释义</th>
              <th class="hidden px-5 py-3 text-left font-semibold md:table-cell">词性</th>
              <th class="hidden px-5 py-3 text-left font-semibold lg:table-cell">音标</th>
              <th class="px-5 py-3 text-center font-semibold">学习程度</th>
              <th class="px-5 py-3 text-right font-semibold">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100 dark:divide-slate-800">
            <tr
              v-for="item in words"
              :key="item.id"
              class="cursor-pointer transition hover:bg-slate-50 dark:hover:bg-slate-800/50"
              @click="openDetail(item.id)"
            >
              <td class="px-5 py-3">
                <div class="flex items-center gap-2">
                  <span class="font-semibold text-slate-950 dark:text-white">{{ item.word }}</span>
                  <button
                    class="text-slate-300 transition hover:text-emerald-500"
                    @click.stop="playWord(item.word)"
                  >
                    <UIcon
                      name="i-ph-speaker-high"
                      class="h-4 w-4"
                    />
                  </button>
                </div>
              </td>
              <td class="px-5 py-3 text-slate-600 dark:text-slate-300">
                {{ item.chinese || "-" }}
              </td>
              <td class="hidden px-5 py-3 text-slate-500 md:table-cell">
                <span
                  v-if="item.partOfSpeech"
                  class="rounded bg-slate-100 px-1.5 py-0.5 text-xs dark:bg-slate-800"
                  >{{ item.partOfSpeech }}</span
                >
                <span v-else>-</span>
              </td>
              <td class="hidden px-5 py-3 font-mono text-slate-400 lg:table-cell">
                {{ item.phonetic || "-" }}
              </td>
              <td class="px-5 py-3 text-center">
                <div class="flex items-center justify-center gap-0.5">
                  <span
                    v-for="i in 5"
                    :key="i"
                    class="inline-block h-2 w-2 rounded-full"
                    :class="
                      i <= (item.studyLevel || 0)
                        ? 'bg-emerald-500'
                        : 'bg-slate-200 dark:bg-slate-700'
                    "
                  >
                  </span>
                </div>
              </td>
              <td class="px-5 py-3 text-right">
                <button
                  class="text-xs text-slate-400 transition hover:text-red-500"
                  @click.stop="removeWord(item)"
                >
                  移除
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div
        v-if="totalPages > 1"
        class="mt-5 flex items-center justify-center gap-2"
      >
        <button
          :disabled="currentPage === 0"
          class="rounded-md border border-slate-200 px-3 py-1.5 text-sm transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-30 dark:border-slate-700 dark:hover:bg-slate-800"
          @click="goToPage(currentPage - 1)"
        >
          上一页
        </button>
        <span class="text-sm text-slate-500">第 {{ currentPage + 1 }} / {{ totalPages }} 页</span>
        <button
          :disabled="currentPage >= totalPages - 1"
          class="rounded-md border border-slate-200 px-3 py-1.5 text-sm transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-30 dark:border-slate-700 dark:hover:bg-slate-800"
          @click="goToPage(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </template>

    <WordsWordDetailPopup
      :word-id="selectedWordId"
      @close="selectedWordId = null"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";

import type { WordItem } from "~/api/learning";
import { fetchDeleteWord, fetchWords } from "~/api/learning";
import { playRussianText } from "~/composables/main/englishSound";

const words = ref<WordItem[]>([]);
const searchQuery = ref("");
const currentPage = ref(0);
const totalPages = ref(0);
const totalElements = ref(0);
const isLoading = ref(false);
const selectedWordId = ref<string | null>(null);

const PAGE_SIZE = 15;
let searchTimer: ReturnType<typeof setTimeout> | null = null;

async function loadWords() {
  isLoading.value = true;
  try {
    const result = await fetchWords({
      page: currentPage.value,
      size: PAGE_SIZE,
      search: searchQuery.value || undefined,
    });
    words.value = result.content;
    totalPages.value = result.totalPages;
    totalElements.value = result.totalElements;
  } catch (e) {
    console.error(e);
  } finally {
    isLoading.value = false;
  }
}

function onSearch() {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    currentPage.value = 0;
    loadWords();
  }, 300);
}

function goToPage(page: number) {
  currentPage.value = page;
  loadWords();
}

function openDetail(id: string) {
  selectedWordId.value = id;
}

function playWord(text: string) {
  playRussianText(text);
}

async function removeWord(item: WordItem) {
  try {
    await fetchDeleteWord(item.id);
    words.value = words.value.filter((w) => w.id !== item.id);
  } catch (e) {
    console.error(e);
  }
}

onMounted(loadWords);
</script>
