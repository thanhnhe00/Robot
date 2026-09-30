# Research B — Voice tiếng Việt

Ngày tra cứu: 2026-09-30. Thiết bị mục tiêu: Samsung Galaxy Z Flip5 (Snapdragon 8 Gen 2 for Galaxy, RAM 8GB, bộ nhớ trong 512GB). Đây là danh sách ứng viên cho Phase 5, không phải kết quả chạy trên điện thoại.

## R5 — STT

| Ứng viên | Bằng chứng và giới hạn |
|---|---|
| Android SpeechRecognizer on-device | Android có API kiểm tra on-device recognition và tải model ngôn ngữ; khả năng dùng phụ thuộc service/OEM và model đã có trên thiết bị. Không thể khẳng định offline tiếng Việt trước khi kiểm tra Z Flip5. |
| sherpa-onnx | Có build Android cho ASR và các model tiếng Việt trong danh mục pretrained. Đây là ứng viên offline; kích thước, độ trễ, WER tiếng Việt và tiêu thụ pin phải đo trên máy. |
| Cloud STT | Có thể làm fallback khi mạng/quyền riêng tư cho phép; provider cụ thể, giá, chính sách dữ liệu và chất lượng chưa được chọn trong repo. |

Nguồn: [Android SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer), [sherpa-onnx Android](https://github.com/k2-fsa/sherpa-onnx/blob/master/docs/source/onnx/android/build-sherpa-onnx.rst), [sherpa-onnx Zipformer models](https://github.com/k2-fsa/sherpa-onnx/blob/master/docs/source/onnx/pretrained_models/offline-transducer/zipformer-transducer-models.rst).

Benchmark: cùng tập câu tiếng Việt (giọng miền, khoảng cách và nhiễu được ghi lại); đo WER/CER, tỷ lệ nhận đúng ý định/action, độ trễ, RAM, pin và khả năng chạy airplane mode.

## R6 — TTS

| Ứng viên | Bằng chứng và giới hạn |
|---|---|
| Android TextToSpeech | API cho phép kiểm tra ngôn ngữ/voice có sẵn lúc chạy. Z Flip5 có voice tiếng Việt hay không phụ thuộc engine và dữ liệu cài đặt. |
| sherpa-onnx / Piper voice | sherpa-onnx hỗ trợ Android TTS; Piper có danh mục voice `vi_VN`. Cần xem license của từng voice/model riêng, tải thử và đo chất lượng/tốc độ trên máy. |
| Cloud TTS | Ứng viên fallback; cần quyết định provider, chi phí, điều khoản và phân loại dữ liệu trước khi dùng. |

Nguồn: [Android TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech), [sherpa-onnx Android](https://github.com/k2-fsa/sherpa-onnx/blob/master/docs/source/onnx/android/build-sherpa-onnx.rst), [Piper voice list](https://github.com/rhasspy/piper/blob/master/VOICES.md).

Benchmark: chấm nghe mù bằng câu cố định; đo mức dễ nghe, phát âm tên/số, độ trễ, kích thước model, RAM và pin.

## R7 — Wake word “Hey Robot”

| Ứng viên | Bằng chứng và giới hạn |
|---|---|
| openWakeWord | Model pretrained hiện chỉ hỗ trợ tiếng Anh. Code Apache-2.0 nhưng pretrained models CC-BY-NC-SA 4.0; kiểm tra license trước khi dùng. Không coi đây là model tiếng Việt sẵn dùng. |
| Porcupine | Danh sách ngôn ngữ hỗ trợ công bố không gồm tiếng Việt. License/điều khoản triển khai cần xem riêng; không chốt dùng cho “Hey Robot”. |
| sherpa-onnx KWS | Có framework KWS; các model/phrase tiếng Việt pretrained phù hợp chưa được xác nhận trong tài liệu đã rà soát. Có thể thử hướng ASR/KWS tùy biến, nhưng phải đo false accept và false reject. |

Nguồn: [openWakeWord](https://github.com/dscripka/openWakeWord), [Porcupine FAQ](https://picovoice.ai/docs/faq/porcupine/), [sherpa-onnx KWS](https://k2-fsa.github.io/sherpa/onnx/kws/index.html), [KWS pretrained models](https://k2-fsa.github.io/sherpa/onnx/kws/pretrained_models/index.html).

Benchmark: false accept/giờ, false reject, độ trễ, tiêu thụ pin và độ bền với giọng/ngữ cảnh khác nhau. Cần xác định ngưỡng chấp nhận trước thử nghiệm.

## Kết luận / trạng thái

- STT: so sánh Android on-device với sherpa-onnx offline; cloud chỉ là fallback có điều kiện.
- TTS: so sánh Android TTS với voice tiếng Việt sherpa/Piper; kiểm license model cụ thể.
- Wake word tiếng Việt: chưa có model pretrained đã xác nhận trong các nguồn này; giữ mở cho thử nghiệm, không tuyên bố hỗ trợ.
- Chưa có kết quả latency, chất lượng, pin hoặc khả năng chạy nền trên Samsung Galaxy Z Flip5.
