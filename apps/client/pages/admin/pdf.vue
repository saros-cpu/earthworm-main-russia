<template>
  <div class="space-y-6">
    <header>
      <h1 class="text-2xl font-black text-slate-950 dark:text-white">PDF 导入</h1>
      <p class="mt-1 text-sm text-slate-500">从 PDF 文件或本机目录快速生成俄语课程包</p>
    </header>

    <div class="grid gap-4 lg:grid-cols-2">
      <section
        class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 class="mb-3 text-sm font-bold text-slate-900 dark:text-slate-100">上传 PDF 文件</h2>
        <input
          v-model="pdfTitle"
          class="input input-sm input-bordered mb-3 w-full"
          placeholder="课程包名称（选填）"
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
          {{ isImportingPdf ? "上传中…" : "选择 PDF 文件" }}
        </button>
        <div
          v-if="activePdfJob"
          class="mt-3 rounded bg-emerald-50 p-2 text-xs text-emerald-800 dark:bg-emerald-950 dark:text-emerald-100"
        >
          <div class="mb-1 flex items-center justify-between gap-2">
            <span class="truncate">{{ activePdfJob.message }}</span>
            <span>{{ activePdfJob.progress }}%</span>
          </div>
          <progress
            class="progress progress-success w-full"
            :value="activePdfJob.progress"
            max="100"
          />
        </div>
      </section>

      <section
        class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 class="mb-3 text-sm font-bold text-slate-900 dark:text-slate-100">本机目录导入</h2>
        <input
          v-model="localPdfDirectory"
          class="input input-sm input-bordered mb-2 w-full"
          placeholder="本机 PDF 文件夹路径，如 D:\俄语教材"
        />
        <label class="mb-3 flex items-center gap-2 text-xs text-slate-500">
          <input
            v-model="localPdfRecursive"
            type="checkbox"
            class="checkbox checkbox-xs"
          />
          包含子文件夹
        </label>
        <button
          class="btn btn-outline btn-sm w-full"
          :disabled="isImportingLocalPdf"
          @click="createLocalPdfJobs"
        >
          {{ isImportingLocalPdf ? "创建任务中…" : "批量导入" }}
        </button>
      </section>
    </div>

    <section
      class="rounded-xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <div
        class="flex items-center justify-between border-b border-slate-200 px-5 py-4 dark:border-slate-800"
      >
        <h2 class="text-sm font-bold text-slate-900 dark:text-slate-100">最近导入任务</h2>
        <button
          class="btn btn-ghost btn-xs"
          @click="loadJobs"
        >
          刷新
        </button>
      </div>
      <div class="divide-y divide-slate-100 dark:divide-slate-800">
        <div
          v-for="job in pdfImportJobs"
          :key="job.jobId"
          class="flex items-center gap-4 px-5 py-3"
        >
          <div class="min-w-0 flex-1">
            <div class="flex items-center gap-2">
              <span class="truncate text-sm font-medium text-slate-900 dark:text-slate-100">{{
                job.title || job.filename || "未命名"
              }}</span>
              <span
                class="rounded px-1.5 py-0.5 text-xs font-bold"
                :class="jobStatusClass(job.status)"
                >{{ jobStatusLabel(job.status) }}</span
              >
            </div>
            <div class="text-xs text-slate-400">
              {{ job.message || job.errorMessage || job.jobId }}
            </div>
          </div>
          <div class="flex gap-2">
            <NuxtLink
              v-if="job.coursePackId"
              :to="`/admin/courses#${job.coursePackId}`"
              class="btn btn-outline btn-xs"
              >查看课程包</NuxtLink
            >
            <button
              class="btn btn-outline btn-error btn-xs"
              @click="removeJob(job.jobId)"
            >
              清理
            </button>
          </div>
        </div>
        <div
          v-if="pdfImportJobs.length === 0"
          class="px-5 py-10 text-center text-sm text-slate-400"
        >
          暂无导入记录
        </div>
      </div>
    </section>

    <section
      v-if="lastMessage"
      class="rounded-md border border-emerald-300 bg-emerald-50 p-3 text-sm text-emerald-900 dark:border-emerald-800 dark:bg-emerald-950 dark:text-emerald-100"
    >
      {{ lastMessage }}
    </section>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from "vue";
