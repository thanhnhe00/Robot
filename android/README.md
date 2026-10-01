# Robot Android Application

Ứng dụng Android đóng vai trò "Bộ não" (Brain) của Robot trên Samsung Galaxy Z Flip5 (SM-F731B), chạy Android 15 (API 35), One UI 7.
Giao diện xây dựng bằng **Jetpack Compose**, logic tầng `domain/` là Kotlin thuần chạy JVM test không cần giả lập thiết bị.

---

## 1. Yêu cầu môi trường

- **Hệ điều hành:** Windows / Linux / macOS.
- **IDE:** Android Studio (phiên bản khuyến nghị: Ladybug trở lên).
- **JDK:** Java 21 hoặc 17 (Temurin hoặc Android Studio default JBR).
- **Android SDK:**
  - `compileSdk = 35`
  - `targetSdk = 35`
  - `minSdk = 33`
- **Thiết bị:** Samsung Galaxy Z Flip5 (SM-F731B) đã bật **Developer Options** và **USB Debugging**.

---

## 2. Kết nối với Backend khi phát triển

Backend FastAPI chạy trên laptop ở cổng `8000`. Để ứng dụng Android trên điện thoại kết nối được với backend qua cáp USB mà không cần mở IP ra toàn mạng LAN, sử dụng lệnh chuyển tiếp cổng qua `adb`:

```bash
adb reverse tcp:8000 tcp:8000
```

Khi đó, app Android kết nối tới:
```
http://127.0.0.1:8000
```

---

## 3. Cấu hình biến môi trường (`local.properties`)

File `local.properties` được Git bỏ qua (`.gitignore`) để tránh lộ thông tin và cấu hình máy dev.
Nếu cần cấu hình backend URL hoặc API key tùy biến, thêm các dòng sau vào file `android/local.properties`:

```properties
ROBOT_BACKEND_URL="http://127.0.0.1:8000"
ROBOT_API_KEY=""
```

> **Lưu ý:** Tuyệt đối không commit file `local.properties` hoặc bất kỳ khóa API key nào vào Git.

---

## 4. Lệnh kiểm thử và build

Thực hiện trong thư mục `android/`:

- **Chạy Unit Test trên JVM:**
  ```bash
  ./gradlew testDebugUnitTest
  ```
- **Kiểm tra Lint:**
  ```bash
  ./gradlew lintDebug
  ```
- **Build APK Debug:**
  ```bash
  ./gradlew assembleDebug
  ```
- **Cài đặt trực tiếp lên điện thoại:**
  ```bash
  ./gradlew installDebug
  ```

---

## 5. Kiến trúc & Tính năng (Phase 3)

- **Giao diện khuôn mặt Robot (RobotFace):** Vẽ bằng Canvas Jetpack Compose phong cách Cyber-Organic, có chớp mắt tự nhiên, đảo mắt suy nghĩ, cong mắt tươi cười và miệng sóng âm thanh neon mấp máy khi nói.
- **State Machine:** Vòng đời `IDLE` → `PROCESSING` → `SPEAKING` → `IDLE` / `ERROR` xử lý qua hàm thuần `reduce()`.
- **Thẩm định an toàn (ActionValidator):** Kiểm tra schema, whitelist 4 app, giới hạn tham số, STUB độc lập ngay trên máy không cần mạng.
- **Cơ chế duyệt hành động:** Mọi action hệ thống đều hiện hộp thoại hỏi duyệt trước khi `ActionExecutor` thực thi trên điện thoại.
- **3 AIProvider:**
  - `BackendProvider`: Gọi FastAPI qua cổng đảo chiều ADB `127.0.0.1:8000`.
  - `MockProvider`: Chạy offline 100% bằng luật xác định tiếng Việt.
  - `DebugScriptedProvider`: Thử nghiệm 6 kịch bản vi phạm an toàn.

