type MorphologyKind =
  | "verb"
  | "noun"
  | "adjective"
  | "pronoun"
  | "preposition"
  | "conjunction"
  | "adverb"
  | "particle"
  | "other";

interface MorphologyHint {
  label: string;
  kind: MorphologyKind;
  note?: string;
}

const PREPOSITIONS = new Set([
  "в",
  "во",
  "на",
  "с",
  "со",
  "к",
  "ко",
  "у",
  "о",
  "об",
  "обо",
  "от",
  "до",
  "из",
  "за",
  "по",
  "под",
  "над",
  "перед",
  "при",
  "без",
  "для",
  "через",
]);

const CONJUNCTIONS = new Set(["и", "а", "но", "или", "что", "чтобы", "если", "когда", "потому"]);
const PARTICLES = new Set(["не", "ни", "ли", "же", "бы", "да"]);
const PRONOUNS = new Set([
  "я",
  "ты",
  "он",
  "она",
  "оно",
  "мы",
  "вы",
  "они",
  "меня",
  "тебя",
  "его",
  "её",
  "нас",
  "вас",
  "их",
  "мне",
  "тебе",
  "ему",
  "ей",
  "нам",
  "вам",
  "им",
  "мой",
  "твой",
  "наш",
  "ваш",
  "этот",
  "эта",
  "это",
  "эти",
]);

export function getRussianMorphologyHint(value: string): MorphologyHint {
  const word = value.toLocaleLowerCase("ru-RU").replace(/ё/g, "е");

  if (PREPOSITIONS.has(word)) {
    return { label: "介词", kind: "preposition" };
  }
  if (CONJUNCTIONS.has(word)) {
    return { label: "连词", kind: "conjunction" };
  }
  if (PARTICLES.has(word)) {
    return { label: "助词", kind: "particle" };
  }
  if (PRONOUNS.has(word)) {
    return { label: "代词", kind: "pronoun" };
  }
  if (/(ть|ться|ешь|ете|ет|ют|ут|ит|им|ишь|ят|ат|л|ла|ли)$/.test(word)) {
    return { label: "动词", kind: "verb", note: "变位" };
  }
  if (/(ый|ий|ой|ая|яя|ое|ее|ые|ие|ого|его|ому|ему|ым|им|ую|юю)$/.test(word)) {
    return { label: "形容词", kind: "adjective" };
  }
  if (/(о|е)$/.test(word) && word.length > 4) {
    return { label: "副词", kind: "adverb" };
  }
  if (/(а|я)$/.test(word)) {
    return { label: "名词", kind: "noun", note: "阴/主格" };
  }
  if (/(у|ю)$/.test(word)) {
    return { label: "名词", kind: "noun", note: "宾/与格" };
  }
  if (/(ом|ем|ой|ей)$/.test(word)) {
    return { label: "名词", kind: "noun", note: "工具/前置格" };
  }
  if (/(ы|и)$/.test(word)) {
    return { label: "名词", kind: "noun", note: "复数/属格" };
  }

  return { label: "词", kind: "other" };
}

export function getRussianMorphologyClasses(value: string) {
  const hint = getRussianMorphologyHint(value);
  return {
    verb: "text-rose-600 border-b-rose-400 dark:text-rose-300 dark:border-b-rose-500",
    noun: "text-sky-700 border-b-sky-400 dark:text-sky-300 dark:border-b-sky-500",
    adjective: "text-violet-700 border-b-violet-400 dark:text-violet-300 dark:border-b-violet-500",
    pronoun: "text-amber-700 border-b-amber-400 dark:text-amber-300 dark:border-b-amber-500",
    preposition: "text-emerald-700 border-b-emerald-400 dark:text-emerald-300 dark:border-b-emerald-500",
    conjunction: "text-cyan-700 border-b-cyan-400 dark:text-cyan-300 dark:border-b-cyan-500",
    adverb: "text-indigo-700 border-b-indigo-400 dark:text-indigo-300 dark:border-b-indigo-500",
    particle: "text-slate-600 border-b-slate-400 dark:text-slate-300 dark:border-b-slate-500",
    other: "text-slate-700 border-b-slate-300 dark:text-slate-200 dark:border-b-slate-500",
  }[hint.kind];
}

export function getRussianMorphologyBadgeClasses(value: string) {
  const hint = getRussianMorphologyHint(value);
  return {
    verb: "bg-rose-50 text-rose-700 dark:bg-rose-950 dark:text-rose-300",
    noun: "bg-sky-50 text-sky-700 dark:bg-sky-950 dark:text-sky-300",
    adjective: "bg-violet-50 text-violet-700 dark:bg-violet-950 dark:text-violet-300",
    pronoun: "bg-amber-50 text-amber-700 dark:bg-amber-950 dark:text-amber-300",
    preposition: "bg-emerald-50 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300",
    conjunction: "bg-cyan-50 text-cyan-700 dark:bg-cyan-950 dark:text-cyan-300",
    adverb: "bg-indigo-50 text-indigo-700 dark:bg-indigo-950 dark:text-indigo-300",
    particle: "bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300",
    other: "bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300",
  }[hint.kind];
}
