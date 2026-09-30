---
title: [Phase 12] Dashboard + DevOps hoàn thiện + Model update
labels: phase-12, epic
milestone: Phase 12
---
## Mục tiêu
Dashboard theo dõi robot; CI/CD đầy đủ; cơ chế cập nhật model an toàn.

## Vì sao cần phase này
Quan sát được hệ thống và phát hành ổn định.

## Phụ thuộc
Phase 6 trở đi

## Việc cần làm
- [ ] Dashboard API + UI: online/offline, pin, CPU/RAM, model, latency, hội thoại, action log, lỗi, trạng thái ESP32/sensor, telemetry
- [ ] CI: thêm build Android (khi ổn định), API tests, Docker build
- [ ] Model update: version, checksum, rollback, kiểm tra dung lượng và tương thích
- [ ] Metrics và cảnh báo cơ bản

## Definition of Done
- [ ] Xem được trạng thái robot theo thời gian thực
- [ ] Cập nhật model có rollback
- [ ] CI kiểm tra đủ các tầng

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
