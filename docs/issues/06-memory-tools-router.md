---
title: [Phase 6] Memory + Tools + AI Router (hybrid/offline) + RAG gate
labels: phase-6, epic
milestone: Phase 6
---
## Mục tiêu
Bộ nhớ ngắn/dài hạn, tool system xác định, **AI Router** chọn Local/Laptop/Cloud, chế độ offline cơ bản; đánh giá RAG chỉ khi có bằng chứng cần.

## Vì sao cần phase này
Đây là chỗ hiện thực hóa local-first + hybrid: việc xác định (giờ, âm lượng) không hỏi LLM; dữ liệu cá nhân không ra cloud.

## Phụ thuộc
Phase 4, Phase 5

## Việc cần làm
- [ ] Memory: lưu / truy xuất / cập nhật / xóa, ranh giới quyền riêng tư, chọn ngữ cảnh đưa vào prompt
- [ ] Tools: get_time (đồng hồ hệ thống), get_weather, get_location; luồng Tool selection → validator → execute → kết quả → AI trả lời
- [ ] AI Router v0 (luật): phân loại yêu cầu theo loại tác vụ, trạng thái mạng, độ phức tạp, yêu cầu riêng tư
- [ ] Phân loại dữ liệu: LOCAL ONLY / OPTIONAL CLOUD / PUBLIC-SAFE
- [ ] Offline mode cơ bản: wake word + STT/TTS local + LLM local + memory + Android actions (kiểm tra bằng chế độ máy bay)
- [ ] RAG gate: chỉ làm nếu benchmark chứng minh cần; kết thúc bằng ADR "cần / không cần RAG" kèm số liệu

## Definition of Done
- [ ] Chế độ máy bay: các chức năng cơ bản vẫn chạy
- [ ] Dữ liệu LOCAL ONLY không bao giờ gửi cloud (có test)
- [ ] Có ADR về RAG với bằng chứng

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
