"""Evaluation Harness v0 – Chạy golden test set qua provider và tính điểm.

Sử dụng:
    python scripts/evaluate.py                    # dùng provider trong .env
    python scripts/evaluate.py --provider mock    # dùng MockProvider
    python scripts/evaluate.py --provider ollama  # cần Ollama đang chạy

Kết quả lưu vào docs/benchmarks/phase2-eval-v0.md (nếu có --save).
"""

from __future__ import annotations

import argparse
import asyncio
import json
import os
import sys
import time
from pathlib import Path
from typing import Any

# Đặt cwd vào backend để import app.*
REPO_ROOT = Path(__file__).resolve().parents[1]
BACKEND = REPO_ROOT / "backend"
sys.path.insert(0, str(BACKEND))

os.environ.setdefault("LLM_PROVIDER", "mock")
os.environ.setdefault("API_KEY", "")
os.environ.setdefault("DB_PATH", ":memory:")
os.environ.setdefault("PERSISTENCE_ENABLED", "false")


def load_golden_set(path: Path) -> list[dict[str, Any]]:
    items: list[dict[str, Any]] = []
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line:
                items.append(json.loads(line))
    return items


async def run_eval(provider_name: str, golden_path: Path) -> dict[str, Any]:
    """Chạy eval và trả về kết quả tổng hợp."""
    # Import sau khi đã set env
    os.environ["LLM_PROVIDER"] = provider_name

    # Reload modules để pick up provider mới
    import importlib

    import app.config
    import app.llm
    import app.llm.factory
    import app.service

    importlib.reload(app.config)
    importlib.reload(app.llm.factory)
    importlib.reload(app.llm)

    from app.config import Settings

    new_settings = Settings()
    app.service.settings = new_settings  # type: ignore[attr-defined]
    app.service.provider = app.llm.get_provider()  # type: ignore[attr-defined]

    from app.actions import parse_model_output
    from app.prompt import RETRY_HINT, SYSTEM_PROMPT

    provider = app.service.provider

    golden = load_golden_set(golden_path)
    results: list[dict[str, Any]] = []

    total = len(golden)
    skipped = 0
    json_valid = 0
    action_correct = 0
    params_correct = 0
    intent_correct = 0
    tool_selection_correct = 0
    latencies: list[float] = []

    for i, sample in enumerate(golden):
        input_text = sample["input"]
        expected_action = sample.get("expected_action")
        expected_params = sample.get("expected_params")
        expected_intent = sample.get("intent")  # intent category nếu có trong golden
        sample_id = sample.get("id", f"#{i}")

        # Skip các input rỗng (bị chặn bởi validation đầu vào)
        if not input_text:
            results.append({
                "id": sample_id,
                "skipped": True,
                "reason": "empty input (bị chặn bởi ChatRequest validation, min_length=1)",
            })
            skipped += 1
            total -= 1
            continue

        messages = [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": input_text},
        ]

        start = time.perf_counter()
        try:
            raw = await provider.generate(messages)
        except Exception as e:  # noqa: BLE001
            results.append({
                "id": sample_id,
                "error": str(e),
                "json_valid": False,
                "action_match": False,
                "params_match": False,
                "intent_match": False,
                "tool_selection_match": False,
            })
            continue
        elapsed_ms = (time.perf_counter() - start) * 1000
        latencies.append(elapsed_ms)

        # Parse
        is_json_valid = True
        try:
            parsed = parse_model_output(raw)
        except ValueError:
            # Retry 1 lần
            try:
                raw = await provider.generate(
                    [*messages, {"role": "user", "content": RETRY_HINT}]
                )
                parsed = parse_model_output(raw)
            except ValueError:
                is_json_valid = False
                parsed = None

        if is_json_valid and parsed is not None:
            json_valid += 1

        # So sánh action
        actual_action = parsed.action.type if parsed and parsed.action else None
        action_match = actual_action == expected_action
        if action_match:
            action_correct += 1

        # Tool selection: model có chọn đúng tool (action type) hay không?
        # Khác action_match ở chỗ: tool selection chỉ cần type đúng,
        # kể cả khi validator reject params. Dùng raw action type trước validate.
        # Vì parse_model_output giờ validate trong, ta dùng action_match + rejection check.
        actual_proposed = None
        if parsed and parsed.action:
            actual_proposed = parsed.action.type
        elif parsed and parsed.action_rejection:
            # Action bị reject nhưng model đã đề xuất đúng type
            # Trích type từ rejection reason (format: "reason:type")
            parts = (parsed.action_rejection or "").split(":")
            if len(parts) >= 2:
                actual_proposed = parts[-1]
        tool_match = actual_proposed == expected_action if expected_action else actual_action is None
        if tool_match:
            tool_selection_correct += 1

        # So sánh params
        actual_params = parsed.action.params if parsed and parsed.action else None
        # Nếu expected_params là null, chấp nhận actual_params là None hoặc {}
        if expected_params is None:
            params_match = actual_params is None or actual_params == {}
        elif actual_params is None:
            params_match = False
        else:
            # So sánh từng key trong expected_params
            params_match = True
            for key, val in expected_params.items():
                if actual_params.get(key) != val:
                    params_match = False
                    break
        if params_match and action_match:
            params_correct += 1

        # Intent match: nếu golden set có trường "intent", so sánh
        intent_match = None
        if expected_intent:
            # Intent được suy từ action type:
            # - expected_action is None → "chat"
            # - expected_action is not None → expected_action
            expected_intent_cat = expected_intent
            if actual_action is None and expected_action is None:
                actual_intent_cat = "chat"
            elif actual_action:
                actual_intent_cat = actual_action
            else:
                actual_intent_cat = "chat"  # model không đề xuất action
            intent_match = actual_intent_cat == expected_intent_cat
            if intent_match:
                intent_correct += 1

        results.append({
            "id": sample_id,
            "input": input_text[:60],
            "expected_action": expected_action,
            "actual_action": actual_action,
            "json_valid": is_json_valid,
            "action_match": action_match,
            "params_match": params_match,
            "tool_selection_match": tool_match,
            "intent_match": intent_match,
            "action_rejection": parsed.action_rejection if parsed else None,
            "latency_ms": round(elapsed_ms, 2),
        })

    # Tính tổng
    json_rate = (json_valid / total * 100) if total else 0
    action_rate = (action_correct / total * 100) if total else 0
    params_rate = (params_correct / total * 100) if total else 0
    tool_rate = (tool_selection_correct / total * 100) if total else 0
    # Intent rate chỉ tính khi golden set có trường intent
    samples_with_intent = sum(1 for d in results if d.get("intent_match") is not None)
    intent_rate = (intent_correct / samples_with_intent * 100) if samples_with_intent else None
    avg_latency = sum(latencies) / len(latencies) if latencies else 0
    p95_latency = sorted(latencies)[int(len(latencies) * 0.95)] if latencies else 0

    summary = {
        "provider": provider_name,
        "model": new_settings.model_name,
        "prompt_version": new_settings.prompt_version,
        "total_samples": total,
        "skipped_samples": skipped,
        "json_validity_pct": round(json_rate, 1),
        "action_accuracy_pct": round(action_rate, 1),
        "params_accuracy_pct": round(params_rate, 1),
        "tool_selection_accuracy_pct": round(tool_rate, 1),
        "intent_accuracy_pct": round(intent_rate, 1) if intent_rate is not None else None,
        "avg_latency_ms": round(avg_latency, 2),
        "p95_latency_ms": round(p95_latency, 2),
    }

    return {"summary": summary, "details": results}


