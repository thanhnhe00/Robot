# Biên bản Kiểm thử Nghiệm thu Phase 3 trên Samsung Galaxy Z Flip5

> Ngày lập: 2026-10-01  
> Tài liệu thuộc Phase 3: Android App (Robot UI + Action Executor)  
> Phương pháp: Kiểm thử hộp đen thực tế trên thiết bị phần cứng thật kết nối qua ADB.

---

## 1. Thông số thiết bị kiểm thử thực tế

| Thông số | Giá trị thực tế ghi nhận qua ADB | Ghi chú |
|---|---|---|
| Thiết bị | **Samsung Galaxy Z Flip5** | Dòng máy gập cao cấp của Samsung |
| Mã Model (`ro.product.model`) | `SM-F731B` | Bản quốc tế |
| Mã định danh ADB | `R5CW72WTDDR` | Cắm trực tiếp qua cáp USB |
| Phiên bản Android (`ro.build.version.release`) | **Android 15** | compileSdk 35, targetSdk 35, minSdk 33 |
| API Level (`ro.build.version.sdk`) | **API 35** | Hỗ trợ edge-to-edge và insets |
| Phiên bản One UI (`ro.build.version.oneui`) | **One UI 7.0** (`70000`) | Giao diện mới nhất của Samsung |
| Tần số quét màn hình | 120 Hz Dynamic AMOLED 2X | Màn hình chính gập |
| Độ phân giải màn hình | 1080 x 2640 px | Tỷ lệ 22:9 |

---

## 2. Bảng ca kiểm thử nghiệm thu (Manual Test Matrix)

> **Hướng dẫn người dùng:** Các cột **Kết quả thực tế** và **Đạt?** được để trống để bạn tự trải nghiệm trên điện thoại và điền xác nhận (`P` = Pass / Đạt, `F` = Fail / Không đạt).

