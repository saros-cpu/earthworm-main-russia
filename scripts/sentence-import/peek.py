import json
from pathlib import Path
import sys

level = sys.argv[1] if len(sys.argv) > 1 else "A1"
start = int(sys.argv[2]) if len(sys.argv) > 2 else 18
count = int(sys.argv[3]) if len(sys.argv) > 3 else 15

obj = json.loads(Path(f"backend/src/main/resources/torfl/levels/{level}.json").read_text(encoding="utf-8"))
sents = obj["sentences"]
print(f"=== {level} sentences {start}-{start+count} (of {len(sents)}) ===\n")
for i, s in enumerate(sents[start:start + count], start=start + 1):
    print(f"  [{i}] RU: {s['russian']}")
    print(f"      ZH: {s['chinese']}\n")
