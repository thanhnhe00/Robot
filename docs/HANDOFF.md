# ROBOTV1 – Handoff Brief (cho AI coding agent / IDE)

Tổng hợp ngày 2026-09-30. Đây là tài liệu tự đủ: đọc xong là hiểu project, quy tắc, trạng thái, lộ trình và việc cần chuẩn bị.
Ngôn ngữ tài liệu: tiếng Việt. Định danh trong code (tên biến, hàm, trường JSON): tiếng Anh.

---

## 0. Prompt khởi động (dán vào agent)

> Bạn là kỹ sư phần mềm/AI/robotics làm việc trong repo ROBOTV1. Đọc toàn bộ `docs/HANDOFF.md`, `docs/ARCHITECTURE.md`, `docs/ROADMAP.md`, `docs/RESEARCH_BACKLOG.md` và `docs/decisions/`.
> Tuân thủ mục 2 (Quy tắc bất biến). Chỉ làm **một issue mỗi lần**, bắt đầu từ issue Phase 0 chưa xong (hiện là **0.2 Research nhóm A**). Không viết code ứng dụng mới trước khi Phase 0 hoàn tất.
> Với mỗi issue: tóm tắt kế hoạch trước, chờ xác nhận, làm, chạy test, rồi báo kết quả (đã làm gì, bằng chứng, việc còn lại, điều chưa chắc). Không tự đổi kiến trúc: nếu phát hiện vấn đề thì dừng, giải thích, đưa phương án và trade-off.

---

## 1. Tóm tắt project

**ROBOTV1** là robot AI **local-first, hybrid**: điện thoại Android (LG G8) là "bộ não", backend FastAPI chạy trên laptop, về sau có ESP32 làm "thân" để di chuyển.

- Giao tiếp tiếng Việt (wake word → STT → AI → TTS), có memory, có agent/tool, thực thi action Android an toàn.
- Ưu tiên AI chạy trực tiếp trên điện thoại; nếu không đủ thì laptop; nếu cần thì cloud. Không phụ thuộc hoàn toàn cloud.
- Mục tiêu song song: portfolio xin việc IT, nghiên cứu AI thực tế, robot vật lý, nền tảng có thể phát triển thành sản phẩm.
- Đây là project thật: không demo giả, không hard-code kết quả, mock phải nói rõ là mock.

Người dùng: Thanh, sinh viên CNTT. Làm trong VS Code, repo GitHub public. Quen backend (Spring Boot, Node), dùng tiếng Việt.

---

## 2. Quy tắc bất biến cho agent

**An toàn và kiến trúc**
1. **LLM chỉ đề xuất action.** Luồng bắt buộc: `User → STT → LLM → Structured output → Schema validation → Permission/Safety validation → Action Executor → Android/ESP32`. LLM không bao giờ điều khiển phần cứng/motor trực tiếp.
2. **Whitelist**: action hoặc package ngoài danh sách cho phép thì từ chối, không thực thi.
3. Validator + executor nằm **trong app Android** (chạy được offline); backend validate lại như lớp thứ hai (ADR-0001). Luật hợp lệ định nghĩa ở **một JSON Schema chung**.
4. Mọi lệnh motor phải có direction, giới hạn tốc độ, duration, timeout; có obstacle safety; **mất kết nối phone↔ESP32 thì dừng ngay** (watchdog trên ESP32); có **e-stop vật lý** ưu tiên cao hơn mọi lệnh phần mềm. An toàn không phụ thuộc AI.
5. Không dùng LLM cho việc xác định được: "mấy giờ rồi" gọi đồng hồ hệ thống, không hỏi LLM.
6. Không khóa vào một model: truy cập qua `AIProvider` (Local/Laptop/Cloud/Mock), đổi bằng cấu hình. Dataset, prompt (có version), evaluation độc lập với model. Personality là file config riêng.
7. Nếu JSON của model sai: validate → repair/retry nếu hợp lý → vẫn sai thì **không execute**, trả "Xin lỗi, mình chưa hiểu yêu cầu đó."

**Trung thực và quy trình**
8. Không bịa. Dùng nhãn **CHƯA BIẾT / CẦN RESEARCH / ASSUMPTION**; không biến assumption thành fact.
9. Không tuyên bố model/engine nào "tốt nhất" hay "chạy tốt trên G8" khi chưa benchmark thật.
10. Trước khi chốt model, thư viện, SDK, engine: kiểm tra tài liệu/nguồn chính thức hiện hành; nguồn cộng đồng phải ghi rõ. Không dựa vào thông tin cũ.
11. Không tự đổi kiến trúc lớn; mọi quyết định quan trọng ghi **ADR** (Decision / Why / Alternatives / Trade-offs / Chosen / Date) trong `docs/decisions/`.
12. Làm từng bước nhỏ, báo kết quả, chờ xác nhận. Không đưa quá nhiều code một lần. Nếu bước trước chưa xong thì không nhảy bước sau.
13. Không tự mua hay chọn phần cứng thay người dùng. Không giả lập phần cứng mà không ghi rõ là mock.
14. Thứ tự ưu tiên: Correctness → Safety → Architecture → Maintainability → Performance → Feature count. Không overengineering; không thêm WebSocket, auth, vector DB, RAG chỉ để "trông chuyên nghiệp".

**Code và repo**
15. Cân bằng dễ hiểu và production-ready: type/schema, validation, error handling, test, tài liệu.
16. Mỗi phase có test. Commit theo `feat: / fix: / refactor: / test: / docs: / perf: / chore:`. Không commit API key, mật khẩu, dữ liệu riêng tư, model weights (`*.gguf` đã nằm trong `.gitignore`).
17. Log: timestamp, request, model, provider, latency, response, action, kết quả validate, kết quả thực thi, lỗi. Không log dữ liệu nhạy cảm không cần thiết.
18. Dữ liệu cá nhân phân loại LOCAL ONLY / OPTIONAL CLOUD / PUBLIC-SAFE; dữ liệu LOCAL ONLY không gửi cloud.

