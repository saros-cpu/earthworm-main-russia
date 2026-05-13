<!-- 测试模式：Logto 回调页面已简化，直接跳转首页 -->
<script setup lang="ts">
import { navigateTo } from "nuxt/app";
import { onMounted } from "vue";

onMounted(() => {
  navigateTo("/");
});
</script>

<template>
  <div class="flex w-full flex-col items-center justify-center pt-10">
    <p>正在跳转...</p>
  </div>
</template>
