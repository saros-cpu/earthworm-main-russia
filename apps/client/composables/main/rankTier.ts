import { computed, ref } from "vue";

export interface Tier {
  id: string;
  label: string;
  icon: string;
  color: string;
  minScore: number;
}

const TIERS: Tier[] = [
  { id: "bronze", label: "青铜", icon: "i-ph-circle", color: "text-amber-700", minScore: 0 },
  { id: "silver", label: "白银", icon: "i-ph-circle", color: "text-slate-400", minScore: 500 },
  { id: "gold", label: "黄金", icon: "i-ph-circle", color: "text-yellow-500", minScore: 1500 },
  { id: "platinum", label: "铂金", icon: "i-ph-diamond", color: "text-cyan-500", minScore: 3000 },
  { id: "diamond", label: "钻石", icon: "i-ph-diamond", color: "text-blue-500", minScore: 5000 },
  { id: "master", label: "大师", icon: "i-ph-crown", color: "text-purple-600", minScore: 8000 },
];

const totalScore = ref(0);

export function useRankTier() {
  const currentTier = computed(() => {
    let tier = TIERS[0];
    for (const t of TIERS) {
      if (totalScore.value >= t.minScore) tier = t;
    }
    return tier;
  });

  const nextTier = computed(() => {
    const idx = TIERS.findIndex((t) => t.id === currentTier.value.id);
    return idx < TIERS.length - 1 ? TIERS[idx + 1] : null;
  });

  const tierProgress = computed(() => {
    if (!nextTier.value) return 100;
    const current = currentTier.value.minScore;
    const next = nextTier.value.minScore;
    const progress = ((totalScore.value - current) / (next - current)) * 100;
    return Math.min(Math.max(Math.round(progress), 0), 100);
  });

  function setScore(score: number) {
    totalScore.value = score;
  }

  return { TIERS, currentTier, nextTier, tierProgress, totalScore, setScore };
}
