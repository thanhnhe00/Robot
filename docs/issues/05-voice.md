---
title: [Phase 5] Voice: wake word, STT, TTS tiếng Việt
labels: phase-5, epic
milestone: Phase 5
---
## Mục tiêu
Luồng microphone → wake word → ghi âm → STT → AI → TTS → loa, với ngân sách tài nguyên đã biết từ Phase 4.

## Vì sao cần phase này
Robot phải nghe và nói tiếng Việt; wake word phải nhẹ để không hao pin.

## Phụ thuộc
Phase 4, research nhóm B

## Việc cần làm
- [ ] Wake word ("Hey Robot") + VAD
- [ ] STT tiếng Việt: benchmark WER, latency, RAM cho các ứng viên
- [ ] TTS tiếng Việt: chất lượng, latency, dung lượng
- [ ] Foreground service cho mic nền theo quy định Android
- [ ] Chế độ hybrid: local ưu tiên, cloud fallback
- [ ] Đo pin khi wake word chạy nền

## Definition of Done
- [ ] Nói "Hey Robot, mấy giờ rồi?" → robot trả lời bằng giọng nói
- [ ] Có bảng benchmark từng khối
- [ ] Kiểm thử offline cho phần voice đã chọn local

## Ghi chú
> Issue này là **khung sườn**. Khi bắt đầu phase sẽ bổ sung đủ 16 mục (input/output, kiến trúc, luồng dữ liệu, test, benchmark, security, safety, troubleshooting…) và tách thành issue con nếu cần.
