from ..config import settings
from .base import AIProvider, ProviderNotConfiguredError
from .gemini import GeminiProvider
from .mock import MockProvider
from .ollama import OllamaProvider


def get_provider() -> AIProvider:
    providers = {
        "ollama": OllamaProvider,
        "gemini": GeminiProvider,
        "mock": MockProvider,
    }
    try:
        return providers[settings.provider]()
    except KeyError as exc:
        raise ProviderNotConfiguredError(
            f"LLM_PROVIDER không hợp lệ: {settings.provider}"
        ) from exc
