"""Probe entries.jsonl: find all entries with given cn headword, or all that gloss into given Russian lemma."""
import json
import re
import sys
from pathlib import Path

query = sys.argv[1] if len(sys.argv) > 1 else ""
mode = sys.argv[2] if len(sys.argv) > 2 else "cn"  # "cn" or "ru"
limit = int(sys.argv[3]) if len(sys.argv) > 3 else 30

count = 0
with open("scripts/dsl-import/output/entries.jsonl", encoding="utf-8") as f:
    for line in f:
        obj = json.loads(line)
        if mode == "cn":
            if obj.get("cn") == query:
                print(json.dumps(obj, ensure_ascii=False, indent=2))
                count += 1
        elif mode == "ru":
            for g in obj.get("ru", []):
                # Find first cyrillic token
                m = re.search(r"[а-яёА-ЯЁ][а-яёА-ЯЁ\-]+", g)
                if m and m.group(0).lower() == query.lower():
                    print(f"{obj['cn']:<10}  {g}")
                    count += 1
                    break
        if count >= limit:
            break
print(f"\n[{count} results]")
