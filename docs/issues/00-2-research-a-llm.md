---
title: [Phase 0] 0.2 Research nhóm A: engine LLM, tăng tốc, model, quantization
labels: phase-0, research
milestone: Phase 0
---
## Mục tiêu
Trả lời R1–R4 trong `docs/RESEARCH_BACKLOG.md` để chốt **danh sách ứng viên** cho spike ở Phase 4 (chưa chốt model cuối).

## Việc cần làm
- [ ] R1: So sánh engine (llama.cpp build NDK chính thức vs wrapper, MLC, ONNX Runtime GenAI, MediaPipe/LiteRT-LM, ExecuTorch): Android + Snapdragon 855, ARM64, GGUF, structured output/grammar, license, mức độ bảo trì
- [ ] R2: GPU (Adreno 640) / DSP có dùng được cho LLM trên 855 không; nếu không → CPU-only
- [ ] R3: Model card hiện hành của Qwen3 0.6B/1.7B + 1–2 model nhỏ khác: license, tiếng Việt, tool calling, dung lượng GGUF
- [ ] R4: Quantization Q4_K_M / Q5_K_M / Q6_K / Q8_0: đánh đổi chất lượng, RAM, tốc độ
- [ ] Viết `docs/research/A-llm.md`: mỗi kết luận có link nguồn + ngày; nguồn cộng đồng ghi rõ
- [ ] ADR (Proposed): engine ứng viên cho Phase 4; ADR (Proposed): shortlist model

## Definition of Done
- [ ] Không còn mục nào ghi CHƯA BIẾT mà chưa có nguồn hoặc lý do
- [ ] Có shortlist engine + model + quantization để benchmark ở Phase 4
- [ ] R1–R4 chuyển `DONE` trong `RESEARCH_BACKLOG.md`

## Ràng buộc
Không tuyên bố model nào "tốt nhất" khi chưa benchmark trên G8.
