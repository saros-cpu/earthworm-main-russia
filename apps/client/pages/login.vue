<template>
  <div class="mx-auto flex min-h-[60vh] w-full max-w-md items-center justify-center py-10">
    <div class="w-full rounded-md border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <h1 class="mb-1 text-2xl font-black text-slate-950 dark:text-white">{{ isLogin ? '登录' : '注册' }}</h1>
      <p class="mb-6 text-sm text-slate-500 dark:text-slate-400">
        {{ isLogin ? '欢迎回来，继续你的俄语学习之旅' : '创建账号，开始学习俄语' }}
      </p>

      <div v-if="error" class="mb-4 rounded-md bg-red-50 p-3 text-sm font-bold text-red-600 dark:bg-red-950 dark:text-red-300">
        {{ error }}
      </div>

      <div class="space-y-4">
        <div>
          <label class="mb-1 block text-xs font-bold text-slate-600 dark:text-slate-300">账号</label>
          <input v-model="username" type="text" placeholder="输入账号"
            class="h-11 w-full rounded-md border border-slate-200 bg-white px-3 text-sm outline-none focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-800" />
        </div>
        <div v-if="!isLogin">
          <label class="mb-1 block text-xs font-bold text-slate-600 dark:text-slate-300">用户名</label>
          <input v-model="nickname" type="text" placeholder="输入用户名（显示用）"
            class="h-11 w-full rounded-md border border-slate-200 bg-white px-3 text-sm outline-none focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-800" />
        </div>
        <div>
          <label class="mb-1 block text-xs font-bold text-slate-600 dark:text-slate-300">密码</label>
          <input v-model="password" type="password" placeholder="输入密码"
            class="h-11 w-full rounded-md border border-slate-200 bg-white px-3 text-sm outline-none focus:border-emerald-400 dark:border-slate-700 dark:bg-slate-800"
            @keydown.enter="submit" />
        </div>
      </div>

      <button @click="submit" :disabled="loading"
        class="mt-6 flex h-11 w-full items-center justify-center rounded-md bg-slate-950 text-sm font-bold text-white transition hover:bg-slate-800 disabled:opacity-50 dark:bg-white dark:text-slate-950">
        {{ loading ? '处理中...' : (isLogin ? '登录' : '注册') }}
      </button>

      <p class="mt-4 text-center text-sm text-slate-500">
        {{ isLogin ? '没有账号？' : '已有账号？' }}
        <button class="font-bold text-emerald-600 hover:text-emerald-700 dark:text-emerald-400" @click="isLogin = !isLogin">
          {{ isLogin ? '注册' : '登录' }}
        </button>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { login, register } from "~/services/auth";
const isLogin = ref(true);
const username = ref("");
const nickname = ref("");
const password = ref("");
const error = ref("");
const loading = ref(false);

async function submit() {
  error.value = "";
  if (!username.value.trim() || !password.value.trim()) {
    error.value = "请填写账号和密码";
    return;
  }
  loading.value = true;
  try {
    if (isLogin.value) {
      await login(username.value, password.value);
    } else {
      await register(username.value, password.value, nickname.value);
    }
    window.location.href = "/";
  } catch (e: any) {
    error.value = e.message || "操作失败";
  }
  loading.value = false;
}
</script>
