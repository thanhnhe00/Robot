import asyncio
import logging

import httpx
import pytest
from fastapi.testclient import TestClient

from app import service
from app.config import settings
from app.db import DisabledConversationRepository
from app.llm.base import ProviderNotConfiguredError
from app.main import app
from app.service import FALLBACK_RESPONSE


def test_flow():
    with TestClient(app) as c:
        assert c.get("/health").json()["provider"] == "mock"

        r = c.post("/chat", json={"session_id": "t", "text": "Bây giờ là mấy giờ?"})
        assert r.status_code == 200 and r.json()["action"]["type"] == "get_time"

        r = c.post("/chat", json={"session_id": "t", "text": "Đặt báo thức 7 giờ 30"})
        assert r.json()["action"] == {
            "type": "set_alarm",
            "params": {"time": "07:30"},
        }

        r = c.post("/chat", json={"session_id": "t", "text": "mở youtube"})
        assert r.json()["action"] == {
            "type": "open_app",
            "params": {"package": "com.google.android.youtube"},
        }

        r = c.post("/chat", json={"session_id": "t", "text": "mở banking"})
        assert r.json()["action"] is None  # package không nằm trong whitelist

        r = c.post("/chat", json={"session_id": "t", "text": "Xin chào"})
        assert r.json()["action"] is None and r.json()["response"]


def test_ws():
    with TestClient(app) as c, c.websocket_connect("/ws") as ws:
        ws.send_json({"session_id": "w", "text": "pin còn bao nhiêu"})
        assert ws.receive_json()["action"]["type"] == "get_battery"


def test_auth_with_api_key(monkeypatch):
    import dataclasses

    monkeypatch.setattr(
        "app.main.settings",
        dataclasses.replace(settings, api_key="secret123"),
    )
    with TestClient(app) as c:
        # Thiếu header
        r = c.post("/chat", json={"session_id": "t", "text": "alo"})
        assert r.status_code == 401
        assert r.json()["detail"] == "Sai API key"

        # Sai header
        r = c.post(
            "/chat",
            json={"session_id": "t", "text": "alo"},
            headers={"x-api-key": "wrong"},
        )
        assert r.status_code == 401

        # Đúng header
        r = c.post(
            "/chat",
            json={"session_id": "t", "text": "Bây giờ là mấy giờ?"},
            headers={"x-api-key": "secret123"},
        )
        assert r.status_code == 200


def test_ws_auth(monkeypatch):
    import dataclasses

    monkeypatch.setattr(
        "app.main.settings",
        dataclasses.replace(settings, api_key="secret123"),
    )
    with TestClient(app) as c:
        # Sai auth → bị close
        with pytest.raises(Exception):
            with c.websocket_connect("/ws") as ws:
                ws.send_json({"auth": "wrong", "session_id": "w", "text": "alo"})
                ws.receive_json()

        # Đúng auth gửi kèm request → nhận response
        with c.websocket_connect("/ws") as ws:
            ws.send_json({"auth": "secret123", "session_id": "w", "text": "pin còn bao nhiêu"})
            assert ws.receive_json()["action"]["type"] == "get_battery"

        # Auth riêng rồi gửi request sau
        with c.websocket_connect("/ws") as ws:
            ws.send_json({"auth": "secret123"})
            ws.send_json({"session_id": "w", "text": "pin còn bao nhiêu"})
            assert ws.receive_json()["action"]["type"] == "get_battery"


def test_ws_error_handling(monkeypatch):
    async def mock_fail(*args, **kwargs):
        raise ValueError("Lỗi xử lý")

    monkeypatch.setattr("app.main.handle_chat", mock_fail)
    with TestClient(app) as c, c.websocket_connect("/ws") as ws:
        ws.send_json({"session_id": "w", "text": "lỗi đi"})
        res = ws.receive_json()
        assert res == {"error": "Không thể xử lý yêu cầu."}


def test_provider_error_502(monkeypatch):
    async def mock_http_err(*args, **kwargs):
        raise httpx.ConnectError("Connection refused")

    monkeypatch.setattr("app.main.handle_chat", mock_http_err)
    with TestClient(app) as c:
        r = c.post("/chat", json={"session_id": "t", "text": "alo"})
        assert r.status_code == 502
        assert r.json()["detail"] == "Không thể kết nối tới AI provider."


def test_provider_error_503(monkeypatch):
    async def mock_not_config(*args, **kwargs):
        raise ProviderNotConfiguredError("Chưa cấu hình")

    monkeypatch.setattr("app.main.handle_chat", mock_not_config)
    with TestClient(app) as c:
        r = c.post("/chat", json={"session_id": "t", "text": "alo"})
        assert r.status_code == 503
        assert r.json()["detail"] == "AI provider chưa được cấu hình."


def test_disabled_persistence():
    repo = DisabledConversationRepository()
    repo.initialize()
    repo.add_message("session_disabled", "user", "Xin chào")
    assert repo.get_history("session_disabled", 10) == []


def test_fallback_when_json_invalid(monkeypatch):
    class InvalidJsonProvider:
        async def generate(self, messages: list[dict]) -> str:
            return "Đây không phải JSON"

    monkeypatch.setattr(service, "provider", InvalidJsonProvider())
    res = asyncio.run(service.handle_chat("session_fallback", "yêu cầu lạ"))
    assert res.response == FALLBACK_RESPONSE
    assert res.action is None


def test_structured_logging_fields(monkeypatch, caplog):
    caplog.set_level(logging.INFO, logger="robot")

    class DummyProvider:
        async def generate(self, messages: list[dict]) -> str:
            return '{"response": "Xin chào!", "action": null}'

    monkeypatch.setattr(service, "provider", DummyProvider())
    asyncio.run(service.handle_chat("session_log", "Chào bạn"))

    found = False
    for record in caplog.records:
        if getattr(record, "event", "") == "chat.completed":
            assert hasattr(record, "latency_ms")
            assert record.provider == settings.provider
            assert record.prompt_version == settings.prompt_version
            assert record.validation == "valid"
            found = True
    assert found
