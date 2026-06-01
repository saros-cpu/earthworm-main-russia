<template>
  <div class="space-y-6">
    <header>
      <h1 class="text-2xl font-black text-slate-950 dark:text-white">媒体管理</h1>
      <p class="mt-1 text-sm text-slate-500">管理课程包的视频和音频文件路径</p>
    </header>

    <div class="rounded-xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <div class="border-b border-slate-200 px-5 py-4 dark:border-slate-800">
        <div class="flex items-center gap-3">
          <input v-model="searchQuery" class="input input-bordered input-sm max-w-xs" placeholder="搜索课程包..." />
          <span class="text-xs text-slate-400">{{ filteredPacks.length }} / {{ packs.length }} 包</span>
        </div>
      </div>

      <div style="height: 540px; overflow-y: auto;" class="divide-y divide-slate-100 dark:divide-slate-800">
        <div v-for="pack in filteredPacks" :key="pack.id">
          <button class="flex w-full items-center justify-between px-5 py-3 text-left hover:bg-slate-50 dark:hover:bg-slate-800/50" @click="togglePack(pack.id)">
            <div class="flex items-center gap-3">
              <UIcon :name="expandedPacks.has(pack.id) ? 'i-ph-caret-down' : 'i-ph-caret-right'" class="h-4 w-4 text-slate-400" />
              <div>
                <span class="text-sm font-bold text-slate-900 dark:text-slate-100">{{ pack.title }}</span>
                <span v-if="pack.archived" class="ml-2 rounded bg-amber-100 px-2 py-0.5 text-xs text-amber-700 dark:bg-amber-950 dark:text-amber-200">已归档</span>
                <span class="ml-2 text-xs text-slate-400">{{ pack.id }}</span>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <span v-if="pack.mediaCount !== null" class="rounded bg-emerald-100 px-2 py-0.5 text-xs font-bold text-emerald-700 dark:bg-emerald-950 dark:text-emerald-200">{{ pack.mediaCount }} 个媒体</span>
              <span v-else class="rounded bg-slate-100 px-2 py-0.5 text-xs text-slate-500 dark:bg-slate-800 dark:text-slate-300">展开后统计</span>
              <span v-if="pack.mediaCount !== null" class="rounded bg-slate-100 px-2 py-0.5 text-xs text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                <template v-if="(pack.videoCount ?? 0) > 0">{{ pack.videoCount }} 视频</template>
                <template v-else-if="(pack.audioCount ?? 0) > 0">{{ pack.audioCount }} 音频</template>
                <template v-else>无媒体</template>
              </span>
            </div>
          </button>

          <div v-if="expandedPacks.has(pack.id)" class="border-t border-slate-100 bg-slate-50/50 px-5 py-3 dark:border-slate-800 dark:bg-slate-900/50">
            <div v-if="loadingCourses.has(pack.id)" class="py-4 text-center text-sm text-slate-400">加载中...</div>
            <div v-else-if="!packCourses[pack.id]" class="py-4 text-center text-sm text-slate-400">
              <button class="text-purple-600 hover:text-purple-700" @click="loadPackCourses(pack.id)">点击加载课程</button>
            </div>
            <div v-else class="space-y-2">
              <div v-for="course in packCourses[pack.id]" :key="course.id" class="flex items-center gap-3 rounded-md border border-slate-200 bg-white px-4 py-2.5 text-sm dark:border-slate-700 dark:bg-slate-900">
                <div class="min-w-0 flex-1">
                  <div class="font-medium text-slate-900 dark:text-slate-100">{{ course.title }}</div>
                  <div v-if="course.archived" class="text-xs text-amber-600 dark:text-amber-300">已归档</div>
                </div>
                <div v-if="course.video" class="flex items-center gap-2">
                  <span class="rounded bg-emerald-50 px-2 py-0.5 text-xs font-bold text-emerald-700 dark:bg-emerald-950 dark:text-emerald-200">
                    <UIcon :name="isVideo(course.video) ? 'i-ph-video' : 'i-ph-music-note'" class="mr-0.5 inline h-3 w-3" />
                    {{ isVideo(course.video) ? '视频' : '音频' }}
                  </span>
                  <span class="max-w-[200px] truncate text-xs text-slate-400" :title="course.video">{{ course.video.split('/').pop() }}</span>
                </div>
                <div v-else class="text-xs text-slate-400">无媒体</div>
                <button class="rounded p-1 text-slate-400 hover:text-purple-600" title="编辑路径" @click="editCourseMedia(course)">
                  <UIcon name="i-ph-pencil" class="h-3.5 w-3.5" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="filteredPacks.length === 0" class="px-5 py-10 text-center text-sm text-slate-400">
          没有匹配的课程包
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <Teleport to="body">
      <div v-if="editTarget" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="editTarget = null">
        <div class="w-[480px] rounded-xl border bg-white p-5 shadow-lg dark:border-slate-700 dark:bg-slate-900">
          <div class="mb-4 flex items-center justify-between">
            <h3 class="font-bold text-slate-950 dark:text-white">编辑媒体路径</h3>
            <button class="rounded p-1 text-slate-400 hover:text-slate-600" @click="editTarget = null">
              <UIcon name="i-ph-x" class="h-4 w-4" />
            </button>
          </div>
          <div class="mb-2 text-xs text-slate-500">课程：{{ editTarget.title }}</div>
          <input v-model="editPath" class="input input-bordered input-sm w-full font-mono text-xs" placeholder="媒体文件路径（如 east-uni/01/lesson1.mp3）" />
          <div class="mt-4 flex gap-2">
            <button class="btn btn-primary btn-sm flex-1" @click="saveMediaPath">保存</button>
            <button class="btn btn-sm flex-1" @click="editTarget = null">取消</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: "admin", middleware: "admin" });

