package com.thanhnhe00.robot.domain.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RobotStateTest {

    @Test
    fun testNormalLifecycleCycle() {
        var state = RobotState.IDLE

        // 1. Submit text
        state = reduce(state, RobotEvent.SubmitText("Hello"))
        assertEquals(RobotState.PROCESSING, state)

        // 2. Processing finished
        state = reduce(state, RobotEvent.ProcessingFinished)
        assertEquals(RobotState.SPEAKING, state)

        // 3. Speaking finished
        state = reduce(state, RobotEvent.SpeakingFinished)
        assertEquals(RobotState.IDLE, state)
    }

    @Test
    fun testErrorAndDismissCycle() {
        var state = RobotState.IDLE

        state = reduce(state, RobotEvent.ErrorOccurred("Network timeout"))
        assertEquals(RobotState.ERROR, state)

        state = reduce(state, RobotEvent.DismissError)
        assertEquals(RobotState.IDLE, state)
    }

    @Test
    fun testSubmitFromErrorState() {
        val state = reduce(RobotState.ERROR, RobotEvent.SubmitText("Thử lại"))
        assertEquals(RobotState.PROCESSING, state)
    }

    @Test
    fun testIgnoreDuplicateSubmitWhileProcessing() {
        val state = reduce(RobotState.PROCESSING, RobotEvent.SubmitText("Spam click"))
        assertEquals(RobotState.PROCESSING, state)
    }

    @Test
    fun testIgnoreSubmitWhileSpeaking() {
        val state = reduce(RobotState.SPEAKING, RobotEvent.SubmitText("Cắt ngang"))
        assertEquals(RobotState.SPEAKING, state)
    }

    @Test
    fun testInvalidEventsAreIgnoredGracefully() {
        assertEquals(RobotState.IDLE, reduce(RobotState.IDLE, RobotEvent.ProcessingFinished))
        assertEquals(RobotState.IDLE, reduce(RobotState.IDLE, RobotEvent.SpeakingFinished))
        assertEquals(RobotState.IDLE, reduce(RobotState.IDLE, RobotEvent.DismissError))
    }

    @Test
    fun testWakeAndListeningCannotBeReachedInPhase3() {
        val reachableStates = listOf(
            RobotState.IDLE,
            RobotState.PROCESSING,
            RobotState.SPEAKING,
            RobotState.ERROR
        )
        val sampleEvents = listOf(
            RobotEvent.SubmitText("Test"),
            RobotEvent.ProcessingFinished,
            RobotEvent.SpeakingFinished,
            RobotEvent.ErrorOccurred("Test error"),
            RobotEvent.DismissError
        )

        for (s in reachableStates) {
            for (e in sampleEvents) {
                val next = reduce(s, e)
                assertNotEquals("Reachable state must not become WAKE in Phase 3", RobotState.WAKE, next)
                assertNotEquals("Reachable state must not become LISTENING in Phase 3", RobotState.LISTENING, next)
            }
        }
    }
}
