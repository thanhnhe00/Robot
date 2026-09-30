from typing import Protocol


class LLMProvider(Protocol):
    """Giao diện chung. Đổi model = đổi provider, phần còn lại không phải sửa."""

    async def generate(self, messages: list[dict]) -> str:
        """messages: [{"role": "system|user|assistant", "content": "..."}]. Trả về text thô."""
        ...
