<template>
  <div class="mx-auto flex h-[calc(100vh-5rem)] w-full max-w-screen-2xl gap-4 overflow-hidden px-4 py-4">
    <aside class="w-72 overflow-y-auto border-r pr-4 dark:border-gray-800">
      <div class="mb-3 flex items-center justify-between">
        <h1 class="text-xl font-semibold">后台管理</h1>
        <button
          class="btn btn-ghost btn-xs"
          @click="loadCoursePacks"
        >
          刷新
        </button>
      </div>
      <div class="mb-4 space-y-2 rounded-md border p-3 dark:border-gray-800">
        <div class="text-sm font-semibold">PDF 生成课程包</div>
        <input
          v-model="pdfTitle"
          class="input input-bordered input-sm w-full"
          placeholder="课程包名称"
        />
        <input
          ref="pdfInputRef"
          type="file"
          accept="application/pdf,.pdf"
          class="hidden"
          @change="handlePdfSelected"
        />
        <button
          class="btn btn-primary btn-sm w-full"
          :disabled="isImportingPdf"
          @click="openPdfPicker"
        >
          {{ isImportingPdf ? "正在生成" : "上传 PDF" }}
        </button>
        <div class="border-t pt-2 dark:border-gray-800">
          <input
            v-model="localPdfDirectory"
            class="input input-bordered input-sm w-full"
            placeholder="本机 PDF 文件夹路径"
          />
          <label class="mt-2 flex items-center gap-2 text-xs text-gray-500">
            <input
              v-model="localPdfRecursive"
              type="checkbox"
              class="checkbox checkbox-xs"
            />
            包含子文件夹
          </label>
          <button
            class="btn btn-outline btn-sm mt-2 w-full"
            :disabled="isImportingLocalPdf"
            @click="createLocalPdfJobs"
          >
            {{ isImportingLocalPdf ? "正在创建任务" : "导入本机目录 PDF" }}
          </button>
        </div>
        <div
          v-if="activePdfJob"
          class="rounded bg-emerald-50 p-2 text-xs text-emerald-800 dark:bg-emerald-950 dark:text-emerald-100"
        >
          <div class="mb-1 flex items-center justify-between gap-2">
            <span class="truncate">{{ activePdfJob.message }}</span>
            <span>{{ activePdfJob.progress }}%</span>
          </div>
          <progress
            class="progress progress-success w-full"
            :value="activePdfJob.progress"
            max="100"
          ></progress>
        </div>
      </div>
      <div class="mb-4 space-y-2 rounded-md border p-3 dark:border-gray-800">
        <div class="flex items-center justify-between">
          <div class="text-sm font-semibold">最近导入任务</div>
          <button
            class="btn btn-ghost btn-xs"
            @click="loadPdfImportJobs"
          >
            刷新
          </button>
        </div>
        <div
          v-if="pdfImportJobs.length === 0"
          class="text-xs text-gray-500"
        >
          暂无导入记录
        </div>
        <article
          v-for="job in pdfImportJobs"
          :key="job.jobId"
          class="rounded border p-2 text-xs dark:border-gray-800"
        >
          <div class="mb-1 flex items-center justify-between gap-2">
            <span class="truncate font-medium">{{ job.title || job.filename || "未命名课件" }}</span>
            <span
              class="rounded px-1.5 py-0.5"
              :class="jobStatusClass(job.status)"
            >
              {{ jobStatusLabel(job.status) }}
            </span>
          </div>
          <div class="mb-2 text-gray-500">
            {{ job.message || job.errorMessage || job.jobId }}
          </div>
          <div class="flex gap-2">
            <button
              v-if="job.coursePackId"
              class="btn btn-outline btn-xs flex-1"
              @click="selectPack(job.coursePackId)"
            >
              查看课程包
            </button>
            <button
              class="btn btn-error btn-outline btn-xs"
              @click="removePdfImportJob(job.jobId)"
            >
              清理
            </button>
          </div>
        </article>
      </div>
      <div class="space-y-2">
        <button
          v-for="pack in coursePacks"
          :key="pack.id"
          class="w-full rounded-md border px-3 py-2 text-left text-sm transition hover:border-purple-400 dark:border-gray-800"
          :class="{ 'border-purple-500 bg-purple-50 dark:bg-purple-950': pack.id === selectedPack?.id }"
          @click="selectPack(pack.id)"
        >
          <div class="truncate font-medium">{{ pack.title }}</div>
          <div class="mt-1 flex items-center gap-2 text-xs text-gray-500">
            <span>{{ pack.shareLevel }}</span>
            <span>{{ pack.isFree ? "免费" : "付费" }}</span>
          </div>
        </button>
      </div>
    </aside>

    <main class="grid min-w-0 flex-1 grid-cols-[320px_minmax(0,1fr)] gap-4">
      <section class="overflow-y-auto border-r pr-4 dark:border-gray-800">
        <template v-if="selectedPack">
          <div class="mb-4 space-y-3 rounded-md border p-3 dark:border-gray-800">
            <input
              v-model="packForm.title"
              class="input input-bordered input-sm w-full"
              placeholder="课程包标题"
            />
            <textarea
              v-model="packForm.description"
              class="textarea textarea-bordered min-h-20 w-full text-sm"
              placeholder="课程包描述"
            ></textarea>
            <select
              v-model="packForm.shareLevel"
              class="select select-bordered select-sm w-full"
            >
              <option value="public">公开</option>
              <option value="private">私有</option>
            </select>
            <label class="flex items-center gap-2 text-sm">
              <input
                v-model="packForm.isFree"
                type="checkbox"
                class="checkbox checkbox-sm"
              />
              免费课程包
            </label>
            <button
              class="btn btn-primary btn-sm w-full"
              @click="savePack"
            >
              保存课程包
            </button>
          </div>
          <div class="mb-4 space-y-3 rounded-md border p-3 dark:border-gray-800">
            <div class="text-sm font-semibold">AI 生成课程</div>
            <input
              v-model="generationForm.topic"
              class="input input-bordered input-sm w-full"
              placeholder="主题，如：餐厅点餐、机场问路"
            />
            <div class="grid grid-cols-2 gap-2">
              <select
                v-model="generationForm.level"
                class="select select-bordered select-sm"
              >
                <option value="beginner">beginner</option>
                <option value="elementary">elementary</option>
                <option value="intermediate">intermediate</option>
              </select>
              <input
                v-model.number="generationForm.count"
                class="input input-bordered input-sm"
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
              {{ isGeneratingCourse ? "正在生成" : "生成游戏化课程" }}
            </button>
            <button
              class="btn btn-outline btn-sm w-full"
              :disabled="isSearchingTopics"
              @click="searchTopicSuggestions(false)"
            >
              {{ isSearchingTopics ? "正在搜索" : "搜索开放课程主题" }}
            </button>
            <button
              class="btn btn-outline btn-sm w-full"
              :disabled="isSearchingTopics"
              @click="searchTopicSuggestions(true)"
            >
              联网读取开放资源摘要
            </button>
            <div
              v-if="topicSuggestions.length > 0"
              class="space-y-2"
            >
              <article
                v-for="suggestion in topicSuggestions"
                :key="suggestion.topic"
                class="rounded border p-2 text-xs dark:border-gray-800"
              >
                <div class="mb-1 flex items-center justify-between gap-2">
                  <span class="font-medium">{{ suggestion.topic }}</span>
                  <span class="text-gray-500">{{ suggestion.level }}</span>
                </div>
                <div class="mb-2 line-clamp-2 text-gray-500">
                  {{ suggestion.sourceNote }}
                </div>
                <div
                  v-if="suggestion.summary"
                  class="mb-2 line-clamp-2 text-gray-500"
                >
                  摘要：{{ suggestion.summary }}
                </div>
                <div class="mb-2 truncate text-gray-500">
                  参考：{{ suggestion.sources.map((source) => source.title).join("、") || "主题库" }}
                </div>
                <button
                  class="btn btn-outline btn-xs w-full"
                  @click="applyTopicSuggestion(suggestion)"
                >
                  使用这个主题
                </button>
              </article>
            </div>
          </div>
          <div class="mb-4 space-y-3 rounded-md border p-3 dark:border-gray-800">
            <div class="text-sm font-semibold">单词课程包</div>
            <input
              v-model="vocabularyForm.title"
              class="input input-bordered input-sm w-full"
              placeholder="课程包名称"
            />
            <input
              v-model.number="vocabularyForm.limit"
              class="input input-bordered input-sm w-full"
              type="number"
              min="20"
              max="1000"
            />
            <button
              class="btn btn-accent btn-sm w-full"
              :disabled="isGeneratingVocabularyPack"
              @click="generateVocabularyPack"
            >
              {{ isGeneratingVocabularyPack ? "正在生成" : "生成核心单词包" }}
            </button>
            <button
              v-if="selectedPack?.title?.includes('单词') || selectedPack?.id?.startsWith('vocab-pack-')"
              class="btn btn-outline btn-sm w-full"
              :disabled="isEnrichingVocabulary"
              @click="enrichCurrentVocabulary"
            >
              {{ isEnrichingVocabulary ? "正在补充" : "AI 补充单词释义" }}
            </button>
            <button
              v-if="selectedPack?.title?.includes('单词') || selectedPack?.id?.startsWith('vocab-pack-')"
              class="btn btn-outline btn-sm w-full"
              :disabled="isRefreshingVocabularyPrompts"
              @click="refreshCurrentVocabularyPrompts"
            >
              {{ isRefreshingVocabularyPrompts ? "正在刷新" : "刷新当前单词包提示" }}
            </button>
            <button
              v-if="selectedPack?.title?.includes('单词') || selectedPack?.id?.startsWith('vocab-pack-')"
              class="btn btn-outline btn-sm w-full"
              :disabled="isOrganizingVocabulary"
              @click="organizeCurrentVocabularyCourses"
            >
              {{ isOrganizingVocabulary ? "正在分层" : "按词性重排课程" }}
            </button>
            <p class="text-xs leading-relaxed text-gray-500">
              单词包会优先使用中文释义出题；没有释义的词，会通过发音提示来练习俄语输入。
            </p>
          </div>

          <div class="mb-2 flex items-center justify-between">
            <h2 class="text-sm font-semibold text-gray-500">课程</h2>
            <button
              class="btn btn-outline btn-xs"
              @click="addCourse"
            >
              新增课程
            </button>
          </div>
          <div class="space-y-2">
            <button
              v-for="course in selectedPack.courses || []"
              :key="course.id"
              class="w-full rounded-md border px-3 py-2 text-left transition hover:border-purple-400 dark:border-gray-800"
              :class="{ 'border-purple-500 bg-purple-50 dark:bg-purple-950': course.id === selectedCourse?.id }"
              @click="selectCourse(course.id)"
            >
              <div class="truncate text-sm font-medium">{{ course.title }}</div>
              <div class="text-xs text-gray-500">{{ course.description }}</div>
            </button>
          </div>
        </template>
      </section>

      <section class="min-w-0 overflow-y-auto">
        <template v-if="selectedCourse">
          <div class="mb-4 grid grid-cols-2 gap-3 rounded-md border p-3 dark:border-gray-800">
            <input
              v-model="courseForm.title"
              class="input input-bordered input-sm"
              placeholder="课程标题"
            />
            <input
              v-model="courseForm.video"
              class="input input-bordered input-sm"
              placeholder="视频链接"
            />
            <textarea
              v-model="courseForm.description"
              class="textarea textarea-bordered col-span-2 min-h-16 text-sm"
              placeholder="课程描述"
            ></textarea>
            <button
              class="btn btn-primary btn-sm col-span-2"
              @click="saveCourse"
            >
              保存课程
            </button>
            <button
              class="btn btn-error btn-outline btn-sm col-span-2"
              @click="removeCourse"
            >
              删除课程
            </button>
          </div>

          <div class="mb-3 flex justify-end">
            <button
              class="btn btn-outline btn-sm"
              @click="addStatement"
            >
              新增句子
            </button>
          </div>

          <div class="space-y-3">
            <article
              v-for="statement in selectedCourse.statements || []"
              :key="statement.id"
              class="rounded-md border p-3 dark:border-gray-800"
            >
              <div class="mb-2 flex items-center justify-between">
                <span class="text-sm font-semibold">句子 {{ statement.order }}</span>
                <span class="rounded bg-gray-100 px-2 py-1 text-xs text-gray-600 dark:bg-gray-800 dark:text-gray-300">
                  {{ statement.refinementMode || "未精炼" }}
                </span>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <textarea
                  v-model="statement.sourceText"
                  class="textarea textarea-bordered min-h-20 text-sm"
                  placeholder="中文释义"
                ></textarea>
                <textarea
                  v-model="statement.targetText"
                  class="textarea textarea-bordered min-h-20 text-sm"
                  placeholder="俄语句子"
                ></textarea>
                <input
                  v-model="statement.phonetic"
                  class="input input-bordered input-sm"
                  placeholder="音标/读音提示"
                />
                <select
                  v-model="statement.difficulty"
                  class="select select-bordered select-sm"
                >
                  <option value="beginner">beginner</option>
                  <option value="elementary">elementary</option>
                  <option value="intermediate">intermediate</option>
                </select>
                <textarea
                  v-model="statement.grammarNote"
                  class="textarea textarea-bordered col-span-2 min-h-16 text-sm"
                  placeholder="语法提示"
                ></textarea>
                <textarea
                  :value="vocabularyText(statement)"
                  class="textarea textarea-bordered col-span-2 min-h-20 text-sm"
                  placeholder="词汇点，每行：单词：释义"
                  @input="updateVocabulary(statement, $event)"
                ></textarea>
              </div>
              <button
                class="btn btn-outline btn-sm mt-3"
                @click="saveStatement(statement)"
              >
                保存句子
              </button>
              <button
                class="btn btn-outline btn-sm ml-2 mt-3"
                @click="refineStatement(statement)"
              >
                重新 AI 精炼
              </button>
              <button
                class="btn btn-error btn-outline btn-sm ml-2 mt-3"
                @click="removeStatement(statement)"
              >
                删除
              </button>
            </article>
          </div>
        </template>
        <div
          v-else
          class="flex h-full items-center justify-center text-gray-500"
        >
          选择一个课程开始编辑
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { toast } from "vue-sonner";

