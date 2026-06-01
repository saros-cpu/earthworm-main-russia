"""Trim each level JSON back to only its original hand-curated words."""
import json
from pathlib import Path

CURATED = {"A1": 177, "A2": 142, "B1": 103, "B2": 79, "C1": 70, "C2": 61}
ORIGINAL_DESC = {
    "A1": "ТРКИ-A1 入门级核心 ~150 词 + 18 个基础对话例句。覆盖问候、人称、家庭、日常动词、时间、数字、基本名词、形容词、副词、疑问词、介词、连词。",
    "A2": "ТРКИ-A2 基础级核心 ~120 词 + 18 个日常生活例句。覆盖居家、出行、饮食、购物、天气、时间、人体、情感、常见动词与副词。",
    "B1": "ТРКИ-B1 一级核心 ~100 词 + 15 个社会与日常话题例句。覆盖工作、教育、媒体、抽象概念、常见动词、连接副词。",
    "B2": "ТРКИ-B2 二级核心 ~80 词 + 12 个职场与社会议题例句。覆盖企业、法律、抽象关系、动作动词、政策性形容词。",
    "C1": "ТРКИ-C1 三级核心 ~70 词 + 12 个学术与抽象表达例句。覆盖科研、思辨、复杂动词、高阶形容词。",
    "C2": "ТРКИ-C2 四级核心 ~60 词 + 12 个文学与政论高级例句。覆盖文学、政治、经济、哲学性动词与精细形容词。",
}

base = Path("backend/src/main/resources/torfl/levels")
for lvl, n in CURATED.items():
    p = base / f"{lvl}.json"
    obj = json.loads(p.read_text(encoding="utf-8"))
    obj["description"] = ORIGINAL_DESC[lvl]
    obj["words"] = obj["words"][:n]
    p.write_text(json.dumps(obj, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"{lvl}: kept {len(obj['words'])} curated words")
