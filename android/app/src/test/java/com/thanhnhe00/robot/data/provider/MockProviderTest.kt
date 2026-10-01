package com.thanhnhe00.robot.data.provider

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockProviderTest {

    private val provider = MockProvider()

    @Test
    fun testGetTimeQuery() = runTest {
        val result = provider.generate(ChatRequest(text = "Mấy giờ rồi robot?"))
        assertTrue(result.isSuccess)
        val reply = result.getOrThrow()
        assertEquals("get_time", reply.action?.type)
    }

    @Test
    fun testGetBatteryQuery() = runTest {
        val result = provider.generate(ChatRequest(text = "Kiểm tra mức pin giúp mình"))
        assertTrue(result.isSuccess)
        val reply = result.getOrThrow()
        assertEquals("get_battery", reply.action?.type)
    }

    @Test
    fun testSetAlarmQuery() = runTest {
        val result = provider.generate(ChatRequest(text = "Đặt báo thức lúc 07:30"))
        assertTrue(result.isSuccess)
        val reply = result.getOrThrow()
        assertEquals("set_alarm", reply.action?.type)
        assertNotNull(reply.action?.params)
    }

    @Test
    fun testOpenYoutubeQuery() = runTest {
        val result = provider.generate(ChatRequest(text = "Mở YouTube nghe nhạc"))
        assertTrue(result.isSuccess)
        val reply = result.getOrThrow()
        assertEquals("open_app", reply.action?.type)
    }

    @Test
    fun testSetVolumeQuery() = runTest {
        val result = provider.generate(ChatRequest(text = "Chỉnh âm lượng 70"))
        assertTrue(result.isSuccess)
        val reply = result.getOrThrow()
        assertEquals("set_volume", reply.action?.type)
    }

    @Test
    fun testGreetingQuery() = runTest {
        val result = provider.generate(ChatRequest(text = "Xin chào bạn"))
        assertTrue(result.isSuccess)
        val reply = result.getOrThrow()
        assertNull(reply.action)
        assertTrue(reply.response.contains("Chào bạn"))
    }

    @Test
    fun testUnknownQueryFallback() = runTest {
        val result = provider.generate(ChatRequest(text = "Hôm nay nấu món gì ăn ngon nhỉ?"))
        assertTrue(result.isSuccess)
        val reply = result.getOrThrow()
        assertNull(reply.action)
        assertTrue(reply.response.contains("chưa hiểu"))
    }
}
