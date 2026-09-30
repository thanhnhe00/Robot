# Research A — LLM trên Android

Ngày tra cứu: 2026-09-30. Phạm vi: lựa chọn ứng viên cho spike Phase 4 trên LG G8; nội dung dưới đây là tổng hợp tài liệu, không phải kết quả benchmark trên thiết bị.

## R1 — Runtime

| Ứng viên | Điều tài liệu xác nhận | Chưa xác nhận trên LG G8 |
|---|---|---|
| llama.cpp | Có hướng dẫn Android/NDK, chạy ARM64 và sử dụng model GGUF. Upstream có tài liệu backend Snapdragon/QNN riêng. | Tốc độ, RAM, tương thích driver và tăng tốc trên đúng biến thể G8. Chọn làm baseline CPU để spike vì đường chạy Android/NDK trực tiếp và định dạng GGUF phù hợp shortlist. |
| MLC LLM | Dự án công bố Android OpenCL cho Adreno; Android deployment yêu cầu compile model/runtime artifacts. | G8/Adreno 640 cụ thể, ổn định, pin, nhiệt và chất lượng. Chỉ là ứng viên GPU so sánh. |
| ONNX Runtime GenAI / QNN EP | Runtime có execution provider QNN cho thiết bị Qualcomm. Tài liệu QNN nêu các chip đã kiểm thử, nhưng không xác nhận Snapdragon 855/SM8150 trong danh sách đó. | Không suy ra NPU/DSP trên G8 được hỗ trợ. Cần xác minh chip/API thực tế và một mẫu chạy trước khi đưa vào benchmark. |
| LiteRT-LM | Có hướng dẫn Android và định dạng model riêng; cung cấp API Android. | Tương thích, hiệu năng/driver trên G8 và chi phí chuyển đổi model. |
| ExecuTorch | Có đường triển khai Android; backend phụ thuộc cấu hình và có backend Qualcomm riêng. | Tài liệu được rà soát chưa xác nhận Snapdragon 855 là cấu hình được hỗ trợ/kiểm thử. Không chọn làm baseline ban đầu. |

Nguồn: [llama.cpp Android](https://github.com/crc-org/llama.cpp/blob/main/docs/android.md), [llama.cpp Snapdragon backend](https://github.com/ggml-org/llama.cpp/blob/master/docs/backend/snapdragon/README.md), [MLC LLM](https://github.com/mlc-ai/mlc-llm), [ONNX Runtime QNN EP](https://onnxruntime.ai/docs/execution-providers/QNN-ExecutionProvider.html), [LiteRT-LM Android](https://github.com/google-ai-edge/LiteRT-LM/blob/main/docs/getting-started/build-and-run.md), [ExecuTorch Android](https://docs.pytorch.org/executorch/stable/using-executorch-android.html), [ExecuTorch Qualcomm backend](https://docs.pytorch.org/executorch/stable/backends-qualcomm.html).

## R2 — Phần cứng tăng tốc

Qualcomm xác nhận Snapdragon 855 có Adreno 640 và Hexagon 690. Thông tin SoC này không chứng minh thư viện Android của dự án dùng được GPU/DSP/NPU trên điện thoại cụ thể. Chốt spike: CPU llama.cpp là đường cơ sở; GPU MLC là nhánh thử nghiệm; không cam kết tăng tốc QNN/DSP trước khi có smoke test và số đo trên máy.

Nguồn: [Qualcomm Snapdragon 855](https://www.qualcomm.com/smartphones/products/8-series/snapdragon-855-mobile-platform), [ONNX Runtime QNN EP](https://onnxruntime.ai/docs/execution-providers/QNN-ExecutionProvider.html).

## R3 — Model shortlist

| Model | Dùng trong spike | Điều cần giữ mở |
|---|---|---|
| Qwen3-0.6B | Ứng viên nhỏ để đo RAM/độ trễ và khả năng làm intent/action đơn giản; Qwen3 model card nêu hỗ trợ đa ngôn ngữ và khả năng tool use. | Độ chính xác tiếng Việt, JSON/action compliance và GGUF quantized cần đo. |
| Qwen3-1.7B | Ứng viên chất lượng cao hơn trong shortlist; model card ghi Apache-2.0, hỗ trợ đa ngôn ngữ và tool use. | Chất lượng tiếng Việt và khả năng chạy ổn định trên G8 chưa được benchmark. |
| Gemma 3 1B IT | Ứng viên đối chứng nhỏ; model card nêu hỗ trợ đa ngôn ngữ. | Xem điều khoản Gemma riêng; không coi license như Apache. Chưa có số đo/đánh giá tiếng Việt của dự án. |

Nguồn: [Qwen3-0.6B](https://huggingface.co/Qwen/Qwen3-0.6B), [Qwen3-1.7B](https://huggingface.co/Qwen/Qwen3-1.7B), [Gemma 3 1B IT](https://huggingface.co/google/gemma-3-1b-it).

## R4 — Benchmark quantization

Không có một cỡ file/tốc độ chung áp dụng cho mọi model và runtime. Spike dùng cùng model, context, prompt và runtime; tối thiểu so Q4_K_M và Q5_K_M nếu GGUF có sẵn. Chỉ thêm Q6_K/Q8_0 khi dung lượng/RAM cho phép. Ghi cỡ file, RAM đỉnh, thời gian nạp, TTFT, token/s, JSON hợp lệ, action accuracy, nhiệt độ/throttling và mức pin. Không suy ra số đo từ bảng quantization của model khác.

Nguồn: [llama.cpp quantization implementation](https://github.com/ggml-org/llama.cpp/blob/master/tools/quantize/quantize.cpp), [llama.cpp tensor encoding](https://github.com/ggml-org/llama.cpp/wiki/Tensor-Encoding-Schemes).

## Kết luận / trạng thái

- Ứng viên baseline spike: llama.cpp + CPU + model GGUF; MLC/Adreno là so sánh tùy thời gian.
- Model shortlist: Qwen3-0.6B, Qwen3-1.7B, Gemma 3 1B IT (license review riêng).
- Chưa chọn model/runtime cuối và chưa tuyên bố G8 chạy tốt. Kết quả thực nghiệm thuộc Phase 4.
