<template>
  <HttpErrorProvider>
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
    <div
      v-if="status === 'pending'"
      class="fixed inset-0 z-50 h-screen w-screen"
    >
      <Loading />
    </div>
    <UModals />
    <Toaster
      :theme="darkMode === Theme.DARK ? 'dark' : 'light'"
      position="top-center"
      :toastOptions="{
        style: {
          background: darkMode === Theme.DARK ? '#c084fc' : '#f3e8ff',
          color: darkMode === Theme.DARK ? '#000' : '#6b21a8',
        },
      }"
    />
  </HttpErrorProvider>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { Toaster } from "vue-sonner";

import { fetchBrowserSession, fetchCurrentUser } from "~/api/user";
import { Theme, useDarkMode } from "~/composables/darkMode";
import { clearAuth, clearLegacyToken, setStoredUser } from "~/services/auth";
import { useUserStore } from "./store/user";

const { initDarkMode, darkMode } = useDarkMode();
initDarkMode();

const userStore = useUserStore();
const status = ref("pending");

onMounted(async () => {
  clearLegacyToken();
  try {
    const session = await fetchBrowserSession();
    if (!session.authenticated) {
      clearAuth();
      status.value = "success";
      return;
    }
    const user = await fetchCurrentUser();
    setStoredUser({
      userId: user.id,
      username: user.username,
      nickname: user.nickname,
      avatar: user.avatar,
      role: user.role || "USER",
    });
    userStore.initUser(user);
  } catch {
    clearAuth();
  } finally {
    status.value = "success";
  }
});
</script>

<style>
#jfToolbar,
.mod-json {
  display: none !important;
}
</style>
