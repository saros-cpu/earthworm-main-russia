import { ref } from "vue";

export const SHORTCUT_KEYS = "SHORTCUT_KEYS";

export const SHORTCUT_KEY_TYPES = {
  SOUND: "sound",
  ANSWER: "answer",
  SKIP: "skip",
  PREVIOUS: "previous",
  MASTERED: "mastered",
  PAUSE: "pause",
} as const;

export const DEFAULT_SHORTCUT_KEYS = {
  [SHORTCUT_KEY_TYPES.SOUND]: "Ctrl+'",
  [SHORTCUT_KEY_TYPES.ANSWER]: "Ctrl+;",
  [SHORTCUT_KEY_TYPES.SKIP]: "Ctrl+.",
  [SHORTCUT_KEY_TYPES.PREVIOUS]: "Ctrl+,",
  [SHORTCUT_KEY_TYPES.MASTERED]: "Ctrl+m",
  [SHORTCUT_KEY_TYPES.PAUSE]: "Ctrl+p",
};

type ShortcutKeyType = (typeof SHORTCUT_KEY_TYPES)[keyof typeof SHORTCUT_KEY_TYPES];
type ShortcutKeys = typeof DEFAULT_SHORTCUT_KEYS;

const shortcutKeys = ref<ShortcutKeys>({ ...DEFAULT_SHORTCUT_KEYS });
const showModal = ref(false);
const shortcutKeyStr = ref("");
const shortcutKeyTip = ref("");
const hasSameShortcutKey = ref(false);
const editingType = ref<ShortcutKeyType | null>(null);

export function useShortcutKeyMode() {
  setShortcutKeys();

  function setShortcutKeys() {
    const cached = readShortcutCache();
    shortcutKeys.value = cached ? { ...DEFAULT_SHORTCUT_KEYS, ...cached } : { ...DEFAULT_SHORTCUT_KEYS };
  }

  function reset() {
    shortcutKeys.value = { ...DEFAULT_SHORTCUT_KEYS };
    showModal.value = false;
    shortcutKeyStr.value = "";
    shortcutKeyTip.value = "";
    hasSameShortcutKey.value = false;
    editingType.value = null;
    removeStorageItem(SHORTCUT_KEYS);
  }

  function handleEdit(type: ShortcutKeyType) {
    editingType.value = type;
    shortcutKeyStr.value = "";
    shortcutKeyTip.value = "";
    hasSameShortcutKey.value = false;
    showModal.value = true;
  }

  function handleCloseDialog() {
    showModal.value = false;
    shortcutKeyStr.value = "";
    shortcutKeyTip.value = "";
    hasSameShortcutKey.value = false;
    editingType.value = null;
  }

  function handleKeydown(event: KeyboardEvent) {
    if (!showModal.value) return;

    event.preventDefault?.();

    if (event.key === "Enter") {
      confirmShortcut();
      return;
    }

    if (["Control", "Shift", "Alt", "Meta", "Command"].includes(event.key)) {
      return;
    }

    const nextShortcut = eventToShortcut(event);
    shortcutKeyStr.value = nextShortcut;
    shortcutKeyTip.value = nextShortcut;
    hasSameShortcutKey.value = false;
  }

  function confirmShortcut() {
    if (!editingType.value) {
      handleCloseDialog();
      return;
    }

    if (!shortcutKeyStr.value) {
      handleCloseDialog();
      return;
    }

    const duplicated = Object.entries(shortcutKeys.value).some(
      ([type, value]) => type !== editingType.value && value === shortcutKeyStr.value,
    );

    if (duplicated) {
      hasSameShortcutKey.value = true;
      return;
    }

    shortcutKeys.value = {
      ...shortcutKeys.value,
      [editingType.value]: shortcutKeyStr.value,
    };
    writeShortcutCache(shortcutKeys.value);
    handleCloseDialog();
  }

  return {
    shortcutKeys,
    showModal,
    shortcutKeyStr,
    shortcutKeyTip,
    hasSameShortcutKey,
    setShortcutKeys,
    reset,
    handleEdit,
    handleCloseDialog,
    handleKeydown,
  };
}

export function useShortcutKey() {
  const { shortcutKeys } = useShortcutKeyMode();

  function setShortcut(name: string, config: string) {
    shortcutKeys.value = { ...shortcutKeys.value, [name]: config };
    writeShortcutCache(shortcutKeys.value);
  }

  function getShortcut(name: string) {
    return shortcutKeys.value[name as ShortcutKeyType];
  }

  function removeShortcut(name: string) {
    const { [name as ShortcutKeyType]: _, ...rest } = shortcutKeys.value;
    shortcutKeys.value = rest as ShortcutKeys;
    writeShortcutCache(shortcutKeys.value);
  }

  return {
    shortcuts: shortcutKeys,
    setShortcut,
    getShortcut,
    removeShortcut,
  };
}

export function parseShortcut(shortcut: string) {
  return shortcut.split("+").map((key) => (key.length === 1 ? key.toUpperCase() : key));
}

export function convertMacKey(key: string) {
  if (key === "Meta") return "Command";
  if (key === "Control") return "Ctrl";
  if (key === " ") return "Space";
  return key;
}

function eventToShortcut(event: KeyboardEvent) {
  const keys: string[] = [];
  if (event.ctrlKey) keys.push("Ctrl");
  if (event.metaKey) keys.push("Command");
  if (event.altKey) keys.push("Alt");
  if (event.shiftKey) keys.push("Shift");
  keys.push(convertMacKey(event.key));
  return keys.join("+");
}

function readShortcutCache(): Partial<ShortcutKeys> | null {
  try {
    const raw = getStorageItem(SHORTCUT_KEYS);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function writeShortcutCache(value: ShortcutKeys) {
  setStorageItem(SHORTCUT_KEYS, JSON.stringify(value));
}

function getStorageItem(key: string) {
  return typeof localStorage === "undefined" ? null : localStorage.getItem(key);
}

function setStorageItem(key: string, value: string) {
  if (typeof localStorage !== "undefined") {
    localStorage.setItem(key, value);
  }
}

function removeStorageItem(key: string) {
  if (typeof localStorage !== "undefined") {
    localStorage.removeItem(key);
  }
}
