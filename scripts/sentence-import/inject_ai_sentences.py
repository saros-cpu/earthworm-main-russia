"""
Merge AI-generated sentence JSONL files back into the seed JSON files.

For oil_engineering.json:
  - Append a new top-level set of "例句课" courses (10 sentences per lesson).
  - Updates pack description with sentence-course count.

For C2.json:
  - Append sentences to the "sentences" array (so TorflPackService creates "组词成句" courses).

Run AFTER `ai_generate.py` finishes its batches. Re-run is safe — content is
appended only if missing.
"""
from __future__ import annotations

import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).parent
OUT = HERE / "output"

OIL_PACK = ROOT / "backend/src/main/resources/customs/oil_engineering.json"
C2_LEVEL = ROOT / "backend/src/main/resources/torfl/levels/C2.json"


def short_hash(s: str) -> str:
    return hashlib.sha1(s.encode("utf-8")).hexdigest()[:8]


def load_jsonl(path: Path) -> list[dict]:
    if not path.exists():
        return []
    out = []
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line:
            continue
        try:
            out.append(json.loads(line))
        except Exception:
            pass
    return out


def merge_oil():
    sents = load_jsonl(OUT / "oil_sentences.jsonl")
    if not sents:
        print("  oil: no sentences file or empty, skip")
        return
    pack = json.loads(OIL_PACK.read_text(encoding="utf-8"))
    existing_ids = {c.get("id") for c in pack.get("courses", [])}
    existing_ru = set()
    for c in pack.get("courses", []):
        for s in c.get("statements", []):
            existing_ru.add((s.get("english") or "").strip())

    # Filter duplicates
    fresh = [s for s in sents if s.get("russian") and s["russian"] not in existing_ru]
    print(f"  oil: {len(sents)} AI sentences → {len(fresh)} new (after dedup)")

    # Build new courses, 10 statements each, appended at the end
    pack_id = pack["id"]
    next_order = max((c.get("order", 0) for c in pack.get("courses", [])), default=0)
    added_courses = 0
    for i in range(0, len(fresh), 10):
        chunk = fresh[i:i + 10]
        next_order += 1
        lesson_num = i // 10 + 1
        cid = f"{pack_id}-ai-sent-{lesson_num}-{short_hash('ai-sent-' + str(lesson_num))}"
        if cid in existing_ids:
            continue
        statements = []
        for k, s in enumerate(chunk, start=1):
            statements.append({
                "english": s["russian"],
                "chinese": s["chinese"],
                "soundmark": ("术语: " + s.get("term", "") + (" ｜ " + s["note"] if s.get("note") else "")),
            })
        pack["courses"].append({
            "id": cid,
            "order": next_order,
            "title": f"AI 例句课 第 {lesson_num} 课（{i+1}-{i+len(chunk)}）",
            "description": f"由 AI 在术语基础上生成的实用句子（已自动校验俄语词形 + 句长）。",
            "statements": statements,
        })
        existing_ids.add(cid)
        added_courses += 1

    # Update pack description marker (idempotent)
    pack["description"] = re.sub(r"\s*\+\s*\d+\s*节 AI 例句课.*$", "", pack["description"]).strip()
    pack["description"] += f"\n+ {added_courses} 节 AI 例句课 / {len(fresh)} 条句子（gpt-4o-mini 生成 + 词形校验）。"
    OIL_PACK.write_text(json.dumps(pack, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"  oil: +{added_courses} 例句课 (+{len(fresh)} 句) → {OIL_PACK.name}")


def merge_c2():
    sents = load_jsonl(OUT / "c2_sentences.jsonl")
    if not sents:
        print("  C2: no sentences file or empty, skip")
        return
    obj = json.loads(C2_LEVEL.read_text(encoding="utf-8"))
    existing = obj.get("sentences", [])
    existing_keys = {s.get("russian", "").strip() for s in existing}
    added = 0
    for s in sents:
        if not s.get("russian") or s["russian"] in existing_keys:
            continue
        existing.append({"russian": s["russian"], "chinese": s["chinese"]})
        existing_keys.add(s["russian"])
        added += 1
    obj["sentences"] = existing
    obj["description"] = re.sub(r"\s*\+\s*\d+\s*句 AI.*$", "", obj.get("description", "")).strip()
    obj["description"] = (obj.get("description", "") + f"\n+ {added} 句 AI 生成例句（针对 C2 高级词汇）。").strip()
    C2_LEVEL.write_text(json.dumps(obj, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"  C2: +{added} sentences → {C2_LEVEL.name} (total now {len(existing)})")


if __name__ == "__main__":
    merge_oil()
    merge_c2()
