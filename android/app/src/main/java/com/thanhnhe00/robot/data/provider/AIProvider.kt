package com.thanhnhe00.robot.data.provider

import com.thanhnhe00.robot.domain.action.ProposedAction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    @SerialName("session_id")
    val sessionId: String = "android_session",
    @SerialName("text")
    val text: String
)

data class ModelReply(
    val response: String,
    val action: ProposedAction? = null
)

sealed class ProviderError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    data class Unauthorized(
        val msg: String = "401 Unauthorized: Khóa API không hợp lệ hoặc thiếu"
    ) : ProviderError(msg)

    data class ProviderUnavailable(
        val statusCode: Int,
        val msg: String = "Nhà cung cấp AI không khả dụng ($statusCode)"
    ) : ProviderError(msg)

    data class Network(
        val msg: String,
        override val cause: Throwable? = null
    ) : ProviderError(msg, cause)

    data class Timeout(
        val msg: String = "Kết nối đến nhà cung cấp AI bị quá thời gian (timeout)"
    ) : ProviderError(msg)

    data class InvalidResponse(
        val msg: String
    ) : ProviderError(msg)
}

/**
 * Giao diện nhà cung cấp AI phía Android (ADR-0001, ADR-0006).
 *
 * Mọi nguồn AI (Backend FastAPI trên laptop, Mock offline, hoặc Local LLM trên máy ở Phase 4)
 * đều triển khai interface này, giúp UI hoàn toàn độc lập với nguồn suy luận.
 */
interface AIProvider {
    suspend fun generate(request: ChatRequest): Result<ModelReply>
}