def format_report(data: dict[str, Any]) -> str:
    """Tạo markdown report."""
    s = data["summary"]
    is_mock = s["provider"] == "mock"

    lines = [
        "# Phase 2 – Evaluation v0 Report",
        "",
    ]

    if is_mock:
        lines += [
            "> **⚠️ DISCLAIMER**: Báo cáo này chạy bằng **MockProvider** (rule-based, không phải AI model).",
            "> Kết quả phản ánh khả năng pattern matching của mock, **không phải benchmark AI thật**.",
            "> Không sử dụng số liệu này trong portfolio hoặc so sánh model.",
            "",
        ]

    lines += [
        "## Tổng quan",
        "",
        "| Metric | Giá trị |",
        "|---|---|",
        f"| Provider | {s['provider']} |",
        f"| Model | {s['model']} |",
        f"| Prompt version | {s['prompt_version']} |",
        f"| Tổng mẫu đánh giá | {s['total_samples']} |",
    ]

    if s["skipped_samples"] > 0:
        lines.append(
            f"| Mẫu bỏ qua | {s['skipped_samples']} (input rỗng, bị ChatRequest min_length=1 chặn) |"
        )

    lines += [
        f"| JSON validity | {s['json_validity_pct']}% |",
        f"| Action accuracy | {s['action_accuracy_pct']}% |",
        f"| Params accuracy | {s['params_accuracy_pct']}% |",
        f"| Tool selection accuracy | {s['tool_selection_accuracy_pct']}% |",
    ]

    if s.get("intent_accuracy_pct") is not None:
        lines.append(f"| Intent accuracy | {s['intent_accuracy_pct']}% |")
    else:
        lines.append("| Intent accuracy | N/A (golden set chưa có trường `intent`) |")

    lines += [
        f"| Avg latency | {s['avg_latency_ms']}ms |",
        f"| P95 latency | {s['p95_latency_ms']}ms |",
        "",
        "## Chi tiết",
        "",
        "| ID | Input | Expected | Actual | JSON | Action | Params | Tool | Latency |",
        "|---|---|---|---|---|---|---|---|---|",
    ]
    for d in data["details"]:
        if d.get("skipped"):
            lines.append(f"| {d['id']} | _(skipped: {d.get('reason', '')})_ | - | - | - | - | - | - | - |")
            continue
        rejection = f" ⚠️{d['action_rejection']}" if d.get("action_rejection") else ""
        lines.append(
            f"| {d['id']} | {d.get('input', '')[:30]} | "
            f"{d.get('expected_action', '-')} | {d.get('actual_action', '-')}{rejection} | "
            f"{'✅' if d.get('json_valid') else '❌'} | "
            f"{'✅' if d.get('action_match') else '❌'} | "
            f"{'✅' if d.get('params_match') else '❌'} | "
            f"{'✅' if d.get('tool_selection_match') else '❌'} | "
            f"{d.get('latency_ms', '-')} |"
        )
    return "\n".join(lines) + "\n"


