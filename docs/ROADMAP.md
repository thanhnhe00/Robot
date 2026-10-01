# Robot – Roadmap (cập nhật 2026-09-30, ADR-0002 và ADR-0004)

Thay đổi so với bản gốc:
- **Spike LLM trên Samsung Galaxy Z Flip5 (Phase 4) trước Voice (Phase 5)** – ADR-0002.
- Bổ sung 6 hạng mục xuyên suốt (offline/hybrid, model abstraction, evaluation, dataset strategy, RAG gate, test/reliability) – ADR-0004.

| Phase | Nội dung | Definition of Done (tóm tắt) | Phần cứng |
|---|---|---|---|
| 0 | Architecture + Research | Tạm để mở; quay lại review ADR-0005 và đóng các mục Phase 0 sau giai đoạn thực hành | Galaxy Z Flip5, laptop |
| 1 | Backend AI Gateway **+ Model abstraction** | Provider đổi bằng cấu hình, prompt versioned, personality yaml, Docker, CI, Swagger | laptop |
| 2 | Agent + Action System **+ Evaluation v0** | Action registry, JSON Schema chung, safety pipeline, test an toàn, eval harness, **golden test set** | laptop |
| 3 | Android app | UI + state machine, Action Executor, `AIProvider` phía Android, nhập text | Galaxy Z Flip5 |
| 4 | Spike LLM trên Galaxy Z Flip5 **+ Model Manager v0** | `LocalProvider`, benchmark đủ metric, go/no-go | Galaxy Z Flip5 |
| 5 | Voice | Wake word, STT, TTS tiếng Việt, benchmark từng khối | Galaxy Z Flip5 |
| 6 | Memory + Tools **+ AI Router + RAG gate** | Memory, tools xác định, router Local/Laptop/Cloud, phân loại riêng tư, offline cơ bản, ADR "cần/không cần RAG" | Galaxy Z Flip5 |
| 7 | **Dataset strategy** + Fine-tuning | Dataset (sẵn → synthetic → augment → kiểm tay), base vs fine-tuned có số liệu | Colab/Kaggle |
| 8 | Camera + CV | Object detection, face recognition (prototype) | Camera Galaxy Z Flip5 |
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

Quy tắc thông thường: xong phase trước rồi mới sang phase sau. Ngoại lệ hiện tại: theo chỉ đạo của chủ dự án, thực hành Phase 1 trước và quay lại đóng Phase 0 sau.

Trạng thái tại 2026-10-01: Phase 1 (AI Gateway), Phase 2 (Action Registry & Eval v0), và Phase 3 (Android App + Safety Pipeline + Robot Face + Action Executor trên Samsung Galaxy Z Flip5) đã hoàn thành. Sẵn sàng bắt đầu Phase 4 (Spike LLM trên Z Flip5 + Model Manager v0).
