package com.thanhnhe00.robot.domain.model

import kotlinx.serialization.Serializable

/**
 * Metadata thông tin của một mô hình LLM dạng GGUF (Phase 4.1).
 *
 * Lưu ý kiến trúc: ModelMetadata chỉ đại diện cho thông tin tệp và cấu hình của artifact,
 * KHÔNG sở hữu native pointer hay native lifecycle của llama.cpp.
 */
@Serializable
data class ModelMetadata(
    val id: String,
    val displayName: String,
    val fileName: String,
    val filePath: String? = null,
    val format: String = "GGUF",
    val sizeBytes: Long,
    val sha256: String,
    val architecture: String = "llama",
    val parameterSize: String? = null,
    val quantization: String = "Q4_K_M",
    val estimatedMemoryBytes: Long = (sizeBytes * 1.3).toLong()
)
