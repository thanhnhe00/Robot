# Research A — LLM trên Android

Ngày rà soát: 2026-09-30. Thiết bị mục tiêu do chủ dự án chỉ định: Samsung Galaxy Z Flip5, Snapdragon 8 Gen 2 for Galaxy, RAM 8GB, bộ nhớ trong 512GB. Đây là tổng hợp tài liệu cho spike Phase 4, không phải kết quả benchmark trên điện thoại.

## R1 — Runtime

| Ứng viên | Điều tài liệu xác nhận | Điều chưa xác nhận trên Galaxy Z Flip5 |
|---|---|---|
| `llama.cpp` | Có hướng dẫn Android/NDK, ARM64, model GGUF và backend Snapdragon/QNN. | Tốc độ, RAM thực, driver, và chế độ tăng tốc dùng được trên firmware/SKU cụ thể. Dùng CPU làm baseline tương thích ban đầu; xác minh build trên thiết bị trước benchmark đầy đủ. |
| MLC LLM | Dự án có đường triển khai Android dùng OpenCL cho GPU Adreno; cần chuẩn bị model/runtime artifacts theo hướng dẫn. | Tương thích driver GPU cụ thể, ổn định, mức tăng tốc, pin và nhiệt trên điện thoại. Giữ là ứng viên so sánh GPU. |
| ONNX Runtime QNN EP | Có execution provider QNN cho Android/Qualcomm. Danh sách SoC và cấu hình được hỗ trợ/kiểm thử trong tài liệu hiện rà soát không xác nhận SM8550. | Chưa có căn cứ để coi QNN EP đã được kiểm thử trên Z Flip5. Kiểm tra phiên bản runtime/QNN, SDK và smoke test trước khi đưa vào benchmark. |
| LiteRT-LM | Có hướng dẫn và API triển khai Android. | Tương thích với firmware, model format, tốc độ và tiêu thụ bộ nhớ trên điện thoại chưa được đo. |
| ExecuTorch Qualcomm backend | Tài liệu Qualcomm backend của ExecuTorch nêu `SM8550`; tài liệu Android Qualcomm phiên bản 1.2 liệt kê Snapdragon 8 Gen 2 / SM8550 và báo cáo ví dụ được xác minh với SM8550. Đây là căn cứ cho một ứng viên QNN/HTP. | Bằng chứng backend/SoC không đảm bảo app build, firmware Samsung, phiên bản QNN/SDK hay hiệu năng bền vững. Cần smoke test trên Z Flip5. |

Nguồn: [llama.cpp Android](https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md), [llama.cpp Snapdragon backend](https://github.com/ggml-org/llama.cpp/blob/master/docs/backend/snapdragon/README.md), [MLC LLM](https://github.com/mlc-ai/mlc-llm), [ONNX Runtime QNN EP](https://onnxruntime.ai/docs/execution-providers/QNN-ExecutionProvider.html), [LiteRT-LM Android](https://github.com/google-ai-edge/LiteRT-LM/blob/main/docs/getting-started/build-and-run.md), [ExecuTorch Qualcomm backend](https://docs.pytorch.org/executorch/stable/backends-qualcomm.html), [ExecuTorch Android Qualcomm 1.2](https://docs.pytorch.org/executorch/1.2/android-qualcomm.html).

## R2 — Phần cứng tăng tốc

Samsung xác nhận Galaxy Z Flip5 dùng Snapdragon 8 Gen 2 for Galaxy; cấu hình được chọn có RAM 8GB và bộ nhớ 512GB. Qualcomm nêu nền tảng Snapdragon 8 Gen 2 có GPU Adreno, Vulkan 1.3/OpenCL 2.0 FP và các tính năng AI trên Hexagon. Tài liệu ExecuTorch Qualcomm liệt kê SM8550, là ứng viên tăng tốc có căn cứ để thử.

Các thông tin nền tảng này không chứng minh bất kỳ runtime cụ thể nào đang dùng GPU/DSP/NPU thành công trên firmware Z Flip5, cũng không dự đoán được tốc độ hoặc nhiệt. Chốt spike: llama.cpp CPU làm baseline; MLC/Adreno và ExecuTorch/QNN là nhánh thử nghiệm; chỉ ghi nhận tăng tốc sau khi smoke test và đo trên máy.

Nguồn: [Samsung Galaxy Z Flip5 specifications](https://images.samsung.com/is/content/samsung/assets/us/2307/business/mobile/phones/b5/B2B_Galaxy_Z_Flip5_Spec_Sheet_Generic_HR.pdf), [Qualcomm Snapdragon 8 Gen 2](https://www.qualcomm.com/smartphones/products/8-series/snapdragon-8-gen-2-mobile-platform), [ExecuTorch Qualcomm backend](https://docs.pytorch.org/executorch/stable/backends-qualcomm.html).

## R3 — Model shortlist

| Model | Dùng trong spike | Điều cần giữ mở |
|---|---|---|
| Qwen3-0.6B | Ứng viên nhỏ để đo RAM/độ trễ và khả năng làm intent/action đơn giản; model card nêu hỗ trợ đa ngôn ngữ và tool use. | Độ chính xác tiếng Việt, JSON/action compliance và bản quantized cần đo. |
| Qwen3-1.7B | Ứng viên chất lượng cao hơn trong shortlist; model card ghi Apache-2.0, hỗ trợ đa ngôn ngữ và tool use. | Chất lượng tiếng Việt và khả năng chạy ổn định trên Z Flip5 chưa được benchmark. |
| Gemma 3 1B IT | Ứng viên đối chứng nhỏ; model card nêu hỗ trợ đa ngôn ngữ. | Xem điều khoản Gemma riêng; không coi license như Apache. Chưa có số đo/đánh giá tiếng Việt của dự án. |

Nguồn: [Qwen3-0.6B](https://huggingface.co/Qwen/Qwen3-0.6B), [Qwen3-1.7B](https://huggingface.co/Qwen/Qwen3-1.7B), [Gemma 3 1B IT](https://huggingface.co/google/gemma-3-1b-it).

## R4 — Benchmark quantization

Không có một cỡ file/tốc độ chung áp dụng cho mọi model và runtime. Spike dùng cùng model, context, prompt và runtime; tối thiểu so Q4_K_M và Q5_K_M nếu GGUF có sẵn. Chỉ thêm Q6_K/Q8_0 khi dung lượng/RAM cho phép. Ghi cỡ file, RAM đỉnh, thời gian nạp, TTFT, token/s, JSON hợp lệ, action accuracy, nhiệt độ/throttling và mức pin. Không suy ra số đo từ bảng quantization của model khác.

Nguồn: [llama.cpp quantization implementation](https://github.com/ggml-org/llama.cpp/blob/master/tools/quantize/quantize.cpp), [llama.cpp tensor encoding](https://github.com/ggml-org/llama.cpp/wiki/Tensor-Encoding-Schemes).

## Kết luận / trạng thái

- Thiết bị mục tiêu: Samsung Galaxy Z Flip5 — Snapdragon 8 Gen 2 for Galaxy, RAM 8GB, bộ nhớ trong 512GB.
- Ứng viên baseline spike: llama.cpp + CPU + model GGUF; MLC/Adreno và ExecuTorch/QNN là nhánh so sánh cần smoke test.
- Model shortlist: Qwen3-0.6B, Qwen3-1.7B, Gemma 3 1B IT (license review riêng).
- Chưa chọn model/runtime cuối và chưa tuyên bố runtime nào chạy tốt hay tăng tốc trên Z Flip5. Kết quả thực nghiệm thuộc Phase 4.
