---
name: robot-task
description: Handles one scoped task or issue in the Robot repository, including phase selection, code/document changes, appropriate verification, and a concise Vietnamese handoff. Use when planning or implementing project work.
---

# Thực hiện một việc trong toàn dự án Robot

Skill này áp dụng cho mọi phase và mọi khu vực của monorepo: tài liệu/nghiên cứu, backend, AI/model, Android, voice, dữ liệu, camera/vision, ESP32/firmware, robot vật lý, dashboard, DevOps và tích hợp.

## Quy trình

1. Đọc `AGENTS.md` và `docs/HANDOFF.md`; xác định phase hiện tại từ yêu cầu mới nhất, rồi tìm đúng issue/phase trong `docs/issues/`, `docs/ROADMAP.md` và ADR cần thiết. Không giới hạn công việc ở Phase 1.
2. Xem code thực tế và `git status`. Nếu handoff cũ khác repo hiện tại, báo chênh lệch và căn cứ theo code + yêu cầu mới nhất của người dùng.
3. Viết ngắn gọn mục tiêu, phạm vi, file dự kiến tác động và cách xác nhận kết quả. Với yêu cầu “giờ làm gì?”, chỉ đưa một bước thực hành tiếp theo. Với yêu cầu thực hiện rõ ràng, làm luôn trong phạm vi đó.
4. Làm thay đổi nhỏ nhất đáp ứng yêu cầu. Không tự chuyển phase, tạo issue mới, thay kiến trúc, commit hoặc push.
5. Chỉ chạy test/kiểm tra khi người dùng yêu cầu xác minh hoặc khi đó là phần được yêu cầu trong task. Không gọi dịch vụ cloud thật hay cần secret khi không được yêu cầu.
6. Báo bằng tiếng Việt: đã thay đổi gì, file nào, kiểm tra nào thực sự chạy và kết quả, điều còn chưa xác minh, cùng đúng một bước kế tiếp nếu người dùng đang học theo từng bước.

## Khi phát hiện ngoài phạm vi

- Nếu cần quyết định kiến trúc/model/phần cứng, tóm tắt các lựa chọn và trade-off; không tự chốt thay người dùng.
- Nếu công việc thuộc phase sau, ghi rõ vì sao và chỉ làm phần độc lập cần thiết cho yêu cầu hiện tại.
- Nếu thiếu thông tin có thể suy ra từ repo, tự đọc repo trước khi hỏi. Chỉ hỏi khi thiếu dữ liệu làm thay đổi kết quả hoặc có nguy cơ sửa sai.
