---
name: embedded-robot-safety
description: Designs or reviews Robot ESP32 firmware, phone-to-controller communication, motors, sensors, obstacle handling, navigation, and physical safety. Use for firmware or robot-hardware tasks.
---

# Firmware và an toàn robot

1. Đọc kiến trúc, ADR, issue và code/firmware thực tế trước khi sửa. Đánh dấu rõ phần cứng chưa có hoặc chưa được kiểm tra.
2. Lệnh từ AI/điện thoại là dữ liệu không tin cậy. Firmware phải kiểm tra whitelist, hướng, giới hạn tốc độ, thời lượng, timeout và trạng thái kết nối trước khi điều khiển cơ cấu chấp hành.
3. Watchdog firmware phải đưa robot về trạng thái dừng an toàn khi mất liên lạc hoặc lệnh hết hạn. E-stop vật lý phải ưu tiên hơn mọi lệnh phần mềm.
4. Không dùng LLM để quyết định trực tiếp xung motor, PWM hoặc hành vi phản xạ an toàn. Các vòng dừng/giới hạn phải deterministic và chạy trên firmware/phần cứng phù hợp.
5. Mọi mô phỏng/mock phải ghi rõ là mô phỏng/mock; không báo đã xác nhận an toàn phần cứng nếu chưa thử với cấu hình thật.
6. Nếu thay đổi có thể làm motor chuyển động ngoài ý muốn, trước tiên đề xuất cách thử an toàn không tải/giới hạn; không tự kích hoạt phần cứng.
7. Khi xác minh, kiểm tra timeout, mất kết nối, lệnh sai/ngoài giới hạn, khởi động lại và E-stop theo khả năng môi trường. Báo các tình huống chưa kiểm tra.
