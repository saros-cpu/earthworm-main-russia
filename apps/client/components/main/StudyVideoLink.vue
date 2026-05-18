<template>
  <a
    v-if="isValidUrl"
    :href="video"
    target="_blank"
    rel="noopener noreferrer"
    class="flex cursor-pointer items-center fill-current text-xl hover:text-fuchsia-500"
  >
    <UTooltip text="边看边练">
      <UIcon
        name="i-simple-icons-bilibili"
        class="h-5 w-5"
      />
    </UTooltip>
  </a>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  video?: string;
}>();

const isValidUrl = computed(() => {
  if (!props.video) return false;
  try {
    const url = new URL(props.video);
    return url.protocol === "http:" || url.protocol === "https:";
  } catch {
    return false;
  }
});
</script>
