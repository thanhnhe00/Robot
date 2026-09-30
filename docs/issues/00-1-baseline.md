---
title: [Phase 0] 0.1 Đồng bộ baseline và dựng khung tài liệu
labels: phase-0, docs
milestone: Phase 0
---
## Mục tiêu
Đưa baseline backend về đúng spec ROBOTV1 và dựng khung tài liệu/ADR.

## Việc đã làm
- [x] Đổi `reply` → `response`; `open_app` dùng `params.package`, whitelist theo package (ADR-0003)
- [x] Cấu trúc repo theo mục 61 (`ai/`, `firmware/esp32/`, `dashboard/`, `scripts/`, `tests/`, `docker/`)
- [x] `docs/ARCHITECTURE.md`, `docs/ROADMAP.md`, `docs/RESEARCH_BACKLOG.md`
- [x] ADR-0001 (validator trên Android + JSON Schema chung), ADR-0002 (spike LLM trước Voice), ADR-0003
- [x] 12 test backend qua

## Definition of Done
- [x] `python -m pytest -q` → `12 passed`
- [ ] Code + docs đã được đưa lên GitHub (đóng issue này sau khi push)
