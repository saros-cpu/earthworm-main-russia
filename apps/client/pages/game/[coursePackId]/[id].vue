<template>
  <div class="flex min-h-[calc(100vh-9rem)] w-full flex-col py-4">
    <template v-if="isLoading">
      <Loading></Loading>
    </template>
    <template v-else>
      <div class="overflow-hidden rounded-md border border-slate-200 bg-[#f8f5ef] shadow-sm dark:border-slate-800 dark:bg-slate-950">
        <MainTool />
        <div class="min-h-[calc(100vh-15rem)] px-4 py-8 md:px-8">
          <MainGame />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { toast } from "vue-sonner";

import { useGameMode } from "~/composables/main/game";
import { useNavigation } from "~/composables/useNavigation";
import { isAuthenticated } from "~/services/auth";
import { useCourseStore } from "~/store/course";
import { useCoursePackStore } from "~/store/coursePack";
import { useMasteredElementsStore } from "~/store/masteredElements";

const route = useRoute();
const { t } = useI18n();
const coursePackStore = useCoursePackStore();
const courseStore = useCourseStore();
const masteredElementsStore = useMasteredElementsStore();
const { gotoCourseList } = useNavigation();
const isLoading = ref(true);
const { showQuestion } = useGameMode();

showQuestion();

onMounted(async () => {
  const { coursePackId, id } = route.params;
  if (isAuthenticated()) {
    await masteredElementsStore.setup();
  }
  await courseStore.setup(coursePackId as string, id as string);
  await coursePackStore.setupCoursePack(coursePackId as string);

  if (courseStore.isAllMastered()) {
    toast.info(t("summary.allMastered"), {
      duration: 1500,
      onAutoClose: () => {
        gotoCourseList(coursePackId as string);
      },
    });
    return;
  }
  isLoading.value = false;
});
</script>