---

## 3. Trạng thái hiện tại (tính đến 2026-09-30)

**Đã có (trong repo `robot/`)**
- Backend FastAPI baseline: `POST /chat`, WebSocket `/ws`, SQLite lưu hội thoại, provider Ollama / Gemini / Mock, kiểm tra action (whitelist, giới hạn tham số), tự thử lại 1 lần khi JSON sai, `API_KEY` tùy chọn. **12 test qua** (với provider mock; chưa test với Ollama/Gemini thật).
- Hợp đồng dữ liệu: `{"response": "...", "action": {"type": "...", "params": {...}}}`. Action v1: `get_time`, `get_battery`, `set_alarm`, `open_app` (theo `package`), `set_volume`.
- Cấu trúc repo theo mục 61 của spec: `android/ backend/ ai/ firmware/esp32/ dashboard/ docs/ scripts/ tests/ docker/`.
- Docs: `ARCHITECTURE.md`, `ROADMAP.md`, `RESEARCH_BACKLOG.md`, ADR-0001…0004, 21 file issue trong `docs/issues/`, script `scripts/create_issues.py`, cấu hình VS Code.

**Chưa có / chưa xác nhận**
- Việc đẩy repo lên GitHub và tạo issue thật bằng `create_issues.py` (script mới chỉ chạy `--dry-run`): **do người dùng xác nhận**.
- Chưa có: Docker, CI, Android app, engine LLM local, STT/TTS/wake word, memory ngoài lịch sử hội thoại, ESP32, camera/CV, dashboard.
- Chưa có benchmark nào trên G8.

**Phần cứng**
- LG G8 (điện thoại duy nhất, không có máy phụ). Người dùng ghi RAM 8GB, Android 13/14, màn hình/cảm ứng hoạt động — **ASSUMPTION, chưa xác minh** (nguồn tra cứu ghi G8 ThinQ 6GB RAM, Android chính hãng tối đa 11–12). Người dùng bảo bỏ qua việc xác minh; thông số thật sẽ lộ ra khi benchmark ở Phase 4.
- Laptop dev: Ryzen 7 5700U, 16GB RAM, không GPU, Windows + Linux. Train nặng dùng Colab/Kaggle/cloud (ước tính chi phí trước).
- Chưa có ESP32, motor, driver, servo, sensor, camera/mic/loa riêng, pin robot.

---

## 4. Kiến trúc

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

Các lớp an toàn: **L0** e-stop phần cứng → **L1** firmware ESP32 → **L2** safety pipeline trên Android → **L3** LLM chỉ đề xuất.

Ràng buộc offline: wake word, STT/TTS local, LLM local, memory local, Android actions, điều khiển robot cơ bản phải chạy không cần mạng; thời tiết, tin tức, cloud model cần mạng.

---

## 5. Công nghệ

**Đã chốt**
| Thành phần | Lựa chọn |
|---|---|
| Backend | FastAPI + Python |
| Database prototype | SQLite (PostgreSQL sau nếu cần) |
| Repo | Monorepo, GitHub public |
| CI/CD | GitHub Actions (bắt đầu Phase 1) |
| Editor | VS Code |

**Chưa chốt (cần research/benchmark, KHÔNG được coi là quyết định)**
| Thành phần | Ứng viên | Ghi chú |
|---|---|---|
| Framework Android | Kotlin (nghiêng), Flutter | Nghiêng Kotlin vì cần JNI/NDK, audio, service; chốt bằng ADR sau research nhóm C |
| Engine LLM local | llama.cpp (ứng viên đầu, tự build bằng NDK), MLC, ONNX Runtime GenAI, MediaPipe/LiteRT-LM, ExecuTorch | Research nhóm A |
| Model | Qwen3 0.6B / 1.7B (ứng viên đầu), 1–2 model nhỏ khác | Benchmark trên G8; quantization Q4_K_M/Q5_K_M/Q6_K/Q8_0 |
| STT / TTS / wake word tiếng Việt | Android SpeechRecognizer, sherpa-onnx, whisper.cpp, cloud; Piper; openWakeWord, Porcupine | Research nhóm B |
| Phone ↔ ESP32 | USB (nghiêng), BLE, WiFi | Chốt ở Phase 9. Lưu ý USB: phone gắn trên robot nên cáp ngắn; rủi ro là sạc + OTG cùng lúc, rung |
| Vision | MediaPipe, TFLite, họ YOLO | Kiểm tra license (một số bản là AGPL); Phase 8 |
| RAG / vector DB | — | **Chỉ làm nếu benchmark chứng minh cần** (Phase 6) |

---

## 6. Lộ trình chi tiết

Thứ tự đã duyệt (ADR-0002, ADR-0004): spike LLM trên G8 (Phase 4) đặt **trước** Voice (Phase 5). Quy tắc: xong phase trước, báo kết quả, mới sang phase sau.

| Phase | Nội dung | Phần cứng |
|---|---|---|
| 0 | Architecture + Research | G8, laptop |
| 1 | Backend AI Gateway + Model abstraction | laptop |
| 2 | Agent + Action System + Evaluation v0 | laptop |
| 3 | Android app | G8 |
| 4 | Spike LLM trên G8 + Model Manager v0 | G8 |
| 5 | Voice | G8 |
| 6 | Memory + Tools + AI Router + RAG gate | G8 |
| 7 | Dataset strategy + Fine-tuning | Colab/Kaggle |
| 8 | Camera + Computer Vision | camera G8 |
| 9 | ESP32 + liên kết phone↔ESP32 | ESP32 |
| 10 | Motor + Safety | motor, driver, pin |
| 11 | Navigation | sensor |
| 12 | Dashboard + DevOps + Model update | – |
| 13 | Tích hợp cuối + E2E + Reliability | tất cả |
| 14 | Portfolio, tài liệu, demo | – |

