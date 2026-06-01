<template>
  <div class="w-full py-6">
    <section class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-sm font-bold text-emerald-600 dark:text-emerald-300">{{ $t('pages.mastered') }}</p>
          <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">复习本</h1>
          <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
            这里收纳你已经掌握的俄语句子，适合回看、朗读和二次复习。
          </p>
        </div>
        <div class="rounded-md bg-slate-50 px-4 py-3 text-sm dark:bg-slate-950">
          <span class="font-black text-slate-950 dark:text-white">
            {{ masteredElementsStore.totalMasteredElementsCount }}
          </span>
          <span class="ml-1 text-slate-500 dark:text-slate-400">条已掌握</span>
        </div>
      </div>

      <div class="mt-5 flex flex-col gap-3 md:flex-row md:items-center">
        <div class="relative flex-1">
          <UIcon
            name="i-ph-magnifying-glass"
            class="pointer-events-none absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400"
          />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索俄语句子"
            class="h-11 w-full rounded-md border border-slate-200 bg-white pl-10 pr-3 text-sm outline-none transition focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-950"
          />
        </div>
        <NuxtLink
          to="/course-pack"
          class="inline-flex h-11 items-center justify-center rounded-md bg-slate-950 px-4 text-sm font-bold text-white transition hover:bg-slate-800 dark:bg-white dark:text-slate-950"
        >
          继续练习
        </NuxtLink>
      </div>
    </section>

    <section
      v-if="filteredItems.length"
      class="grid gap-3 md:grid-cols-2 xl:grid-cols-3"
    >
      <article
        v-for="item in filteredItems"
        :key="item.id"
        class="rounded-md border border-slate-200 bg-white p-4 shadow-sm transition hover:-translate-y-0.5 hover:border-emerald-300 hover:shadow-md dark:border-slate-800 dark:bg-slate-900"
      >
        <div class="mb-3 flex items-start justify-between gap-3">
          <div class="min-w-0">
            <div class="line-clamp-3 text-lg font-black leading-7 text-slate-950 dark:text-white">
              {{ item.content.targetText || item.content.english }}
            </div>
            <div class="mt-2 text-xs font-semibold text-slate-400">
              掌握于 {{ formatDate(item.masteredAt) }}
            </div>
          </div>
          <button
            class="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-md border border-slate-200 text-slate-500 transition hover:border-red-200 hover:bg-red-50 hover:text-red-600 dark:border-slate-700 dark:hover:border-red-900 dark:hover:bg-red-950"
            @click="removeItem(item)"
          >
            <UTooltip text="移出复习本">
              <UIcon
                name="i-ph-trash"
                class="h-5 w-5"
              />
            </UTooltip>
          </button>
        </div>
        <button
          class="inline-flex h-9 items-center gap-2 rounded-md border border-slate-200 px-3 text-sm font-bold text-slate-600 transition hover:border-emerald-300 hover:text-emerald-700 dark:border-slate-700 dark:text-slate-300"
          @click="playItem(item)"
        >
          <UIcon
            name="i-ph-speaker-high"
            class="h-4 w-4"
          />
          播放发音
        </button>
      </article>
    </section>

    <section
      v-else
      class="rounded-md border border-dashed border-slate-300 bg-white p-10 text-center text-slate-500 dark:border-slate-700 dark:bg-slate-900"
    >
      暂无复习内容，先完成几道练习吧。
    </section>
  </div>
</template>

<script setup lang="ts">
import Fuse from "fuse.js";
import { computed, onMounted, ref } from "vue";

import { playRussianText } from "~/composables/main/englishSound";
import { useMasteredElementsStore } from "~/store/masteredElements";

const masteredElementsStore = useMasteredElementsStore();
const searchQuery = ref("");

const fuse = computed(
  () =>
    new Fuse(masteredElementsStore.masteredElements, {
      keys: ["content.targetText", "content.english"],
      threshold: 0.4,
    }),
);

const filteredItems = computed(() => {
  if (!searchQuery.value) return masteredElementsStore.masteredElements;
  return fuse.value.search(searchQuery.value).map((result) => result.item);
});

function formatDate(dateString: string) {
  const date = new Date(dateString);
  return date.toISOString().split("T")[0];
}

function removeItem(item: any) {
  masteredElementsStore.removeElement(item.id + "");
}

function playItem(item: any) {
  playRussianText(item.content.targetText || item.content.english);
}

onMounted(async () => {
  masteredElementsStore.setup();
});
</script>
