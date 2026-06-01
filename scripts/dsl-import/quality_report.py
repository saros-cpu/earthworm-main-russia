"""
Generate quality audit reports for A1-C2 level word lists.

For each level, output a CSV file under scripts/dsl-import/output/reports/
with columns:

    rank, russian, chinese, pos, source, hsk_match, confidence, suggested_action, notes

Where:
  - source        : "curated" (one of the 632 hand-entries) or "bkrs-auto"
  - hsk_match     : "HSK1-3" / "HSK4-6" / "off-list" / "n/a"
  - confidence    : "high" / "medium" / "low"
  - suggested_action: "ok" / "review" / "replace" / "drop"
  - notes         : human-readable hints

Curated-vs-auto detection: we cannot reliably tag rows after the user has saved files,
so we re-parse the original A?.json structure: the first N words per level (177 / 142 / 103 / 79 / 70 / 61)
are curated.

HSK match: uses the same HSK 1-6 word list as build_levels.py, and we also load HSK 1-3
separately for "core daily" classification.

Usage:
    python quality_report.py
"""

from __future__ import annotations

import csv
import json
import re
import sys
import urllib.request
from pathlib import Path

HERE = Path(__file__).parent
LEVELS_DIR = HERE.parent.parent / "backend" / "src" / "main" / "resources" / "torfl" / "levels"
OUT_DIR = HERE / "output" / "reports"
HSK_DIR = HERE / "output" / "hsk_by_level"

CURATED_COUNT = {"A1": 177, "A2": 142, "B1": 103, "B2": 79, "C1": 70, "C2": 61}
HSK_URLS = {i: f"https://raw.githubusercontent.com/glxxyz/hskhsk.com/master/data/lists/HSK%20Official%202012%20L{i}.txt"
            for i in range(1, 7)}

HAS_HAN = re.compile(r"[\u4e00-\u9fff]")
RARE_CHAR = re.compile(r"[\u3400-\u4dbf\U00020000-\U0002ffff\uf900-\ufaff]")  # CJK Ext A/B/etc + CJK Compat
HAS_CJK_PUNCT = re.compile(r"[…；、，；！？]")


def download_hsk_levels() -> dict[int, set[str]]:
    HSK_DIR.mkdir(parents=True, exist_ok=True)
    out = {}
    for lvl, url in HSK_URLS.items():
        f = HSK_DIR / f"hsk{lvl}.txt"
        if not f.exists():
            print(f">>> downloading {f.name} ...", file=sys.stderr)
            data = urllib.request.urlopen(url).read().decode("utf-8", errors="replace")
            f.write_text(data, encoding="utf-8")
        words = set()
        for line in f.read_text(encoding="utf-8").splitlines():
            w = line.strip().lstrip("\ufeff").lstrip("?")
            if w and all(0x4E00 <= ord(c) <= 0x9FFF for c in w):
                words.add(w)
        out[lvl] = words
    return out


def classify_hsk(cn: str, hsk: dict[int, set[str]]) -> str:
    parts = [p for p in re.split(r"[;；]", cn) if p.strip()]
    if not parts:
        return "n/a"
    best_lvl = None
    for p in parts:
        for lvl in range(1, 7):
            if p.strip() in hsk[lvl]:
                if best_lvl is None or lvl < best_lvl:
                    best_lvl = lvl
                break
    if best_lvl is None:
        return "off-list"
    if best_lvl <= 3:
        return f"HSK{best_lvl}"
    return f"HSK{best_lvl}"


