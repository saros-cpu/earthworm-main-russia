export interface WeaknessCategory {
  id: string;
  label: string;
  description: string;
  icon: string;
  keywords: string[];
  count?: number;
  suggestedPackId?: string;
  suggestedPackTitle?: string;
}

const CATEGORIES: WeaknessCategory[] = [
  {
    id: "cases",
    label: "名词变格",
    description: "名词的格（主/宾/生/与/工/前置）变化",
    icon: "i-ph-article",
    keywords: ["а", "я", "у", "ю", "ом", "ем", "ой", "ей", "ы", "и", "е", "ам", "ами", "ах"],
    suggestedPackId: "ru_grammar_cases",
    suggestedPackTitle: "名词变格专项",
  },
  {
    id: "verbs",
    label: "动词变位",
    description: "动词的时态和变位",
    icon: "i-ph-arrow-bend-right-up",
    keywords: ["ть", "л", "ла", "ло", "ли", "ю", "ут", "ют", "ат", "ят"],
    suggestedPackId: "ru_grammar_verbs",
    suggestedPackTitle: "动词变位专项",
  },
  {
    id: "prepositions",
    label: "前置词",
    description: "前置词的用法搭配",
    icon: "i-ph-link",
    keywords: [
      "в",
      "на",
      "о",
      "об",
      "при",
      "для",
      "без",
      "до",
      "из",
      "с",
      "ко",
      "за",
      "под",
      "над",
    ],
    suggestedPackId: "ru_grammar_prepositions",
    suggestedPackTitle: "前置词专项",
  },
  {
    id: "adjectives",
    label: "形容词变格",
    description: "形容词与名词的性数格一致",
    icon: "i-ph-palette",
    keywords: ["ый", "ий", "ой", "ая", "яя", "ое", "ее", "ые", "ие"],
    suggestedPackId: "ru_grammar_adjectives",
    suggestedPackTitle: "形容词变格专项",
  },
  {
    id: "pronouns",
    label: "代词变格",
    description: "人称代词和物主代词的变化",
    icon: "i-ph-users",
    keywords: ["меня", "тебя", "его", "её", "нам", "вам", "мне", "тебе"],
    suggestedPackId: "ru_grammar_pronouns",
    suggestedPackTitle: "代词专项",
  },
  {
    id: "numerals",
    label: "数词",
    description: "基数词和序数词的使用",
    icon: "i-ph-number-circle",
    keywords: [
      "один",
      "два",
      "три",
      "четыре",
      "пять",
      "шесть",
      "семь",
      "восемь",
      "девять",
      "десять",
    ],
    suggestedPackId: "ru_grammar_numerals",
    suggestedPackTitle: "数词专项",
  },
  {
    id: "tenses",
    label: "时态",
    description: "动词的过去/现在/将来时",
    icon: "i-ph-clock",
    keywords: ["буду", "будет", "будут", "был", "была", "было", "были"],
    suggestedPackId: "ru_grammar_tenses",
    suggestedPackTitle: "时态专项",
  },
];

let errorCounts: Record<string, number> = {};
let totalErrors = 0;

export function useWeaknessAnalysis() {
  function analyzeError(sentenceRussian: string) {
    const lower = sentenceRussian.toLowerCase();
    totalErrors++;
    for (const cat of CATEGORIES) {
      for (const kw of cat.keywords) {
        if (lower.includes(kw)) {
          errorCounts[cat.id] = (errorCounts[cat.id] || 0) + 1;
          break;
        }
      }
    }
  }

  function getWeaknesses(): WeaknessCategory[] {
    return CATEGORIES.map((c) => ({
      ...c,
      count: errorCounts[c.id] || 0,
    }))
      .filter((c) => c.count > 0)
      .sort((a, b) => b.count - a.count);
  }

  function getTotalErrors() {
    return totalErrors;
  }

  function reset() {
    errorCounts = {};
    totalErrors = 0;
  }

  function getTopWeakness() {
    const list = getWeaknesses();
    return list.length > 0 ? list[0] : null;
  }

  return { analyzeError, getWeaknesses, getTotalErrors, reset, getTopWeakness };
}
