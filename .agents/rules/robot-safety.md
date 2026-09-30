# An toàn AI và robot

- LLM chỉ tạo đề xuất có cấu trúc; không coi văn bản/JSON từ model là lệnh đã được cho phép.
- Chỉ thực thi action có trong whitelist. Kiểm tra schema, giá trị tham số, quyền Android và điều kiện an toàn tại executor; từ chối action lạ hoặc dữ liệu sai.
- Không chạy action khi parse/validation thất bại. Có thể thử sửa định dạng có giới hạn; nếu vẫn lỗi thì trả lời an toàn và không thực thi.
- Không dùng LLM cho dữ liệu hệ thống xác định được, như giờ hiện tại hoặc trạng thái pin khi hệ điều hành có API tương ứng.
- Không để backend/cloud là lớp bảo vệ duy nhất. Luồng xác thực và executor trên thiết bị phải có kiểm tra độc lập, phù hợp với kiến trúc đã duyệt.
- Không cho model gọi motor trực tiếp. Mọi lệnh chuyển động phải có hướng, giới hạn tốc độ, thời lượng, timeout và kiểm tra vật cản theo thiết kế; mất kết nối phải dừng qua watchdog firmware. Nút dừng khẩn cấp vật lý luôn ưu tiên hơn phần mềm.
- Không nới whitelist, quyền, timeout hay giới hạn an toàn chỉ để làm test/demo chạy qua.
