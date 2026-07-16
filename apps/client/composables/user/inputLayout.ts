import { ref, watch } from "vue";

const STORAGE_KEY = "inputLayout";

export type InputLayout = "russian" | "phonetic";

const currentLayout = ref<InputLayout>(loadLayout());

function loadLayout(): InputLayout {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored === "russian" || stored === "phonetic") return stored;
  return "russian";
}

function persistLayout(layout: InputLayout) {
  localStorage.setItem(STORAGE_KEY, layout);
}

watch(currentLayout, persistLayout);

export function useInputLayout() {
  function toggleLayout() {
    currentLayout.value = currentLayout.value === "russian" ? "phonetic" : "russian";
  }

  function setLayout(layout: InputLayout) {
    currentLayout.value = layout;
  }

  return {
    currentLayout,
    toggleLayout,
    setLayout,
  };
}
