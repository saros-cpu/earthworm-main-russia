<template>
  <div class="space-y-6">
    <header>
      <h1 class="text-2xl font-black text-slate-950 dark:text-white">AI 工具</h1>
      <p class="mt-1 text-sm text-slate-500">AI 课程生成与主题搜索；词汇工具暂未启用</p>
    </header>

    <div class="grid gap-4 lg:grid-cols-2">
      <section class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <h2 class="mb-3 text-sm font-bold text-slate-900 dark:text-slate-100">选择课程包</h2>
        <select v-model="selectedPackId" class="select select-bordered select-sm w-full">
          <option value="">— 选择课程包 —</option>
          <option v-for="p in coursePacks" :key="p.id" :value="p.id" :disabled="p.archived">
            {{ p.title }}{{ p.archived ? "（已归档，无法生成）" : "" }}
          </option>
        </select>
        <p class="mt-1 text-xs text-slate-400">部分工具需要先选择一个课程包</p>
      </section>

      <section class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <h2 class="mb-3 text-sm font-bold text-slate-900 dark:text-slate-100">生成核心单词包</h2>
        <p class="rounded bg-slate-50 p-3 text-sm text-slate-500 dark:bg-slate-800 dark:text-slate-300">
          该能力尚未完成，现已停用，避免生成空课程包。
        </p>
      </section>
    </div>

    <div v-if="selectedPackId" class="grid gap-4 lg:grid-cols-2">
      <section class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <h2 class="mb-3 text-sm font-bold text-slate-900 dark:text-slate-100">AI 生成课程</h2>
        <input v-model="genForm.topic" class="input input-bordered input-sm mb-2 w-full" placeholder="主题，如：餐厅点餐、机场问路" />
        <div class="mb-2 grid grid-cols-2 gap-2">
          <select v-model="genForm.level" class="select select-bordered select-sm">
            <option value="beginner">beginner</option>
            <option value="elementary">elementary</option>
            <option value="intermediate">intermediate</option>
          </select>
          <input v-model.number="genForm.count" class="input input-bordered input-sm" type="number" min="4" max="20" />
        </div>
        <button class="btn btn-secondary btn-sm w-full" :disabled="isGeneratingCourse" @click="generateCourse">
          {{ isGeneratingCourse ? "生成中…" : "生成游戏化课程" }}
        </button>
        <div class="mt-3 space-y-2">
          <button class="btn btn-outline btn-sm w-full" :disabled="isSearching" @click="searchTopics(false)">
            {{ isSearching ? "搜索中…" : "搜索开放课程主题" }}
          </button>
          <button class="btn btn-outline btn-sm w-full" :disabled="isSearching" @click="searchTopics(true)">
            联网读取开放资源摘要
          </button>
        </div>
        <div v-if="topicSuggestions.length > 0" class="mt-3 space-y-2">
          <article v-for="s in topicSuggestions" :key="s.topic" class="rounded border border-slate-200 p-2 text-xs dark:border-slate-700">
            <div class="mb-1 flex items-center justify-between gap-2">
              <span class="font-medium text-slate-900 dark:text-slate-100">{{ s.topic }}</span>
              <span class="text-slate-400">{{ s.level }}</span>
            </div>
            <div class="mb-1 line-clamp-2 text-slate-500">{{ s.sourceNote }}</div>
            <div v-if="s.summary" class="mb-1 line-clamp-2 text-slate-500">摘要：{{ s.summary }}</div>
            <button class="btn btn-outline btn-xs w-full" @click="applySuggestion(s)">使用这个主题</button>
          </article>
        </div>
      </section>

      <section class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <h2 class="mb-3 text-sm font-bold text-slate-900 dark:text-slate-100">词汇工具</h2>
        <p class="rounded bg-slate-50 p-3 text-sm text-slate-500 dark:bg-slate-800 dark:text-slate-300">
          补充释义、刷新提示和重排课程暂未启用。
        </p>
      </section>
    </div>

    <section v-if="lastMessage" class="rounded-md border border-emerald-300 bg-emerald-50 p-3 text-sm text-emerald-900 dark:border-emerald-800 dark:bg-emerald-950 dark:text-emerald-100">
      {{ lastMessage }}
    </section>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: "admin", middleware: "admin" });

import { ref, onMounted } from "vue";
import { toast } from "vue-sonner";
import {
  fetchAdminCoursePacks,
  generateAdminCourse,
  fetchCourseTopicSuggestions,
  type AdminCoursePack,
  type CourseTopicSuggestion,
} from "~/api/admin";

const coursePacks = ref<AdminCoursePack[]>([]);
const selectedPackId = ref("");
const genForm = ref({ topic: "", level: "beginner", count: 12 });
const isGeneratingCourse = ref(false);
const isSearching = ref(false);
const topicSuggestions = ref<CourseTopicSuggestion[]>([]);
const lastMessage = ref("");

onMounted(async () => {
  coursePacks.value = await fetchAdminCoursePacks();
});

async function generateCourse() {
  if (!selectedPackId.value) { toast.error("请先选择课程包"); return; }
  if (!genForm.value.topic.trim()) { toast.error("请先填写课程主题"); return; }
  isGeneratingCourse.value = true;
  try {
    const course = await generateAdminCourse(selectedPackId.value, genForm.value);
    toast.success(`课程「${course.title}」已生成`);
    lastMessage.value = `课程已生成：${course.title}`;
  } catch (e: any) {
    toast.error(e?.data?.message || e?.message || "课程生成失败");
  } finally { isGeneratingCourse.value = false; }
}

async function searchTopics(online = false) {
  if (!genForm.value.topic.trim()) { toast.error("请先填写搜索关键词"); return; }
  isSearching.value = true;
  try {
    topicSuggestions.value = await fetchCourseTopicSuggestions(genForm.value.topic, online);
    if (topicSuggestions.value.length === 0) toast.info("暂无匹配主题，可换关键词");
  } catch (e: any) {
    toast.error(e?.data?.message || e?.message || "搜索失败");
  } finally { isSearching.value = false; }
}

function applySuggestion(s: CourseTopicSuggestion) {
  genForm.value.topic = s.topic;
  genForm.value.level = s.level;
  genForm.value.count = s.count;
  toast.success("已填入课程主题");
}

</script>
