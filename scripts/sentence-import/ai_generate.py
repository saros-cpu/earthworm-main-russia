"""
Generate short, grammatically-correct Russian example sentences for a list of words
using OpenRouter (gpt-4o-mini by default). Each batch:
  - sends 10 terms
  - requests JSON array of {term, russian, chinese, note}
  - validates Cyrillic in russian + length 4-14 tokens
  - retries once on parse failure

Usage:
    set OPENROUTER_API_KEY=...
    python ai_generate.py --input <terms.json|jsonl> --output <out.jsonl> [--start 0] [--limit 50] [--batch 10]

Input formats:
  - .json:  list of "russian_term" strings, or list of {russian:..., chinese:..., (optional context fields)}
  - .jsonl: one such object per line

Output: jsonl, one object per line:
    {"term": "...", "russian": "...", "chinese": "...", "note": "..."}
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys
import time
import urllib.request
from pathlib import Path

API_KEY = os.environ.get("OPENROUTER_API_KEY") or ""
BASE_URL = os.environ.get("OPENROUTER_BASE_URL", "https://openrouter.ai/api/v1")
MODEL = os.environ.get("OPENROUTER_MODEL", "openai/gpt-4o-mini")

CYRILLIC = re.compile(r"[а-яёА-ЯЁ]")
CYRILLIC_TOKEN = re.compile(r"[а-яёА-ЯЁ][а-яёА-ЯЁ\-]*")

SYSTEM = (
    "You are a Russian language teaching assistant. "
    "For each Russian word or term provided, write ONE short practical Russian sentence "
    "(between 5 and 12 Cyrillic words) that uses the term in its correct grammatical form "
    "(declension/conjugation/aspect must all be correct). "
    "Then give a faithful, natural Chinese translation (simplified, ~12 characters or so). "
    "Optionally add a one-line `note` in Chinese explaining a tricky grammar point. "
    'Return ONLY a JSON object of shape {"results": [...]} where "results" is an array '
    "with EXACTLY ONE element per input term, in the same order, each element having "
    'string keys "term", "russian", "chinese", "note". Never collapse multiple inputs '
    "into one element. No prose, no markdown fences."
)


def load_terms(path: Path) -> list[dict]:
    terms: list[dict] = []
    if path.suffix.lower() == ".jsonl":
        for line in path.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if not line:
                continue
            obj = json.loads(line)
            terms.append(obj if isinstance(obj, dict) else {"term": str(obj)})
    else:
        data = json.loads(path.read_text(encoding="utf-8"))
        for item in data:
            if isinstance(item, str):
                terms.append({"term": item})
            else:
                terms.append(item)
    return terms


def chat(prompt_user: str, max_attempts: int = 2) -> str:
    """Make a single chat completion call, return raw assistant content."""
    if not API_KEY:
        raise RuntimeError("OPENROUTER_API_KEY not set")
    body = {
        "model": MODEL,
        "temperature": 0.3,
        "messages": [
            {"role": "system", "content": SYSTEM},
            {"role": "user", "content": prompt_user},
        ],
        "response_format": {"type": "json_object"},
    }
    payload = json.dumps(body).encode("utf-8")
    last_err = None
    for attempt in range(max_attempts):
        try:
            req = urllib.request.Request(
                f"{BASE_URL}/chat/completions",
                data=payload,
                headers={
                    "Content-Type": "application/json",
                    "Authorization": f"Bearer {API_KEY}",
                    "HTTP-Referer": "http://localhost:3000",
                    "X-Title": "Russian Learning Pipeline",
                },
                method="POST",
            )
            with urllib.request.urlopen(req, timeout=60) as r:
                obj = json.loads(r.read().decode("utf-8"))
            content = obj.get("choices", [{}])[0].get("message", {}).get("content")
            return content if isinstance(content, str) else ""
        except urllib.error.HTTPError as e:
            # 402 (out of credits), 401 (bad key), 403 (forbidden) are terminal — abort immediately.
            if e.code in (401, 402, 403):
                body = ""
                try:
                    body = e.read().decode("utf-8", errors="replace")[:300]
                except Exception:
                    pass
                raise SystemExit(f"[ai_generate] OpenRouter returned HTTP {e.code}: {body}\n"
                                 f"Likely cause: invalid key or insufficient credits. Top up at https://openrouter.ai/credits then re-run with --resume.")
            last_err = e
            time.sleep(1.5 * (attempt + 1))
        except Exception as e:
            last_err = e
            time.sleep(1.5 * (attempt + 1))
    raise RuntimeError(f"chat call failed after {max_attempts} attempts: {last_err}")


def parse_response(raw) -> list[dict]:
    """Parse the JSON content, tolerating common formatting quirks."""
    if not isinstance(raw, str) or not raw.strip():
        return []
    # Strip code fences
    raw = re.sub(r"^```(?:json)?\s*", "", raw.strip())
    raw = re.sub(r"\s*```$", "", raw.strip())
    try:
        obj = json.loads(raw)
    except Exception:
        # try to find array
        m = re.search(r"\[\s*\{.*?\}\s*\]", raw, flags=re.DOTALL)
        if not m:
            return []
        obj = json.loads(m.group(0))
    if isinstance(obj, dict):
        # Case 1: model returned ONE result object directly → wrap as singleton list
        if any(k in obj for k in ("russian", "russian_sentence", "ru")):
            obj = [obj]
        else:
            # Case 2: model wrapped in a dict like {"items": [...], "results": [...]}
            for v in obj.values():
                if isinstance(v, list):
                    obj = v
                    break
            else:
                obj = []
    return obj if isinstance(obj, list) else []


def validate(item) -> bool:
    if not isinstance(item, dict):
        return False
    ru = item.get("russian")
    zh = item.get("chinese")
    if not isinstance(ru, str) or not isinstance(zh, str):
        return False
    ru = ru.strip()
    zh = zh.strip()
    if not ru or not zh:
        return False
    if not CYRILLIC.search(ru):
        return False
    tokens = CYRILLIC_TOKEN.findall(ru)
    return 4 <= len(tokens) <= 16


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--input", required=True)
    ap.add_argument("--output", required=True)
    ap.add_argument("--start", type=int, default=0)
    ap.add_argument("--limit", type=int, default=0, help="max terms to process (0 = all)")
    ap.add_argument("--batch", type=int, default=10)
    ap.add_argument("--resume", action="store_true", help="skip terms already present in output file")
    args = ap.parse_args()

    in_path = Path(args.input)
    out_path = Path(args.output)
    out_path.parent.mkdir(parents=True, exist_ok=True)

    terms = load_terms(in_path)
    print(f">>> {len(terms)} input terms from {in_path}", file=sys.stderr)
    terms = terms[args.start:]
    if args.limit:
        terms = terms[: args.limit]
    print(f">>> processing {len(terms)} terms (start={args.start}, limit={args.limit or 'all'})", file=sys.stderr)

    done_terms: set[str] = set()
    if args.resume and out_path.exists():
        for line in out_path.read_text(encoding="utf-8").splitlines():
            try:
                done_terms.add(json.loads(line).get("term", ""))
            except Exception:
                pass
        print(f">>> resume: {len(done_terms)} terms already in output", file=sys.stderr)

    mode = "a" if args.resume else "w"
    written = 0
    failed = 0
    with out_path.open(mode, encoding="utf-8") as out:
        for i in range(0, len(terms), args.batch):
            batch = terms[i:i + args.batch]
            batch = [t for t in batch if t.get("term") not in done_terms and (t.get("russian") or t.get("term")) not in done_terms]
            if not batch:
                continue
            # Build input
            entries = []
            for t in batch:
                key = t.get("russian") or t.get("term")
                ctx = []
                if t.get("chinese"):
                    ctx.append(f"中文: {t['chinese']}")
                if t.get("pos"):
                    ctx.append(f"词性: {t['pos']}")
                ctx_str = "（" + ", ".join(ctx) + "）" if ctx else ""
                entries.append(f"{key}{ctx_str}")
            user_prompt = (
                "Generate practical example sentences for each Russian term below. "
                "Return strict JSON array, one object per term, with keys term/russian/chinese/note.\n\n"
                + "\n".join(f"{j+1}. {e}" for j, e in enumerate(entries))
            )
            try:
                raw = chat(user_prompt)
                items = parse_response(raw)
            except Exception as e:
                print(f"  batch {i}-{i+len(batch)} FAILED: {e}", file=sys.stderr)
                failed += len(batch)
                continue
            # Match items back to batch input. Prefer matching by `term` field
            # if the model echoes it, else fall back to positional alignment.
            keyed = {}
            for k, it in enumerate(items):
                if isinstance(it, dict):
                    term_key = (it.get("term") or "").strip().lower()
                    if term_key:
                        keyed.setdefault(term_key, it)
            for j, t in enumerate(batch):
                term_key = (t.get("russian") or t.get("term") or "").strip().lower()
                item = keyed.get(term_key)
                if item is None and j < len(items):
                    item = items[j]
                if validate(item):
                    note = item.get("note")
                    rec = {
                        "term": t.get("term") or t.get("russian"),
                        "russian": item["russian"].strip(),
                        "chinese": item["chinese"].strip(),
                        "note": (note.strip() if isinstance(note, str) else ""),
                    }
                    out.write(json.dumps(rec, ensure_ascii=False) + "\n")
                    out.flush()
                    written += 1
                else:
                    failed += 1
            print(f"  batch {i:>5}-{i+len(batch):<5}: ok={written}, fail={failed}", file=sys.stderr)

    print(f"\n>>> done. written={written}, failed={failed}, output={out_path}", file=sys.stderr)


if __name__ == "__main__":
    main()