Mốc gợi ý: **dừng sau Phase 7** vẫn là project đủ mạnh để xin việc (app + backend + model riêng + số liệu). Phase 8 trở đi là phần robot vật lý.

Mỗi phase, khi bắt đầu, phải trình đủ 16 mục: mục tiêu, lý do, input, output, kiến trúc, công nghệ, cấu trúc thư mục, luồng dữ liệu, kế hoạch triển khai, code, test, benchmark (nếu phù hợp), security, safety, troubleshooting, Definition of Done. Các issue bên dưới là khung sườn, sẽ tách thành issue con khi tới phase.

### 6.0 Phase 0 – Architecture + Research (đang làm)

### [Phase 0] 0.1 Đồng bộ baseline và dựng khung tài liệu

#### Mục tiêu
Đưa baseline backend về đúng spec ROBOTV1 và dựng khung tài liệu/ADR.

#### Việc đã làm
- [x] Đổi `reply` → `response`; `open_app` dùng `params.package`, whitelist theo package (ADR-0003)
- [x] Cấu trúc repo theo mục 61 (`ai/`, `firmware/esp32/`, `dashboard/`, `scripts/`, `tests/`, `docker/`)
- [x] `docs/ARCHITECTURE.md`, `docs/ROADMAP.md`, `docs/RESEARCH_BACKLOG.md`
- [x] ADR-0001 (validator trên Android + JSON Schema chung), ADR-0002 (spike LLM trước Voice), ADR-0003
- [x] 12 test backend qua

#### Definition of Done
- [x] `python -m pytest -q` → `12 passed`
- [ ] Code + docs đã được đưa lên GitHub (đóng issue này sau khi push)

### [Phase 0] 0.2 Research nhóm A: engine LLM, tăng tốc, model, quantization

#### Mục tiêu
Trả lời R1–R4 trong `docs/RESEARCH_BACKLOG.md` để chốt **danh sách ứng viên** cho spike ở Phase 4 (chưa chốt model cuối).

#### Việc cần làm
- [ ] R1: So sánh engine (llama.cpp build NDK chính thức vs wrapper, MLC, ONNX Runtime GenAI, MediaPipe/LiteRT-LM, ExecuTorch): Android + Snapdragon 855, ARM64, GGUF, structured output/grammar, license, mức độ bảo trì
- [ ] R2: GPU (Adreno 640) / DSP có dùng được cho LLM trên 855 không; nếu không → CPU-only
- [ ] R3: Model card hiện hành của Qwen3 0.6B/1.7B + 1–2 model nhỏ khác: license, tiếng Việt, tool calling, dung lượng GGUF
- [ ] R4: Quantization Q4_K_M / Q5_K_M / Q6_K / Q8_0: đánh đổi chất lượng, RAM, tốc độ
- [ ] Viết `docs/research/A-llm.md`: mỗi kết luận có link nguồn + ngày; nguồn cộng đồng ghi rõ
- [ ] ADR (Proposed): engine ứng viên cho Phase 4; ADR (Proposed): shortlist model

#### Definition of Done
- [ ] Không còn mục nào ghi CHƯA BIẾT mà chưa có nguồn hoặc lý do
- [ ] Có shortlist engine + model + quantization để benchmark ở Phase 4
- [ ] R1–R4 chuyển `DONE` trong `RESEARCH_BACKLOG.md`

#### Ràng buộc
Không tuyên bố model nào "tốt nhất" khi chưa benchmark trên G8.

### [Phase 0] 0.3 Research nhóm B: STT, TTS, wake word tiếng Việt

#### Mục tiêu
Trả lời R5–R7 để có danh sách ứng viên voice cho Phase 5.

#### Việc cần làm
- [ ] R5: STT tiếng Việt: Android SpeechRecognizer (có offline không), sherpa-onnx, whisper.cpp, cloud — model Việt có sẵn, dung lượng, độ trễ
- [ ] R6: TTS tiếng Việt: giọng Việt của Android TTS, Piper/sherpa-onnx, cloud
- [ ] R7: Wake word cho "Hey Robot": openWakeWord, Porcupine, sherpa-onnx KWS — khả năng dùng, tiêu thụ pin, license
- [ ] Viết `docs/research/B-voice.md` (nguồn + ngày)
- [ ] ADR (Proposed): ứng viên voice cho Phase 5

#### Definition of Done
- [ ] Mỗi khối (STT/TTS/wake word) có ≥2 ứng viên và kế hoạch benchmark
- [ ] R5–R7 chuyển `DONE`

### [Phase 0] 0.4 Research nhóm C: giới hạn Android và liên kết phone-ESP32

#### Mục tiêu
Trả lời R8–R9; đủ thông tin để chọn framework Android và định hướng liên kết với ESP32.

#### Việc cần làm
- [ ] R8: Giới hạn Android theo tài liệu chính thức: mic nền (foreground service), báo thức (`AlarmClock` intent), âm lượng, WiFi (hạn chế từ Android 10), mở app, quyền cần khai báo
- [ ] So sánh Kotlin native vs Flutter theo tiêu chí mục 20 (local AI, audio, camera, service, JNI/NDK, portfolio)
- [ ] R9: USB host + sạc cùng lúc, thư viện serial cho Android, so với BLE/WiFi (độ trễ, độ tin cậy, pin, độ phức tạp)
- [ ] Viết `docs/research/C-android-hardware.md` (nguồn + ngày)
- [ ] ADR (Proposed): framework Android; ghi nhận hướng liên kết ESP32 (quyết định cuối ở Phase 9)

#### Definition of Done
- [ ] Danh sách action Android nào làm được / không làm được / cần fallback
- [ ] Có ADR Proposed cho framework Android
- [ ] R8–R9 chuyển `DONE`

