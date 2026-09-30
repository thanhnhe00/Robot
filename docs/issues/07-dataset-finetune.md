---
title: [Phase 7] Dataset strategy + Fine-tuning
labels: phase-7, epic
milestone: Phase 7
---
## Mục tiêu
Chiến lược dataset (có sẵn → synthetic → augmentation → kiểm tay khi thật sự cần) rồi fine-tune LoRA/QLoRA và chứng minh bằng số liệu base vs fine-tuned.

## Vì sao cần phase này
Fine-tune chỉ có ý nghĩa khi đã có baseline và golden set (Phase 2, Phase 4).

## Phụ thuộc
Phase 2, Phase 4

## Việc cần làm
- [ ] Khảo sát dataset có sẵn phù hợp; sinh dữ liệu synthetic + augmentation tiếng Việt; lọc và kiểm chất lượng
- [ ] Chia train/val/test; golden set giữ nguyên, không rò rỉ
- [ ] Data card: nguồn, license, cách tạo
- [ ] Train LoRA/QLoRA trên Colab/Kaggle/cloud — ghi model, dataset, VRAM, thời gian, chi phí ước tính, cách tái lập
- [ ] So sánh base vs fine-tuned trên toàn bộ metric; lượng tử hóa và chạy thử trên G8

## Definition of Done
- [ ] Báo cáo base vs fine-tuned có số liệu
- [ ] Tái lập được từ script + config
- [ ] Chi phí và thời gian được ghi lại

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
