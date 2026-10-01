"""Test suite chạy qua toàn bộ test vector chung trong ai/schemas/action_vectors.json.

Bộ test này đảm bảo backend Python và app Android Kotlin tuân thủ cùng một
hợp đồng kiểm tra hành vi cho các action đề xuất.
"""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any

import pytest

from app.actions import validate_action

VECTORS_FILE = Path(__file__).resolve().parents[2] / "ai" / "schemas" / "action_vectors.json"


def load_vectors() -> list[dict[str, Any]]:
    assert VECTORS_FILE.exists(), f"Không tìm thấy file test vector tại: {VECTORS_FILE}"
    with open(VECTORS_FILE, encoding="utf-8") as f:
        return json.load(f)


VECTORS = load_vectors()


@pytest.mark.parametrize("vector", VECTORS, ids=[v["id"] for v in VECTORS])
def test_action_vector(vector: dict[str, Any]):
    vid = vector["id"]
    raw_action = vector["action"]
    expected = vector["expected"]

    action, reason = validate_action(raw_action)

    if expected is None:
        assert action is None, (
            f"[{vid}] Kỳ vọng bị từ chối (expected=null) nhưng lại hợp lệ: "
            f"type={action.type}, params={action.params} (reason={reason})"
        )
    else:
        assert action is not None, (
            f"[{vid}] Kỳ vọng hợp lệ nhưng bị từ chối: reason={reason}"
        )
        assert action.type == expected["type"], (
            f"[{vid}] Lệch type: {action.type} != {expected['type']}"
        )
        assert action.params == expected["params"], (
            f"[{vid}] Lệch params: {action.params} != {expected['params']}"
        )
