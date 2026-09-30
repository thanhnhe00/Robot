# ADR-0004: Bổ sung 6 hạng mục xuyên suốt vào roadmap
Date: 2026-09-29
Status: Accepted

Decision:
Bổ sung vào roadmap: (1) offline-first/hybrid router, (2) model abstraction, (3) evaluation xuyên suốt, (4) dataset strategy, (5) RAG chỉ khi có bằng chứng, (6) test + reliability. Thêm hai điểm phát hiện thêm: Model Manager update/rollback (Phase 12) và phân loại riêng tư LOCAL ONLY / OPTIONAL CLOUD / PUBLIC (Phase 6).

Why:
Bản bảng phase gọn chưa thể hiện rõ các yêu cầu này của spec dự án Robot; nếu không ghi rõ sẽ dễ bị bỏ sót hoặc làm muộn.

Alternatives:
1. Tạo phase riêng cho từng hạng mục (làm roadmap dài, tách rời khỏi nơi chúng thực sự được dùng).
2. Giữ nguyên bảng và nhớ trong đầu.

Trade-offs:
Một số phase nặng hơn (Phase 2, 6, 13). Đổi lại các yêu cầu có chỗ đứng rõ ràng, có issue và Definition of Done.

Chosen:
Gắn từng hạng mục vào phase phù hợp và thêm hai issue xuyên suốt (Evaluation, Testing). Golden test set bắt đầu từ Phase 2 để tránh rò rỉ khi fine-tune ở Phase 7. RAG là "gate": chỉ làm khi benchmark chứng minh cần.
