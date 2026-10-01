"""Action Registry: metadata, permission, timeout, executor cho từng action.

Mỗi action đăng ký: tên, tham số cần validate, quyền Android, timeout,
và có nối phần cứng hay chưa. Registry là nguồn sự thật duy nhất để
validate_action() quyết định cho phép hay từ chối.

An toàn: action ngoài registry luôn bị từ chối (whitelist, không blacklist).
"""

from __future__ import annotations

import math
import re
from dataclasses import dataclass
from enum import StrEnum
from typing import Any, ClassVar


class Permission(StrEnum):
    """Android permission tương ứng. Giá trị là tên thật sẽ dùng ở Phase 3."""

    NONE = "none"
    SET_ALARM = "com.android.alarm.permission.SET_ALARM"
    LAUNCH_APP = "android.intent.action.MAIN"
    AUDIO = "android.permission.MODIFY_AUDIO_SETTINGS"
    MOTOR = "robot.permission.MOTOR_CONTROL"


class HardwareStatus(StrEnum):
    """Trạng thái nối phần cứng."""

    READY = "ready"  # sẵn sàng thực thi
    STUB = "stub"  # đăng ký schema nhưng chưa nối (ESP32 chưa có)


def _strict_int(value: Any) -> int | None:
    """Ép sang int chặt: chỉ nhận int thật, không nhận bool/float/str/Inf/NaN.

    - bool bị từ chối vì ``isinstance(True, int)`` là True trong Python.
    - float bị từ chối vì ``int(50.9)`` cắt ngầm thành 50.
    - str bị từ chối vì ``int("50")`` ép ngầm.
    - Infinity/NaN gây OverflowError khi gọi ``int()``.
    """
    if isinstance(value, bool):
        return None
    if not isinstance(value, int):
        return None
    # int thật trong Python không có Inf/NaN, nhưng phòng trường hợp
    # ai đó serialize float rồi truyền vào
    try:
        if math.isnan(value) or math.isinf(value):
            return None
    except (TypeError, OverflowError):
        return None
    return value


@dataclass(frozen=True)
class ActionSpec:
    """Đặc tả một action trong registry."""

    name: str
    description: str
    permissions: tuple[Permission, ...] = (Permission.NONE,)
    timeout_ms: int = 5000
    hardware: HardwareStatus = HardwareStatus.READY
    required_params: tuple[str, ...] = ()
    optional_params: tuple[str, ...] = ()

    # Validator tùy chỉnh cho params — nhận raw params dict,
    # trả về params đã làm sạch hoặc None nếu invalid.
    # Nếu None (không cung cấp validator), chỉ kiểm tra required_params tồn tại.
    _validators: ClassVar[dict[str, Any]] = {}

    def validate_params(self, params: dict[str, Any]) -> dict[str, Any] | None:
        """Trả về params đã validate, hoặc None nếu sai."""
        validator = ActionSpec._validators.get(self.name)
        if validator:
            return validator(params)
        # Mặc định: chỉ kiểm tra required params tồn tại
        for key in self.required_params:
            if key not in params:
                return None
        return {k: params[k] for k in (*self.required_params, *self.optional_params) if k in params}


# ── Validators cho từng action ──────────────────────────────────────

TIME_RE = re.compile(r"^([01]\d|2[0-3]):[0-5]\d$")


def _validate_no_params(params: dict[str, Any]) -> dict[str, Any] | None:
    """Action không cần params (get_time, get_battery, stop)."""
    return {}


def _validate_set_alarm(params: dict[str, Any]) -> dict[str, Any] | None:
    t = str(params.get("time", ""))
    if not TIME_RE.match(t):
        return None
    result: dict[str, Any] = {"time": t}
    label = params.get("label")
    if label is not None:
        if not isinstance(label, str):
            return None
        label = label.strip()[:100]
        if label:
            result["label"] = label
    return result


ALLOWED_PACKAGES = frozenset({
    "com.google.android.youtube",
    "com.android.chrome",
    "com.android.settings",
    "com.spotify.music",
})


