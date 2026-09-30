"""Kiểm tra JSON do model trả về. Model chỉ ĐỀ XUẤT, backend mới quyết định hợp lệ.

Phase 2: dùng action_registry làm nguồn sự thật. Action ngoài registry bị từ chối.
Action có hardware=STUB bị từ chối khi validate (chưa nối phần cứng).
"""

import json
import re
from typing import Any

from .action_registry import REGISTRY, HardwareStatus
from .schemas import Action, ChatResponse

THINK_RE = re.compile(r"<think>.*?</think>", re.DOTALL)


def validate_action(raw: Any) -> Action | None:
    """Validate action do model đề xuất. Trả None nếu bất kỳ bước nào fail.

    Luồng: action type trong registry? → hardware READY? → params hợp lệ? → OK.
    """
    if not isinstance(raw, dict):
        return None
    action_type = str(raw.get("type", "")).strip()

    # Action ngoài registry → từ chối
    spec = REGISTRY.get(action_type)
    if spec is None:
        return None

    # Action chưa nối phần cứng → từ chối
    if spec.hardware != HardwareStatus.READY:
        return None

    params = raw.get("params") or {}
    if not isinstance(params, dict):
        return None

    clean = spec.validate_params(params)
    if clean is None:
        return None

    return Action(type=action_type, params=clean)


def parse_model_output(text: str) -> ChatResponse:
    """Tách JSON từ output của model. Ném ValueError nếu không đọc được."""
    text = THINK_RE.sub("", text).strip()
    start, end = text.find("{"), text.rfind("}")
    if start == -1 or end <= start:
        raise ValueError("Không tìm thấy JSON trong output của model")
    try:
        data = json.loads(text[start : end + 1])
    except json.JSONDecodeError as e:
        raise ValueError(f"JSON không hợp lệ: {e}") from e
    if not isinstance(data, dict):
        raise ValueError("JSON gốc phải là object")  # noqa: TRY004  # service bắt ValueError để retry
    return ChatResponse(
        response=str(data.get("response", "")).strip(),
        action=validate_action(data.get("action")),
    )
