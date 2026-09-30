# Hướng dẫn cho AI agent của dự án Robot

## Bắt đầu một việc mới

- Dùng repo `Robot` làm nguồn sự thật về sản phẩm. Đọc `docs/HANDOFF.md` để biết trạng thái gần nhất; sau đó chỉ đọc phần liên quan trong `docs/ARCHITECTURE.md`, `docs/ROADMAP.md`, `docs/issues/` và `docs/decisions/`.
- Kiểm tra code, test và Git hiện tại trước khi kết luận một việc đã làm hay chưa. Nếu tài liệu và repo không khớp, nêu rõ điểm lệch; không âm thầm coi tài liệu cũ là trạng thái code.
- Bộ hướng dẫn này áp dụng cho toàn repo và mọi phase. Đọc handoff/roadmap cùng yêu cầu mới nhất để xác định phase đang làm; trạng thái phase ghi trong handoff có thể thay đổi và không phải giới hạn của bộ rule.
- Làm một issue hoặc một mục tiêu nhỏ mỗi lần. Với câu hỏi “giờ làm gì?”, hướng dẫn đúng bước kế tiếp bằng tiếng Việt đơn giản. Khi người dùng yêu cầu thực hiện cụ thể, tiến hành phần việc đã yêu cầu, không biến nó thành chuỗi câu hỏi xác nhận.

## Nguyên tắc dự án

- Trả lời và tài liệu hướng dẫn bằng tiếng Việt; tên biến, hàm, API và định danh trong code bằng tiếng Anh.
- Giữ thay đổi nhỏ, dễ hiểu, khớp kiến trúc hiện có. Không dựng lại baseline hay thêm framework, dịch vụ, abstraction hoặc tính năng ngoài phạm vi issue.
- LLM chỉ đề xuất action. Mọi action phải qua schema, whitelist, kiểm tra quyền/an toàn và executor phù hợp; đầu ra lỗi hoặc không rõ thì không thực thi.
- Không để LLM điều khiển motor/phần cứng trực tiếp. Lệnh motor cần giới hạn và timeout; ESP32 phải có watchdog khi mất kết nối và robot phải có nút dừng khẩn cấp vật lý.
- Không gửi dữ liệu `LOCAL ONLY` lên cloud. Không ghi API key, mật khẩu, dữ liệu riêng tư hoặc model weights vào Git.
- Không khẳng định model, engine hay thiết bị chạy tốt nếu chưa có benchmark thật. Gắn nhãn `CHƯA BIẾT`, `CẦN RESEARCH` hoặc `ASSUMPTION` khi cần.
- Quyết định kiến trúc/model/phần cứng quan trọng cần ghi trong ADR theo mẫu hiện có ở `docs/decisions/`.
- Không tự commit, push, tạo release hay triển khai nếu người dùng chưa yêu cầu.

## Antigravity

- Đây là cấu hình workspace cho Antigravity IDE độc lập: rules ở `.agents/rules/`, skills ở `.agents/skills/`, workflow tương thích cũ ở `.agents/workflows/`.
- `.vscode/` là cấu hình phụ trợ cho VS Code; không coi đó là cấu hình agent của Antigravity và không yêu cầu cài extension Antigravity vào Antigravity IDE.
- Với coding/research task ở bất kỳ thành phần hoặc phase nào, dùng skill phù hợp trong `.agents/skills/`. Workflow `/start-robot-issue` chỉ là lối gọi nhanh trong thời gian Antigravity còn hỗ trợ workflows.
