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
Phase 0 đã có thiết kế/research trong repo; theo hướng thực hành đã thống nhất, có thể làm Phase 1 trước và quay lại đóng Phase 0 sau.

## Việc cần làm
- [x] Baseline đã có `LLMProvider` protocol, factory chọn provider theo `LLM_PROVIDER`, và provider Ollama / Gemini / Mock.
- [x] Đối chiếu/chuẩn hóa tên và contract `AIProvider` theo roadmap; thêm test chứng minh thêm provider không cần sửa service.
- [x] Prompt được tách ra `ai/prompts/v1/system.md`, chọn phiên bản bằng `PROMPT_VERSION`.
- [x] Tách personality vào `robot_personality.yaml` (bổ sung đủ behavior, response_style, rules theo spec).
- [x] Logging có cấu trúc (timestamp, request, model, provider, latency, response, action, validation, prompt_version, lỗi) — không log dữ liệu nhạy cảm.
- [x] Dockerfile + docker-compose (bind an toàn `127.0.0.1:8000:8000`); Swagger có ví dụ request/response/lỗi 401, 502, 503.
- [x] CI GitHub Actions: lint (Ruff ghim 0.16.9, runner ubuntu-24.04), unit test, API test, docker build.
- [x] Repository/service abstraction cho lưu hội thoại (bật/tắt persistence bằng cấu hình `PERSISTENCE_ENABLED`).
- [x] Sửa fallback khi model trả sai JSON: cố định câu `"Xin lỗi, mình chưa hiểu yêu cầu đó."` kèm `action: null`.

## Definition of Done
- [x] Đổi provider chỉ bằng `.env`, không sửa code
- [ ] CI xanh trên GitHub Actions (local Ruff 0 findings và Pytest 24 passed đã đạt; chờ push commit lên remote)
- [x] `docker compose up` chạy được backend
- [x] Có test cho từng provider (mock cho provider ngoài)

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
