<template>
  <article
    :ref="isActiveCourse ? 'activeCourseRef' : undefined"
    :class="[
      'group relative flex min-h-[260px] w-full cursor-pointer flex-col rounded-md border bg-white p-4 shadow-sm transition hover:-translate-y-1 hover:shadow-lg dark:bg-slate-900',
      hasFinished
        ? 'border-emerald-300 dark:border-emerald-700'
        : isActiveCourse
          ? 'border-amber-300 dark:border-amber-600'
          : 'border-slate-200 hover:border-emerald-300 dark:border-slate-800 dark:hover:border-emerald-600',
    ]"
  >
    <div class="mb-3 flex items-center justify-between gap-3">
      <span
        :class="[
          'inline-flex h-8 min-w-8 items-center justify-center rounded-md px-2 text-xs font-black',
          hasFinished
            ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300'
            : isActiveCourse
              ? 'bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-300'
              : 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300',
        ]"
      >
        {{ lessonIndex }}
      </span>
      <span
        v-if="hasFinished || isActiveCourse"
        :class="[
          'rounded px-2 py-1 text-xs font-bold',
          hasFinished
            ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300'
            : 'bg-amber-50 text-amber-700 dark:bg-amber-950 dark:text-amber-300',
        ]"
      >
        {{ hasFinished ? `已完成 ${count} 次` : "继续学习" }}
      </span>
    </div>

    <div class="mb-2 flex flex-wrap items-center gap-2">
      <span
        v-if="categoryMeta"
        :class="[
          'inline-flex items-center gap-1 rounded px-2 py-1 text-xs font-bold',
          categoryMeta.className,
        ]"
      >
        <UIcon
          :name="categoryMeta.icon"
          class="h-3.5 w-3.5"
        />
        {{ categoryMeta.label }}
      </span>
      <span
        v-if="statementCount"
        class="rounded bg-slate-100 px-2 py-1 text-xs font-bold text-slate-600 dark:bg-slate-800 dark:text-slate-300"
      >
        {{ statementCount }} 词
      </span>
    </div>

    <h3 class="line-clamp-2 text-base font-black leading-6 text-slate-950 dark:text-white">
      {{ title }}
    </h3>
    <p
      class="mt-3 line-clamp-3 flex-1 text-sm leading-6 text-slate-500 dark:text-slate-400"
      :title="description"
    >
      {{ description }}
    </p>

    <div class="mt-4 flex items-center justify-between border-t border-slate-100 pt-3 text-sm dark:border-slate-800">
      <span class="font-bold text-slate-700 group-hover:text-emerald-700 dark:text-slate-200 dark:group-hover:text-emerald-300">
        进入练习
      </span>
      <UIcon
        name="i-ph-play-circle"
        class="h-5 w-5 text-slate-400 group-hover:text-emerald-600"
      />
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";

import { useActiveCourseMap } from "~/composables/courses/activeCourse";

const props = defineProps<{
  title: string;
  id: string;
  count: number | undefined;
  coursePackId: string;
  description: string;
  statementCount?: number;
}>();
const { activeCourseMap } = useActiveCourseMap();

const activeCourseRef = ref<HTMLDivElement>();
const hasFinished = computed(() => !!props.count);
const isActiveCourse = computed(() => activeCourseMap.value[props.coursePackId] == props.id);
const lessonIndex = computed(() => {
  const match = props.title.match(/第\s*(\d+)\s*课/);
  return match?.[1] ? `第 ${match[1]} 课` : "课程";
});

const categoryMeta = computed(() => categoryForTitle(props.title));

function categoryForTitle(title: string) {
  if (title.includes("代词")) {
    return {
      label: "代词",
      icon: "i-ph-user-focus",
      className: "bg-sky-50 text-sky-700 dark:bg-sky-950 dark:text-sky-200",
    };
  }
  if (title.includes("名词")) {
    return {
      label: "名词",
      icon: "i-ph-book-open-text",
      className: "bg-emerald-50 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-200",
    };
  }
  if (title.includes("动词")) {
    return {
      label: "动词",
      icon: "i-ph-lightning",
      className: "bg-amber-50 text-amber-700 dark:bg-amber-950 dark:text-amber-200",
    };
  }
  if (title.includes("形容词") || title.includes("副词")) {
    return {
      label: "修饰词",
      icon: "i-ph-palette",
      className: "bg-fuchsia-50 text-fuchsia-700 dark:bg-fuchsia-950 dark:text-fuchsia-200",
    };
  }
  if (title.includes("介词") || title.includes("连接词")) {
    return {
      label: "功能词",
      icon: "i-ph-link",
      className: "bg-indigo-50 text-indigo-700 dark:bg-indigo-950 dark:text-indigo-200",
    };
  }
  if (title.includes("待补")) {
    return {
      label: "待精炼",
      icon: "i-ph-hourglass-medium",
      className: "bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300",
    };
  }
}

onMounted(() => {
  activeCourseRef.value?.scrollIntoView({
    behavior: "smooth",
    block: "start",
  });
});
</script>
