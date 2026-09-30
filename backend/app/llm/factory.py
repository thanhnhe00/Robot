from ..config import settings
from .base import LLMProvider
from .gemini import GeminiProvider
from .mock import MockProvider
from .ollama import OllamaProvider


def get_provider() -> LLMProvider:
    providers = {"ollama": OllamaProvider, "gemini": GeminiProvider, "mock": MockProvider}
    try:
        return providers[settings.provider]()
    except KeyError:
        raise RuntimeError(f"LLM_PROVIDER không hợp lệ: {settings.provider}")
