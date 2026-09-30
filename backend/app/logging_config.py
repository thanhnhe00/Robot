import json
import logging
from datetime import datetime, timezone

_FIELDS = (
    "request",
    "provider",
    "model",
    "latency_ms",
    "response",
    "action",
    "validation",
    "error_type",
)


class JsonFormatter(logging.Formatter):
    def format(self, record: logging.LogRecord) -> str:
        data = {
            "timestamp": datetime.fromtimestamp(
                record.created, timezone.utc
            ).isoformat(),
            "level": record.levelname,
            "event": getattr(record, "event", "app.log"),
        }
        for field in _FIELDS:
            value = getattr(record, field, None)
            if value is not None:
                data[field] = value
        return json.dumps(data, ensure_ascii=False)


def configure_logging(level: str) -> None:
    logger = logging.getLogger("robot")
    logger.handlers.clear()

    handler = logging.StreamHandler()
    handler.setFormatter(JsonFormatter())
    logger.addHandler(handler)
    logger.setLevel(getattr(logging, level.upper(), logging.INFO))
    logger.propagate = False
