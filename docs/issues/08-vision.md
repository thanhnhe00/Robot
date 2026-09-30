---
title: [Phase 8] Camera + Computer Vision
labels: phase-8, epic
milestone: Phase 8
---
## Mục tiêu
Camera → Vision → thông tin cảnh cho AI: object detection và face recognition mức prototype, tách khỏi LLM.

## Vì sao cần phase này
Robot cần "nhìn"; vision và language model có trách nhiệm riêng.

## Phụ thuộc
Phase 3 (Phase 6 cho tích hợp)

## Việc cần làm
- [ ] Pipeline camera trên G8
- [ ] Object detection: chọn model sau khi kiểm tra license và tốc độ
- [ ] Face detection + recognition + identity store
- [ ] Quyền riêng tư: dữ liệu khuôn mặt chỉ lưu local, có xóa/quản lý
- [ ] Tích hợp kết quả vision vào ngữ cảnh cho agent
- [ ] Benchmark FPS, RAM, pin

## Definition of Done
- [ ] Nhận diện được vật thể thường gặp trong nhà
- [ ] Nhận diện khuôn mặt đã đăng ký (prototype)
- [ ] Có tài liệu quyền riêng tư

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
