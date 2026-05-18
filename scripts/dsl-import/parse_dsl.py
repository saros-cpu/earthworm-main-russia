"""
Stream-parse 大БКРС DSL files (UTF-16 LE) → JSONL of (chinese, pinyin, russian_meanings, raw_body).

Each entry in DSL:
    <headword non-indent>
    \t<body line 1>
    \t<body line 2>
    ...
    <blank line>

Body lines may use DSL markup: [m1] [m2] [p] [i] [c] [ref] [ex] [*] [s]...[/s] (sounds) etc.

Usage:
    python parse_dsl.py <dsl_dir> [<output.jsonl>]

Default output: output/entries.jsonl
"""

from __future__ import annotations

import json
import os
import re
import sys
from pathlib import Path
from typing import Iterator, List, Optional

# DSL inline tags that wrap content we want to KEEP plain text from
TAG_RE = re.compile(r"\[/?[a-zA-Z\*][a-zA-Z0-9_]*(\s+[^\]]*)?\]")
# Reference like [ref]xxx[/ref] — we keep inner text
# Sound like [s]xxx.wav[/s] — we drop
SOUND_RE = re.compile(r"\[s\][^\[\]]*\[/s\]")
# Collapsible sub-section [*]...[/*] contains example expansions: DROP entirely (non-greedy)
COLLAPSIBLE_RE = re.compile(r"\[\*\].*?\[/\*\]", re.DOTALL)
TRN_RE = re.compile(r"\[trn\]|\[/trn\]|\[trs\]|\[/trs\]")
ESCAPED_BRACKETS = {"\\[": "[", "\\]": "]", "\\\\": "\\"}

# Heuristic: does this text contain at least one Cyrillic letter?
HAS_CYRILLIC = re.compile(r"[\u0400-\u04FF]")
# Cyrillic-only word (single token, may include hyphen/ё)
CYRILLIC_WORD = re.compile(r"^[а-яёА-ЯЁ\-]+$")


def strip_dsl(body: str) -> str:
    """Strip DSL markup, leaving plain text."""
    body = SOUND_RE.sub("", body)
    body = COLLAPSIBLE_RE.sub("", body)
    body = TRN_RE.sub("", body)
    # Replace tags with spaces (so consecutive tags don't glue tokens)
    body = TAG_RE.sub(" ", body)
    # Unescape \[ \] \\
    for k, v in ESCAPED_BRACKETS.items():
        body = body.replace(k, v)
    # Collapse whitespace
    body = re.sub(r"[ \t]+", " ", body)
    body = re.sub(r" *\n *", "\n", body)
    return body.strip()


def split_meanings(plain_body: str) -> List[str]:
    """
    Split a multi-meaning body into individual Russian gloss strings.

    Pattern handling:
    - Lines starting with "N)" mark separate senses
    - Each line is one [m1] block (already separated by \n after strip_dsl)
    - Parenthetical Russian italics like "(в Тибете)" are kept as context
    """
    meanings: List[str] = []
    for line in plain_body.split("\n"):
        line = line.strip()
        if not line:
            continue
        # Strip leading sense number "1)" / "2." etc
        line = re.sub(r"^\d+[\.\)]\s*", "", line)
        # Strip leading bullet markers
        line = re.sub(r"^[•\-·]\s*", "", line)
        if line:
            meanings.append(line)
    return meanings


def extract_russian_glosses(plain_body: str) -> List[str]:
    """
    From the plain text body, extract glosses that are likely Russian translations.
    Returns at most 5 distinct short glosses.
    """
    glosses: List[str] = []
    seen = set()
    for meaning in split_meanings(plain_body):
        if not HAS_CYRILLIC.search(meaning):
            continue
        # Cut off after example markers we missed
        # Keep only up to the first ";" or ":" if followed by example-ish text
        gloss = meaning.strip()
        # Drop trailing parenthetical context if too long
        # Keep gloss as-is for now; downstream can refine
        if gloss not in seen:
            seen.add(gloss)
            glosses.append(gloss)
        if len(glosses) >= 5:
            break
    return glosses


def iter_dsl_entries(path: Path) -> Iterator[tuple]:
    """Yield (headword, pinyin_or_None, raw_body_lines:list[str]) from a single DSL file."""
    # The DSL file is UTF-16 LE with BOM; python's "utf-16" handles BOM automatically.
    headword: Optional[str] = None
    pinyin: Optional[str] = None
    body_lines: List[str] = []
    in_header = True

    with open(path, "r", encoding="utf-16", errors="replace") as f:
        for raw in f:
            line = raw.rstrip("\n").rstrip("\r")
            if in_header:
                # Skip #NAME, #INDEX_LANGUAGE etc until first non-# line
                if line.startswith("#") or not line.strip():
                    continue
                in_header = False

            if not line.strip():
                # Entry separator
                if headword is not None and body_lines:
                    yield headword, pinyin, body_lines
                headword = None
                pinyin = None
                body_lines = []
                continue

            if line.startswith((" ", "\t")):
                # Body line
                stripped = line.strip()
                # First indented line of an entry is typically pinyin (no brackets, ASCII+tone marks)
                if pinyin is None and not stripped.startswith("[") and "[" not in stripped:
                    pinyin = stripped if stripped != "_" else None
                else:
                    body_lines.append(stripped)
            else:
                # New headword. Flush previous entry first.
                if headword is not None and body_lines:
                    yield headword, pinyin, body_lines
                headword = line.strip()
                pinyin = None
                body_lines = []

        # Final flush
        if headword is not None and body_lines:
            yield headword, pinyin, body_lines


def main() -> None:
    if len(sys.argv) < 2:
        print("Usage: python parse_dsl.py <dsl_dir> [<output.jsonl>]", file=sys.stderr)
        sys.exit(2)

    dsl_dir = Path(sys.argv[1])
    out_path = Path(sys.argv[2]) if len(sys.argv) > 2 else Path(__file__).parent / "output" / "entries.jsonl"
    out_path.parent.mkdir(parents=True, exist_ok=True)

    dsl_files = sorted(dsl_dir.glob("*.dsl"))
    if not dsl_files:
        print(f"No .dsl files in {dsl_dir}", file=sys.stderr)
        sys.exit(2)

    total = 0
    russian_total = 0
    with out_path.open("w", encoding="utf-8") as out:
        for dsl in dsl_files:
            print(f">>> parsing {dsl.name} ...", file=sys.stderr)
            local = 0
            for headword, pinyin, body_lines in iter_dsl_entries(dsl):
                raw_body = "\n".join(body_lines)
                plain = strip_dsl(raw_body)
                ru_glosses = extract_russian_glosses(plain)
                if not ru_glosses:
                    total += 1
                    local += 1
                    continue
                out.write(
                    json.dumps(
                        {
                            "cn": headword,
                            "pinyin": pinyin,
                            "ru": ru_glosses,
                        },
                        ensure_ascii=False,
                    )
                    + "\n"
                )
                total += 1
                local += 1
                russian_total += 1
                if local % 50000 == 0:
                    print(f"  {dsl.name}: {local} entries scanned ({russian_total} with Russian)", file=sys.stderr)
            print(f"  done {dsl.name}: {local} entries", file=sys.stderr)
    print(f"Total entries scanned: {total}; with Russian glosses: {russian_total}", file=sys.stderr)
    print(f"Output: {out_path}")


if __name__ == "__main__":
    main()