import {
  createPdfImportJob,
  fetchPdfImportJob,
  type PdfImportJobStatus,
} from "~/api/course-pack";
import {
  createLocalPdfImportJobs,
  type AdminCourse,
  type AdminCoursePack,
  type AdminStatement,
  type CourseTopicSuggestion,
  createAdminCourse,
  createAdminStatement,
  deleteAdminCourse,
  deleteAdminPdfImportJob,
  deleteAdminStatement,
  enrichVocabulary,
  fetchAdminCourse,
  fetchAdminCoursePack,
  fetchAdminCoursePacks,
  fetchAdminPdfImportJobs,
  fetchCourseTopicSuggestions,
  generateAdminCourse,
  generateVocabularyCoursePack,
  organizeVocabularyCourses,
  refineAdminStatement,
  refreshVocabularyPrompts,
  updateAdminCourse,
  updateAdminCoursePack,
  updateAdminStatement,
} from "~/api/admin";

const coursePacks = ref<AdminCoursePack[]>([]);
const selectedPack = ref<AdminCoursePack>();
const selectedCourse = ref<AdminCourse>();
const packForm = reactive({ title: "", description: "", shareLevel: "public", isFree: true });
const courseForm = reactive({ title: "", description: "", video: "" });
const generationForm = reactive({ topic: "", level: "beginner", count: 12 });
const vocabularyForm = reactive({ title: "俄语核心单词课程包", limit: 400 });
const isGeneratingCourse = ref(false);
const isGeneratingVocabularyPack = ref(false);
const isEnrichingVocabulary = ref(false);
const isOrganizingVocabulary = ref(false);
const isRefreshingVocabularyPrompts = ref(false);
const isSearchingTopics = ref(false);
const topicSuggestions = ref<CourseTopicSuggestion[]>([]);
const pdfInputRef = ref<HTMLInputElement>();
const pdfTitle = ref("");
const localPdfDirectory = ref("");
const localPdfRecursive = ref(true);
const isImportingPdf = ref(false);
const isImportingLocalPdf = ref(false);
const activePdfJob = ref<PdfImportJobStatus | null>(null);
const pdfImportJobs = ref<PdfImportJobStatus[]>([]);
const maxPdfSize = 120 * 1024 * 1024;
let pdfJobTimer: ReturnType<typeof setTimeout> | undefined;

