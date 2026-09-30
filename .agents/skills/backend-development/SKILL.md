---
name: backend-development
description: Works on Robot's Python backend across project phases, including API gateway, provider integrations, agent/action services, memory/tools, dashboard APIs, reliability, and backend CI. Use for backend implementation or review.
---

# Phát triển backend Robot

## Phạm vi

- Skill này dành cho phần backend trong toàn bộ repo, không chỉ Phase 1. Đọc roadmap và issue để biết backend đang hỗ trợ mục tiêu phase nào.
- Trước khi sửa, đọc handoff, kiến trúc, issue liên quan và code hiện tại trong `backend/` hoặc thành phần server liên quan. Không giả định trạng thái từ tài liệu nếu code khác.
- Không kéo chức năng Android, firmware, dashboard hay hạ tầng vào backend nếu issue không yêu cầu.

## Cách làm

1. Theo module, provider abstraction, schema và API contract hiện hữu; không thay contract âm thầm.
2. Phân biệt lỗi cấu hình, lỗi kết nối provider, output không hợp lệ và lỗi request. Không trả stack trace, secret hoặc prompt nhạy cảm cho client.
3. Provider Mock phải được ghi rõ là mock; không dùng phản hồi giả để tuyên bố tích hợp model thật thành công.
4. Bảo vệ API key và nội dung hội thoại trong log. Không log secret; không chuyển dữ liệu `LOCAL ONLY` tới provider cloud.
5. Tránh thêm dependency khi thư viện chuẩn hoặc dependency đã có đáp ứng được.
6. Khi task yêu cầu test, dùng cách chạy trong tài liệu/cấu hình hiện hành. Không gọi dịch vụ model thật nếu chưa được yêu cầu và cấu hình rõ.
7. Chỉ kiểm tra Docker khi Docker daemon khả dụng. Báo rõ giới hạn môi trường nếu không chạy được.
8. Không tự sửa lỗi lint có trước hoặc tái định dạng toàn repo nếu chúng không thuộc yêu cầu.

## Báo cáo

Nêu thành phần/API/hành vi thay đổi, file chính, kiểm tra thực sự chạy, kết quả và phần cần xác minh ở môi trường khác.
