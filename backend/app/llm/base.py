from typing import Protocol


class AIProvider(Protocol):
    async def generate(self, messages: list[dict]) -> str:
        """Nhận danh sách message và trả về nội dung thô từ model."""
        ...