### [Phase 0] 0.5 Chốt Phase 0

#### Việc cần làm
- [ ] Cập nhật `docs/RESEARCH_BACKLOG.md` (R1–R9 = DONE, R10 giữ OPEN cho Phase 8)
- [ ] Chuyển các ADR Proposed thành Accepted/Rejected sau khi thống nhất
- [ ] Cập nhật mục "Trạng thái các thành phần" trong `docs/ARCHITECTURE.md`
- [ ] Rà lại `docs/ROADMAP.md`
- [ ] Tạo tag `v0.1.0-phase0` và đóng milestone Phase 0

#### Definition of Done
- [ ] Không còn quyết định lớn nào ở trạng thái mơ hồ trước khi vào Phase 1

### 6.1 Các phase còn lại

### [Phase 1] Backend AI Gateway + Model abstraction

#### Mục tiêu
Nâng baseline thành AI Gateway: đổi model chỉ bằng cấu hình (`AIProvider`: Local/Laptop/Cloud/Mock, ví dụ Ollama, llama.cpp, Gemini), prompt versioned, personality tách khỏi model.

#### Vì sao cần phase này
Model thay đổi rất nhanh; app và dataset không được khóa vào một model.

#### Phụ thuộc
Phase 0

#### Việc cần làm
- [ ] Interface `AIProvider` + provider Ollama / Gemini / Mock (thêm provider mới không sửa service)
- [ ] Prompt đưa vào `ai/prompts/` có version; personality trong `robot_personality.yaml`
- [ ] Logging có cấu trúc (timestamp, request, model, provider, latency, response, action, validation, lỗi) — không log dữ liệu nhạy cảm
- [ ] Dockerfile + docker-compose; Swagger có ví dụ request/response/lỗi
- [ ] CI GitHub Actions: lint, unit test, API test, docker build
- [ ] Repository/service abstraction cho lưu hội thoại (bật/tắt persistence bằng cấu hình)

#### Definition of Done
- [ ] Đổi provider chỉ bằng `.env`, không sửa code
- [ ] CI xanh
- [ ] `docker compose up` chạy được backend
- [ ] Có test cho từng provider (mock cho provider ngoài)

### [Phase 2] Agent + Action System + Evaluation v0

#### Mục tiêu
Action registry có schema/permission/timeout/executor/log; safety pipeline; JSON Schema dùng chung (ADR-0001); bộ đánh giá đầu tiên và **golden test set** độc lập model.

#### Vì sao cần phase này
LLM chỉ đề xuất; phải có lớp kiểm tra trước khi thực thi. Eval phải có từ sớm để mọi thay đổi sau này đo được.

#### Phụ thuộc
Phase 1

#### Việc cần làm
- [ ] JSON Schema chung cho action; sinh model Pydantic (kế hoạch sinh Kotlin cho Phase 3)
- [ ] Action registry: get_time, get_battery, set_alarm, open_app, set_volume (+ khung cho move/stop, chưa nối phần cứng)
- [ ] Xử lý JSON sai: validate → repair/retry → không execute → fallback "Xin lỗi, mình chưa hiểu yêu cầu đó."
- [ ] Test an toàn (mục 58): action hợp lệ/không hợp lệ, thiếu/sai kiểu tham số, action lạ, ngoài phạm vi, không có quyền, JSON lỗi, tool bịa, timeout
- [ ] Golden test set ~100–200 mẫu (kiểm tay, KHÔNG dùng để train) + `ai/datasets/README.md` (nguồn, cách tạo)
- [ ] Eval harness v0: JSON validity, action accuracy, intent accuracy, tool selection accuracy; kết quả lưu ở `docs/benchmarks/`

#### Definition of Done
- [ ] Action ngoài whitelist luôn bị từ chối (có test)
- [ ] Eval chạy bằng 1 lệnh, cho ra bảng số liệu
- [ ] Golden set tách khỏi tập train

### [Phase 3] Android app (Robot UI + Action Executor)

#### Mục tiêu
App Android: mặt robot, state machine, Action Executor qua API chính thức, gọi backend, nhập bằng text. Có sẵn interface `AIProvider` phía Android để Phase 4 gắn model local.

#### Vì sao cần phase này
App là giao diện chính; validator/executor phải nằm trên Android để chạy offline (ADR-0001).

#### Phụ thuộc
Phase 2, research nhóm C

#### Việc cần làm
- [ ] Project Android theo framework chốt ở Phase 0 (nghiêng Kotlin + Compose)
- [ ] State machine IDLE → WAKE → LISTENING → PROCESSING → SPEAKING (+ ERROR) và animation mặt robot
- [ ] Validator + Executor: get_time, get_battery, set_alarm, open_app (whitelist package), set_volume
- [ ] Client REST tới backend; MockProvider để chạy không cần mạng
- [ ] Unit test cho validator/executor; test thủ công trên G8

#### Definition of Done
- [ ] Nhập text → robot phản hồi và thực thi action trên G8
- [ ] Action ngoài whitelist bị từ chối trên app
- [ ] Có test cho validator

### [Phase 4] Spike LLM trên G8 + Model Manager v0

#### Mục tiêu
Chạy LLM local trên G8, benchmark thật, đưa ra quyết định go/no-go cho local-first (ADR-0002).

#### Vì sao cần phase này
Toàn bộ luận điểm local-first phụ thuộc vào việc này; kết quả quyết định ngân sách cho Voice.

#### Phụ thuộc
Phase 3, research nhóm A

#### Việc cần làm
- [ ] `LocalProvider` phía Android tích hợp engine đã chọn (interface giống provider backend)
- [ ] Model Manager v0: metadata, version, checksum, đường dẫn lưu, nạp/gỡ model
- [ ] Benchmark ≥3 model × quantization × context bằng CÙNG bộ prompt
- [ ] Đo: latency, tokens/sec, RAM, CPU, nhiệt (nếu có API), pin, thời gian nạp model, JSON accuracy, crash rate; thêm chạy liên tục ≥10 phút để thấy throttling
- [ ] Báo cáo `docs/benchmarks/phase4-llm-g8.md` + ADR go/no-go