onMounted(async () => {
  await loadCoursePacks();
  await loadPdfImportJobs();
});

onBeforeUnmount(() => {
  stopPdfJobPolling();
});

async function loadCoursePacks() {
  coursePacks.value = await fetchAdminCoursePacks();
  if (!selectedPack.value && coursePacks.value.length > 0) {
    await selectPack(coursePacks.value[0].id);
  }
}

async function loadPdfImportJobs() {
  pdfImportJobs.value = await fetchAdminPdfImportJobs();
}

function openPdfPicker() {
  if (isImportingPdf.value) return;
  pdfInputRef.value?.click();
}

async function handlePdfSelected(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = "";

  if (!file) return;

  if (file.size > maxPdfSize) {
    toast.error("PDF 文件太大，请上传 120MB 以内的文件");
    return;
  }

  if (file.type && file.type !== "application/pdf" && !file.name.toLowerCase().endsWith(".pdf")) {
    toast.error("请上传 PDF 文件");
    return;
  }

  isImportingPdf.value = true;
  try {
    const job = await createPdfImportJob(file, pdfTitle.value);
    activePdfJob.value = {
      ...job,
      message: job.message || "已收到 PDF，正在准备生成课程包",
    };
    toast.success("PDF 已开始导入");
    pollPdfImportJob(job.jobId);
  } catch (error: any) {
    isImportingPdf.value = false;
    toast.error(
      error?.data?.message ||
        error?.response?._data?.message ||
        error?.message ||
        "PDF 导入启动失败，请确认文件可以正常读取",
    );
  }
}

