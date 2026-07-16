"""
enrich-torfl.py
===============
为 TORFL (ТРКИ) 课程包中的语句补充语法注释 (grammar_note)。

使用 OpenRouter (GPT-4o-mini) 逐条生成语法注释并写入 statement_refinements 表。

用法:
    python scripts/enrich-torfl.py [--batch BATCH] [--delay DELAY] [--dry-run]

参数:
    --batch BATCH      每批处理的语句数 (默认: 10)
    --delay DELAY      每批之间等待秒数 (默认: 2)
    --dry-run          仅预览，不写入数据库
    --level LEVEL      只处理指定等级 (如 A1, B2)，不传则处理全部
    --force            重新生成已有注释的语句
    --db-url URL       数据库 URL (默认: mysql+pymysql://{DB_USERNAME}:{DB_PASSWORD}@localhost:3306/earthworm)
"""

import argparse
import json
import os
import re
import sys
import time
from typing import Any

try:
    import pymysql
    from openai import OpenAI
except ImportError as e:
    print(f"缺少依赖: {e}")
    print("请安装: pip install pymysql openai")
    sys.exit(1)

_DB_USER = os.getenv("DB_USERNAME", "reader")
_DB_PASS = os.getenv("DB_PASSWORD", "")
DB_URL = os.getenv("DB_URL", f"mysql+pymysql://{_DB_USER}:{_DB_PASS}@localhost:3306/earthworm")
OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY") or os.getenv("OPENAI_API_KEY", "")
OPENROUTER_MODEL = os.getenv("OPENROUTER_MODEL", "openai/gpt-4o-mini")
OPENROUTER_BASE_URL = os.getenv("OPENROUTER_BASE_URL", "https://openrouter.ai/api/v1")

LEVELS = ["A1", "A2", "B1", "B2", "C1", "C2"]

SYSTEM_PROMPT = """你是一位俄语语法专家。分析下面的俄语句子，提供简要的语法注释。
注释应指出关键语法现象，例如：
- 名词的性、数、格变化
- 动词的变位、时态、体
- 形容词与名词的性数一致
- 前置词的用法
- 从句类型
- 其他重要语法特征

格式要求：
- 用中文写
- 简洁，1-3句话
- 只写语法点，不翻译句子
- 如无明显语法特徵，注明"基础陈述句"即可

示例：
句子: Я читаю книгу.
注释: "читать" 第一变位法现在时第一人称单数；"книга" 第四格作直接宾语。

句子: Я пошёл в магазин, чтобы купить хлеб.
注释: "пойти" 完成体过去时；"чтобы" 引导目的从句，主从句主语一致时用不定式。"""


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="为 TORFL 语句补充语法注释")
    parser.add_argument("--batch", type=int, default=10, help="每批处理语句数")
    parser.add_argument("--delay", type=int, default=2, help="每批之间等待秒数")
    parser.add_argument("--dry-run", action="store_true", help="仅预览不写入")
    parser.add_argument("--level", type=str, default="", help="只处理指定等级 (如 A1)")
    parser.add_argument("--force", action="store_true", help="重新生成已有注释的语句")
    return parser.parse_args()


def build_dsn(url: str) -> dict:
    m = re.match(
        r"mysql\+pymysql://([^:]+):([^@]+)@([^:/]+):?(\d+)?/([^?]+)",
        url,
    )
    if not m:
        raise ValueError(f"无法解析数据库 URL: {url}")
    return {
        "host": m.group(3),
        "port": int(m.group(4) or 3306),
        "user": m.group(1),
        "password": m.group(2),
        "database": m.group(5),
        "charset": "utf8mb4",
    }


def get_connection(dsn: dict):
    return pymysql.connect(**dsn)


