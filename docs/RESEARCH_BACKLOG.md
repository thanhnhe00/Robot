# Research backlog (Phase 0)

Nguồn ưu tiên: tài liệu chính thức, model card và repository chính thức. Ngày rà soát: 2026-09-30. `DONE` nghĩa là câu hỏi nghiên cứu tài liệu đã có kết luận và nguồn; không có nghĩa thiết bị đã benchmark. Kết quả Phase 4/5/9 phải cập nhật riêng khi đo thực tế.

| # | Chủ đề | Kết quả nghiên cứu | Trạng thái |
|---|---|---|---|
| R1 | Engine LLM Android | `llama.cpp` NDK/GGUF làm ứng viên CPU baseline; MLC/Adreno là thử nghiệm GPU tùy chọn. LiteRT-LM, ONNX Runtime GenAI/QNN và ExecuTorch chưa có bằng chứng trong tài liệu rà soát xác nhận Snapdragon 855/G8 cụ thể. Chi tiết và nguồn: [A-llm.md](research/A-llm.md). | DONE — benchmark Phase 4 |
| R2 | Tăng tốc phần cứng | 855 có Adreno 640/Hexagon 690; chưa xác nhận runtime/driver tăng tốc trên máy cụ thể. CPU baseline, không tuyên bố NPU/GPU hoạt động. | DONE — kiểm tra Phase 4 |
| R3 | Danh sách model benchmark | Qwen3-0.6B, Qwen3-1.7B và Gemma 3 1B IT là shortlist nghiên cứu; giấy phép/độ chính xác tiếng Việt cần kiểm tra theo model card và benchmark. | DONE — benchmark Phase 4 |
| R4 | Quantization | Cỡ và chất lượng phụ thuộc model/runtime; đề xuất so Q4_K_M/Q5_K_M trước, thêm Q6_K/Q8_0 nếu RAM cho phép; đo các metric trong [A-llm.md](research/A-llm.md). | DONE — benchmark Phase 4 |
| R5 | STT tiếng Việt | So sánh Android on-device (availability phụ thuộc device/service/model) với sherpa-onnx Vietnamese ASR; chưa đo chất lượng/độ trễ G8. | DONE — benchmark Phase 5 |
| R6 | TTS tiếng Việt | Android TTS cần kiểm tra voice khả dụng; sherpa/Piper là offline candidate, kiểm giấy phép từng voice; chưa đo trên G8. | DONE — benchmark Phase 5 |
| R7 | Wake word | openWakeWord pretrained hiện tiếng Anh; Porcupine công bố ngôn ngữ không gồm tiếng Việt; chưa xác nhận pretrained Vietnamese KWS. Cần thử nghiệm và đặt ngưỡng false accept/reject. | DONE — giải pháp chờ thử nghiệm Phase 5 |
| R8 | Giới hạn Android | Đã ghi giới hạn microphone foreground/background, alarm intent, volume, Wi‑Fi và fallback trong [C-android-hardware.md](research/C-android-hardware.md). Hành vi OEM phải kiểm tra trên G8. | DONE — kiểm chứng app Phase 3/5 |
| R9 | USB phone↔ESP32 | Android USB Host API có điều kiện phần cứng; nguồn chính thức không xác nhận OTG + sạc đồng thời trên G8. Giữ USB/BLE/Wi‑Fi là phương án chưa xếp hạng đến Phase 9. | DONE — kiểm tra phần cứng Phase 9 |
| R10 | Vision | Detector/face recognition, license, tốc độ, quyền riêng tư. | OPEN — Phase 8 |

## Nguồn chính

- [Qualcomm Snapdragon 855](https://www.qualcomm.com/smartphones/products/8-series/snapdragon-855-mobile-platform)
- [Android foreground service types](https://developer.android.com/develop/background-work/services/fgs/service-types)
- [Android USB Host](https://developer.android.com/develop/connectivity/usb/host)
- [Research A — LLM](research/A-llm.md), [Research B — Voice](research/B-voice.md), [Research C — Android/Hardware](research/C-android-hardware.md)