import { ref, computed } from "vue";
import { toast } from "vue-sonner";
import { fetchAdminCoursePacks, fetchAdminCoursePack, updateAdminCourse } from "~/api/admin";
import type { AdminCourse, AdminCoursePack } from "~/api/admin";
import { isVideoMediaSource } from "~/utils/media";

type MediaPack = AdminCoursePack & {
  mediaCount: number | null;
  videoCount: number | null;
  audioCount: number | null;
};

const searchQuery = ref("");
const packs = ref<MediaPack[]>([]);
const expandedPacks = ref(new Set<string>());
const packCourses = ref<Record<string, AdminCourse[]>>({});
const loadingCourses = ref(new Set<string>());
const editTarget = ref<AdminCourse | null>(null);
const editPath = ref("");

const filteredPacks = computed(() => {
  const q = searchQuery.value.toLowerCase();
  if (!q) return packs.value;
  return packs.value.filter(p => p.title.toLowerCase().includes(q) || p.id.toLowerCase().includes(q));
});

function isVideo(path: string) {
  return isVideoMediaSource(path);
}

function togglePack(id: string) {
  if (expandedPacks.value.has(id)) {
    expandedPacks.value.delete(id);
  } else {
    expandedPacks.value.add(id);
    if (!packCourses.value[id]) {
      loadPackCourses(id);
    }
  }
}

async function loadPackCourses(id: string) {
  loadingCourses.value.add(id);
  try {
    const pack = await fetchAdminCoursePack(id);
    const courses = pack.courses || [];
    packCourses.value[id] = courses;
    const summary = summarizeMedia(courses);
    packs.value = packs.value.map(item => item.id === id ? { ...item, ...summary } : item);
  } catch {
    toast.error("加载课程失败");
  } finally {
    loadingCourses.value.delete(id);
  }
}

function editCourseMedia(course: AdminCourse) {
  editTarget.value = course;
  editPath.value = course.video || "";
}

async function saveMediaPath() {
  if (!editTarget.value) return;
  try {
    const updated = await updateAdminCourse(editTarget.value.id, {
      title: editTarget.value.title,
      description: editTarget.value.description,
      video: editPath.value,
    });
    Object.assign(editTarget.value, updated);
    const containingPack = packs.value.find(pack => packCourses.value[pack.id]?.some(course => course.id === updated.id));
    if (containingPack) {
      Object.assign(containingPack, summarizeMedia(packCourses.value[containingPack.id]));
    }
    toast.success("媒体路径已更新");
    editTarget.value = null;
  } catch {
    toast.error("保存失败");
  }
}

async function load() {
  try {
    const raw = await fetchAdminCoursePacks();
    packs.value = raw.map(p => ({ ...p, mediaCount: null, videoCount: null, audioCount: null }));
  } catch {
    toast.error("加载失败");
  }
}

function summarizeMedia(courses: AdminCourse[]) {
  const mediaCount = courses.filter(course => course.video && course.video.trim()).length;
  const videoCount = courses.filter(course => course.video && isVideo(course.video)).length;
  return { mediaCount, videoCount, audioCount: mediaCount - videoCount };
}

load();
</script>