async function createLocalPdfJobs() {
  const directory = localPdfDirectory.value.trim();
  if (!directory) {
    toast.error("请先填写本机 PDF 文件夹路径");
    return;
  }

  isImportingLocalPdf.value = true;
  try {
    const result = await createLocalPdfImportJobs(directory, localPdfRecursive.value);
    await loadPdfImportJobs();
    toast.success(`已创建 ${result.createdCount} 个导入任务`);
    if (result.skippedCount > 0) {
      toast.warning(`${result.skippedCount} 个 PDF 未加入任务，请检查文件大小或格式`);
    }
  } catch (error: any) {
    toast.error(
      error?.data?.message ||
        error?.response?._data?.message ||
        error?.message ||
        "本机目录导入任务创建失败",
    );
  } finally {
    isImportingLocalPdf.value = false;
  }
}

async function pollPdfImportJob(jobId: string) {
  stopPdfJobPolling();

  const loadJob = async () => {
    try {
      const job = await fetchPdfImportJob(jobId);
      activePdfJob.value = job;

      if (job.status === "completed" && job.coursePackId) {
        isImportingPdf.value = false;
        stopPdfJobPolling();
        await loadPdfImportJobs();
        await loadCoursePacks();
        await selectPack(job.coursePackId);
        toast.success("课程包已生成");
        return;
      }

      if (job.status === "failed") {
        isImportingPdf.value = false;
        stopPdfJobPolling();
        await loadPdfImportJobs();
        toast.error(job.errorMessage || "PDF 导入失败，请确认文件内容可识别");
        return;
      }

      pdfJobTimer = setTimeout(loadJob, 2000);
    } catch (error: any) {
      isImportingPdf.value = false;
      stopPdfJobPolling();
      toast.error(
        error?.data?.message ||
          error?.response?._data?.message ||
          error?.message ||
          "读取导入进度失败",
      );
    }
  };

  await loadJob();
}

