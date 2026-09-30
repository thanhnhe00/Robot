---
title: [Phase 10] Motor + Safety
labels: phase-10, epic, safety, hardware
milestone: Phase 10
---
## Mục tiêu
Robot di chuyển an toàn: giới hạn tốc độ/thời gian, timeout, obstacle safety, e-stop vật lý.

## Vì sao cần phase này
An toàn không được phụ thuộc AI.

## Phụ thuộc
Phase 9

## Việc cần làm
- [ ] Thiết kế nguồn và e-stop vật lý cắt nguồn/enable motor, ưu tiên cao hơn mọi lệnh phần mềm
- [ ] Mọi lệnh motor có direction, speed limit, duration, timeout
- [ ] Dừng khi có vật cản; dừng khi mất kết nối
- [ ] Thử nghiệm ban đầu: nhấc bánh khỏi mặt đất
- [ ] Safety test phần cứng: e-stop, mất link, vật cản, lệnh vượt giới hạn
- [ ] LLM không có đường điều khiển motor trực tiếp (có test)

## Definition of Done
- [ ] Nhấn e-stop → motor dừng ngay dù phần mềm đang gửi lệnh
- [ ] Lệnh vượt giới hạn bị từ chối
- [ ] Mất liên lạc → robot dừng

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.

> **Safety:** không được bỏ qua e-stop vật lý, watchdog, giới hạn tốc độ/thời gian. LLM không điều khiển motor trực tiếp.
