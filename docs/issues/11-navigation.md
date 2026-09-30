---
title: [Phase 11] Navigation
labels: phase-11, epic, safety, hardware
milestone: Phase 11
---
## Mục tiêu
Né vật cản, đi theo người: Camera → Person detection → Tracking → Navigation → Motor controller.

## Vì sao cần phase này
Robot di chuyển quanh nhà; LLM chỉ ở tầng suy luận cấp cao.

## Phụ thuộc
Phase 8, Phase 10

## Việc cần làm
- [ ] Né vật cản bằng sensor
- [ ] Person following: detection → tracking → controller
- [ ] Nghiên cứu (chưa cam kết) localization/SLAM ở mức phù hợp
- [ ] Giới hạn an toàn khi tự di chuyển
- [ ] Test trong môi trường kiểm soát

## Definition of Done
- [ ] Robot đi theo người trong khoảng cách an toàn và dừng khi mất người/có vật cản
- [ ] Không có đường LLM → motor

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.

> **Safety:** không được bỏ qua e-stop vật lý, watchdog, giới hạn tốc độ/thời gian. LLM không điều khiển motor trực tiếp.
