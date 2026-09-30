# Bằng chứng, riêng tư và thay đổi code

- Phân biệt điều đã thấy trong repo, điều được tài liệu chính thức xác nhận, giả định và điều chưa biết. Không biến suy đoán thành kết luận.
- Với thông tin có thể thay đổi như SDK, API, license, giá hoặc hỗ trợ model, kiểm tra tài liệu chính thức hiện hành trước khi chốt; lưu nguồn và ngày truy cập trong tài liệu nghiên cứu/ADR khi phù hợp.
- Không tuyên bố đã chạy test, linter, Docker hay benchmark nếu chưa thực sự chạy. Chỉ chạy kiểm tra phù hợp với yêu cầu và môi trường; ghi rõ lệnh/kết quả khi báo cáo.
- Không tự sửa các lỗi lint tồn tại ngoài phạm vi yêu cầu. Không đổi nhiều file chỉ để làm sạch hoặc chuẩn hóa repo nếu người dùng chưa yêu cầu.
- Bảo vệ dữ liệu: giữ phân loại `LOCAL ONLY`, `OPTIONAL CLOUD`, `PUBLIC-SAFE`; không gửi dữ liệu riêng tư lên cloud hoặc ghi nội dung bí mật vào log.
- Tuân thủ `.gitignore`; tuyệt đối không commit `.env`, thông tin xác thực, database riêng tư, dữ liệu cá nhân hoặc weights.
- Commit message khi được yêu cầu theo tiền tố đã dùng trong repo: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `perf:`, `chore:`.
