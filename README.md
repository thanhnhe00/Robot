# Robot – trợ lý giọng nói tiếng Việt

Kiến trúc: **Android launcher** ⇄ **Backend FastAPI** ⇄ **Model AI** (Ollama / Gemini / Mock).
Model chỉ *đề xuất* action, backend kiểm tra hợp lệ, app Android mới là bên thực thi.

## 1. Chạy backend cục bộ

```bash
cd backend
python -m venv .venv
# Windows: .venv\Scripts\activate      Linux/macOS: source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env          # Windows: copy .env.example .env
uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```

## 2. Chạy bằng Docker Compose

```bash
docker compose up -d
```
Backend sẽ khởi chạy tại `http://127.0.0.1:8000`, dữ liệu SQLite được mount an toàn vào Docker volume `robot_data`.

## 3. Cấu hình biến môi trường (`.env`)

| Biến | Mặc định | Mô tả |
|---|---|---|
| `LLM_PROVIDER` | `mock` / `ollama` | Provider: `mock`, `ollama`, `gemini` |
| `PROMPT_VERSION` | `v1` | Phiên bản system prompt trong `ai/prompts/{PROMPT_VERSION}/system.md` |
| `OLLAMA_URL` | `http://localhost:11434` | Endpoint Ollama khi dùng provider `ollama` |
| `OLLAMA_MODEL` | `qwen3:1.7b` | Model Ollama |
| `GEMINI_API_KEY` | *(trống)* | API Key Google Gemini |
| `GEMINI_MODEL` | `gemini-2.5-flash` | Model Gemini |
| `API_KEY` | *(trống)* | Khóa xác thực API gateway qua header `X-API-Key` |
| `PERSISTENCE_ENABLED`| `true` | Bật/tắt lưu lịch sử vào SQLite database (`true`/`false`) |
| `DB_PATH` | `robot.db` | Đường dẫn file SQLite |
| `LOG_LEVEL` | `INFO` | Cấp độ log có cấu trúc JSON |

Nhân vật robot được định nghĩa độc lập tại [ai/robot_personality.yaml](ai/robot_personality.yaml) (`name`, `role`, `language`, `tone`, `behavior`, `rules`...).

## 4. Thử API

```bash
curl -X POST http://127.0.0.1:8000/chat \
  -H "Content-Type: application/json" \
  -d '{"session_id":"thanh","text":"Bây giờ là mấy giờ?"}'
```
- Tài liệu API tương tác tự sinh (Swagger UI): http://127.0.0.1:8000/docs
- WebSocket endpoint: `ws://127.0.0.1:8000/ws` (hỗ trợ `?key=...` nếu có cấu hình `API_KEY`)

## 5. Định dạng phản hồi

```json
{
  "response": "Mình đặt báo thức 07:30 nhé.",
  "action": {
    "type": "set_alarm",
    "params": {"time": "07:30"}
  }
}
```
- Action v1: `get_time`, `get_battery`, `set_alarm`, `open_app` (whitelist theo package), `set_volume`.
- Fallback an toàn: Khi model trả JSON sai định dạng sau lượt thử lại, hệ thống trả lời cố định: `"Xin lỗi, mình chưa hiểu yêu cầu đó."` với `action: null`.

## 6. Kiểm thử và Lint (CI)

```bash
# Kiểm tra lint bằng Ruff
ruff check backend

# Chạy toàn bộ test
pytest backend/tests -q
```
GitHub Actions CI tự động kiểm tra Lint (Ruff), Unit/Contract Tests (Pytest) và Docker build trên runner `ubuntu-24.04`.

## 7. Cấu trúc Monorepo

```
android/    Ứng dụng robot (Phase 3)
backend/    FastAPI AI Gateway, routing, validation, repository, logging
ai/         prompts/ (versioned), robot_personality.yaml, datasets/, models/
firmware/   esp32/ (Phase 9)
dashboard/  (Phase 12)
docs/       ARCHITECTURE, ROADMAP, RESEARCH_BACKLOG, decisions/ (ADR), issues/
docker/     Dockerfile.backend, docker-compose.yml
```
