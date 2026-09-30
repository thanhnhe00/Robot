---
title: [Phase 0] 0.2 Research nhóm A: engine LLM, tăng tốc, model, quantization
labels: phase-0, research
milestone: Phase 0
---
## Mục tiêu
Trả lời R1–R4 trong `docs/RESEARCH_BACKLOG.md` để chốt **danh sách ứng viên** cho spike ở Phase 4 (chưa chốt model cuối).

## Việc cần làm
- [x] R1: So sánh hướng runtime và ghi giới hạn bằng chứng cho Android/855; xem `docs/research/A-llm.md`.
- [x] R2: Xác nhận phần cứng SoC và ghi rõ chưa chứng minh tăng tốc trên G8; CPU là baseline, GPU/QNN là thử nghiệm Phase 4.
- [x] R3: Lập shortlist Qwen3-0.6B/1.7B và Gemma 3 1B IT; giữ mở benchmark tiếng Việt, tool/action và license cụ thể.
- [x] R4: Ghi ma trận quantization và metric đo, không ngoại suy số liệu model khác.
- [x] Viết `docs/research/A-llm.md` với nguồn chính thức và ngày tra cứu.
- [x] Ghi đề xuất runtime/model trong ADR-0005 (Proposed); lựa chọn cuối chờ review và benchmark.

## Definition of Done
- [x] Mục chưa biết đều có lý do và bước kiểm chứng.
- [x] Có shortlist engine + model + quantization để benchmark ở Phase 4.
- [x] R1–R4 hoàn tất nghiên cứu tài liệu; benchmark trên G8 thuộc Phase 4.

## Ràng buộc
Không tuyên bố model nào "tốt nhất" khi chưa benchmark trên G8.
