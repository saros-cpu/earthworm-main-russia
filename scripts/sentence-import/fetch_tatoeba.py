"""
Fetch & assemble Tatoeba RU-ZH parallel sentences.

Output: scripts/sentence-import/output/ru_zh_pairs.jsonl
Each line: {"ru": "...", "zh": "...", "ru_id": int, "zh_id": int}
"""
from __future__ import annotations

import bz2
import json
import sys
import urllib.request
from pathlib import Path

URLS = {
    "rus": "https://downloads.tatoeba.org/exports/per_language/rus/rus_sentences_detailed.tsv.bz2",
    "cmn": "https://downloads.tatoeba.org/exports/per_language/cmn/cmn_sentences_detailed.tsv.bz2",
    "links": "https://downloads.tatoeba.org/exports/per_language/rus/rus-cmn_links.tsv.bz2",
}

CACHE = Path(__file__).parent / "output"
CACHE.mkdir(parents=True, exist_ok=True)


def fetch(name: str) -> bytes:
    cache_file = CACHE / f"{name}.bz2"
    if cache_file.exists():
        return cache_file.read_bytes()
    print(f">>> downloading {name} ({URLS[name]}) ...", file=sys.stderr)
    data = urllib.request.urlopen(URLS[name], timeout=120).read()
    cache_file.write_bytes(data)
    print(f"    got {len(data)} bytes", file=sys.stderr)
    return data


def load_sentences(name: str) -> dict[int, str]:
    raw = bz2.decompress(fetch(name)).decode("utf-8")
    out: dict[int, str] = {}
    for line in raw.splitlines():
        parts = line.split("\t")
        if len(parts) < 3:
            continue
        try:
            sid = int(parts[0])
        except ValueError:
            continue
        out[sid] = parts[2].strip()
    print(f"    loaded {len(out)} {name} sentences", file=sys.stderr)
    return out


def main() -> None:
    rus = load_sentences("rus")
    cmn = load_sentences("cmn")
    raw_links = bz2.decompress(fetch("links")).decode("utf-8")

    pairs = []
    for line in raw_links.splitlines():
        parts = line.split("\t")
        if len(parts) < 2:
            continue
        try:
            a, b = int(parts[0]), int(parts[1])
        except ValueError:
            continue
        # Links are bidirectional; a may be either rus or cmn
        if a in rus and b in cmn:
            pairs.append({"ru_id": a, "zh_id": b, "ru": rus[a], "zh": cmn[b]})
        elif b in rus and a in cmn:
            pairs.append({"ru_id": b, "zh_id": a, "ru": rus[b], "zh": cmn[a]})
    print(f"    assembled {len(pairs)} RU-ZH pairs", file=sys.stderr)

    # Dedup by ru_id (keep first zh)
    seen = {}
    for p in pairs:
        seen.setdefault(p["ru_id"], p)
    deduped = list(seen.values())
    print(f"    {len(deduped)} unique RU sentences with ZH translation", file=sys.stderr)

    out_path = CACHE / "ru_zh_pairs.jsonl"
    with out_path.open("w", encoding="utf-8") as f:
        for p in deduped:
            f.write(json.dumps(p, ensure_ascii=False) + "\n")
    print(f">>> wrote {out_path}")


if __name__ == "__main__":
    main()