function stopPdfJobPolling() {
  if (pdfJobTimer) {
    clearTimeout(pdfJobTimer);
    pdfJobTimer = undefined;
  }
}

async function removePdfImportJob(jobId: string) {
  if (!confirm("确认清理这条导入记录吗？课程包本身不会被删除。")) return;
  await deleteAdminPdfImportJob(jobId);
  await loadPdfImportJobs();
  toast.success("导入记录已清理");
}

function jobStatusLabel(status: PdfImportJobStatus["status"]) {
  return {
    queued: "排队",
    running: "处理中",
    completed: "完成",
    failed: "失败",
  }[status];
}

function jobStatusClass(status: PdfImportJobStatus["status"]) {
  return {
    queued: "bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300",
    running: "bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-200",
    completed: "bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-200",
    failed: "bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-200",
  }[status];
}

async function selectPack(id: string) {
  selectedPack.value = await fetchAdminCoursePack(id);
  packForm.title = selectedPack.value.title;
  packForm.description = selectedPack.value.description;
  packForm.shareLevel = selectedPack.value.shareLevel || "public";
  packForm.isFree = selectedPack.value.isFree;
  selectedCourse.value = undefined;
  const firstCourse = selectedPack.value.courses?.[0];
  if (firstCourse) {
    await selectCourse(firstCourse.id);
  }
}

