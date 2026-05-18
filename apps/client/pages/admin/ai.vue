<template>
  <div class="space-y-6">
    <header>
      <h1 class="text-2xl font-black text-slate-950 dark:text-white">AI 工具</h1>
      <p class="mt-1 text-sm text-slate-500">AI 课程生成、词汇包管理、主题搜索</p>
    </header>

    <div class="grid gap-4 lg:grid-cols-2">
      <section
        class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 class="mb-3 text-sm font-bold text-slate-900 dark:text-slate-100">选择课程包</h2>
        <select
          v-model="selectedPackId"
          class="select select-bordered select-sm w-full"
        >
          <option value="">— 选择课程包 —</option>
          <option
            v-for="p in coursePacks"
            :key="p.id"
            :value="p.id"
          >
            {{ p.title }}
          </option>
        </select>
        <p class="mt-1 text-xs text-slate-400">部分工具需要先选择一个课程包</p>
      </section>

      <section
        class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 class="mb-3 text-sm font-bold text-slate-900 dark:text-slate-100">生成核心单词包</h2>
        <input
          v-model="vocabForm.title"
          class="input input-sm input-bordered mb-2 w-full"
          placeholder="课程包名称"
        />
        <input
          v-model.number="vocabForm.limit"
          class="input input-sm input-bordered mb-2 w-full"
          type="number"
          min="20"
          max="1000"
          placeholder="单词数量"
        />
        <button
          class="btn btn-accent btn-sm w-full"
          :disabled="isGeneratingVocab"
          @click="generateVocab"
        >
          {{ isGeneratingVocab ? "生成中…" : "生成核心单词包" }}
        </button>
      </section>
    </div>

    <div
      v-if="selectedPackId"
      class="grid gap-4 lg:grid-cols-2"
    >
      <section
        class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 class="mb-3 text-sm font-bold text-slate-900 dark:text-slate-100">AI 生成课程</h2>
        <input
          v-model="genForm.topic"
          class="input input-sm input-bordered mb-2 w-full"
          placeholder="主题，如：餐厅点餐、机场问路"
        />
        <div class="mb-2 grid grid-cols-2 gap-2">
          <select
            v-model="genForm.level"
            class="select select-bordered select-sm"
          >
            <option value="beginner">beginner</option>
            <option value="elementary">elementary</option>
            <option value="intermediate">intermediate</option>
          </select>
          <input
            v-model.number="genForm.count"
            class="input input-sm input-bordered"
            type="number"
            min="4"
            max="20"
          />
        </div>
        <button
          class="btn btn-secondary btn-sm w-full"
          :disabled="isGeneratingCourse"
          @click="generateCourse"
        >
          {{ isGeneratingCourse ? "生成中…" : "生成游戏化课程" }}
        </button>
        <div class="mt-3 space-y-2">
          <button
            class="btn btn-outline btn-sm w-full"
            :disabled="isSearching"
            @click="searchTopics(false)"
          >
            {{ isSearching ? "搜索中…" : "搜索开放课程主题" }}
          </button>
          <button
            class="btn btn-outline btn-sm w-full"
            :disabled="isSearching"
            @click="searchTopics(true)"
          >
            联网读取开放资源摘要
          </button>
        </div>
        <div
          v-if="topicSuggestions.length > 0"
          class="mt-3 space-y-2"
        >
          <article
            v-for="s in topicSuggestions"
            :key="s.topic"
            class="rounded border border-slate-200 p-2 text-xs dark:border-slate-700"
          >
            <div class="mb-1 flex items-center justify-between gap-2">
              <span class="font-medium text-slate-900 dark:text-slate-100">{{ s.topic }}</span>
              <span class="text-slate-400">{{ s.level }}</span>
            </div>
            <div class="mb-1 line-clamp-2 text-slate-500">{{ s.sourceNote }}</div>
            <div
              v-if="s.summary"
              class="mb-1 line-clamp-2 text-slate-500"
            >
              摘要：{{ s.summary }}
            </div>
            <button
              class="btn btn-outline btn-xs w-full"
              @click="applySuggestion(s)"
            >
              使用这个主题
            </button>
          </article>
        </div>
      </section>

      <section
        class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 class="mb-3 text-sm font-bold text-slate-900 dark:text-slate-100">词汇工具</h2>
        <p
          v-if="!isVocabPack"
          class="mb-3 text-xs text-slate-400"
        >
          仅单词课程包可用
        </p>
        <div class="space-y-2">
          <button
            class="btn btn-outline btn-sm w-full"
            :disabled="isEnriching || !isVocabPack"
            @click="enrich"
          >
            {{ isEnriching ? "补充中…" : "AI 补充单词释义" }}
          </button>
          <button
            class="btn btn-outline btn-sm w-full"
            :disabled="isRefreshing || !isVocabPack"
            @click="refreshPrompts"
          >
            {{ isRefreshing ? "刷新中…" : "刷新单词包提示" }}
          </button>
          <button
            class="btn btn-outline btn-sm w-full"
            :disabled="isOrganizing || !isVocabPack"
            @click="organize"
          >
            {{ isOrganizing ? "重排中…" : "按词性重排课程" }}
          </button>
        </div>
      </section>
    </div>

    <section
      v-if="lastMessage"
      class="rounded-md border border-emerald-300 bg-emerald-50 p-3 text-sm text-emerald-900 dark:border-emerald-800 dark:bg-emerald-950 dark:text-emerald-100"
    >
      {{ lastMessage }}
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { toast } from "vue-sonner";

