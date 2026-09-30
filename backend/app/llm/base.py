from typing import Protocol


class ProviderNotConfiguredError(Exception):
    """Ném ra khi AI provider chưa được cấu hình hoặc cấu hình không hợp lệ."""


class AIProvider(Protocol):
    async def generate(self, messages: list[dict]) -> str:
        """Nhận danh sách message và trả về nội dung thô từ model."""
        ...

