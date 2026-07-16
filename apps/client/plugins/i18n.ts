import { createI18n } from "vue-i18n";

import ru from "~/locales/ru.json";
import zh from "~/locales/zh.json";

export default defineNuxtPlugin(({ vueApp }) => {
  const i18n = createI18n({
    legacy: false,
    locale: "ru",
    fallbackLocale: "zh",
    messages: { zh, ru },
  });

  vueApp.use(i18n);
});