import type { AdminCoursePack, CourseTopicSuggestion } from "~/api/admin";
import {
  enrichVocabulary,
  fetchAdminCoursePacks,
  fetchCourseTopicSuggestions,
  generateAdminCourse,
  generateVocabularyCoursePack,
  organizeVocabularyCourses,
  refreshVocabularyPrompts,
} from "~/api/admin";

definePageMeta({ layout: "admin", middleware: "admin" });

const coursePacks = ref<AdminCoursePack[]>([]);
const selectedPackId = ref("");
const genForm = ref({ topic: "", level: "beginner", count: 12 });
const vocabForm = ref({ title: "俄语核心单词课程包", limit: 400 });
const isGeneratingCourse = ref(false);
const isGeneratingVocab = ref(false);
const isEnriching = ref(false);
const isRefreshing = ref(false);
const isOrganizing = ref(false);
const isSearching = ref(false);
const topicSuggestions = ref<CourseTopicSuggestion[]>([]);
const lastMessage = ref("");

const selectedPack = computed(() => coursePacks.value.find((p) => p.id === selectedPackId.value));
const isVocabPack = computed(() => {
  if (!selectedPack.value) return false;
  const t = selectedPack.value.title.toLowerCase();
  return t.includes("单词") || t.includes("词汇") || t.includes("vocab");
});

onMounted(async () => {
  coursePacks.value = await fetchAdminCoursePacks();
});

async function generateCourse() {
  if (!selectedPackId.value) {
    toast.error("请先选择课程包");
    return;
  }
  if (!genForm.value.topic.trim()) {
    toast.error("请先填写课程主题");
    return;
  }
  isGeneratingCourse.value = true;
  try {
    const course = await generateAdminCourse(selectedPackId.value, genForm.value);
    toast.success(`课程「${course.title}」已生成`);
    lastMessage.value = `课程已生成：${course.title}`;
  } catch (e: any) {
    toast.error(e?.data?.message || e?.message || "课程生成失败");
  } finally {
    isGeneratingCourse.value = false;
  }
}

async function generateVocab() {
  isGeneratingVocab.value = true;
  try {
    const result = await generateVocabularyCoursePack(vocabForm.value);
    await fetchAdminCoursePacks();
    toast.success(`已生成 ${result.wordCount} 个核心单词`);
    lastMessage.value = `单词包已生成，包含 ${result.wordCount} 个单词`;
  } catch (e: any) {
    toast.error(e?.data?.message || e?.message || "单词包生成失败");
  } finally {
    isGeneratingVocab.value = false;
  }
}

async function searchTopics(online = false) {
  if (!genForm.value.topic.trim()) {
    toast.error("请先填写搜索关键词");
    return;
  }
  isSearching.value = true;
  try {
    topicSuggestions.value = await fetchCourseTopicSuggestions(genForm.value.topic, online);
    if (topicSuggestions.value.length === 0) toast.info("暂无匹配主题，可换关键词");
  } catch (e: any) {
    toast.error(e?.data?.message || e?.message || "搜索失败");
  } finally {
    isSearching.value = false;
  }
}

function applySuggestion(s: CourseTopicSuggestion) {
  genForm.value.topic = s.topic;
  genForm.value.level = s.level;
  genForm.value.count = s.count;
  toast.success("已填入课程主题");
}

async function enrich() {
  if (!selectedPackId.value) return;
  isEnriching.value = true;
  try {
    const r = await enrichVocabulary(selectedPackId.value, 40);
    toast.success(`已补充 ${r.enrichedCount} 个单词释义`);
  } catch (e: any) {
    toast.error(e?.data?.message || e?.message || "补充失败");
  } finally {
    isEnriching.value = false;
  }
}

async function refreshPrompts() {
  if (!selectedPackId.value) return;
  isRefreshing.value = true;
  try {
    const r = await refreshVocabularyPrompts(selectedPackId.value);
    toast.success(`已刷新 ${r.statementCount} 个提示，含 ${r.withMeaningCount} 个中文释义`);
  } catch (e: any) {
    toast.error(e?.data?.message || e?.message || "刷新失败");
  } finally {
    isRefreshing.value = false;
  }
}

async function organize() {
  if (!selectedPackId.value) return;
  if (!confirm("确认按词性和学习阶段重排吗？")) return;
  isOrganizing.value = true;
  try {
    const r = await organizeVocabularyCourses(selectedPackId.value);
    toast.success(`已重排为 ${r.courseCount} 个分层课程`);
  } catch (e: any) {
    toast.error(e?.data?.message || e?.message || "重排失败");
  } finally {
    isOrganizing.value = false;
  }
}
</script>
