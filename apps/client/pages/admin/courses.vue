<template>
  <div class="space-y-4">
    <section class="rounded-md border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900 dark:border-amber-800 dark:bg-amber-950 dark:text-amber-100">
      为保护学习记录，下架操作会将内容归档而不物理删除；历史学习记录继续保留，归档内容也可恢复。
    </section>

    <!-- 工具栏 -->
    <div class="flex flex-wrap items-center gap-3">
      <select v-model="selectedPackId" class="select select-bordered select-sm max-w-xs">
        <option value="">— 选择课程包 —</option>
        <option v-for="p in coursePacks" :key="p.id" :value="p.id">{{ p.title }}{{ p.archived ? "（已归档）" : "" }}</option>
      </select>
      <template v-if="selectedPack">
        <select v-model="selectedCourseId" class="select select-bordered select-sm max-w-xs">
          <option value="">— 选择课程 —</option>
          <option v-for="c in selectedPack.courses || []" :key="c.id" :value="c.id">{{ c.title }}{{ c.archived ? "（已归档）" : "" }}</option>
        </select>
        <button class="btn btn-outline btn-xs" @click="addCourse">新增课程</button>
      </template>
      <button class="btn btn-ghost btn-xs" @click="loadPacks">刷新</button>
    </div>

    <!-- 课程包设置（折叠） -->
    <section v-if="selectedPack" class="rounded-xl border border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900">
      <button class="flex w-full items-center justify-between px-4 py-3 text-left" @click="showPackSettings = !showPackSettings">
        <span class="text-xs font-bold uppercase tracking-wider text-slate-500">课程包设置</span>
        <UIcon :name="showPackSettings ? 'i-ph-caret-up' : 'i-ph-caret-down'" class="h-4 w-4 text-slate-400" />
      </button>
      <div v-if="showPackSettings" class="border-t border-slate-200 px-4 pb-4 pt-3 dark:border-slate-800">
        <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <input v-model="packForm.title" class="input input-bordered input-sm" placeholder="标题" />
          <select v-model="packForm.shareLevel" class="select select-bordered select-sm">
            <option value="public">公开</option>
            <option value="private">私有</option>
          </select>
          <textarea v-model="packForm.description" class="textarea textarea-bordered col-span-full min-h-14 text-sm" placeholder="描述" />
          <div class="col-span-full flex items-center gap-4">
            <label class="flex items-center gap-2 text-xs">
              <input v-model="packForm.isFree" type="checkbox" class="checkbox checkbox-xs" />
              免费课程包
            </label>
            <button class="btn btn-primary btn-xs" @click="savePack">保存</button>
            <button class="btn btn-outline btn-xs" @click="togglePackArchive">
              {{ selectedPack.archived ? "恢复课程包" : "归档课程包" }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 课程编辑器 -->
    <section v-if="selectedCourse" class="rounded-xl border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900">
      <h3 class="mb-3 text-xs font-bold uppercase tracking-wider text-slate-500">课程设置</h3>
      <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <input v-model="courseForm.title" class="input input-bordered input-sm" placeholder="标题" />
        <input v-model="courseForm.video" class="input input-bordered input-sm" placeholder="媒体路径" />
        <textarea v-model="courseForm.description" class="textarea textarea-bordered col-span-full min-h-12 text-sm" placeholder="描述" />
        <div class="col-span-full flex gap-2">
          <button class="btn btn-primary btn-xs" @click="saveCourse">保存</button>
          <button class="btn btn-outline btn-xs" @click="toggleCourseArchive">
            {{ selectedCourse.archived ? "恢复课程" : "归档课程" }}
          </button>
        </div>
      </div>
    </section>

    <!-- 句列表 -->
    <div v-if="selectedCourse" class="flex items-center justify-between">
      <h3 class="text-xs font-bold uppercase tracking-wider text-slate-500">句子 ({{ (selectedCourse.statements || []).length }})</h3>
      <button class="btn btn-outline btn-xs" @click="addStatement">新增句子</button>
    </div>

    <div v-if="selectedCourse" class="space-y-3">
      <article v-for="s in selectedCourse.statements || []" :key="s.id"
        class="rounded-xl border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900">
        <div class="mb-3 flex items-center justify-between">
          <span class="text-sm font-bold text-slate-900 dark:text-slate-100">#{{ s.order }}</span>
          <div class="flex items-center gap-2">
            <span class="rounded bg-slate-100 px-2 py-0.5 text-[10px] text-slate-500 dark:bg-slate-800 dark:text-slate-300">{{ s.refinementMode || "未精炼" }}</span>
            <button class="rounded p-1 text-slate-400 hover:text-emerald-600" title="规则精炼" @click="refineStatement(s)">
              <UIcon name="i-ph-sparkle" class="h-3.5 w-3.5" />
            </button>
          </div>
        </div>
        <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <textarea v-model="s.sourceText" class="textarea textarea-bordered min-h-14 text-sm" placeholder="中文释义" />
          <textarea v-model="s.targetText" class="textarea textarea-bordered min-h-14 text-sm" placeholder="俄语句子" />
          <input v-model="s.phonetic" class="input input-bordered input-sm" placeholder="音标" />
          <select v-model="s.difficulty" class="select select-bordered select-sm">
            <option value="beginner">beginner</option>
            <option value="elementary">elementary</option>
            <option value="intermediate">intermediate</option>
          </select>
          <textarea v-model="s.grammarNote" class="textarea textarea-bordered col-span-full min-h-12 text-sm" placeholder="语法提示" />
          <textarea :value="vocabText(s)" class="textarea textarea-bordered col-span-full min-h-14 text-sm" placeholder="词汇点，每行：单词：释义" @input="updateVocab(s, $event)" />
        </div>
        <div class="mt-3 flex gap-2">
          <button class="btn btn-outline btn-xs" @click="saveStatement(s)">保存句子</button>
          <button class="btn btn-outline btn-xs" @click="toggleStatementArchive(s)">
            {{ s.archived ? "恢复句子" : "归档句子" }}
          </button>
        </div>
      </article>
      <div v-if="(selectedCourse.statements || []).length === 0" class="py-10 text-center text-sm text-slate-400">暂无句子</div>
    </div>

    <div v-if="selectedPack && !selectedCourse" class="py-10 text-center text-sm text-slate-400">选择一个课程开始编辑</div>
    <div v-if="!selectedPack" class="py-10 text-center text-sm text-slate-400">选择课程包</div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: "admin", middleware: "admin" });

