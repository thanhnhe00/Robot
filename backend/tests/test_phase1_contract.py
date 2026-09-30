import asyncio
import json
from types import SimpleNamespace

import httpx

from app import service
from app.llm import gemini, ollama
from app.llm.mock import MockProvider


def patch_httpx_transport(monkeypatch, module, handler):
    real_client = httpx.AsyncClient

    def make_client(*args, **kwargs):
        kwargs["transport"] = httpx.MockTransport(handler)
        return real_client(*args, **kwargs)

    monkeypatch.setattr(module.httpx, "AsyncClient", make_client)


def test_mock_provider():
    raw = asyncio.run(
        MockProvider().generate([{"role": "user", "content": "Bây giờ là mấy giờ?"}])
    )
    result = json.loads(raw)
    assert result["action"]["type"] == "get_time"


def test_ollama_provider_uses_http_mock(monkeypatch):
    monkeypatch.setattr(
        ollama,
        "settings",
        SimpleNamespace(
            ollama_model="test-model",
            ollama_url="http://ollama.test",
            request_timeout=1,
        ),
    )

    def handler(request):
        body = json.loads(request.content)
        assert request.url.path == "/api/chat"
        assert body["model"] == "test-model"
        return httpx.Response(
            200,
            json={"message": {"content": '{"response":"ok","action":null}'}},
        )

    patch_httpx_transport(monkeypatch, ollama, handler)
    result = asyncio.run(
        ollama.OllamaProvider().generate([{"role": "user", "content": "Xin chào"}])
    )
    assert json.loads(result)["response"] == "ok"


def test_gemini_provider_uses_http_mock(monkeypatch):
    monkeypatch.setattr(
        gemini,
        "settings",
        SimpleNamespace(
            gemini_api_key="test-key",
            gemini_model="test-model",
            request_timeout=1,
        ),
    )

    def handler(request):
        body = json.loads(request.content)
        assert request.headers["x-goog-api-key"] == "test-key"
        assert "test-model:generateContent" in str(request.url)
        assert body["contents"][0]["role"] == "user"
        return httpx.Response(
            200,
            json={
                "candidates": [
                    {
                        "content": {
                            "parts": [{"text": '{"response":"ok","action":null}'}]
                        }
                    }
                ]
            },
        )

    patch_httpx_transport(monkeypatch, gemini, handler)
    result = asyncio.run(
        gemini.GeminiProvider().generate([{"role": "user", "content": "Xin chào"}])
    )
    assert json.loads(result)["response"] == "ok"


class ExtraProvider:
    """Provider giả chỉ cần tuân theo hàm generate, không cần sửa service."""

    async def generate(self, messages: list[dict]) -> str:
        return '{"response":"Provider mới hoạt động","action":null}'


class MemoryRepository:
    def initialize(self):
        pass

    def get_history(self, session_id: str, limit: int) -> list[dict]:
        return []

    def add_message(self, session_id: str, role: str, content: str):
        pass


def test_service_accepts_an_unregistered_provider(monkeypatch):
    monkeypatch.setattr(service, "provider", ExtraProvider())
    monkeypatch.setattr(service, "repository", MemoryRepository())

    result = asyncio.run(service.handle_chat("test", "Xin chào"))
    assert result.response == "Provider mới hoạt động"
