package com.thanhnhe00.robot.data.provider.local

import com.thanhnhe00.robot.data.provider.ModelReply
import com.thanhnhe00.robot.domain.action.ProposedAction
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Trình phân tích kết quả sinh chữ thô của Local LLM thành ModelReply (Phase 4.2).
 *
 * Xử lý:
 * - Trích xuất JSON hợp lệ (kể cả khi bị bao quanh bởi markdown code blocks hoặc văn bản thừa).
 * - Trích xuất `response` (chuỗi hiển thị) và `action` (`ProposedAction(type, params)`).
 * - Trường hợp JSON bị lỗi hoặc rỗng: Trả về câu fallback an toàn cố định của Robot
 *   kèm `action = null`.
 */
object LocalModelOutputParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    const val FALLBACK_MESSAGE = "Xin lỗi, mình chưa hiểu yêu cầu đó."

    fun parse(rawOutput: String): ModelReply {
        val trimmed = rawOutput.trim()
        if (trimmed.isEmpty()) {
            return ModelReply(response = FALLBACK_MESSAGE, action = null)
        }

        val jsonString = extractJsonCandidate(trimmed)
            ?: return ModelReply(response = FALLBACK_MESSAGE, action = null)

        return try {
            val root = json.parseToJsonElement(jsonString).jsonObject

            // 1. Trích xuất response
            val responseText = root["response"]?.jsonPrimitive?.content?.trim()
                ?.ifEmpty { null }
                ?: FALLBACK_MESSAGE

            // 2. Trích xuất action nếu có
            val actionElement = root["action"]
            val proposedAction = if (actionElement != null && actionElement is JsonObject) {
                val type = actionElement["type"]?.jsonPrimitive?.content?.trim()
                if (!type.isNullOrEmpty()) {
                    val params = actionElement["params"] as? JsonObject
                    ProposedAction(type = type, params = params)
                } else {
                    null
                }
            } else {
                null
            }

            ModelReply(response = responseText, action = proposedAction)
        } catch (_: Exception) {
            ModelReply(response = FALLBACK_MESSAGE, action = null)
        }
    }

    /**
     * Trích xuất khối chuỗi có khả năng là JSON:
     * - Bóc tách khỏi ```json ... ``` nếu có
     * - Tìm cặp dấu ngoặc nhọn ngoài cùng '{' ... '}'
     */
    private fun extractJsonCandidate(text: String): String? {
        // 1. Kiểm tra markdown fence
        val fenceRegex = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```", RegexOption.IGNORE_CASE)
        val match = fenceRegex.find(text)
        if (match != null) {
            val content = match.groupValues[1].trim()
            val start = content.indexOf('{')
            val end = content.lastIndexOf('}')
            if (start != -1 && end != -1 && end > start) {
                return content.substring(start, end + 1)
            }
        }

        // 2. Tìm cặp ngoặc nhọn '{' và '}' ngoài cùng trong toàn bộ văn bản
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1)
        }

        return null
    }
}
