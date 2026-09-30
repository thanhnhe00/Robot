from typing import Any, Optional

from pydantic import BaseModel, Field


class ChatRequest(BaseModel):
    session_id: str = Field(default="default", max_length=64)
    text: str = Field(min_length=1, max_length=2000)


class Action(BaseModel):
    type: str
    params: dict[str, Any] = Field(default_factory=dict)


class ChatResponse(BaseModel):
    response: str
    action: Optional[Action] = None
