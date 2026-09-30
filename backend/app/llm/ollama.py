import httpx

from ..config import settings


class OllamaProvider:
    async def generate(self, messages: list[dict]) -> str:
        payload = {
            "model": settings.ollama_model,
            "messages": messages,
            "stream": False,
            "format": "json",   # ép output là JSON
            "think": False,     # tắt chế độ suy nghĩ của Qwen3 cho nhanh
            "options": {"temperature": 0.3},
        }
        async with httpx.AsyncClient(timeout=settings.request_timeout) as client:
            r = await client.post(f"{settings.ollama_url}/api/chat", json=payload)
            r.raise_for_status()
            return r.json()["message"]["content"]