#### Definition of Done
- [ ] Có số liệu thật cho từng cấu hình
- [ ] Có quyết định: local đủ dùng / chỉ dùng cho lệnh / cần laptop-cloud
- [ ] Không có tuyên bố nào chưa có số liệu

### [Phase 5] Voice: wake word, STT, TTS tiếng Việt

#### Mục tiêu
Luồng microphone → wake word → ghi âm → STT → AI → TTS → loa, với ngân sách tài nguyên đã biết từ Phase 4.

#### Vì sao cần phase này
Robot phải nghe và nói tiếng Việt; wake word phải nhẹ để không hao pin.

#### Phụ thuộc
Phase 4, research nhóm B

#### Việc cần làm
- [ ] Wake word ("Hey Robot") + VAD
- [ ] STT tiếng Việt: benchmark WER, latency, RAM cho các ứng viên
- [ ] TTS tiếng Việt: chất lượng, latency, dung lượng
- [ ] Foreground service cho mic nền theo quy định Android
- [ ] Chế độ hybrid: local ưu tiên, cloud fallback
- [ ] Đo pin khi wake word chạy nền

#### Definition of Done
- [ ] Nói "Hey Robot, mấy giờ rồi?" → robot trả lời bằng giọng nói
- [ ] Có bảng benchmark từng khối
- [ ] Kiểm thử offline cho phần voice đã chọn local

### [Phase 6] Memory + Tools + AI Router (hybrid/offline) + RAG gate

#### Mục tiêu
Bộ nhớ ngắn/dài hạn, tool system xác định, **AI Router** chọn Local/Laptop/Cloud, chế độ offline cơ bản; đánh giá RAG chỉ khi có bằng chứng cần.

#### Vì sao cần phase này
Đây là chỗ hiện thực hóa local-first + hybrid: việc xác định (giờ, âm lượng) không hỏi LLM; dữ liệu cá nhân không ra cloud.

#### Phụ thuộc
Phase 4, Phase 5

#### Việc cần làm
- [ ] Memory: lưu / truy xuất / cập nhật / xóa, ranh giới quyền riêng tư, chọn ngữ cảnh đưa vào prompt
- [ ] Tools: get_time (đồng hồ hệ thống), get_weather, get_location; luồng Tool selection → validator → execute → kết quả → AI trả lời
- [ ] AI Router v0 (luật): phân loại yêu cầu theo loại tác vụ, trạng thái mạng, độ phức tạp, yêu cầu riêng tư
- [ ] Phân loại dữ liệu: LOCAL ONLY / OPTIONAL CLOUD / PUBLIC-SAFE
- [ ] Offline mode cơ bản: wake word + STT/TTS local + LLM local + memory + Android actions (kiểm tra bằng chế độ máy bay)
- [ ] RAG gate: chỉ làm nếu benchmark chứng minh cần; kết thúc bằng ADR "cần / không cần RAG" kèm số liệu

#### Definition of Done
- [ ] Chế độ máy bay: các chức năng cơ bản vẫn chạy
- [ ] Dữ liệu LOCAL ONLY không bao giờ gửi cloud (có test)
- [ ] Có ADR về RAG với bằng chứng

### [Phase 7] Dataset strategy + Fine-tuning

#### Mục tiêu
Chiến lược dataset (có sẵn → synthetic → augmentation → kiểm tay khi thật sự cần) rồi fine-tune LoRA/QLoRA và chứng minh bằng số liệu base vs fine-tuned.

#### Vì sao cần phase này
Fine-tune chỉ có ý nghĩa khi đã có baseline và golden set (Phase 2, Phase 4).

#### Phụ thuộc
Phase 2, Phase 4

#### Việc cần làm
- [ ] Khảo sát dataset có sẵn phù hợp; sinh dữ liệu synthetic + augmentation tiếng Việt; lọc và kiểm chất lượng
- [ ] Chia train/val/test; golden set giữ nguyên, không rò rỉ
- [ ] Data card: nguồn, license, cách tạo
- [ ] Train LoRA/QLoRA trên Colab/Kaggle/cloud — ghi model, dataset, VRAM, thời gian, chi phí ước tính, cách tái lập
- [ ] So sánh base vs fine-tuned trên toàn bộ metric; lượng tử hóa và chạy thử trên G8

#### Definition of Done
- [ ] Báo cáo base vs fine-tuned có số liệu
- [ ] Tái lập được từ script + config
- [ ] Chi phí và thời gian được ghi lại

### [Phase 8] Camera + Computer Vision

#### Mục tiêu
Camera → Vision → thông tin cảnh cho AI: object detection và face recognition mức prototype, tách khỏi LLM.

#### Vì sao cần phase này
Robot cần "nhìn"; vision và language model có trách nhiệm riêng.

#### Phụ thuộc
Phase 3 (Phase 6 cho tích hợp)

#### Việc cần làm
- [ ] Pipeline camera trên G8
- [ ] Object detection: chọn model sau khi kiểm tra license và tốc độ
- [ ] Face detection + recognition + identity store
- [ ] Quyền riêng tư: dữ liệu khuôn mặt chỉ lưu local, có xóa/quản lý
- [ ] Tích hợp kết quả vision vào ngữ cảnh cho agent
- [ ] Benchmark FPS, RAM, pin

#### Definition of Done
- [ ] Nhận diện được vật thể thường gặp trong nhà
- [ ] Nhận diện khuôn mặt đã đăng ký (prototype)
- [ ] Có tài liệu quyền riêng tư

### [Phase 9] ESP32 + liên kết phone-ESP32

