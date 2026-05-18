"""
Build RU→CN reverse index from entries.jsonl, then assign each Russian lemma
to a TORFL level based on its rank in a Russian frequency list. Output JSON files
for each level under backend/src/main/resources/torfl/levels/.

Steps:
  1. Read entries.jsonl
  2. For every entry's Russian gloss, extract a principal lemma (first Cyrillic
     token) and record the (chinese_headword, gloss) with a quality score.
  3. For each Russian lemma, keep the cleanest Chinese gloss.
  4. Read freq list (hingston/russian top 10000)
  5. Slot lemmas by rank into A1/A2/B1/B2/C1 buckets:
        A1: 1-760
        A2: 761-1300
        B1: 1301-2300
        B2: 2301-6000
        C1: 6001-10000
  6. Merge with hand-curated existing words (so curated entries take priority),
     then write each level JSON.

Usage:
    python build_levels.py [--entries path] [--freq path] [--output-dir path]
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import urllib.request
from collections import defaultdict
from pathlib import Path
from typing import Dict, List, Optional, Tuple

HERE = Path(__file__).parent
DEFAULT_ENTRIES = HERE / "output" / "entries.jsonl"
DEFAULT_FREQ = HERE / "output" / "ru_freq_10k.txt"
DEFAULT_HSK = HERE / "output" / "hsk_all.txt"
DEFAULT_OUTPUT_DIR = HERE.parent.parent / "backend" / "src" / "main" / "resources" / "torfl" / "levels"
HINGSTON_URL = "https://raw.githubusercontent.com/hingston/russian/master/10000-russian-words.txt"


def load_hsk(path: Path) -> set:
    """Load HSK vocabulary words (one per line, possibly with BOM)."""
    if not path.exists():
        return set()
    words = set()
    for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        w = line.strip().lstrip("\ufeff").lstrip("?")
        if w and all(0x4E00 <= ord(c) <= 0x9FFF for c in w):
            words.add(w)
    return words

CYRILLIC_TOKEN = re.compile(r"[а-яёА-ЯЁ][а-яёА-ЯЁ\-]*")
HAS_CYRILLIC = re.compile(r"[\u0400-\u04FF]")

# Slot boundaries (1-indexed rank, inclusive upper bound)
LEVEL_RANGES: List[Tuple[str, int, int]] = [
    ("A1", 1, 760),
    ("A2", 761, 1300),
    ("B1", 1301, 2300),
    ("B2", 2301, 6000),
    ("C1", 6001, 10000),
]


def download_freq(path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    print(f">>> downloading freq list to {path} ...", file=sys.stderr)
    urllib.request.urlretrieve(HINGSTON_URL, path)
    print(f"    done ({path.stat().st_size} bytes)", file=sys.stderr)


def guess_pos(gloss: str) -> str:
    """Very rough POS guess from a Russian gloss string."""
    g = gloss.strip().lower()
    # If gloss contains multiple words: phrase
    tokens = CYRILLIC_TOKEN.findall(g)
    if not tokens:
        return "其他"
    word = tokens[0]
    if len(tokens) >= 3:
        return "词组"
    if word.endswith(("ться", "ться")) or word.endswith("ть") or word.endswith("ти") or word.endswith("чь"):
        return "动词"
    if word.endswith(("ый", "ий", "ой", "ая", "яя", "ое", "ее", "ые", "ие")):
        return "形容词"
    if word.endswith(("ость", "есть", "ция", "сия", "тия", "жия", "ика", "ура", "иня")):
        return "名词"
    if word.endswith(("ие", "ье", "тво", "ство", "ние", "ция")):
        return "名词"
    if word.endswith(("а", "я")):
        return "名词"
    if word.endswith(("о", "е")) and len(word) <= 6:
        # Could be adverb or short neuter noun; default to adverb when short
        return "副词"
    if word.endswith(("о", "е")):
        return "名词"
    # Consonant ending: most likely masculine noun
    return "名词"


def gloss_quality(gloss: str) -> int:
    """Lower is better. Prefer short, clean glosses."""
    g = gloss.strip()
    score = len(g)
    # Penalize parenthetical context (italic abbreviation, etc)
    if "(" in g or "（" in g:
        score += 20
    # Penalize examples / colons
    if ":" in g or "：" in g:
        score += 50
    # Penalize commas (multiple synonyms — still useful but more verbose)
    score += g.count(",") * 5
    # Penalize "см. xxx" cross-references heavily (they redirect)
    if re.search(r"\bсм\.\b", g):
        score += 200
    if re.search(r"\bустар\.\b|\bуст\.\b", g):
        score += 50
    return score


def normalize_chinese(cn: str) -> str:
    """Strip common annotations from the Chinese headword."""
    cn = cn.strip()
    # Remove anything in trailing parens like "三比西河 (河名)"
    cn = re.sub(r"\s*[\(（].*?[\)）]\s*$", "", cn)
    return cn


# Allow 1-4 Russian synonyms separated by , or ;
DIRECT_EQUIV_RE = re.compile(r"^\s*[а-яёА-ЯЁ\-]+(\s*[,;]\s*[а-яёА-ЯЁ\-]+){0,3}\s*$")
SUBSENSE_RE = re.compile(r"\s*\d+\)\s*.*$", re.DOTALL)  # cut "2) жарг. xxx" off


def build_ru_to_cn(entries_path: Path, hsk: Optional[set] = None) -> Dict[str, Tuple[str, str, str]]:
    """
    Returns: russian_lemma_lower → (chinese, gloss, pos)

    STRICT: only accepts entries where the Russian gloss is essentially just the
    lemma itself (optionally with up to 3 synonyms separated by commas, plus
    optional bracketed context that we strip). This filters out collocation
    entries that would yield garbage like "对于 来说" for "для".

    For each lemma, prefer entries where the Chinese headword:
      - has length 1-4 chars
      - contains no Latin / digits / punctuation
      - has no obvious parenthetical
    """
    candidates: Dict[str, List[Tuple[int, str, str, str]]] = defaultdict(list)
    n_lines = 0
    accepted = 0
    with entries_path.open("r", encoding="utf-8") as f:
        for line in f:
            n_lines += 1
            try:
                obj = json.loads(line)
            except Exception:
                continue
            cn = normalize_chinese(obj.get("cn") or "")
            if not cn:
                continue
            # Filter out Chinese headwords containing latin/digits/spaces/punct
            if re.search(r"[A-Za-z0-9\s\.\-_/]", cn):
                continue
            if not (1 <= len(cn) <= 5):
                continue
            for gloss in obj.get("ru", []):
                gloss = gloss.strip()
                if not gloss:
                    continue
                # Cut off subsense like "2) жарг. xxx" (keep only sense 1)
                gloss = SUBSENSE_RE.sub("", gloss).strip()
                # Strip bracketed context like "(книжн.)" "(перен.)"
                core = re.sub(r"\s*[\(（].*?[\)）]\s*", " ", gloss).strip()
                core = re.sub(r"\s+", " ", core)
                # Strip leading topic labels like "ист.", "перен.", "уст.", "жарг." etc.
                core = re.sub(r"^(?:[а-яё]{1,8}\.\s+)+", "", core, flags=re.IGNORECASE)
                # Strip trailing topic labels in case
                core = re.sub(r"\s+(?:[а-яё]{1,8}\.\s*)+$", "", core, flags=re.IGNORECASE).strip()
                if not core:
                    continue
                # Skip cross-references
                low = core.lower()
                if low.startswith("см.") or low.startswith("то же"):
                    continue
                # Require direct equivalence form
                if not DIRECT_EQUIV_RE.match(core):
                    continue
                tokens = CYRILLIC_TOKEN.findall(core)
                if not tokens:
                    continue
                lemma = tokens[0].lower()
                if len(lemma) <= 1:
                    continue
                score = 0
                # Prefer single-synonym
                score += core.count(",") * 5
                score += core.count(";") * 5
                # Prefer short Chinese
                score += len(cn) * 3
                # Penalize rare CJK chars (outside BMP basic range)
                if any(ord(c) > 0x9FFF for c in cn):
                    score += 500
                # HSK whitelist: STRONG bonus if entire Chinese gloss is an HSK word
                if hsk and cn in hsk:
                    score -= 5000
                pos = guess_pos(core)
                candidates[lemma].append((score, cn, core, pos))
                accepted += 1
            if n_lines % 200000 == 0:
                print(f"    scanned {n_lines} lines, {len(candidates)} unique lemmas, {accepted} accepted glosses", file=sys.stderr)

    # Pick best per lemma using score (HSK strong bonus + voting).
    result: Dict[str, Tuple[str, str, str]] = {}
    skipped_low_evidence = 0
    no_hsk_match = 0
    for lemma, items in candidates.items():
        items.sort(key=lambda x: x[0])
        # Skip if total evidence is too weak (just one entry and not in HSK)
        if hsk and not any(it[1] in hsk for it in items) and len(items) <= 1:
            skipped_low_evidence += 1
            continue
        # Prefer top item if it is HSK
        best = items[0]
        # Take top distinct CN glosses, prefer HSK first
        seen_cn = []
        for sc, cn, gl, pos in items:
            if cn not in seen_cn:
                seen_cn.append(cn)
            if len(seen_cn) >= 3:
                break
        # Reorder: HSK first
        if hsk:
            seen_cn.sort(key=lambda c: (0 if c in hsk else 1))
        agg_cn = ";".join(seen_cn[:2])
        pos = guess_pos(best[2])
        if hsk and best[1] not in hsk:
            no_hsk_match += 1
        result[lemma] = (agg_cn, best[2], pos)
    print(f"    skipped {skipped_low_evidence} lemmas with too-weak evidence", file=sys.stderr)
    if hsk:
        print(f"    {no_hsk_match}/{len(result)} lemmas had NO HSK-matching CN gloss", file=sys.stderr)
    return result


def load_freq_list(path: Path) -> List[str]:
    words: List[str] = []
    with path.open("r", encoding="utf-8") as f:
        for line in f:
            w = line.strip()
            if w and HAS_CYRILLIC.search(w):
                words.append(w.lower())
    return words


def slot_by_level(freq_words: List[str]) -> Dict[str, List[Tuple[int, str]]]:
    slots: Dict[str, List[Tuple[int, str]]] = {lvl: [] for lvl, _, _ in LEVEL_RANGES}
    for idx, word in enumerate(freq_words, start=1):
        for lvl, lo, hi in LEVEL_RANGES:
            if lo <= idx <= hi:
                slots[lvl].append((idx, word))
                break
    return slots


def load_existing_level(level_file: Path) -> Tuple[dict, set]:
    """Load existing curated level JSON; return (full_obj, lowercase_lemmas_already_present)."""
    if not level_file.exists():
        return {
            "title": f"TORFL {level_file.stem} · 等级核心词汇与例句",
            "description": "",
            "words": [],
            "sentences": [],
        }, set()
    obj = json.loads(level_file.read_text(encoding="utf-8"))
    seen = set()
    for w in obj.get("words", []):
        ru = w.get("russian") or ""
        seen.add(ru.lower())
        # Also seed lookups for first cyrillic token (for multi-word entries)
        tokens = CYRILLIC_TOKEN.findall(ru)
        if tokens:
            seen.add(tokens[0].lower())
    return obj, seen


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--entries", default=str(DEFAULT_ENTRIES))
    ap.add_argument("--freq", default=str(DEFAULT_FREQ))
    ap.add_argument("--output-dir", default=str(DEFAULT_OUTPUT_DIR))
    ap.add_argument("--no-download", action="store_true", help="Skip freq list download")
    args = ap.parse_args()

    entries_path = Path(args.entries)
    freq_path = Path(args.freq)
    out_dir = Path(args.output_dir)

    if not entries_path.exists():
        print(f"entries file not found: {entries_path}", file=sys.stderr)
        sys.exit(2)
    if not freq_path.exists():
        if args.no_download:
            print(f"freq file not found: {freq_path}", file=sys.stderr)
            sys.exit(2)
        download_freq(freq_path)

    print(">>> loading HSK vocabulary ...", file=sys.stderr)
    hsk = load_hsk(DEFAULT_HSK)
    print(f"    {len(hsk)} HSK words", file=sys.stderr)

    print(">>> building RU→CN index ...", file=sys.stderr)
    ru_to_cn = build_ru_to_cn(entries_path, hsk=hsk)
    print(f"    {len(ru_to_cn)} unique Russian lemmas", file=sys.stderr)

    print(">>> loading freq list ...", file=sys.stderr)
    freq_words = load_freq_list(freq_path)
    print(f"    {len(freq_words)} freq words", file=sys.stderr)

    slots = slot_by_level(freq_words)
    for lvl, _, _ in LEVEL_RANGES:
        print(f"    {lvl}: {len(slots[lvl])} ranks", file=sys.stderr)

    out_dir.mkdir(parents=True, exist_ok=True)

    summary = {}
    for lvl, _, _ in LEVEL_RANGES:
        level_file = out_dir / f"{lvl}.json"
        obj, existing_lemmas = load_existing_level(level_file)
        curated_count = len(obj.get("words", []))

        added = 0
        missing = 0
        for rank, word in slots[lvl]:
            if word in existing_lemmas:
                continue  # already curated
            entry = ru_to_cn.get(word)
            if not entry:
                missing += 1
                continue
            cn, gloss, pos = entry
            obj.setdefault("words", []).append({
                "russian": word,
                "chinese": cn,
                "pos": pos,
            })
            existing_lemmas.add(word)
            added += 1

        # Bump description
        total = len(obj["words"])
        sent_count = len(obj.get("sentences", []))
        old_desc = obj.get("description", "")
        # Strip any previous "(N 词 ...)" suffix
        base_desc = re.sub(r"\s*ТРКИ-[\w]+.*", "", old_desc).strip() or old_desc
        obj["description"] = f"{base_desc}\n\n词表共 {total} 词 + {sent_count} 例句（{curated_count} 条手工编辑 + {added} 条由 БКРС 词典按 Leeds 频次自动补全）。"

        level_file.write_text(json.dumps(obj, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        summary[lvl] = {"curated": curated_count, "added": added, "missing": missing, "total": total}
        print(f"  {lvl}: curated={curated_count}, added={added}, missing={missing}, total={total}", file=sys.stderr)

    print("\nDone. Summary:")
    print(json.dumps(summary, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
