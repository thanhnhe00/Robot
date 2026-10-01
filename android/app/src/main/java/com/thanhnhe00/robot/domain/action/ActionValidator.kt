package com.thanhnhe00.robot.domain.action

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

/**
 * Bộ kiểm tra an toàn Action trên điện thoại Android (ADR-0001, ADR-0006).
 *
 * Nhiệm vụ:
 * 1. Kiểm tra action do model đề xuất qua schema, whitelist, kiểu dữ liệu, giới hạn.
 * 2. Độc lập hoàn toàn với backend (chạy offline).
 * 3. Tuyệt đối KHÔNG ném ngoại lệ ra ngoài (mọi input xấu trả về ValidationResult.Rejected).
 * 4. Chỉ khi hợp lệ mới tạo đối tượng ValidatedAction an toàn cho ActionExecutor.
 */
object ActionValidator {

    private val TIME_REGEX = Regex("""^([01]\d|2[0-3]):[0-5]\d$""")

    val ALLOWED_PACKAGES: Set<String> = setOf(
        "com.google.android.youtube",
        "com.android.chrome",
        "com.android.settings",
        "com.spotify.music"
    )

    val REGISTRY: Map<String, ActionSpec> = mapOf(
        "get_time" to ActionSpec(
            name = "get_time",
            timeoutMs = 1000L,
            permission = "none",
            hardware = HardwareStatus.READY
        ),
        "get_battery" to ActionSpec(
            name = "get_battery",
            timeoutMs = 1000L,
            permission = "none",
            hardware = HardwareStatus.READY
        ),
        "set_alarm" to ActionSpec(
            name = "set_alarm",
            timeoutMs = 3000L,
            permission = "com.android.alarm.permission.SET_ALARM",
            hardware = HardwareStatus.READY
        ),
        "open_app" to ActionSpec(
            name = "open_app",
            timeoutMs = 5000L,
            permission = "android.intent.action.MAIN",
            hardware = HardwareStatus.READY
        ),
        "set_volume" to ActionSpec(
            name = "set_volume",
            timeoutMs = 2000L,
            permission = "android.permission.MODIFY_AUDIO_SETTINGS",
            hardware = HardwareStatus.READY
        ),
        "move" to ActionSpec(
            name = "move",
            timeoutMs = 5000L,
            permission = "robot.permission.MOTOR_CONTROL",
            hardware = HardwareStatus.STUB
        ),
        "stop" to ActionSpec(
            name = "stop",
            timeoutMs = 1000L,
            permission = "robot.permission.MOTOR_CONTROL",
            hardware = HardwareStatus.STUB
        )
    )

    /**
     * Xác thực ProposedAction đã được parse sơ bộ thành object.
     */
    fun validate(proposed: ProposedAction?): ValidationResult {
        if (proposed == null) {
            return ValidationResult.Rejected(RejectionReason.MALFORMED)
        }
        return validateInternal(proposed.type, proposed.params)
    }

    /**
     * Xác thực JsonElement thô nhận từ mạng hoặc file test vector.
     * Kiểm tra toàn diện cả trường hợp action không phải object, params không phải object.
     */
    fun validateJson(raw: JsonElement?): ValidationResult {
        try {
            if (raw == null || raw !is JsonObject) {
                return ValidationResult.Rejected(RejectionReason.MALFORMED)
            }

            val typeElement = raw["type"]
            if (typeElement !is JsonPrimitive || !typeElement.isString) {
                return ValidationResult.Rejected(RejectionReason.UNKNOWN_ACTION)
            }

            val paramsElement = raw["params"]
            val paramsObj: JsonObject? = when (paramsElement) {
                null, is JsonNull -> null
                is JsonObject -> paramsElement
                is JsonArray, is JsonPrimitive -> {
                    // params không phải object và không phải null -> MALFORMED
                    return ValidationResult.Rejected(RejectionReason.MALFORMED)
                }
            }

            return validateInternal(typeElement.content, paramsObj)
        } catch (_: Exception) {
            return ValidationResult.Rejected(RejectionReason.MALFORMED)
        }
    }

