import json, os, re

CUSTOMS = r"D:\earthworm-main\backend\src\main\resources\customs"
os.makedirs(CUSTOMS, exist_ok=True)

def stmt(ru, zh, ph):
    return {"english": ru, "chinese": zh, "soundmark": ph}

PACKS = []

