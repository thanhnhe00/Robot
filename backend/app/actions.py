"""Kiểm tra JSON do model trả về. Model chỉ ĐỀ XUẤT, backend mới quyết định hợp lệ.

Phase 2: dùng action_registry làm nguồn sự thật. Action ngoài registry bị từ chối.
Action có hardware=STUB bị từ chối khi validate (chưa nối phần cứng).
"""

import json
import logging
import re
from typing import Any

from .action_registry import REGISTRY, HardwareStatus
from .schemas import Action, ChatResponse

log = logging.getLogger("robot")

THINK_RE = re.compile(r"<think>.*?</think>", re.DOTALL)


def validate_action(raw: Any) -> tuple[Action | None, str]:
    """Validate action do model đề xuất.

    Returns:
        (Action, "ok") nếu hợp lệ.
        (None, lý_do) nếu bất kỳ bước nào fail.
    Lý do luôn là chuỗi ngắn gọn để ghi log, không chứa dữ liệu nhạy cảm.
    """
    if not isinstance(raw, dict):
        return None, "not_dict"
    action_type = str(raw.get("type", "")).strip()

    # Action ngoài registry → từ chối
    spec = REGISTRY.get(action_type)
    if spec is None:
        return None, f"unknown_type:{action_type or '(empty)'}"

    # Action chưa nối phần cứng → từ chối
    if spec.hardware != HardwareStatus.READY:
        return None, f"hardware_stub:{action_type}"

    raw_params = raw.get("params")
    if raw_params is not None and not isinstance(raw_params, dict):
        return None, f"params_not_dict:{action_type}"
    params = raw_params or {}

    clean = spec.validate_params(params)
    if clean is None:
        return None, f"invalid_params:{action_type}"

    return Action(type=action_type, params=clean), "ok"


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

    raw_action = data.get("action")
    if raw_action is None:
        action = None
        action_rejection = None
    else:
        action, reason = validate_action(raw_action)
        action_rejection = reason if action is None else None
        if action is None:
            log.warning(
                "action.rejected",
                extra={
                    "event": "action.rejected",
                    "proposed_action": raw_action,
                    "reason": reason,
                },
            )

    return ChatResponse(
        response=str(data.get("response", "")).strip(),
        action=action,
        action_rejection=action_rejection,
    )
