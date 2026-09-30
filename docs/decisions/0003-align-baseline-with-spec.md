# ADR-0003: Đồng bộ baseline backend với spec
Date: 2026-09-29
Status: Accepted

Decision:
- Trường câu trả lời: `reply` → `response`.
- `open_app` dùng `params.package` (package name Android) thay cho `name`, whitelist theo package.
- Cấu trúc repo theo mục 61 (thư mục `ai/` thay cho `model/`, thêm `dashboard/`, `scripts/`, `tests/`, `docker/`, `firmware/esp32/`).

Why:
Khớp spec dự án Robot để các phase sau không phải đổi hợp đồng dữ liệu.

Alternatives:
Giữ hợp đồng cũ và sửa sau.

Trade-offs:
Package name khác nhau theo hãng máy (camera, đồng hồ, máy tính), nên whitelist mặc định chỉ có YouTube, Chrome, Cài đặt, Spotify. Các app hệ thống dùng intent chuẩn phía Android (ASSUMPTION – kiểm tra ở Phase 3).

Chosen:
Áp dụng ngay (baseline đã cập nhật, 12 test qua).
