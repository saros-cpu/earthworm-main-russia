import json
import sys
from pathlib import Path

level = sys.argv[1] if len(sys.argv) > 1 else "A1"
start = int(sys.argv[2]) if len(sys.argv) > 2 else 0
count = int(sys.argv[3]) if len(sys.argv) > 3 else 40

path = Path("backend/src/main/resources/torfl/levels") / f"{level}.json"
obj = json.loads(path.read_text(encoding="utf-8"))
ws = obj["words"]
print(f"{level}: total {len(ws)}")
for i, w in enumerate(ws[start : start + count], start=start + 1):
    print(f"  {i:>4}  {w['russian']:<22} {w['pos']:<6} {w['chinese']}")
