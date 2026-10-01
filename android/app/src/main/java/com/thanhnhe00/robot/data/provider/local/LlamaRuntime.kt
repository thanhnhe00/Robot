package com.thanhnhe00.robot.data.provider.local

/**
 * Interface trừu tượng quản lý vòng đời và suy luận của native LLM engine (Phase 4.2).
 */
interface LlamaRuntime {
    val isNativeLoaded: Boolean
    val isModelLoaded: Boolean
    val currentModelPath: String?

    /**
     * Nạp model GGUF vào bộ nhớ RAM native.
     * @param modelPath Đường dẫn tuyệt đối tới tệp .gguf
     * @param threads Số luồng CPU (mặc định 4 hoặc 6)
     * @param contextSize Kích thước context (mặc định 1024 hoặc 2048)
     */
    suspend fun loadModel(
        modelPath: String,
        threads: Int = DEFAULT_THREADS,
        contextSize: Int = DEFAULT_CONTEXT_SIZE
    ): Result<Unit>

    /**
     * Sinh phản hồi từ chuỗi prompt.
     */
    suspend fun generate(
        prompt: String,
        temperature: Float = DEFAULT_TEMPERATURE,
        maxTokens: Int = DEFAULT_MAX_TOKENS
    ): Result<String>

    /**
     * Giải phóng mô hình và toàn bộ con trỏ native C++ khỏi RAM.
     */
    suspend fun unloadModel(): Result<Unit>

    companion object {
        const val DEFAULT_THREADS = 4
        const val DEFAULT_CONTEXT_SIZE = 1024
        const val DEFAULT_TEMPERATURE = 0.2f
        const val DEFAULT_MAX_TOKENS = 256
    }
}
