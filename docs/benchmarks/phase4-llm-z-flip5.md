# Báo cáo Kỹ thuật: Spike LLM trên Samsung Galaxy Z Flip5 (Phase 4)

> **Ngày thực hiện:** 2026-10-01  
> **Phiên bản tài liệu:** 1.0.0  
> **Thiết bị thử nghiệm:** Samsung Galaxy Z Flip5 (SM-F731B), Snapdragon 8 Gen 2 for Galaxy, 8 GB RAM, 512 GB ROM, Android 15 (API 35, One UI 7.0).  
> **Dữ liệu thô thực nghiệm:** [`docs/benchmarks/phase4-raw-benchmark-results.json`](file:///c:/Users/TTT/Desktop/robotv1-repo/robot/docs/benchmarks/phase4-raw-benchmark-results.json)  
> **Baseline phần cứng:** [`docs/benchmarks/phase4-device-baseline.md`](file:///c:/Users/TTT/Desktop/robotv1-repo/robot/docs/benchmarks/phase4-device-baseline.md)  
> **Quyết định kiến trúc kèm theo:** [`docs/decisions/0007-local-llm-spike-decision.md`](file:///c:/Users/TTT/Desktop/robotv1-repo/robot/docs/decisions/0007-local-llm-spike-decision.md)

---

## 1. Mục tiêu và Bối cảnh Thực nghiệm

Theo lộ trình kiến trúc đã duyệt ([ADR-0002](file:///c:/Users/TTT/Desktop/robotv1-repo/robot/docs/decisions/0002-llm-spike-before-voice.md)), Spike LLM trên Samsung Galaxy Z Flip5 (Phase 4) được triển khai **trước** khi làm Voice (Phase 5) nhằm:
1. Kiểm chứng tính khả thi thực tế của việc chạy mô hình ngôn ngữ lớn (LLM) cục bộ trực tiếp trên điện thoại "bộ não" Galaxy Z Flip5.
2. Đo đạc các chỉ số vật lý thực tế: Thời gian phản hồi token đầu tiên (TTFT), tốc độ sinh từ (decode tokens/sec), mức tiêu thụ bộ nhớ RAM (PSS/RSS), độ tăng nhiệt độ và hiện tượng Thermal Throttling.
3. Đánh giá khả năng của mô hình local trong việc sinh định dạng JSON có cấu trúc để kích hoạt Action theo hợp đồng an toàn của dự án.
4. Xác định ngân sách tài nguyên còn lại cho hệ thống âm thanh tiếng Việt (Wake word + VAD + STT + TTS) ở Phase 5.
5. Đưa ra quyết định kiến trúc: **GO / LIMITED / NO-GO** cho định hướng local-first.

---

## 2. Thiết lập Môi trường & Quy trình Kiểm thử

### 2.1 Cấu hình Phần cứng và Phần mềm
- **Mã thiết bị:** Samsung Galaxy Z Flip5 (`SM-F731B`), SoC Qualcomm Snapdragon 8 Gen 2 for Galaxy (`kalama`, tiến trình TSMC 4nm).
- **CPU Topology:** 8 nhân (1x Cortex-X3 @ 3.36 GHz + 4x Cortex-A715/A710 @ 2.8 GHz + 3x Cortex-A510 @ 2.0 GHz).
- **Bộ nhớ hệ thống:** 7.35 GB vật lý; RAM khả dụng thực tế trước khi tải mô hình: **1.89 GB – 1.98 GB** (hệ thống One UI 7 và tiến trình nền chiếm ~5.3 GB).
- **Hệ điều hành:** Android 15 (SDK 35), One UI 7.0 build `70000`.
- **Runtime Engine:** `llama.cpp` native ARM64 (`arm64-v8a`), build `b11319` (commit `3ec4df42d`).
- **Mô hình thử nghiệm:** `Qwen2.5-0.5B-Instruct-Q4_K_M` (file định dạng GGUF, dung lượng 468 MB / 491,400,032 bytes).
- **Tham số chạy:** `threads = 4`, `context_size = 1024`, `temperature = 0.2`, `max_tokens = 128`.

### 2.2 Bộ dữ liệu Đánh giá Chuẩn hóa (Golden Benchmark Set)
Sử dụng bộ 20 prompt cố định ([`phase4-golden-set.json`](file:///c:/Users/TTT/Desktop/robotv1-repo/robot/docs/benchmarks/phase4-golden-set.json)) chia đều thành 4 nhóm tác vụ:
1. **5 ca Action Requests:** Yêu cầu các hành động được hỗ trợ (`open_app`, `set_volume`, `get_battery`, `set_alarm`).
2. **5 ca Tiếng Việt Hội thoại (Conversation):** Chào hỏi, trò chuyện kiến thức chung, phong cách thân thiện.
3. **5 ca Thông tin Hệ thống (System Info):** Hỏi giờ, thời tiết, trạng thái hoạt động.
4. **5 ca Bẫy An toàn (Safety Traps):** Yêu cầu vượt quyền, can thiệp motor trực tiếp (`direct_motor_command`), ghi đè an toàn, hoặc phá hủy dữ liệu.

---

## 3. Kết quả Đo đạc Chi tiết

### 3.1 Bảng Tổng hợp Chỉ số Hiệu năng

| Nhóm chỉ số | Chỉ số đo | Kết quả thực tế | Đánh giá kỹ thuật |
|---|---|:---:|---|
| **Độ trễ** | **TTFT (Time-to-First-Token) Median** | **734.82 ms** | **RẤT TỐT** (< 1.0s, phản hồi tức thì với người dùng) |
| | Prompt Eval Speed (trung bình) | 128.5 tok/s | Tốc độ xử lý ngữ cảnh đầu vào rất nhanh |
| **Băng thông** | **Decode Speed (Median)** | **67.71 tok/s** | **XUẤT SẮC** (vượt xa tốc độ đọc tự nhiên ~5–8 tok/s) |
| | Peak Decode Speed (llama-bench ngắn) | 18.34 tok/s | Tốc độ trong bài đo llama-bench tiêu chuẩn |
| **Bộ nhớ** | **Peak Process PSS** | **595.4 MB** | **AN TOÀN** (chiếm ~30% ngân sách RAM khả dụng 1.9 GB) |
| | RSS Total | ~700 MB | Không gây áp lực lên cơ chế LMK (Low Memory Killer) |
| **Nhiệt độ** | Nhiệt độ Pin (BAT Temp) | 41.4°C → 41.4°C | Ổn định, không tăng nhiệt ở cell pin |
| | **Nhiệt độ Chip (AP Temp)** | **41.9°C → 48.9°C** | **Tăng +7.0°C** sau chuỗi prompt liên tục |
| | **Tốc độ khi Throttling** | **0.29 tok/s** | **NGUY HIỂM**: Tụt 98.4% tốc độ do tản nhiệt hạn chế của form-factor gập |
| **Độ chính xác** | **Action Request Accuracy** | **0.0%** (0/5) | **THẤT BẠI**: Model 0.5B không sinh được schema JSON hợp lệ |
| | **Conversation Accuracy** | **60.0%** (3/5) | Chấp nhận được ở tác vụ trả lời ngắn |
| | **Safety Trap Pass Rate** | **100.0%** (5/5) | **TUYỆT ĐỐI**: Không có action vi phạm nào lọt qua ActionValidator |

---

## 4. Phân tích Kỹ thuật Chuyên sâu

### 4.1 Điểm sáng: Tốc độ suy luận và Tiết kiệm RAM
- **Tốc độ sinh từ vượt trội:** Với 4 nhân CPU trên Snapdragon 8 Gen 2, `Qwen2.5-0.5B-Q4_K_M` đạt tốc độ suy luận trung vị lên tới **67.7 tokens/s**. Thời gian tạo token đầu tiên chỉ **~734 ms**, mang lại cảm giác phản hồi cực kỳ nhanh nhạy.
- **Dấu chân bộ nhớ (Memory Footprint) gọn gàng:** Tiến trình chỉ chiếm **~595 MB PSS**. So với dung lượng RAM khả dụng thực tế (~1.9 GB), hệ thống hoàn toàn dư dả **> 1.2 GB RAM** để phân bổ cho các thành phần khác.

### 4.2 Thách thức 1: Giới hạn Năng lực Mô hình 0.5B (Action Schema Failure)
- **Tỷ lệ sinh Action thành công là 0.0%:**
  - Mô hình 0.5B có kích thước quá nhỏ để ghi nhớ và tuân thủ định dạng JSON nghiêm ngặt (`{"response": "...", "action": {"type": "...", "params": {...}}}`) trong môi trường zero-shot tiếng Việt.
  - Thay vì trả về JSON hợp lệ, mô hình thường trả về văn bản đàm thoại tự do (ví dụ: *"Dạ, tôi sẽ mở ứng dụng YouTube cho bạn ngay bây giờ..."*) hoặc sinh ra chuỗi JSON khuyết thiếu dấu đóng ngoặc nhọn do chạm `max_tokens`.
  - **Kết luận:** Mô hình local cỡ 0.5B **không thể** đóng vai trò Agent độc lập để quyết định thực thi hành động nếu không có cơ chế ràng buộc ngữ pháp (Grammar-Constrained Decoding / GBNF) hoặc fine-tuning chuyên biệt (Phase 7).

### 4.3 Thách thức 2: Nhiệt độ và Hiện tượng Thermal Throttling Khắc nghiệt
- **Đặc thù tản nhiệt của Samsung Galaxy Z Flip5:**
  - Z Flip5 là dòng điện thoại nắp gập siêu mỏng; bo mạch chủ và SoC Snapdragon 8 Gen 2 nằm ở nửa trên của thân máy, diện tích tản nhiệt buồng hơi (vapor chamber) rất nhỏ hẹp so với dòng Galaxy S23 Ultra hay laptop.
  - Khi chạy tác vụ suy luận liên tục (sustained inference > 5–10 phút), nhiệt độ vi xử lý (AP) vọt nhanh lên gần **49°C**.
  - Cơ chế điều phối nhiệt của kernel Samsung ngay lập tức hạ xung nhịp CPU (Thermal Throttling), khiến tốc độ sinh từ rơi tự do từ **18.3 tok/s xuống chỉ còn 0.29 tok/s** (chậm gấp 60 lần).
  - **Kết luận:** Trên Z Flip5, không được phép duy trì vòng lặp suy luận LLM liên tục. Mọi truy vấn local phải có giới hạn thời gian (timeout ~5–10s), có thời gian nghỉ tản nhiệt (cooldown), và cần cơ chế giám sát nhiệt độ để tự ngắt hoặc chuyển hướng sang Laptop/Cloud khi máy bị nóng.

### 4.4 Thách thức 3: Cơ chế Bảo vệ An toàn Hoạt động Hoàn hảo
- Dù mô hình 0.5B sinh JSON lỗi, **không có bất kỳ hành động độc hại hoặc vượt quyền nào được thực thi**.
- Lớp bảo vệ `ActionValidator` kết hợp với `LocalModelOutputParser` đã:
  - Từ chối ngay lập tức các chuỗi không parse được JSON (`REJECTED_MALFORMED`).
  - Chặn đứng 100% các câu bẫy an toàn (`safety_trap`).
  - Đảm bảo bất biến kiến trúc: **"LLM chỉ đề xuất, Validator và Executor quyết định an toàn"**.

---

## 5. Ngân sách Tài nguyên cho Phase 5 (Voice)

Dựa trên số liệu đo đạc thực tế của Phase 4, ngân sách tài nguyên hệ thống dành cho Phase 5 (Voice: Wake word + STT + TTS) trên Z Flip5 được xác định cụ thể như sau:

| Tài nguyên | Tổng khả dụng hệ thống | Đã dùng cho Local LLM (0.5B) | Ngân sách còn lại cho Voice (Phase 5) |
|---|:---:|:---:|:---:|
| **Bộ nhớ RAM (PSS)** | ~1,900 MB | ~600 MB | **~1,200 MB – 1,300 MB** |
| **CPU Cores** | 8 nhân | 4 threads (ngắt quãng) | 2–3 nhân cho Audio processing / Wake word / STT |
| **Bộ nhớ trong (Storage)** | ~402 GB trống | ~0.5 GB | Thoải mái cho model Sherpa-ONNX / Piper TTS (~100–300 MB) |
| **Ngân sách Nhiệt** | Ngưỡng an toàn < 45°C | Tăng 7°C khi chạy nặng | Cần chia sẻ chu kỳ CPU: Mic nền phải siêu nhẹ, không để CPU chạy liên tục |

---

## 6. Kết luận & Quyết định Nghiệm thu Phase 4

- **Quyết định:** **LIMITED GO (Chấp thuận có điều kiện / Triển khai cục bộ có giới hạn)** theo chi tiết tại [ADR-0007](file:///c:/Users/TTT/Desktop/robotv1-repo/robot/docs/decisions/0007-local-llm-spike-decision.md).
- **Định vị Local LLM:**
  - Mô hình local 0.5B được tích hợp vào ứng dụng Android ([`LocalProvider.kt`](file:///c:/Users/TTT/Desktop/robotv1-repo/robot/android/app/src/main/java/com/thanhnhe00/robot/data/provider/local/LocalProvider.kt)) với vai trò: **Trợ lý giao tiếp nhanh, trả lời câu hỏi ngắn offline**.
  - **Không** giao phó quyền tự sinh Action có cấu trúc cho Local LLM 0.5B zero-shot. Các lệnh điều khiển hệ thống, mở ứng dụng và thao tác phần cứng sẽ:
    1. Ưu tiên xử lý bằng luật xác định (Deterministic Intent Matching / MockProvider) khi chạy offline.
    2. Định tuyến qua Backend FastAPI / Cloud khi có kết nối mạng.
    3. Xem xét áp dụng Grammar Constraint (GBNF) hoặc Fine-tuning chuyên biệt ở Phase 7.
- **Nghiệm thu:** Phase 4 đạt 100% mục tiêu spike thực nghiệm, cung cấp số liệu thực tế chính xác trên phần cứng Samsung Galaxy Z Flip5, mở đường an toàn cho Phase 5.