import { reactive, ref, watch } from "vue";
import { toast } from "vue-sonner";
import {
  fetchAdminCoursePacks, fetchAdminCoursePack, fetchAdminCourse,
  updateAdminCoursePack, updateAdminCourse, updateAdminStatement,
  createAdminCourse, createAdminStatement,
  deleteAdminCoursePack, deleteAdminCourse, deleteAdminStatement,
  refineAdminStatement,
  type AdminCoursePack, type AdminCourse, type AdminStatement,
} from "~/api/admin";

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

async function togglePackArchive() {
  if (!selectedPack.value) return;
  const id = selectedPack.value.id;
  if (selectedPack.value.archived) {
    await updateAdminCoursePack(id, { archived: false });
    toast.success("课程包已恢复，请确认公开状态后再发布");
  } else {
    await deleteAdminCoursePack(id);
    toast.success("课程包已归档");
  }
  coursePacks.value = await fetchAdminCoursePacks();
  await selectPack(id);
}

async function addCourse() {
  if (!selectedPack.value) return;
  const c = await createAdminCourse(selectedPack.value.id, { title: "新课程", description: "请填写课程描述" });
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

async function toggleCourseArchive() {
  if (!selectedCourse.value) return;
  if (selectedCourse.value.archived) {
    await updateAdminCourse(selectedCourse.value.id, { archived: false });
    toast.success("课程已恢复");
  } else {
    await deleteAdminCourse(selectedCourse.value.id);
    toast.success("课程已归档");
  }
  if (selectedPack.value) await selectPack(selectedPack.value.id);
  await selectCourse(selectedCourse.value.id);
}

async function addStatement() {
  if (!selectedCourse.value) return;
  await createAdminStatement(selectedCourse.value.id, {
    sourceText: "请填写中文释义", targetText: "Пожалуйста, заполните русский текст.", phonetic: "",
  });
  await selectCourse(selectedCourse.value.id);
  toast.success("句子已新增");
}

async function saveStatement(s: AdminStatement) {
  const updated = await updateAdminStatement(s.id, {
    sourceText: s.sourceText, targetText: s.targetText, phonetic: s.phonetic,
    translation: s.sourceText, vocabulary: s.vocabulary || [],
    grammarNote: s.grammarNote, difficulty: s.difficulty,
  });
  Object.assign(s, updated);
  toast.success("句子已保存");
}

async function toggleStatementArchive(s: AdminStatement) {
  if (s.archived) {
    Object.assign(s, await updateAdminStatement(s.id, { archived: false }));
    toast.success("句子已恢复");
  } else {
    await deleteAdminStatement(s.id);
    s.archived = true;
    toast.success("句子已归档");
  }
}

async function refineStatement(s: AdminStatement) {
  const updated = await refineAdminStatement(s.id);
  Object.assign(s, updated);
  toast.success("规则精炼完成");
}

function vocabText(s: AdminStatement) {
  return (s.vocabulary || []).map(v => `${v.word}：${v.meaning}`).join("\n");
}

function updateVocab(s: AdminStatement, event: Event) {
  const val = (event.target as HTMLTextAreaElement).value;
  s.vocabulary = val.split("\n").map(l => l.trim()).filter(Boolean).map(line => {
    const [word, ...rest] = line.split(/[:：]/);
    return { word: word?.trim() || "", meaning: rest.join("：").trim() };
  }).filter(v => v.word && v.meaning);
}

loadPacks();
</script>
