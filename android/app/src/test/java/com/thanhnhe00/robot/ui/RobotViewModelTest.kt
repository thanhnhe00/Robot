package com.thanhnhe00.robot.ui

import com.thanhnhe00.robot.data.provider.AIProvider
import com.thanhnhe00.robot.data.provider.ChatRequest
import com.thanhnhe00.robot.data.provider.ModelReply
import com.thanhnhe00.robot.domain.action.ProposedAction
import com.thanhnhe00.robot.domain.exec.ActionExecutor
import com.thanhnhe00.robot.domain.exec.AlarmScheduler
import com.thanhnhe00.robot.domain.exec.AppLauncher
import com.thanhnhe00.robot.domain.exec.BatteryReader
import com.thanhnhe00.robot.domain.exec.TimeProvider
import com.thanhnhe00.robot.domain.exec.VolumeController
import com.thanhnhe00.robot.domain.state.RobotState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RobotViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeTimeProvider : TimeProvider {
        var callCount = 0
        override fun getCurrentTimeFormatted(): String {
            callCount++
            return "15:00"
        }
    }

    private class FakeBatteryReader : BatteryReader {
        var callCount = 0
        override fun getBatteryPercentage(): Int {
            callCount++
            return 85
        }
    }

    private class FakeAlarmScheduler : AlarmScheduler {
        var callCount = 0
        override fun setAlarm(hour: Int, minute: Int, label: String?): Boolean {
            callCount++
            return true
        }
    }

    private class FakeAppLauncher : AppLauncher {
        var callCount = 0
        override fun isAppInstalled(packageName: String): Boolean = true
        override fun launchApp(packageName: String): Boolean {
            callCount++
            return true
        }
    }

    private class FakeVolumeController : VolumeController {
        var callCount = 0
        override fun setMusicVolume(percent: Int): Boolean {
            callCount++
            return true
        }
        override fun getMusicVolume(): Int = 50
    }

    private class FakeProvider(var replyToReturn: ModelReply) : AIProvider {
        override suspend fun generate(request: ChatRequest): Result<ModelReply> {
            return Result.success(replyToReturn)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testValidActionRequiresApprovalBeforeExecution() = runTest {
        val launcher = FakeAppLauncher()
        val executor = ActionExecutor(
            timeProvider = FakeTimeProvider(),
            batteryReader = FakeBatteryReader(),
            alarmScheduler = FakeAlarmScheduler(),
            appLauncher = launcher,
            volumeController = FakeVolumeController(),
            dispatcher = testDispatcher
        )
        val fakeProvider = FakeProvider(
            ModelReply(
                response = "Mở YouTube nhé.",
                action = ProposedAction(
                    type = "open_app",
                    params = buildJsonObject { put("package", "com.google.android.youtube") }
                )
            )
        )

        val vm = RobotViewModel(
            backendProvider = fakeProvider,
            mockProvider = fakeProvider,
            debugProvider = fakeProvider,
            executor = executor
        )

        // Gửi lệnh
        vm.submitText("Mở YouTube")
        advanceUntilIdle()

        // 1. Kiểm tra chưa được gọi executor tự động (chờ duyệt)
        assertEquals(0, launcher.callCount)
        assertTrue(vm.uiState.value.isAwaitingApproval)
        assertNotNull(vm.uiState.value.pendingAction)

        // 2. Người dùng bấm DUYỆT
        vm.approvePendingAction()
        advanceUntilIdle()

        // 3. Executor đã được gọi đúng 1 lần
        assertEquals(1, launcher.callCount)
        assertNull(vm.uiState.value.pendingAction)
        assertFalse(vm.uiState.value.isAwaitingApproval)
    }

    @Test
    fun testUserRejectPendingActionCancelsExecution() = runTest {
        val launcher = FakeAppLauncher()
        val executor = ActionExecutor(
            timeProvider = FakeTimeProvider(),
            batteryReader = FakeBatteryReader(),
            alarmScheduler = FakeAlarmScheduler(),
            appLauncher = launcher,
            volumeController = FakeVolumeController(),
            dispatcher = testDispatcher
        )
        val fakeProvider = FakeProvider(
            ModelReply(
                response = "Mở YouTube.",
                action = ProposedAction(
                    type = "open_app",
                    params = buildJsonObject { put("package", "com.google.android.youtube") }
                )
            )
        )

        val vm = RobotViewModel(
            backendProvider = fakeProvider,
            mockProvider = fakeProvider,
            debugProvider = fakeProvider,
            executor = executor
        )

        vm.submitText("Mở YouTube")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isAwaitingApproval)

        // Người dùng bấm HỦY / TỪ CHỐI
        vm.rejectPendingAction()
        advanceUntilIdle()

        // Executor tuyệt đối KHÔNG được gọi
        assertEquals(0, launcher.callCount)
        assertNull(vm.uiState.value.pendingAction)
        assertTrue(vm.uiState.value.speechBubble.contains("Đã hủy"))
    }

    @Test
    fun testInvalidActionNeverCallsExecutor() = runTest {
        val launcher = FakeAppLauncher()
        val executor = ActionExecutor(
            timeProvider = FakeTimeProvider(),
            batteryReader = FakeBatteryReader(),
            alarmScheduler = FakeAlarmScheduler(),
            appLauncher = launcher,
            volumeController = FakeVolumeController(),
            dispatcher = testDispatcher
        )
        val fakeProvider = FakeProvider(
            ModelReply(
                response = "Đang hack hệ thống...",
                action = ProposedAction("hack_system", buildJsonObject { })
            )
        )

        val vm = RobotViewModel(
            backendProvider = fakeProvider,
            mockProvider = fakeProvider,
            debugProvider = fakeProvider,
            executor = executor
        )

        vm.submitText("hack")
        advanceUntilIdle()

        // Executor tuyệt đối KHÔNG bao giờ được gọi
        assertEquals(0, launcher.callCount)
        assertNull(vm.uiState.value.pendingAction)
        assertEquals(RobotState.ERROR, vm.uiState.value.robotState)
        assertNotNull(vm.uiState.value.safetyWarning)
    }

    @Test
    fun testLyingModelResponseIsReplacedWithSafeFallback() = runTest {
        val alarm = FakeAlarmScheduler()
        val executor = ActionExecutor(
            timeProvider = FakeTimeProvider(),
            batteryReader = FakeBatteryReader(),
            alarmScheduler = alarm,
            appLauncher = FakeAppLauncher(),
            volumeController = FakeVolumeController(),
            dispatcher = testDispatcher
        )
        // Model khẳng định đã làm, nhưng action gửi kèm sai trái
        val fakeProvider = FakeProvider(
            ModelReply(
                response = "Mình đã đặt báo thức cho bạn lúc 07:30 rồi nhé!",
                action = ProposedAction(
                    type = "set_alarm",
                    params = buildJsonObject { put("time", "99:99") } // Sai giờ
                )
            )
        )

        val vm = RobotViewModel(
            backendProvider = fakeProvider,
            mockProvider = fakeProvider,
            debugProvider = fakeProvider,
            executor = executor
        )

        vm.submitText("lie")
        advanceUntilIdle()

        // Câu trả lời nói dối phải bị thay thế bằng câu an toàn
        assertEquals(RobotViewModel.SAFE_FALLBACK_MESSAGE, vm.uiState.value.speechBubble)
        assertEquals(0, alarm.callCount)
        assertEquals(RobotState.ERROR, vm.uiState.value.robotState)
    }
}
