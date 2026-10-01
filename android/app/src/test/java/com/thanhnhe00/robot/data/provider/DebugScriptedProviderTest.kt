package com.thanhnhe00.robot.data.provider

import com.thanhnhe00.robot.domain.action.ActionValidator
import com.thanhnhe00.robot.domain.action.RejectionReason
import com.thanhnhe00.robot.domain.action.ValidationResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugScriptedProviderTest {

    private val provider = DebugScriptedProvider()

    @Test
    fun testHackActionIsRejectedAsUnknownAction() = runTest {
        val result = provider.generate(ChatRequest(text = "hack"))
        assertTrue(result.isSuccess)
        val reply = result.getOrThrow()
        assertEquals("hack_system", reply.action?.type)

        val validation = ActionValidator.validate(reply.action)
        assertTrue(validation is ValidationResult.Rejected)
        assertEquals(RejectionReason.UNKNOWN_ACTION, (validation as ValidationResult.Rejected).reason)
    }

    @Test
    fun testEvilAppIsRejectedAsInvalidParams() = runTest {
        val result = provider.generate(ChatRequest(text = "evil"))
        assertTrue(result.isSuccess)
        val reply = result.getOrThrow()
        assertEquals("open_app", reply.action?.type)

        val validation = ActionValidator.validate(reply.action)
        assertTrue(validation is ValidationResult.Rejected)
        assertEquals(RejectionReason.INVALID_PARAMS, (validation as ValidationResult.Rejected).reason)
    }

    @Test
    fun testVolumeOverflowIsRejectedAsInvalidParams() = runTest {
        val result = provider.generate(ChatRequest(text = "volume_overflow"))
        assertTrue(result.isSuccess)
        val reply = result.getOrThrow()
        assertEquals("set_volume", reply.action?.type)

        val validation = ActionValidator.validate(reply.action)
        assertTrue(validation is ValidationResult.Rejected)
        assertEquals(RejectionReason.INVALID_PARAMS, (validation as ValidationResult.Rejected).reason)
    }

    @Test
    fun testMoveActionIsRejectedAsHardwareNotReady() = runTest {
        val result = provider.generate(ChatRequest(text = "move"))
        assertTrue(result.isSuccess)
        val reply = result.getOrThrow()
        assertEquals("move", reply.action?.type)

        val validation = ActionValidator.validate(reply.action)
        assertTrue(validation is ValidationResult.Rejected)
        assertEquals(RejectionReason.HARDWARE_NOT_READY, (validation as ValidationResult.Rejected).reason)
    }

    @Test
    fun testInvalidAlarmTimeIsRejectedAsInvalidParams() = runTest {
        val result = provider.generate(ChatRequest(text = "25:00"))
        assertTrue(result.isSuccess)
        val reply = result.getOrThrow()
        assertEquals("set_alarm", reply.action?.type)

        val validation = ActionValidator.validate(reply.action)
        assertTrue(validation is ValidationResult.Rejected)
        assertEquals(RejectionReason.INVALID_PARAMS, (validation as ValidationResult.Rejected).reason)
    }

    @Test
    fun testLyingModelActionIsRejected() = runTest {
        val result = provider.generate(ChatRequest(text = "lie"))
        assertTrue(result.isSuccess)
        val reply = result.getOrThrow()
        assertTrue(reply.response.contains("đã đặt báo thức"))

        val validation = ActionValidator.validate(reply.action)
        assertTrue(validation is ValidationResult.Rejected)
        assertEquals(RejectionReason.INVALID_PARAMS, (validation as ValidationResult.Rejected).reason)
    }
}
