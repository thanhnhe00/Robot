# ADR-0006: Android Kotlin + Jetpack Compose và cơ chế đồng bộ hợp đồng

Date: 2026-10-01  
Status: Accepted  

## Bối cảnh và mục tiêu

Robot sử dụng điện thoại Samsung Galaxy Z Flip5 (SM-F731B) làm "bộ não" (Android Brain). Phase 3 bắt đầu phát triển ứng dụng Android thực thi UI robot (mặt biểu cảm, state machine) và Action Executor gọi các API hệ điều hành Android cục bộ.

Tài liệu này ghi nhận 4 quyết định kỹ thuật đã chốt cho ứng dụng Android và cơ chế đồng bộ với backend.

---

## Decision

1. **Ngôn ngữ và UI Toolkit: Kotlin + Jetpack Compose**
   - Sử dụng Kotlin hiện đại cùng Jetpack Compose làm nền tảng chính cho ứng dụng Android.
   - Không sử dụng Flutter hay React Native.

2. **Cơ chế đồng bộ luật Kotlin ↔ Python: Test vector JSON dùng chung + Contract test**
   - Sử dụng chung một file test vector JSON (`ai/schemas/action_vectors.json`) cho cả hai bộ validator: bộ kiểm tra Python ở backend (`backend/app/action_registry.py`) và bộ kiểm tra Kotlin trên Android app (`domain/action/ActionValidator.kt`).
   - Cả hai phía (Pytest và JVM JUnit) cùng đọc chung file vector này trong quá trình test tự động.
   - Thêm Contract test đối chiếu danh sách action, tham số, whitelist package, timeout và giới hạn giá trị giữa mã nguồn Kotlin và `ai/schemas/action_schema.json`.
   - **Chưa** dựng bộ công cụ sinh mã nguồn tự động (code generator) từ JSON Schema ở Phase 3.

3. **Môi trường kết nối phát triển (Dev Environment): `adb reverse`**
   - Khi phát triển và gỡ lỗi qua cáp USB, app Android kết nối tới backend FastAPI (chạy trên laptop) thông qua `http://127.0.0.1:8000` nhờ lệnh:
     ```bash
     adb reverse tcp:8000 tcp:8000
     ```
   - Backend tiếp tục duy trì cấu hình bind an toàn vào `127.0.0.1`, không mở port ra toàn mạng LAN cục bộ.
   - Chỉ mở port ra LAN khi có yêu cầu bắt buộc, và khi đó phải cấu hình `API_KEY` bảo vệ.

4. **Phiên bản SDK và Thiết bị mục tiêu:**
   - **Thiết bị đích:** Samsung Galaxy Z Flip5 (SM-F731B, Snapdragon 8 Gen 2 for Galaxy, RAM 8GB, 512GB ROM), chạy **Android 15 (API 35), One UI 7**.
   - `compileSdk = 35`
   - `targetSdk = 35`
   - `minSdk = 33` (`ASSUMPTION`: Z Flip5 ra mắt với Android 13 / API 33; dự án chỉ nhắm tới một thiết bị đích duy nhất, không cần tương thích ngược Android 12 trở xuống).
   - **Màn hình hỗ trợ:** Màn hình chính gập (main display). Giao diện Flex mode cần kiểm tra; giao diện màn hình phụ (cover screen) thuộc backlog, chưa làm ở Phase 3.

---

## Why

1. **Vì sao chọn Kotlin + Jetpack Compose:**
   - **Khả năng tích hợp hệ thống sâu:** Dự án cần chạy Foreground Service cho microphone nền (Phase 5), giao tiếp USB Host / BLE trực tiếp tới ESP32 (Phase 9), và gọi trực tiếp C/C++ runtime qua JNI/NDK cho mô hình LLM local (Phase 4). Các framework đa nền tảng (Flutter / React Native) buộc phải viết thêm lớp cầu nối (Platform Channels / JSI) phức tạp, gây overhead và khó gỡ lỗi tầng thấp.
   - **Jetpack Compose:** Cho phép xây dựng giao diện biểu cảm robot (`RobotFace` Canvas animation) mượt mà, phản ứng linh hoạt theo State Machine mà không chịu gánh nặng phân mảnh view truyền thống.
   - **Đội ngũ và mục tiêu:** Phù hợp với năng lực kỹ thuật và hồ sơ năng lực (portfolio) kỹ sư phần mềm chuẩn chỉ.

2. **Vì sao dùng Test vector dùng chung thay vì sinh code tự động ngay:**
   - Số lượng action ở Phase 3 còn nhỏ gọn (5 action: `get_time`, `get_battery`, `set_alarm`, `open_app`, `set_volume`, và 2 stub: `move`, `stop`).
   - Việc thiết lập parser sinh mã Kotlin (`quicktype` hoặc custom template) ở thời điểm này gây phát sinh phụ thuộc công cụ build phức tạp trong khi schema vẫn đang hoàn thiện.
   - Dùng chung file vector kiểm thử JSON (`action_vectors.json`) đảm bảo tính tương đương 100% về hành vi và góc cạnh (edge cases: khoảng trắng, null, số thực, số vượt ngưỡng) giữa Python và Kotlin mà vẫn giữ code sạch, dễ đọc.

