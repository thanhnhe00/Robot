from pathlib import Path
from typing import Any

import yaml

from .config import settings

PROJECT_ROOT = Path(__file__).resolve().parents[2]
PROMPT_FILE = PROJECT_ROOT / "ai" / "prompts" / settings.prompt_version / "system.md"
PERSONALITY_FILE = PROJECT_ROOT / "ai" / "robot_personality.yaml"


def _load_personality() -> dict[str, Any]:
    data = yaml.safe_load(PERSONALITY_FILE.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        raise TypeError("robot_personality.yaml phải chứa một YAML object")

    required = ("name", "role", "language", "tone", "response_length")
    missing = [key for key in required if not data.get(key)]
    if missing:
        raise RuntimeError(f"robot_personality.yaml thiếu trường: {', '.join(missing)}")
    return data


def _build_personality_text(data: dict[str, Any]) -> str:
    """Ghép các trường personality thành chuỗi mô tả cho system prompt."""
    parts = [
        f"Tên: {data['name']}",
        f"Vai trò: {data['role']}",
        f"Ngôn ngữ: {data['language']}",
        f"Giọng điệu: {data['tone']}",
        f"Độ dài câu trả lời: {data['response_length']}",
    ]
    if "behavior" in data:
        parts.append(f"Hành vi: {data['behavior']}")
    if "response_style" in data:
        parts.append(f"Phong cách phản hồi: {data['response_style']}")
    if "rules" in data and isinstance(data["rules"], list):
        rules_text = "; ".join(str(r) for r in data["rules"])
        parts.append(f"Quy tắc cốt lõi: {rules_text}")
    return "\n".join(parts)


personality = _load_personality()

SYSTEM_PROMPT = (
    f"Thông tin nhân vật:\n{_build_personality_text(personality)}\n\n"
    + PROMPT_FILE.read_text(encoding="utf-8").strip()
)

RETRY_HINT = (
    "Lần trước bạn trả sai định dạng. Chỉ trả về một object JSON hợp lệ "
    "theo mẫu, không giải thích."
)
