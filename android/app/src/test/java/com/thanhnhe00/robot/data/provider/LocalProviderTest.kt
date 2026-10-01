package com.thanhnhe00.robot.data.provider

import com.thanhnhe00.robot.data.provider.local.LlamaRuntime
import com.thanhnhe00.robot.data.provider.local.LocalModelOutputParser
import com.thanhnhe00.robot.data.provider.local.LocalPromptBuilder
import com.thanhnhe00.robot.data.provider.local.LocalProvider
import com.thanhnhe00.robot.domain.model.DefaultModelManager
import com.thanhnhe00.robot.domain.model.MemoryGuard
import com.thanhnhe00.robot.domain.model.ModelMetadata
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File

class LocalProviderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    // ==========================================
    // 1. Tests cho LocalPromptBuilder
    // ==========================================

    @Test
    fun promptBuilder_buildsValidChatMlFormat() {
        val prompt = LocalPromptBuilder.buildPrompt("Mở YouTube giúp tôi")
        assertTrue(prompt.contains("<|im_start|>system"))
        assertTrue(prompt.contains("Bạn là Robot"))
        assertTrue(prompt.contains("<|im_start|>user\nMở YouTube giúp tôi<|im_end|>"))
        assertTrue(prompt.endsWith("<|im_start|>assistant\n"))
    }

    // ==========================================
    // 2. Tests cho LocalModelOutputParser
    // ==========================================

    @Test
    fun outputParser_parsesValidActionJson() {
        val raw = """
            {
              "response": "Đang mở YouTube cho bạn.",
              "action": {
                "type": "open_app",
                "params": {
                  "package": "com.google.android.youtube"
                }
              }
            }
        """.trimIndent()

        val reply = LocalModelOutputParser.parse(raw)
        assertEquals("Đang mở YouTube cho bạn.", reply.response)
        assertNotNull(reply.action)
        assertEquals("open_app", reply.action?.type)
        assertEquals(
            "\"com.google.android.youtube\"",
            reply.action?.params?.get("package")?.toString()
        )
    }

    @Test
    fun outputParser_parsesConversationWithoutAction() {
        val raw = """
            {
              "response": "Chào bạn! Mình là Robot AI.",
              "action": null
            }
        """.trimIndent()

        val reply = LocalModelOutputParser.parse(raw)
        assertEquals("Chào bạn! Mình là Robot AI.", reply.response)
        assertNull(reply.action)
    }

    @Test
    fun outputParser_extractsJsonFromMarkdownFences() {
        val raw = """
            Dưới đây là câu trả lời của tôi:
            ```json
            {
              "response": "Bây giờ là 10 giờ 15 phút.",
              "action": {
                "type": "get_time",
                "params": {}
              }
            }
            ```
            Chúc bạn một ngày tốt lành!
        """.trimIndent()

        val reply = LocalModelOutputParser.parse(raw)
        assertEquals("Bây giờ là 10 giờ 15 phút.", reply.response)
        assertNotNull(reply.action)
        assertEquals("get_time", reply.action?.type)
    }

    @Test
    fun outputParser_fallsBackSafelyOnMalformedJson() {
        val raw = "Đây là văn bản hoàn toàn không phải JSON hoặc bị lỗi { malformed ..."
        val reply = LocalModelOutputParser.parse(raw)

        assertEquals(LocalModelOutputParser.FALLBACK_MESSAGE, reply.response)
        assertNull(reply.action)
    }

    @Test
    fun outputParser_fallsBackOnEmptyString() {
        val reply = LocalModelOutputParser.parse("   ")
        assertEquals(LocalModelOutputParser.FALLBACK_MESSAGE, reply.response)
        assertNull(reply.action)
    }

    // ==========================================
    // 3. Tests cho LocalProvider (Integration logic)
    // ==========================================

    private class FakeLlamaRuntime(
        var generateResult: Result<String> = Result.success("""{"response": "Xin chào", "action": null}"""),
        var loadResult: Result<Unit> = Result.success(Unit)
    ) : LlamaRuntime {
        override var isNativeLoaded: Boolean = true
        override var isModelLoaded: Boolean = false
        override var currentModelPath: String? = null

        override suspend fun loadModel(modelPath: String, threads: Int, contextSize: Int): Result<Unit> {
            return if (loadResult.isSuccess) {
                isModelLoaded = true
                currentModelPath = modelPath
                Result.success(Unit)
            } else {
                loadResult
            }
        }

        override suspend fun generate(prompt: String, temperature: Float, maxTokens: Int): Result<String> {
            return generateResult
        }

        override suspend fun unloadModel(): Result<Unit> {
            isModelLoaded = false
            currentModelPath = null
            return Result.success(Unit)
        }
    }

    @Test
    fun localProvider_generatesSuccess_whenModelVerifiedAndMemorySufficient() = runBlocking {
        val rootDir = tempFolder.newFolder("models")
        val content = "fake-model-weights".toByteArray(Charsets.UTF_8)
        val testFile = File(rootDir, "qwen2.5-0.5b-instruct-q4_k_m.gguf").apply {
            writeBytes(content)
        }
        val expectedHash = DefaultModelManager.calculateSha256(ByteArrayInputStream(content))

        val catalog = listOf(
            ModelMetadata(
                id = "qwen2.5-0.5b-instruct-q4_k_m",
                displayName = "Qwen 0.5B",
                fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
                sizeBytes = content.size.toLong(),
                sha256 = expectedHash
            )
        )

        val modelManager = DefaultModelManager(rootDir, catalog)
        val memoryGuard = MemoryGuard(minSafetyHeadroomBytes = 500L * 1024 * 1024)
        val fakeRuntime = FakeLlamaRuntime(
            generateResult = Result.success("""{"response": "Mức pin hiện tại.", "action": {"type": "get_battery", "params": {}}}""")
        )

        val provider = LocalProvider(
            modelManager = modelManager,
            memoryGuard = memoryGuard,
            runtime = fakeRuntime,
            availableMemoryReader = { 2000L * 1024 * 1024 } // 2000 MB available
        )

        val result = provider.generate(ChatRequest(text = "Kiểm tra pin"))
        assertTrue(result.isSuccess)

        val reply = result.getOrThrow()
        assertEquals("Mức pin hiện tại.", reply.response)
        assertEquals("get_battery", reply.action?.type)
        assertTrue(fakeRuntime.isModelLoaded)
        assertEquals(testFile.absolutePath, fakeRuntime.currentModelPath)
    }

    @Test
    fun localProvider_rejects_whenMemoryInsufficient() = runBlocking {
        val rootDir = tempFolder.newFolder("models")
        val content = "fake-model-weights".toByteArray(Charsets.UTF_8)
        File(rootDir, "qwen2.5-0.5b-instruct-q4_k_m.gguf").apply {
            writeBytes(content)
        }
        val expectedHash = DefaultModelManager.calculateSha256(ByteArrayInputStream(content))

        val catalog = listOf(
            ModelMetadata(
                id = "qwen2.5-0.5b-instruct-q4_k_m",
                displayName = "Qwen 0.5B",
                fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
                sizeBytes = content.size.toLong(),
                sha256 = expectedHash,
                estimatedMemoryBytes = 1000L * 1024 * 1024 // Cần 1000 MB RAM
            )
        )

        val modelManager = DefaultModelManager(rootDir, catalog)
        val memoryGuard = MemoryGuard(minSafetyHeadroomBytes = 500L * 1024 * 1024)
        val fakeRuntime = FakeLlamaRuntime()

        val provider = LocalProvider(
            modelManager = modelManager,
            memoryGuard = memoryGuard,
            runtime = fakeRuntime,
            availableMemoryReader = { 100L * 1024 * 1024 } // Chỉ có 100 MB available -> Không đủ!
        )

        val result = provider.generate(ChatRequest(text = "Xem giờ"))
        assertTrue(result.isFailure)

        val error = result.exceptionOrNull()
        assertTrue(error is ProviderError.ProviderUnavailable)
        assertEquals(507, (error as ProviderError.ProviderUnavailable).statusCode)
        assertTrue(error.message?.contains("bảo vệ hệ thống") == true)
    }

    @Test
    fun localProvider_rejects_whenArtifactNotReady() = runBlocking {
        val rootDir = tempFolder.newFolder("models") // Thư mục rỗng, chưa có file .gguf
        val modelManager = DefaultModelManager(rootDir)
        val memoryGuard = MemoryGuard()
        val fakeRuntime = FakeLlamaRuntime()

        val provider = LocalProvider(
            modelManager = modelManager,
            memoryGuard = memoryGuard,
            runtime = fakeRuntime,
            availableMemoryReader = { 2000L * 1024 * 1024 }
        )

        val result = provider.generate(ChatRequest(text = "Chào robot"))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ProviderError.ProviderUnavailable)
    }
}
