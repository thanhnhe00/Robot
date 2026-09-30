import logging
from contextlib import asynccontextmanager

import httpx
from fastapi import Depends, FastAPI, Header, HTTPException, WebSocket, WebSocketDisconnect

from . import db
from .config import settings
from .schemas import ChatRequest, ChatResponse
from .service import handle_chat

logging.basicConfig(level=logging.INFO)


@asynccontextmanager
async def lifespan(app: FastAPI):
    db.init_db()
    yield


app = FastAPI(title="Robot Backend", version="0.1.0", lifespan=lifespan)


def check_key(x_api_key: str = Header(default="")) -> None:
    if settings.api_key and x_api_key != settings.api_key:
        raise HTTPException(status_code=401, detail="Sai API key")


@app.get("/health")
async def health():
    return {"status": "ok", "provider": settings.provider}


@app.post("/chat", response_model=ChatResponse, dependencies=[Depends(check_key)])
async def chat(req: ChatRequest):
    try:
        return await handle_chat(req.session_id, req.text)
    except httpx.HTTPError as e:
        raise HTTPException(status_code=502, detail=f"Lỗi khi gọi model: {e}")
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.websocket("/ws")
async def ws_chat(ws: WebSocket, key: str = ""):
    if settings.api_key and key != settings.api_key:
        await ws.close(code=1008)
        return
    await ws.accept()
    try:
        while True:
            try:
                req = ChatRequest.model_validate(await ws.receive_json())
                res = await handle_chat(req.session_id, req.text)
                await ws.send_json(res.model_dump())
            except WebSocketDisconnect:
                raise
            except Exception as e:  # giữ kết nối, báo lỗi cho client
                await ws.send_json({"error": str(e)})
    except WebSocketDisconnect:
        pass
