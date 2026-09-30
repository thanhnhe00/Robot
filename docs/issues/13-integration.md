---
title: [Phase 13] Tích hợp cuối + End-to-End + Reliability
labels: phase-13, epic
milestone: Phase 13
---
## Mục tiêu
Toàn hệ thống chạy liền mạch; kiểm thử end-to-end và độ tin cậy.

## Vì sao cần phase này
Portfolio production-ready cần chứng minh hệ thống chạy ổn định, không chỉ từng khối.

## Phụ thuộc
Phase 1–12

## Việc cần làm
- [ ] E2E: giọng nói → STT → AI → validator → executor → hành động thật (Android và ESP32)
- [ ] Nghiệm thu offline: chế độ máy bay + các chức năng cơ bản
- [ ] Crash/reconnect: mất mạng, mất ESP32, kill app, hết pin thấp
- [ ] Soak test chạy dài (ổn định bộ nhớ, nhiệt, crash rate)
- [ ] Chạy lại toàn bộ eval/benchmark trên bản tích hợp
- [ ] Tài liệu troubleshooting

## Definition of Done
- [ ] Có báo cáo E2E + reliability
- [ ] Crash rate và các metric được ghi lại
- [ ] Kịch bản demo chạy lặp lại được

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
