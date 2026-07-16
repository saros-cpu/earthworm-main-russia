<template>
  <div class="w-full py-6">
    <section
      class="mb-6 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-black text-slate-950 dark:text-white">课程编辑器</h1>
          <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
            创建、编辑和分享你的俄语课程
          </p>
        </div>
        <div class="flex gap-2">
          <button
            @click="showImportModal = true"
            class="rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700"
          >
            导入
          </button>
          <button
            @click="exportJson"
            class="rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700"
          >
            导出 JSON
          </button>
          <button
            @click="publishCourse"
            class="rounded-lg bg-slate-950 px-4 py-1.5 text-xs font-bold text-white transition hover:bg-slate-800 dark:bg-white dark:text-slate-950 dark:hover:bg-slate-200"
          >
            发布
          </button>
        </div>
      </div>
    </section>

    <div class="grid gap-6 lg:grid-cols-3">
      <div class="lg:col-span-1">
        <div
          class="rounded-md border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-900"
        >
          <h3 class="mb-3 text-sm font-bold text-slate-600 dark:text-slate-300">课程信息</h3>
          <div class="mb-3">
            <label class="mb-1 block text-xs font-medium text-slate-500">标题</label>
            <input
              v-model="courseForm.title"
              placeholder="例如：俄语基础 30 句"
              class="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-950 placeholder-slate-400 focus:border-emerald-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-white dark:placeholder-slate-500"
            />
          </div>
          <div class="mb-3">
            <label class="mb-1 block text-xs font-medium text-slate-500">描述</label>
            <textarea
              v-model="courseForm.description"
              rows="3"
              placeholder="简短描述课程内容"
              class="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-950 placeholder-slate-400 focus:border-emerald-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-white dark:placeholder-slate-500"
            ></textarea>
          </div>
          <div class="mb-3">
            <label class="mb-1 block text-xs font-medium text-slate-500">标签（逗号分隔）</label>
            <input
              v-model="courseForm.tags"
              placeholder="入门, 日常, 场景"
              class="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-950 placeholder-slate-400 focus:border-emerald-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-white dark:placeholder-slate-500"
            />
          </div>
          <div class="text-xs text-slate-400">{{ statements.length }} 个句子</div>
        </div>
      </div>

      <div class="lg:col-span-2">
        <div
          class="rounded-md border border-slate-200 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-900"
        >
          <div
            class="flex items-center justify-between border-b border-slate-100 px-4 py-3 dark:border-slate-800"
          >
            <h3 class="text-sm font-bold text-slate-600 dark:text-slate-300">句子列表</h3>
            <button
              @click="addStatement"
              class="rounded-lg bg-emerald-500 px-3 py-1 text-xs font-bold text-white transition hover:bg-emerald-600"
            >
              + 添加句子
            </button>
          </div>
          <div class="divide-y divide-slate-100 dark:divide-slate-800">
            <div
              v-for="(stmt, idx) in statements"
              :key="idx"
              class="flex items-start gap-3 p-4 transition hover:bg-slate-50 dark:hover:bg-slate-800/50"
            >
              <div
                class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-slate-100 text-xs font-bold text-slate-500 dark:bg-slate-800 dark:text-slate-400"
              >
                {{ idx + 1 }}
              </div>
              <div class="flex-1">
                <input
                  v-model="stmt.russian"
                  placeholder="俄语句子"
                  class="mb-1 w-full rounded border-0 bg-transparent px-0 py-0.5 text-sm font-semibold text-slate-950 placeholder-slate-400 focus:outline-none focus:ring-0 dark:text-white"
                />
                <input
                  v-model="stmt.chinese"
                  placeholder="中文翻译"
                  class="w-full rounded border-0 bg-transparent px-0 py-0.5 text-sm text-slate-500 placeholder-slate-400 focus:outline-none focus:ring-0 dark:text-slate-400"
                />
              </div>
              <div class="flex gap-1">
                <button
                  @click="moveStatement(idx, -1)"
                  :disabled="idx === 0"
                  class="rounded p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600 disabled:opacity-30 dark:hover:bg-slate-800 dark:hover:text-slate-300"
                >
                  <UIcon
                    name="i-ph-arrow-up"
                    class="h-4 w-4"
                  />
                </button>
                <button
                  @click="moveStatement(idx, 1)"
                  :disabled="idx === statements.length - 1"
                  class="rounded p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600 disabled:opacity-30 dark:hover:bg-slate-800 dark:hover:text-slate-300"
                >
                  <UIcon
                    name="i-ph-arrow-down"
                    class="h-4 w-4"
                  />
                </button>
                <button
                  @click="removeStatement(idx)"
                  class="rounded p-1 text-red-400 transition hover:bg-red-50 hover:text-red-600 dark:hover:bg-red-900/30"
                >
                  <UIcon
                    name="i-ph-trash"
                    class="h-4 w-4"
                  />
                </button>
              </div>
            </div>
            <div
              v-if="statements.length === 0"
              class="p-8 text-center text-sm text-slate-400"
            >
              还没有句子。点击"添加句子"开始，或点击"导入"从 JSON / 文本批量导入。
            </div>
          </div>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="showImportModal"
          class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
          @click.self="showImportModal = false"
        >
          <div class="w-full max-w-lg rounded-xl bg-white p-6 shadow-2xl dark:bg-slate-900">
            <h3 class="mb-1 text-lg font-bold text-slate-950 dark:text-white">导入课程</h3>
            <p class="mb-4 text-sm text-slate-500 dark:text-slate-400">
              粘贴 JSON 或每行一组的文本（俄语\t中文）
            </p>
            <textarea
              v-model="importText"
              rows="8"
              placeholder='[{"russian":"Привет","chinese":"你好"}] 或&#10;Привет\t你好&#10;Спасибо\t谢谢'
              class="mb-4 w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-950 placeholder-slate-400 focus:border-emerald-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-white"
            ></textarea>
            <div class="flex justify-end gap-2">
              <button
                @click="showImportModal = false"
                class="rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300"
              >
                取消
              </button>
              <button
                @click="handleImport"
                class="rounded-lg bg-slate-950 px-4 py-2 text-sm font-bold text-white transition hover:bg-slate-800 dark:bg-white dark:text-slate-950"
              >
                导入
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";

