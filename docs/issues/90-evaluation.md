---
title: [Cross-cutting] Hệ thống Evaluation xuyên suốt
labels: cross-cutting, evaluation, epic
milestone: Cross-cutting
---
## Mục tiêu
Một hệ thống đánh giá dùng lại cho mọi phase, không chỉ benchmark một lần. Bộ test và dataset đánh giá **độc lập với model**.

## Metric
Action accuracy • JSON validity • Intent accuracy • Tool selection accuracy • Response latency • Tokens/sec • RAM • CPU • Battery • Model load time • Crash rate (Phase 5+ thêm: WER, latency STT/TTS; Phase 8: FPS, độ chính xác nhận diện)

## Nguyên tắc
- Cùng một bộ prompt cho mọi model; ghi model, quantization, context, thiết bị, ngày
- Kết quả lưu ở `docs/benchmarks/` (có thể tái lập bằng script)
- Không tuyên bố model tốt hơn khi chưa có số liệu

## Theo phase
- [ ] Phase 2: harness v0 + golden set
- [ ] Phase 4: thêm metric tài nguyên trên G8 (RAM, CPU, pin, nạp model, crash)
- [ ] Phase 5: thêm metric voice
- [ ] Phase 7: so sánh base vs fine-tuned
- [ ] Phase 8: metric vision
- [ ] Phase 13: chạy lại toàn bộ trên bản tích hợp

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