def score_entry(russian: str, chinese: str, pos: str, source: str, hsk_band: str) -> tuple[str, str, str]:
    """Returns (confidence, suggested_action, notes)."""
    notes_parts = []
    if source == "curated":
        return "high", "ok", "hand-curated"

    # Auto entries — apply heuristics
    if RARE_CHAR.search(chinese):
        notes_parts.append("含罕用/扩展汉字")
    if HAS_CJK_PUNCT.search(chinese):
        notes_parts.append("含 CJK 标点")
    if not HAS_HAN.search(chinese):
        notes_parts.append("无汉字")

    pieces = [p.strip() for p in re.split(r"[;；]", chinese) if p.strip()]
    primary = pieces[0] if pieces else ""
    primary_len = len(primary)

    if hsk_band in ("HSK1", "HSK2", "HSK3"):
        confidence = "high"
        action = "ok"
        notes_parts.append(f"{hsk_band} 核心")
    elif hsk_band in ("HSK4", "HSK5", "HSK6"):
        confidence = "medium"
        action = "ok"
        notes_parts.append(f"{hsk_band} 较书面")
    elif RARE_CHAR.search(chinese):
        confidence = "low"
        action = "replace"
        notes_parts.append("含罕用扩展字，建议替换")
    elif primary_len >= 5:
        confidence = "low"
        action = "review"
        notes_parts.append("首选释义偏长")
    elif primary_len <= 1 and primary not in {"是", "有", "我", "你", "他", "她", "们", "和", "在", "了", "也"}:
        confidence = "low"
        action = "review"
        notes_parts.append("首选释义为生僻单字")
    else:
        confidence = "medium"
        action = "review"
        notes_parts.append("不在 HSK 词表内")

    # 高频功能词（前 200 freq）若无 HSK 命中，强制 review
    return confidence, action, "; ".join(notes_parts)


def main() -> None:
    hsk_levels = download_hsk_levels()
    print("    HSK sizes:", {k: len(v) for k, v in hsk_levels.items()}, file=sys.stderr)
    OUT_DIR.mkdir(parents=True, exist_ok=True)

    summary_rows = []
    for lvl, curated_n in CURATED_COUNT.items():
        path = LEVELS_DIR / f"{lvl}.json"
        if not path.exists():
            print(f"[skip] {path} missing", file=sys.stderr)
            continue
        obj = json.loads(path.read_text(encoding="utf-8"))
        words = obj.get("words", [])

        out_csv = OUT_DIR / f"{lvl}_quality.csv"
        counts = {"high": 0, "medium": 0, "low": 0}
        actions = {"ok": 0, "review": 0, "replace": 0, "drop": 0}
        with out_csv.open("w", encoding="utf-8-sig", newline="") as f:
            w = csv.writer(f)
            w.writerow(["rank_in_level", "russian", "chinese", "pos", "source", "hsk_band", "confidence", "action", "notes"])
            for i, item in enumerate(words, start=1):
                ru = item.get("russian", "")
                cn = item.get("chinese", "")
                pos = item.get("pos", "")
                source = "curated" if i <= curated_n else "bkrs-auto"
                hsk_band = classify_hsk(cn, hsk_levels)
                conf, act, notes = score_entry(ru, cn, pos, source, hsk_band)
                counts[conf] = counts.get(conf, 0) + 1
                actions[act] = actions.get(act, 0) + 1
                w.writerow([i, ru, cn, pos, source, hsk_band, conf, act, notes])

        row = {
            "level": lvl,
            "total": len(words),
            "curated": curated_n,
            "auto": len(words) - curated_n,
            **{f"conf_{k}": v for k, v in counts.items()},
            **{f"act_{k}": v for k, v in actions.items()},
        }
        summary_rows.append(row)
        print(f"  {lvl}: total={len(words)}, curated={curated_n}, "
              f"high={counts['high']}, medium={counts['medium']}, low={counts['low']}, "
              f"ok={actions['ok']}, review={actions['review']}, replace={actions['replace']}", file=sys.stderr)
        print(f"  → {out_csv}", file=sys.stderr)

    # Aggregated summary CSV
    summary_path = OUT_DIR / "summary.csv"
    if summary_rows:
        keys = list(summary_rows[0].keys())
        with summary_path.open("w", encoding="utf-8-sig", newline="") as f:
            w = csv.DictWriter(f, fieldnames=keys)
            w.writeheader()
            for r in summary_rows:
                w.writerow(r)
        print(f"\n>>> summary: {summary_path}", file=sys.stderr)

        # Print tabular summary to stdout
        print("\n等级  总计   手工    自动    高     中     低     ok    审核   替换")
        for r in summary_rows:
            print(f"  {r['level']:<3} {r['total']:>5} {r['curated']:>6} {r['auto']:>6} "
                  f"{r.get('conf_high',0):>5} {r.get('conf_medium',0):>5} {r.get('conf_low',0):>5} "
                  f"{r.get('act_ok',0):>5} {r.get('act_review',0):>5} {r.get('act_replace',0):>5}")


if __name__ == "__main__":
    main()
