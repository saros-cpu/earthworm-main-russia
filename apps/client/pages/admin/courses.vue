<template>
  <div class="space-y-4">
    <!-- 工具栏 -->
    <div class="flex flex-wrap items-center gap-3">
      <select
        v-model="selectedPackId"
        class="select select-bordered select-sm max-w-xs"
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
      <template v-if="selectedPack">
        <select
          v-model="selectedCourseId"
          class="select select-bordered select-sm max-w-xs"
        >
          <option value="">— 选择课程 —</option>
          <option
            v-for="c in selectedPack.courses || []"
            :key="c.id"
            :value="c.id"
          >
            {{ c.title }}
          </option>
        </select>
        <button
          class="btn btn-outline btn-xs"
          @click="addCourse"
        >
          新增课程
        </button>
      </template>
      <button
        class="btn btn-ghost btn-xs"
        @click="loadPacks"
      >
        刷新
      </button>
    </div>

    <!-- 课程包设置（折叠） -->
    <section
      v-if="selectedPack"
      class="rounded-xl border border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900"
    >
      <button
        class="flex w-full items-center justify-between px-4 py-3 text-left"
        @click="showPackSettings = !showPackSettings"
      >
        <span class="text-xs font-bold uppercase tracking-wider text-slate-500">课程包设置</span>
        <UIcon
          :name="showPackSettings ? 'i-ph-caret-up' : 'i-ph-caret-down'"
          class="h-4 w-4 text-slate-400"
        />
      </button>
      <div
        v-if="showPackSettings"
        class="border-t border-slate-200 px-4 pb-4 pt-3 dark:border-slate-800"
      >
        <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <input
            v-model="packForm.title"
            class="input input-sm input-bordered"
            placeholder="标题"
          />
          <select
            v-model="packForm.shareLevel"
            class="select select-bordered select-sm"
          >
            <option value="public">公开</option>
            <option value="private">私有</option>
          </select>
          <textarea
            v-model="packForm.description"
            class="textarea textarea-bordered col-span-full min-h-14 text-sm"
            placeholder="描述"
          />
          <div class="col-span-full flex items-center gap-4">
            <label class="flex items-center gap-2 text-xs">
              <input
                v-model="packForm.isFree"
                type="checkbox"
                class="checkbox checkbox-xs"
              />
              免费课程包
            </label>
            <button
              class="btn btn-primary btn-xs"
              @click="savePack"
            >
              保存
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 课程编辑器 -->
    <section
      v-if="selectedCourse"
      class="rounded-xl border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
    >
      <h3 class="mb-3 text-xs font-bold uppercase tracking-wider text-slate-500">课程设置</h3>
      <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <input
          v-model="courseForm.title"
          class="input input-sm input-bordered"
          placeholder="标题"
        />
        <input
          v-model="courseForm.video"
          class="input input-sm input-bordered"
          placeholder="媒体路径"
        />
        <textarea
          v-model="courseForm.description"
          class="textarea textarea-bordered col-span-full min-h-12 text-sm"
          placeholder="描述"
        />
        <div class="col-span-full flex gap-2">
          <button
            class="btn btn-primary btn-xs"
            @click="saveCourse"
          >
            保存
          </button>
          <button
            class="btn btn-outline btn-error btn-xs"
            @click="removeCourse"
          >
            删除
          </button>
        </div>
      </div>
    </section>

    <!-- 句列表 -->
    <div
      v-if="selectedCourse"
      class="flex items-center justify-between"
    >
      <h3 class="text-xs font-bold uppercase tracking-wider text-slate-500">
        句子 ({{ (selectedCourse.statements || []).length }})
      </h3>
      <button
        class="btn btn-outline btn-xs"
        @click="addStatement"
      >
        新增句子
      </button>
    </div>

    <div
      v-if="selectedCourse"
      class="space-y-3"
    >
      <article
        v-for="s in selectedCourse.statements || []"
        :key="s.id"
        class="rounded-xl border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
      >
        <div class="mb-3 flex items-center justify-between">
          <span class="text-sm font-bold text-slate-900 dark:text-slate-100">#{{ s.order }}</span>
          <div class="flex items-center gap-2">
            <span
              class="rounded bg-slate-100 px-2 py-0.5 text-[10px] text-slate-500 dark:bg-slate-800 dark:text-slate-300"
              >{{ s.refinementMode || "未精炼" }}</span
            >
            <button
              class="rounded p-1 text-slate-400 hover:text-emerald-600"
              title="AI 精炼"
              @click="refineStatement(s)"
            >
              <UIcon
                name="i-ph-sparkle"
                class="h-3.5 w-3.5"
              />
            </button>
            <button
              class="rounded p-1 text-slate-400 hover:text-red-600"
              title="删除"
              @click="removeStatement(s)"
            >
              <UIcon
                name="i-ph-trash"
                class="h-3.5 w-3.5"
              />
            </button>
          </div>
        </div>
        <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <textarea
            v-model="s.sourceText"
            class="textarea textarea-bordered min-h-14 text-sm"
            placeholder="中文释义"
          />
          <textarea
            v-model="s.targetText"
            class="textarea textarea-bordered min-h-14 text-sm"
            placeholder="俄语句子"
          />
          <input
            v-model="s.phonetic"
            class="input input-sm input-bordered"
            placeholder="音标"
          />
          <select
            v-model="s.difficulty"
            class="select select-bordered select-sm"
          >
            <option value="beginner">beginner</option>
            <option value="elementary">elementary</option>
            <option value="intermediate">intermediate</option>
          </select>
          <textarea
            v-model="s.grammarNote"
            class="textarea textarea-bordered col-span-full min-h-12 text-sm"
            placeholder="语法提示"
          />
          <textarea
            :value="vocabText(s)"
            class="textarea textarea-bordered col-span-full min-h-14 text-sm"
            placeholder="词汇点，每行：单词：释义"
            @input="updateVocab(s, $event)"
          />
        </div>
        <button
          class="btn btn-outline btn-xs mt-3"
          @click="saveStatement(s)"
        >
          保存句子
        </button>
      </article>
      <div
        v-if="(selectedCourse.statements || []).length === 0"
        class="py-10 text-center text-sm text-slate-400"
      >
        暂无句子
      </div>
    </div>

    <div
      v-if="selectedPack && !selectedCourse"
      class="py-10 text-center text-sm text-slate-400"
    >
      选择一个课程开始编辑
    </div>
    <div
      v-if="!selectedPack"
      class="py-10 text-center text-sm text-slate-400"
    >
      选择课程包
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from "vue";
import { toast } from "vue-sonner";

