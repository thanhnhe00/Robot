---
title: [Phase 3] Android app (Robot UI + Action Executor)
labels: phase-3, epic
milestone: Phase 3
---
## Mục tiêu
App Android: mặt robot, state machine, Action Executor qua API chính thức, gọi backend, nhập bằng text. Có sẵn interface `AIProvider` phía Android để Phase 4 gắn model local.

## Vì sao cần phase này
App là giao diện chính; validator/executor phải nằm trên Android để chạy offline (ADR-0001).

## Phụ thuộc
Phase 2, research nhóm C

## Việc cần làm
- [ ] Project Android theo framework chốt ở Phase 0 (nghiêng Kotlin + Compose)
- [ ] State machine IDLE → WAKE → LISTENING → PROCESSING → SPEAKING (+ ERROR) và animation mặt robot
- [ ] Validator + Executor: get_time, get_battery, set_alarm, open_app (whitelist package), set_volume
- [ ] Client REST tới backend; MockProvider để chạy không cần mạng
- [ ] Unit test cho validator/executor; test thủ công trên Samsung Galaxy Z Flip5

## Definition of Done
- [ ] Nhập text → robot phản hồi và thực thi action trên Samsung Galaxy Z Flip5
- [ ] Action ngoài whitelist bị từ chối trên app
- [ ] Có test cho validator

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
