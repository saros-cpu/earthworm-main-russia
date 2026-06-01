import csv
from pathlib import Path

p = Path("scripts/dsl-import/output/reports/A1_quality.csv")
rows = list(csv.DictReader(p.open(encoding="utf-8-sig")))
print(f"Total rows: {len(rows)}")

cur = [r for r in rows if r["source"] == "curated"]
auto = [r for r in rows if r["source"] == "bkrs-auto"]
print(f"curated: {len(cur)}, auto: {len(auto)}")
print()

print("=== curated samples (first 3) ===")
for r in cur[:3]:
    print(f"  {r['russian']:<20} {r['chinese']:<25} {r['confidence']}/{r['action']}")
print()

print("=== auto · HIGH (HSK1-3 hit) — first 12 ===")
for r in [x for x in auto if x["confidence"] == "high"][:12]:
    print(f"  {r['russian']:<22} {r['chinese']:<28} hsk={r['hsk_band']}")
print()

print("=== auto · MEDIUM (HSK4-6) — first 12 ===")
for r in [x for x in auto if x["confidence"] == "medium"][:12]:
    print(f"  {r['russian']:<22} {r['chinese']:<28} hsk={r['hsk_band']}")
print()

print("=== auto · LOW · action=replace — first 12 ===")
for r in [x for x in auto if x["action"] == "replace"][:12]:
    print(f"  {r['russian']:<22} {r['chinese']:<28} notes={r['notes']}")
print()

print("=== auto · LOW · action=review — first 12 ===")
revs = [x for x in auto if x["action"] == "review"]
print(f"  (total review={len(revs)})")
for r in revs[:12]:
    print(f"  {r['russian']:<22} {r['chinese']:<28} notes={r['notes']}")
