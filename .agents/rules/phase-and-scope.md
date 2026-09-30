# Phase và phạm vi công việc

- Trước khi làm, xác định yêu cầu thuộc phase nào dựa trên `docs/HANDOFF.md`, `docs/ROADMAP.md` và issue liên quan.
- Bộ rule này áp dụng cho toàn bộ vòng đời dự án Robot, Phase 0 đến Phase 14 và mọi thành phần trong repo. Handoff cho biết trạng thái gần nhất, nhưng không khóa agent vào một phase cụ thể.
- Xác định phase theo yêu cầu mới nhất của người dùng và roadmap/handoff hiện tại. Nếu người dùng đổi hướng, cập nhật phạm vi theo chỉ đạo mới.
- Không kéo việc của phase khác vào thay đổi hiện tại. Chỉ làm các thành phần cần cho issue, dù chúng thuộc backend, Android, model/AI, voice, dữ liệu, camera, firmware, robot vật lý, dashboard hay tích hợp.
- Một issue/mục tiêu mỗi lượt. Nếu người dùng chỉ hỏi bước tiếp theo, đưa đúng một hành động cụ thể, chỉ rõ file/thư mục hoặc lệnh cần dùng.
- Giữ nguyên thay đổi chưa commit của người dùng. Trước khi sửa, xem trạng thái Git và tránh ghi đè nội dung có sẵn.
- Nếu phát hiện yêu cầu mâu thuẫn với quyết định đã duyệt hoặc làm thay đổi kiến trúc, giải thích điểm mâu thuẫn và phương án trước khi triển khai phần mâu thuẫn.
