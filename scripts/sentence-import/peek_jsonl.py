import json
import sys
from pathlib import Path

p = Path(sys.argv[1])
limit = int(sys.argv[2]) if len(sys.argv) > 2 else 10
for i, line in enumerate(p.read_text(encoding="utf-8").splitlines()[:limit], 1):
    o = json.loads(line)
    print(f"[{i}] term: {o.get('term')}")
    print(f"    RU: {o.get('russian')}")
    print(f"    ZH: {o.get('chinese')}")
    if o.get("note"):
        print(f"    Note: {o['note']}")
    print()
