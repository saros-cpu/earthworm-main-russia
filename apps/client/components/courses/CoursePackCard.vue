<template>
  <article
    class="group flex h-full cursor-pointer flex-col overflow-hidden rounded-md border border-slate-200 bg-white shadow-sm transition hover:-translate-y-1 hover:border-emerald-300 hover:shadow-lg dark:border-slate-800 dark:bg-slate-900"
    @click="$emit('cardClick', coursePack)"
  >
    <figure class="relative aspect-[5/3] max-h-44 overflow-hidden bg-slate-100 dark:bg-slate-800">
      <NuxtImg
        :src="coursePack.cover"
        :alt="coursePack.title"
        preset="cover"
        class="h-full w-full object-cover transition duration-500 group-hover:scale-105"
      />
      <div class="absolute inset-x-0 bottom-0 bg-gradient-to-t from-slate-950/70 to-transparent p-3">
        <span class="rounded bg-white/90 px-2 py-1 text-xs font-bold text-slate-800">
          {{ coursePack.isFree ? "免费练习" : "会员课程" }}
        </span>
      </div>
    </figure>
    <div class="flex flex-1 flex-col p-4">
      <h2 class="line-clamp-2 min-h-12 text-lg font-black leading-6 text-slate-950 dark:text-white">
        {{ coursePack.title }}
      </h2>
      <p
        class="mt-3 line-clamp-3 flex-1 text-sm leading-6 text-slate-500 dark:text-slate-400"
        :title="coursePack.description"
      >
        {{ coursePack.description }}
      </p>
      <div class="mt-4 flex items-center justify-between border-t border-slate-100 pt-3 text-sm dark:border-slate-800">
        <span class="font-bold text-emerald-700 dark:text-emerald-300">开始闯关</span>
        <UIcon
          name="i-ph-arrow-right"
          class="h-5 w-5 text-slate-400 transition group-hover:translate-x-1 group-hover:text-emerald-600"
        />
      </div>
      <slot name="actions"></slot>
    </div>
  </article>
</template>

<script setup lang="ts">
interface Props {
  coursePack: {
    id: string;
    title: string;
    description: string;
    cover: string;
    isFree: boolean;
  };
}

defineProps<Props>();

defineEmits<{
  (e: "cardClick", coursePack: any): void;
}>();
</script>
