from pathlib import Path

from .config import settings


_PROMPT_FILE = Path(__file__).resolve().parents[2] / "ai" / "prompts" / settings.prompt_version / "system.md"
SYSTEM_PROMPT = _PROMPT_FILE.read_text(encoding="utf-8")

RETRY_HINT = "Lần trước bạn trả sai định dạng. Chỉ trả về một object JSON hợp lệ theo mẫu, không giải thích."