    private fun validateInternal(rawType: String, params: JsonObject?): ValidationResult {
        val trimmedType = rawType.trim()
        val spec = REGISTRY[trimmedType]
            ?: return ValidationResult.Rejected(RejectionReason.UNKNOWN_ACTION)

        if (spec.hardware == HardwareStatus.STUB) {
            return ValidationResult.Rejected(RejectionReason.HARDWARE_NOT_READY)
        }

        val effectiveParams = params ?: JsonObject(emptyMap())

        return when (trimmedType) {
            "get_time" -> ValidationResult.Valid(ValidatedAction.GetTime)
            "get_battery" -> ValidationResult.Valid(ValidatedAction.GetBattery)
            "set_alarm" -> validateSetAlarm(effectiveParams)
            "open_app" -> validateOpenApp(effectiveParams)
            "set_volume" -> validateSetVolume(effectiveParams)
            else -> ValidationResult.Rejected(RejectionReason.UNKNOWN_ACTION)
        }
    }

    private fun validateSetAlarm(params: JsonObject): ValidationResult {
        val timeElement = params["time"] ?: return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
        if (timeElement !is JsonPrimitive || !timeElement.isString) {
            return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
        }

        val timeStr = timeElement.content
        if (!TIME_REGEX.matches(timeStr)) {
            return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
        }

        val parts = timeStr.split(":")
        val hour = parts[0].toIntOrNull() ?: return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
        val minute = parts[1].toIntOrNull() ?: return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)

        val labelElement = params["label"]
        val labelString = when (labelElement) {
            null, is JsonNull -> null
            is JsonPrimitive -> {
                if (!labelElement.isString) {
                    // label không phải chuỗi (ví dụ số 5) -> INVALID_PARAMS
                    return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
                }
                val trimmed = labelElement.content.trim()
                if (trimmed.isEmpty()) null else takeCodePoints(trimmed, 100)
            }
            else -> return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
        }

        return ValidationResult.Valid(ValidatedAction.SetAlarm(hour, minute, labelString))
    }

    private fun validateOpenApp(params: JsonObject): ValidationResult {
        val pkgElement = params["package"] ?: return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
        if (pkgElement !is JsonPrimitive || !pkgElement.isString) {
            return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
        }

        val pkgName = pkgElement.content.trim()
        if (pkgName !in ALLOWED_PACKAGES) {
            return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
        }

        return ValidationResult.Valid(ValidatedAction.OpenApp(pkgName))
    }

    private fun validateSetVolume(params: JsonObject): ValidationResult {
        val levelElement = params["level"] ?: return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
        if (levelElement !is JsonPrimitive || levelElement.isString) {
            return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
        }

        // Từ chối boolean (true/false)
        if (levelElement.booleanOrNull != null) {
            return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
        }

        val content = levelElement.content
        // Từ chối số thực (chứa dấu chấm), ký hiệu số mũ (e/E), vô cực
        if (content.contains('.') || content.contains('e', ignoreCase = true)) {
            return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
        }

        val intLevel = content.toIntOrNull() ?: return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
        if (intLevel !in 0..100) {
            return ValidationResult.Rejected(RejectionReason.INVALID_PARAMS)
        }

        return ValidationResult.Valid(ValidatedAction.SetVolume(intLevel))
    }

    /**
     * Cắt chuỗi an toàn theo số lượng Unicode code points thay vì ký tự UTF-16 thô.
     */
    private fun takeCodePoints(s: String, maxCodePoints: Int): String {
        var count = 0
        var offset = 0
        while (offset < s.length && count < maxCodePoints) {
            val codePoint = s.codePointAt(offset)
            offset += Character.charCount(codePoint)
            count++
        }
        return s.substring(0, offset)
    }
}
