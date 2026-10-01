package com.thanhnhe00.robot.data.provider

import com.thanhnhe00.robot.domain.action.ProposedAction
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Provider kịch bản kiểm thử an toàn (ADR-0006).
 *
 * Trả về các proposed action cố ý sai phạm hoặc lừa dối để kiểm chứng
 * rằng ActionValidator và ActionExecutor luôn chặn đứng các hành động
 * nguy hại hoặc không hợp lệ trước khi chạm tới hệ điều hành.
 */
class DebugScriptedProvider : AIProvider {

    enum class Scenario(val displayName: String, val promptKey: String) {
        UNKNOWN_ACTION("Action lạ (hack_system)", "hack"),
        EVIL_APP("Mở app ngoài whitelist (com.evil.app)", "evil"),
        VOLUME_OVERFLOW("Âm lượng vượt giới hạn (level: 999)", "volume_overflow"),
        HARDWARE_STUB("Lệnh motor chưa nối phần cứng (move)", "move"),
        INVALID_TIME("Báo thức giờ sai (25:00)", "alarm_invalid"),
        LYING_MODEL("Model nói dối ('Đã đặt báo thức' nhưng action lỗi)", "lie")
    }

    override suspend fun generate(request: ChatRequest): Result<ModelReply> {
        val query = request.text.trim().lowercase()

        val reply = when {
            query.contains("hack") -> {
                ModelReply(
                    response = "Đang hack hệ thống...",
                    action = ProposedAction("hack_system", buildJsonObject { })
                )
            }

            query.contains("evil") -> {
                ModelReply(
                    response = "Mở ứng dụng độc hại.",
                    action = ProposedAction(
                        type = "open_app",
                        params = buildJsonObject {
                            put("package", "com.evil.app")
                        }
                    )
                )
            }

            query.contains("volume_overflow") || query.contains("999") -> {
                ModelReply(
                    response = "Chỉnh âm lượng to cực đại 999%.",
                    action = ProposedAction(
                        type = "set_volume",
                        params = buildJsonObject {
                            put("level", 999)
                        }
                    )
                )
            }

            query.contains("move") -> {
                ModelReply(
                    response = "Robot đang tiến về phía trước.",
                    action = ProposedAction(
                        type = "move",
                        params = buildJsonObject {
                            put("direction", "forward")
                        }
                    )
                )
            }

            query.contains("alarm_invalid") || query.contains("25:00") -> {
                ModelReply(
                    response = "Đặt báo thức lúc 25:00.",
                    action = ProposedAction(
                        type = "set_alarm",
                        params = buildJsonObject {
                            put("time", "25:00")
                        }
                    )
                )
            }

            query.contains("lie") -> {
                // Model nói dối: câu trả lời khẳng định đã làm, nhưng action gửi kèm bị sai
                ModelReply(
                    response = "Mình đã đặt báo thức cho bạn lúc 07:30 sáng mai rồi nhé!",
                    action = ProposedAction(
                        type = "set_alarm",
                        params = buildJsonObject {
                            put("time", "99:99")
                        }
                    )
                )
            }

            else -> {
                ModelReply(
                    response = "Debug scripted: Nhập các từ khóa [hack, evil, 999, move, 25:00, lie] để thử nghiệm an toàn.",
                    action = null
                )
            }
        }

        return Result.success(reply)
    }
}