#### Mục tiêu
Firmware ESP32, giao thức lệnh giữa điện thoại và ESP32, telemetry, watchdog. Chốt USB/BLE/WiFi bằng ADR.

#### Vì sao cần phase này
ESP32 là "thân": motor, sensor, an toàn mức thấp.

#### Phụ thuộc
Phase 3, research nhóm C

#### Việc cần làm
- [ ] Dự án firmware (PlatformIO/Arduino hoặc ESP-IDF — quyết định bằng ADR)
- [ ] Giao thức lệnh: direction, speed, duration, timeout; checksum/ack
- [ ] Liên kết phone↔ESP32 (USB / BLE / WiFi): so sánh và ADR
- [ ] Telemetry: trạng thái, sensor
- [ ] Watchdog: mất liên lạc → dừng ngay
- [ ] Test giao tiếp ESP32 và test mất kết nối/kết nối lại (crash/reconnect)

#### Definition of Done
- [ ] Điện thoại gửi lệnh, ESP32 nhận và phản hồi
- [ ] Ngắt cáp/tắt link → ESP32 báo mất kết nối và dừng
- [ ] Có test giao tiếp

### [Phase 10] Motor + Safety

#### Mục tiêu
Robot di chuyển an toàn: giới hạn tốc độ/thời gian, timeout, obstacle safety, e-stop vật lý.

#### Vì sao cần phase này
An toàn không được phụ thuộc AI.

#### Phụ thuộc
Phase 9

#### Việc cần làm
- [ ] Thiết kế nguồn và e-stop vật lý cắt nguồn/enable motor, ưu tiên cao hơn mọi lệnh phần mềm
- [ ] Mọi lệnh motor có direction, speed limit, duration, timeout
- [ ] Dừng khi có vật cản; dừng khi mất kết nối
- [ ] Thử nghiệm ban đầu: nhấc bánh khỏi mặt đất
- [ ] Safety test phần cứng: e-stop, mất link, vật cản, lệnh vượt giới hạn
- [ ] LLM không có đường điều khiển motor trực tiếp (có test)

#### Definition of Done
- [ ] Nhấn e-stop → motor dừng ngay dù phần mềm đang gửi lệnh
- [ ] Lệnh vượt giới hạn bị từ chối
- [ ] Mất liên lạc → robot dừng

### [Phase 11] Navigation

#### Mục tiêu
Né vật cản, đi theo người: Camera → Person detection → Tracking → Navigation → Motor controller.

#### Vì sao cần phase này
Robot di chuyển quanh nhà; LLM chỉ ở tầng suy luận cấp cao.

#### Phụ thuộc
Phase 8, Phase 10

#### Việc cần làm
- [ ] Né vật cản bằng sensor
- [ ] Person following: detection → tracking → controller
- [ ] Nghiên cứu (chưa cam kết) localization/SLAM ở mức phù hợp
- [ ] Giới hạn an toàn khi tự di chuyển
- [ ] Test trong môi trường kiểm soát

#### Definition of Done
- [ ] Robot đi theo người trong khoảng cách an toàn và dừng khi mất người/có vật cản
- [ ] Không có đường LLM → motor

### [Phase 12] Dashboard + DevOps hoàn thiện + Model update

#### Mục tiêu
Dashboard theo dõi robot; CI/CD đầy đủ; cơ chế cập nhật model an toàn.

#### Vì sao cần phase này
Quan sát được hệ thống và phát hành ổn định.

#### Phụ thuộc
Phase 6 trở đi

#### Việc cần làm
- [ ] Dashboard API + UI: online/offline, pin, CPU/RAM, model, latency, hội thoại, action log, lỗi, trạng thái ESP32/sensor, telemetry
- [ ] CI: thêm build Android (khi ổn định), API tests, Docker build
- [ ] Model update: version, checksum, rollback, kiểm tra dung lượng và tương thích
- [ ] Metrics và cảnh báo cơ bản

#### Definition of Done
- [ ] Xem được trạng thái robot theo thời gian thực
- [ ] Cập nhật model có rollback
- [ ] CI kiểm tra đủ các tầng

### [Phase 13] Tích hợp cuối + End-to-End + Reliability

#### Mục tiêu
Toàn hệ thống chạy liền mạch; kiểm thử end-to-end và độ tin cậy.

#### Vì sao cần phase này
Portfolio production-ready cần chứng minh hệ thống chạy ổn định, không chỉ từng khối.

#### Phụ thuộc
Phase 1–12

#### Việc cần làm
- [ ] E2E: giọng nói → STT → AI → validator → executor → hành động thật (Android và ESP32)
- [ ] Nghiệm thu offline: chế độ máy bay + các chức năng cơ bản
- [ ] Crash/reconnect: mất mạng, mất ESP32, kill app, hết pin thấp
- [ ] Soak test chạy dài (ổn định bộ nhớ, nhiệt, crash rate)
- [ ] Chạy lại toàn bộ eval/benchmark trên bản tích hợp
- [ ] Tài liệu troubleshooting

#### Definition of Done
- [ ] Có báo cáo E2E + reliability
- [ ] Crash rate và các metric được ghi lại
- [ ] Kịch bản demo chạy lặp lại được

### [Phase 14] Portfolio, tài liệu và demo

#### Mục tiêu
Đóng gói project để xin việc: README, kiến trúc, benchmark, báo cáo kỹ thuật, video demo.

#### Vì sao cần phase này
Chứng minh kỹ năng bằng bằng chứng, không chỉ bằng lời.

#### Phụ thuộc
Phase 13

#### Việc cần làm
- [ ] README + sơ đồ kiến trúc
- [ ] Setup Guide, Development Guide, AI/Android/ESP32 Guide, API docs, Model docs, Testing, Troubleshooting, Roadmap
- [ ] Tổng hợp benchmark và báo cáo kỹ thuật
- [ ] Video demo 2–3 phút
- [ ] Dọn repo: không lộ key/dữ liệu riêng tư

