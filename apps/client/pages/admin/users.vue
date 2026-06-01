<template>
  <div class="space-y-6">
    <header class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-black text-slate-950 dark:text-white">用户管理</h1>
        <p class="mt-1 text-sm text-slate-500">管理系统注册用户和角色权限；账号删除已停用以保留学习历史</p>
      </div>
      <button class="inline-flex h-9 items-center rounded-md border border-slate-300 bg-white px-3 text-sm font-bold text-slate-700 hover:border-emerald-400 hover:text-emerald-700 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200" :disabled="loading" @click="load">
        {{ loading ? "加载中…" : "刷新" }}
      </button>
    </header>

    <div class="rounded-xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="border-b border-slate-200 text-xs text-slate-500 dark:border-slate-800">
            <tr>
              <th class="px-5 py-3 text-left font-semibold">账号</th>
              <th class="px-5 py-3 text-left font-semibold">用户名</th>
              <th class="px-5 py-3 text-left font-semibold">邮箱</th>
              <th class="px-5 py-3 text-center font-semibold">角色</th>
              <th class="px-5 py-3 text-right font-semibold">注册时间</th>
              <th class="px-5 py-3 text-center font-semibold">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100 dark:divide-slate-800">
            <tr v-for="user in users" :key="user.id" class="hover:bg-slate-50 dark:hover:bg-slate-800/50">
              <td class="px-5 py-3 font-mono text-slate-600 dark:text-slate-300">{{ user.username }}</td>
              <td class="px-5 py-3">
                <div class="flex items-center gap-3">
                  <div class="flex h-8 w-8 items-center justify-center rounded-full bg-purple-100 text-sm font-bold text-purple-700 dark:bg-purple-950 dark:text-purple-200">
                    {{ (user.nickname || user.username).charAt(0).toUpperCase() }}
                  </div>
                  <span class="font-semibold text-slate-900 dark:text-slate-100">{{ user.nickname || user.username }}</span>
                </div>
              </td>
              <td class="px-5 py-3 text-slate-600 dark:text-slate-300">{{ user.email || '-' }}</td>
              <td class="px-5 py-3 text-center">
                <span class="rounded px-2 py-0.5 text-xs font-bold" :class="isAdmin(user) ? 'bg-purple-100 text-purple-700 dark:bg-purple-950 dark:text-purple-200' : 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300'">
                  {{ isAdmin(user) ? '管理员' : '用户' }}
                </span>
              </td>
              <td class="px-5 py-3 text-right font-mono text-xs text-slate-400">{{ formatDate(user.createdAt) }}</td>
              <td class="px-5 py-3 text-center">
                <div class="flex items-center justify-center gap-1">
                  <button class="rounded px-2 py-1 text-xs font-bold text-blue-600 hover:bg-blue-50 hover:text-blue-800 dark:text-blue-400 dark:hover:bg-blue-950/50" @click="openEdit(user)">
                    编辑
                  </button>
                  <button v-if="!isAdmin(user)" class="rounded px-2 py-1 text-xs font-bold text-purple-600 hover:bg-purple-50 hover:text-purple-800 dark:text-purple-400 dark:hover:bg-purple-950/50" :disabled="toggling === user.id" @click="toggleRole(user)">
                    {{ toggling === user.id ? "..." : "设为管理" }}
                  </button>
                  <span class="text-xs text-slate-400">保留历史</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="users.length === 0" class="px-5 py-10 text-center text-sm text-slate-400">暂无用户</div>
    </div>

    <section v-if="lastMessage" class="rounded-md border border-emerald-300 bg-emerald-50 p-3 text-sm text-emerald-900 dark:border-emerald-800 dark:bg-emerald-950 dark:text-emerald-100">
      {{ lastMessage }}
    </section>

    <!-- 编辑弹窗 -->
    <div v-if="editing" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="editing = null">
      <div class="w-full max-w-md rounded-xl border border-slate-200 bg-white p-6 shadow-xl dark:border-slate-700 dark:bg-slate-900">
        <h3 class="mb-4 text-lg font-bold text-slate-900 dark:text-slate-100">编辑用户</h3>

        <div class="space-y-3">
          <div>
            <label class="mb-1 block text-xs font-bold text-slate-600 dark:text-slate-300">用户名</label>
            <input v-model="editForm.nickname" type="text"
              class="h-10 w-full rounded-md border border-slate-200 bg-white px-3 text-sm outline-none focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-800" />
          </div>
          <div>
            <label class="mb-1 block text-xs font-bold text-slate-600 dark:text-slate-300">邮箱</label>
            <input v-model="editForm.email" type="text"
              class="h-10 w-full rounded-md border border-slate-200 bg-white px-3 text-sm outline-none focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-800" />
          </div>
          <div>
            <label class="mb-1 block text-xs font-bold text-slate-600 dark:text-slate-300">新密码（留空不修改）</label>
            <input v-model="editForm.password" type="password"
              class="h-10 w-full rounded-md border border-slate-200 bg-white px-3 text-sm outline-none focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-800" placeholder="输入新密码" />
          </div>
        </div>

        <div class="mt-6 flex justify-end gap-2">
          <button class="rounded-md border border-slate-300 px-4 py-2 text-sm font-bold text-slate-700 hover:bg-slate-50 dark:border-slate-600 dark:text-slate-200 dark:hover:bg-slate-800" @click="editing = null">
            取消
          </button>
          <button class="rounded-md bg-emerald-600 px-4 py-2 text-sm font-bold text-white hover:bg-emerald-700" :disabled="saving" @click="saveEdit">
            {{ saving ? "保存中…" : "保存" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: "admin", middleware: "admin" });

