<template>
  <slot></slot>
</template>

<script setup lang="ts">
import { toast } from "vue-sonner";

import { signIn } from "~/api/auth";
import { injectHttpStatusErrorHandler } from "~/api/http.js";

useHttpStatusError();

function useHttpStatusError() {
  injectHttpStatusErrorHandler(async (errMessage, statusCode) => {
    switch (statusCode) {
      case 401:
        toast.error(errMessage, {
          duration: 2000,
          onAutoClose() {
            signIn(window.location.pathname);
          },
        });
        break;
      default:
        toast.error(errMessage);
        break;
    }
  });
}
</script>

<style scoped></style>
~/store/user
