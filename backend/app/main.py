import logging
import secrets
from contextlib import asynccontextmanager

import httpx
from fastapi import (
    Depends,
    FastAPI,
    Header,
    HTTPException,
    WebSocket,
    WebSocketDisconnect,
)

from .config import settings
from .db import repository
from .llm.base import ProviderNotConfiguredError
from .logging_config import configure_logging
from .schemas import ChatRequest, ChatResponse
from .service import handle_chat

configure_logging(settings.log_level)
log = logging.getLogger("robot")


@asynccontextmanager
async def lifespan(app: FastAPI):
    if settings.provider == "gemini" and not settings.api_key:
        log.warning(
            "Cảnh báo bảo mật: Provider Gemini đang bật nhưng API_KEY bảo vệ backend chưa được thiết lập."
        )
    repository.initialize()
    yield


app = FastAPI(
    title="Robot Backend",
    version="0.1.0",
    description="AI Gateway cho trợ lý Robot.",
    lifespan=lifespan,
)


def check_key(x_api_key: str = Header(default="")) -> None:
    if settings.api_key and not secrets.compare_digest(x_api_key, settings.api_key):
        raise HTTPException(status_code=401, detail="Sai API key")


@app.get(
    "/health",
    summary="Kiểm tra backend",
    description="Trả về trạng thái backend và provider đang cấu hình.",
)
async def health():
    return {"status": "ok", "provider": settings.provider}


@app.post(
    "/chat",
    summary="Gửi tin nhắn cho Robot",
    response_model=ChatResponse,
    dependencies=[Depends(check_key)],
    responses={
        401: {
            "description": "API key không hợp lệ.",
            "content": {"application/json": {"example": {"detail": "Sai API key"}}},
        },
        502: {
            "description": "Không gọi được AI provider.",
            "content": {
                "application/json": {
                    "example": {"detail": "Không thể kết nối tới AI provider."}
                }
            },
        },
        503: {
            "description": "AI provider chưa được cấu hình.",
            "content": {
                "application/json": {
                    "example": {"detail": "AI provider chưa được cấu hình."}
                }
            },
        },
    },
)
async def chat(req: ChatRequest):
    try:
        return await handle_chat(req.session_id, req.text)
    except httpx.HTTPError as exc:
        raise HTTPException(
            status_code=502,
            detail="Không thể kết nối tới AI provider.",
        ) from exc
    except ProviderNotConfiguredError as exc:
        raise HTTPException(
            status_code=503,
            detail="AI provider chưa được cấu hình.",
        ) from exc


@app.websocket("/ws")
async def ws_chat(ws: WebSocket):
    """WebSocket chat endpoint.

    Auth: nếu API_KEY được cấu hình, client gửi message đầu tiên dạng
    ``{"auth": "<key>", "session_id": "...", "text": "..."}``
    hoặc ``{"auth": "<key>"}`` rồi gửi request sau.
    Key KHÔNG truyền qua query param ``?key=`` vì dễ lộ trong log/URL.
    """
    await ws.accept()
    authenticated = not settings.api_key  # no key configured → auto-auth

    try:
        while True:
            try:
                data = await ws.receive_json()

                # Auth check trên message đầu tiên
                if not authenticated:
                    client_key = data.pop("auth", "")
                    if not secrets.compare_digest(str(client_key), settings.api_key):
                        await ws.close(code=1008, reason="Sai API key")
                        return
                    authenticated = True
                    # Nếu message chỉ chứa auth, đợi message tiếp
                    if not data.get("text"):
                        continue

                req = ChatRequest.model_validate(data)
                res = await handle_chat(req.session_id, req.text)
                await ws.send_json(res.model_dump())
            except WebSocketDisconnect:
                raise
            except Exception as exc:  # noqa: BLE001  # WebSocket loop fallback for unhandled exceptions
                log.exception("ws.request_failed", exc_info=exc)
                await ws.send_json({"error": "Không thể xử lý yêu cầu."})
    except WebSocketDisconnect:
        pass