import type { AdminCourse, AdminCoursePack, AdminStatement } from "~/api/admin";
import {
  createAdminCourse,
  createAdminStatement,
  deleteAdminCourse,
  deleteAdminStatement,
  fetchAdminCourse,
  fetchAdminCoursePack,
  fetchAdminCoursePacks,
  refineAdminStatement,
  updateAdminCourse,
  updateAdminCoursePack,
  updateAdminStatement,
} from "~/api/admin";

definePageMeta({ layout: "admin", middleware: "admin" });

const coursePacks = ref<AdminCoursePack[]>([]);
const selectedPackId = ref("");
const selectedCourseId = ref("");
const selectedPack = ref<AdminCoursePack>();
const selectedCourse = ref<AdminCourse>();
const showPackSettings = ref(true);
const packForm = reactive({ title: "", description: "", shareLevel: "public", isFree: true });
const courseForm = reactive({ title: "", description: "", video: "" });

watch(selectedPackId, async (id) => {
  selectedCourse.value = undefined;
  selectedCourseId.value = "";
  if (id) await selectPack(id);
  else selectedPack.value = undefined;
});

watch(selectedCourseId, async (id) => {
  if (id) await selectCourse(id);
  else selectedCourse.value = undefined;
});

async function loadPacks() {
  coursePacks.value = await fetchAdminCoursePacks();
  if (coursePacks.value.length > 0 && !selectedPackId.value) {
    selectedPackId.value = coursePacks.value[0].id;
  }
}

async function selectPack(id: string) {
  selectedPack.value = await fetchAdminCoursePack(id);
  packForm.title = selectedPack.value.title;
  packForm.description = selectedPack.value.description;
  packForm.shareLevel = selectedPack.value.shareLevel || "public";
  packForm.isFree = selectedPack.value.isFree;
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
  coursePacks.value = await fetchAdminCoursePacks();
  toast.success("课程包已保存");
}

async function addCourse() {
  if (!selectedPack.value) return;
  const c = await createAdminCourse(selectedPack.value.id, {
    title: "新课程",
    description: "请填写课程描述",
  });
  await selectPack(selectedPack.value.id);
  await selectCourse(c.id);
  toast.success("课程已新增");
}

async function saveCourse() {
  if (!selectedCourse.value) return;
  selectedCourse.value = await updateAdminCourse(selectedCourse.value.id, courseForm);
  if (selectedPack.value) await selectPack(selectedPack.value.id);
  toast.success("课程已保存");
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

async function saveStatement(s: AdminStatement) {
  const updated = await updateAdminStatement(s.id, {
    sourceText: s.sourceText,
    targetText: s.targetText,
    phonetic: s.phonetic,
    translation: s.sourceText,
    vocabulary: s.vocabulary || [],
    grammarNote: s.grammarNote,
    difficulty: s.difficulty,
  });
  Object.assign(s, updated);
  toast.success("句子已保存");
}

async function refineStatement(s: AdminStatement) {
  const updated = await refineAdminStatement(s.id);
  Object.assign(s, updated);
  toast.success(updated.refinementMode === "ai" ? "AI 精炼完成" : "规则精炼完成");
}

async function removeStatement(s: AdminStatement) {
  if (!selectedCourse.value) return;
  if (!confirm(`确认删除第 ${s.order} 句吗？`)) return;
  await deleteAdminStatement(s.id);
  await selectCourse(selectedCourse.value.id);
  toast.success("句子已删除");
}

function vocabText(s: AdminStatement) {
  return (s.vocabulary || []).map((v) => `${v.word}：${v.meaning}`).join("\n");
}

function updateVocab(s: AdminStatement, event: Event) {
  const val = (event.target as HTMLTextAreaElement).value;
  s.vocabulary = val
    .split("\n")
    .map((l) => l.trim())
    .filter(Boolean)
    .map((line) => {
      const [word, ...rest] = line.split(/[:：]/);
      return { word: word?.trim() || "", meaning: rest.join("：").trim() };
    })
    .filter((v) => v.word && v.meaning);
}

loadPacks();
</script>