3. **Vì sao dùng `adb reverse`:**
   - Tránh việc mở backend ra địa chỉ IP LAN (rủi ro bảo mật trong mạng dùng chung).
   - Không bị ảnh hưởng bởi việc đổi địa chỉ IP Wi-Fi của laptop khi di chuyển.
   - Cho phép app trên điện thoại dùng thẳng URL `http://127.0.0.1:8000` nhất quán với cấu hình bảo mật `network_security_config`.

---

## Alternatives

1. **Flutter / React Native:**
   - *Ưu điểm:* Viết UI nhanh, có thể mang sang iOS.
   - *Lý do từ chối:* Robot chạy trên phần cứng vật lý chuyên biệt (Samsung Z Flip5), không có nhu cầu iOS. Cần can thiệp sâu vào audio recording while-in-use, NDK runtime (`llama.cpp`), USB serial OTG mà Flutter không đem lại lợi thế rõ rệt nào ngoài việc tăng thêm lớp abstraction.

2. **Xây dựng công cụ Code Generation sinh mã Kotlin từ JSON Schema ngay lập tức:**
   - *Ưu điểm:* Tự động hóa hoàn toàn việc tạo data class.
   - *Lý do từ chối:* Quá sớm cho giai đoạn prototype hiện tại; việc sinh sealed classes cho polymorphic action cần tinh chỉnh thủ công để compiler Kotlin hỗ trợ kiểm tra kiểu chặt chẽ trong `ActionExecutor`.

3. **Hai bộ luật viết tay riêng cho Kotlin và Python mà không có test vector chung:**
   - *Ưu điểm:* Nhanh, không cần phối hợp file test.
   - *Lý do từ chối:* Rất dễ dẫn tới lệch pha (drift) giữa app và backend (ví dụ: cách xử lý `params: null`, cắt chuỗi label, hoặc parse số `1e999`).

---

## Trade-offs

- **Đánh đổi khi không sinh code:** Khi cập nhật thêm action mới vào schema, lập trình viên phải cập nhật bằng tay ở cả hai phía (Python và Kotlin). Đổi lại, test vector chung sẽ bắt lỗi ngay lập tức trong CI nếu hai phía không ăn khớp.
- **minSdk 33:** Ứng dụng không thể cài trên các thiết bị Android cũ hơn API 33. Đây là chủ đích thiết kế vì chỉ phục vụ một máy Samsung Galaxy Z Flip5 duy nhất.
- **targetSdk 35:** Bắt buộc phải xử lý giao diện tràn viền (Edge-to-edge) và quản lý chặt chẽ Foreground Service.

---

## Điều kiện xem lại (Review Conditions)

Quyết định về cơ chế đồng bộ luật (mục 2) sẽ được đánh giá và xem xét lại khi:
1. Xuất hiện **bên tiêu thụ thứ ba (consumer thứ ba)** cần chia sẻ luật hợp lệ:
   - Firmware ESP32 ở Phase 9 (C/C++ parser/validator).
   - Dashboard quản trị ở Phase 12 (Web frontend).
2. Hoặc khi số lượng action trong registry mở rộng vượt quá 15 action với cấu trúc tham số phức tạp nhiều tầng.

---

## Bằng chứng và Nguồn tra cứu chính thức

1. **Android 15 (API level 35) Edge-to-edge enforcement:**
   - Khi app đặt `targetSdk = 35`, chế độ hiển thị Edge-to-edge được hệ thống bật mặc định và bắt buộc (các thanh hệ thống Status Bar và Navigation Bar trở nên trong suốt/bán trong suốt, cửa sổ ứng dụng vẽ tràn xuống dưới các thanh này). Ứng dụng phải chủ động xử lý `WindowInsets` / `WindowInsetsCompat` / Compose padding để tránh che khuất nội dung điều khiển.
   - Nguồn: [Android Developers - Behavior changes: Apps targeting Android 15 (Edge-to-edge enforcement)](https://developer.android.com/about/versions/15/behavior-changes-15#edge-to-edge) (tra cứu ngày 2026-10-01).

2. **Android Foreground Service Type Microphone:**
   - Android 14 (API 34) và Android 15 (API 35) tiếp tục áp dụng quy định nghiêm ngặt về Foreground Service (FGS) loại `microphone`: bắt buộc khai báo service type trong manifest, quyền `RECORD_AUDIO` bị giới hạn trong thời gian sử dụng (while-in-use), không được tự ý khởi chạy microphone FGS khi app ở chế độ chạy ngầm hoàn toàn.
   - Nguồn: [Android Developers - Foreground service types: Microphone](https://developer.android.com/develop/background-work/services/fgs/service-types#microphone) (tra cứu ngày 2026-10-01).

3. **Thông số Samsung Galaxy Z Flip5 (SM-F731B):**
   - Thiết bị xuất xưởng với Android 13 (API level 33), One UI 5.1.1. Hiện đã được cập nhật lên Android 15 (API level 35), One UI 7. Việc chọn `minSdk = 33` đảm bảo app tương thích ngay từ phiên bản gốc của dòng máy này.
   - Nguồn: Thông số công bố của Samsung và cấu hình thiết bị thực tế do chủ dự án cung cấp.
