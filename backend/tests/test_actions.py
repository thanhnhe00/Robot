"""Test cho module actions.py — cập nhật Phase 2 dùng action_registry."""

import pytest

from app.actions import parse_model_output, validate_action


def test_parse_ok():
    r = parse_model_output('{"response":"","action":{"type":"get_time","params":{}}}')
    assert r.action.type == "get_time"
    assert r.action_rejection is None


def test_parse_strips_think_and_noise():
    r = parse_model_output('<think>hmm</think> Đây: {"response":"chào","action":null} xong')
    assert r.response == "chào" and r.action is None
    assert r.action_rejection is None


def test_invalid_json_raises():
    with pytest.raises(ValueError):
        parse_model_output("không có json")


# ── validate_action trả (Action|None, reason) ───────────────────────


@pytest.mark.parametrize(
    "raw",
    [
        {"type": "hack_the_planet", "params": {}},
        {"type": "set_alarm", "params": {"time": "25:99"}},
        {"type": "open_app", "params": {"package": "com.bank.evil"}},
        {"type": "open_app", "params": {}},
        {"type": "set_volume", "params": {"level": 500}},
        "không phải dict",
        # Phase 2: STUB actions bị từ chối
        {"type": "move", "params": {"direction": "forward"}},
        {"type": "stop", "params": {}},
    ],
)
def test_bad_actions_dropped(raw):
    action, reason = validate_action(raw)
    assert action is None
    assert reason != "ok"


def test_good_actions_kept():
    action, reason = validate_action({"type": "set_alarm", "params": {"time": "07:30"}})
    assert action is not None and reason == "ok"
    assert action.params == {"time": "07:30"}

    yt = "com.google.android.youtube"
    action, reason = validate_action({"type": "open_app", "params": {"package": yt}})
    assert action is not None and reason == "ok"
    assert action.params == {"package": yt}

    action, reason = validate_action({"type": "set_volume", "params": {"level": 50}})
    assert action is not None and reason == "ok"
    assert action.params == {"level": 50}

    action, reason = validate_action({"type": "get_time", "params": {}})
    assert action is not None and reason == "ok"
    assert action.type == "get_time"

    action, reason = validate_action({"type": "get_battery", "params": {}})
    assert action is not None and reason == "ok"
    assert action.type == "get_battery"


# ── Bug mục 1: _strict_int reject bool / float / str / Inf / NaN ────


class TestStrictIntValidation:
    """int() trên Infinity ném OverflowError → phải trả None thay vì 500."""

    def test_infinity_rejected(self):
        action, reason = validate_action({"type": "set_volume", "params": {"level": float("inf")}})
        assert action is None
        assert "invalid_params" in reason

    def test_negative_infinity_rejected(self):
        action, reason = validate_action({"type": "set_volume", "params": {"level": float("-inf")}})
        assert action is None

    def test_nan_rejected(self):
        action, reason = validate_action({"type": "set_volume", "params": {"level": float("nan")}})
        assert action is None

    def test_bool_true_rejected(self):
        """True là int trong Python (int(True)==1), nhưng không nên là volume level."""
        action, reason = validate_action({"type": "set_volume", "params": {"level": True}})
        assert action is None

    def test_bool_false_rejected(self):
        action, reason = validate_action({"type": "set_volume", "params": {"level": False}})
        assert action is None

    def test_float_truncation_rejected(self):
        """50.9 không nên im lặng thành 50."""
        action, reason = validate_action({"type": "set_volume", "params": {"level": 50.9}})
        assert action is None

    def test_string_number_rejected(self):
        """'50' không nên im lặng thành 50."""
        action, reason = validate_action({"type": "set_volume", "params": {"level": "50"}})
        assert action is None

    def test_valid_int_accepted(self):
        action, reason = validate_action({"type": "set_volume", "params": {"level": 50}})
        assert action is not None
        assert action.params["level"] == 50

    # Tương tự cho move (speed / duration_ms)
    def test_move_speed_bool_rejected(self):
        action, _ = validate_action({
            "type": "move",
            "params": {"direction": "forward", "speed": True},
        })
        # move là STUB nên bị reject vì hardware trước, nhưng nếu READY thì speed bool cũng bị reject
        assert action is None

    def test_move_speed_float_rejected(self):
        """Kiểm tra _strict_int qua _validate_move — speed float."""
        from app.action_registry import _validate_move

        assert _validate_move({"direction": "forward", "speed": 50.5}) is None

    def test_move_duration_string_rejected(self):
        from app.action_registry import _validate_move

        assert _validate_move({"direction": "forward", "duration_ms": "1000"}) is None


# ── Bug mục 2: action bị từ chối phải có dấu vết ────────────────────


class TestActionRejectionTraceability:
    """Khi model đề xuất action nhưng validator từ chối, response phải ghi lại."""

    def test_rejected_action_has_reason_in_response(self):
        """parse_model_output phải set action_rejection khi action bị reject."""
        r = parse_model_output(
            '{"response":"Mình đã đặt báo thức","action":{"type":"set_alarm","params":{"time":"99:99"}}}'
        )
        assert r.action is None
        assert r.action_rejection is not None
        assert "invalid_params" in r.action_rejection

    def test_unknown_action_has_rejection_reason(self):
        r = parse_model_output(
            '{"response":"ok","action":{"type":"launch_missile","params":{}}}'
        )
        assert r.action is None
        assert r.action_rejection is not None
        assert "unknown_type" in r.action_rejection

    def test_valid_action_no_rejection(self):
        r = parse_model_output(
            '{"response":"","action":{"type":"get_time","params":{}}}'
        )
        assert r.action is not None
        assert r.action_rejection is None

    def test_null_action_no_rejection(self):
        """Model không đề xuất action → action_rejection cũng là None."""
        r = parse_model_output('{"response":"Chào bạn!","action":null}')
        assert r.action is None
        assert r.action_rejection is None

    def test_rejection_reason_descriptive(self):
        """Lý do reject phải chứa action type để debug."""
        _, reason = validate_action({"type": "open_app", "params": {"package": "com.evil"}})
        assert "open_app" in reason

    def test_stub_rejection_reason(self):
        _, reason = validate_action({"type": "move", "params": {"direction": "forward"}})
        assert "hardware_stub" in reason
        assert "move" in reason