import { ref, onMounted } from "vue";
import { toast } from "vue-sonner";
import { fetchAdminUsers, updateAdminUserRole, updateAdminUser, type AdminUser } from "~/api/admin";

const users = ref<AdminUser[]>([]);
const loading = ref(false);
const toggling = ref("");
const lastMessage = ref("");

const editing = ref<AdminUser | null>(null);
const editForm = ref({ nickname: "", email: "", password: "" });
const saving = ref(false);

function isAdmin(user: AdminUser) {
  return user.role?.toUpperCase() === "ADMIN";
}

function formatDate(dateStr: string) {
  try {
    return new Date(dateStr).toLocaleDateString("zh-CN", { year: "numeric", month: "2-digit", day: "2-digit" });
  } catch {
    return dateStr;
  }
}

async function load() {
  loading.value = true;
  try {
    users.value = await fetchAdminUsers();
  } catch (e: any) {
    lastMessage.value = `加载失败: ${e?.message || e}`;
  } finally {
    loading.value = false;
  }
}

async function toggleRole(user: AdminUser) {
  toggling.value = user.id;
  try {
    await updateAdminUserRole(user.id, "ADMIN");
    toast.success(`${user.nickname || user.username} 已设为管理员`);
    await load();
  } catch (e: any) {
    toast.error(`操作失败: ${e?.message || e}`);
  } finally {
    toggling.value = "";
  }
}

function openEdit(user: AdminUser) {
  editing.value = user;
  editForm.value = { nickname: user.nickname || "", email: user.email || "", password: "" };
}

async function saveEdit() {
  if (!editing.value) return;
  saving.value = true;
  try {
    const body: { nickname?: string; email?: string; password?: string } = {
      nickname: editForm.value.nickname,
      email: editForm.value.email,
    };
    if (editForm.value.password.trim()) body.password = editForm.value.password.trim();
    await updateAdminUser(editing.value.id, body);
    toast.success("用户信息已更新");
    editing.value = null;
    await load();
  } catch (e: any) {
    toast.error(`保存失败: ${e?.message || e}`);
  } finally {
    saving.value = false;
  }
}

onMounted(() => load());
</script>
