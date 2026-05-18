"""
Convert 俄语工程石油加油站财务政府术语库 CSV → JSON seed file.

Output: backend/src/main/resources/customs/oil_engineering.json
Structure: ONE pack with courses grouped by 一级分类.

Usage:
    python build_oil_engineering.py [csv_path]
"""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
import sys
from collections import defaultdict
from pathlib import Path

LESSON_SIZE = 10
PACK_ID = "ru-oil-station"
PACK_TITLE = "加油站·石油·工程俄语 · 行业术语库"
PACK_DESCRIPTION = (
    "面向中俄能源贸易、油气工程、政府对接等场景的俄语术语库。"
    "覆盖加油站运营、设备维修、维修故障代码、消防安全、IT 系统、政府外联、财务税务、行政人事、采购库存等 18 大类。"
)
PACK_COVER = "https://images.unsplash.com/photo-1545209463-e2825498edbf?q=80&w=1200&auto=format&fit=crop"
DEFAULT_INPUT = Path(r"C:\Users\Administrator\Downloads\Compressed\dsl_dabkrs_250920\俄语工程石油加油站财务政府术语库_3000词_含维修故障代码_UTF8.csv")
DEFAULT_OUTPUT = Path(__file__).resolve().parents[2] / "backend/src/main/resources/customs/oil_engineering.json"

CATEGORY_ORDER = [
    "加油站运营",
    "消防安全",
    "设备维修",
    "工程维修",
    "工程施工",
    "石油设备",
    "石油油品",
    "IT系统",
    "IT系统/数据可视化",
    "维修故障代码库",
    "政府外联",
    "政府外联/内部管理",
    "行政管理",
    "人事行政",
    "财务税务",
    "采购管理",
    "库存管理",
    "销售管理",
]


def short_hash(s: str) -> str:
    return hashlib.sha1(s.encode("utf-8")).hexdigest()[:8]


def slug(s: str) -> str:
    return re.sub(r"[^a-zA-Z0-9]+", "", s)


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("csv_path", nargs="?", default=str(DEFAULT_INPUT))
    ap.add_argument("--output", default=str(DEFAULT_OUTPUT))
    args = ap.parse_args()

    csv_path = Path(args.csv_path)
    out_path = Path(args.output)
    out_path.parent.mkdir(parents=True, exist_ok=True)

    print(f">>> loading {csv_path} ...", file=sys.stderr)
    by_cat: dict[str, list[dict]] = defaultdict(list)
    total = 0

    with csv_path.open(encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            ru = (row.get("俄语") or "").strip()
            zh = (row.get("中文") or "").strip()
            if not ru or not zh:
                continue
            c1 = (row.get("一级分类") or "其它").strip()
            c2 = (row.get("二级分类") or "").strip()
            kind = (row.get("类型") or "").strip()
            code = (row.get("故障代码") or "").strip()
            abbr = (row.get("缩写/关键词") or "").strip()
            sm_parts = []
            if code:
                sm_parts.append(f"代码 {code}")
            if abbr:
                sm_parts.append(abbr)
            if c2:
                sm_parts.append(c2)
            if kind:
                sm_parts.append(kind)
            soundmark = " ｜ ".join(sm_parts) or ru
            by_cat[c1].append({
                "english": ru,
                "chinese": zh,
                "soundmark": soundmark,
            })
            total += 1

    # Order categories
    ordered_cats = [c for c in CATEGORY_ORDER if c in by_cat] + [c for c in by_cat if c not in CATEGORY_ORDER]

    courses: list[dict] = []
    course_order = 0
    for cat in ordered_cats:
        items = by_cat[cat]
        cat_slug = slug(cat) or short_hash(cat)
        # Split into lessons of LESSON_SIZE
        lesson_idx = 0
        for i in range(0, len(items), LESSON_SIZE):
            lesson_idx += 1
            course_order += 1
            chunk = items[i:i + LESSON_SIZE]
            lo = i + 1
            hi = i + len(chunk)
            course_id = f"{PACK_ID}-{cat_slug}-{lesson_idx}-{short_hash(cat + str(lesson_idx))}"
            courses.append({
                "id": course_id,
                "order": course_order,
                "title": f"{cat} · 第 {lesson_idx} 课（{lo}-{hi}）",
                "description": f"{cat} 主题术语 {lo}-{hi}，看中文输入俄语术语；soundmark 含故障代码/缩写/二级分类。",
                "statements": chunk,
            })
        print(f"  {cat:<24s} {len(items):>4d} items → {lesson_idx} courses", file=sys.stderr)

    pack = {
        "id": PACK_ID,
        "title": PACK_TITLE,
        "description": f"{PACK_DESCRIPTION}\n\n共 {total} 条术语，{len(courses)} 节课。",
        "cover": PACK_COVER,
        "isFree": True,
        "courses": courses,
    }

    out_path.write_text(json.dumps(pack, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f">>> wrote {out_path} ({len(courses)} courses, {total} statements)")


if __name__ == "__main__":
    main()
