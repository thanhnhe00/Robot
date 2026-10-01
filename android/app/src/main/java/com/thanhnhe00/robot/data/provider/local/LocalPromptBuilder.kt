package com.thanhnhe00.robot.data.provider.local

/**
 * Xây dựng prompt suy luận cho Local LLM trên thiết bị (Phase 4.2).
 *
 * Định dạng theo chuẩn ChatML / Instruct tối giản:
 * - Hệ thống: Vai trò trợ lý tiếng Việt của Robot, xuất DUY NHẤT một chuỗi JSON.
 * - Cấu trúc: {"response": "<câu nói>", "action": {"type": "<tên>", "params": {...}}}.
 * - Ràng buộc: LLM chỉ là bộ đề xuất, không được điều khiển trực tiếp.
 */
object LocalPromptBuilder {

    const val SYSTEM_PROMPT = """Bạn là Robot, trợ lý AI trên thiết bị Android. Trả lời bằng tiếng Việt ngắn gọn, thân thiện.
Bạn CHỈ ĐƯỢC PHÉP trả về DUY NHẤT một chuỗi JSON hợp lệ (không kèm giải thích ngoài JSON) theo đúng cấu trúc:
{
  "response": "câu trả lời bằng tiếng Việt cho người dùng",
  "action": {
    "type": "tên_action",
    "params": {}
  }
}
Nếu yêu cầu không cần thực hiện hành động nào (chỉ trò chuyện, chào hỏi), hãy để "action": null.
Các action hợp lệ gồm:
- "get_time": xem giờ hệ thống, params: {}
- "get_battery": kiểm tra mức pin, params: {}
- "set_alarm": đặt báo thức, params: {"time": "HH:MM", "label": "tên"}
- "open_app": mở ứng dụng, params: {"package": "com.google.android.youtube" | "com.android.chrome" | "com.android.settings" | "com.spotify.music"}
- "set_volume": chỉnh âm lượng, params: {"level": 0..100}
Mọi action khác hoặc điều khiển motor đều không được phép."""

    fun buildPrompt(userText: String): String {
        return """<|im_start|>system
$SYSTEM_PROMPT<|im_end|>
<|im_start|>user
${userText.trim()}<|im_end|>
<|im_start|>assistant
"""
    }
}
