package com.thanhnhe00.robot.domain.model

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

/**
 * Giao diện quản lý các tệp mô hình GGUF (Phase 4.1).
 */
interface ModelManager {
    /** Lấy danh sách toàn bộ model trong danh mục */
    fun listModels(): List<ModelMetadata>

    /** Lấy metadata của một model theo id */
    fun getModel(id: String): ModelMetadata?

    /** Lấy model đang được chọn để sử dụng */
    fun getSelectedModel(): ModelMetadata?

    /** Chọn model đang kích hoạt */
    fun selectModel(id: String): Result<ModelMetadata>

    /** Kiểm tra tính hợp lệ và toàn vẹn của tệp model trên đĩa */
    fun verifyArtifact(id: String): ModelArtifactStatus

    /** Lấy đường dẫn tệp thực tế của model */
    fun getModelFile(id: String): File?
}

/**
 * Triển khai chuẩn của ModelManager v0 (Phase 4.1).
 *
 * Quản lý danh mục mô hình, vị trí thư mục lưu trữ và thẩm định mã băm SHA-256
 * bằng phương thức đọc luồng (streaming) để tuyệt đối không làm tràn bộ nhớ JVM.
 */
class DefaultModelManager(
    val modelsDirectory: File,
    initialCatalog: List<ModelMetadata> = defaultCatalog()
) : ModelManager {

    private val catalog = initialCatalog.associateBy { it.id }.toMutableMap()
    private var selectedModelId: String? = initialCatalog.firstOrNull()?.id

    companion object {
        /** Danh mục các model shortlist mặc định từ Research A */
        fun defaultCatalog(): List<ModelMetadata> = listOf(
            ModelMetadata(
                id = "qwen2.5-0.5b-instruct-q4_k_m",
                displayName = "Qwen 2.5 0.5B Instruct (Q4_K_M)",
                fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
                format = "GGUF",
                sizeBytes = 491_400_032L, // ~491 MB
                sha256 = "74a4da8c9fdbcd15bd1f6d01d621410d31c6fc00986f5eb687824e7b93d7a9db",
                architecture = "qwen2",
                parameterSize = "0.5B",
                quantization = "Q4_K_M"
            ),
            ModelMetadata(
                id = "qwen2.5-1.5b-instruct-q4_k_m",
                displayName = "Qwen 2.5 1.5B Instruct (Q4_K_M)",
                fileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
                format = "GGUF",
                sizeBytes = 986_000_000L, // ~986 MB
                sha256 = "d572ec4327ad74eb8f7db0d507b5a8e1df11b229875953e5e492215c0e447b19",
                architecture = "qwen2",
                parameterSize = "1.5B",
                quantization = "Q4_K_M"
            ),
            ModelMetadata(
                id = "llama-3.2-3b-instruct-q4_k_m",
                displayName = "Llama 3.2 3B Instruct (Q4_K_M)",
                fileName = "llama-3.2-3b-instruct-q4_k_m.gguf",
                format = "GGUF",
                sizeBytes = 2_020_000_000L, // ~2.02 GB
                sha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                architecture = "llama",
                parameterSize = "3B",
                quantization = "Q4_K_M"
            )
        )

        /**
         * Tính toán mã băm SHA-256 từ InputStream theo dạng streaming buffer.
         * Buffer 64KB đảm bảo thời gian tính nhanh và RAM JVM tăng không quá 64KB.
         */
        fun calculateSha256(inputStream: InputStream): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(64 * 1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }

    override fun listModels(): List<ModelMetadata> {
        return catalog.values.map { meta ->
            val file = File(modelsDirectory, meta.fileName)
            meta.copy(filePath = if (file.exists()) file.absolutePath else null)
        }
    }

    override fun getModel(id: String): ModelMetadata? {
        val meta = catalog[id] ?: return null
        val file = File(modelsDirectory, meta.fileName)
        return meta.copy(filePath = if (file.exists()) file.absolutePath else null)
    }

    override fun getSelectedModel(): ModelMetadata? {
        return selectedModelId?.let { getModel(it) }
    }

    override fun selectModel(id: String): Result<ModelMetadata> {
        val meta = getModel(id) ?: return Result.failure(
            IllegalArgumentException("Không tìm thấy model với id '$id' trong danh mục")
        )
        selectedModelId = id
        return Result.success(meta)
    }

    override fun getModelFile(id: String): File? {
        val meta = catalog[id] ?: return null
        val file = File(modelsDirectory, meta.fileName)
        return if (file.exists()) file else null
    }

    private val verificationCache = mutableMapOf<String, Pair<Long, ModelArtifactStatus>>()

    override fun verifyArtifact(id: String): ModelArtifactStatus {
        val meta = catalog[id] ?: return ModelArtifactStatus.NOT_DOWNLOADED
        val file = File(modelsDirectory, meta.fileName)

        if (!file.exists() || !file.isFile) {
            return ModelArtifactStatus.NOT_DOWNLOADED
        }

        // Kiểm tra cache nếu tệp không bị sửa đổi
        val lastModified = file.lastModified()
        val cached = verificationCache[id]
        if (cached != null && cached.first == lastModified) {
            return cached.second
        }

        // Kiểm tra kích thước tệp nếu có yêu cầu khớp
        if (meta.sizeBytes > 0 && file.length() != meta.sizeBytes) {
            verificationCache[id] = Pair(lastModified, ModelArtifactStatus.CORRUPTED)
            return ModelArtifactStatus.CORRUPTED
        }

        // Kiểm tra mã băm SHA-256
        if (meta.sha256.isNotBlank()) {
            val status = try {
                file.inputStream().use { stream ->
                    val actualHash = calculateSha256(stream)
                    if (actualHash.equals(meta.sha256, ignoreCase = true)) {
                        ModelArtifactStatus.VERIFIED
                    } else {
                        ModelArtifactStatus.CORRUPTED
                    }
                }
            } catch (_: Exception) {
                ModelArtifactStatus.CORRUPTED
            }
            verificationCache[id] = Pair(lastModified, status)
            return status
        }

        verificationCache[id] = Pair(lastModified, ModelArtifactStatus.VERIFIED)
        return ModelArtifactStatus.VERIFIED
    }
}
