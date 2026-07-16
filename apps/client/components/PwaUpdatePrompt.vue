<template>
  <div />
</template>

<script setup lang="ts">
import { useRegisterSW } from "virtual:pwa-register/vue";
import { toast } from "vue-sonner";

const { needRefresh, updateServiceWorker } = useRegisterSW();
watch(needRefresh, (v) => {
  if (v) {
    toast.info("新版本已可用，点击刷新", {
      action: {
        label: "刷新",
        onClick: () => updateServiceWorker(),
      },
      duration: Infinity,
    });
  }
});
</script>
