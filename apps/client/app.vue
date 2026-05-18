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
import { ref } from "vue";
import { Toaster } from "vue-sonner";

import { fetchCurrentUser } from "~/api/user";
import { Theme, useDarkMode } from "~/composables/darkMode";
import { getStoredUser, isAuthenticated } from "~/services/auth";
import { useUserStore } from "./store/user";

const { initDarkMode, darkMode } = useDarkMode();
initDarkMode();

const userStore = useUserStore();
const status = ref("pending");

if (isAuthenticated()) {
  const stored = getStoredUser();
  if (stored) {
    userStore.initUser({
      iss: "local-dev",
      sub: stored.userId || "dev-user-001",
      aud: "local-dev",
      exp: Math.floor(Date.now() / 1000) + 86400,
      iat: Math.floor(Date.now() / 1000),
      id: stored.userId || "dev-user-001",
      username: stored.username || "dev-user",
      nickname: stored.nickname || stored.username || "dev-user",
      name: stored.nickname || stored.username || "dev-user",
      primaryEmail: "",
      avatar: stored.avatar || "",
      picture: "",
      membership: { isMember: false, details: null },
    } as any);
  }
  status.value = "success";
  fetchCurrentUser()
    .then((user) => {
      userStore.initUser(user);
    })
    .catch(() => {});
} else {
  status.value = "success";
}
</script>

<style>
#jfToolbar,
.mod-json {
  display: none !important;
}
</style>
