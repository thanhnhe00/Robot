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

## Việc đã làm
- [x] Project Android theo framework chốt ở Phase 0: Kotlin DSL + Jetpack Compose, compileSdk 35, targetSdk 35, minSdk 33 (ADR-0006).
- [x] State machine IDLE → WAKE → LISTENING → PROCESSING → SPEAKING (+ ERROR) với hàm thuần `reduce()`; bảo đảm WAKE/LISTENING không thể chạm tới ở Phase 3.
- [x] Animation mặt robot sinh động vẽ bằng Compose Canvas phong cách Cyber-Organic: mắt chớp tự nhiên, đảo mắt suy nghĩ, cong mắt tươi cười, miệng sóng âm thanh neon động.
- [x] Thẩm định an toàn (ActionValidator) độc lập trên Android: chạy offline, kiểm tra 47 test vectors chung với backend Python (`action_vectors.json`), contract test đối chiếu `action_schema.json`.
- [x] Thực thi hệ điều hành (ActionExecutor): get_time, get_battery, set_alarm, open_app (whitelist 4 package với `<queries>`), set_volume; xử lý timeout và exceptions.
- [x] Hộp thoại duyệt hành động: Mọi action đều phải qua xác nhận trực tiếp của người dùng trước khi chạm tới hệ điều hành.
- [x] Nguồn AI (AIProvider): BackendProvider kết nối FastAPI qua ADB reverse (`127.0.0.1:8000`), MockProvider chạy offline bằng luật xác định tiếng Việt, DebugScriptedProvider kiểm thử an toàn.
- [x] Unit tests đầy đủ: 9 test suites (49 unit tests JVM) đạt 100% pass; lintDebug đạt 0 lỗi.
- [x] Cài đặt và chạy thực tế thành công trên Samsung Galaxy Z Flip5 (Android 15, One UI 7.0).

## Definition of Done
- [x] Nhập text → robot phản hồi và thực thi action trên Samsung Galaxy Z Flip5 (có bằng chứng hình ảnh và log thực tế).
- [x] Action ngoài whitelist bị từ chối trên app (kiểm chứng bằng 6 ca thử nghiệm DebugScriptedProvider).
- [x] Có test cho validator (47 test vectors khớp 100% giữa Python và Kotlin).
- [x] Biên bản kiểm thử nghiệm thu lập tại `docs/benchmarks/phase3-android-manual-test.md`.

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
