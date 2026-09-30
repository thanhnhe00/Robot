"""Test cho module actions.py — cập nhật Phase 2 dùng action_registry."""

import pytest

from app.actions import parse_model_output, validate_action


def test_parse_ok():
    r = parse_model_output('{"response":"","action":{"type":"get_time","params":{}}}')
    assert r.action.type == "get_time"


def test_parse_strips_think_and_noise():
    r = parse_model_output('<think>hmm</think> Đây: {"response":"chào","action":null} xong')
    assert r.response == "chào" and r.action is None


def test_invalid_json_raises():
    with pytest.raises(ValueError):
        parse_model_output("không có json")


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
    assert validate_action(raw) is None


def test_good_actions_kept():
    assert validate_action({"type": "set_alarm", "params": {"time": "07:30"}}).params == {"time": "07:30"}
    yt = "com.google.android.youtube"
    assert validate_action({"type": "open_app", "params": {"package": yt}}).params == {"package": yt}
    assert validate_action({"type": "set_volume", "params": {"level": 50}}).params == {"level": 50}
    assert validate_action({"type": "get_time", "params": {}}).type == "get_time"
    assert validate_action({"type": "get_battery", "params": {}}).type == "get_battery"
