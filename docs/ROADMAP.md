# ROBOTV1 – Roadmap (cập nhật 2026-09-29, ADR-0002 và ADR-0004)

Thay đổi so với bản gốc:
- **Spike LLM trên G8 (Phase 4) trước Voice (Phase 5)** – ADR-0002.
- Bổ sung 6 hạng mục xuyên suốt (offline/hybrid, model abstraction, evaluation, dataset strategy, RAG gate, test/reliability) – ADR-0004.

| Phase | Nội dung | Definition of Done (tóm tắt) | Phần cứng |
|---|---|---|---|
| 0 | Architecture + Research | Docs, ADR, research backlog xử lý xong, khung repo | G8, laptop |
| 1 | Backend AI Gateway **+ Model abstraction** | `AIProvider` (đổi model bằng cấu hình), prompt versioned, personality yaml, Docker, CI, Swagger | laptop |
| 2 | Agent + Action System **+ Evaluation v0** | Action registry, JSON Schema chung, safety pipeline, test an toàn, eval harness, **golden test set** | laptop |
| 3 | Android app | UI + state machine, Action Executor, `AIProvider` phía Android, nhập text | G8 |
| 4 | Spike LLM trên G8 **+ Model Manager v0** | `LocalProvider`, benchmark đủ metric, go/no-go | G8 |
| 5 | Voice | Wake word, STT, TTS tiếng Việt, benchmark từng khối | G8 |
| 6 | Memory + Tools **+ AI Router + RAG gate** | Memory, tools xác định, router Local/Laptop/Cloud, phân loại riêng tư, offline cơ bản, ADR "cần/không cần RAG" | G8 |
| 7 | **Dataset strategy** + Fine-tuning | Dataset (sẵn → synthetic → augment → kiểm tay), base vs fine-tuned có số liệu | Colab/Kaggle |
| 8 | Camera + CV | Object detection, face recognition (prototype) | camera G8 |
| 9 | ESP32 | Liên kết phone↔ESP32, watchdog, telemetry, test giao tiếp | ESP32 |
| 10 | Motor + Safety | Motor có giới hạn, e-stop vật lý, dừng khi mất kết nối/vật cản | motor, driver, pin |
| 11 | Navigation | Né vật cản, đi theo người | sensor |
| 12 | Dashboard + DevOps **+ Model update** | Dashboard, CI đầy đủ, cập nhật model có checksum/rollback | – |
| 13 | Tích hợp cuối **+ E2E + Reliability** | E2E, nghiệm thu offline, crash/reconnect, soak test | tất cả |
| 14 | Portfolio | README, kiến trúc, benchmark, báo cáo, demo | – |

## Các mảng xuyên suốt

| Mảng | Nội dung | Bắt đầu |
|---|---|---|
| Model abstraction | Mọi model truy cập qua `AIProvider` (backend và Android); đổi bằng cấu hình | Phase 1 |
| Evaluation | Metric: action accuracy, JSON validity, intent/tool accuracy, latency, tokens/s, RAM, CPU, pin, model load time, crash rate; cùng bộ prompt cho mọi model | Phase 2, mở rộng mọi phase |
| Dataset | Golden test set (không dùng để train) từ Phase 2; chiến lược train dataset ở Phase 7 | Phase 2 |
| Offline / Hybrid | Router + offline mode ở Phase 6; nghiệm thu offline ở Phase 13 | Phase 6 |
| Test / Reliability | Unit → API → Agent/Action → Android → ESP32 → E2E → crash/reconnect | Phase 1 |
| RAG | Chỉ làm nếu benchmark chứng minh cần (Phase 6) | Phase 6 |

Quy tắc: xong phase trước, báo kết quả, mới sang phase sau. Docker và CI tối thiểu bắt đầu từ Phase 1.
