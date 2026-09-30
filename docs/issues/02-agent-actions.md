---
title: "[Phase 2] Agent + Action System + Evaluation v0"
labels: phase-2, epic
milestone: Phase 2
---
## Mục tiêu
Action registry có schema/permission/timeout/executor/log; safety pipeline; JSON Schema dùng chung (ADR-0001); bộ đánh giá đầu tiên và **golden test set** độc lập model.

## Vì sao cần phase này
LLM chỉ đề xuất; phải có lớp kiểm tra trước khi thực thi. Eval phải có từ sớm để mọi thay đổi sau này đo được.

## Phụ thuộc
Phase 1

## Việc cần làm
- [x] JSON Schema chung cho action (`ai/schemas/action_schema.json`); sinh model Pydantic (kế hoạch sinh Kotlin cho Phase 3)
- [x] Action registry: get_time, get_battery, set_alarm, open_app, set_volume (+ khung cho move/stop, chưa nối phần cứng)
- [x] Xử lý JSON sai: validate → repair/retry → không execute → fallback "Xin lỗi, mình chưa hiểu yêu cầu đó."
- [x] Test an toàn (mục 58): action hợp lệ/không hợp lệ, thiếu/sai kiểu tham số, action lạ, ngoài phạm vi, không có quyền, JSON lỗi, tool bịa, timeout
- [x] Golden test set ~120 mẫu (kiểm tay, KHÔNG dùng để train) + `ai/datasets/README.md` (nguồn, cách tạo)
- [x] Eval harness v0: JSON validity, action accuracy, intent accuracy, tool selection accuracy; kết quả lưu ở `docs/benchmarks/`

## Definition of Done
- [x] Action ngoài whitelist luôn bị từ chối (có test)
- [x] Eval chạy bằng 1 lệnh, cho ra bảng số liệu
- [x] Golden set tách khỏi tập train

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
