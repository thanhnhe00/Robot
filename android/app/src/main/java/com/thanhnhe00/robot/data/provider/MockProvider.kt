package com.thanhnhe00.robot.data.provider

import com.thanhnhe00.robot.domain.action.ProposedAction
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Provider giả lập ngoại tuyến (Offline Mock Provider).
 *
 * Chạy hoàn toàn cục bộ trên máy mà không cần mạng Internet hay Backend.
 * Đáp ứng các câu lệnh tiếng Việt cơ bản bằng luật xác định (deterministic).
 */
class MockProvider : AIProvider {

    private val timeRegex = Regex("""([01]\d|2[0-3]):[0-5]\d""")
    private val numberRegex = Regex("""\b(\d{1,3})\b""")

    override suspend fun generate(request: ChatRequest): Result<ModelReply> {
        val query = request.text.trim().lowercase()

        val reply = when {
            query.contains("mấy giờ") || query.contains("thời gian") || query.contains("xem giờ") -> {
                ModelReply(
                    response = "Bây giờ là mấy giờ rồi.",
                    action = ProposedAction("get_time", buildJsonObject { })
                )
            }

            query.contains("pin") -> {
                ModelReply(
                    response = "Mức pin hiện tại đây.",
                    action = ProposedAction("get_battery", buildJsonObject { })
                )
            }

            query.contains("báo thức") -> {
                val match = timeRegex.find(query)
                if (match != null) {
                    val time = match.value
                    ModelReply(
                        response = "Mình đặt báo thức lúc $time nhé.",
                        action = ProposedAction(
                            type = "set_alarm",
                            params = buildJsonObject {
                                put("time", time)
                                put("label", "Báo thức")
                            }
                        )
                    )
                } else {
                    ModelReply(
                        response = "Bạn muốn đặt báo thức lúc mấy giờ? Hãy nói giờ theo định dạng HH:MM nhé.",
                        action = null
                    )
                }
            }

            query.contains("youtube") -> {
                ModelReply(
                    response = "Đang mở YouTube cho bạn.",
                    action = ProposedAction(
                        type = "open_app",
                        params = buildJsonObject {
                            put("package", "com.google.android.youtube")
                        }
                    )
                )
            }

            query.contains("chrome") -> {
                ModelReply(
                    response = "Đang mở trình duyệt Chrome.",
                    action = ProposedAction(
                        type = "open_app",
                        params = buildJsonObject {
                            put("package", "com.android.chrome")
                        }
                    )
                )
            }

            query.contains("cài đặt") -> {
                ModelReply(
                    response = "Đang mở Cài đặt máy.",
                    action = ProposedAction(
                        type = "open_app",
                        params = buildJsonObject {
                            put("package", "com.android.settings")
                        }
                    )
                )
            }

            query.contains("spotify") -> {
                ModelReply(
                    response = "Đang mở Spotify nghe nhạc.",
                    action = ProposedAction(
                        type = "open_app",
                        params = buildJsonObject {
                            put("package", "com.spotify.music")
                        }
                    )
                )
            }

            query.contains("âm lượng") -> {
                val match = numberRegex.find(query)
                val level = match?.value?.toIntOrNull()?.coerceIn(0, 100) ?: 50
                ModelReply(
                    response = "Đang chỉnh âm lượng về $level%.",
                    action = ProposedAction(
                        type = "set_volume",
                        params = buildJsonObject {
                            put("level", level)
                        }
                    )
                )
            }

            query.contains("chào") || query.contains("hello") || query.contains("hi") -> {
                ModelReply(
                    response = "Chào bạn! Mình là Robot. Mình có thể giúp gì cho bạn hôm nay?",
                    action = null
                )
            }

            else -> {
                ModelReply(
                    response = "Xin lỗi, mình chưa hiểu yêu cầu đó.",
                    action = null
                )
            }
        }

        return Result.success(reply)
    }
}
