"""Bộ test an toàn Phase 2 — mục 58 spec.

Kiểm tra:
- Action ngoài whitelist bị từ chối
- Thiếu/sai kiểu tham số bị từ chối
- Action lạ / tool bịa bị từ chối
- Action STUB (move, stop) bị từ chối
- JSON sai → fallback
- Ngoài phạm vi → action null
- Timeout / permission metadata đúng
- Registry + schema nhất quán
"""

import asyncio

import pytest

from app.action_registry import (
    ALLOWED_ACTIONS,
    ALLOWED_PACKAGES,
    EXECUTABLE_ACTIONS,
    REGISTRY,
    HardwareStatus,
    Permission,
)
from app.actions import parse_model_output, validate_action

# ── 1. Action ngoài whitelist ────────────────────────────────────────


class TestWhitelist:
    """Action ngoài REGISTRY luôn bị từ chối."""

    @pytest.mark.parametrize("action_type", [
        "hack_system",
        "delete_files",
        "send_sms",
        "call_phone",
        "run_shell",
        "self_destruct",
        "install_malware",
        "transfer_money",
        "",
        "GET_TIME",  # case-sensitive
    ])
    def test_unknown_action_rejected(self, action_type):
        action, reason = validate_action({"type": action_type, "params": {}})
        assert action is None, f"Action '{action_type}' ngoài whitelist phải bị từ chối"
        assert "unknown_type" in reason

    def test_whitelist_is_exhaustive(self):
        """Registry chỉ chứa action đã đăng ký."""
        expected = {"get_time", "get_battery", "set_alarm", "open_app", "set_volume", "move", "stop"}
        assert ALLOWED_ACTIONS == expected


# ── 2. Thiếu / sai kiểu tham số ─────────────────────────────────────


class TestInvalidParams:
    """Tham số sai hoặc thiếu bị từ chối."""

    @pytest.mark.parametrize("raw", [
        {"type": "set_alarm", "params": {}},  # thiếu time
        {"type": "set_alarm", "params": {"time": "25:99"}},  # giờ sai
        {"type": "set_alarm", "params": {"time": "abc"}},  # không phải giờ
        {"type": "set_alarm", "params": {"time": "7:30"}},  # thiếu zero-pad
        {"type": "set_alarm", "params": {"time": ""}},  # rỗng
    ])
    def test_set_alarm_bad_params(self, raw):
        action, reason = validate_action(raw)
        assert action is None
        assert reason != "ok"

    @pytest.mark.parametrize("raw", [
        {"type": "open_app", "params": {}},  # thiếu package
        {"type": "open_app", "params": {"package": "com.bank.evil"}},
        {"type": "open_app", "params": {"package": "com.facebook.katana"}},
        {"type": "open_app", "params": {"package": ""}},
        {"type": "open_app", "params": {"package": 123}},  # sai kiểu
    ])
    def test_open_app_bad_params(self, raw):
        action, reason = validate_action(raw)
        assert action is None
        assert reason != "ok"

    @pytest.mark.parametrize("raw", [
        {"type": "set_volume", "params": {}},  # thiếu level
        {"type": "set_volume", "params": {"level": -1}},
        {"type": "set_volume", "params": {"level": 101}},
        {"type": "set_volume", "params": {"level": 500}},
        {"type": "set_volume", "params": {"level": "loud"}},
        {"type": "set_volume", "params": {"level": None}},
    ])
    def test_set_volume_bad_params(self, raw):
        action, reason = validate_action(raw)
        assert action is None
        assert reason != "ok"

    def test_params_not_dict(self):
        action, _ = validate_action({"type": "get_time", "params": "string"})
        assert action is None
        action, _ = validate_action({"type": "get_time", "params": [1, 2]})
        assert action is None

    def test_raw_not_dict(self):
        action, _ = validate_action("not a dict")
        assert action is None
        action, _ = validate_action(42)
        assert action is None
        action, _ = validate_action(None)
        assert action is None
        action, _ = validate_action([])
        assert action is None


# ── 3. Action hợp lệ được chấp nhận ─────────────────────────────────


class TestValidActions:
    """Action hợp lệ được chấp nhận với params đã làm sạch."""

    def test_get_time(self):
        action, reason = validate_action({"type": "get_time", "params": {}})
        assert action is not None and reason == "ok"
        assert action.type == "get_time"
        assert action.params == {}

    def test_get_battery(self):
        action, reason = validate_action({"type": "get_battery", "params": {}})
        assert action is not None and reason == "ok"
        assert action.type == "get_battery"

    def test_set_alarm_valid(self):
        action, reason = validate_action({"type": "set_alarm", "params": {"time": "07:30"}})
        assert action is not None and reason == "ok"
        assert action.params == {"time": "07:30"}

    def test_set_alarm_with_label(self):
        action, reason = validate_action({"type": "set_alarm", "params": {"time": "06:00", "label": "Dậy đi học"}})
        assert action is not None and reason == "ok"
        assert action.params["time"] == "06:00"
        assert action.params["label"] == "Dậy đi học"

    @pytest.mark.parametrize("package", list(ALLOWED_PACKAGES))
    def test_open_app_whitelisted(self, package):
        action, reason = validate_action({"type": "open_app", "params": {"package": package}})
        assert action is not None and reason == "ok"
        assert action.params["package"] == package

    @pytest.mark.parametrize("level", [0, 50, 100])
    def test_set_volume_valid(self, level):
        action, reason = validate_action({"type": "set_volume", "params": {"level": level}})
        assert action is not None and reason == "ok"
        assert action.params["level"] == level


