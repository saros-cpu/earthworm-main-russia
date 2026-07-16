import { computed, ref } from "vue";

export interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: string;
  category: "milestone" | "streak" | "mastery" | "combo" | "social";
  check: () => boolean;
}

const STORAGE_KEY = "earthworm_achievements";

function loadUnlocked(): string[] {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
  } catch {
    return [];
  }
}

function saveUnlocked(ids: string[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(ids));
}

const unlockedIds = ref<string[]>(loadUnlocked());

export function useAchievements() {
  const all: Achievement[] = [
    {
      id: "first_course",
      title: "初出茅庐",
      description: "完成第一课",
      icon: "i-ph-star",
      category: "milestone",
      check: () => false,
    },
    {
      id: "ten_courses",
      title: "持之以恒",
      description: "完成 10 课",
      icon: "i-ph-books",
      category: "milestone",
      check: () => false,
    },
    {
      id: "fifty_courses",
      title: "学而不厌",
      description: "完成 50 课",
      icon: "i-ph-book-open-text",
      category: "milestone",
      check: () => false,
    },
    {
      id: "hundred_courses",
      title: "百课达人",
      description: "完成 100 课",
      icon: "i-ph-trophy",
      category: "milestone",
      check: () => false,
    },
    {
      id: "streak_3",
      title: "三日不辍",
      description: "连续学习 3 天",
      icon: "i-ph-fire",
      category: "streak",
      check: () => false,
    },
    {
      id: "streak_7",
      title: "一周全勤",
      description: "连续学习 7 天",
      icon: "i-ph-fire-simple",
      category: "streak",
      check: () => false,
    },
    {
      id: "streak_30",
      title: "月度之星",
      description: "连续学习 30 天",
      icon: "i-ph-calendar-star",
      category: "streak",
      check: () => false,
    },
    {
      id: "combo_10",
      title: "十连击",
      description: "单次连击达到 10",
      icon: "i-ph-lightning",
      category: "combo",
      check: () => false,
    },
    {
      id: "combo_30",
      title: "三十连击",
      description: "单次连击达到 30",
      icon: "i-ph-lightning-slash",
      category: "combo",
      check: () => false,
    },
    {
      id: "combo_50",
      title: "五十连击",
      description: "单次连击达到 50",
      icon: "i-ph-rocket-launch",
      category: "combo",
      check: () => false,
    },
    {
      id: "accuracy_90",
      title: "精准打击",
      description: "单课正确率 90%+",
      icon: "i-ph-target",
      category: "mastery",
      check: () => false,
    },
    {
      id: "accuracy_100",
      title: "完美一课",
      description: "单课正确率 100%",
      icon: "i-ph-crown",
      category: "mastery",
      check: () => false,
    },
    {
      id: "grammar_master",
      title: "语法能手",
      description: "查看 20 次 AI 语法分析",
      icon: "i-ph-notebook",
      category: "mastery",
      check: () => false,
    },
    {
      id: "vocab_collector",
      title: "词汇收藏家",
      description: "收藏 50 个生词",
      icon: "i-ph-bookmark-simple",
      category: "mastery",
      check: () => false,
    },
    {
      id: "dialogue_first",
      title: "开口说",
      description: "完成一次 AI 对话练习",
      icon: "i-ph-chat-circle",
      category: "social",
      check: () => false,
    },
  ];

  const unlocked = computed(() => all.filter((a) => unlockedIds.value.includes(a.id)));
  const locked = computed(() => all.filter((a) => !unlockedIds.value.includes(a.id)));
  const progress = computed(() => Math.round((unlocked.value.length / all.length) * 100));

  function unlock(id: string) {
    if (!unlockedIds.value.includes(id)) {
      unlockedIds.value.push(id);
      saveUnlocked(unlockedIds.value);
    }
  }

  function isUnlocked(id: string) {
    return unlockedIds.value.includes(id);
  }

  function resetAll() {
    unlockedIds.value = [];
    saveUnlocked([]);
  }

  return { all, unlocked, locked, progress, unlock, isUnlocked, resetAll };
}