async function selectCourse(id: string) {
  selectedCourse.value = await fetchAdminCourse(id);
  courseForm.title = selectedCourse.value.title;
  courseForm.description = selectedCourse.value.description;
  courseForm.video = selectedCourse.value.video || "";
}

async function savePack() {
  if (!selectedPack.value) return;
  selectedPack.value = await updateAdminCoursePack(selectedPack.value.id, packForm);
  await loadCoursePacks();
  toast.success("课程包已保存");
}

async function saveCourse() {
  if (!selectedCourse.value) return;
  selectedCourse.value = await updateAdminCourse(selectedCourse.value.id, courseForm);
  if (selectedPack.value) {
    await selectPack(selectedPack.value.id);
  }
  toast.success("课程已保存");
}

async function addCourse() {
  if (!selectedPack.value) return;
  const course = await createAdminCourse(selectedPack.value.id, {
    title: "新课程",
    description: "请填写课程描述",
  });
  await selectPack(selectedPack.value.id);
  await selectCourse(course.id);
  toast.success("课程已新增");
}

async function generateCourse() {
  if (!selectedPack.value) return;
  if (!generationForm.topic.trim()) {
    toast.error("请先填写课程主题");
    return;
  }
  isGeneratingCourse.value = true;
  try {
    const course = await generateAdminCourse(selectedPack.value.id, generationForm);
    await selectPack(selectedPack.value.id);
    await selectCourse(course.id);
    toast.success("课程已生成");
  } catch (error: any) {
    toast.error(
      error?.data?.message ||
        error?.response?._data?.message ||
        error?.message ||
        "课程生成失败",
    );
  } finally {
    isGeneratingCourse.value = false;
  }
}

async function generateVocabularyPack() {
  isGeneratingVocabularyPack.value = true;
  try {
    const result = await generateVocabularyCoursePack(vocabularyForm);
    await loadCoursePacks();
    await selectPack(result.coursePackId);
    toast.success(`已生成 ${result.wordCount} 个核心单词`);
  } catch (error: any) {
    toast.error(
      error?.data?.message ||
        error?.response?._data?.message ||
        error?.message ||
        "单词课程包生成失败",
    );
  } finally {
    isGeneratingVocabularyPack.value = false;
  }
}

async function refreshCurrentVocabularyPrompts() {
  if (!selectedPack.value) return;
  isRefreshingVocabularyPrompts.value = true;
  try {
    const result = await refreshVocabularyPrompts(selectedPack.value.id);
    await selectPack(selectedPack.value.id);
    toast.success(`已刷新 ${result.statementCount} 个单词提示，其中 ${result.withMeaningCount} 个带中文释义`);
  } catch (error: any) {
    toast.error(
      error?.data?.message ||
        error?.response?._data?.message ||
        error?.message ||
        "单词提示刷新失败",
    );
  } finally {
    isRefreshingVocabularyPrompts.value = false;
  }
}

