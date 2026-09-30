# Issues theo phase

Mỗi file `NN-*.md` là một GitHub issue (front matter: `title`, `labels`, `milestone`, `state`).
Sửa nội dung ở đây rồi commit; issue trên GitHub tạo bằng:

```bash
gh auth login                                  # một lần
python scripts/create_issues.py --dry-run      # xem trước
python scripts/create_issues.py                # tạo thật (chạy lại an toàn, bỏ qua issue trùng tiêu đề)
```

- `00-*`: Phase 0 (chi tiết). `01`–`14`: khung sườn từng phase, sẽ hoàn thiện khi tới phase đó.
- `90-*`, `91-*`: các mảng xuyên suốt (Evaluation, Testing).
- Script chỉ **tạo** issue mới; sửa issue đã tạo thì sửa trực tiếp trên GitHub.
