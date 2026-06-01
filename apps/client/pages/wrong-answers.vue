<template>
  <div class="w-full py-6">
    <section class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-sm font-bold text-red-600 dark:text-red-400">{{ $t('pages.wrongAnswers') }}</p>
          <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">错题本</h1>
          <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
            练习中答错的句子会自动记录到这里，集中攻克薄弱环节。
          </p>
        </div>
        <div class="rounded-md bg-slate-50 px-4 py-3 text-sm dark:bg-slate-950">
          <span class="font-black text-slate-950 dark:text-white">{{ items.length }}</span>
          <span class="ml-1 text-slate-500 dark:text-slate-400">道错题</span>
        </div>
      </div>
    </section>

    <section v-if="items.length > 0" class="space-y-3">
      <article v-for="item in items" :key="item.id"
        class="rounded-md border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div class="mb-2 flex items-start justify-between gap-3">
          <div class="min-w-0 flex-1">
            <div class="text-lg font-black text-slate-950 dark:text-white">{{ item.english }}</div>
            <div class="mt-1 text-sm text-slate-500 dark:text-slate-400">{{ item.chinese }}</div>
          </div>
          <button class="shrink-0 rounded-md bg-emerald-100 px-3 py-1.5 text-xs font-bold text-emerald-700 transition hover:bg-emerald-200 dark:bg-emerald-900 dark:text-emerald-300"
            @click="practiceItem(item)">
            练习
          </button>
        </div>
        <div class="flex items-center gap-3 text-xs text-slate-400">
          <span>错误 {{ item.wrongCount }} 次</span>
          <span>最后错误 {{ formatDate(item.lastWrongAt) }}</span>
        </div>
      </article>
    </section>

    <section v-else
      class="rounded-md border border-dashed border-slate-300 bg-white p-10 text-center text-slate-500 dark:border-slate-700 dark:bg-slate-900">
      暂无错题，继续练习吧！
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { getHttp } from "~/api/http";

interface WrongItem {
  id: string;
  statementId: string;
  coursePackId: string;
  courseId: string;
  english: string;
  chinese: string;
  wrongCount: number;
  lastWrongAt: string;
}

const router = useRouter();
const items = ref<WrongItem[]>([]);

function formatDate(d: string) {
  if (!d) return "";
  return new Date(d).toISOString().split("T")[0];
}

function practiceItem(item: WrongItem) {
  router.push(`/game/${item.coursePackId}/${item.courseId}`);
}

onMounted(async () => {
  try {
    const http = getHttp();
    const due = await http<any[]>("/reviews/due", { method: "get" });
    // Map review items to wrong answers with context
    const enriched = [];
    for (const r of due) {
      try {
        const course = await http<any>("/course-pack/" + r.coursePackId + "/courses/" + r.courseId, { method: "get" });
        const stmt = course.statements.find((s: any) => s.id === r.statementId);
        if (stmt) {
          enriched.push({
            id: r.id,
            statementId: r.statementId,
            coursePackId: r.coursePackId,
            courseId: r.courseId,
            english: stmt.english || stmt.targetText,
            chinese: stmt.chinese || stmt.sourceText,
            wrongCount: Math.max(0, 3 - r.repetitions),
            lastWrongAt: r.lastReviewedAt || r.nextReviewAt,
          });
        }
      } catch (_) {}
    }
    items.value = enriched.sort((a, b) => b.wrongCount - a.wrongCount);
  } catch (_) {}
});
</script>
