---
title: [Phase 4] Spike LLM trên Z Flip5 + Model Manager v0
labels: phase-4, epic
milestone: Phase 4
---
## Mục tiêu
Chạy LLM local trên Samsung Galaxy Z Flip5, benchmark thật, đưa ra quyết định go/no-go cho local-first (ADR-0002).

Thiết bị mục tiêu: Snapdragon 8 Gen 2 for Galaxy, RAM 8GB, bộ nhớ trong 512GB. Ghi nhận trực tiếp SKU, Android/API, firmware và RAM khả dụng trước khi đo; đây chưa phải kết quả benchmark.

## Vì sao cần phase này
Toàn bộ luận điểm local-first phụ thuộc vào việc này; kết quả quyết định ngân sách cho Voice.

## Phụ thuộc
Phase 3, research nhóm A

## Việc cần làm
- [x] `LocalProvider` phía Android tích hợp engine đã chọn (interface giống provider backend: `NativeLlamaBridge`, `LlamaRuntime`, `LocalModelOutputParser`)
- [x] Model Manager v0: metadata, version, checksum, đường dẫn lưu, nạp/gỡ model, `MemoryGuard` preflight check RAM
- [x] Benchmark trên phần cứng thực tế với bộ prompt cố định 20 câu (Golden Benchmark Set `phase4-golden-set.json`)
- [x] Đo: latency (TTFT ~734.8ms), tokens/sec (~67.7 tok/s), RAM (PSS ~595MB), CPU (4 threads), nhiệt (AP tăng từ 41.9°C lên 48.9°C), pin, JSON accuracy (0.0% cho 0.5B), safety traps pass (100%), sustained test phát hiện throttling tụt về 0.29 tok/s
- [x] Báo cáo `docs/benchmarks/phase4-llm-z-flip5.md` + ADR-0007 (Quyết định Limited Go)

## Definition of Done
- [x] Có số liệu thật cho cấu hình trên Samsung Galaxy Z Flip5 (SM-F731B)
- [x] Có quyết định: Limited Local-First (local đủ dùng cho chat ngắn offline; action phức tạp định tuyến laptop-cloud hoặc fine-tuning Phase 7)
- [x] Không có tuyên bố nào chưa có số liệu (toàn bộ đo đạc thực tế lưu tại `docs/benchmarks/phase4-raw-benchmark-results.json`)

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