async function enrichCurrentVocabulary() {
  if (!selectedPack.value) return;
  isEnrichingVocabulary.value = true;
  try {
    const result = await enrichVocabulary(selectedPack.value.id, 40);
    await selectPack(selectedPack.value.id);
    toast.success(`已补充 ${result.enrichedCount} 个单词释义`);
  } catch (error: any) {
    toast.error(
      error?.data?.message ||
        error?.response?._data?.message ||
        error?.message ||
        "单词释义补充失败",
    );
  } finally {
    isEnrichingVocabulary.value = false;
  }
}

async function organizeCurrentVocabularyCourses() {
  if (!selectedPack.value) return;
  if (!confirm("确认按词性和学习阶段重排当前单词包课程吗？题目和精炼内容会保留。")) return;
  isOrganizingVocabulary.value = true;
  try {
    const result = await organizeVocabularyCourses(selectedPack.value.id);
    await selectPack(selectedPack.value.id);
    toast.success(`已重排为 ${result.courseCount} 个分层课程`);
  } catch (error: any) {
    toast.error(
      error?.data?.message ||
        error?.response?._data?.message ||
        error?.message ||
        "单词课程分层失败",
    );
  } finally {
    isOrganizingVocabulary.value = false;
  }
}

async function searchTopicSuggestions(online = false) {
  isSearchingTopics.value = true;
  try {
    topicSuggestions.value = await fetchCourseTopicSuggestions(generationForm.topic, online);
    if (topicSuggestions.value.length === 0) {
      toast.info("暂时没有找到匹配主题，可以换个关键词");
    }
  } catch (error: any) {
    toast.error(
      error?.data?.message ||
        error?.response?._data?.message ||
        error?.message ||
        "课程主题搜索失败",
    );
  } finally {
    isSearchingTopics.value = false;
  }
}

function applyTopicSuggestion(suggestion: CourseTopicSuggestion) {
  generationForm.topic = suggestion.topic;
  generationForm.level = suggestion.level;
  generationForm.count = suggestion.count;
  toast.success("已填入课程主题");
}

async function removeCourse() {
  if (!selectedCourse.value || !selectedPack.value) return;
  if (!confirm(`确认删除课程「${selectedCourse.value.title}」吗？`)) return;
  await deleteAdminCourse(selectedCourse.value.id);
  await selectPack(selectedPack.value.id);
  toast.success("课程已删除");
}

async function addStatement() {
  if (!selectedCourse.value) return;
  await createAdminStatement(selectedCourse.value.id, {
    sourceText: "请填写中文释义",
    targetText: "Пожалуйста, заполните русский текст.",
    phonetic: "",
  });
  await selectCourse(selectedCourse.value.id);
  toast.success("句子已新增");
}

async function saveStatement(statement: AdminStatement) {
  const updated = await updateAdminStatement(statement.id, {
    sourceText: statement.sourceText,
    targetText: statement.targetText,
    phonetic: statement.phonetic,
    translation: statement.sourceText,
    vocabulary: statement.vocabulary || [],
    grammarNote: statement.grammarNote,
    difficulty: statement.difficulty,
  });
  Object.assign(statement, updated);
  toast.success("句子已保存");
}

async function refineStatement(statement: AdminStatement) {
  const updated = await refineAdminStatement(statement.id);
  Object.assign(statement, updated);
  toast.success(updated.refinementMode === "ai" ? "AI 精炼已完成" : "已使用规则精炼");
}

async function removeStatement(statement: AdminStatement) {
  if (!selectedCourse.value) return;
  if (!confirm(`确认删除第 ${statement.order} 句吗？`)) return;
  await deleteAdminStatement(statement.id);
  await selectCourse(selectedCourse.value.id);
  toast.success("句子已删除");
}

function vocabularyText(statement: AdminStatement) {
  return (statement.vocabulary || [])
    .map((item) => `${item.word}：${item.meaning}`)
    .join("\n");
}

function updateVocabulary(statement: AdminStatement, event: Event) {
  const value = (event.target as HTMLTextAreaElement).value;
  statement.vocabulary = value
    .split("\n")
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      const [word, ...meaning] = line.split(/[:：]/);
      return {
        word: word?.trim() || "",
        meaning: meaning.join("：").trim(),
      };
    })
    .filter((item) => item.word && item.meaning);
}
</script>
