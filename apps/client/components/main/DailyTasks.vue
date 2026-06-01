<template>
  <div v-if="tasks.length > 0" class="rounded-md border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
    <h3 class="mb-3 text-sm font-bold text-slate-950 dark:text-white">📋 每日任务</h3>
    <div class="space-y-2">
      <div v-for="task in tasks" :key="task.taskType"
        class="flex items-center gap-3 rounded-md bg-slate-50 px-3 py-2 dark:bg-slate-800">
        <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full"
          :class="task.completed ? 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900 dark:text-emerald-300' : 'bg-slate-200 text-slate-500 dark:bg-slate-700'">
          <UIcon :name="taskIcon(task.taskType)" class="h-4 w-4" />
        </div>
        <div class="flex-1 min-w-0">
          <div class="text-xs font-bold text-slate-700 dark:text-slate-200">{{ taskLabel(task.taskType) }}</div>
          <div class="mt-0.5 flex items-center gap-2">
            <div class="h-1.5 flex-1 overflow-hidden rounded-full bg-slate-200 dark:bg-slate-700">
              <div class="h-full rounded-full transition-all duration-500"
                :class="task.completed ? 'bg-emerald-500' : 'bg-amber-400'"
                :style="{ width: Math.min(100, (task.progress / task.target) * 100) + '%' }"></div>
            </div>
            <span class="text-[10px] font-bold text-slate-400">{{ task.progress }}/{{ task.target }}</span>
          </div>
        </div>
        <div v-if="task.completed && !task.rewardClaimed"
          class="cursor-pointer rounded-md bg-emerald-100 px-2 py-1 text-[10px] font-bold text-emerald-700 transition hover:bg-emerald-200 dark:bg-emerald-900 dark:text-emerald-300"
          @click="claimReward(task)">
          领取
        </div>
        <div v-else-if="task.completed && task.rewardClaimed" class="text-[10px] font-bold text-emerald-500">✓</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { getHttp } from "~/api/http";

interface TaskItem {
  id: string;
  taskType: string;
  target: number;
  progress: number;
  completed: boolean;
  rewardClaimed: boolean;
}

const tasks = ref<TaskItem[]>([]);

const taskLabels: Record<string, string> = {
  complete_10: "完成 10 题",
  combo_5: "连续答对 5 题",
  learn_15min: "学习 15 分钟",
};

const taskIcons: Record<string, string> = {
  complete_10: "i-ph-checks",
  combo_5: "i-ph-lightning",
  learn_15min: "i-ph-clock",
};

function taskLabel(type: string) { return taskLabels[type] || type; }
function taskIcon(type: string) { return taskIcons[type] || "i-ph-question"; }

async function claimReward(task: TaskItem) {
  try {
    const http = getHttp();
    await http("/tasks/claim", { method: "post", body: { taskType: task.taskType } });
    task.rewardClaimed = true;
  } catch (_) {}
}

onMounted(async () => {
  try {
    const http = getHttp();
    await http("/tasks/ensure", { method: "post" });
    const res = await http<{ tasks: TaskItem[] }>("/tasks/today", { method: "get" });
    tasks.value = res.tasks;
  } catch (_) {}
});
</script>
