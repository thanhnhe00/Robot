# ADR-0001: Validator + Executor nằm trên Android, JSON Schema dùng chung
Date: 2026-09-29
Status: Accepted

Decision:
Việc kiểm tra (schema, permission, whitelist, giới hạn) và thực thi action diễn ra **trong app Android**. Backend validate lại như lớp phòng thủ thứ hai. Luật hợp lệ được định nghĩa ở **một JSON Schema duy nhất**, từ đó sinh model Kotlin và Pydantic.

Why:
Robot phải hoạt động offline. Nếu chỉ backend kiểm tra thì khi laptop tắt hoặc mất mạng sẽ không còn lớp kiểm soát nào. Schema chung tránh việc hai bên hiểu luật khác nhau.

Alternatives:
1. Chỉ validate ở backend.
2. Hai bộ luật viết tay riêng cho Kotlin và Python.

Trade-offs:
Cần cơ chế sinh code từ schema và giữ hai đầu đồng bộ (công sức Phase 2). Đổi lại: an toàn offline và một nguồn sự thật.

Chosen:
Validator/executor trên Android + backend validate lại + JSON Schema chung (thực hiện ở Phase 2; trước đó luật nằm trong `backend/app/actions.py`).
