from typing import Any

from pydantic import BaseModel, ConfigDict, Field


class ChatRequest(BaseModel):
    model_config = ConfigDict(
        json_schema_extra={
            "examples": [
                {
                    "session_id": "thanh",
                    "text": "Bây giờ là mấy giờ?",
                }
            ]
        }
    )

    session_id: str = Field(default="default", max_length=64)
    text: str = Field(min_length=1, max_length=2000)


class Action(BaseModel):
    type: str
    params: dict[str, Any] = Field(default_factory=dict)


class ChatResponse(BaseModel):
    model_config = ConfigDict(
        json_schema_extra={
            "examples": [
                {
                    "response": "",
                    "action": {"type": "get_time", "params": {}},
                },
                {"response": "Chào bạn!", "action": None},
            ]
        }
    )

    response: str
    action: Action | None = None
