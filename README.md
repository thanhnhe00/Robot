# Robot – trợ lý giọng nói tiếng Việt

Kiến trúc: **Android launcher** ⇄ **Backend FastAPI** ⇄ **Model AI** (Ollama / Gemini).
Model chỉ *đề xuất* action, backend kiểm tra hợp lệ, app Android mới là bên thực thi.

## Chạy backend

```bash
cd backend
python -m venv .venv
# Windows: .venv\Scripts\activate      Linux/macOS: source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env          # Windows: copy .env.example .env
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Đổi `LLM_PROVIDER` trong `.env`:
- `mock`   : giả lập bằng luật, test API không cần model
- `ollama` : chạy local (`ollama pull qwen3:1.7b`)
- `gemini` : cần `GEMINI_API_KEY` (không đưa dữ liệu cá nhân lên bậc miễn phí)

## Thử API

```bash
curl -X POST http://localhost:8000/chat \
  -H "Content-Type: application/json" \
  -d '{"session_id":"thanh","text":"Bây giờ là mấy giờ?"}'
```
Tài liệu API tự sinh: http://localhost:8000/docs

## Định dạng phản hồi (hợp đồng giữa model, backend và app)

```json
{"response": "Mình đặt báo thức 07:30 nhé.",
 "action": {"type": "set_alarm", "params": {"time": "07:30"}}}
```
Action v1: `get_time`, `get_battery`, `set_alarm`, `open_app` (whitelist theo package), `set_volume`.

## Test

```bash
cd backend && python -m pytest -q
```

## Cấu trúc

```
android/    Ứng dụng robot (Phase 3)
backend/    FastAPI, AI Gateway, kiểm tra action, SQLite
ai/         models/ datasets/ training/ evaluation/ prompts/
firmware/   esp32/ (Phase 9)
dashboard/  (Phase 12)
docs/       ARCHITECTURE, ROADMAP, RESEARCH_BACKLOG, decisions/ (ADR)
scripts/  tests/  docker/
```

Tài liệu: `docs/ARCHITECTURE.md`, `docs/ROADMAP.md`, `docs/RESEARCH_BACKLOG.md`, `docs/decisions/`.
