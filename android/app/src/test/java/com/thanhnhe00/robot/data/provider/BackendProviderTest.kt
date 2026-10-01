package com.thanhnhe00.robot.data.provider

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class BackendProviderTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testGenerateSuccess200() = runTest {
        val mockJson = """
            {
                "response": "Bây giờ là 14:00.",
                "action": {
                    "type": "get_time",
                    "params": {}
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mockJson)
        )

        val provider = BackendProvider(
            baseUrl = mockWebServer.url("/").toString(),
            apiKey = "test-secret-key"
        )

        val result = provider.generate(ChatRequest(text = "mấy giờ"))
        assertTrue(result.isSuccess)
        val reply = result.getOrThrow()
        assertEquals("Bây giờ là 14:00.", reply.response)
        assertNotNull(reply.action)
        assertEquals("get_time", reply.action?.type)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/chat", recordedRequest.path)
        assertEquals("test-secret-key", recordedRequest.getHeader("X-API-Key"))
    }

    @Test
    fun testGenerateUnauthorized401() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("{\"detail\": \"Unauthorized\"}")
        )

        val provider = BackendProvider(
            baseUrl = mockWebServer.url("/").toString(),
            apiKey = "wrong-key"
        )

        val result = provider.generate(ChatRequest(text = "xin chào"))
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue("Lỗi phải là Unauthorized", error is ProviderError.Unauthorized)
    }

    @Test
    fun testGenerateGatewayError502() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(502)
                .setBody("Bad Gateway")
        )

        val provider = BackendProvider(baseUrl = mockWebServer.url("/").toString())

        val result = provider.generate(ChatRequest(text = "xin chào"))
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is ProviderError.ProviderUnavailable)
        assertEquals(502, (error as ProviderError.ProviderUnavailable).statusCode)
    }

    @Test
    fun testGenerateServiceUnavailable503() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(503)
                .setBody("Service Unavailable")
        )

        val provider = BackendProvider(baseUrl = mockWebServer.url("/").toString())

        val result = provider.generate(ChatRequest(text = "xin chào"))
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is ProviderError.ProviderUnavailable)
        assertEquals(503, (error as ProviderError.ProviderUnavailable).statusCode)
    }

    @Test
    fun testGenerateMalformedJson() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("{ hỏng json không parse được")
        )

        val provider = BackendProvider(baseUrl = mockWebServer.url("/").toString())

        val result = provider.generate(ChatRequest(text = "xin chào"))
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is ProviderError.InvalidResponse)
    }

    @Test
    fun testGenerateMissingRequiredField() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("{\"wrong_field\": 123}")
        )

        val provider = BackendProvider(baseUrl = mockWebServer.url("/").toString())

        val result = provider.generate(ChatRequest(text = "xin chào"))
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is ProviderError.InvalidResponse)
    }

    @Test
    fun testGenerateTimeout() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("{\"response\": \"ok\"}")
                .setBodyDelay(500, TimeUnit.MILLISECONDS)
        )

        val shortTimeoutClient = OkHttpClient.Builder()
            .readTimeout(100, TimeUnit.MILLISECONDS)
            .build()

        val provider = BackendProvider(
            baseUrl = mockWebServer.url("/").toString(),
            client = shortTimeoutClient
        )

        val result = provider.generate(ChatRequest(text = "xin chào"))
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue("Lỗi phải là Timeout", error is ProviderError.Timeout)
    }
}
