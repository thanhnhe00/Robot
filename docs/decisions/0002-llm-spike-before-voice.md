# ADR-0002: Spike LLM trên Galaxy Z Flip5 trước Voice
Date: 2026-09-29
Status: Accepted

Decision:
Chạy và benchmark LLM trên Samsung Galaxy Z Flip5 (Phase 4) trước khi làm Voice (Phase 5).

Why:
Model và STT/TTS/wake word dùng chung RAM/CPU/nhiệt/pin của một điện thoại. Biết ngân sách còn lại của model trước thì chọn STT/TTS hợp lý hơn, và nếu local LLM không khả thi thì phát hiện sớm.

Alternatives:
Giữ thứ tự gốc: Voice (Phase 4) rồi Local LLM (Phase 5).

Trade-offs:
Chưa có trải nghiệm giọng nói sớm để demo. Đổi lại là giảm rủi ro làm lại.

Chosen:
Đưa spike LLM lên Phase 4, Voice thành Phase 5.
