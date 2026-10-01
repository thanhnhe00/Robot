package com.thanhnhe00.robot.data.provider.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cầu nối native/local tới llama.cpp runtime trên Android ARM64 (Phase 4.2 & 4.3).
 *
 * Thiết kế an toàn (Crash-safe & Flexible):
 * 1. Hỗ trợ JNI libllama.so nếu được đóng gói trong APK.
 * 2. Tự động liên kết với tiến trình on-device llama-server chạy cục bộ tại 127.0.0.1:8080.
 * 3. Đảm bảo chạy 100% on-device không phụ thuộc cloud hay internet.
 */
class NativeLlamaBridge(
    private val localServerUrl: String = "http://127.0.0.1:8080"
) : LlamaRuntime {

    private var nativeLoaded = false
    private var modelLoaded = false
    private var activeModelPath: String? = null

    init {
        nativeLoaded = try {
            System.loadLibrary("llama")
            true
        } catch (_: Throwable) {
            false
        }
    }

    override val isNativeLoaded: Boolean
        get() = nativeLoaded || isLocalServerActive()

    override val isModelLoaded: Boolean
        get() = modelLoaded || isLocalServerActive()

    override val currentModelPath: String?
        get() = activeModelPath

    private fun isLocalServerActive(): Boolean {
        return try {
            val url = URL("$localServerUrl/health")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 800
            conn.readTimeout = 800
            conn.requestMethod = "GET"
            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        } catch (_: Throwable) {
            false
        }
    }

    override suspend fun loadModel(
        modelPath: String,
        threads: Int,
        contextSize: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (nativeLoaded) {
            try {
                val success = nativeLoadModel(modelPath, threads, contextSize)
                if (success) {
                    modelLoaded = true
                    activeModelPath = modelPath
                    return@withContext Result.success(Unit)
                }
            } catch (_: Throwable) {
                // Tiếp tục thử kiểm tra on-device server
            }
        }

        if (isLocalServerActive()) {
            modelLoaded = true
            activeModelPath = modelPath
            Result.success(Unit)
        } else {
            Result.failure(
                IllegalStateException("Mô hình chưa sẵn sàng trên native runtime hoặc local server (127.0.0.1:8080).")
            )
        }
    }

    override suspend fun generate(
        prompt: String,
        temperature: Float,
        maxTokens: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        if (nativeLoaded && modelLoaded) {
            try {
                val response = nativeGenerate(prompt, temperature, maxTokens)
                if (response != null) {
                    return@withContext Result.success(response)
                }
            } catch (_: Throwable) {
                // Tiếp tục fallback local server
            }
        }

        // Thực thi qua tiến trình llama on-device
        try {
            val url = URL("$localServerUrl/completion")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 60000
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true

            val jsonBody = JSONObject().apply {
                put("prompt", prompt)
                put("temperature", temperature.toDouble())
                put("n_predict", maxTokens)
                put("stream", false)
            }

            conn.outputStream.use { os ->
                os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
            }

            val code = conn.responseCode
            if (code in 200..299) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val respJson = JSONObject(responseText)
                val content = respJson.optString("content", "")
                conn.disconnect()
                Result.success(content)
            } else {
                conn.disconnect()
                Result.failure(IllegalStateException("Local LLM Server trả về mã lỗi HTTP $code"))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override suspend fun unloadModel(): Result<Unit> = withContext(Dispatchers.IO) {
        if (nativeLoaded) {
            try {
                nativeUnloadModel()
            } catch (_: Throwable) {}
        }
        modelLoaded = false
        activeModelPath = null
        Result.success(Unit)
    }

    // Các hàm JNI C++ (khi có libllama.so được load)
    private external fun nativeLoadModel(modelPath: String, threads: Int, contextSize: Int): Boolean
    private external fun nativeGenerate(prompt: String, temperature: Float, maxTokens: Int): String?
    private external fun nativeUnloadModel()
}
