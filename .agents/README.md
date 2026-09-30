# Cấu hình Antigravity cho toàn repo Robot

Mở thư mục gốc của repo `Robot` trong Antigravity IDE. Bộ này áp dụng cho toàn bộ monorepo và các phase của dự án; nó không khóa công việc vào Phase 1. Agent nạp hướng dẫn từ `AGENTS.md`, rules từ `.agents/rules/` và skills từ `.agents/skills/`.

## Skills

- `/robot-task` — làm một task/issue theo phase và phạm vi.
- `/backend-development` — backend ở mọi phase.
- `/android-development` — ứng dụng và tích hợp Android.
- `/ai-model-evaluation` — model, router, dataset và benchmark.
- `/embedded-robot-safety` — ESP32, phần cứng và an toàn robot.
- `/research-and-adr` — nghiên cứu có nguồn và ghi ADR.
- `/robot-task` — điều phối một việc bất kỳ theo phase/phạm vi hiện hành.

## Workflow tương thích cũ

- `/start-robot-issue` — lối gọi nhanh theo một issue, nằm trong `.agents/workflows/`.
- Antigravity dự kiến ngừng nhận diện workflow sau ngày 1/11/2026; sau đó dùng `/robot-task` và các skill chuyên biệt. Workflow này không phải GitHub Actions workflow.

## Cập nhật

Để dùng các thay đổi mới nhất sau khi pull repo, mở lại workspace hoặc dùng mục Customizations trong Antigravity để xem rules và skills đã được nhận diện chưa.
