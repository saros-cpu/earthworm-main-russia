<template>
  <div v-if="statementId">
    <button class="inline-flex h-8 items-center gap-1 rounded-md border border-slate-200 px-2.5 text-xs font-bold text-slate-500 transition hover:border-emerald-300 hover:text-emerald-700 dark:border-slate-700 dark:text-slate-400"
      @click="open = !open">
      <UIcon name="i-ph-notebook" class="h-3.5 w-3.5" />
      {{ hasNote ? '编辑笔记' : '添加笔记' }}
    </button>

    <div v-if="open" class="mt-2 rounded-md border border-slate-200 bg-white p-3 shadow-sm dark:border-slate-700 dark:bg-slate-800">
      <textarea v-model="noteContent" rows="3" placeholder="添加你的学习笔记..."
        class="w-full rounded-md border border-slate-200 bg-white p-2 text-xs outline-none transition focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-900"></textarea>
      <div class="mt-2 flex justify-end gap-2">
        <button class="rounded-md px-3 py-1 text-xs font-bold text-slate-500 hover:bg-slate-100" @click="open = false">取消</button>
        <button class="rounded-md bg-emerald-600 px-3 py-1 text-xs font-bold text-white hover:bg-emerald-700" @click="saveNote" :disabled="saving">
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { getHttp } from "~/api/http";

const props = defineProps<{ statementId?: string }>();
const open = ref(false);
const noteContent = ref("");
const hasNote = ref(false);
const saving = ref(false);

async function saveNote() {
  if (!props.statementId) return;
  saving.value = true;
  try {
    const http = getHttp();
    await http("/notes", { method: "post", body: { statementId: props.statementId, content: noteContent.value } });
    hasNote.value = !!noteContent.value;
    open.value = false;
  } catch (_) {}
  saving.value = false;
}

async function loadNote() {
  if (!props.statementId) return;
  try {
    const http = getHttp();
    const notes = await http<any[]>("/notes?statementId=" + props.statementId, { method: "get" });
    if (notes.length > 0) {
      noteContent.value = notes[0].content;
      hasNote.value = true;
    }
  } catch (_) {}
}

onMounted(loadNote);
</script>
