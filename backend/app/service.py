import logging
import time
import uuid

from .actions import parse_model_output
from .config import settings
from .db import repository
from .llm import get_provider
from .llm.base import AIProvider
from .prompt import RETRY_HINT, SYSTEM_PROMPT
from .schemas import ChatResponse

log = logging.getLogger("robot")
provider: AIProvider = get_provider()


FALLBACK_RESPONSE = "Xin lỗi, mình chưa hiểu yêu cầu đó."


async def handle_chat(session_id: str, text: str) -> ChatResponse:
    request_id = uuid.uuid4().hex
    started = time.perf_counter()

    try:
        history = repository.get_history(session_id, settings.history_limit)
        messages = [
            {"role": "system", "content": SYSTEM_PROMPT},
            *history,
            {"role": "user", "content": text},
        ]

        raw = await provider.generate(messages)
        validation = "valid"

        try:
            result = parse_model_output(raw)
            # Model parsed OK, but validator may have rejected the action
            if result.action_rejection:
                validation = "action_rejected"
        except ValueError:
            validation = "retry"
            log.warning(
                "chat.output_invalid",
                extra={
                    "event": "chat.output_invalid",
                    "request": {"id": request_id, "chars": len(text)},
                    "validation": validation,
                },
            )
            raw = await provider.generate(
                [*messages, {"role": "user", "content": RETRY_HINT}]
            )
            try:
                result = parse_model_output(raw)
                validation = "retry_recovered"
                if result.action_rejection:
                    validation = "retry_action_rejected"
            except ValueError:
                result = ChatResponse(response=FALLBACK_RESPONSE, action=None)
                validation = "fallback_text"

        repository.add_message(session_id, "user", text)
        repository.add_message(session_id, "assistant", result.model_dump_json())

        log.info(
            "chat.completed",
            extra={
                "event": "chat.completed",
                "request": {"id": request_id, "chars": len(text)},
                "provider": settings.provider,
                "model": settings.model_name,
                "prompt_version": settings.prompt_version,
                "latency_ms": round((time.perf_counter() - started) * 1000, 2),
                "response": {"chars": len(result.response)},
                "action": result.action.type if result.action else None,
                "action_rejection": result.action_rejection,
                "validation": validation,
            },
        )
        return result
    except Exception as exc:
        log.error(
            "chat.failed",
            extra={
                "event": "chat.failed",
                "request": {"id": request_id, "chars": len(text)},
                "provider": settings.provider,
                "model": settings.model_name,
                "prompt_version": settings.prompt_version,
                "latency_ms": round((time.perf_counter() - started) * 1000, 2),
                "error_type": type(exc).__name__,
                "error_detail": str(exc),
            },
        )
        raise
