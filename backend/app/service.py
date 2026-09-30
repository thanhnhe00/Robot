import logging

from . import db
from .actions import parse_model_output
from .config import settings
from .llm import get_provider
from .prompt import RETRY_HINT, SYSTEM_PROMPT
from .schemas import ChatResponse

log = logging.getLogger("robot")
provider = get_provider()


async def handle_chat(session_id: str, text: str) -> ChatResponse:
    history = db.get_history(session_id, settings.history_limit)
    messages = [{"role": "system", "content": SYSTEM_PROMPT}, *history, {"role": "user", "content": text}]

    raw = await provider.generate(messages)
    try:
        result = parse_model_output(raw)
    except ValueError as e:
        log.warning("Output lỗi (%s), thử lại 1 lần: %r", e, raw[:200])
        raw = await provider.generate([*messages, {"role": "user", "content": RETRY_HINT}])
        try:
            result = parse_model_output(raw)
        except ValueError:
            log.error("Vẫn lỗi, dùng text thô: %r", raw[:200])
            result = ChatResponse(response=raw.strip()[:300], action=None)

    db.add_message(session_id, "user", text)
    db.add_message(session_id, "assistant", result.model_dump_json())
    return result
