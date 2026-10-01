package com.thanhnhe00.robot.data.provider

import com.thanhnhe00.robot.domain.action.ProposedAction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

@Serializable
internal data class BackendChatResponse(
    val response: String,
    val action: ProposedAction? = null,
    @SerialName("action_rejection")
    val actionRejection: String? = null
)

class BackendProvider(
    private val baseUrl: String,
    private val apiKey: String = "",
    private val client: OkHttpClient = defaultOkHttpClient(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val json: Json = defaultJson()
) : AIProvider {

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        fun defaultOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()
        }

        fun defaultJson(): Json {
            return Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            }
        }
    }

    override suspend fun generate(request: ChatRequest): Result<ModelReply> = withContext(dispatcher) {
        val requestUrl = "${baseUrl.trimEnd('/')}/chat"
        val jsonPayload = try {
            json.encodeToString(request)
        } catch (e: Exception) {
            return@withContext Result.failure(ProviderError.InvalidResponse("Không thể mã hóa request: ${e.message}"))
        }

        val httpRequestBuilder = Request.Builder()
            .url(requestUrl)
            .post(jsonPayload.toRequestBody(JSON_MEDIA_TYPE))
            .addHeader("Content-Type", "application/json")

        if (apiKey.isNotBlank()) {
            httpRequestBuilder.addHeader("X-API-Key", apiKey)
        }

        val httpRequest = httpRequestBuilder.build()

        try {
            client.newCall(httpRequest).execute().use { response ->
                when (response.code) {
                    200 -> {
                        val bodyString = response.body?.string()
                            ?: return@withContext Result.failure(ProviderError.InvalidResponse("Phản hồi rỗng"))
                        val parsed = json.decodeFromString<BackendChatResponse>(bodyString)
                        Result.success(
                            ModelReply(
                                response = parsed.response,
                                action = parsed.action
                            )
                        )
                    }

                    401 -> Result.failure(ProviderError.Unauthorized())

                    502, 503 -> Result.failure(
                        ProviderError.ProviderUnavailable(
                            statusCode = response.code,
                            msg = "Máy chủ AI không khả dụng (mã ${response.code})"
                        )
                    )

                    else -> Result.failure(
                        ProviderError.ProviderUnavailable(
                            statusCode = response.code,
                            msg = "Lỗi phản hồi HTTP ${response.code}"
                        )
                    )
                }
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(ProviderError.Timeout())
        } catch (e: SerializationException) {
            Result.failure(ProviderError.InvalidResponse("Lỗi cú pháp phản hồi JSON: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(ProviderError.Network("Lỗi kết nối mạng: ${e.localizedMessage ?: "Mất kết nối"}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
