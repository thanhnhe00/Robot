"""Kiểm tra JSON do model trả về. Model chỉ ĐỀ XUẤT, backend mới quyết định hợp lệ."""

import json
import re
from typing import Any

from .schemas import Action, ChatResponse

# Danh sách action hợp lệ (Phase 2 sẽ mở rộng thành action registry có schema/permission).
ALLOWED_ACTIONS = frozenset({"get_time", "get_battery", "set_alarm", "open_app", "set_volume"})

# Whitelist theo package name Android. Chỉ package trong danh sách này mới được mở.
# Camera/đồng hồ/máy tính có package khác nhau theo hãng máy nên chưa đưa vào;
# phía Android sẽ xử lý chúng bằng intent chuẩn (xem docs/decisions).
ALLOWED_PACKAGES = {
    "com.google.android.youtube",
    "com.android.chrome",
    "com.android.settings",
    "com.spotify.music",
}

TIME_RE = re.compile(r"^([01]\d|2[0-3]):[0-5]\d$")
THINK_RE = re.compile(r"<think>.*?</think>", re.DOTALL)


def _validate_params(action_type: str, params: dict[str, Any]) -> dict[str, Any] | None:
    """Trả về params đã làm sạch, hoặc None nếu không hợp lệ."""
    if action_type in ("get_time", "get_battery"):
        return {}
    if action_type == "set_alarm":
        t = str(params.get("time", ""))
        return {"time": t} if TIME_RE.match(t) else None
    if action_type == "open_app":
        package = str(params.get("package", "")).strip()
        return {"package": package} if package in ALLOWED_PACKAGES else None
    if action_type == "set_volume":
        try:
            level = int(params.get("level"))
        except (TypeError, ValueError):
            return None
        return {"level": level} if 0 <= level <= 100 else None
    return None  # action lạ -> bỏ


def validate_action(raw: Any) -> Action | None:
    if not isinstance(raw, dict):
        return None
    action_type = str(raw.get("type", "")).strip()
    params = raw.get("params") or {}
    if not isinstance(params, dict):
        return None
    clean = _validate_params(action_type, params)
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
