import os
from dataclasses import dataclass
from pathlib import Path

from dotenv import load_dotenv

load_dotenv(Path(__file__).resolve().parent.parent / ".env")


@dataclass(frozen=True)
class Settings:
    provider: str = os.getenv("LLM_PROVIDER", "ollama").lower()
    prompt_version: str = os.getenv("PROMPT_VERSION", "v1")
    ollama_url: str = os.getenv("OLLAMA_URL", "http://localhost:11434")
    ollama_model: str = os.getenv("OLLAMA_MODEL", "qwen3:1.7b")
    gemini_api_key: str = os.getenv("GEMINI_API_KEY", "")
    gemini_model: str = os.getenv("GEMINI_MODEL", "gemini-2.5-flash")
    api_key: str = os.getenv("API_KEY", "")
    history_limit: int = int(os.getenv("HISTORY_LIMIT", "10"))
    db_path: str = os.getenv("DB_PATH", "robot.db")
    request_timeout: float = float(os.getenv("REQUEST_TIMEOUT", "60"))


settings = Settings()
