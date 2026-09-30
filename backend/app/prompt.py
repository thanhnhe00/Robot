from pathlib import Path

import yaml

from .config import settings


PROJECT_ROOT = Path(__file__).resolve().parents[2]
PROMPT_FILE = PROJECT_ROOT / "ai" / "prompts" / settings.prompt_version / "system.md"
PERSONALITY_FILE = PROJECT_ROOT / "ai" / "robot_personality.yaml"


def _load_personality() -> dict[str, str]:
    data = yaml.safe_load(PERSONALITY_FILE.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        raise RuntimeError("robot_personality.yaml phải chứa một YAML object")

    required = ("name", "role", "language", "tone", "response_length")
    missing = [key for key in required if not data.get(key)]
    if missing:
        raise RuntimeError(f"robot_personality.yaml thiếu trường: {', '.join(missing)}")
    return data


personality = _load_personality()
personality_text = "\n".join(
    (
        f"Tên: {personality['name']}",
        f"Vai trò: {personality['role']}",
        f"Ngôn ngữ: {personality['language']}",
        f"Giọng điệu: {personality['tone']}",
        f"Độ dài câu trả lời: {personality['response_length']}",
    )
)

SYSTEM_PROMPT = (
    f"Thông tin nhân vật:\n{personality_text}\n\n"
    + PROMPT_FILE.read_text(encoding="utf-8").strip()
)

RETRY_HINT = (
    "Lần trước bạn trả sai định dạng. Chỉ trả về một object JSON hợp lệ "
    "theo mẫu, không giải thích."
)
