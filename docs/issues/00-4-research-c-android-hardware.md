---
title: [Phase 0] 0.4 Research nhóm C: giới hạn Android và liên kết phone-ESP32
labels: phase-0, research, hardware
milestone: Phase 0
---
## Mục tiêu
Trả lời R8–R9; đủ thông tin để chọn framework Android và định hướng liên kết với ESP32.

## Việc cần làm
- [x] R8: Ghi giới hạn API/OS và fallback cho microphone nền, AlarmClock, âm lượng, Wi‑Fi và mở app.
- [x] So sánh Kotlin/Flutter theo yêu cầu tích hợp; ghi đây là đề xuất dự án, chưa phải quyết định.
- [x] R9: Tra USB Host API; ghi rõ tài liệu không kết luận OTG+sạc trên Z Flip5; để kiểm tra serial/USB/BLE/Wi‑Fi thực tế ở Phase 9.
- [x] Viết `docs/research/C-android-hardware.md` với nguồn chính thức và ngày tra cứu.
- [x] Ghi hướng Kotlin/USB là Proposed và các kiểm tra thiết bị trong ADR-0005; không chốt liên kết ESP32 trước Phase 9.

## Definition of Done
- [x] Có danh sách khả năng API và fallback; các hành vi phụ thuộc Z Flip5 được gắn nhãn cần kiểm tra.
- [x] Có ADR Proposed cho framework Android và ghi nhận các lựa chọn kết nối.
- [x] R8–R9 hoàn tất nghiên cứu tài liệu; kiểm tra máy/phần cứng được lên kế hoạch cho Phase 3/9.
