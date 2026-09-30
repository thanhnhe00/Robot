# Golden Test Set v0
# Nguồn, cách tạo, quy tắc sử dụng

## Mục đích
Bộ mẫu tiếng Việt đã gắn nhãn thủ công, dùng để đánh giá chất lượng model/prompt.
**KHÔNG dùng để train hay fine-tune.**

## Cấu trúc dữ liệu (`golden_v0.jsonl`)
Mỗi dòng là một JSON object:
```json
{
  "id": "G001",
  "input": "Bây giờ là mấy giờ rồi?",
  "expected_action": "get_time",
  "expected_params": {},
  "category": "time",
  "difficulty": "easy",
  "notes": ""
}
```

### Các trường
| Trường | Mô tả |
|---|---|
| `id` | Mã định danh duy nhất, tiền tố `G` |
| `input` | Câu nói tiếng Việt của người dùng |
| `expected_action` | Action đúng (`null` nếu chỉ trò chuyện) |
| `expected_params` | Params kỳ vọng (object hoặc `null`) |
| `category` | Nhóm: `time`, `battery`, `alarm`, `app`, `volume`, `chat`, `safety`, `edge` |
| `difficulty` | `easy`, `medium`, `hard` |
| `notes` | Ghi chú đặc biệt |

## Nguồn
- Tự viết bằng tay, kiểm tra chéo, dựa trên ngữ cảnh sử dụng robot gia đình tiếng Việt.
- Không dùng LLM để sinh (tránh bias). Nếu sau này bổ sung bằng LLM, phải ghi rõ và kiểm tra tay.

## Quy tắc
1. **KHÔNG** dùng để train / fine-tune.
2. Mỗi khi đổi model hoặc prompt, chạy lại eval trên toàn bộ tập này.
3. Khi thêm mẫu mới, gắn nhãn bằng tay và ghi lại ngày thêm.
4. Không xóa mẫu cũ (giữ để so sánh lịch sử).

## Thống kê v0
- Tổng: ~120 mẫu
- Phân bố:
  - `time`: ~12 mẫu
  - `battery`: ~10 mẫu
  - `alarm`: ~16 mẫu
  - `app`: ~18 mẫu
  - `volume`: ~14 mẫu
  - `chat`: ~20 mẫu
  - `safety`: ~16 mẫu (action lạ, tool bịa, SQL injection, prompt injection)
  - `edge`: ~14 mẫu (mơ hồ, nhiều action, typo, emoji)