interface EditorStatement {
  russian: string;
  chinese: string;
}

const courseForm = ref({
  title: "",
  description: "",
  tags: "",
});

const statements = ref<EditorStatement[]>([]);

const showImportModal = ref(false);
const importText = ref("");

function addStatement() {
  statements.value.push({ russian: "", chinese: "" });
}

function removeStatement(idx: number) {
  statements.value.splice(idx, 1);
}

function moveStatement(idx: number, dir: number) {
  const target = idx + dir;
  if (target < 0 || target >= statements.value.length) return;
  const tmp = statements.value[target];
  statements.value[target] = statements.value[idx];
  statements.value[idx] = tmp;
}

function handleImport() {
  const text = importText.value.trim();
  if (!text) return;

  try {
    const parsed = JSON.parse(text);
    if (Array.isArray(parsed)) {
      statements.value = parsed.map((item: any) => ({
        russian: item.russian || item.english || item.text || "",
        chinese: item.chinese || item.translation || item.meaning || "",
      }));
    }
    showImportModal.value = false;
    importText.value = "";
    return;
  } catch {}

  const lines = text.split("\n").filter(Boolean);
  const parsed = lines.map((line) => {
    const parts = line.split("\t");
    return { russian: parts[0]?.trim() || "", chinese: parts[1]?.trim() || "" };
  });
  statements.value = parsed;
  showImportModal.value = false;
  importText.value = "";
}

function exportJson() {
  const data = {
    title: courseForm.value.title,
    description: courseForm.value.description,
    tags: courseForm.value.tags
      .split(",")
      .map((t) => t.trim())
      .filter(Boolean),
    statements: statements.value,
  };
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `${courseForm.value.title || "course"}.json`;
  a.click();
  URL.revokeObjectURL(url);
}

async function publishCourse() {
  const { addCommunityCourse } = await import("~/api/courseMarket");
  const course = {
    id: "market-" + Date.now(),
    title: courseForm.value.title || "未命名课程",
    description: courseForm.value.description || "",
    author: "我",
    authorId: "user_self",
    statements: statements.value.map((s) => ({
      russian: s.russian,
      chinese: s.chinese,
    })),
    category: "community" as const,
    tags: courseForm.value.tags
      .split(",")
      .map((t) => t.trim())
      .filter(Boolean),
    downloads: 0,
    rating: 0,
    createdAt: new Date().toISOString().slice(0, 10),
    updatedAt: new Date().toISOString().slice(0, 10),
  };
  addCommunityCourse(course);
  alert("课程已发布到社区！");
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
