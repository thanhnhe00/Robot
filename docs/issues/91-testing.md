---
title: [Cross-cutting] Test và Reliability xuyên suốt
labels: cross-cutting, testing, epic
milestone: Cross-cutting
---
## Mục tiêu
Mỗi phase có test; Phase 13 tổng hợp end-to-end và reliability.

## Loại test và phase bắt đầu
| Loại | Bắt đầu |
|---|---|
| Unit test | Phase 1 |
| API test / Integration test | Phase 1 |
| Agent/Action test (mục 58) | Phase 2 |
| Android test | Phase 3 |
| Model/Eval test | Phase 2, mở rộng Phase 4 |
| ESP32 communication test | Phase 9 |
| Safety test phần cứng (e-stop, mất link, vật cản) | Phase 10 |
| End-to-End | Phase 13 |
| Crash/reconnect/soak | Phase 9 (link), Phase 13 (toàn hệ thống) |

## Việc cần làm
- [ ] Quy ước cấu trúc test (`backend/tests`, `tests/` cho tích hợp, test Android)
- [ ] Chạy test trong CI ở mọi PR
- [ ] Tài liệu `docs/TESTING.md`

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