import { toast } from "vue-sonner";

import type { PdfImportJobStatus } from "~/api/course-pack";
import {
  createLocalPdfImportJobs,
  deleteAdminPdfImportJob,
  fetchAdminPdfImportJobs,
} from "~/api/admin";
import { createPdfImportJob, fetchPdfImportJob } from "~/api/course-pack";

definePageMeta({ layout: "admin", middleware: "admin" });

const pdfInputRef = ref<HTMLInputElement>();
const pdfTitle = ref("");
const localPdfDirectory = ref("");
const localPdfRecursive = ref(true);
const isImportingPdf = ref(false);
const isImportingLocalPdf = ref(false);
const activePdfJob = ref<PdfImportJobStatus | null>(null);
const pdfImportJobs = ref<PdfImportJobStatus[]>([]);
const lastMessage = ref("");
let pdfJobTimer: ReturnType<typeof setTimeout> | undefined;
const maxPdfSize = 120 * 1024 * 1024;

onMounted(() => {
  loadJobs();
});
onBeforeUnmount(() => {
  if (pdfJobTimer) clearTimeout(pdfJobTimer);
});

function jobStatusLabel(status: PdfImportJobStatus["status"]) {
  return { queued: "排队", running: "处理中", completed: "完成", failed: "失败" }[status];
}
function jobStatusClass(status: PdfImportJobStatus["status"]) {
  return {
    queued: "bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300",
    running: "bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-200",
    completed: "bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-200",
    failed: "bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-200",
  }[status];
}

async function loadJobs() {
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
    activePdfJob.value = { ...job, message: job.message || "已收到 PDF" };
    toast.success("PDF 已开始导入");
    pollJob(job.jobId);
  } catch (error: any) {
    isImportingPdf.value = false;
    toast.error(
      error?.data?.message ||
        error?.response?._data?.message ||
        error?.message ||
        "PDF 导入启动失败",
    );
  }
}

async function pollJob(jobId: string) {
  if (pdfJobTimer) clearTimeout(pdfJobTimer);
  const tick = async () => {
    try {
      const job = await fetchPdfImportJob(jobId);
      activePdfJob.value = job;
      if (job.status === "completed" && job.coursePackId) {
        isImportingPdf.value = false;
        clearTimeout(pdfJobTimer);
        await loadJobs();
        toast.success("课程包已生成");
        return;
      }
      if (job.status === "failed") {
        isImportingPdf.value = false;
        clearTimeout(pdfJobTimer);
        await loadJobs();
        toast.error(job.errorMessage || "PDF 导入失败");
        return;
      }
      pdfJobTimer = setTimeout(tick, 2000);
    } catch {
      isImportingPdf.value = false;
      clearTimeout(pdfJobTimer);
      toast.error("读取导入进度失败");
    }
  };
  await tick();
}

async function createLocalPdfJobs() {
  const dir = localPdfDirectory.value.trim();
  if (!dir) {
    toast.error("请先填写本机 PDF 文件夹路径");
    return;
  }
  isImportingLocalPdf.value = true;
  try {
    const result = await createLocalPdfImportJobs(dir, localPdfRecursive.value);
    await loadJobs();
    toast.success(`已创建 ${result.createdCount} 个导入任务`);
    if (result.skippedCount > 0) toast.warning(`${result.skippedCount} 个 PDF 未加入任务`);
  } catch (error: any) {
    toast.error(
      error?.data?.message ||
        error?.response?._data?.message ||
        error?.message ||
        "本机目录导入失败",
    );
  } finally {
    isImportingLocalPdf.value = false;
  }
}

async function removeJob(jobId: string) {
  if (!confirm("确认清理这条导入记录吗？课程包本身不会被删除。")) return;
  await deleteAdminPdfImportJob(jobId);
  await loadJobs();
  toast.success("记录已清理");
}
</script>
