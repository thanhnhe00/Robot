# ADR-0007: Kết quả Spike LLM Local trên Galaxy Z Flip5 và Định hướng Quyết định (Limited Go)

> **Date:** 2026-10-01  
> **Status:** Accepted  
> **Tài liệu liên quan:**  
> - Báo cáo thực nghiệm: [`docs/benchmarks/phase4-llm-z-flip5.md`](file:///c:/Users/TTT/Desktop/robotv1-repo/robot/docs/benchmarks/phase4-llm-z-flip5.md)  
> - Baseline thiết bị: [`docs/benchmarks/phase4-device-baseline.md`](file:///c:/Users/TTT/Desktop/robotv1-repo/robot/docs/benchmarks/phase4-device-baseline.md)  
> - Tiền đề kiến trúc: [`ADR-0002`](file:///c:/Users/TTT/Desktop/robotv1-repo/robot/docs/decisions/0002-llm-spike-before-voice.md), [`ADR-0005`](file:///c:/Users/TTT/Desktop/robotv1-repo/robot/docs/decisions/0005-phase0-research-shortlists.md)

---

## 1. Bối cảnh và Mục tiêu

Theo lộ trình tại [ADR-0002](file:///c:/Users/TTT/Desktop/robotv1-repo/robot/docs/decisions/0002-llm-spike-before-voice.md), Phase 4 thực hiện spike LLM trực tiếp trên điện thoại "bộ não" Samsung Galaxy Z Flip5 (`SM-F731B`, Snapdragon 8 Gen 2 for Galaxy, 8 GB RAM, Android 15 / API 35) trước khi bước sang làm Voice (Phase 5).

Mục tiêu cốt lõi: Có số liệu đo đạc thực tế để trả lời dứt khoát câu hỏi: **Việc chạy LLM local trên điện thoại có đủ khả thi để làm nền tảng cho robot hay không (GO, LIMITED, hay NO-GO)?**

---

## 2. Quyết định (Decision)

Quyết định lựa chọn phương án: **LIMITED LOCAL-FIRST (Chấp thuận triển khai cục bộ có giới hạn)**.

1. **Định vị Local LLM:**
   - Mô hình cục bộ trên điện thoại (hiện tại là `Qwen2.5-0.5B-Instruct-Q4_K_M` chạy qua `NativeLlamaBridge` ARM64) được sử dụng cho tác vụ: **Hội thoại ngắn, trả lời câu hỏi thông thường khi ngoại tuyến (offline)**.
   - **Tuyệt đối không giao quyền tự sinh Action có cấu trúc cho mô hình 0.5B zero-shot**, do tỷ lệ sinh JSON Action thành công đạt **0.0%**.

2. **Cơ chế xử lý Action an toàn:**
   - Các hành động điều khiển hệ thống Android (`open_app`, `set_volume`, `get_time`, `get_battery`, `set_alarm`) khi offline sẽ ưu tiên xử lý qua **bộ luật xác định (Deterministic Rule Matching / Intent Regex / MockProvider)**.
   - Khi có kết nối mạng hoặc cần lập luận phức tạp, hệ thống định tuyến (AI Router ở Phase 6) sẽ gửi yêu cầu về **Laptop/Backend (FastAPI) hoặc Cloud**.
   - Khả năng sinh Action trên mô hình local chỉ được xem xét kích hoạt lại khi có **Grammar-Constrained Decoding (GBNF)** hoặc sau khi được **Fine-tuning chuyên biệt** ở Phase 7.

3. **Kiểm soát nhiệt độ và tài nguyên (Thermal & Memory Policy):**
   - Áp dụng `MemoryGuard` với ngưỡng an toàn: từ chối nạp mô hình nếu RAM khả dụng của hệ thống < 1,000 MB.
   - Giới hạn thời gian suy luận (timeout tối đa 10 giây/lượt) và bắt buộc có khoảng nghỉ tản nhiệt giữa các chu kỳ thoại nhằm tránh Thermal Throttling nặng trên thân máy gập Z Flip5.

4. **Xác định ngân sách cho Phase 5 (Voice):**
   - Với việc LLM 0.5B chiếm ~600 MB PSS và 4 nhân CPU khi sinh từ, hệ thống bảo toàn được **~1,200 MB – 1,300 MB RAM khả dụng** và headroom CPU nhàn rỗi cho các module Wake word, STT (Sherpa-ONNX) và TTS (Piper) trong Phase 5.

---

## 3. Vì sao (Why)

Quyết định này dựa trên bằng chứng đo đạc thực nghiệm 100% trên phần cứng thật (ghi nhận tại [`docs/benchmarks/phase4-llm-z-flip5.md`](file:///c:/Users/TTT/Desktop/robotv1-repo/robot/docs/benchmarks/phase4-llm-z-flip5.md)):

1. **Về mặt khả thi (Ưu điểm vượt trội):**
   - Tốc độ sinh từ đạt **67.7 tokens/s** (trung vị) và TTFT chỉ **~734.8 ms**. Đây là tốc độ vượt xa kỳ vọng đàm thoại thông thường.
   - Dung lượng bộ nhớ chỉ chiếm **~595 MB PSS**, hoạt động an toàn trong budget RAM ~1.9 GB khả dụng của Galaxy Z Flip5 mà không gây tràn bộ nhớ (OOM).

2. **Về rào cản năng lực (Lý do không chọn FULL GO):**
   - Mô hình 0.5B hoàn toàn bất lực trong việc xuất chuỗi JSON Action chuẩn xác bằng tiếng Việt (đạt 0/5 ca kiểm thử). Nếu để 0.5B tự quyết định Action, app sẽ liên tục rơi vào trạng thái parse error hoặc không thể thực thi hành động người dùng mong muốn.

3. **Về rào cản vật lý (Nhiệt độ):**
   - Galaxy Z Flip5 có thiết kế gập mỏng, tản nhiệt buồng hơi rất hạn chế. Khi chạy liên tục, chip AP nóng lên **48.9°C** và bị kernel can thiệp giảm xung cực mạnh (tốc độ tụt từ **18.3 tok/s xuống 0.29 tok/s**). Do đó không thể để LLM local chạy thường trực hoặc chạy các tác vụ nền nặng.

---

## 4. Các phương án đã cân nhắc (Alternatives)

### Phương án A: FULL LOCAL-FIRST (Ép toàn bộ tính năng chạy offline trên máy)
- *Ý tưởng:* Nâng kích thước mô hình lên `Qwen2.5-1.5B` hoặc `3B` để cải thiện khả năng sinh Action JSON.
- *Lý do bác bỏ:*
  - Mô hình 1.5B – 3B sẽ chiếm từ 1.2 GB đến 2.2 GB RAM, chạm sát hoặc vượt trần RAM khả dụng (~1.9 GB), khiến hệ điều hành One UI 7 có thể tự động tắt app (OOM Killer).
  - Tải nhiệt tăng gấp đôi sẽ kích hoạt throttling ngay sau 1–2 lượt hỏi đáp.
  - Không còn chỗ trống cho STT/TTS ở Phase 5.

### Phương án B: PURE CLOUD / LAPTOP (Bỏ hoàn toàn Local LLM)
- *Ý tưởng:* Điện thoại chỉ đóng vai trò client UI; toàn bộ suy luận AI đẩy lên backend laptop hoặc cloud API.
- *Lý do bác bỏ:*
  - Phá vỡ tôn chỉ cốt lõi của dự án Robot: **Local-first, độc lập mạng**. Khi mất kết nối Wi-Fi/cáp USB, robot sẽ trở nên hoàn toàn bất động và câm lặng.

### Phương án C: LIMITED HYBRID LOCAL (Phương án được chọn)
- *Ý tưởng:* Kết hợp hài hòa điểm mạnh của từng tầng: Local LLM phụ trách hội thoại nhanh, tức thì, offline; Tầng luật xác định lo Action cục bộ; Tầng Backend/Cloud lo tác vụ nặng/lập luận phức tạp.
- *Ưu điểm:* Đảm bảo tính sẵn sàng cao, giữ trọn vẹn an toàn kiến trúc và không đánh đổi trải nghiệm vì quá nhiệt.

---

## 5. Đánh đổi (Trade-offs)

- **Điểm đánh đổi:** Phải duy trì hai luồng xử lý: luồng luật cục bộ/mock cho các lệnh cơ bản khi offline và luồng AI Gateway khi online; đòi hỏi Phase 6 (AI Router) phải thiết kế cẩn trọng bộ quy tắc phân loại yêu cầu.
- **Lợi ích thu lại:** Thiết bị Z Flip5 mát mẻ, không bị tụt xung, app chạy mượt mà, sẵn sàng tích hợp cụm Voice ở Phase 5 mà không lo cạn kiệt tài nguyên.

---

## 6. Kết luận & Phê duyệt

- **Trạng thái:** **Accepted**.
- **Hiệu lực:** Áp dụng ngay cho việc thiết kế kiến trúc Phase 5 (Voice) và Phase 6 (AI Router & Memory).
