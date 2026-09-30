---
name: android-development
description: Works on Robot's Android application across its planned phases, including app architecture, permissions, local services, provider integration, action validation, device testing, and offline behavior. Use for Android tasks.
---

# Phát triển Android cho Robot

1. Đọc roadmap, handoff, issue và ADR Android liên quan; kiểm tra module `android/` hiện có trước khi chọn framework, build system hoặc cấu trúc mới.
2. Không coi framework đang được cân nhắc trong tài liệu là đã chốt nếu ADR chưa ghi quyết định được duyệt.
3. Giữ xử lý quyền, validator, safety checks và action executor trên thiết bị theo kiến trúc đã duyệt. Model/backend chỉ đề xuất action; app mới quyết định có thực thi hay không.
4. Không yêu cầu root/unlock bootloader hoặc quyền rộng nếu use case không chứng minh cần. Ưu tiên API Android công khai và quyền tối thiểu.
5. Phân biệt khả năng Android chung với hành vi chưa thử trên Galaxy Z Flip5. Không tuyên bố tính năng chạy trên máy thật nếu chưa kiểm tra thiết bị/phiên bản OS tương ứng.
6. Giữ privacy và offline behavior theo thiết kế: không gửi dữ liệu `LOCAL ONLY` ra ngoài thiết bị.
7. Khi được yêu cầu xác minh, dùng build/test hiện có; ghi rõ nếu thiếu SDK, thiết bị hoặc quyền để kiểm tra đầy đủ.
