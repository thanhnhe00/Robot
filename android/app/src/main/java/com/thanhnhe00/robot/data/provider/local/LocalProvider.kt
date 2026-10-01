package com.thanhnhe00.robot.data.provider.local

import com.thanhnhe00.robot.data.provider.AIProvider
import com.thanhnhe00.robot.data.provider.ChatRequest
import com.thanhnhe00.robot.data.provider.ModelReply
import com.thanhnhe00.robot.data.provider.ProviderError
import com.thanhnhe00.robot.domain.model.MemoryGuard
import com.thanhnhe00.robot.domain.model.MemoryGuardDecision
import com.thanhnhe00.robot.domain.model.ModelArtifactStatus
import com.thanhnhe00.robot.domain.model.ModelManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Nhà cung cấp AI cục bộ (Local LLM Provider) trên thiết bị (Phase 4.2).
 *
 * Triển khai interface AIProvider sẵn có của repository (ADR-0001, ADR-0006).
 *
 * Ràng buộc an toàn bất biến:
 * 1. LocalProvider chỉ đề xuất ModelReply (với ProposedAction chưa tin cậy).
 * 2. Tuyệt đối KHÔNG gọi ActionExecutor và KHÔNG tạo ValidatedAction.
 * 3. Mọi action bắt buộc phải đi qua ActionValidator và UI duyệt của người dùng.
 */
class LocalProvider(
    private val modelManager: ModelManager,
    private val memoryGuard: MemoryGuard,
    private val runtime: LlamaRuntime,
    private val availableMemoryReader: () -> Long,
    private val threads: Int = LlamaRuntime.DEFAULT_THREADS,
    private val contextSize: Int = LlamaRuntime.DEFAULT_CONTEXT_SIZE,
    private val temperature: Float = LlamaRuntime.DEFAULT_TEMPERATURE
) : AIProvider {

    private val inferenceMutex = Mutex()

    override suspend fun generate(request: ChatRequest): Result<ModelReply> {
        val query = request.text.trim()
        if (query.isEmpty()) {
            return Result.failure(ProviderError.InvalidResponse("Nội dung yêu cầu rỗng"))
        }

        return inferenceMutex.withLock {
            // 1. Kiểm tra model đã được chọn trong ModelManager
            val selectedModel = modelManager.getSelectedModel()
                ?: return@withLock Result.failure(
                    ProviderError.ProviderUnavailable(404, "Chưa có model cục bộ nào được chọn trong ModelManager")
                )

            // 2. Thẩm định tệp artifact trên đĩa
            val artifactStatus = modelManager.verifyArtifact(selectedModel.id)
            if (artifactStatus != ModelArtifactStatus.VERIFIED) {
                return@withLock Result.failure(
                    ProviderError.ProviderUnavailable(
                        404,
                        "Tệp mô hình '${selectedModel.displayName}' chưa sẵn sàng hoặc bị lỗi (Status: $artifactStatus)"
                    )
                )
            }

            val modelFile = modelManager.getModelFile(selectedModel.id)
                ?: return@withLock Result.failure(
                    ProviderError.ProviderUnavailable(404, "Không tìm thấy tệp mô hình tại ${selectedModel.fileName}")
                )

            // 3. Preflight thẩm định an toàn RAM hệ thống trước khi nạp
            if (!runtime.isModelLoaded || runtime.currentModelPath != modelFile.absolutePath) {
                val availableBytes = availableMemoryReader()
                val decision = memoryGuard.evaluate(availableBytes, selectedModel.estimatedMemoryBytes)
                if (decision is MemoryGuardDecision.Rejected) {
                    return@withLock Result.failure(
                        ProviderError.ProviderUnavailable(
                            507, // Insufficient Storage / Memory
                            "Từ chối nạp model để bảo vệ hệ thống: ${decision.reason}"
                        )
                    )
                }

                // Nếu đang có model cũ khác thì unload trước
                if (runtime.isModelLoaded) {
                    runtime.unloadModel()
                }

                val loadResult = runtime.loadModel(modelFile.absolutePath, threads, contextSize)
                if (loadResult.isFailure) {
                    return@withLock Result.failure(
                        ProviderError.ProviderUnavailable(
                            500,
                            "Lỗi khi nạp model vào native RAM: ${loadResult.exceptionOrNull()?.message}"
                        )
                    )
                }
            }

            // 4. Xây dựng prompt tiếng Việt và JSON constraint
            val prompt = LocalPromptBuilder.buildPrompt(query)

            // 5. Thực hiện suy luận (Inference)
            val generateResult = runtime.generate(prompt, temperature)
            if (generateResult.isFailure) {
                val error = generateResult.exceptionOrNull()
                return@withLock Result.failure(
                    ProviderError.InvalidResponse("Lỗi trong quá trình suy luận native: ${error?.message}")
                )
            }

            val rawOutput = generateResult.getOrNull() ?: ""

            // 6. Bóc tách ModelReply (response + ProposedAction?)
            val modelReply = LocalModelOutputParser.parse(rawOutput)
            Result.success(modelReply)
        }
    }
}