| STT | Nhóm | Ca kiểm thử | Thao tác thực hiện | Kết quả kỳ vọng | Kết quả thực tế (Người dùng điền) | Đạt? (P/F) | Ghi chú |
|:---:|---|---|---|---|---|:---:|---|
| **1** | Action | Xem giờ hệ thống (`get_time`) | Chọn Mock → Bấm nút `Mấy giờ rồi?` → Bấm `Duyệt thực thi` | Robot nói giờ thực tế hệ thống (vd: "Bây giờ là 16:10.") | | | API Android chính thức, không hỏi model |
| **2** | Action | Đọc mức pin (`get_battery`) | Chọn Mock → Bấm nút `Mức pin?` → Bấm `Duyệt thực thi` | Robot nói mức pin thực tế (vd: "Pin hiện tại còn 64%.") | | | Đọc từ `BatteryManager` |
| **3** | Action | Đặt báo thức 07:30 (`set_alarm`) | Chọn Mock → Bấm nút `Báo thức 07:30` → Bấm `Duyệt thực thi` | Mở ứng dụng Đồng hồ hệ thống với giờ 07:30 | | | Dùng `AlarmClock.ACTION_SET_ALARM` |
| **4** | Action | Mở app YouTube (`open_app`) | Chọn Mock → Bấm nút `Mở YouTube` → Bấm `Duyệt thực thi` | Ứng dụng YouTube thật khởi chạy lên màn hình | | | Thuộc whitelist package |
| **5** | Action | Mở app Chrome (`open_app`) | Chọn Mock → Nhập "Mở Chrome" → Bấm `Duyệt thực thi` | Ứng dụng Google Chrome khởi chạy | | | Thuộc whitelist package |
| **6** | Action | Mở app Cài đặt (`open_app`) | Chọn Mock → Nhập "Mở cài đặt" → Bấm `Duyệt thực thi` | Màn hình Cài đặt máy mở lên | | | Thuộc whitelist package |
| **7** | Action | Mở app Spotify (`open_app`) | Chọn Mock → Nhập "Mở Spotify" → Bấm `Duyệt thực thi` | Mở Spotify nếu có cài; nếu chưa cài báo "chưa được cài đặt" | | | Thuộc whitelist package |
| **8** | Action | Chỉnh âm lượng (`set_volume`) | Chọn Mock → Bấm nút `Âm lượng 40` → Bấm `Duyệt thực thi` | Âm lượng đa phương tiện của máy đặt về 40% | | | Dùng `AudioManager.STREAM_MUSIC` |
| **9** | Kiểm soát | Người dùng HỦY duyệt | Bấm bất kỳ action → Hiện thẻ duyệt → Bấm `Từ chối (Hủy)` | Robot thông báo "Đã hủy thực hiện hành động theo ý bạn.", tuyệt đối không chạy action | | | Đáp ứng yêu cầu kiểm soát hành động |
| **10** | An toàn | Action lạ (`hack_system`) | Chọn Debug → Bấm `hack` | Thẻ đỏ TỪ CHỐI AN TOÀN [UNKNOWN_ACTION], mắt robot chuyển đỏ cam áy náy, không chạy | | | Chặn ở lớp L2 ActionValidator |
| **11** | An toàn | App ngoài whitelist (`evil`) | Chọn Debug → Bấm `evil` (`com.evil.app`) | Thẻ đỏ TỪ CHỐI AN TOÀN [INVALID_PARAMS], không chạy | | | Chặn mở app độc lạ |
| **12** | An toàn | Âm lượng vượt trần (`999`) | Chọn Debug → Bấm `999` (`level: 999`) | Thẻ đỏ TỪ CHỐI AN TOÀN [INVALID_PARAMS], không chạy | | | Chỉ chấp nhận 0..100 |
| **13** | An toàn | Motor phần cứng STUB (`move`) | Chọn Debug → Bấm `move` | Thẻ đỏ TỪ CHỐI AN TOÀN [HARDWARE_NOT_READY], không chạy | | | Chưa có ESP32 thì luôn từ chối |
| **14** | An toàn | Báo thức giờ sai (`25:00`) | Chọn Debug → Bấm `25:00` | Thẻ đỏ TỪ CHỐI AN TOÀN [INVALID_PARAMS], không chạy | | | Regex kiểm soát nghiêm ngặt |
| **15** | An toàn | Model nói dối (`lie`) | Chọn Debug → Bấm `lie` | Robot KHÔNG nói câu dối mà nói câu an toàn: "Xin lỗi, mình chưa thực hiện được yêu cầu đó." | | | Quy tắc mục 4 prompt |
| **16** | Ngoại tuyến | Chế độ máy bay + Mock | Bật chế độ máy bay trên Z Flip5 → Dùng Mock offline | Hoạt động 100% mượt mà không cần bất kỳ kết nối mạng nào | | | Chạy local-first độc lập |
| **17** | Mạng | Chế độ máy bay + Backend | Bật chế độ máy bay → Gửi câu hỏi qua Backend | Vào trạng thái ERROR hiển thị thân thiện "Lỗi kết nối mạng", app không bị crash | | | Bắt ngoại lệ OkHttp an toàn |
| **18** | Mạng | Mất kết nối Backend | Tắt tiến trình FastAPI trên laptop → Gửi lệnh qua Backend | Báo lỗi kết nối mạng thân thiện, hiển thị nút "Thử lại" và "Bỏ qua" | | | Không treo app |
| **19** | Phần cứng | Gập máy Flex Mode | Gập máy Z Flip5 ở góc 90° – 115° | Giao diện cuộn mượt mà trong nửa màn hình, mặt robot và các nút hiển thị trọn vẹn | | | Đặc tính màn hình gập Samsung |

---

## 3. Kết luận nghiệm thu
- **Trạng thái:** Sẵn sàng cho người dùng đánh giá và xác nhận nghiệm thu.
- **Tiến độ tiếp theo:** Bước sang **Phase 4: Spike LLM trên Samsung Galaxy Z Flip5 + Model Manager v0**.
