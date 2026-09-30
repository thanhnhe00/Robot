---
title: [Phase 0] 0.4 Research nhóm C: giới hạn Android và liên kết phone-ESP32
labels: phase-0, research, hardware
milestone: Phase 0
---
## Mục tiêu
Trả lời R8–R9; đủ thông tin để chọn framework Android và định hướng liên kết với ESP32.

## Việc cần làm
- [ ] R8: Giới hạn Android theo tài liệu chính thức: mic nền (foreground service), báo thức (`AlarmClock` intent), âm lượng, WiFi (hạn chế từ Android 10), mở app, quyền cần khai báo
- [ ] So sánh Kotlin native vs Flutter theo tiêu chí mục 20 (local AI, audio, camera, service, JNI/NDK, portfolio)
- [ ] R9: USB host + sạc cùng lúc, thư viện serial cho Android, so với BLE/WiFi (độ trễ, độ tin cậy, pin, độ phức tạp)
- [ ] Viết `docs/research/C-android-hardware.md` (nguồn + ngày)
- [ ] ADR (Proposed): framework Android; ghi nhận hướng liên kết ESP32 (quyết định cuối ở Phase 9)

## Definition of Done
- [ ] Danh sách action Android nào làm được / không làm được / cần fallback
- [ ] Có ADR Proposed cho framework Android
- [ ] R8–R9 chuyển `DONE`
