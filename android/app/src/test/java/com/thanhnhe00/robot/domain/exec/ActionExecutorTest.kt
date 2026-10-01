package com.thanhnhe00.robot.domain.exec

import com.thanhnhe00.robot.domain.action.ValidatedAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionExecutorTest {

    private class FakeTimeProvider(var timeToReturn: String = "Bây giờ là 10:00.") : TimeProvider {
        override fun getCurrentTimeFormatted(): String = timeToReturn
    }

    private class FakeBatteryReader(var batteryPercent: Int = 80) : BatteryReader {
        override fun getBatteryPercentage(): Int = batteryPercent
    }

    private class FakeAlarmScheduler(var success: Boolean = true) : AlarmScheduler {
        var recordedHour: Int? = null
        var recordedMinute: Int? = null
        var recordedLabel: String? = null

        override fun setAlarm(hour: Int, minute: Int, label: String?): Boolean {
            recordedHour = hour
            recordedMinute = minute
            recordedLabel = label
            return success
        }
    }

    private class FakeAppLauncher(
        var isInstalled: Boolean = true,
        var launchSuccess: Boolean = true
    ) : AppLauncher {
        var launchedPackage: String? = null

        override fun isAppInstalled(packageName: String): Boolean = isInstalled

        override fun launchApp(packageName: String): Boolean {
            launchedPackage = packageName
            return launchSuccess
        }
    }

    private class FakeVolumeController(var success: Boolean = true) : VolumeController {
        var recordedPercent: Int? = null
        var currentVolume: Int = 50

        override fun setMusicVolume(percent: Int): Boolean {
            recordedPercent = percent
            currentVolume = percent
            return success
        }

        override fun getMusicVolume(): Int = currentVolume
    }

    private val testDispatcher = StandardTestDispatcher()

    private fun createExecutor(
        timeProvider: TimeProvider = FakeTimeProvider(),
        batteryReader: BatteryReader = FakeBatteryReader(),
        alarmScheduler: AlarmScheduler = FakeAlarmScheduler(),
        appLauncher: AppLauncher = FakeAppLauncher(),
        volumeController: VolumeController = FakeVolumeController(),
        dispatcher: kotlinx.coroutines.CoroutineDispatcher = testDispatcher
    ): ActionExecutor {
        return ActionExecutor(
            timeProvider = timeProvider,
            batteryReader = batteryReader,
            alarmScheduler = alarmScheduler,
            appLauncher = appLauncher,
            volumeController = volumeController,
            dispatcher = dispatcher
        )
    }

    @Test
    fun testExecuteGetTimeSuccess() = runTest(testDispatcher) {
        val timeProvider = FakeTimeProvider("Bây giờ là 14:15.")
        val executor = createExecutor(timeProvider = timeProvider)

        val result = executor.execute(ValidatedAction.GetTime)

        assertTrue(result is ExecutionResult.Success)
        assertEquals("Bây giờ là 14:15.", result.userMessage)
    }

    @Test
    fun testExecuteGetBatterySuccess() = runTest(testDispatcher) {
        val batteryReader = FakeBatteryReader(75)
        val executor = createExecutor(batteryReader = batteryReader)

        val result = executor.execute(ValidatedAction.GetBattery)

        assertTrue(result is ExecutionResult.Success)
        assertEquals("Pin hiện tại còn 75%.", result.userMessage)
    }

    @Test
    fun testExecuteGetBatteryFailureWhenNegative() = runTest(testDispatcher) {
        val batteryReader = FakeBatteryReader(-1)
        val executor = createExecutor(batteryReader = batteryReader)

        val result = executor.execute(ValidatedAction.GetBattery)

        assertTrue(result is ExecutionResult.Failed)
        assertEquals(FailureCategory.EXECUTION_ERROR, (result as ExecutionResult.Failed).category)
    }

    @Test
    fun testExecuteSetAlarmSuccess() = runTest(testDispatcher) {
        val scheduler = FakeAlarmScheduler(success = true)
        val executor = createExecutor(alarmScheduler = scheduler)

        val result = executor.execute(ValidatedAction.SetAlarm(7, 30, "Đi học"))

        assertTrue(result is ExecutionResult.Success)
        assertEquals("Đã mở cài đặt báo thức lúc 07:30 (Đi học).", result.userMessage)
        assertEquals(7, scheduler.recordedHour)
        assertEquals(30, scheduler.recordedMinute)
        assertEquals("Đi học", scheduler.recordedLabel)
    }

    @Test
    fun testExecuteSetAlarmWithoutLabel() = runTest(testDispatcher) {
        val scheduler = FakeAlarmScheduler(success = true)
        val executor = createExecutor(alarmScheduler = scheduler)

        val result = executor.execute(ValidatedAction.SetAlarm(6, 0, null))

        assertTrue(result is ExecutionResult.Success)
        assertEquals("Đã mở cài đặt báo thức lúc 06:00.", result.userMessage)
    }

    @Test
    fun testExecuteOpenAppSuccess() = runTest(testDispatcher) {
        val launcher = FakeAppLauncher(isInstalled = true, launchSuccess = true)
        val executor = createExecutor(appLauncher = launcher)

        val result = executor.execute(ValidatedAction.OpenApp("com.google.android.youtube"))

        assertTrue(result is ExecutionResult.Success)
        assertEquals("Đang mở YouTube...", result.userMessage)
        assertEquals("com.google.android.youtube", launcher.launchedPackage)
    }

    @Test
    fun testExecuteOpenAppNotInstalled() = runTest(testDispatcher) {
        val launcher = FakeAppLauncher(isInstalled = false)
        val executor = createExecutor(appLauncher = launcher)

        val result = executor.execute(ValidatedAction.OpenApp("com.spotify.music"))

        assertTrue(result is ExecutionResult.Failed)
        val failed = result as ExecutionResult.Failed
        assertEquals(FailureCategory.APP_NOT_INSTALLED, failed.category)
        assertTrue(failed.userMessage.contains("Spotify chưa được cài đặt"))
    }

    @Test
    fun testExecuteSetVolumeSuccess() = runTest(testDispatcher) {
        val volumeController = FakeVolumeController(success = true)
        val executor = createExecutor(volumeController = volumeController)

        val result = executor.execute(ValidatedAction.SetVolume(60))

        assertTrue(result is ExecutionResult.Success)
        assertEquals("Đã chỉnh âm lượng về 60%.", result.userMessage)
        assertEquals(60, volumeController.recordedPercent)
    }

    @Test
    fun testSecurityExceptionHandledGracefully() = runTest(testDispatcher) {
        val errorController = object : VolumeController {
            override fun setMusicVolume(percent: Int): Boolean {
                throw SecurityException("Không có quyền MODIFY_AUDIO_SETTINGS")
            }
            override fun getMusicVolume(): Int = 0
        }
        val executor = createExecutor(volumeController = errorController)

        val result = executor.execute(ValidatedAction.SetVolume(50))

        assertTrue(result is ExecutionResult.Failed)
        assertEquals(FailureCategory.PERMISSION_DENIED, (result as ExecutionResult.Failed).category)
        assertTrue(result.userMessage.contains("Thiếu quyền hệ thống"))
    }

    @Test
    fun testActionTimeoutReturnsFailedResult() = kotlinx.coroutines.runBlocking {
        val hangingTimeProvider = object : TimeProvider {
            override fun getCurrentTimeFormatted(): String {
                try {
                    Thread.sleep(1500L)
                } catch (_: InterruptedException) {
                    // Interrupted by runInterruptible
                }
                return "12:00"
            }
        }
        val executor = createExecutor(
            timeProvider = hangingTimeProvider,
            dispatcher = kotlinx.coroutines.Dispatchers.IO
        )

        val result = executor.execute(ValidatedAction.GetTime)

        assertTrue("Quá thời gian phải trả về Failed", result is ExecutionResult.Failed)
        val failed = result as ExecutionResult.Failed
        assertEquals(FailureCategory.TIMEOUT, failed.category)
    }
}
