---
name: research-and-adr
description: Researches a technical choice for Robot using current authoritative sources and records evidence, unknowns, trade-offs, and decisions in the existing research or ADR format. Use for model, Android, voice, hardware, library, and architecture research.
---

# Research và ADR cho Robot

1. Đọc câu hỏi tương ứng trong `docs/RESEARCH_BACKLOG.md`, roadmap và ADR liên quan trước khi tìm nguồn mới.
2. Xác định điều cần trả lời và tiêu chí quyết định; không mở rộng sang chủ đề lân cận nếu không cần.
3. Ưu tiên tài liệu chính thức của nhà cung cấp, paper gốc, spec/API chính thức và license gốc. Ghi link trực tiếp, ngày truy cập, phiên bản/ngày phát hành khi có.
4. Tách rõ: đã xác minh bằng tài liệu, suy luận từ bằng chứng, giả định, chưa biết và cần benchmark thực tế.
5. Không tuyên bố benchmark trên Galaxy Z Flip5 nếu chưa đo trực tiếp trên đúng thiết bị. Không dùng benchmark thiết bị khác thay kết quả của máy này.
6. Đưa bảng ngắn các lựa chọn, lợi ích, hạn chế, license/chi phí/thiết bị cần có nếu liên quan. Đưa đề xuất nhưng không ghi thành quyết định đã duyệt.
7. Khi cần lưu, dùng cấu trúc hiện có trong `docs/research/` hoặc `docs/decisions/`; theo ADR format hiện hành và cập nhật trạng thái `Proposed` cho quyết định chưa được người dùng chốt.
8. Kết thúc bằng câu trả lời trực tiếp và bước kiểm chứng kế tiếp; không bắt đầu huấn luyện, cài đặt lớn hoặc mua phần cứng trong tác vụ nghiên cứu.