def fetch_torfl_statements(conn, level_filter: str = "", force: bool = False) -> list[dict]:
    sql = """
        SELECT s.id, s.english, s.chinese, c.id AS course_id, cp.id AS pack_id, cp.title AS pack_title
        FROM statements s
        JOIN courses c ON c.id = s.course_id
        JOIN course_packs cp ON cp.id = c.course_pack_id
        WHERE cp.id LIKE 'torfl-%'
    """
    params: list[Any] = []
    if level_filter:
        sql += " AND cp.id LIKE %s"
        params.append(f"torfl-{level_filter.lower()}-%")
    sql += " ORDER BY cp.id, c.`order`, s.`order`"
    params_tuple = tuple(params)
    with conn.cursor() as cur:
        cur.execute(sql, params_tuple)
        rows = cur.fetchall()

    if not force:
        enriched_ids = fetch_enriched_ids(conn, [r[0] for r in rows])
        rows = [r for r in rows if r[0] not in enriched_ids]

    return [
        {
            "id": r[0],
            "english": r[1],
            "chinese": r[2],
            "courseId": r[3],
            "packId": r[4],
            "packTitle": r[5],
        }
        for r in rows
    ]


def fetch_enriched_ids(conn, statement_ids: list[str]) -> set[str]:
    if not statement_ids:
        return set()
    placeholders = ",".join(["%s"] * len(statement_ids))
    sql = f"SELECT statement_id FROM statement_refinements WHERE statement_id IN ({placeholders}) AND grammar_note IS NOT NULL AND grammar_note != ''"
    with conn.cursor() as cur:
        cur.execute(sql, statement_ids)
        return {r[0] for r in cur.fetchall()}


def generate_grammar_note(client: OpenAI, english: str, chinese: str) -> str | None:
    try:
        resp = client.chat.completions.create(
            model=OPENROUTER_MODEL,
            messages=[
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": f"句子: {english}\n翻译: {chinese}"},
            ],
            temperature=0.3,
            max_tokens=200,
        )
        return resp.choices[0].message.content.strip()
    except Exception as e:
        print(f"  [!] API 调用失败: {e}")
        return None


def upsert_grammar_note(conn, statement_id: str, grammar_note: str):
    sql = """
        INSERT INTO statement_refinements (statement_id, grammar_note, refinement_mode)
        VALUES (%s, %s, 'ai')
        ON DUPLICATE KEY UPDATE grammar_note = VALUES(grammar_note), refinement_mode = 'ai'
    """
    with conn.cursor() as cur:
        cur.execute(sql, (statement_id, grammar_note))
    conn.commit()


def main():
    args = parse_args()
    dsn = build_dsn(DB_URL)

    if not OPENROUTER_API_KEY:
        print("错误: 请设置 OPENROUTER_API_KEY 或 OPENAI_API_KEY 环境变量")
        sys.exit(1)

    client = OpenAI(api_key=OPENROUTER_API_KEY, base_url=OPENROUTER_BASE_URL)

    print(f"数据库: {dsn['host']}:{dsn['port']}/{dsn['database']}")
    print(f"模型: {OPENROUTER_MODEL}")
    print(f"每批: {args.batch} 条, 间隔: {args.delay}s")
    if args.level:
        print(f"等级过滤: {args.level.upper()}")
    if args.dry_run:
        print("[DRY RUN] 不会写入数据库")
    if args.force:
        print("[FORCE] 将重新生成已有注释的语句")
    print()

    conn = get_connection(dsn)
    try:
        statements = fetch_torfl_statements(conn, args.level.upper(), args.force)
        print(f"获取到 {len(statements)} 条待处理语句\n")

        total = len(statements)
        success = 0
        skip = 0
        fail = 0

        for i in range(0, total, args.batch):
            batch = statements[i : i + args.batch]
            print(f"[{i + 1}-{i + len(batch)}/{total}] 处理中...")

            for stmt in batch:
                english = stmt["english"]
                chinese = stmt["chinese"]
                pack = stmt["packTitle"]

                if args.dry_run:
                    print(f"  [{pack}] {english[:50]}... -> (语法注释)")
                    skip += 1
                    continue

                note = generate_grammar_note(client, english, chinese)
                if note:
                    upsert_grammar_note(conn, stmt["id"], note)
                    success += 1
                    print(f"  ✓ [{pack}] {english[:40]}... -> {note[:60]}...")
                else:
                    fail += 1
                    print(f"  ✗ [{pack}] {english[:40]}... 生成失败")

            if i + args.batch < total:
                time.sleep(args.delay)

        print(f"\n完成! 成功: {success}, 跳过: {skip}, 失败: {fail}")

    finally:
        conn.close()


if __name__ == "__main__":
    main()
