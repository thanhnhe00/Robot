package com.thanhnhe00.robot.domain.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File

class ModelManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val json = Json { ignoreUnknownKeys = true }

    // ==========================================
    // 1. Tests cho MemoryGuard
    // ==========================================

    @Test
    fun memoryGuard_allowsLoad_whenMemorySufficient() {
        val guard = MemoryGuard(minSafetyHeadroomBytes = 500L * 1024 * 1024) // 500 MB headroom
        val availableBytes = 2000L * 1024 * 1024 // 2000 MB available
        val modelBytes = 1000L * 1024 * 1024 // 1000 MB model

        val decision = guard.evaluate(availableBytes, modelBytes)
        assertTrue(decision is MemoryGuardDecision.Allowed)

        val allowed = decision as MemoryGuardDecision.Allowed
        assertEquals(2000L * 1024 * 1024, allowed.availableBytes)
        assertEquals(1000L * 1024 * 1024, allowed.estimatedModelBytes)
        assertEquals(1000L * 1024 * 1024, allowed.remainingHeadroomBytes)
    }

    @Test
    fun memoryGuard_rejectsLoad_whenMemoryInsufficientForHeadroom() {
        val guard = MemoryGuard(minSafetyHeadroomBytes = 500L * 1024 * 1024) // 500 MB
        val availableBytes = 1200L * 1024 * 1024 // 1200 MB
        val modelBytes = 1000L * 1024 * 1024 // 1000 MB (Cần tổng 1500 MB, thiếu 300 MB)

        val decision = guard.evaluate(availableBytes, modelBytes)
        assertTrue(decision is MemoryGuardDecision.Rejected)

        val rejected = decision as MemoryGuardDecision.Rejected
        assertTrue(rejected.reason.contains("không đủ"))
        assertEquals(availableBytes, rejected.availableBytes)
    }

    @Test
    fun memoryGuard_rejectsLoad_whenInputsInvalid() {
        val guard = MemoryGuard()

        // availableBytes <= 0
        val decisionZeroAvail = guard.evaluate(0L, 500L * 1024 * 1024)
        assertTrue(decisionZeroAvail is MemoryGuardDecision.Rejected)

        // estimatedModelBytes <= 0
        val decisionZeroModel = guard.evaluate(2000L * 1024 * 1024, -10L)
        assertTrue(decisionZeroModel is MemoryGuardDecision.Rejected)
    }

    // ==========================================
    // 2. Tests cho ModelMetadata & Serialization
    // ==========================================

    @Test
    fun modelMetadata_serializationRoundtrip() {
        val original = ModelMetadata(
            id = "test-model",
            displayName = "Test Model 0.5B",
            fileName = "test.gguf",
            filePath = "/data/models/test.gguf",
            format = "GGUF",
            sizeBytes = 1024L * 1024L,
            sha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            architecture = "qwen2",
            parameterSize = "0.5B",
            quantization = "Q4_K_M"
        )

        val encoded = json.encodeToString(ModelMetadata.serializer(), original)
        val decoded = json.decodeFromString(ModelMetadata.serializer(), encoded)

        assertEquals(original, decoded)
        assertEquals((1024L * 1024L * 1.3).toLong(), decoded.estimatedMemoryBytes)
    }

    // ==========================================
    // 3. Tests cho SHA-256 Checksum Calculation
    // ==========================================

    @Test
    fun calculateSha256_computesCorrectHash() {
        // Chuỗi rỗng: e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
        val emptyStream = ByteArrayInputStream(ByteArray(0))
        val emptyHash = DefaultModelManager.calculateSha256(emptyStream)
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", emptyHash)

        // Chuỗi "Hello World": a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e
        val helloStream = ByteArrayInputStream("Hello World".toByteArray(Charsets.UTF_8))
        val helloHash = DefaultModelManager.calculateSha256(helloStream)
        assertEquals("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e", helloHash)
    }

    // ==========================================
    // 4. Tests cho ModelManager Artifact Validation
    // ==========================================

    @Test
    fun modelManager_verifyArtifact_whenFileMissing_returnsNotDownloaded() {
        val rootDir = tempFolder.newFolder("models")
        val manager = DefaultModelManager(modelsDirectory = rootDir)

        val status = manager.verifyArtifact("qwen2.5-0.5b-instruct-q4_k_m")
        assertEquals(ModelArtifactStatus.NOT_DOWNLOADED, status)
        assertNull(manager.getModelFile("qwen2.5-0.5b-instruct-q4_k_m"))
    }

    @Test
    fun modelManager_verifyArtifact_whenSizeMismatch_returnsCorrupted() {
        val rootDir = tempFolder.newFolder("models")
        val testFile = File(rootDir, "test-model.gguf").apply {
            writeBytes("Wrong size content".toByteArray())
        }

        val customCatalog = listOf(
            ModelMetadata(
                id = "test-model",
                displayName = "Test Model",
                fileName = "test-model.gguf",
                sizeBytes = 100_000L, // Kỳ vọng 100,000 bytes nhưng file chỉ có 18 bytes
                sha256 = "some-hash"
            )
        )

        val manager = DefaultModelManager(modelsDirectory = rootDir, initialCatalog = customCatalog)
        val status = manager.verifyArtifact("test-model")
        assertEquals(ModelArtifactStatus.CORRUPTED, status)
    }

    @Test
    fun modelManager_verifyArtifact_whenChecksumMismatch_returnsCorrupted() {
        val rootDir = tempFolder.newFolder("models")
        val content = "Corrupted content".toByteArray(Charsets.UTF_8)
        File(rootDir, "test-model.gguf").apply {
            writeBytes(content)
        }

        val customCatalog = listOf(
            ModelMetadata(
                id = "test-model",
                displayName = "Test Model",
                fileName = "test-model.gguf",
                sizeBytes = content.size.toLong(),
                sha256 = "0000000000000000000000000000000000000000000000000000000000000000" // Hash sai
            )
        )

        val manager = DefaultModelManager(modelsDirectory = rootDir, initialCatalog = customCatalog)
        val status = manager.verifyArtifact("test-model")
        assertEquals(ModelArtifactStatus.CORRUPTED, status)
    }

    @Test
    fun modelManager_verifyArtifact_whenValid_returnsVerified() {
        val rootDir = tempFolder.newFolder("models")
        val content = "Valid GGUF content test".toByteArray(Charsets.UTF_8)
        val testFile = File(rootDir, "valid-model.gguf").apply {
            writeBytes(content)
        }

        val expectedHash = DefaultModelManager.calculateSha256(ByteArrayInputStream(content))

        val customCatalog = listOf(
            ModelMetadata(
                id = "valid-model",
                displayName = "Valid Model",
                fileName = "valid-model.gguf",
                sizeBytes = content.size.toLong(),
                sha256 = expectedHash
            )
        )

        val manager = DefaultModelManager(modelsDirectory = rootDir, initialCatalog = customCatalog)
        val status = manager.verifyArtifact("valid-model")
        assertEquals(ModelArtifactStatus.VERIFIED, status)
        assertNotNull(manager.getModelFile("valid-model"))
        assertEquals(testFile.absolutePath, manager.getModel("valid-model")?.filePath)
    }

    @Test
    fun modelManager_listAndSelectModel() {
        val rootDir = tempFolder.newFolder("models")
        val manager = DefaultModelManager(modelsDirectory = rootDir)

        val models = manager.listModels()
        assertTrue(models.size >= 3)
        assertEquals("qwen2.5-0.5b-instruct-q4_k_m", manager.getSelectedModel()?.id)

        val selectResult = manager.selectModel("qwen2.5-1.5b-instruct-q4_k_m")
        assertTrue(selectResult.isSuccess)
        assertEquals("qwen2.5-1.5b-instruct-q4_k_m", manager.getSelectedModel()?.id)

        val invalidSelect = manager.selectModel("non-existent-id")
        assertTrue(invalidSelect.isFailure)
    }
}
