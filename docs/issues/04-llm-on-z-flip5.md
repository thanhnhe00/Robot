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
- [ ] `LocalProvider` phía Android tích hợp engine đã chọn (interface giống provider backend)
- [ ] Model Manager v0: metadata, version, checksum, đường dẫn lưu, nạp/gỡ model
- [ ] Benchmark ≥3 model × quantization × context bằng CÙNG bộ prompt
- [ ] Đo: latency, tokens/sec, RAM, CPU, nhiệt (nếu có API), pin, thời gian nạp model, JSON accuracy, crash rate; thêm chạy liên tục ≥10 phút để thấy throttling
- [ ] Báo cáo `docs/benchmarks/phase4-llm-z-flip5.md` + ADR go/no-go

## Definition of Done
- [ ] Có số liệu thật cho từng cấu hình
- [ ] Có quyết định: local đủ dùng / chỉ dùng cho lệnh / cần laptop-cloud
- [ ] Không có tuyên bố nào chưa có số liệu

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
