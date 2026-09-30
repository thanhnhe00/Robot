# ROBOTV1 – Architecture (v0.1, Phase 0)

Trạng thái: **bản nháp kiến trúc**. Mọi thay đổi lớn phải có Decision Record trong `docs/decisions/`.

## Nguyên tắc
1. **Local-first, hybrid:** điện thoại → laptop/local server → cloud. Không phụ thuộc hoàn toàn cloud.
2. **LLM chỉ đề xuất.** Validate + thực thi do ứng dụng làm (ADR-0001).
3. **LLM không bao giờ điều khiển phần cứng trực tiếp.**
4. **Không khóa vào một model:** truy cập qua `AIProvider`, đổi bằng cấu hình.
5. **An toàn không phụ thuộc AI:** watchdog trên ESP32 + nút e-stop vật lý.

## Sơ đồ

```
 ┌──────────────────────── ANDROID BRAIN (LG G8) ─────────────────────────┐
 │ UI + State machine: IDLE → WAKE → LISTENING → PROCESSING → SPEAKING    │
 │ Audio: Wake word → VAD → STT                    TTS → Speaker          │
 │ AGENT: Personality (yaml) + Memory + Tool selection                    │
 │ AI ROUTER: task, privacy, network, complexity                          │
 │   LocalProvider │ LaptopProvider │ CloudProvider │ MockProvider        │
 │        │ structured output (chỉ là ĐỀ XUẤT)                            │
 │ SAFETY PIPELINE: Schema → Permission → Whitelist → Limits              │
 │ ACTION EXECUTOR (timeout, log): Android actions │ Robot commands       │
 └──────────┬──────────────────────────────────────┬──────────────────────┘
            │ REST (WebSocket khi có use case)      │ USB / BLE / WiFi (chưa chốt)
            ▼                                       ▼
 ┌─── BACKEND (FastAPI, laptop/Docker) ──┐   ┌────── ESP32 BODY ───────────┐
 │ AI Gateway, routing, logging, DB      │   │ L1: watchdog, giới hạn tốc  │
 │ metrics, dashboard API                │   │ độ/thời gian, dừng khi mất  │
 └───────────────────────────────────────┘   │ kết nối / có vật cản        │
                                             │ L0: E-STOP VẬT LÝ           │
 (Sau này) Camera → Vision → Perception →    └─────────────────────────────┘
 Agent → Navigation → ESP32 → Motor
```

## Các lớp an toàn
| Lớp | Nơi | Vai trò |
|---|---|---|
| L0 | Phần cứng | E-stop cắt nguồn/enable motor, ưu tiên cao hơn mọi lệnh phần mềm |
| L1 | Firmware ESP32 | Watchdog, timeout, giới hạn tốc độ/thời gian, dừng khi mất liên lạc/có vật cản |
| L2 | App Android | Safety pipeline: schema, permission, whitelist, giới hạn tham số |
| L3 | LLM | Chỉ đề xuất action |

## Hợp đồng dữ liệu (baseline v1)
```json
{"response": "Được, mình mở YouTube nhé.",
 "action": {"type": "open_app", "params": {"package": "com.google.android.youtube"}}}
```
Nguồn sự thật cuối cùng sẽ là **JSON Schema dùng chung** (ADR-0001, thực hiện ở Phase 2).
Hiện tại (Phase 1 baseline) luật nằm trong `backend/app/actions.py`.

## Trạng thái các thành phần
| Thành phần | Trạng thái |
|---|---|
| Backend FastAPI + provider abstraction | Baseline có, sẽ nâng cấp ở Phase 1 |
| Safety pipeline / action registry | Baseline sơ khai, làm đầy đủ ở Phase 2 |
| Engine LLM local, model, STT, TTS, wake word | **CHƯA CHỐT** – xem `RESEARCH_BACKLOG.md` |
| Android: Kotlin hay Flutter | Nghiêng Kotlin, **chưa chốt** |
| Phone ↔ ESP32 | Nghiêng USB, **chưa chốt** |
