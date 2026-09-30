# Research C — Android và liên kết phần cứng

Ngày tra cứu: 2026-09-30. Tài liệu Android xác nhận hành vi API/OS; không xác nhận cấu hình phần cứng và chính sách firmware của chiếc LG G8 đang dùng.

## R8 — Khả năng và giới hạn Android

| Use case | Điều tài liệu xác nhận | Yêu cầu/fallback |
|---|---|---|
| Microphone nền | Android yêu cầu khai báo foreground service type `microphone` và quyền phù hợp trên các phiên bản mục tiêu mới. `RECORD_AUDIO` chịu while-in-use restriction; không khởi tạo microphone FGS tùy ý khi app ở nền/boot. | Khởi động khi app đang hiển thị, xin quyền, thông báo FGS; thử pin/OEM behavior trên G8. Không hứa “Hey Robot” luôn hoạt động sau reboot. |
| Báo thức | Android cung cấp `AlarmClock` intents để yêu cầu hệ thống đặt báo thức. | Mở/ủy quyền qua giao diện ứng dụng đồng hồ; kiểm tra app đích và hành vi trên G8. |
| Âm lượng | Android cung cấp `AudioManager` để đọc/điều khiển stream theo quyền API. | Chọn đúng stream và kiểm tra hành vi/permission trên G8; có fallback bằng UI/volume controls. |
| Wi‑Fi | Android 10+ hạn chế app thông thường bật/tắt Wi‑Fi trực tiếp. | Mở Settings panel hoặc để người dùng thao tác; không hứa bật/tắt trực tiếp. |
| Mở ứng dụng | Android cho phép khởi chạy activity qua intent khi app đích có thể được resolve. | Dùng package allowlist đã thống nhất, xử lý app không cài/không có activity; kiểm tra visibility rules theo target SDK. |

Nguồn: [Foreground service types](https://developer.android.com/develop/background-work/services/fgs/service-types), [AlarmClock](https://developer.android.com/reference/android/provider/AlarmClock), [AudioManager](https://developer.android.com/reference/android/media/AudioManager), [Android 10 privacy changes](https://developer.android.com/about/versions/10/privacy/changes), [USB Host overview](https://developer.android.com/develop/connectivity/usb/host).

## Kotlin native và Flutter

Đây là lựa chọn dự án, không có tài liệu API nào chứng minh một framework tốt hơn cho toàn bộ mục tiêu. **Đề xuất để ADR xem xét:** nghiêng Kotlin vì repo hướng tới Android APIs, foreground audio service, NDK/JNI và USB; Flutter vẫn là lựa chọn khả thi nếu nhóm muốn UI đa nền tảng và chấp nhận plugin/platform-channel cho các phần native. Chưa chốt framework cho đến khi quyết định được chấp nhận trong ADR.

## R9 — Kết nối điện thoại ↔ ESP32

- Android USB Host API cho phép app giao tiếp với thiết bị USB khi phần cứng hỗ trợ; quyền truy cập thiết bị có luồng cấp quyền của Android.
- Tài liệu Android không xác nhận LG G8 vừa làm USB host/OTG vừa sạc ổn định. Đây là kiểm tra phần cứng bắt buộc trước khi chọn USB.
- BLE/Wi‑Fi là các phương án cần so sánh ở Phase 9; chưa có số đo latency, độ tin cậy, pin hoặc nhiễu trong repo để xếp hạng.
- Trước khi quyết định: ghi model/SKU, Android/API level, kiểm tra USB host, nguồn cấp/charge-through và kết nối serial thực tế; thử BLE/Wi‑Fi trong cùng điều kiện. Giữ watchdog firmware và e-stop độc lập với đường truyền.

Nguồn: [USB Host overview](https://developer.android.com/develop/connectivity/usb/host), [USB package API](https://developer.android.com/reference/android/hardware/usb/package-summary).

## Những thông tin thiết bị còn thiếu

Xác minh trên G8: model/SKU chính xác, Android/API level, RAM khả dụng, engine TTS/STT và gói tiếng Việt, quyền chạy microphone, chính sách tiết kiệm pin, USB host, sạc đồng thời và độ ổn định cáp. Các giá trị ghi trong handoff nhưng chưa đo phải tiếp tục mang nhãn `ASSUMPTION`.
