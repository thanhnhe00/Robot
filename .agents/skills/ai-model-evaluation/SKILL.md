---
name: ai-model-evaluation
description: Plans or evaluates Robot AI models, local/laptop/cloud providers, routing, datasets, prompts, and benchmarks using explicit metrics and hardware evidence. Use for model selection or AI performance claims.
---

# Đánh giá AI và model

1. Đọc `docs/RESEARCH_BACKLOG.md`, roadmap, ADR và dữ liệu/evaluation hiện có để xác định câu hỏi và phase.
2. Phân biệt model chạy trên điện thoại, laptop và cloud; mô tả đường đi dữ liệu, yêu cầu mạng, privacy, chi phí và fallback.
3. Trước khi khuyến nghị model/runtime mới, xác minh tài liệu chính thức, license, định dạng và yêu cầu thiết bị hiện hành.
4. Thiết kế benchmark bằng tập input cố định, tiêu chí đo rõ (chất lượng, latency, RAM, nhiệt, pin/chi phí nếu phù hợp), phiên bản model/runtime và môi trường ghi nhận.
5. Không tuyên bố model chạy tốt trên Galaxy Z Flip5 hoặc model nào tốt nhất khi chưa có đo thật trên đúng thiết bị. Báo riêng kết quả tài liệu và kết quả benchmark.
6. Không gửi dữ liệu `LOCAL ONLY` sang cloud. Dataset cần ghi nguồn, quyền sử dụng, nhãn riêng tư và cách loại thông tin cá nhân.
7. Không bắt đầu training, tải weights lớn hay phát sinh chi phí nếu chưa nằm trong yêu cầu; không commit weights hoặc dữ liệu nhạy cảm.
8. Lưu kế hoạch/kết quả vào vị trí `ai/` hoặc `docs/research/` phù hợp, không ghi một đề xuất thành quyết định đã duyệt nếu chưa có ADR được chấp nhận.