#### Definition of Done
- [ ] Người lạ đọc README làm theo dựng được backend
- [ ] Có video demo và báo cáo benchmark

---

## 7. Mảng xuyên suốt

### [Cross-cutting] Hệ thống Evaluation xuyên suốt

#### Mục tiêu
Một hệ thống đánh giá dùng lại cho mọi phase, không chỉ benchmark một lần. Bộ test và dataset đánh giá **độc lập với model**.

#### Metric
Action accuracy • JSON validity • Intent accuracy • Tool selection accuracy • Response latency • Tokens/sec • RAM • CPU • Battery • Model load time • Crash rate (Phase 5+ thêm: WER, latency STT/TTS; Phase 8: FPS, độ chính xác nhận diện)

#### Nguyên tắc
- Cùng một bộ prompt cho mọi model; ghi model, quantization, context, thiết bị, ngày
- Kết quả lưu ở `docs/benchmarks/` (có thể tái lập bằng script)
- Không tuyên bố model tốt hơn khi chưa có số liệu

#### Theo phase
- [ ] Phase 2: harness v0 + golden set
- [ ] Phase 4: thêm metric tài nguyên trên G8 (RAM, CPU, pin, nạp model, crash)
- [ ] Phase 5: thêm metric voice
- [ ] Phase 7: so sánh base vs fine-tuned
- [ ] Phase 8: metric vision
- [ ] Phase 13: chạy lại toàn bộ trên bản tích hợp

### [Cross-cutting] Test và Reliability xuyên suốt

#### Mục tiêu
Mỗi phase có test; Phase 13 tổng hợp end-to-end và reliability.

#### Loại test và phase bắt đầu
| Loại | Bắt đầu |
|---|---|
| Unit test | Phase 1 |
| API test / Integration test | Phase 1 |
| Agent/Action test (mục 58) | Phase 2 |
| Android test | Phase 3 |
| Model/Eval test | Phase 2, mở rộng Phase 4 |
| ESP32 communication test | Phase 9 |
| Safety test phần cứng (e-stop, mất link, vật cản) | Phase 10 |
| End-to-End | Phase 13 |
| Crash/reconnect/soak | Phase 9 (link), Phase 13 (toàn hệ thống) |

#### Việc cần làm
- [ ] Quy ước cấu trúc test (`backend/tests`, `tests/` cho tích hợp, test Android)
- [ ] Chạy test trong CI ở mọi PR
- [ ] Tài liệu `docs/TESTING.md`

**Định nghĩa hoàn thành toàn project** (Definition of Done tổng): local model chạy trên G8; chế độ trò chuyện + agent + structured action; provider abstraction; Android có UI robot, mic, STT, TTS, local inference, action execution; backend có API, validation, logging, database, Docker, Swagger; memory ngắn/dài hạn; camera + object detection + face recognition (prototype); ESP32 + giao tiếp + motor + e-stop + watchdog + obstacle safety; GitHub + CI/CD + test + tài liệu; benchmark đủ metric; README, sơ đồ, video demo, báo cáo kỹ thuật.

---

## 8. Việc cần chuẩn bị

### 8.1 Làm ngay (thứ tự)
1. Đẩy repo lên GitHub (public), chạy `python scripts/create_issues.py --dry-run` rồi chạy thật (cần `gh auth login`).
2. Làm lần lượt issue 0.2 (nhóm A: LLM) → 0.3 (nhóm B: voice) → 0.4 (nhóm C: Android + phần cứng) → 0.5 (chốt Phase 0, tag `v0.1.0-phase0`).
3. Sau Phase 0: Phase 1 (nâng cấp backend).

### 8.2 Môi trường trên laptop (Windows/Linux)
| Công cụ | Khi nào cần | Ghi chú |
|---|---|---|
| VS Code + extension (Python, Pylance, Ruff, GitHub PR, Markdown, EditorConfig) | Ngay | Đã có `.vscode/extensions.json` |
| Python 3.11+ | Ngay | Backend đã chạy test trên Python 3.12 |
| Git + GitHub CLI (`gh`) | Ngay | `gh auth login` |
| Ollama + model nhỏ (vd `qwen3:1.7b`) | Khi test provider Ollama | Kiểm tra tên model hiện hành trên thư viện Ollama |
| Docker Desktop | Phase 1 | |
| Android Studio + NDK + CMake | Phase 3 (NDK: Phase 4) | |
| `adb` + `scrcpy` | Phase 3 | Điều khiển/debug G8 từ laptop |
| PlatformIO (hoặc ESP-IDF) | Phase 9 | Chốt bằng ADR |

### 8.3 Tài khoản và khóa
- GitHub (repo public).
- Google AI Studio: API key Gemini (chỉ để trong `backend/.env`, không commit). Kiểm tra hạn mức và điều khoản bậc miễn phí hiện hành; không gửi dữ liệu cá nhân lên bậc miễn phí.
- Hugging Face: tải model.
- Kaggle / Google Colab: cho Phase 7 (kiểm tra hạn mức GPU hiện hành, ghi chi phí nếu dùng bản trả phí).

### 8.4 Điện thoại G8
- Bật Tùy chọn nhà phát triển và Gỡ lỗi USB; cáp USB-C truyền dữ liệu; kiểm tra nhận USB OTG.
- Ghi lại số liệu thật (RAM, Android, nhiệt độ, pin) trong benchmark Phase 4.
- Kiểm tra tình trạng pin (benchmark liên tục sẽ làm nóng/hao pin).

### 8.5 Dữ liệu
- Bộ prompt benchmark cố định (dùng cho mọi model) và **golden test set** ~100–200 mẫu kiểm tay, **không dùng để train** (bắt đầu Phase 2).
- Chiến lược dataset train ở Phase 7: có sẵn → synthetic → augmentation → kiểm tay khi thật sự cần (người dùng không muốn nhập tay hàng nghìn mẫu, chấp nhận vài nghìn).