# ── 4. Action STUB bị từ chối ────────────────────────────────────────


class TestStubActions:
    """Action có hardware=STUB phải bị validate_action từ chối."""

    def test_move_rejected(self):
        action, reason = validate_action({
            "type": "move",
            "params": {"direction": "forward", "speed": 50, "duration_ms": 1000},
        })
        assert action is None, "move là STUB, phải bị từ chối"
        assert "hardware_stub" in reason

    def test_stop_rejected(self):
        action, reason = validate_action({"type": "stop", "params": {}})
        assert action is None, "stop là STUB, phải bị từ chối"
        assert "hardware_stub" in reason

    def test_stub_not_in_executable(self):
        for name, spec in REGISTRY.items():
            if spec.hardware == HardwareStatus.STUB:
                assert name not in EXECUTABLE_ACTIONS
            else:
                assert name in EXECUTABLE_ACTIONS


# ── 5. JSON sai → fallback ───────────────────────────────────────────


class TestJsonFallback:
    """Output không có JSON → ValueError."""

    def test_no_json(self):
        with pytest.raises(ValueError, match="Không tìm thấy JSON"):
            parse_model_output("Đây không phải JSON")

    def test_invalid_json(self):
        with pytest.raises(ValueError, match="JSON không hợp lệ"):
            parse_model_output("{invalid json}")

    def test_array_not_object(self):
        """JSON array → rfind(}) miss → Không tìm thấy JSON hoặc parse lỗi."""
        with pytest.raises(ValueError):
            parse_model_output("[1, 2, 3]")

    def test_think_tags_stripped(self):
        result = parse_model_output(
            '<think>ignore this</think>{"response":"ok","action":null}'
        )
        assert result.response == "ok"
        assert result.action is None

    def test_json_with_noise(self):
        result = parse_model_output(
            'blah blah {"response":"hello","action":{"type":"get_time","params":{}}} more blah'
        )
        assert result.action is not None
        assert result.action.type == "get_time"

    def test_action_outside_registry_becomes_null(self):
        """Model bịa action → action bị null sau validate."""
        result = parse_model_output(
            '{"response":"ok","action":{"type":"launch_missile","params":{}}}'
        )
        assert result.action is None
        assert result.response == "ok"
        assert result.action_rejection is not None
        assert "unknown_type" in result.action_rejection


# ── 6. Fallback khi JSON liên tục sai ───────────────────────────────


class TestFallbackFlow:
    """Service trả fallback khi JSON sai sau retry."""

    def test_fallback_response(self, monkeypatch):
        from app import service

        class AlwaysBadProvider:
            async def generate(self, messages: list[dict]) -> str:
                return "Không phải JSON đâu nhé"

        monkeypatch.setattr(service, "provider", AlwaysBadProvider())
        result = asyncio.run(service.handle_chat("safety_test", "bất kỳ"))
        assert result.response == service.FALLBACK_RESPONSE
        assert result.action is None


# ── 7. Registry metadata đúng ────────────────────────────────────────


class TestRegistryMetadata:
    """Kiểm tra metadata registry nhất quán."""

    def test_all_actions_have_timeout(self):
        for name, spec in REGISTRY.items():
            assert spec.timeout_ms > 0, f"{name} thiếu timeout"

    def test_all_actions_have_permission(self):
        for name, spec in REGISTRY.items():
            assert len(spec.permissions) > 0, f"{name} thiếu permission"

    def test_motor_actions_need_motor_permission(self):
        for name in ("move", "stop"):
            spec = REGISTRY[name]
            assert Permission.MOTOR in spec.permissions

    def test_no_params_actions(self):
        for name in ("get_time", "get_battery", "stop"):
            spec = REGISTRY[name]
            assert spec.required_params == ()

    def test_registry_names_match_keys(self):
        for name, spec in REGISTRY.items():
            assert spec.name == name

    def test_action_descriptions_not_empty(self):
        for name, spec in REGISTRY.items():
            assert spec.description, f"{name} thiếu description"

    def test_executable_is_subset_of_allowed(self):
        assert EXECUTABLE_ACTIONS <= ALLOWED_ACTIONS
