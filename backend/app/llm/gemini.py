import httpx

from ..config import settings
from .base import ProviderNotConfiguredError

BASE = "https://generativelanguage.googleapis.com/v1beta/models"


class GeminiProvider:
    async def generate(self, messages: list[dict]) -> str:
        if not settings.gemini_api_key:
            raise ProviderNotConfiguredError("Thiếu GEMINI_API_KEY trong .env")
        system = "\n".join(m["content"] for m in messages if m["role"] == "system")
        contents = [
            {
                "role": "model" if m["role"] == "assistant" else "user",
                "parts": [{"text": m["content"]}],
            }
            for m in messages
            if m["role"] != "system"
        ]
        body = {
            "systemInstruction": {"parts": [{"text": system}]},
            "contents": contents,
            "generationConfig": {"responseMimeType": "application/json", "temperature": 0.3},
        }
        url = f"{BASE}/{settings.gemini_model}:generateContent"
        async with httpx.AsyncClient(timeout=settings.request_timeout) as client:
            r = await client.post(url, headers={"x-goog-api-key": settings.gemini_api_key}, json=body)
            r.raise_for_status()
            return r.json()["candidates"][0]["content"]["parts"][0]["text"]