def main() -> None:
    parser = argparse.ArgumentParser(description="Robot Eval Harness v0")
    parser.add_argument(
        "--provider",
        default=os.getenv("LLM_PROVIDER", "mock"),
        help="Provider: mock, ollama, gemini (mặc định: mock hoặc .env)",
    )
    parser.add_argument(
        "--golden",
        default=str(REPO_ROOT / "ai" / "datasets" / "golden_v0.jsonl"),
        help="Đường dẫn golden test set (mặc định: ai/datasets/golden_v0.jsonl)",
    )
    parser.add_argument(
        "--save",
        action="store_true",
        help="Lưu kết quả vào docs/benchmarks/phase2-eval-v0.md",
    )
    parser.add_argument(
        "--json",
        action="store_true",
        help="Xuất kết quả JSON thay vì markdown",
    )
    args = parser.parse_args()

    golden_path = Path(args.golden)
    if not golden_path.exists():
        print(f"Không tìm thấy golden set: {golden_path}", file=sys.stderr)
        sys.exit(1)

    data = asyncio.run(run_eval(args.provider, golden_path))

    if args.json:
        print(json.dumps(data, ensure_ascii=False, indent=2))
    else:
        report = format_report(data)
        print(report)

        if args.save:
            out_dir = REPO_ROOT / "docs" / "benchmarks"
            out_dir.mkdir(parents=True, exist_ok=True)
            out_file = out_dir / "phase2-eval-v0.md"
            out_file.write_text(report, encoding="utf-8")
            print(f"\nĐã lưu: {out_file}")

    # Tóm tắt nhanh trên console
    s = data["summary"]
    print(f"\n{'='*60}")
    if s["provider"] == "mock":
        print("⚠️  MockProvider — kết quả KHÔNG phải AI benchmark")
    print(f"Provider: {s['provider']} | Model: {s['model']}")
    print(
        f"JSON: {s['json_validity_pct']}% | Action: {s['action_accuracy_pct']}% | "
        f"Params: {s['params_accuracy_pct']}% | Tool: {s['tool_selection_accuracy_pct']}%"
    )
    if s.get("intent_accuracy_pct") is not None:
        print(f"Intent: {s['intent_accuracy_pct']}%")
    print(f"Latency: avg={s['avg_latency_ms']}ms p95={s['p95_latency_ms']}ms")
    if s["skipped_samples"] > 0:
        print(f"Skipped: {s['skipped_samples']} mẫu (input rỗng)")
    print(f"{'='*60}")


if __name__ == "__main__":
    main()
