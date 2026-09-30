# Research backlog (Phase 0)

Nguồn ưu tiên: tài liệu chính thức, model card, GitHub chính thức. Nguồn cộng đồng phải ghi rõ.
Trạng thái: `OPEN` → `DONE` (kèm link nguồn + ngày) → dẫn tới ADR nếu là quyết định.

| # | Chủ đề | Câu hỏi cần trả lời | Trạng thái |
|---|---|---|---|
| R1 | Engine LLM Android | llama.cpp (build NDK chính thức vs wrapper) so với MLC, ONNX Runtime GenAI, MediaPipe/LiteRT-LM, ExecuTorch: hỗ trợ Snapdragon 855, ARM64, GGUF, structured output/grammar, license, mức độ bảo trì | OPEN |
| R2 | Tăng tốc phần cứng | GPU (Adreno 640) / DSP có dùng được cho LLM trên 855 không, hay chỉ CPU | OPEN |
| R3 | Danh sách model benchmark | Model card hiện hành của Qwen3 (0.6B/1.7B) và 1–2 model nhỏ khác; license; hỗ trợ tiếng Việt; function calling | OPEN |
| R4 | Quantization | Q4_K_M/Q5_K_M/Q6_K/Q8_0: kích thước và đánh đổi cho điện thoại | OPEN |
| R5 | STT tiếng Việt | Android SpeechRecognizer (offline?), sherpa-onnx, whisper.cpp, cloud: model Việt có sẵn, kích thước, độ trễ | OPEN |
| R6 | TTS tiếng Việt | Giọng Việt của Android TTS, Piper/sherpa-onnx, cloud | OPEN |
| R7 | Wake word | openWakeWord, Porcupine, sherpa-onnx KWS: có dùng được cho "Hey Robot", tiêu thụ pin, license | OPEN |
| R8 | Giới hạn Android | Mic nền (foreground service), báo thức (`AlarmClock` intent), âm lượng, WiFi (bị hạn chế từ Android 10), quyền cần thiết | OPEN |
| R9 | USB phone↔ESP32 | USB host + sạc cùng lúc, thư viện serial cho Android, phương án BLE/WiFi | OPEN |
| R10 | Vision | Detector và face recognition cho Android: license (lưu ý AGPL), tốc độ, quyền riêng tư | OPEN (làm ở Phase 8) |
