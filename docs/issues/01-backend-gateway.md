---
title: [Phase 1] Backend AI Gateway + Model abstraction
labels: phase-1, epic
milestone: Phase 1
---
## Mục tiêu
Nâng baseline thành AI Gateway: đổi model chỉ bằng cấu hình (`AIProvider`: Local/Laptop/Cloud/Mock, ví dụ Ollama, llama.cpp, Gemini), prompt versioned, personality tách khỏi model.

## Vì sao cần phase này
Model thay đổi rất nhanh; app và dataset không được khóa vào một model.

## Phụ thuộc
Phase 0

## Việc cần làm
- [x] Baseline đã có `LLMProvider` protocol, factory chọn provider theo `LLM_PROVIDER`, và provider Ollama / Gemini / Mock.
- [ ] Đối chiếu/chuẩn hóa tên và contract `AIProvider` theo roadmap; thêm test chứng minh thêm provider không cần sửa service.
- [ ] Prompt đưa vào `ai/prompts/` có version; personality trong `robot_personality.yaml`
- [ ] Logging có cấu trúc (timestamp, request, model, provider, latency, response, action, validation, lỗi) — không log dữ liệu nhạy cảm
- [ ] Dockerfile + docker-compose; Swagger có ví dụ request/response/lỗi
- [ ] CI GitHub Actions: lint, unit test, API test, docker build
- [ ] Repository/service abstraction cho lưu hội thoại (bật/tắt persistence bằng cấu hình)

## Definition of Done
- [ ] Đổi provider chỉ bằng `.env`, không sửa code
- [ ] CI xanh
- [ ] `docker compose up` chạy được backend
- [ ] Có test cho từng provider (mock cho provider ngoài)

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