def _validate_open_app(params: dict[str, Any]) -> dict[str, Any] | None:
    package = str(params.get("package", "")).strip()
    return {"package": package} if package in ALLOWED_PACKAGES else None


def _validate_set_volume(params: dict[str, Any]) -> dict[str, Any] | None:
    level = _strict_int(params.get("level"))
    if level is None:
        return None
    return {"level": level} if 0 <= level <= 100 else None


DIRECTION_VALUES = frozenset({"forward", "backward", "left", "right"})


def _validate_move(params: dict[str, Any]) -> dict[str, Any] | None:
    direction = str(params.get("direction", "")).strip().lower()
    if direction not in DIRECTION_VALUES:
        return None
    result: dict[str, Any] = {"direction": direction}
    speed = _strict_int(params.get("speed", 50))
    if speed is None:
        return None
    if not 0 <= speed <= 100:
        return None
    result["speed"] = speed
    duration_ms = _strict_int(params.get("duration_ms", 1000))
    if duration_ms is None:
        return None
    if not 0 <= duration_ms <= 5000:
        return None
    result["duration_ms"] = duration_ms
    return result


# ── Registry gốc ────────────────────────────────────────────────────

# Gắn validator vào class variable
ActionSpec._validators = {
    "get_time": _validate_no_params,
    "get_battery": _validate_no_params,
    "set_alarm": _validate_set_alarm,
    "open_app": _validate_open_app,
    "set_volume": _validate_set_volume,
    "move": _validate_move,
    "stop": _validate_no_params,
}


# Registry: dict tên → ActionSpec. Import bằng `from app.action_registry import REGISTRY`.
REGISTRY: dict[str, ActionSpec] = {}


def _register(spec: ActionSpec) -> None:
    REGISTRY[spec.name] = spec


_register(ActionSpec(
    name="get_time",
    description="Xem giờ hiện tại. Ứng dụng tự đọc đồng hồ hệ thống, không hỏi LLM.",
    permissions=(Permission.NONE,),
    timeout_ms=1000,
))

_register(ActionSpec(
    name="get_battery",
    description="Xem phần trăm pin điện thoại.",
    permissions=(Permission.NONE,),
    timeout_ms=1000,
))

_register(ActionSpec(
    name="set_alarm",
    description="Đặt báo thức. Cần param time (HH:MM, 24h). Tùy chọn: label.",
    permissions=(Permission.SET_ALARM,),
    timeout_ms=3000,
    required_params=("time",),
    optional_params=("label",),
))

_register(ActionSpec(
    name="open_app",
    description="Mở ứng dụng Android. Package phải nằm trong whitelist.",
    permissions=(Permission.LAUNCH_APP,),
    timeout_ms=5000,
    required_params=("package",),
))

_register(ActionSpec(
    name="set_volume",
    description="Chỉnh âm lượng thiết bị. Level từ 0 (tắt tiếng) đến 100 (to nhất).",
    permissions=(Permission.AUDIO,),
    timeout_ms=2000,
    required_params=("level",),
))

_register(ActionSpec(
    name="move",
    description="Di chuyển robot. Chưa nối ESP32 — chỉ đăng ký schema, chưa thực thi.",
    permissions=(Permission.MOTOR,),
    timeout_ms=5000,
    hardware=HardwareStatus.STUB,
    required_params=("direction",),
    optional_params=("speed", "duration_ms"),
))

_register(ActionSpec(
    name="stop",
    description="Dừng robot khẩn cấp. Chưa nối ESP32.",
    permissions=(Permission.MOTOR,),
    timeout_ms=1000,
    hardware=HardwareStatus.STUB,
))

# Xuất danh sách tên action hợp lệ (freeze để tránh sửa runtime)
ALLOWED_ACTIONS: frozenset[str] = frozenset(REGISTRY.keys())

# Chỉ action READY mới được thực thi; STUB bị từ chối khi execute (nhưng schema vẫn đăng ký)
EXECUTABLE_ACTIONS: frozenset[str] = frozenset(
    name for name, spec in REGISTRY.items() if spec.hardware == HardwareStatus.READY
)