### 8.6 Phần cứng robot (chưa mua gì; chọn cụ thể cùng nhau khi tới phase)
| Khi nào | Nhóm linh kiện |
|---|---|
| Phase 9 | Board ESP32, cáp USB dữ liệu |
| Phase 10 | Motor + driver + khung robot, pin và mạch nguồn, **nút e-stop vật lý** cắt nguồn/enable motor, giá đỡ điện thoại |
| Phase 11 | Cảm biến khoảng cách; có thể thêm encoder/IMU |
| Tùy chọn | Hub USB có cấp nguồn (vừa sạc vừa OTG), mic/loa rời, servo, LED, LiDAR |

Camera dùng camera của G8 trước. Không mua phần cứng trước khi phase tương ứng bắt đầu và người dùng đồng ý.

---

## 9. Rủi ro chính

| Rủi ro | Giảm thiểu |
|---|---|
| Snapdragon 855 (2019) chậm hơn mục tiêu 3–5 giây; RAM thực có thể khác kỳ vọng | Spike sớm ở Phase 4; router; intent nhẹ cho lệnh xác định; laptop/cloud fallback |
| Nóng máy, throttling, hao pin | Benchmark liên tục ≥10 phút; giới hạn thread; nạp model theo nhu cầu |
| STT/TTS/wake word tiếng Việt local kém | Benchmark từng khối; hybrid với cloud |
| Hạn chế Android (CẦN KIỂM TRA tài liệu): bật/tắt WiFi bằng code, mic nền cần foreground service | Dùng API chính thức hoặc mở panel cài đặt; thiết kế fallback |
| LLM bịa action/JSON sai | Schema + whitelist + không execute nếu sai + test an toàn |
| Mất liên lạc phone↔ESP32 | Watchdog + e-stop vật lý |
| USB: sạc + OTG cùng lúc, cáp lỏng do rung | Kiểm tra sớm; phương án BLE/WiFi |
| Nhận diện sai, dữ liệu khuôn mặt | Vision tách khỏi LLM; lưu local, có xóa |
| Phạm vi quá lớn | Lát cắt dọc, mỗi phase chạy được và có demo; có mốc dừng sau Phase 7 |

### Research backlog (Phase 0)
| # | Chủ đề | Câu hỏi | Nhóm |
|---|---|---|---|
| R1 | Engine LLM Android | llama.cpp (build NDK chính thức vs wrapper) vs MLC, ONNX Runtime GenAI, MediaPipe/LiteRT-LM, ExecuTorch: Snapdragon 855, ARM64, GGUF, structured output/grammar, license, bảo trì | A |
| R2 | Tăng tốc phần cứng | GPU Adreno 640 / DSP có dùng được cho LLM không, hay chỉ CPU | A |
| R3 | Model benchmark | Model card hiện hành Qwen3 0.6B/1.7B + 1–2 model khác: license, tiếng Việt, tool calling | A |
| R4 | Quantization | Q4_K_M/Q5_K_M/Q6_K/Q8_0: đánh đổi cho điện thoại | A |
| R5 | STT tiếng Việt | SpeechRecognizer (offline?), sherpa-onnx, whisper.cpp, cloud | B |
| R6 | TTS tiếng Việt | Android TTS, Piper/sherpa-onnx, cloud | B |
| R7 | Wake word | openWakeWord, Porcupine, sherpa-onnx KWS cho "Hey Robot" | B |
| R8 | Giới hạn Android | Mic nền, `AlarmClock` intent, âm lượng, WiFi, quyền | C |
| R9 | USB phone↔ESP32 | USB host + sạc, thư viện serial, so với BLE/WiFi | C |
| R10 | Vision | Detector, face recognition, license (AGPL) | Phase 8 |

---

## 10. Quy trình làm việc cho mỗi task

1. Chọn **một** issue (Phase hiện tại, theo thứ tự).
2. Agent tóm tắt kế hoạch và các điểm chưa chắc → người dùng xác nhận.
3. Tạo nhánh (vd `feat/phase1-aiprovider`), làm nhỏ, commit theo quy ước.
4. Chạy test (`cd backend && python -m pytest -q`), thêm test cho phần mới.
5. Cập nhật docs/ADR nếu có quyết định; cập nhật checklist trong issue.
6. Báo cáo: đã làm gì, bằng chứng (test/benchmark/log), việc còn lại, điều chưa chắc (ghi nhãn CHƯA BIẾT / ASSUMPTION).
7. Người dùng chạy thử và báo kết quả → mới sang task kế tiếp.

**Ví dụ giao việc cho agent**
- "Làm issue 0.2. Chỉ dùng tài liệu chính thức và model card hiện hành. Ghi kết quả vào `docs/research/A-llm.md` với link nguồn và ngày truy cập, tạo ADR ở trạng thái Proposed. Không viết code ứng dụng."
- "Làm Phase 1.1: định nghĩa `AIProvider`, refactor provider Ollama/Gemini/Mock để service không phụ thuộc lớp cụ thể, thêm test. Không đổi hợp đồng dữ liệu."

---

## 11. Những điều KHÔNG làm

- Train LLM từ đầu (không có GPU, không cần thiết).
- SLAM/navigation phức tạp ở giai đoạn đầu.
- Vector DB/RAG, WebSocket, tài khoản người dùng chỉ để "trông chuyên nghiệp".
- Launcher/kiosk mặc định trước khi hệ thống ổn định.
- Hỗ trợ đa ngôn ngữ ngay (chỉ giữ kiến trúc mở rộng được; ưu tiên tiếng Việt).
- Để LLM điều khiển motor hay bất kỳ phần cứng nào trực tiếp.
