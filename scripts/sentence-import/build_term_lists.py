"""
Prepare term-list JSON files for the AI sentence generator.

Two outputs:
  - output/oil_terms.json   — all oil_engineering terms ({russian, chinese, pos? from soundmark})
  - output/c2_missing_terms.json — C2 words that don't yet have an example sentence in C2.json
"""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(__file__).parent / "output"
OUT.mkdir(parents=True, exist_ok=True)

CYR = re.compile(r"[а-яёА-ЯЁ]")


def build_oil():
    src = ROOT / "backend/src/main/resources/customs/oil_engineering.json"
    pack = json.loads(src.read_text(encoding="utf-8"))
    seen = set()
    terms = []
    for course in pack.get("courses", []):
        for s in course.get("statements", []):
            ru = (s.get("english") or "").strip()
            zh = (s.get("chinese") or "").strip()
            if not ru or not zh or not CYR.search(ru):
                continue
            key = ru.lower()
            if key in seen:
                continue
            seen.add(key)
            terms.append({"russian": ru, "chinese": zh})
    (OUT / "oil_terms.json").write_text(json.dumps(terms, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"  oil: {len(terms)} unique terms -> output/oil_terms.json")


def build_c2():
    src = ROOT / "backend/src/main/resources/torfl/levels/C2.json"
    obj = json.loads(src.read_text(encoding="utf-8"))
    existing_sentence_words = set()
    for s in obj.get("sentences", []):
        ru = (s.get("russian") or "").lower()
        for tok in re.findall(r"[а-яё][а-яё\-]+", ru):
            existing_sentence_words.add(tok)
    missing = []
    for w in obj.get("words", []):
        ru = (w.get("russian") or "").strip()
        if not ru:
            continue
        # consider "missing" if the lemma form is not already in any existing sentence (loose check)
        if ru.lower() not in existing_sentence_words:
            missing.append({"russian": ru, "chinese": w.get("chinese", ""), "pos": w.get("pos", "")})
    (OUT / "c2_missing_terms.json").write_text(json.dumps(missing, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"  C2: {len(missing)}/{len(obj.get('words', []))} words still need sentences -> output/c2_missing_terms.json")


if __name__ == "__main__":
    build_oil()
    build_c2()
