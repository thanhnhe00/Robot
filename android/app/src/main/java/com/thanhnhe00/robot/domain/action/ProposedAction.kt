package com.thanhnhe00.robot.domain.action

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Action do mô hình LLM hoặc backend đề xuất.
 * Đây là dữ liệu CHƯA TIN CẬY (untrusted) và bắt buộc phải đi qua ActionValidator.
 */
@Serializable
data class ProposedAction(
    val type: String,
    val params: JsonObject? = null
)
