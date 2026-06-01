<template>
  <div class="font-customFont">
    <LandingBanner @start-lesson="startLesson" />
    <LandingFeatures />
    <CommonBackTop class="sticky bottom-28 ml-auto flex justify-end sm:block" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";

import { isAuthenticated } from "~/services/auth";
import { cancelShortcut, registerShortcut } from "~/utils/keyboardShortcuts";

const { startLesson } = useShortcutToGame();

function useShortcutToGame() {
  const router = useRouter();

  async function startLesson() {
    if (!isAuthenticated()) {
      router.push(`/course-pack`);
    }
  }

  onMounted(() => {
    registerShortcut("enter", startLesson);
  });

  onUnmounted(() => {
    cancelShortcut("enter", startLesson);
  });

  return {
    startLesson,
  };
}
</script>
