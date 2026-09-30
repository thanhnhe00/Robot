#!/usr/bin/env python3
"""Tạo label, milestone và issue trên GitHub từ docs/issues/*.md.

Yêu cầu: GitHub CLI (`gh`) đã cài và đăng nhập (`gh auth login`), chạy trong thư mục repo.
Chạy lại nhiều lần an toàn: issue trùng tiêu đề sẽ bị bỏ qua.

    python scripts/create_issues.py --dry-run   # xem trước, không gọi GitHub
    python scripts/create_issues.py             # tạo thật
"""
import argparse
import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ISSUES_DIR = Path(__file__).resolve().parent.parent / "docs" / "issues"

LABELS = {
    "epic": ("5319e7", "Nhóm việc lớn của một phase"),
    "research": ("0e8a16", "Nghiên cứu"),
    "docs": ("0075ca", "Tài liệu"),
    "safety": ("b60205", "Liên quan an toàn"),
    "hardware": ("fbca04", "Liên quan phần cứng"),
    "evaluation": ("1d76db", "Đánh giá / benchmark"),
    "testing": ("c5def5", "Kiểm thử"),
    "cross-cutting": ("bfdadc", "Xuyên suốt nhiều phase"),
}


def parse(path: Path) -> dict:
    m = re.match(r"^---\n(.*?)\n---\n(.*)$", path.read_text(encoding="utf-8"), re.DOTALL)
    if not m:
        raise ValueError(f"{path.name}: thiếu front matter")
    meta = {}
    for line in m.group(1).splitlines():
        key, _, value = line.partition(":")
        meta[key.strip()] = value.strip()
    return {
        "title": meta["title"],
        "labels": [x.strip() for x in meta.get("labels", "").split(",") if x.strip()],
        "milestone": meta.get("milestone", ""),
        "state": meta.get("state", "open"),
        "body": m.group(2).strip(),
        "file": path.name,
    }


def gh(args, dry, capture=True):
    print("  $ gh " + " ".join(a if " " not in a else f'"{a[:40]}"' for a in args))
    if dry:
        return ""
    r = subprocess.run(["gh", *args], capture_output=True, text=True, encoding="utf-8")
    if r.returncode != 0:
        print("    !", r.stderr.strip())
        return None
    return r.stdout.strip()


def label_color(name: str):
    if name in LABELS:
        return LABELS[name]
    if name.startswith("phase-"):
        return ("ededed", "Phase " + name[6:])
    return ("ededed", "")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()
    dry = args.dry_run

    if not dry and not shutil.which("gh"):
        sys.exit("Không tìm thấy `gh`. Cài GitHub CLI rồi chạy `gh auth login`.")

    issues = [parse(p) for p in sorted(ISSUES_DIR.glob("[0-9]*.md"))]
    print(f"Đọc được {len(issues)} issue từ {ISSUES_DIR}")

    print("\n== Labels ==")
    for name in sorted({l for i in issues for l in i["labels"]}):
        color, desc = label_color(name)
        gh(["label", "create", name, "--color", color, "--description", desc, "--force"], dry)

    print("\n== Milestones ==")
    existing_ms = set()
    if not dry:
        out = gh(["api", "repos/{owner}/{repo}/milestones?state=all&per_page=100", "--jq", ".[].title"], dry)
        existing_ms = set((out or "").splitlines())
    for ms in dict.fromkeys(i["milestone"] for i in issues if i["milestone"]):
        if ms in existing_ms:
            print(f"  (đã có) {ms}")
            continue
        gh(["api", "repos/{owner}/{repo}/milestones", "-f", f"title={ms}"], dry)

    print("\n== Issues ==")
    existing = set()
    if not dry:
        out = gh(["issue", "list", "--state", "all", "--limit", "500", "--json", "title"], dry)
        existing = {x["title"] for x in json.loads(out or "[]")}
    for i in issues:
        if i["title"] in existing:
            print(f"  (đã có) {i['title']}")
            continue
        cmd = ["issue", "create", "--title", i["title"], "--body", i["body"]]
        for l in i["labels"]:
            cmd += ["--label", l]
        if i["milestone"]:
            cmd += ["--milestone", i["milestone"]]
        url = gh(cmd, dry)
        if url and i["state"] == "closed":
            gh(["issue", "close", url.splitlines()[-1], "--reason", "completed"], dry)
    print("\nXong." if not dry else "\n(dry-run: chưa gọi GitHub)")


if __name__ == "__main__":
    main()
